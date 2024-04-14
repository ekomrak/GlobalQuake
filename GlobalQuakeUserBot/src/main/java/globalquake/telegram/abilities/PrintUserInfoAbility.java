package globalquake.telegram.abilities;

import globalquake.client.GlobalQuakeClient;
import globalquake.telegram.TelegramService;
import globalquake.db.entities.TelegramUser;
import globalquake.telegram.util.TelegramUtils;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;


import static org.telegram.telegrambots.abilitybots.api.objects.Locality.USER;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.ADMIN;

public class PrintUserInfoAbility extends AbstractAbility {
    public PrintUserInfoAbility(TelegramService telegramService) {
        super(telegramService);
    }

    public Ability userinfo() {
        return Ability.builder()
                .name("userinfo")
                .info("Вывести информацию о пользователе.")
                .input(1)
                .locality(USER)
                .privacy(ADMIN)
                .action(ctx -> {
                    try {
                        long userId = Long.parseLong(ctx.firstArg());
                        TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(userId);

                        if (telegramUser != null) {
                            int order = GlobalQuakeClient.instance.getDatabaseService().getUserOrder(telegramUser.getId());

                            getTelegramService().getSilent().sendMd(("*Пользователь*%nId: %d%nChatId: %d%nFirstName: %s%nLastName: %s%nUserName: %s%nLanguageCode: %s%nPremium: %s%nEnabled: %s%nSubscriptionDate: %s%nUpdatedDate: %s%n%n".formatted(telegramUser.getId(), telegramUser.getChatId(), telegramUser.getFirstName(), telegramUser.getLastName(), telegramUser.getUserName(), telegramUser.getLanguageCode(), TelegramUtils.booleanToString(telegramUser.getPremium()), TelegramUtils.booleanToString(telegramUser.getEnabled()), telegramUser.getSubscriptionDate().format(TelegramUtils.DATE_FORMAT), telegramUser.getUpdatedDate().format(TelegramUtils.DATE_FORMAT)) +
                                    "*Координаты дома*%nШирота: %.6f%nДолгота: %.6f%n%n".formatted(telegramUser.getHomeLat(), telegramUser.getHomeLon()) +
                                    "*Землетрясения*%nПолучать уведомления: %s%nПолучать геолокацию: %s%nПолучать картинку: %s%nПолучать карту: %s%nЗона 1:%nРадиус: %d км. Магнитуда: %.1f%nЗона 2:%nРадиус: %d км. Магнитуда: %.1f%nУровень ощутимости: %d%n%n".formatted(TelegramUtils.booleanToString(telegramUser.getEnableTelegramEarthquakeAlert()), TelegramUtils.booleanToString(telegramUser.getEnableTelegramEarthquakeLocation()), TelegramUtils.booleanToString(telegramUser.getEnableTelegramEarthquakeImage()), TelegramUtils.booleanToString(telegramUser.getEnableTelegramEarthquakeMap()), telegramUser.getTsEarthquakeMaxDistArea1(), telegramUser.getTsEarthquakeMinMagnitudeArea1(), telegramUser.getTsEarthquakeMaxDistArea2(), telegramUser.getTsEarthquakeMinMagnitudeArea2(), telegramUser.getTsEarthquakeMinIntensity() + 1) +
                                    "*Потенциальные землетрясения*%nПолучать уведомления: %s%nПолучать геолокацию: %s%nПолучать картинку: %s%nПолучать карту: %s%nРадиус: %d км.%nУровень: %d%n%n".formatted(TelegramUtils.booleanToString(telegramUser.getEnableTelegramPossibleShakingAlert()), TelegramUtils.booleanToString(telegramUser.getEnableTelegramPossibleShakingLocation()), TelegramUtils.booleanToString(telegramUser.getEnableTelegramPossibleShakingImage()), TelegramUtils.booleanToString(telegramUser.getEnableTelegramPossibleShakingMap()), telegramUser.getTsPossibleShakingMaxDist(), telegramUser.getTsPossibleShakingMinLevel()) +
                                    "*Высокий уровень датчиков*%nПолучать уведомления: %s%nПолучать геолокацию: %s%nПолучать картинку: %s%nПолучать карту: %s%nЗона 1:%nРадиус: %d км.%nИнтенсивность: %d%nЗона 2:%nРадиус: %d км.%nИнтенсивность: %d%n%n".formatted(TelegramUtils.booleanToString(telegramUser.getEnableTelegramStationHighIntensityAlert()), TelegramUtils.booleanToString(telegramUser.getEnableTelegramStationHighIntensityLocation()), TelegramUtils.booleanToString(telegramUser.getEnableTelegramStationHighIntensityImage()), TelegramUtils.booleanToString(telegramUser.getEnableTelegramStationHighIntensityMap()), telegramUser.getTsStationMaxDist1(), telegramUser.getTsStationMinIntensity1(), telegramUser.getTsStationMaxDist2(), telegramUser.getTsStationMinIntensity2()) +
                                    "Позиция в списке: %d".formatted(order)), ctx.chatId());
                        } else {
                            getTelegramService().getSilent().send("Юзер не найден.", ctx.chatId());
                        }
                        GlobalQuakeClient.instance.getRegistry().counter("ability.used", "name", "userinfo", "user", ctx.user().getId().toString()).increment();
                    } catch (NumberFormatException e) {
                        getTelegramService().getSilent().send("Юзер не найден.", ctx.chatId());
                    }
                })
                .build();
    }
}
