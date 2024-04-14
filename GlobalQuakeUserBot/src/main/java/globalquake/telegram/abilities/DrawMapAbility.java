package globalquake.telegram.abilities;

import globalquake.client.GlobalQuakeClient;
import globalquake.telegram.TelegramService;
import globalquake.db.entities.TelegramUser;
import globalquake.telegram.util.MapImageDrawer;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.tinylog.Logger;

import java.io.IOException;

import static org.telegram.telegrambots.abilitybots.api.objects.Locality.USER;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

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
                .privacy(PUBLIC)
                .action(ctx -> {
                    TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(ctx.user().getId());
                    if (telegramUser != null && telegramUser.getEnabled()) {
                        updateTelegramUser(telegramUser, ctx.user(), ctx.chatId());

                        try {
                            getTelegramService().getTelegramClient().execute(SendPhoto.builder().chatId(ctx.chatId()).photo(new InputFile(MapImageDrawer.instance.drawMap(telegramUser), "map_%d.png".formatted(System.currentTimeMillis()))).build());
                            GlobalQuakeClient.instance.getRegistry().counter("ability.used", "name", "map", "user", ctx.user().getId().toString()).increment();
                        } catch (TelegramApiException | IOException e) {
                            Logger.error(e);
                        }
                    } else {
                        sendNotActiveWarning(telegramUser, ctx.user(), ctx.chatId());
                    }
                })
                .build();
    }
}
