package globalquake.telegram.abilities;

import globalquake.client.GlobalQuakeClient;
import globalquake.telegram.TelegramService;
import globalquake.db.entities.TelegramUser;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.tinylog.Logger;

import static org.telegram.telegrambots.abilitybots.api.objects.Locality.USER;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

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
                .privacy(PUBLIC)
                .action(ctx -> {
                    TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(ctx.user().getId());
                    if (telegramUser != null && telegramUser.getEnabled()) {
                        updateTelegramUser(telegramUser, ctx.user(), ctx.chatId());

                        InlineKeyboardMarkup markupInline = InlineKeyboardMarkup.builder()
                                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Основные настройки").callbackData("general_settings").build(), InlineKeyboardButton.builder().text("Сейсмограммы").callbackData("graphs_settings").build()))
                                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Координаты дома").callbackData("home_settings").build(), InlineKeyboardButton.builder().text("Потенциальные землетрясения").callbackData("cluster_settings").build()))
                                .keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Землетрясения").callbackData("earthquake_settings").build(), InlineKeyboardButton.builder().text("Датчики").callbackData("station_settings").build())).build();

                        try {
                            getTelegramService().getTelegramClient().execute(SendMessage.builder().text("Выберите настройки, которые хотите поменять:").chatId(ctx.chatId()).replyMarkup(markupInline).build());
                            GlobalQuakeClient.instance.getRegistry().counter("ability.used", "name", "settings", "user", ctx.user().getId().toString()).increment();
                        } catch (TelegramApiException e) {
                            Logger.error(e);
                        }
                    } else {
                        sendNotActiveWarning(telegramUser, ctx.user(), ctx.chatId());
                    }
                })
                .build();
    }
}
