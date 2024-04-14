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
import globalquake.core.intensity.IntensityScales;
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
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
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
                new DrawMapAbility(this));
    }

    @Override
    public void consume(Update update) {
        super.consume(update);
        if (update.hasCallbackQuery()) {
            String callData = update.getCallbackQuery().getData();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            switch (callData) {
                case "settings" -> navigateToSettings(chatId, messageId);
                case "home_settings" -> navigateToHomeSettings(chatId, messageId);
                case "home_lat" -> {
                    settingsState = SettingsState.HOME_LAT;
                    getSilent().forceReply("Введите значение для:\nШирота:", chatId);
                }
                case "home_lon" -> {
                    settingsState = SettingsState.HOME_LON;
                    getSilent().forceReply("Введите значение для:\nДолгота:", chatId);
                }
                case "earthquake_settings" -> navigateToEarthquakeSettings(chatId, messageId);
                case "earthquake_alert" -> {
                    Settings.enableTelegramEarthquakeAlert = !Settings.enableTelegramEarthquakeAlert;
                    Settings.save();
                    navigateToEarthquakeSettings(chatId, messageId);
                }
                case "earthquake_location" -> {
                    Settings.enableTelegramEarthquakeLocation = !Settings.enableTelegramEarthquakeLocation;
                    Settings.save();
                    navigateToEarthquakeSettings(chatId, messageId);
                }
                case "earthquake_image" -> {
                    Settings.enableTelegramEarthquakeImage = !Settings.enableTelegramEarthquakeImage;
                    Settings.save();
                    navigateToEarthquakeSettings(chatId, messageId);
                }
                case "earthquake_map" -> {
                    Settings.enableTelegramEarthquakeMap = !Settings.enableTelegramEarthquakeMap;
                    Settings.save();
                    navigateToEarthquakeSettings(chatId, messageId);
                }
                case "earthquake_distance_1" -> {
                    settingsState = SettingsState.EARTHQUAKE_DIST_1;
                    getSilent().forceReply("Введите значение для:\nЗона 1: Расстояние:", chatId);
                }
                case "earthquake_mag_1" -> {
                    settingsState = SettingsState.EARTHQUAKE_MAG_1;
                    getSilent().forceReply("Введите значение для:\nЗона 1: Магнитуда:", chatId);
                }
                case "earthquake_distance_2" -> {
                    settingsState = SettingsState.EARTHQUAKE_DIST_2;
                    getSilent().forceReply("Введите значение для:\nЗона 2: Расстояние:", chatId);
                }
                case "earthquake_mag_2" -> {
                    settingsState = SettingsState.EARTHQUAKE_MAG_2;
                    getSilent().forceReply("Введите значение для:\nЗона 2: Магнитуда:", chatId);
                }
                case "earthquake_intensity" -> {
                    settingsState = SettingsState.EARTHQUAKE_INTENSITY;
                    getSilent().forceReply("Введите значение для:\nУровень ощутимости:", chatId);
                }
                case "cluster_settings" -> navigateToClusterSettings(chatId, messageId);
                case "cluster_alert" -> {
                    Settings.enableTelegramPossibleShakingAlert = !Settings.enableTelegramPossibleShakingAlert;
                    Settings.save();
                    navigateToClusterSettings(chatId, messageId);
                }
                case "cluster_location" -> {
                    Settings.enableTelegramPossibleShakingLocation = !Settings.enableTelegramPossibleShakingLocation;
                    Settings.save();
                    navigateToClusterSettings(chatId, messageId);
                }
                case "cluster_image" -> {
                    Settings.enableTelegramPossibleShakingImage = !Settings.enableTelegramPossibleShakingImage;
                    Settings.save();
                    navigateToClusterSettings(chatId, messageId);
                }
                case "cluster_map" -> {
                    Settings.enableTelegramPossibleShakingMap = !Settings.enableTelegramPossibleShakingMap;
                    Settings.save();
                    navigateToClusterSettings(chatId, messageId);
                }
                case "cluster_distance" -> {
                    settingsState = SettingsState.CLUSTER_DIST;
                    getSilent().forceReply("Введите значение для:\nРасстояние:", chatId);
                }
                case "cluster_level" -> {
                    settingsState = SettingsState.CLUSTER_LEVEL;
                    getSilent().forceReply("Введите значение для:\nУровень:", chatId);
                }
                case "station_settings" -> navigateToStationSettings(chatId, messageId);
                case "station_alert" -> {
                    Settings.enableTelegramStationHighIntensityAlert = !Settings.enableTelegramStationHighIntensityAlert;
                    Settings.save();
                    navigateToStationSettings(chatId, messageId);
                }
                case "station_location" -> {
                    Settings.enableTelegramStationHighIntensityLocation = !Settings.enableTelegramStationHighIntensityLocation;
                    Settings.save();
                    navigateToStationSettings(chatId, messageId);
                }
                case "station_image" -> {
                    Settings.enableTelegramStationHighIntensityImage = !Settings.enableTelegramStationHighIntensityImage;
                    Settings.save();
                    navigateToStationSettings(chatId, messageId);
                }
                case "station_map" -> {
                    Settings.enableTelegramStationHighIntensityMap = !Settings.enableTelegramStationHighIntensityMap;
                    Settings.save();
                    navigateToStationSettings(chatId, messageId);
                }
                case "station_distance" -> {
                    settingsState = SettingsState.STATION_DIST;
                    getSilent().forceReply("Введите значение для:\nРасстояние:", chatId);
                }
                case "station_intensity" -> {
                    settingsState = SettingsState.STATION_INTENSITY;
                    getSilent().forceReply("Введите значение для:\nИнтенсивность:", chatId);
                }
            }
        }
        if (update.hasMessage() && update.getMessage().hasText() && (settingsState != SettingsState.NONE)) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (settingsState) {
                case HOME_LAT -> {
                    try {
                        double homeLat = Double.parseDouble(messageText);
                        if (homeLat >= -90 && homeLat <= 90) {
                            Settings.homeLat = homeLat;
                            Settings.save();
                        } else {
                            getSilent().send("Значение должно быть в интервале от -90 до 90", chatId);
                        }
                    } catch (NumberFormatException e) {
                        getSilent().send("Неправильное число", chatId);
                    }
                    navigateToHomeSettings(chatId);
                }
                case HOME_LON -> {
                    try {
                        double homeLon = Double.parseDouble(messageText);
                        if (homeLon >= -180 && homeLon <= 180) {
                            Settings.homeLon = homeLon;
                            Settings.save();
                        } else {
                            getSilent().send("Значение должно быть в интервале от -180 до 180", chatId);
                        }
                    } catch (NumberFormatException e) {
                        getSilent().send("Неправильное число", chatId);
                    }
                    navigateToHomeSettings(chatId);
                }
                case EARTHQUAKE_DIST_1 -> {
                    try {
                        double dist = Double.parseDouble(messageText);
                        if (dist >=0 && dist <= 30000) {
                            Settings.tsEarthquakeMaxDistArea1 = dist;
                            Settings.save();
                        } else {
                            getSilent().send("Значение должно быть в интервале от 0 до 30000", chatId);
                        }
                    } catch (NumberFormatException e) {
                        getSilent().send("Неправильное число", chatId);
                    }
                    navigateToEarthquakeSettings(chatId);
                }
                case EARTHQUAKE_MAG_1 -> {
                    try {
                        double mag = Double.parseDouble(messageText);
                        if (mag >= 0 && mag <= 10) {
                            Settings.tsEarthquakeMinMagnitudeArea1 = mag;
                            Settings.save();
                        } else {
                            getSilent().send("Значение должно быть в интервале от 0 до 10", chatId);
                        }
                    } catch (NumberFormatException e) {
                        getSilent().send("Неправильное число", chatId);
                    }
                    navigateToEarthquakeSettings(chatId);
                }
                case EARTHQUAKE_DIST_2 -> {
                    try {
                        double dist = Double.parseDouble(messageText);
                        if (dist >=0 && dist <= 30000) {
                            Settings.tsEarthquakeMaxDistArea2 = dist;
                            Settings.save();
                        } else {
                            getSilent().send("Значение должно быть в интервале от 0 до 30000", chatId);
                        }
                    } catch (NumberFormatException e) {
                        getSilent().send("Неправильное число", chatId);
                    }
                    navigateToEarthquakeSettings(chatId);
                }
                case EARTHQUAKE_MAG_2 -> {
                    try {
                        double mag = Double.parseDouble(messageText);
                        if (mag >= 0 && mag <= 10) {
                            Settings.tsEarthquakeMinMagnitudeArea2 = mag;
                            Settings.save();
                        } else {
                            getSilent().send("Значение должно быть в интервале от 0 до 10", chatId);
                        }
                    } catch (NumberFormatException e) {
                        getSilent().send("Неправильное число", chatId);
                    }
                    navigateToEarthquakeSettings(chatId);
                }
                case EARTHQUAKE_INTENSITY -> {
                    try {
                        int intensity = Integer.parseInt(messageText) - 1;
                        if (intensity >= 0 && intensity <= IntensityScales.INTENSITY_SCALES[Settings.tsEarthquakeIntensityScale].getLevels().size() - 1) {
                            Settings.tsEarthquakeMinIntensity = intensity;
                            Settings.save();
                        } else {
                            getSilent().send("Значение должно быть в интервале от 1 до %d".formatted(IntensityScales.INTENSITY_SCALES[Settings.tsEarthquakeIntensityScale].getLevels().size()), chatId);
                        }
                    } catch (NumberFormatException e) {
                        getSilent().send("Неправильное число", chatId);
                    }
                    navigateToEarthquakeSettings(chatId);
                }
                case CLUSTER_DIST -> {
                    try {
                        double dist = Double.parseDouble(messageText);
                        if (dist >=0 && dist <= 30000) {
                            Settings.tsPossibleShakingMaxDist = dist;
                            Settings.save();
                        } else {
                            getSilent().send("Значение должно быть в интервале от 0 до 30000", chatId);
                        }
                    } catch (NumberFormatException e) {
                        getSilent().send("Неправильное число", chatId);
                    }
                    navigateToClusterSettings(chatId);
                }
                case CLUSTER_LEVEL -> {
                    try {
                        int level = Integer.parseInt(messageText);
                        if (level >= 0 && level <= Cluster.MAX_LEVEL) {
                            Settings.tsPossibleShakingMinLevel = level;
                            Settings.save();
                        } else {
                            getSilent().send("Значение должно быть в интервале от 0 до %d".formatted(Cluster.MAX_LEVEL), chatId);
                        }
                    } catch (NumberFormatException e) {
                        getSilent().send("Неправильное число", chatId);
                    }
                    navigateToClusterSettings(chatId);
                }
                case STATION_DIST -> {
                    try {
                        double dist = Double.parseDouble(messageText);
                        if (dist >=0 && dist <= 30000) {
                            Settings.tsStationMaxDist = dist;
                            Settings.save();
                        } else {
                            getSilent().send("Значение должно быть в интервале от 0 до 30000", chatId);
                        }
                    } catch (NumberFormatException e) {
                        getSilent().send("Неправильное число", chatId);
                    }
                    navigateToStationSettings(chatId);
                }
                case STATION_INTENSITY -> {
                    try {
                        double intensity = Double.parseDouble(messageText);
                        if (intensity >= 0 && intensity <= 1000000) {
                            Settings.tsStationMinIntensity = intensity;
                            Settings.save();
                        } else {
                            getSilent().send("Значение должно быть в интервале от 0 до 1000000", chatId);
                        }
                    } catch (NumberFormatException e) {
                        getSilent().send("Неправильное число", chatId);
                    }
                    navigateToStationSettings(chatId);
                }
            }
            settingsState = SettingsState.NONE;
        }
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

    private void sendInlineKeyboard(InlineKeyboardMarkup markupInline, long chatId, Integer messageId) {
        try {
            if (messageId == null) {
                SendMessage sendMessage = SendMessage.builder()
                        .chatId(chatId)
                        .text("Выберите параметр, который хотите изменить:")
                        .replyMarkup(markupInline)
                        .build();

                if (Settings.telegramMessageThreadId != 0) {
                    sendMessage.setMessageThreadId(Settings.telegramMessageThreadId);
                }

                telegramClient.execute(sendMessage);
            } else {
                telegramClient.execute(EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .text("Выберите параметр, который хотите изменить:")
                        .replyMarkup(markupInline)
                        .build());
            }
        } catch (TelegramApiException e) {
            Logger.error(e);
        }
    }

    private void navigateToSettings(long chatId, int messageId) {
        InlineKeyboardMarkup markupInline = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Координаты дома").callbackData("home_settings").build(), InlineKeyboardButton.builder().text("Потенциальные землетрясения").callbackData("cluster_settings").build()))
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Землетрясения").callbackData("earthquake_settings").build(), InlineKeyboardButton.builder().text("Датчики").callbackData("station_settings").build())).build();
        sendInlineKeyboard(markupInline, chatId, messageId);
    }

    private void navigateToHomeSettings(long chatId) {
        navigateToHomeSettings(chatId, null);
    }

    private void navigateToHomeSettings(long chatId, Integer messageId) {
        InlineKeyboardMarkup markupInline = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Широта: %.6f".formatted(Settings.homeLat)).callbackData("home_lat").build(), InlineKeyboardButton.builder().text("Долгота: %.6f".formatted(Settings.homeLon)).callbackData("home_lon").build()))
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Назад").callbackData("settings").build())).build();
        sendInlineKeyboard(markupInline, chatId, messageId);
    }

    private void navigateToEarthquakeSettings(long chatId) {
        navigateToEarthquakeSettings(chatId, null);
    }

    private void navigateToEarthquakeSettings(long chatId, Integer messageId) {
        InlineKeyboardMarkup markupInline = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Получать уведомления: %s".formatted(TelegramUtils.booleanToString(Settings.enableTelegramEarthquakeAlert))).callbackData("earthquake_alert").build()))
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Получать геолокацию: %s".formatted(TelegramUtils.booleanToString(Settings.enableTelegramEarthquakeLocation))).callbackData("earthquake_location").build()))
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Получать картинку: %s".formatted(TelegramUtils.booleanToString(Settings.enableTelegramEarthquakeImage))).callbackData("earthquake_image").build()))
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Получать карту: %s".formatted(TelegramUtils.booleanToString(Settings.enableTelegramEarthquakeMap))).callbackData("earthquake_map").build()))
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Зона 1. Расстояние: %.1f".formatted(Settings.tsEarthquakeMaxDistArea1)).callbackData("earthquake_distance_1").build(), InlineKeyboardButton.builder().text("Зона 1: Магнитуда: %.1f".formatted(Settings.tsEarthquakeMinMagnitudeArea1)).callbackData("earthquake_mag_1").build()))
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Зона 2. Расстояние: %.1f".formatted(Settings.tsEarthquakeMaxDistArea2)).callbackData("earthquake_distance_2").build(), InlineKeyboardButton.builder().text("Зона 2: Магнитуда: %.1f".formatted(Settings.tsEarthquakeMinMagnitudeArea2)).callbackData("earthquake_mag_2").build()))
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Уровень ощутимости: %d".formatted(Settings.tsEarthquakeMinIntensity + 1)).callbackData("earthquake_intensity").build()))
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Назад").callbackData("settings").build())).build();
        sendInlineKeyboard(markupInline, chatId, messageId);
    }

    private void navigateToClusterSettings(long chatId) {
        navigateToClusterSettings(chatId, null);
    }

    private void navigateToClusterSettings(long chatId, Integer messageId) {
        InlineKeyboardMarkup markupInline = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Получать уведомления: %s".formatted(TelegramUtils.booleanToString(Settings.enableTelegramPossibleShakingAlert))).callbackData("cluster_alert").build()))
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Получать геолокацию: %s".formatted(TelegramUtils.booleanToString(Settings.enableTelegramPossibleShakingLocation))).callbackData("cluster_location").build()))
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Получать картинку: %s".formatted(TelegramUtils.booleanToString(Settings.enableTelegramPossibleShakingImage))).callbackData("cluster_image").build()))
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Получать карту: %s".formatted(TelegramUtils.booleanToString(Settings.enableTelegramPossibleShakingMap))).callbackData("cluster_map").build()))
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Расстояние: %.1f".formatted(Settings.tsPossibleShakingMaxDist)).callbackData("cluster_distance").build(), InlineKeyboardButton.builder().text("Уровень: %d".formatted(Settings.tsPossibleShakingMinLevel)).callbackData("cluster_level").build()))
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Назад").callbackData("settings").build())).build();
        sendInlineKeyboard(markupInline, chatId, messageId);
    }

    private void navigateToStationSettings(long chatId) {
        navigateToStationSettings(chatId, null);
    }

    private void navigateToStationSettings(long chatId, Integer messageId) {
        InlineKeyboardMarkup markupInline = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Получать уведомления: %s".formatted(TelegramUtils.booleanToString(Settings.enableTelegramStationHighIntensityAlert))).callbackData("station_alert").build()))
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Получать геолокацию: %s".formatted(TelegramUtils.booleanToString(Settings.enableTelegramStationHighIntensityLocation))).callbackData("station_location").build()))
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Получать картинку: %s".formatted(TelegramUtils.booleanToString(Settings.enableTelegramStationHighIntensityImage))).callbackData("station_image").build()))
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Получать карту: %s".formatted(TelegramUtils.booleanToString(Settings.enableTelegramStationHighIntensityMap))).callbackData("station_map").build()))
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Расстояние: %.1f".formatted(Settings.tsStationMaxDist)).callbackData("station_distance").build(), InlineKeyboardButton.builder().text("Интенсивность: %.1f".formatted(Settings.tsStationMinIntensity)).callbackData("station_intensity").build()))
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Назад").callbackData("settings").build())).build();
        sendInlineKeyboard(markupInline, chatId, messageId);
    }
}
