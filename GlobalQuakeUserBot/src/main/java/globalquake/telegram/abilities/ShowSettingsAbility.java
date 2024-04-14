package globalquake.telegram.abilities;

import globalquake.client.GlobalQuakeClient;
import globalquake.telegram.TelegramService;
import globalquake.db.entities.TelegramUser;
import globalquake.telegram.util.TelegramUtils;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;

import static org.telegram.telegrambots.abilitybots.api.objects.Locality.USER;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

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
                .privacy(PUBLIC)
                .action(ctx -> {
                    TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(ctx.user().getId());
                    if (telegramUser != null && telegramUser.getEnabled()) {
                        updateTelegramUser(telegramUser, ctx.user(), ctx.chatId());
                        int order = GlobalQuakeClient.instance.getDatabaseService().getUserOrder(telegramUser.getId());

                        getTelegramService().getSilent().sendMd(("*Координаты дома*%nШирота: %.6f%nДолгота: %.6f%n%n" +
                                "*Землетрясения*%nПолучать уведомления: %s%nПолучать геолокацию: %s%nПолучать картинку: %s%nПолучать карту: %s%nЗона 1:%nРадиус: %d км. Магнитуда: %.1f%nЗона 2:%nРадиус: %d км. Магнитуда: %.1f%nУровень ощутимости: %d%n%n" +
                                "*Потенциальные землетрясения*%nПолучать уведомления: %s%nПолучать геолокацию: %s%nПолучать картинку: %s%nПолучать карту: %s%nРадиус: %d км.%nУровень: %d%n%n" +
                                "*Высокий уровень датчиков*%nПолучать уведомления: %s%nПолучать геолокацию: %s%nПолучать картинку: %s%nПолучать карту: %s%nЗона 1:%nРадиус: %d км.%nИнтенсивность: %d%nЗона 2:%nРадиус: %d км.%nИнтенсивность: %d%n%n" +
                                "Позиция в списке: %d").formatted(telegramUser.getHomeLat(), telegramUser.getHomeLon(),
                                TelegramUtils.booleanToString(telegramUser.getEnableTelegramEarthquakeAlert()), TelegramUtils.booleanToString(telegramUser.getEnableTelegramEarthquakeLocation()), TelegramUtils.booleanToString(telegramUser.getEnableTelegramEarthquakeImage()), TelegramUtils.booleanToString(telegramUser.getEnableTelegramEarthquakeMap()), telegramUser.getTsEarthquakeMaxDistArea1(), telegramUser.getTsEarthquakeMinMagnitudeArea1(), telegramUser.getTsEarthquakeMaxDistArea2(), telegramUser.getTsEarthquakeMinMagnitudeArea2(), telegramUser.getTsEarthquakeMinIntensity() + 1,
                                TelegramUtils.booleanToString(telegramUser.getEnableTelegramPossibleShakingAlert()), TelegramUtils.booleanToString(telegramUser.getEnableTelegramPossibleShakingLocation()), TelegramUtils.booleanToString(telegramUser.getEnableTelegramPossibleShakingImage()), TelegramUtils.booleanToString(telegramUser.getEnableTelegramPossibleShakingMap()), telegramUser.getTsPossibleShakingMaxDist(), telegramUser.getTsPossibleShakingMinLevel(),
                                TelegramUtils.booleanToString(telegramUser.getEnableTelegramStationHighIntensityAlert()), TelegramUtils.booleanToString(telegramUser.getEnableTelegramStationHighIntensityLocation()), TelegramUtils.booleanToString(telegramUser.getEnableTelegramStationHighIntensityImage()), TelegramUtils.booleanToString(telegramUser.getEnableTelegramStationHighIntensityMap()), telegramUser.getTsStationMaxDist1(), telegramUser.getTsStationMinIntensity1(), telegramUser.getTsStationMaxDist2(), telegramUser.getTsStationMinIntensity2(),
                                order), ctx.chatId());
                        GlobalQuakeClient.instance.getRegistry().counter("ability.used", "name", "mysettings", "user", ctx.user().getId().toString()).increment();
                    } else {
                        sendNotActiveWarning(telegramUser, ctx.user(), ctx.chatId());
                    }
                })
                .build();
    }
}
