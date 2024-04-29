package globalquake.telegram.abilities;

import globalquake.client.GlobalQuakeClient;
import globalquake.telegram.TelegramService;
import io.github.resilience4j.ratelimiter.RateLimiter;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;

import static org.telegram.telegrambots.abilitybots.api.objects.Flag.MESSAGE;
import static org.telegram.telegrambots.abilitybots.api.objects.Flag.REPLY;
import static org.telegram.telegrambots.abilitybots.api.objects.Locality.USER;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.ADMIN;

public class BroadcastAbility extends AbstractAbility {
    public BroadcastAbility(TelegramService telegramService) {
        super(telegramService);
    }

    public Ability broadcast() {
        String broadcastMessage = "Введите сообщение для пользователей:";

        return Ability.builder()
                .name("broadcast")
                .info("Разослать сообщение всем юзерам.")
                .input(0)
                .locality(USER)
                .privacy(ADMIN)
                .action(ctx -> {
                    getTelegramService().getSilent().forceReply(broadcastMessage, ctx.chatId());
                    GlobalQuakeClient.instance.getRegistry().counter("ability.used", "name", "broadcast", "user", ctx.user().getId().toString()).increment();
                })
                .reply((baseAbilityBot, update) ->
                        GlobalQuakeClient.instance.getDatabaseService().listActiveUsers().parallelStream().forEach(telegramUser -> {
                            Runnable restrictedCall = RateLimiter.decorateRunnable(getTelegramService().getRateLimiter(), () -> getTelegramService().getSilent().sendMd(update.getMessage().getText(), telegramUser.getChatId()));
                            restrictedCall.run();
                        }), MESSAGE, REPLY, isReplyToBot(getTelegramService().getBotUsername()), isReplyToMessage(broadcastMessage)
                )
                .build();
    }
}
