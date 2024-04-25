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
                .info("Получить сейсмограммы.")
                .input(0)
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> {
                    TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(ctx.user().getId());
                    if (telegramUser != null && telegramUser.getEnabled()) {
                        updateTelegramUser(telegramUser, ctx.user(), ctx.chatId());

                        if (telegramUser.getSendPDGKStation()) {
                            AbstractStation pdgkStation = GlobalQuakeClient.instance.getStationManager().getStationByIdentifier("KZ PDGK BHZ ");
                            sendStationGraph(pdgkStation, telegramUser, ctx.chatId());
                        }
                        if (telegramUser.getSendANANStation()) {
                            AbstractStation ananStation = GlobalQuakeClient.instance.getStationManager().getStationByIdentifier("AD ANAN HNZ ");
                            sendStationGraph(ananStation, telegramUser, ctx.chatId());
                        }
                        if (telegramUser.getSendTMCHStation()) {
                            AbstractStation tmchStation = GlobalQuakeClient.instance.getStationManager().getStationByIdentifier("AD TMCH HNZ ");
                            sendStationGraph(tmchStation, telegramUser, ctx.chatId());
                        }
                    } else {
                        sendNotActiveWarning(telegramUser, ctx.user(), ctx.chatId());
                    }
                })
                .build();
    }

    private void sendStationGraph(AbstractStation station, TelegramUser telegramUser, long chatId) {
        try {
            if (Boolean.TRUE.equals(telegramUser.getSendGraphsAsAPhoto())) {
                getTelegramService().getTelegramClient().execute(SendPhoto.builder().chatId(chatId).photo(new InputFile(StationsGraphsDrawer.draw(station), "Stations_%d.png".formatted(System.currentTimeMillis()))).build());
            } else {
                getTelegramService().getTelegramClient().execute(SendDocument.builder().chatId(chatId).document(new InputFile(StationsGraphsDrawer.draw(station), "Stations_%d.png".formatted(System.currentTimeMillis()))).build());
            }
            GlobalQuakeClient.instance.getRegistry().counter("ability.used", "name", "stations", "user", telegramUser.getId().toString()).increment();
        } catch (TelegramApiException | IOException e) {
            Logger.error(e);
        }
    }
}
