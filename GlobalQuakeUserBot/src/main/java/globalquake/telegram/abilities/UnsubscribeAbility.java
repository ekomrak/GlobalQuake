package globalquake.telegram.abilities;

import globalquake.client.GlobalQuakeClient;
import globalquake.db.CountCacheListType;
import globalquake.telegram.TelegramService;
import globalquake.db.entities.TelegramUser;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;

import static org.telegram.telegrambots.abilitybots.api.objects.Locality.USER;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

public class UnsubscribeAbility extends AbstractAbility {
    public UnsubscribeAbility(TelegramService telegramService) {
        super(telegramService);
    }

    public Ability unsubscribe() {
        return Ability.builder()
                .name("stop")
                .info("Отписаться от уведомлений.")
                .input(0)
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> {
                    TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(ctx.user().getId());
                    if (telegramUser != null) {
                        telegramUser.setEnabled(false);
                        updateTelegramUser(telegramUser, ctx.user(), ctx.chatId());
                        GlobalQuakeClient.instance.getDatabaseService().invalidateCountCache(CountCacheListType.ACTIVE);
                    } else {
                        insertTelegramUser(ctx.user(), ctx.chatId(), false);
                    }
                    getTelegramService().getSilent().send("Вы больше не будете получать уведомления.", ctx.chatId());
                    GlobalQuakeClient.instance.getRegistry().counter("ability.used", "name", "stop", "user", ctx.user().getId().toString()).increment();
                })
                .build();
    }
}
