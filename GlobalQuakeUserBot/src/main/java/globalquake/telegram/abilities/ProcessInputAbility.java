package globalquake.telegram.abilities;

import globalquake.client.GlobalQuakeClient;
import globalquake.core.Settings;
import globalquake.core.earthquake.data.Cluster;
import globalquake.core.intensity.IntensityScales;
import globalquake.db.UsersCacheListType;
import globalquake.db.entities.TelegramUser;
import globalquake.telegram.SettingsState;
import globalquake.telegram.TelegramService;
import globalquake.telegram.util.TelegramUtils;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import static org.telegram.telegrambots.abilitybots.api.objects.Locality.USER;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

public class ProcessInputAbility extends AbstractAbility {
    public ProcessInputAbility(TelegramService telegramService) {
        super(telegramService);
    }

    public Ability processInput() {
        return Ability.builder()
                .name("default")
                .flag(update -> update.hasCallbackQuery() || update.hasMessage())
                .privacy(PUBLIC)
                .locality(USER)
                .input(0)
                .action(ctx -> {
                    if (ctx.update().hasCallbackQuery()) {
                        String callData = ctx.update().getCallbackQuery().getData();
                        int messageId = ctx.update().getCallbackQuery().getMessage().getMessageId();
                        long chatId = ctx.update().getCallbackQuery().getMessage().getChatId();
                        long userId = ctx.update().getCallbackQuery().getFrom().getId();

                        switch (callData) {
                            case "general_settings" -> navigateToGeneralSettings(userId, chatId, messageId);
                            case "general_image" -> {
                                TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);

                                if (telegramUser != null) {
                                    telegramUser.setSendImageAsAPhoto(!telegramUser.getSendImageAsAPhoto());
                                    GlobalQuakeClient.instance.getDatabaseService().updateTelegramUser(telegramUser);
                                    GlobalQuakeClient.instance.getDatabaseService().invalidateAllUsersLists();
                                    navigateToGeneralSettings(userId, chatId, messageId);
                                }
                            }
                            case "general_map" -> {
                                TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);

                                if (telegramUser != null) {
                                    telegramUser.setSendMapAsAPhoto(!telegramUser.getSendMapAsAPhoto());
                                    GlobalQuakeClient.instance.getDatabaseService().updateTelegramUser(telegramUser);
                                    GlobalQuakeClient.instance.getDatabaseService().invalidateAllUsersLists();
                                    navigateToGeneralSettings(userId, chatId, messageId);
                                }
                            }
                            case "general_cities" -> {
                                TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);

                                if (telegramUser != null) {
                                    telegramUser.setShowSmallCities(!telegramUser.getShowSmallCities());
                                    GlobalQuakeClient.instance.getDatabaseService().updateTelegramUser(telegramUser);
                                    GlobalQuakeClient.instance.getDatabaseService().invalidateAllUsersLists();
                                    navigateToGeneralSettings(userId, chatId, messageId);
                                }
                            }
                            case "general_faults" -> {
                                TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);

                                if (telegramUser != null) {
                                    telegramUser.setShowFaults(!telegramUser.getShowFaults());
                                    GlobalQuakeClient.instance.getDatabaseService().updateTelegramUser(telegramUser);
                                    GlobalQuakeClient.instance.getDatabaseService().invalidateAllUsersLists();
                                    navigateToGeneralSettings(userId, chatId, messageId);
                                }
                            }
                            case "home_settings" -> navigateToHomeSettings(userId, chatId, messageId);
                            case "home_lat" -> {
                                getTelegramService().getUserState().put(userId, SettingsState.HOME_LAT);
                                getTelegramService().getSilent().forceReply("Введите значение для:\nШирота:", chatId);
                            }
                            case "home_lon" -> {
                                getTelegramService().getUserState().put(userId, SettingsState.HOME_LON);
                                getTelegramService().getSilent().forceReply("Введите значение для:\nДолгота:", chatId);
                            }
                            case "earthquake_settings" -> navigateToEarthquakeSettings(userId, chatId, messageId);
                            case "earthquake_alert" -> {
                                TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);

                                if (telegramUser != null) {
                                    telegramUser.setEnableTelegramEarthquakeAlert(!telegramUser.getEnableTelegramEarthquakeAlert());
                                    GlobalQuakeClient.instance.getDatabaseService().updateTelegramUser(telegramUser);
                                    GlobalQuakeClient.instance.getDatabaseService().invalidateUsersListCache(UsersCacheListType.USERS_WITH_EARTHQUAKE_ALERT);
                                    navigateToEarthquakeSettings(userId, chatId, messageId);
                                }
                            }
                            case "earthquake_location" -> {
                                TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);

                                if (telegramUser != null) {
                                    telegramUser.setEnableTelegramEarthquakeLocation(!telegramUser.getEnableTelegramEarthquakeLocation());
                                    GlobalQuakeClient.instance.getDatabaseService().updateTelegramUser(telegramUser);
                                    navigateToEarthquakeSettings(userId, chatId, messageId);
                                }
                            }
                            case "earthquake_image" -> {
                                TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);

                                if (telegramUser != null) {
                                    telegramUser.setEnableTelegramEarthquakeImage(!telegramUser.getEnableTelegramEarthquakeImage());
                                    GlobalQuakeClient.instance.getDatabaseService().updateTelegramUser(telegramUser);
                                    navigateToEarthquakeSettings(userId, chatId, messageId);
                                }
                            }
                            case "earthquake_map" -> {
                                TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);

                                if (telegramUser != null) {
                                    telegramUser.setEnableTelegramEarthquakeMap(!telegramUser.getEnableTelegramEarthquakeMap());
                                    GlobalQuakeClient.instance.getDatabaseService().updateTelegramUser(telegramUser);
                                    navigateToEarthquakeSettings(userId, chatId, messageId);
                                }
                            }
                            case "earthquake_distance_1" -> {
                                getTelegramService().getUserState().put(userId, SettingsState.EARTHQUAKE_DIST_1);
                                getTelegramService().getSilent().forceReply("Введите значение для:\nЗона 1: Расстояние:", chatId);
                            }
                            case "earthquake_mag_1" -> {
                                getTelegramService().getUserState().put(userId, SettingsState.EARTHQUAKE_MAG_1);
                                getTelegramService().getSilent().forceReply("Введите значение для:\nЗона 1: Магнитуда:", chatId);
                            }
                            case "earthquake_distance_2" -> {
                                getTelegramService().getUserState().put(userId, SettingsState.EARTHQUAKE_DIST_2);
                                getTelegramService().getSilent().forceReply("Введите значение для:\nЗона 2: Расстояние:", chatId);
                            }
                            case "earthquake_mag_2" -> {
                                getTelegramService().getUserState().put(userId, SettingsState.EARTHQUAKE_MAG_2);
                                getTelegramService().getSilent().forceReply("Введите значение для:\nЗона 2: Магнитуда:", chatId);
                            }
                            case "earthquake_intensity" -> {
                                getTelegramService().getUserState().put(userId, SettingsState.EARTHQUAKE_INTENSITY);
                                getTelegramService().getSilent().forceReply("Введите значение для:\nУровень ощутимости:", chatId);
                            }
                            case "cluster_settings" -> navigateToClusterSettings(userId, chatId, messageId);
                            case "cluster_alert" -> {
                                TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);

                                if (telegramUser != null) {
                                    telegramUser.setEnableTelegramPossibleShakingAlert(!telegramUser.getEnableTelegramPossibleShakingAlert());
                                    GlobalQuakeClient.instance.getDatabaseService().updateTelegramUser(telegramUser);
                                    GlobalQuakeClient.instance.getDatabaseService().invalidateUsersListCache(UsersCacheListType.USERS_WITH_CLUSTER_ALERT);
                                    navigateToClusterSettings(userId, chatId, messageId);
                                }
                            }
                            case "cluster_location" -> {
                                TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);

                                if (telegramUser != null) {
                                    telegramUser.setEnableTelegramPossibleShakingLocation(!telegramUser.getEnableTelegramPossibleShakingLocation());
                                    GlobalQuakeClient.instance.getDatabaseService().updateTelegramUser(telegramUser);
                                    navigateToClusterSettings(userId, chatId, messageId);
                                }
                            }
                            case "cluster_image" -> {
                                TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);

                                if (telegramUser != null) {
                                    telegramUser.setEnableTelegramPossibleShakingImage(!telegramUser.getEnableTelegramPossibleShakingImage());
                                    GlobalQuakeClient.instance.getDatabaseService().updateTelegramUser(telegramUser);
                                    navigateToClusterSettings(userId, chatId, messageId);
                                }
                            }
                            case "cluster_map" -> {
                                TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);

                                if (telegramUser != null) {
                                    telegramUser.setEnableTelegramPossibleShakingMap(!telegramUser.getEnableTelegramPossibleShakingMap());
                                    GlobalQuakeClient.instance.getDatabaseService().updateTelegramUser(telegramUser);
                                    navigateToClusterSettings(userId, chatId, messageId);
                                }
                            }
                            case "cluster_distance" -> {
                                getTelegramService().getUserState().put(userId, SettingsState.CLUSTER_DIST);
                                getTelegramService().getSilent().forceReply("Введите значение для:\nРасстояние:", chatId);
                            }
                            case "cluster_level" -> {
                                getTelegramService().getUserState().put(userId, SettingsState.CLUSTER_LEVEL);
                                getTelegramService().getSilent().forceReply("Введите значение для:\nУровень:", chatId);
                            }
                            case "station_settings" -> navigateToStationSettings(userId, chatId, messageId);
                            case "station_alert" -> {
                                TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);

                                if (telegramUser != null) {
                                    telegramUser.setEnableTelegramStationHighIntensityAlert(!telegramUser.getEnableTelegramStationHighIntensityAlert());
                                    GlobalQuakeClient.instance.getDatabaseService().updateTelegramUser(telegramUser);
                                    GlobalQuakeClient.instance.getDatabaseService().invalidateUsersListCache(UsersCacheListType.USERS_WITH_STATION_ALERT);
                                    navigateToStationSettings(userId, chatId, messageId);
                                }
                            }
                            case "station_location" -> {
                                TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);

                                if (telegramUser != null) {
                                    telegramUser.setEnableTelegramStationHighIntensityLocation(!telegramUser.getEnableTelegramStationHighIntensityLocation());
                                    GlobalQuakeClient.instance.getDatabaseService().updateTelegramUser(telegramUser);
                                    navigateToStationSettings(userId, chatId, messageId);
                                }
                            }
                            case "station_image" -> {
                                TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);

                                if (telegramUser != null) {
                                    telegramUser.setEnableTelegramStationHighIntensityImage(!telegramUser.getEnableTelegramStationHighIntensityImage());
                                    GlobalQuakeClient.instance.getDatabaseService().updateTelegramUser(telegramUser);
                                    navigateToStationSettings(userId, chatId, messageId);
                                }
                            }
                            case "station_map" -> {
                                TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);

                                if (telegramUser != null) {
                                    telegramUser.setEnableTelegramStationHighIntensityMap(!telegramUser.getEnableTelegramStationHighIntensityMap());
                                    GlobalQuakeClient.instance.getDatabaseService().updateTelegramUser(telegramUser);
                                    navigateToStationSettings(userId, chatId, messageId);
                                }
                            }
                            case "station_distance_1" -> {
                                getTelegramService().getUserState().put(userId, SettingsState.STATION_DIST_1);
                                getTelegramService().getSilent().forceReply("Введите значение для:\nЗона 1: Расстояние:", chatId);
                            }
                            case "station_intensity_1" -> {
                                getTelegramService().getUserState().put(userId, SettingsState.STATION_INTENSITY_1);
                                getTelegramService().getSilent().forceReply("Введите значение для:\nЗона 1: Интенсивность:", chatId);
                            }
                            case "station_distance_2" -> {
                                getTelegramService().getUserState().put(userId, SettingsState.STATION_DIST_2);
                                getTelegramService().getSilent().forceReply("Введите значение для:\nЗона 2: Расстояние:", chatId);
                            }
                            case "station_intensity_2" -> {
                                getTelegramService().getUserState().put(userId, SettingsState.STATION_INTENSITY_2);
                                getTelegramService().getSilent().forceReply("Введите значение для:\nЗона 2: Интенсивность:", chatId);
                            }
                            default -> navigateToSettings(userId, chatId, messageId);
                        }
                    }
                    if (ctx.update().hasMessage() && ctx.update().getMessage().hasText()) {
                        long userId = ctx.update().getMessage().getFrom().getId();

                        SettingsState settingsState = getTelegramService().getUserState().getIfPresent(userId);
                        if (settingsState != null) {
                            String messageText = ctx.update().getMessage().getText();
                            long chatId = ctx.update().getMessage().getChatId();

                            switch (settingsState) {
                                case HOME_LAT -> {
                                    try {
                                        TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);

                                        if (telegramUser != null) {
                                            double homeLat = Double.parseDouble(messageText);
                                            if (homeLat >= -90 && homeLat <= 90) {
                                                telegramUser.setHomeLat(homeLat);
                                                GlobalQuakeClient.instance.getDatabaseService().updateTelegramUser(telegramUser);
                                            } else {
                                                getTelegramService().getSilent().send("Значение должно быть в интервале от -90 до 90", chatId);
                                            }
                                        }
                                    } catch (NumberFormatException e) {
                                        getTelegramService().getSilent().send("Неправильное число", chatId);
                                    }
                                    navigateToHomeSettings(userId, chatId);
                                }
                                case HOME_LON -> {
                                    try {
                                        TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);

                                        if (telegramUser != null) {
                                            double homeLon = Double.parseDouble(messageText);
                                            if (homeLon >= -180 && homeLon <= 180) {
                                                telegramUser.setHomeLon(homeLon);
                                                GlobalQuakeClient.instance.getDatabaseService().updateTelegramUser(telegramUser);
                                            } else {
                                                getTelegramService().getSilent().send("Значение должно быть в интервале от -180 до 180", chatId);
                                            }
                                        }
                                    } catch (NumberFormatException e) {
                                        getTelegramService().getSilent().send("Неправильное число", chatId);
                                    }
                                    navigateToHomeSettings(userId, chatId);
                                }
                                case EARTHQUAKE_DIST_1 -> {
                                    try {
                                        TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);

                                        if (telegramUser != null) {
                                            int dist = Integer.parseInt(messageText);
                                            if (dist >= 0 && dist <= 1000) {
                                                telegramUser.setTsEarthquakeMaxDistArea1(dist);
                                                GlobalQuakeClient.instance.getDatabaseService().updateTelegramUser(telegramUser);
                                            } else {
                                                getTelegramService().getSilent().send("Значение должно быть в интервале от 0 до 1000", chatId);
                                            }
                                        }
                                    } catch (NumberFormatException e) {
                                        getTelegramService().getSilent().send("Неправильное число", chatId);
                                    }
                                    navigateToEarthquakeSettings(userId, chatId);
                                }
                                case EARTHQUAKE_MAG_1 -> {
                                    try {
                                        TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);

                                        if (telegramUser != null) {
                                            double mag = Double.parseDouble(messageText);
                                            if (mag >= 0 && mag <= 10) {
                                                telegramUser.setTsEarthquakeMinMagnitudeArea1(mag);
                                                GlobalQuakeClient.instance.getDatabaseService().updateTelegramUser(telegramUser);
                                            } else {
                                                getTelegramService().getSilent().send("Значение должно быть в интервале от 0 до 10", chatId);
                                            }
                                        }
                                    } catch (NumberFormatException e) {
                                        getTelegramService().getSilent().send("Неправильное число", chatId);
                                    }
                                    navigateToEarthquakeSettings(userId, chatId);
                                }
                                case EARTHQUAKE_DIST_2 -> {
                                    try {
                                        TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);

                                        if (telegramUser != null) {
                                            int dist = Integer.parseInt(messageText);
                                            if (dist >= 0 && dist <= 1000) {
                                                telegramUser.setTsEarthquakeMaxDistArea2(dist);
                                                GlobalQuakeClient.instance.getDatabaseService().updateTelegramUser(telegramUser);
                                            } else {
                                                getTelegramService().getSilent().send("Значение должно быть в интервале от 0 до 1000", chatId);
                                            }
                                        }
                                    } catch (NumberFormatException e) {
                                        getTelegramService().getSilent().send("Неправильное число", chatId);
                                    }
                                    navigateToEarthquakeSettings(userId, chatId);
                                }
                                case EARTHQUAKE_MAG_2 -> {
                                    try {
                                        TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);

                                        if (telegramUser != null) {
                                            double mag = Double.parseDouble(messageText);
                                            if (mag >= 0 && mag <= 10) {
                                                telegramUser.setTsEarthquakeMinMagnitudeArea2(mag);
                                                GlobalQuakeClient.instance.getDatabaseService().updateTelegramUser(telegramUser);
                                            } else {
                                                getTelegramService().getSilent().send("Значение должно быть в интервале от 0 до 10", chatId);
                                            }
                                        }
                                    } catch (NumberFormatException e) {
                                        getTelegramService().getSilent().send("Неправильное число", chatId);
                                    }
                                    navigateToEarthquakeSettings(userId, chatId);
                                }
                                case EARTHQUAKE_INTENSITY -> {
                                    try {
                                        TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);

                                        if (telegramUser != null) {
                                            int intensity = Integer.parseInt(messageText) - 1;
                                            if (intensity >= 0 && intensity <= IntensityScales.INTENSITY_SCALES[Settings.tsEarthquakeIntensityScale].getLevels().size() - 1) {
                                                telegramUser.setTsEarthquakeMinIntensity(intensity);
                                                GlobalQuakeClient.instance.getDatabaseService().updateTelegramUser(telegramUser);
                                            } else {
                                                getTelegramService().getSilent().send("Значение должно быть в интервале от 1 до %d".formatted(IntensityScales.INTENSITY_SCALES[Settings.tsEarthquakeIntensityScale].getLevels().size()), chatId);
                                            }
                                        }
                                    } catch (NumberFormatException e) {
                                        getTelegramService().getSilent().send("Неправильное число", chatId);
                                    }
                                    navigateToEarthquakeSettings(userId, chatId);
                                }
                                case CLUSTER_DIST -> {
                                    try {
                                        TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);

                                        if (telegramUser != null) {
                                            int dist = Integer.parseInt(messageText);
                                            if (dist >= 0 && dist <= 1000) {
                                                telegramUser.setTsPossibleShakingMaxDist(dist);
                                                GlobalQuakeClient.instance.getDatabaseService().updateTelegramUser(telegramUser);
                                            } else {
                                                getTelegramService().getSilent().send("Значение должно быть в интервале от 0 до 1000", chatId);
                                            }
                                        }
                                    } catch (NumberFormatException e) {
                                        getTelegramService().getSilent().send("Неправильное число", chatId);
                                    }
                                    navigateToClusterSettings(userId, chatId);
                                }
                                case CLUSTER_LEVEL -> {
                                    try {
                                        TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);

                                        if (telegramUser != null) {
                                            int level = Integer.parseInt(messageText);
                                            if (level >= 0 && level <= Cluster.MAX_LEVEL) {
                                                telegramUser.setTsPossibleShakingMinLevel(level);
                                                GlobalQuakeClient.instance.getDatabaseService().updateTelegramUser(telegramUser);
                                            } else {
                                                getTelegramService().getSilent().send("Значение должно быть в интервале от 0 до %d".formatted(Cluster.MAX_LEVEL), chatId);
                                            }
                                        }
                                    } catch (NumberFormatException e) {
                                        getTelegramService().getSilent().send("Неправильное число", chatId);
                                    }
                                    navigateToClusterSettings(userId, chatId);
                                }
                                case STATION_DIST_1 -> {
                                    try {
                                        TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);

                                        if (telegramUser != null) {
                                            int dist = Integer.parseInt(messageText);
                                            if (dist >= 0 && dist <= 1000) {
                                                telegramUser.setTsStationMaxDist1(dist);
                                                GlobalQuakeClient.instance.getDatabaseService().updateTelegramUser(telegramUser);
                                            } else {
                                                getTelegramService().getSilent().send("Значение должно быть в интервале от 0 до 1000", chatId);
                                            }
                                        }
                                    } catch (NumberFormatException e) {
                                        getTelegramService().getSilent().send("Неправильное число", chatId);
                                    }
                                    navigateToStationSettings(userId, chatId);
                                }
                                case STATION_INTENSITY_1 -> {
                                    try {
                                        TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);

                                        if (telegramUser != null) {
                                            int intensity = Integer.parseInt(messageText);
                                            if (intensity >= 500 && intensity <= 100000) {
                                                telegramUser.setTsStationMinIntensity1(intensity);
                                                GlobalQuakeClient.instance.getDatabaseService().updateTelegramUser(telegramUser);
                                            } else {
                                                getTelegramService().getSilent().send("Значение должно быть в интервале от 500 до 100000", chatId);
                                            }
                                        }
                                    } catch (NumberFormatException e) {
                                        getTelegramService().getSilent().send("Неправильное число", chatId);
                                    }
                                    navigateToStationSettings(userId, chatId);
                                }
                                case STATION_DIST_2 -> {
                                    try {
                                        TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);

                                        if (telegramUser != null) {
                                            int dist = Integer.parseInt(messageText);
                                            if (dist >= 0 && dist <= 1000) {
                                                telegramUser.setTsStationMaxDist2(dist);
                                                GlobalQuakeClient.instance.getDatabaseService().updateTelegramUser(telegramUser);
                                            } else {
                                                getTelegramService().getSilent().send("Значение должно быть в интервале от 0 до 1000", chatId);
                                            }
                                        }
                                    } catch (NumberFormatException e) {
                                        getTelegramService().getSilent().send("Неправильное число", chatId);
                                    }
                                    navigateToStationSettings(userId, chatId);
                                }
                                case STATION_INTENSITY_2 -> {
                                    try {
                                        TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);

                                        if (telegramUser != null) {
                                            int intensity = Integer.parseInt(messageText);
                                            if (intensity >= 500 && intensity <= 100000) {
                                                telegramUser.setTsStationMinIntensity2(intensity);
                                                GlobalQuakeClient.instance.getDatabaseService().updateTelegramUser(telegramUser);
                                            } else {
                                                getTelegramService().getSilent().send("Значение должно быть в интервале от 500 до 100000", chatId);
                                            }
                                        }
                                    } catch (NumberFormatException e) {
                                        getTelegramService().getSilent().send("Неправильное число", chatId);
                                    }
                                    navigateToStationSettings(userId, chatId);
                                }
                            }
                            getTelegramService().getUserState().invalidate(userId);
                        }
                    }
                })
                .build();
    }

    private void navigateToSettings(long userId, long chatId, int messageId) {
        TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);
        if (telegramUser != null) {
            InlineKeyboardMarkup markupInline = InlineKeyboardMarkup.builder()
                    .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Основные настройки").callbackData("general_settings").build()))
                    .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Координаты дома").callbackData("home_settings").build(), InlineKeyboardButton.builder().text("Потенциальные землетрясения").callbackData("cluster_settings").build()))
                    .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Землетрясения").callbackData("earthquake_settings").build(), InlineKeyboardButton.builder().text("Датчики").callbackData("station_settings").build())).build();
            sendInlineKeyboard(markupInline, chatId, messageId);
        }
    }

    private void navigateToGeneralSettings(long userId, long chatId) {
        navigateToGeneralSettings(userId, chatId);
    }

    private void navigateToGeneralSettings(long userId, long chatId, Integer messageId) {
        TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);
        if (telegramUser != null) {
            InlineKeyboardMarkup markupInline = InlineKeyboardMarkup.builder()
                    .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Получать картинку как документ: %s".formatted(TelegramUtils.booleanToString(!telegramUser.getSendImageAsAPhoto()))).callbackData("general_image").build()))
                    .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Получать карту как документ: %s".formatted(TelegramUtils.booleanToString(!telegramUser.getSendMapAsAPhoto()))).callbackData("general_map").build()))
                    .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Показывать маленькие города: %s".formatted(TelegramUtils.booleanToString(telegramUser.getShowSmallCities()))).callbackData("general_cities").build()))
                    .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Показывать разломы: %s".formatted(TelegramUtils.booleanToString(telegramUser.getShowFaults()))).callbackData("general_faults").build()))
                    .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Назад").callbackData("settings").build())).build();
            sendInlineKeyboard(markupInline, chatId, messageId);
        }
    }

    private void navigateToHomeSettings(long userId, long chatId) {
        navigateToHomeSettings(userId, chatId, null);
    }

    private void navigateToHomeSettings(long userId, long chatId, Integer messageId) {
        TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);
        if (telegramUser != null) {
            InlineKeyboardMarkup markupInline = InlineKeyboardMarkup.builder()
                    .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Широта: %.6f".formatted(telegramUser.getHomeLat())).callbackData("home_lat").build(), InlineKeyboardButton.builder().text("Долгота: %.6f".formatted(telegramUser.getHomeLon())).callbackData("home_lon").build()))
                    .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Назад").callbackData("settings").build())).build();
            sendInlineKeyboard(markupInline, chatId, messageId);
        }
    }

    private void navigateToEarthquakeSettings(long userId, long chatId) {
        navigateToEarthquakeSettings(userId, chatId, null);
    }

    private void navigateToEarthquakeSettings(long userId, long chatId, Integer messageId) {
        TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);
        if (telegramUser != null) {
            InlineKeyboardMarkup markupInline = InlineKeyboardMarkup.builder()
                    .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Получать уведомления: %s".formatted(TelegramUtils.booleanToString(telegramUser.getEnableTelegramEarthquakeAlert()))).callbackData("earthquake_alert").build()))
                    .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Получать геолокацию: %s".formatted(TelegramUtils.booleanToString(telegramUser.getEnableTelegramEarthquakeLocation()))).callbackData("earthquake_location").build()))
                    .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Получать картинку: %s".formatted(TelegramUtils.booleanToString(telegramUser.getEnableTelegramEarthquakeImage()))).callbackData("earthquake_image").build()))
                    .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Получать карту: %s".formatted(TelegramUtils.booleanToString(telegramUser.getEnableTelegramEarthquakeMap()))).callbackData("earthquake_map").build()))
                    .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Зона 1: Расстояние: %d".formatted(telegramUser.getTsEarthquakeMaxDistArea1())).callbackData("earthquake_distance_1").build(), InlineKeyboardButton.builder().text("Зона 1: Магнитуда: %.1f".formatted(telegramUser.getTsEarthquakeMinMagnitudeArea1())).callbackData("earthquake_mag_1").build()))
                    .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Зона 2: Расстояние: %d".formatted(telegramUser.getTsEarthquakeMaxDistArea2())).callbackData("earthquake_distance_2").build(), InlineKeyboardButton.builder().text("Зона 2: Магнитуда: %.1f".formatted(telegramUser.getTsEarthquakeMinMagnitudeArea2())).callbackData("earthquake_mag_2").build()))
                    .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Уровень ощутимости: %d".formatted(telegramUser.getTsEarthquakeMinIntensity() + 1)).callbackData("earthquake_intensity").build()))
                    .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Назад").callbackData("settings").build())).build();
            sendInlineKeyboard(markupInline, chatId, messageId);
        }
    }

    private void navigateToClusterSettings(long userId, long chatId) {
        navigateToClusterSettings(userId, chatId, null);
    }

    private void navigateToClusterSettings(long userId, long chatId, Integer messageId) {
        TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);
        if (telegramUser != null) {
            InlineKeyboardMarkup markupInline = InlineKeyboardMarkup.builder()
                    .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Получать уведомления: %s".formatted(TelegramUtils.booleanToString(telegramUser.getEnableTelegramPossibleShakingAlert()))).callbackData("cluster_alert").build()))
                    .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Получать геолокацию: %s".formatted(TelegramUtils.booleanToString(telegramUser.getEnableTelegramPossibleShakingLocation()))).callbackData("cluster_location").build()))
                    .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Получать картинку: %s".formatted(TelegramUtils.booleanToString(telegramUser.getEnableTelegramPossibleShakingImage()))).callbackData("cluster_image").build()))
                    .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Получать карту: %s".formatted(TelegramUtils.booleanToString(telegramUser.getEnableTelegramPossibleShakingMap()))).callbackData("cluster_map").build()))
                    .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Расстояние: %d".formatted(telegramUser.getTsPossibleShakingMaxDist())).callbackData("cluster_distance").build(), InlineKeyboardButton.builder().text("Уровень: %d".formatted(telegramUser.getTsPossibleShakingMinLevel())).callbackData("cluster_level").build()))
                    .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Назад").callbackData("settings").build())).build();
            sendInlineKeyboard(markupInline, chatId, messageId);
        }
    }

    private void navigateToStationSettings(long userId, long chatId) {
        navigateToStationSettings(userId, chatId, null);
    }

    private void navigateToStationSettings(long userId, long chatId, Integer messageId) {
        TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);
        if (telegramUser != null) {
            InlineKeyboardMarkup markupInline = InlineKeyboardMarkup.builder()
                    .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Получать уведомления: %s".formatted(TelegramUtils.booleanToString(telegramUser.getEnableTelegramStationHighIntensityAlert()))).callbackData("station_alert").build()))
                    .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Получать геолокацию: %s".formatted(TelegramUtils.booleanToString(telegramUser.getEnableTelegramStationHighIntensityLocation()))).callbackData("station_location").build()))
                    .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Получать картинку: %s".formatted(TelegramUtils.booleanToString(telegramUser.getEnableTelegramStationHighIntensityImage()))).callbackData("station_image").build()))
                    .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Получать карту: %s".formatted(TelegramUtils.booleanToString(telegramUser.getEnableTelegramStationHighIntensityMap()))).callbackData("station_map").build()))
                    .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Зона 1: Расстояние: %d".formatted(telegramUser.getTsStationMaxDist1())).callbackData("station_distance_1").build(), InlineKeyboardButton.builder().text("Зона 1: Интенсивность: %d".formatted(telegramUser.getTsStationMinIntensity1())).callbackData("station_intensity_1").build()))
                    .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Зона 2: Расстояние: %d".formatted(telegramUser.getTsStationMaxDist2())).callbackData("station_distance_2").build(), InlineKeyboardButton.builder().text("Зона 2: Интенсивность: %d".formatted(telegramUser.getTsStationMinIntensity2())).callbackData("station_intensity_2").build()))
                    .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Назад").callbackData("settings").build())).build();
            sendInlineKeyboard(markupInline, chatId, messageId);
        }
    }
}
