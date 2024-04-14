package globalquake.telegram.abilities;

import globalquake.client.GlobalQuakeClient;
import globalquake.telegram.TelegramService;
import globalquake.db.entities.TelegramUser;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;

import static org.telegram.telegrambots.abilitybots.api.objects.Locality.USER;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

public class TestAbility extends AbstractAbility {
    public TestAbility(TelegramService telegramService) {
        super(telegramService);
    }

    public Ability test() {
        return Ability.builder()
                .name("test")
                .info("Отправить тестовое уведомление.")
                .input(0)
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> {
                    TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(ctx.user().getId());
                    if (telegramUser != null && telegramUser.getEnabled()) {
                        updateTelegramUser(telegramUser, ctx.user(), ctx.chatId());
                        getTelegramService().sendTestEarthquake(telegramUser);
                        GlobalQuakeClient.instance.getRegistry().counter("ability.used", "name", "test", "user", ctx.user().getId().toString()).increment();
                    } else {
                        sendNotActiveWarning(telegramUser, ctx.user(), ctx.chatId());
                    }
                })
                .build();
    }
}
