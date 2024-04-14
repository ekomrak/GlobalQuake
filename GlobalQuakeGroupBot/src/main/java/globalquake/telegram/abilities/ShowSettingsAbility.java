package globalquake.telegram.abilities;

import globalquake.core.Settings;
import globalquake.telegram.TelegramService;
import globalquake.telegram.util.TelegramUtils;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;

import static org.telegram.telegrambots.abilitybots.api.objects.Locality.USER;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.ADMIN;

public class ShowSettingsAbility extends AbstractAbility {
    public ShowSettingsAbility(TelegramService telegramService) {
        super(telegramService);
    }

    public Ability mySettings() {
        return Ability.builder()
                .name("mysettings")
                .info("Вывести текущие значения личных настроек.")
                .input(0)
                .locality(USER)
                .privacy(ADMIN)
                .action(ctx -> getTelegramService().getSilent().sendMd(("*Координаты дома*%nШирота: %.6f%nДолгота: %.6f%n%n" +
                        "*Землетрясения*%nПолучать уведомления: %s%nПолучать геолокацию: %s%nПолучать картинку: %s%nПолучать карту: %s%nЗона 1:%nРадиус: %.1f км. Магнитуда: %.1f%nЗона 2:%nРадиус: %.1f км. Магнитуда: %.1f%nУровень ощутимости: %d%n%n" +
                        "*Потенциальные землетрясения*%nПолучать уведомления: %s%nПолучать геолокацию: %s%nПолучать картинку: %s%nПолучать карту: %s%nРадиус: %.1f км.%nУровень: %d%n%n" +
                        "*Высокий уровень датчиков*%nПолучать уведомления: %s%nПолучать геолокацию: %s%nПолучать картинку: %s%nПолучать карту: %s%nРадиус: %.1f км.%nИнтенсивность: %.1f").formatted(Settings.homeLat, Settings.homeLon,
                        TelegramUtils.booleanToString(Settings.enableTelegramEarthquakeAlert), TelegramUtils.booleanToString(Settings.enableTelegramEarthquakeLocation), TelegramUtils.booleanToString(Settings.enableTelegramEarthquakeImage), TelegramUtils.booleanToString(Settings.enableTelegramEarthquakeMap), Settings.tsEarthquakeMaxDistArea1, Settings.tsEarthquakeMinMagnitudeArea1, Settings.tsEarthquakeMaxDistArea2, Settings.tsEarthquakeMinMagnitudeArea2, Settings.tsEarthquakeMinIntensity + 1,
                        TelegramUtils.booleanToString(Settings.enableTelegramPossibleShakingAlert), TelegramUtils.booleanToString(Settings.enableTelegramPossibleShakingLocation), TelegramUtils.booleanToString(Settings.enableTelegramPossibleShakingImage), TelegramUtils.booleanToString(Settings.enableTelegramPossibleShakingMap), Settings.tsPossibleShakingMaxDist, Settings.tsPossibleShakingMinLevel,
                        TelegramUtils.booleanToString(Settings.enableTelegramStationHighIntensityAlert), TelegramUtils.booleanToString(Settings.enableTelegramStationHighIntensityLocation), TelegramUtils.booleanToString(Settings.enableTelegramStationHighIntensityImage), TelegramUtils.booleanToString(Settings.enableTelegramStationHighIntensityMap), Settings.tsStationMaxDist, Settings.tsStationMinIntensity), ctx.chatId()))
                .build();
    }
}
