package globalquake.telegram.abilities;

import globalquake.client.GlobalQuakeClient;
import globalquake.telegram.TelegramService;

import globalquake.db.entities.TelegramUser;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.tinylog.Logger;

import static org.telegram.telegrambots.abilitybots.api.objects.Locality.USER;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

public class HelpAbility extends AbstractAbility {
    private static final String TEXT = """
            *Описание*
            Данный бот умеет уведомлять о землетрясениях, потенциальных землетрясениях и высоких уровнях датчиков\\.
            Задержка при получении уведомления от этого бота может доходить до нескольких минут, т\\.к\\. рассылка идет последовательно всем подписчикам\\. Самый первый в списке получит уведомление мгновенно, самый последний \\- с задержкой до нескольких минут в зависимости от количества подписчиков\\.
            Если у вас есть вопросы или предложения, вы можете отправить их разработчику через обратную связь /feedback\\.


            *Значения по умолчанию*
            Все эти значения каждый может поменять для себя через команду /settings

            __Координаты дома__
            Широта: 43\\.238949
            Долгота: 76\\.889709

            __Землетрясения__
            Бот умеет отслеживать 2 зоны и отправлять уведомления о землетрясениях при выполнении следующих условий\\.
            Зона 1: радиус до 100 км\\., магнитуда от 0
            Зона 2: радиус до 300 км\\., магнитуда от 5
            Уровень ощутимости: 2

            __Потенциальные землетрясения__
            Потенциальные землетрясения рассчитываются на основании показателей и поведении группы датчиков\\. Существует 5 уровней "предсказаний" \\(0, 1, 2, 3, 4\\)\\. Чем выше уровень, тем более верятно землетрясение\\. Уровень 0 \\- вероятность землетрясения меньше 5%\\. Начиная с уровня 2 вероятность уже более 50%\\.
            Радиус: 300 км\\.
            Минимальный уровень "предсказания": 2

            __Высокий уровень датчика__
            Зона 1: Радиус: 100 км\\., Интенсивность: 4000
            Зона 2: Радиус: 400 км\\., Интенсивность: 10000


            *Доступные команды*
            /stop \\- Отписаться от получаения уведомлений\\.
            /help \\- Получения подробной информации о боте\\.
            /settings \\- Задать свои значения для настроек бота\\.
            /mysettings \\- Вывести текущии значения личных настроек\\.
            /map \\- Прислать текущую карту GlobalQuake\\.
            /list \\- Последние 5 землетрясений в радиусе 1000 км\\.
            /test \\- Отправить тестовое сообщение о несуществующем землетрясении \\(проверка бота\\)\\.
            /feedback \\- Отправить сообщение разработчику бота\\.""";

    public HelpAbility(TelegramService telegramService) {
        super(telegramService);
    }

    public Ability help() {
        return Ability.builder()
                .name("help")
                .info("Подробная информация о возможностях бота.")
                .input(0)
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> {
                    TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(ctx.user().getId());
                    if (telegramUser != null && telegramUser.getEnabled()) {
                        updateTelegramUser(telegramUser, ctx.user(), ctx.chatId());
                        try {
                            getTelegramService().getTelegramClient().execute(SendMessage.builder().text(TEXT).chatId(ctx.chatId()).parseMode(ParseMode.MARKDOWNV2).build());
                        } catch (TelegramApiException e) {
                            Logger.error(e);
                        }
                        GlobalQuakeClient.instance.getRegistry().counter("ability.used", "name", "help", "user", ctx.user().getId().toString()).increment();
                    } else {
                        sendNotActiveWarning(telegramUser, ctx.user(), ctx.chatId());
                    }
                })
                .build();
    }
}
