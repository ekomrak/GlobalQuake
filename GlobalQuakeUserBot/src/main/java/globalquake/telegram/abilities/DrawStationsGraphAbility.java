package globalquake.telegram.abilities;

import globalquake.client.GlobalQuakeClient;
import globalquake.core.station.AbstractStation;
import globalquake.db.entities.TelegramUser;
import globalquake.telegram.TelegramService;
import globalquake.telegram.util.StationsGraphsDrawer;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.tinylog.Logger;

import java.io.IOException;

import static org.telegram.telegrambots.abilitybots.api.objects.Locality.USER;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

public class DrawStationsGraphAbility extends AbstractAbility {
    public DrawStationsGraphAbility(TelegramService telegramService) {
        super(telegramService);
    }

    public Ability map() {
        return Ability.builder()
                .name("stations")
                .info("По.")
                .input(0)
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> {
                    TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(ctx.user().getId());
                    if (telegramUser != null && telegramUser.getEnabled()) {
                        updateTelegramUser(telegramUser, ctx.user(), ctx.chatId());

                        AbstractStation pdgkStation = GlobalQuakeClient.instance.getStationManager().getStationByIdentifier("KZ PDGK BHZ ");

                        try {
                            if (Boolean.TRUE.equals(telegramUser.getSendMapAsAPhoto())) {
                                getTelegramService().getTelegramClient().execute(SendPhoto.builder().chatId(ctx.chatId()).photo(new InputFile(StationsGraphsDrawer.draw(pdgkStation), "Stations_%d.png".formatted(System.currentTimeMillis()))).build());
                            } else {
                                getTelegramService().getTelegramClient().execute(SendDocument.builder().chatId(ctx.chatId()).document(new InputFile(StationsGraphsDrawer.draw(pdgkStation), "Stations_%d.png".formatted(System.currentTimeMillis()))).build());
                            }
                            GlobalQuakeClient.instance.getRegistry().counter("ability.used", "name", "stations", "user", ctx.user().getId().toString()).increment();
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
