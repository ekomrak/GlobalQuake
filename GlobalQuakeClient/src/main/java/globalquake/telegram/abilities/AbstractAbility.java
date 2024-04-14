package globalquake.telegram.abilities;

import globalquake.telegram.TelegramService;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

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
}
