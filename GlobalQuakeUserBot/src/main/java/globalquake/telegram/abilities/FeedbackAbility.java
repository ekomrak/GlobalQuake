package globalquake.telegram.abilities;

import globalquake.client.GlobalQuakeClient;
import globalquake.telegram.TelegramService;
import globalquake.db.entities.TelegramUser;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;

import static org.telegram.telegrambots.abilitybots.api.objects.Flag.MESSAGE;
import static org.telegram.telegrambots.abilitybots.api.objects.Flag.REPLY;
import static org.telegram.telegrambots.abilitybots.api.objects.Locality.USER;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

public class FeedbackAbility extends AbstractAbility {
    public FeedbackAbility(TelegramService telegramService) {
        super(telegramService);
    }

    public Ability feedback() {
        String feedbackMessage = "Введите ваше сообщение:";

        return Ability.builder()
                .name("feedback")
                .info("Отправить сообщение автору.")
                .input(0)
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> {
                    TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(ctx.user().getId());
                    if (telegramUser != null) {
                        updateTelegramUser(telegramUser, ctx.user(), ctx.chatId());
                    } else {
                        TelegramUser user = new TelegramUser(ctx.user(), ctx.chatId());
                        user.setEnabled(false);
                        GlobalQuakeClient.instance.getDatabaseService().insertTelegramUser(user);
                    }

                    getTelegramService().getSilent().forceReply(feedbackMessage, ctx.chatId());
                    GlobalQuakeClient.instance.getRegistry().counter("ability.used", "name", "feedback", "user", ctx.user().getId().toString()).increment();
                })
                .reply((baseAbilityBot, update) -> {
                    if (!update.getMessage().getText().isEmpty() && (update.getMessage().getText().charAt(0) != '/' || (update.getMessage().getText().charAt(0) == '/' && !getTelegramService().getAbilities().containsKey(StringUtils.strip(update.getMessage().getText(),"/"))))) {
                        getTelegramService().getSilent().send("UserId: %d. Username: %s%n%s".formatted(update.getMessage().getFrom().getId(), update.getMessage().getFrom().getUserName(), update.getMessage().getText()), getTelegramService().creatorId());
                        getTelegramService().getSilent().send("Спасибо за предоставленную обратную связь!", update.getMessage().getChatId());
                    } else {
                        getTelegramService().getSilent().send("Введено некорректное сообщение.", update.getMessage().getChatId());
                    }
                }, MESSAGE, REPLY, isReplyToBot(getTelegramService().getBotUsername()), isReplyToMessage(feedbackMessage))
                .build();
    }
}
