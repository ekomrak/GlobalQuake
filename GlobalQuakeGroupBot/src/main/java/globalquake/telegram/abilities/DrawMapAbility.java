package globalquake.telegram.abilities;

import globalquake.core.Settings;
import globalquake.telegram.TelegramService;
import globalquake.telegram.util.MapImageDrawer;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.tinylog.Logger;

import java.io.IOException;

import static org.telegram.telegrambots.abilitybots.api.objects.Locality.USER;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.ADMIN;

public class DrawMapAbility extends AbstractAbility {
    public DrawMapAbility(TelegramService telegramService) {
        super(telegramService);
    }

    public Ability map() {
        return Ability.builder()
                .name("map")
                .info("Прислать текущую карту.")
                .input(0)
                .locality(USER)
                .privacy(ADMIN)
                .action(ctx -> {
                    try {
                        if (Boolean.TRUE.equals(Settings.sendMapAsAPhoto)) {
                            getTelegramService().getTelegramClient().execute(SendPhoto.builder().chatId(Settings.telegramChatId).photo(new InputFile(MapImageDrawer.instance.drawMap(), "map_%d.png".formatted(System.currentTimeMillis()))).build());
                        } else {
                            getTelegramService().getTelegramClient().execute(SendDocument.builder().chatId(Settings.telegramChatId).document(new InputFile(MapImageDrawer.instance.drawMap(), "map_%d.png".formatted(System.currentTimeMillis()))).build());
                        }
                    } catch (TelegramApiException | IOException e) {
                        Logger.error(e);
                    }

                })
                .build();
    }
}
