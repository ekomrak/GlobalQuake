package globalquake.telegram;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import globalquake.client.GlobalQuakeClient;
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
import globalquake.db.entities.TelegramUser;
import globalquake.telegram.data.*;
import globalquake.telegram.abilities.*;
import globalquake.telegram.util.EventImageDrawer;
import globalquake.telegram.util.MapImageDrawer;
import globalquake.telegram.util.TelegramUtils;
import globalquake.utils.GeoUtils;
import globalquake.utils.NamedThreadFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
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
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class TelegramService extends AbilityBot {
    private final Cache<Earthquake, TelegramEarthquakeInfo> earthquakes;
    private final Cache<Cluster, TelegramClusterInfo> clusters;
    private final Cache<ClientStation, TelegramStationInfo> stations;
    private final Cache<Long, SettingsState> userState;
    private final ScheduledExecutorService stationsCheckService;
    private final RandomDataGenerator randomDataGenerator;

    public TelegramService(TelegramClient telegramClient) {
        super(telegramClient, Settings.telegramBotUsername);
        randomDataGenerator = new RandomDataGenerator();

        earthquakes = Caffeine.newBuilder().maximumSize(50).build();
        clusters = Caffeine.newBuilder().maximumSize(50).build();
        stations = Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();
        userState = Caffeine.newBuilder().build();

        stationsCheckService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Telegram stations level analysis"));
        stationsCheckService.scheduleAtFixedRate(this::checkStations, 10000, 200, TimeUnit.MILLISECONDS);

        GlobalQuake.instance.getEventHandler().registerEventListener(new GlobalQuakeEventListener() {
            @Override
            public void onClusterCreate(ClusterCreateEvent event) {
                GlobalQuakeClient.instance.getRegistry().timer("process.duration", "type", "cluster", "operation", "create").record(() -> processClusterCreate(event));
            }

            @Override
            public void onQuakeCreate(QuakeCreateEvent event) {
                GlobalQuakeClient.instance.getRegistry().timer("process.duration", "type", "earthquake", "operation", "create").record(() -> processQuakeCreation(event));
            }

            @Override
            public void onQuakeUpdate(QuakeUpdateEvent event) {
                GlobalQuakeClient.instance.getRegistry().timer("process.duration", "type", "earthquake", "operation", "update").record(() -> processQuakeUpdate(event));
            }

            @Override
            public void onClusterLevelup(ClusterLevelUpEvent event) {
                GlobalQuakeClient.instance.getRegistry().timer("process.duration", "type", "cluster", "operation", "update").record(() -> processClusterLevelup(event));
            }

            @Override
            public void onQuakeArchive(QuakeArchiveEvent event) {
                earthquakes.invalidate(event.earthquake());
            }
        });

        addExtensions(new SubscribeAbility(this),
                new UnsubscribeAbility(this),
                new TestAbility(this),
                new BroadcastAbility(this),
                new FeedbackAbility(this),
                new HelpAbility(this),
                new CountAbility(this),
                new ShowSettingsAbility(this),
                new SettingsAbility(this),
                new DrawMapAbility(this),
                new NotifyUserAbility(this),
                new CacheAbility(this),
                new PrintUserInfoAbility(this),
                new ProcessInputAbility(this),
                new ListEarthquakesAbility(this));
    }

    private void processClusterCreate(ClusterCreateEvent event) {
        TelegramClusterInfo info = new TelegramClusterInfo();
        GlobalQuakeClient.instance.getDatabaseService().listUsersWithClusterAlert().forEach(telegramUser -> {
            double distGCD = GeoUtils.greatCircleDistance(event.cluster().getRootLat(), event.cluster().getRootLon(), telegramUser.getHomeLat(), telegramUser.getHomeLon());
            if (TelegramUtils.canSend(event.cluster(), telegramUser, distGCD)) {
                try {
                    InputFile inputFile = null;
                    boolean sendAsAPhoto = true;
                    if (Boolean.TRUE.equals(telegramUser.getEnableTelegramPossibleShakingImage())) {
                        inputFile = new InputFile(EventImageDrawer.drawEventImage(telegramUser, event.cluster().getRootLat(), event.cluster().getRootLon()), "Cluster_%d.png".formatted(System.currentTimeMillis()));
                        sendAsAPhoto = telegramUser.getSendImageAsAPhoto();
                    }
                    if (Boolean.TRUE.equals(telegramUser.getEnableTelegramPossibleShakingMap())) {
                        inputFile = new InputFile(MapImageDrawer.instance.drawMap(telegramUser), "Cluster_%d.png".formatted(System.currentTimeMillis()));
                        sendAsAPhoto = telegramUser.getSendMapAsAPhoto();
                    }
                    sendMessage(EventType.CLUSTER, telegramUser, info, TelegramUtils.generateClusterMessage(event.cluster(), distGCD), event.cluster().getRootLat(), event.cluster().getRootLon(), inputFile, sendAsAPhoto);
                } catch (IOException e) {
                    Logger.error(e);
                }
            }
        });
        if (!info.getMessages().isEmpty()) {
            info.updateWith(event.cluster());
            clusters.put(event.cluster(), info);
        }
    }

    private void processQuakeCreation(QuakeCreateEvent event) {
        TelegramEarthquakeInfo info = new TelegramEarthquakeInfo();
        GlobalQuakeClient.instance.getDatabaseService().listUsersWithEarthquakeAlert().forEach(telegramUser -> {
            double distGCD = GeoUtils.greatCircleDistance(event.earthquake().getLat(), event.earthquake().getLon(), telegramUser.getHomeLat(), telegramUser.getHomeLon());
            double dist = GeoUtils.geologicalDistance(event.earthquake().getLat(), event.earthquake().getLon(), -event.earthquake().getDepth(), telegramUser.getHomeLat(), telegramUser.getHomeLon(), 0);
            double pga = GeoUtils.pgaFunction(event.earthquake().getMag(), dist, event.earthquake().getDepth());

            if (TelegramUtils.canSend(event.earthquake(), telegramUser, distGCD, pga)) {
                try {
                    InputFile inputFile = null;
                    boolean sendAsAPhoto = true;
                    if (Boolean.TRUE.equals(telegramUser.getEnableTelegramEarthquakeImage())) {
                        inputFile = new InputFile(EventImageDrawer.drawEarthquakeImage(telegramUser, event.earthquake()), "Earthquake_%d.png".formatted(System.currentTimeMillis()));
                        sendAsAPhoto = telegramUser.getSendImageAsAPhoto();
                    }
                    if (Boolean.TRUE.equals(telegramUser.getEnableTelegramEarthquakeMap())) {
                        inputFile = new InputFile(MapImageDrawer.instance.drawMap(telegramUser), "Earthquake_%d.png".formatted(System.currentTimeMillis()));
                        sendAsAPhoto = telegramUser.getSendMapAsAPhoto();
                    }
                    sendMessage(EventType.EARTHQUAKE, telegramUser, info, TelegramUtils.generateEarthquakeMessage(event.earthquake(), distGCD, pga), event.earthquake().getLat(), event.earthquake().getLon(), inputFile, sendAsAPhoto);
                } catch (IOException e) {
                    Logger.error(e);
                }
            }
        });
        if (!info.getMessages().isEmpty()) {
            info.updateWith(event.earthquake());
            earthquakes.put(event.earthquake(), info);
        }
    }

    private void processQuakeUpdate(QuakeUpdateEvent event) {
        TelegramEarthquakeInfo info = earthquakes.getIfPresent(event.earthquake());
        if (info != null) {
            GlobalQuakeClient.instance.getDatabaseService().listUsersWithEarthquakeAlert().forEach(telegramUser -> {
                double distGCD = GeoUtils.greatCircleDistance(event.earthquake().getLat(), event.earthquake().getLon(), telegramUser.getHomeLat(), telegramUser.getHomeLon());
                double dist = GeoUtils.geologicalDistance(event.earthquake().getLat(), event.earthquake().getLon(), -event.earthquake().getDepth(), telegramUser.getHomeLat(), telegramUser.getHomeLon(), 0);
                double pga = GeoUtils.pgaFunction(event.earthquake().getMag(), dist, event.earthquake().getDepth());

                if (TelegramUtils.canSend(event.earthquake(), telegramUser, distGCD, pga)) {
                    if (info.getMessages().containsKey(telegramUser.getChatId())) {
                        if (!info.equalsTo(event.earthquake())) {
                            updateMessage(info.getMessages().get(telegramUser.getChatId()), TelegramUtils.generateEarthquakeMessage(event.earthquake(), distGCD, pga), telegramUser.getChatId());
                        }
                    } else {
                        try {
                            InputFile inputFile = null;
                            boolean sendAsAPhoto = true;
                            if (Boolean.TRUE.equals(telegramUser.getEnableTelegramEarthquakeImage())) {
                                inputFile = new InputFile(EventImageDrawer.drawEarthquakeImage(telegramUser, event.earthquake()), "Earthquake_%d.png".formatted(System.currentTimeMillis()));
                                sendAsAPhoto = telegramUser.getSendImageAsAPhoto();
                            }
                            if (Boolean.TRUE.equals(telegramUser.getEnableTelegramEarthquakeMap())) {
                                inputFile = new InputFile(MapImageDrawer.instance.drawMap(telegramUser), "Earthquake_%d.png".formatted(System.currentTimeMillis()));
                                sendAsAPhoto = telegramUser.getSendMapAsAPhoto();
                            }
                            sendMessage(EventType.EARTHQUAKE, telegramUser, info, TelegramUtils.generateEarthquakeMessage(event.earthquake(), distGCD, pga), event.earthquake().getLat(), event.earthquake().getLon(), inputFile, sendAsAPhoto);
                        } catch (IOException e) {
                            Logger.error(e);
                        }
                    }
                }
            });
            info.updateWith(event.earthquake());
        } else {
            processQuakeCreation(new QuakeCreateEvent(event.earthquake()));
        }
    }

    private void processClusterLevelup(ClusterLevelUpEvent event) {
        TelegramClusterInfo info = clusters.getIfPresent(event.cluster());
        if (info != null) {
            GlobalQuakeClient.instance.getDatabaseService().listUsersWithClusterAlert().forEach(telegramUser -> {
                double distGCD = GeoUtils.greatCircleDistance(event.cluster().getRootLat(), event.cluster().getRootLon(), telegramUser.getHomeLat(), telegramUser.getHomeLon());
                if (TelegramUtils.canSend(event.cluster(), telegramUser, distGCD)) {
                    if (info.getMessages().containsKey(telegramUser.getChatId())) {
                        if (!info.equalsTo(event.cluster())) {
                            updateMessage(info.getMessages().get(telegramUser.getChatId()), TelegramUtils.generateClusterMessage(event.cluster(), distGCD), telegramUser.getChatId());
                        }
                    } else {
                        try {
                            InputFile inputFile = null;
                            boolean sendAsAPhoto = true;
                            if (Boolean.TRUE.equals(telegramUser.getEnableTelegramPossibleShakingImage())) {
                                inputFile = new InputFile(EventImageDrawer.drawEventImage(telegramUser, event.cluster().getRootLat(), event.cluster().getRootLon()), "Cluster_%d.png".formatted(System.currentTimeMillis()));
                                sendAsAPhoto = telegramUser.getSendImageAsAPhoto();
                            }
                            if (Boolean.TRUE.equals(telegramUser.getEnableTelegramPossibleShakingMap())) {
                                inputFile = new InputFile(MapImageDrawer.instance.drawMap(telegramUser), "Cluster_%d.png".formatted(System.currentTimeMillis()));
                                sendAsAPhoto = telegramUser.getSendMapAsAPhoto();
                            }
                            sendMessage(EventType.CLUSTER, telegramUser, info, TelegramUtils.generateClusterMessage(event.cluster(), distGCD), event.cluster().getRootLat(), event.cluster().getRootLon(), inputFile, sendAsAPhoto);
                        } catch (IOException e) {
                            Logger.error(e);
                        }
                    }
                }
            });
            info.updateWith(event.cluster());
        } else {
            processClusterCreate(new ClusterCreateEvent(event.cluster()));
        }
    }

    @Override
    public long creatorId() {
        return Settings.telegramCreatorId;
    }

    public void destroy() {
        GlobalQuake.instance.stopService(stationsCheckService);
    }

    public Cache<Long, SettingsState> getUserState() {
        return userState;
    }

    public void sendTestEarthquake(TelegramUser telegramUser) {
        Hypocenter fakeHypocenter = new Hypocenter((Math.random() * 180.0) - 90.0, (Math.random() * 360.0) - 180.0, randomDataGenerator.nextUniform(0, 100), System.currentTimeMillis(), 0, 0, new DepthConfidenceInterval(0, 2), Collections.emptyList());
        fakeHypocenter.magnitude = randomDataGenerator.nextUniform(1, 8);
        Cluster fakeCluster = new Cluster();
        fakeCluster.setPreviousHypocenter(fakeHypocenter);
        Earthquake fakeEarthquake = new Earthquake(fakeCluster);

        double distGCD = GeoUtils.greatCircleDistance(fakeEarthquake.getLat(), fakeEarthquake.getLon(), telegramUser.getHomeLat(), telegramUser.getHomeLon());
        double dist = GeoUtils.geologicalDistance(fakeEarthquake.getLat(), fakeEarthquake.getLon(), -fakeEarthquake.getDepth(), telegramUser.getHomeLat(), telegramUser.getHomeLon(), 0);
        double pga = GeoUtils.pgaFunction(fakeEarthquake.getMag(), dist, fakeEarthquake.getDepth());

        try {
            InputFile inputFile = null;
            boolean sendAsAPhoto = true;
            if (Boolean.TRUE.equals(telegramUser.getEnableTelegramEarthquakeImage())) {
                inputFile = new InputFile(EventImageDrawer.drawEarthquakeImage(telegramUser, fakeEarthquake), "Fake_Earthquake_%d.png".formatted(System.currentTimeMillis()));
                sendAsAPhoto = telegramUser.getSendImageAsAPhoto();
            }
            if (Boolean.TRUE.equals(telegramUser.getEnableTelegramEarthquakeMap())) {
                inputFile = new InputFile(MapImageDrawer.instance.drawMap(telegramUser), "Fake_Earthquake_%d.png".formatted(System.currentTimeMillis()));
                sendAsAPhoto = telegramUser.getSendMapAsAPhoto();
            }
            sendMessage(EventType.EARTHQUAKE, telegramUser, null, TelegramUtils.generateEarthquakeMessage(fakeEarthquake, distGCD, pga, true), fakeEarthquake.getLat(), fakeEarthquake.getLon(), inputFile, sendAsAPhoto);
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    private void checkStations() {
        GlobalStationManager stationManager = GlobalQuakeClient.instance.getStationManager();
        if (stationManager != null && CollectionUtils.isNotEmpty(stationManager.getStations())) {
            stationManager.getStations().forEach(abstractStation -> {
                if (abstractStation instanceof ClientStation clientStation) {
                    GlobalQuakeClient.instance.getRegistry().timer("process.duration", "type", "station", "operation", "both").record(() -> processStation(clientStation));
                }
            });
        }
    }

    private void processStation(ClientStation clientStation) {
        double intensity = clientStation.getMaxRatio60S();
        TelegramStationInfo info = stations.getIfPresent(clientStation);
        if (info != null) {
            GlobalQuakeClient.instance.getDatabaseService().listUsersWithStationAlert().forEach(telegramUser -> {
                double distGCD = GeoUtils.greatCircleDistance(clientStation.getLatitude(), clientStation.getLongitude(), telegramUser.getHomeLat(), telegramUser.getHomeLon());

                if (TelegramUtils.canSend(intensity, telegramUser, distGCD)) {
                    if (info.getMessages().containsKey(telegramUser.getChatId())) {
                        if (!info.equalsTo(clientStation)) {
                            //TODO: Temporary disabled
                            //updateMessage(info.getMessages().get(telegramUser.getChatId()), TelegramUtils.generateStationMessage(clientStation, distGCD), telegramUser.getChatId());
                        }
                    } else {
                        try {
                            InputFile inputFile = null;
                            boolean sendAsAPhoto = true;
                            if (Boolean.TRUE.equals(telegramUser.getEnableTelegramStationHighIntensityImage())) {
                                inputFile = new InputFile(EventImageDrawer.drawEventImage(telegramUser, clientStation.getLatitude(), clientStation.getLongitude()), "Station_%d.png".formatted(System.currentTimeMillis()));
                                sendAsAPhoto = telegramUser.getSendImageAsAPhoto();
                            }
                            if (Boolean.TRUE.equals(telegramUser.getEnableTelegramStationHighIntensityMap())) {
                                inputFile = new InputFile(MapImageDrawer.instance.drawMap(telegramUser), "Station_%d.png".formatted(System.currentTimeMillis()));
                                sendAsAPhoto = telegramUser.getSendMapAsAPhoto();
                            }
                            sendMessage(EventType.STATION, telegramUser, info, TelegramUtils.generateStationMessage(clientStation, intensity, distGCD), clientStation.getLatitude(), clientStation.getLongitude(), inputFile, sendAsAPhoto);
                        } catch (IOException e) {
                            Logger.error(e);
                        }
                    }
                }
            });
            info.updateWith(clientStation);
        } else {
            TelegramStationInfo newInfo = new TelegramStationInfo();
            GlobalQuakeClient.instance.getDatabaseService().listUsersWithStationAlert().forEach(telegramUser -> {
                double distGCD = GeoUtils.greatCircleDistance(clientStation.getLatitude(), clientStation.getLongitude(), telegramUser.getHomeLat(), telegramUser.getHomeLon());
                if (TelegramUtils.canSend(intensity, telegramUser, distGCD)) {
                    try {
                        InputFile inputFile = null;
                        boolean sendAsAPhoto = true;
                        if (Boolean.TRUE.equals(telegramUser.getEnableTelegramStationHighIntensityImage())) {
                            inputFile = new InputFile(EventImageDrawer.drawEventImage(telegramUser, clientStation.getLatitude(), clientStation.getLongitude()), "Station_%d.png".formatted(System.currentTimeMillis()));
                            sendAsAPhoto = telegramUser.getSendImageAsAPhoto();
                        }
                        if (Boolean.TRUE.equals(telegramUser.getEnableTelegramStationHighIntensityMap())) {
                            inputFile = new InputFile(MapImageDrawer.instance.drawMap(telegramUser), "Station_%d.png".formatted(System.currentTimeMillis()));
                            sendAsAPhoto = telegramUser.getSendMapAsAPhoto();
                        }
                        sendMessage(EventType.STATION, telegramUser, newInfo, TelegramUtils.generateStationMessage(clientStation, intensity, distGCD), clientStation.getLatitude(), clientStation.getLongitude(), inputFile, sendAsAPhoto);
                    } catch (IOException e) {
                        Logger.error(e);
                    }
                }
            });
            if (!newInfo.getMessages().isEmpty()) {
                newInfo.updateWith(clientStation);
                stations.put(clientStation, newInfo);
            }
        }
    }

    private void sendMessage(EventType eventType, TelegramUser user, TelegramAbstractInfo<?> info, String text, double lat, double lon, InputFile inputFile, boolean sendAsAPhoto) {
        try {
            Message message = telegramClient.execute(SendMessage.builder().chatId(user.getChatId()).text(text).parseMode(ParseMode.HTML).build());
            if (info != null) {
                info.getMessages().put(user.getChatId(), message.getMessageId());
            }

            if ((eventType == EventType.EARTHQUAKE && user.getEnableTelegramEarthquakeLocation()) ||
                    (eventType == EventType.CLUSTER && user.getEnableTelegramPossibleShakingLocation()) ||
                    (eventType == EventType.STATION && user.getEnableTelegramStationHighIntensityLocation())) {
                telegramClient.execute(SendLocation.builder().chatId(user.getChatId()).latitude(lat).longitude(lon).replyToMessageId(message.getMessageId()).build());
            }
            if (inputFile != null) {
                if (sendAsAPhoto) {
                    telegramClient.execute(SendPhoto.builder().chatId(user.getChatId()).photo(inputFile).replyToMessageId(message.getMessageId()).build());
                } else {
                    telegramClient.execute(SendDocument.builder().chatId(user.getChatId()).document(inputFile).replyToMessageId(message.getMessageId()).build());
                }
            }
            GlobalQuakeClient.instance.getRegistry().counter("message.sent", "user", user.getId().toString(), "type", eventType.toString(), "operation", "create").increment();
        } catch (TelegramApiException e) {
            if (e.getMessage().contains("[403] Forbidden: bot was blocked by the user") || e.getMessage().contains("[403] Forbidden: user is deactivated")) {
                user.setEnabled(false);
                GlobalQuakeClient.instance.getDatabaseService().updateTelegramUser(user);
            }
            Logger.error("Flow: %s".formatted(eventType.name()));
            Logger.error("Send: ChatId: %d".formatted(user.getChatId()));
            Logger.error(e);
        }
    }

    private void updateMessage(Integer messageId, String text, Long chatId) {
        if (messageId != null) {
            try {
                telegramClient.execute(EditMessageText.builder().chatId(chatId).messageId(messageId).text(text).parseMode(ParseMode.HTML).build());
            } catch (TelegramApiException e) {
                Logger.error("Update: ChatId: %d".formatted(chatId));
                Logger.error(e);
            }
        }
    }
}
