package globalquake.telegram.abilities;

import globalquake.client.GlobalQuakeClient;
import globalquake.telegram.TelegramService;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;

import java.util.concurrent.atomic.AtomicLong;

import static org.telegram.telegrambots.abilitybots.api.objects.Flag.MESSAGE;
import static org.telegram.telegrambots.abilitybots.api.objects.Flag.REPLY;
import static org.telegram.telegrambots.abilitybots.api.objects.Locality.USER;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.ADMIN;

public class NotifyUserAbility extends AbstractAbility {
    public NotifyUserAbility(TelegramService telegramService) {
        super(telegramService);
    }

    public Ability notifyuser() {
        String broadcastMessage = "Введите сообщение для пользователя:";
        AtomicLong chatId = new AtomicLong(-1);

        return Ability.builder()
                .name("notifyuser")
                .info("Отправить сообщение конкретному юзеру.")
                .input(1)
                .locality(USER)
                .privacy(ADMIN)
                .action(ctx -> {
                    chatId.set(Long.parseLong(ctx.firstArg()));
                    getTelegramService().getSilent().forceReply(broadcastMessage, ctx.chatId());
                    GlobalQuakeClient.instance.getRegistry().counter("ability.used", "name", "notifyuser", "user", ctx.user().getId().toString()).increment();
                })
                .reply((baseAbilityBot, update) -> {
                    if (chatId.get() != -1) {
                        getTelegramService().getSilent().sendMd(update.getMessage().getText(), chatId.get());
                    }
                }, MESSAGE, REPLY, isReplyToBot(getTelegramService().getBotUsername()), isReplyToMessage(broadcastMessage))
                .build();
    }
}
