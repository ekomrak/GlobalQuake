package globalquake.speech;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import globalquake.client.GlobalQuakeLocal;
import globalquake.client.data.ClientStation;
import globalquake.core.GlobalQuake;
import globalquake.core.Settings;
import globalquake.core.earthquake.data.Cluster;
import globalquake.core.earthquake.data.Earthquake;
import globalquake.core.events.GlobalQuakeEventListener;
import globalquake.core.events.specific.ClusterCreateEvent;
import globalquake.core.events.specific.ClusterLevelUpEvent;
import globalquake.core.events.specific.QuakeCreateEvent;
import globalquake.core.events.specific.QuakeUpdateEvent;
import globalquake.core.intensity.IntensityScales;
import globalquake.core.station.GlobalStationManager;
import globalquake.utils.GeoUtils;
import globalquake.utils.NamedThreadFactory;
import org.apache.commons.collections.CollectionUtils;
import org.tinylog.Logger;

import javax.sound.sampled.*;
import javax.speech.AudioException;
import javax.speech.Central;
import javax.speech.EngineException;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class SpeechAndSoundService {
    private final ScheduledExecutorService stationsCheckService;
    private final Cache<Earthquake, Integer> earthquakesSpeech;
    private final Cache<Cluster, Integer> clustersSpeech;
    //private final Cache<Cluster, Integer> clustersSound;
    private final Cache<ClientStation, Double> stationsSpeech;
    //private final Cache<AbstractStation, Double> stationsSound;
    private Synthesizer synthesizer;
    private final VoiceSelectionParams voice;
    private final AudioConfig audioConfig;

    public SpeechAndSoundService() {
        stationsCheckService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Speech and Sound stations level analysis"));
        stationsCheckService.scheduleAtFixedRate(this::checkStations, 10000, 200, TimeUnit.MILLISECONDS);

        earthquakesSpeech = Caffeine.newBuilder().maximumSize(20).build();
        clustersSpeech = Caffeine.newBuilder().maximumSize(20).build();
        stationsSpeech = Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();

        try {
            System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
            Central.registerEngineCentral("com.sun.speech.freetts.jsapi.FreeTTSEngineCentral");
            synthesizer = Central.createSynthesizer(new SynthesizerModeDesc(Locale.US));
            synthesizer.allocate();
            synthesizer.resume();
        } catch (EngineException | AudioException e) {
            Logger.error(e);
        }

        voice = VoiceSelectionParams.newBuilder().setLanguageCode("ru-RU").setSsmlGender(SsmlVoiceGender.MALE).build();
        audioConfig = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).build();

        GlobalQuake.instance.getEventHandler().registerEventListener(new GlobalQuakeEventListener() {
            @Override
            public void onClusterCreate(ClusterCreateEvent event) {
                double distGCD = GeoUtils.greatCircleDistance(event.cluster().getRootLat(), event.cluster().getRootLon(), Settings.homeLat, Settings.homeLon);
                /*if (canPlay(event.cluster())) {
                    play(event.cluster());
                    clustersSound.put(event.cluster(), 0);
                }*/
                if (canSpeak(event.cluster(), distGCD)) {
                    speak(generateClusterText(event.cluster(), distGCD));
                    clustersSpeech.put(event.cluster(), 0);
                }
            }

            @Override
            public void onQuakeCreate(QuakeCreateEvent event) {
                double distGCD = GeoUtils.greatCircleDistance(event.earthquake().getLat(), event.earthquake().getLon(), Settings.homeLat, Settings.homeLon);
                double dist = GeoUtils.geologicalDistance(event.earthquake().getLat(), event.earthquake().getLon(), -event.earthquake().getDepth(), Settings.homeLat, Settings.homeLon, 0);
                double pga = GeoUtils.pgaFunction(event.earthquake().getMag(), dist, event.earthquake().getDepth());
                /*if (canPlay(event.earthquake())) {
                    play(event.earthquake());
                }*/
                if (canSpeak(event.earthquake(), distGCD, pga)) {
                    speak(generateEarthquakeText(event.earthquake(), distGCD));
                    earthquakesSpeech.put(event.earthquake(), 0);
                }
            }

            @Override
            public void onQuakeUpdate(QuakeUpdateEvent event) {
                Integer flag = earthquakesSpeech.getIfPresent(event.earthquake());
                if (flag == null) {
                    onQuakeCreate(new QuakeCreateEvent(event.earthquake()));
                }
            }

            @Override
            public void onClusterLevelup(ClusterLevelUpEvent event) {
                Integer flag = clustersSpeech.getIfPresent(event.cluster());
                if (flag == null) {
                    onClusterCreate(new ClusterCreateEvent(event.cluster()));
                }
            }
        });
    }

    public void destroy() {
        GlobalQuake.instance.stopService(stationsCheckService);

        if (synthesizer != null) {
            try {
                synthesizer.deallocate();
            } catch (EngineException e) {
                Logger.error(e);
            }
        }
    }

    private void checkStations() {
        if (Boolean.FALSE.equals(Settings.enableSpeechStationHighIntensityAlert)) {
            return;
        }
        GlobalStationManager stationManager = GlobalQuakeLocal.instance.getStationManager();
        if (stationManager != null && CollectionUtils.isNotEmpty(stationManager.getStations())) {
            stationManager.getStations().forEach(abstractStation -> {
                if (abstractStation instanceof ClientStation clientStation) {
                    double distGCD = GeoUtils.greatCircleDistance(clientStation.getLatitude(), clientStation.getLongitude(), Settings.homeLat, Settings.homeLon);
                    Double intensity = stationsSpeech.getIfPresent(clientStation);
                    if (intensity == null && canSpeak(clientStation, distGCD)) {
                        speak(generateStationMessage(clientStation, distGCD));
                        stationsSpeech.put(clientStation, clientStation.getMaxRatio60S());
                    }
                }
            });
        }
    }

    private boolean canSpeak(Earthquake earthquake, double distGCD, double pga) {
        if (Boolean.FALSE.equals(Settings.enableSpeechEarthquakeAlert)) {
            return false;
        }
        return canAlert(earthquake, distGCD, pga);
    }

    private boolean canSpeak(Cluster cluster, double distGCD) {
        if (Boolean.FALSE.equals(Settings.enableSpeechPossibleShakingAlert)) {
            return false;
        }
        return canAlert(cluster, distGCD);
    }

    private boolean canSpeak(ClientStation clientStation, double distGCD) {
        if (Boolean.FALSE.equals(Settings.enableSpeechStationHighIntensityAlert)) {
            return false;
        }
        return canAlert(clientStation, distGCD);
    }

    /*private boolean canPlay(Earthquake earthquake) {
        if (earthquake == null || !Settings.enableSoundEarthquakeAlert) {
            return false;
        }
        return canAlert(earthquake);
    }

    private boolean canPlay(Cluster cluster) {
        if (cluster == null || !Settings.enableSoundPossibleShakingAlert) {
            return false;
        }
        return canAlert(cluster);
    }*/

    private boolean canAlert(Earthquake earthquake, double distGCD, double pga) {
        double earthquakeThreshold = IntensityScales.INTENSITY_SCALES[Settings.tsEarthquakeIntensityScale].getLevels().get(Settings.tsEarthquakeMinIntensity).getPga();
        return (((earthquake.getMag() >= Settings.tsEarthquakeMinMagnitudeArea1) && (distGCD <= Settings.tsEarthquakeMaxDistArea1)) || ((earthquake.getMag() >= Settings.tsEarthquakeMinMagnitudeArea2) && (distGCD <= Settings.tsEarthquakeMaxDistArea2)) || (pga >= earthquakeThreshold));
    }

    private boolean canAlert(Cluster cluster, double distGCD) {
        return (distGCD <= Settings.tsPossibleShakingMaxDist) && (cluster.getLevel() >= Settings.tsPossibleShakingMinLevel);
    }

    private boolean canAlert(ClientStation clientStation, double distGCD) {
        return (distGCD <= Settings.tsStationMaxDist1) && (clientStation.getMaxRatio60S() >= Settings.tsStationMinIntensity1);
    }

    private String generateEarthquakeText(Earthquake earthquake, double distGCD) {
        if (Boolean.TRUE.equals(Settings.useFreeTts)) {
            return "Earthquake detected! %.1f, distance %d, %s.".formatted(earthquake.getMag(), (int) distGCD, earthquake.getRegion());
        } else {
            return "Землетрясение обнаружено! %.1f, расстояние %d, %s.".formatted(earthquake.getMag(), (int) distGCD, earthquake.getRegion());
        }
    }

    private String generateClusterText(Cluster cluster, double distGCD) {
        if (Boolean.TRUE.equals(Settings.useFreeTts)) {
            return "Possible earthquake detected! Level %d, distance %d.".formatted(cluster.getLevel(), (int) distGCD);
        } else {
            return "Возможное землетрясение обнаружено! Уровень %d, расстояние %d.".formatted(cluster.getLevel(), (int) distGCD);
        }
    }

    private String generateStationMessage(ClientStation station, double distGCD) {
        if (Boolean.TRUE.equals(Settings.useFreeTts)) {
            return "High station intensity! Level %d, distance %d".formatted((int) station.getMaxRatio60S(), (int) distGCD);
        } else {
            return "Высокий уровень датчика! Уровень %d, расстояние %d".formatted((int) station.getMaxRatio60S(), (int) distGCD);
        }
    }

    private void speak(String text) {
        if (Boolean.TRUE.equals(Settings.useFreeTts)) {
            if (synthesizer != null) {
                try {
                    synthesizer.speakPlainText(text, null);
                    synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
                } catch (InterruptedException e) {
                    Logger.error(e);
                }
            }
        } else {
            try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
                SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();
                SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
                ByteString audioContents = response.getAudioContent();

                InputStream audioInStream = new ByteArrayInputStream(audioContents.toByteArray());
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(new BufferedInputStream(audioInStream));

                AudioInputStream din;
                if (audioIn != null) {
                    AudioFormat baseFormat = audioIn.getFormat();
                    AudioFormat decodedFormat = new AudioFormat(
                            AudioFormat.Encoding.PCM_SIGNED,
                            baseFormat.getSampleRate(),
                            16,
                            baseFormat.getChannels(),
                            baseFormat.getChannels() * 2,
                            baseFormat.getSampleRate(),
                            false);
                    din = AudioSystem.getAudioInputStream(decodedFormat, audioIn);

                    rawPlay(decodedFormat, din);
                    audioIn.close();
                }
            } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
                Logger.error(e);
            }
        }
    }

    /*private void play(Earthquake earthquake) {
        if (earthquake.getMag() <= 5.0) {
            Sounds.playSound(Sounds.felt);
        } else {
            Sounds.playSound(Sounds.felt);
            //Sounds.playSound(Sounds.felt_strong);
        }
    }

    private void play(Cluster cluster) {
        switch (cluster.getLevel()) {
            case 0: {
                Sounds.playSound(Sounds.level_0);
                break;
            }
            case 1: {
                Sounds.playSound(Sounds.level_1);
                break;
            }
            case 2: {
                Sounds.playSound(Sounds.level_2);
                break;
            }
            case 3: {
                Sounds.playSound(Sounds.level_3);
                break;
            }
            case 4: {
                Sounds.playSound(Sounds.level_4);
                break;
            }
            default:
                Sounds.playSound(Sounds.level_0);
                break;
        }
    }*/

    private SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        SourceDataLine res = (SourceDataLine) AudioSystem.getLine(info);
        res.open(audioFormat);
        return res;
    }

    private void rawPlay(AudioFormat targetFormat, AudioInputStream din) throws IOException, LineUnavailableException {
        byte[] data = new byte[4096];
        SourceDataLine line = getLine(targetFormat);
        line.start();
        int nBytesRead = 0;
        while (nBytesRead != -1) {
            nBytesRead = din.read(data, 0, data.length);
            if (nBytesRead != -1) {
                line.write(data, 0, nBytesRead);
            }
        }

        line.drain();
        line.stop();
        line.close();
        din.close();
    }
}
