package globalquake.telegram.abilities;

import globalquake.telegram.TelegramService;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.tinylog.Logger;

import static org.telegram.telegrambots.abilitybots.api.objects.Locality.USER;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.ADMIN;

public class SettingsAbility extends AbstractAbility {
    public SettingsAbility(TelegramService telegramService) {
        super(telegramService);
    }

    public Ability settingsability() {
        return Ability.builder()
                .name("settings")
                .info("Персональные настройки бота.")
                .input(0)
                .locality(USER)
                .privacy(ADMIN)
                .action(ctx -> {
                    InlineKeyboardMarkup markupInline = InlineKeyboardMarkup.builder()
                            .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Основные настройки").callbackData("general_settings").build()))
                            .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Координаты дома").callbackData("home_settings").build(), InlineKeyboardButton.builder().text("Потенциальные землетрясения").callbackData("cluster_settings").build()))
                            .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Землетрясения").callbackData("earthquake_settings").build(), InlineKeyboardButton.builder().text("Датчики").callbackData("station_settings").build())).build();

                    try {
                        getTelegramService().getTelegramClient().execute(SendMessage.builder().text("Выберите настройки, которые хотите поменять:").chatId(ctx.chatId()).replyMarkup(markupInline).build());
                    } catch (TelegramApiException e) {
                        Logger.error(e);
                    }
                })
                .build();
    }
}
