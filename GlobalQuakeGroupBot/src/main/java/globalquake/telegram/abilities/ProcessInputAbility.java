package globalquake.telegram.abilities;

import globalquake.core.Settings;
import globalquake.core.earthquake.data.Cluster;
import globalquake.core.intensity.IntensityScales;
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

                        switch (callData) {
                            case "home_settings" -> navigateToHomeSettings(chatId, messageId);
                            case "home_lat" -> {
                                getTelegramService().setSettingsState(SettingsState.HOME_LAT);
                                getTelegramService().getSilent().forceReply("Введите значение для:\nШирота:", chatId);
                            }
                            case "home_lon" -> {
                                getTelegramService().setSettingsState(SettingsState.HOME_LON);
                                getTelegramService().getSilent().forceReply("Введите значение для:\nДолгота:", chatId);
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
                                getTelegramService().setSettingsState(SettingsState.EARTHQUAKE_DIST_1);
                                getTelegramService().getSilent().forceReply("Введите значение для:\nЗона 1: Расстояние:", chatId);
                            }
                            case "earthquake_mag_1" -> {
                                getTelegramService().setSettingsState(SettingsState.EARTHQUAKE_MAG_1);
                                getTelegramService().getSilent().forceReply("Введите значение для:\nЗона 1: Магнитуда:", chatId);
                            }
                            case "earthquake_distance_2" -> {
                                getTelegramService().setSettingsState(SettingsState.EARTHQUAKE_DIST_2);
                                getTelegramService().getSilent().forceReply("Введите значение для:\nЗона 2: Расстояние:", chatId);
                            }
                            case "earthquake_mag_2" -> {
                                getTelegramService().setSettingsState(SettingsState.EARTHQUAKE_MAG_2);
                                getTelegramService().getSilent().forceReply("Введите значение для:\nЗона 2: Магнитуда:", chatId);
                            }
                            case "earthquake_intensity" -> {
                                getTelegramService().setSettingsState(SettingsState.EARTHQUAKE_INTENSITY);
                                getTelegramService().getSilent().forceReply("Введите значение для:\nУровень ощутимости:", chatId);
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
                                getTelegramService().setSettingsState(SettingsState.CLUSTER_DIST);
                                getTelegramService().getSilent().forceReply("Введите значение для:\nРасстояние:", chatId);
                            }
                            case "cluster_level" -> {
                                getTelegramService().setSettingsState(SettingsState.CLUSTER_LEVEL);
                                getTelegramService().getSilent().forceReply("Введите значение для:\nУровень:", chatId);
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
                            case "station_distance_1" -> {
                                getTelegramService().setSettingsState(SettingsState.STATION_DIST_1);
                                getTelegramService().getSilent().forceReply("Введите значение для:\nЗона 1: Расстояние:", chatId);
                            }
                            case "station_intensity_1" -> {
                                getTelegramService().setSettingsState(SettingsState.STATION_INTENSITY_1);
                                getTelegramService().getSilent().forceReply("Введите значение для:\nЗона 1: Интенсивность:", chatId);
                            }
                            case "station_distance_2" -> {
                                getTelegramService().setSettingsState(SettingsState.STATION_DIST_2);
                                getTelegramService().getSilent().forceReply("Введите значение для:\nЗона 2: Расстояние:", chatId);
                            }
                            case "station_intensity_2" -> {
                                getTelegramService().setSettingsState(SettingsState.STATION_INTENSITY_2);
                                getTelegramService().getSilent().forceReply("Введите значение для:\nЗона 2: Интенсивность:", chatId);
                            }
                            default -> navigateToSettings(chatId, messageId);
                        }
                    }
                    if (ctx.update().hasMessage() && ctx.update().getMessage().hasText() && (getTelegramService().getSettingsState() != SettingsState.NONE)) {
                        String messageText = ctx.update().getMessage().getText();
                        long chatId = ctx.update().getMessage().getChatId();

                        switch (getTelegramService().getSettingsState()) {
                            case HOME_LAT -> {
                                try {
                                    double homeLat = Double.parseDouble(messageText);
                                    if (homeLat >= -90 && homeLat <= 90) {
                                        Settings.homeLat = homeLat;
                                        Settings.save();
                                    } else {
                                        getTelegramService().getSilent().send("Значение должно быть в интервале от -90 до 90", chatId);
                                    }
                                } catch (NumberFormatException e) {
                                    getTelegramService().getSilent().send("Неправильное число", chatId);
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
                                        getTelegramService().getSilent().send("Значение должно быть в интервале от -180 до 180", chatId);
                                    }
                                } catch (NumberFormatException e) {
                                    getTelegramService().getSilent().send("Неправильное число", chatId);
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
                                        getTelegramService().getSilent().send("Значение должно быть в интервале от 0 до 30000", chatId);
                                    }
                                } catch (NumberFormatException e) {
                                    getTelegramService().getSilent().send("Неправильное число", chatId);
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
                                        getTelegramService().getSilent().send("Значение должно быть в интервале от 0 до 10", chatId);
                                    }
                                } catch (NumberFormatException e) {
                                    getTelegramService().getSilent().send("Неправильное число", chatId);
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
                                        getTelegramService().getSilent().send("Значение должно быть в интервале от 0 до 30000", chatId);
                                    }
                                } catch (NumberFormatException e) {
                                    getTelegramService().getSilent().send("Неправильное число", chatId);
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
                                        getTelegramService().getSilent().send("Значение должно быть в интервале от 0 до 10", chatId);
                                    }
                                } catch (NumberFormatException e) {
                                    getTelegramService().getSilent().send("Неправильное число", chatId);
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
                                        getTelegramService().getSilent().send("Значение должно быть в интервале от 1 до %d".formatted(IntensityScales.INTENSITY_SCALES[Settings.tsEarthquakeIntensityScale].getLevels().size()), chatId);
                                    }
                                } catch (NumberFormatException e) {
                                    getTelegramService().getSilent().send("Неправильное число", chatId);
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
                                        getTelegramService().getSilent().send("Значение должно быть в интервале от 0 до 30000", chatId);
                                    }
                                } catch (NumberFormatException e) {
                                    getTelegramService().getSilent().send("Неправильное число", chatId);
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
                                        getTelegramService().getSilent().send("Значение должно быть в интервале от 0 до %d".formatted(Cluster.MAX_LEVEL), chatId);
                                    }
                                } catch (NumberFormatException e) {
                                    getTelegramService().getSilent().send("Неправильное число", chatId);
                                }
                                navigateToClusterSettings(chatId);
                            }
                            case STATION_DIST_1 -> {
                                try {
                                    double dist = Double.parseDouble(messageText);
                                    if (dist >=0 && dist <= 30000) {
                                        Settings.tsStationMaxDist1 = dist;
                                        Settings.save();
                                    } else {
                                        getTelegramService().getSilent().send("Значение должно быть в интервале от 0 до 30000", chatId);
                                    }
                                } catch (NumberFormatException e) {
                                    getTelegramService().getSilent().send("Неправильное число", chatId);
                                }
                                navigateToStationSettings(chatId);
                            }
                            case STATION_INTENSITY_1 -> {
                                try {
                                    double intensity = Double.parseDouble(messageText);
                                    if (intensity >= 0 && intensity <= 1000000) {
                                        Settings.tsStationMinIntensity1 = intensity;
                                        Settings.save();
                                    } else {
                                        getTelegramService().getSilent().send("Значение должно быть в интервале от 0 до 1000000", chatId);
                                    }
                                } catch (NumberFormatException e) {
                                    getTelegramService().getSilent().send("Неправильное число", chatId);
                                }
                                navigateToStationSettings(chatId);
                            }
                            case STATION_DIST_2 -> {
                                try {
                                    double dist = Double.parseDouble(messageText);
                                    if (dist >=0 && dist <= 30000) {
                                        Settings.tsStationMaxDist2 = dist;
                                        Settings.save();
                                    } else {
                                        getTelegramService().getSilent().send("Значение должно быть в интервале от 0 до 30000", chatId);
                                    }
                                } catch (NumberFormatException e) {
                                    getTelegramService().getSilent().send("Неправильное число", chatId);
                                }
                                navigateToStationSettings(chatId);
                            }
                            case STATION_INTENSITY_2 -> {
                                try {
                                    double intensity = Double.parseDouble(messageText);
                                    if (intensity >= 0 && intensity <= 1000000) {
                                        Settings.tsStationMinIntensity2 = intensity;
                                        Settings.save();
                                    } else {
                                        getTelegramService().getSilent().send("Значение должно быть в интервале от 0 до 1000000", chatId);
                                    }
                                } catch (NumberFormatException e) {
                                    getTelegramService().getSilent().send("Неправильное число", chatId);
                                }
                                navigateToStationSettings(chatId);
                            }
                        }
                        getTelegramService().setSettingsState(SettingsState.NONE);
                    }
                })
                .build();
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
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Зона 1: Расстояние: %.1f".formatted(Settings.tsEarthquakeMaxDistArea1)).callbackData("earthquake_distance_1").build(), InlineKeyboardButton.builder().text("Зона 1: Магнитуда: %.1f".formatted(Settings.tsEarthquakeMinMagnitudeArea1)).callbackData("earthquake_mag_1").build()))
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Зона 2: Расстояние: %.1f".formatted(Settings.tsEarthquakeMaxDistArea2)).callbackData("earthquake_distance_2").build(), InlineKeyboardButton.builder().text("Зона 2: Магнитуда: %.1f".formatted(Settings.tsEarthquakeMinMagnitudeArea2)).callbackData("earthquake_mag_2").build()))
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
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Зона 1: Расстояние: %.1f".formatted(Settings.tsStationMaxDist1)).callbackData("station_distance_1").build(), InlineKeyboardButton.builder().text("Зона 1: Интенсивность: %.1f".formatted(Settings.tsStationMinIntensity1)).callbackData("station_intensity_1").build()))
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Зона 2: Расстояние: %.1f".formatted(Settings.tsStationMaxDist2)).callbackData("station_distance_2").build(), InlineKeyboardButton.builder().text("Зона 2: Интенсивность: %.1f".formatted(Settings.tsStationMinIntensity2)).callbackData("station_intensity_2").build()))
                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Назад").callbackData("settings").build())).build();
        sendInlineKeyboard(markupInline, chatId, messageId);
    }
}
