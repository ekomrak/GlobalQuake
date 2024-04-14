package globalquake.telegram.abilities;

import globalquake.telegram.TelegramService;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
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
