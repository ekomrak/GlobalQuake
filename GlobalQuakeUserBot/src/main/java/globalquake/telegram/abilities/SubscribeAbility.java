package globalquake.telegram.abilities;

import globalquake.client.GlobalQuakeClient;
import globalquake.telegram.TelegramService;
import globalquake.db.entities.TelegramUser;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;

import static org.telegram.telegrambots.abilitybots.api.objects.Locality.USER;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

public class SubscribeAbility extends AbstractAbility {
    public SubscribeAbility(TelegramService telegramService) {
        super(telegramService);
    }

    public Ability subscribe() {
        return Ability.builder()
                .name("start")
                .info("Подписаться на уведомления.")
                .input(0)
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> {
                    TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(ctx.user().getId());
                    if (telegramUser == null) {
                        insertTelegramUser(ctx.user(), ctx.chatId(), true);
                        GlobalQuakeClient.instance.getDatabaseService().invalidateAllLists();
                    } else {
                        telegramUser.setEnabled(true);
                        updateTelegramUser(telegramUser, ctx.user(), ctx.chatId());
                    }

                    getTelegramService().getSilent().send("""
                            Теперь вы будете получать уведомления о землетрясениях.
                            Вы всегда можете отказаться от уведомлений отправив команду /stop.
                            Если вам понадобится подробная информация о возможностях бота, отправьте команду /help.
                            Для персонализированной настройки отправьте команду /settings.""", ctx.chatId());
                    GlobalQuakeClient.instance.getRegistry().counter("ability.used", "name", "start", "user", ctx.user().getId().toString()).increment();
                })
                .build();
    }
}