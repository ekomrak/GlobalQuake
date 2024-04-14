package globalquake.telegram.abilities;

import globalquake.client.GlobalQuakeClient;
import globalquake.telegram.TelegramService;
import globalquake.db.entities.TelegramUser;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.tinylog.Logger;

import java.util.function.Predicate;

public class AbstractAbility implements AbilityExtension {
    private final TelegramService telegramService;

    public AbstractAbility(TelegramService telegramService) {
        this.telegramService = telegramService;
    }

    protected TelegramService getTelegramService() {
        return telegramService;
    }

    protected void insertTelegramUser(User user, long chatId, boolean enabled) {
        TelegramUser telegramUser = new TelegramUser(user, chatId);
        telegramUser.setEnabled(enabled);
        GlobalQuakeClient.instance.getDatabaseService().insertTelegramUser(telegramUser);
    }

    protected void updateTelegramUser(TelegramUser telegramUser, User user, long chatId) {
        telegramUser.updateWith(user, chatId);
        GlobalQuakeClient.instance.getDatabaseService().updateTelegramUser(telegramUser);
    }

    protected void sendNotActiveWarning(TelegramUser telegramUser, User user, long chatId) {
        if (telegramUser == null) {
            insertTelegramUser(user, chatId, false);
        }
        getTelegramService().getSilent().send("Для начала работы с ботом отправьте команду /start.", chatId);
        GlobalQuakeClient.instance.getRegistry().counter("not.active.warning", "user", user.getId().toString()).increment();
    }

    protected Predicate<Update> isReplyToMessage(String message) {
        return upd -> {
            Message reply = upd.getMessage().getReplyToMessage();
            return reply.hasText() && reply.getText().equalsIgnoreCase(message);
        };
    }

    protected Predicate<Update> isReplyToBot(String botUsername) {
        return upd -> upd.getMessage().getReplyToMessage().getFrom().getUserName().equalsIgnoreCase(botUsername);
    }

    protected void sendInlineKeyboard(InlineKeyboardMarkup markupInline, long chatId, Integer messageId) {
        try {
            if (messageId == null) {
                getTelegramService().getTelegramClient().execute(SendMessage.builder()
                        .chatId(chatId)
                        .text("Выберите параметр, который хотите изменить:")
                        .replyMarkup(markupInline)
                        .build());
            } else {
                getTelegramService().getTelegramClient().execute(EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .text("Выберите параметр, который хотите изменить:")
                        .replyMarkup(markupInline)
                        .build());
            }
        } catch (TelegramApiException e) {
            Logger.error(e);
        }
    }
}
