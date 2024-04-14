package globalquake.telegram;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import globalquake.client.GlobalQuakeLocal;
import globalquake.client.data.ClientStation;
import globalquake.core.GlobalQuake;
import globalquake.core.Settings;
import globalquake.core.earthquake.data.Cluster;
import globalquake.core.earthquake.data.Earthquake;
import globalquake.core.earthquake.data.Hypocenter;
import globalquake.core.earthquake.interval.DepthConfidenceInterval;
import globalquake.core.events.GlobalQuakeEventListener;
import globalquake.core.events.specific.*;
import globalquake.core.station.GlobalStationManager;
import globalquake.telegram.abilities.*;
import globalquake.telegram.data.TelegramAbstractInfo;
import globalquake.telegram.data.TelegramClusterInfo;
import globalquake.telegram.data.TelegramEarthquakeInfo;
import globalquake.telegram.data.TelegramStationInfo;
import globalquake.telegram.util.EventImageDrawer;
import globalquake.telegram.util.MapImageDrawer;
import globalquake.telegram.util.TelegramUtils;
import globalquake.utils.GeoUtils;
import globalquake.utils.NamedThreadFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class TelegramService extends AbilityBot {
    private final RandomDataGenerator randomDataGenerator;

    private final Cache<Earthquake, TelegramEarthquakeInfo> earthquakes;
    private final Cache<Cluster, TelegramClusterInfo> clusters;
    private final Cache<ClientStation, TelegramStationInfo> stations;
    private final ScheduledExecutorService stationsCheckService;
    private SettingsState settingsState;

    public TelegramService(TelegramClient telegramClient) {
        super(telegramClient, Settings.telegramBotUsername);
        randomDataGenerator = new RandomDataGenerator();

        earthquakes = Caffeine.newBuilder().maximumSize(50).build();
        clusters = Caffeine.newBuilder().maximumSize(50).build();
        stations = Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();

        settingsState = SettingsState.NONE;

        stationsCheckService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Telegram stations level analysis"));
        stationsCheckService.scheduleAtFixedRate(this::checkStations, 10000, 200, TimeUnit.MILLISECONDS);

        GlobalQuake.instance.getEventHandler().registerEventListener(new GlobalQuakeEventListener() {
            @Override
            public void onClusterCreate(ClusterCreateEvent event) {
                double distGCD = GeoUtils.greatCircleDistance(event.cluster().getRootLat(), event.cluster().getRootLon(), Settings.homeLat, Settings.homeLon);
                if (TelegramUtils.canSend(event.cluster(), distGCD)) {
                    TelegramClusterInfo info = new TelegramClusterInfo(event.cluster());

                    try {
                        InputFile inputFile = null;
                        if (Boolean.TRUE.equals(Settings.enableTelegramPossibleShakingImage)) {
                            inputFile = new InputFile(EventImageDrawer.drawEventImage(event.cluster().getRootLat(), event.cluster().getRootLon()), "Cluster.png");
                        }
                        if (Boolean.TRUE.equals(Settings.enableTelegramPossibleShakingMap)) {
                            inputFile = new InputFile(MapImageDrawer.instance.drawMap(), "Cluster.png");
                        }
                        sendMessage(EventType.CLUSTER, info, TelegramUtils.generateClusterMessage(event.cluster(), distGCD), event.cluster().getRootLat(), event.cluster().getRootLon(), inputFile);
                        clusters.put(event.cluster(), info);
                    } catch (IOException e) {
                        Logger.error(e);
                    }
                }
            }

            @Override
            public void onQuakeCreate(QuakeCreateEvent event) {
                double distGCD = GeoUtils.greatCircleDistance(event.earthquake().getLat(), event.earthquake().getLon(), Settings.homeLat, Settings.homeLon);
                double dist = GeoUtils.geologicalDistance(event.earthquake().getLat(), event.earthquake().getLon(), -event.earthquake().getDepth(), Settings.homeLat, Settings.homeLon, 0);
                double pga = GeoUtils.pgaFunction(event.earthquake().getMag(), dist, event.earthquake().getDepth());

                if (TelegramUtils.canSend(event.earthquake(), distGCD, pga)) {
                    TelegramEarthquakeInfo info = new TelegramEarthquakeInfo(event.earthquake());

                    try {
                        InputFile inputFile = null;
                        if (Boolean.TRUE.equals(Settings.enableTelegramEarthquakeImage)) {
                            inputFile = new InputFile(EventImageDrawer.drawEarthquakeImage(event.earthquake()), "Earthquake.png");
                        }
                        if (Boolean.TRUE.equals(Settings.enableTelegramEarthquakeMap)) {
                            inputFile = new InputFile(MapImageDrawer.instance.drawMap(), "Earthquake.png");
                        }
                        sendMessage(EventType.EARTHQUAKE, info, TelegramUtils.generateEarthquakeMessage(event.earthquake(), distGCD, pga), event.earthquake().getLat(), event.earthquake().getLon(), inputFile);
                        earthquakes.put(event.earthquake(), info);
                    } catch (IOException e) {
                        Logger.error(e);
                    }
                }
            }

            @Override
            public void onQuakeUpdate(QuakeUpdateEvent event) {
                TelegramEarthquakeInfo info = earthquakes.getIfPresent(event.earthquake());
                if (info != null) {
                    double distGCD = GeoUtils.greatCircleDistance(event.earthquake().getLat(), event.earthquake().getLon(), Settings.homeLat, Settings.homeLon);
                    double dist = GeoUtils.geologicalDistance(event.earthquake().getLat(), event.earthquake().getLon(), -event.earthquake().getDepth(), Settings.homeLat, Settings.homeLon, 0);
                    double pga = GeoUtils.pgaFunction(event.earthquake().getMag(), dist, event.earthquake().getDepth());
                    if (TelegramUtils.canSend(event.earthquake(), distGCD, pga) && (!info.equalsTo(event.earthquake()))) {
                        updateMessage(TelegramUtils.generateEarthquakeMessage(event.earthquake(), distGCD, pga), info.getMessageId());
                        info.updateWith(event.earthquake());
                    }
                } else {
                    onQuakeCreate(new QuakeCreateEvent(event.earthquake()));
                }
            }

            @Override
            public void onClusterLevelup(ClusterLevelUpEvent event) {
                TelegramClusterInfo info = clusters.getIfPresent(event.cluster());
                if (info != null) {
                    double distGCD = GeoUtils.greatCircleDistance(event.cluster().getRootLat(), event.cluster().getRootLon(), Settings.homeLat, Settings.homeLon);
                    if (TelegramUtils.canSend(event.cluster(), distGCD) && (!info.equalsTo(event.cluster()))) {
                        updateMessage(TelegramUtils.generateClusterMessage(event.cluster(), distGCD), info.getMessageId());
                        info.updateWith(event.cluster());
                    }
                } else {
                    onClusterCreate(new ClusterCreateEvent(event.cluster()));
                }
            }

            @Override
            public void onQuakeArchive(QuakeArchiveEvent event) {
                earthquakes.invalidate(event.earthquake());
            }
        });

        addExtensions(new TestAbility(this),
                new ShowSettingsAbility(this),
                new SettingsAbility(this),
                new DrawMapAbility(this),
                new ProcessInputAbility(this));
    }

    public SettingsState getSettingsState() {
        return settingsState;
    }

    public void setSettingsState(SettingsState settingsState) {
        this.settingsState = settingsState;
    }

    @Override
    public long creatorId() {
        return Settings.telegramCreatorId;
    }

    public void destroy() {
        GlobalQuake.instance.stopService(stationsCheckService);
    }

    public void sendTestEarthquake() {
        Hypocenter fakeHypocenter = new Hypocenter((Math.random() * 180.0) - 90.0, (Math.random() * 360.0) - 180.0, randomDataGenerator.nextUniform(0, 100), System.currentTimeMillis(), 0, 0, new DepthConfidenceInterval(0, 2), Collections.emptyList());
        fakeHypocenter.magnitude = randomDataGenerator.nextUniform(1, 8);
        Cluster fakeCluster = new Cluster();
        fakeCluster.setPreviousHypocenter(fakeHypocenter);
        Earthquake fakeEarthquake = new Earthquake(fakeCluster);

        double distGCD = GeoUtils.greatCircleDistance(fakeEarthquake.getLat(), fakeEarthquake.getLon(), Settings.homeLat, Settings.homeLon);
        double dist = GeoUtils.geologicalDistance(fakeEarthquake.getLat(), fakeEarthquake.getLon(), -fakeEarthquake.getDepth(), Settings.homeLat, Settings.homeLon, 0);
        double pga = GeoUtils.pgaFunction(fakeEarthquake.getMag(), dist, fakeEarthquake.getDepth());
        try {
            InputFile inputFile = null;
            if (Boolean.TRUE.equals(Settings.enableTelegramEarthquakeImage)) {
                inputFile = new InputFile(EventImageDrawer.drawEarthquakeImage(fakeEarthquake), "Earthquake.png");
            }
            if (Boolean.TRUE.equals(Settings.enableTelegramEarthquakeMap)) {
                inputFile = new InputFile(MapImageDrawer.instance.drawMap(), "Earthquake.png");
            }
            sendMessage(EventType.EARTHQUAKE, null, TelegramUtils.generateEarthquakeMessage(fakeEarthquake, distGCD, pga), fakeEarthquake.getLat(), fakeEarthquake.getLon(), inputFile);
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    private void checkStations() {
        if (Boolean.FALSE.equals(Settings.enableTelegramStationHighIntensityAlert)) {
            return;
        }
        GlobalStationManager stationManager = GlobalQuakeLocal.instance.getStationManager();
        if (stationManager != null && CollectionUtils.isNotEmpty(stationManager.getStations())) {
            stationManager.getStations().forEach(abstractStation -> {
                if (abstractStation instanceof ClientStation clientStation) {
                    double distGCD = GeoUtils.greatCircleDistance(clientStation.getLatitude(), clientStation.getLongitude(), Settings.homeLat, Settings.homeLon);
                    TelegramStationInfo info = stations.getIfPresent(clientStation);
                    if (info != null) {
                        if (TelegramUtils.canSend(clientStation, distGCD) && !info.equalsTo(clientStation)) {
                            //updateMessage(TelegramUtils.generateStationMessage(clientStation, distGCD), info.getMessageId());
                            info.updateWith(clientStation);
                        }
                    } else {
                        if (TelegramUtils.canSend(clientStation, distGCD)) {
                            TelegramStationInfo newInfo = new TelegramStationInfo(clientStation);
                            try {
                                InputFile inputFile = null;
                                if (Boolean.TRUE.equals(Settings.enableTelegramStationHighIntensityImage)) {
                                    inputFile = new InputFile(EventImageDrawer.drawEventImage(clientStation.getLatitude(), clientStation.getLongitude()), "Station.png");
                                }
                                if (Boolean.TRUE.equals(Settings.enableTelegramStationHighIntensityMap)) {
                                    inputFile = new InputFile(MapImageDrawer.instance.drawMap(), "Station.png");
                                }
                                sendMessage(EventType.STATION, info, TelegramUtils.generateStationMessage(clientStation, distGCD), clientStation.getLatitude(), clientStation.getLongitude(), inputFile);
                                stations.put(clientStation, newInfo);
                            } catch (IOException e) {
                                Logger.error(e);
                            }
                        }
                    }
                }
            });
        }
    }

    private void sendMessage(EventType eventType, TelegramAbstractInfo<?> info, String text, double lat, double lon, InputFile inputFile) {
        try {
            SendMessage sendMessage = SendMessage.builder().chatId(Settings.telegramChatId).text(text).parseMode(ParseMode.HTML).build();
            if (Settings.telegramMessageThreadId != 0) {
                sendMessage.setMessageThreadId(Settings.telegramMessageThreadId);
            }

            Message message = telegramClient.execute(sendMessage);
            if (info != null) {
                info.setMessageId(message.getMessageId());
            }

            if ((eventType == EventType.EARTHQUAKE && Settings.enableTelegramEarthquakeLocation) ||
                    (eventType == EventType.CLUSTER && Settings.enableTelegramPossibleShakingLocation) ||
                    (eventType == EventType.STATION && Settings.enableTelegramStationHighIntensityLocation)) {
                SendLocation sendLocation = SendLocation.builder().chatId(Settings.telegramChatId).latitude(lat).longitude(lon).replyToMessageId(message.getMessageId()).build();
                if (Settings.telegramMessageThreadId != 0) {
                    sendLocation.setMessageThreadId(Settings.telegramMessageThreadId);
                }
                telegramClient.execute(sendLocation);
            }

            if (inputFile != null) {
                telegramClient.execute(SendPhoto.builder().chatId(Settings.telegramChatId).photo(inputFile).replyToMessageId(message.getMessageId()).build());
            }
        } catch (TelegramApiException e) {
            Logger.error(e);
        }
    }

    private void updateMessage(String text, Integer messageId) {
        if (messageId != null) {
            try {
                telegramClient.execute(EditMessageText.builder().chatId(Settings.telegramChatId).messageId(messageId).text(text).parseMode(ParseMode.HTML).build());
            } catch (TelegramApiException e) {
                Logger.error(e);
            }
        }
    }
}
