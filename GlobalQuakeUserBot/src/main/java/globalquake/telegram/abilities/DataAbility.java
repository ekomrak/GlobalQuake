package globalquake.telegram.abilities;

import globalquake.client.GlobalQuakeClient;
import globalquake.db.entities.TelegramUser;
import globalquake.telegram.TelegramService;
import globalquake.telegram.util.DataExcelGenerator;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.tinylog.Logger;

import java.io.*;

import static org.telegram.telegrambots.abilitybots.api.objects.Locality.USER;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

public class DataAbility extends AbstractAbility {
    public DataAbility(TelegramService telegramService) {
        super(telegramService);
    }

    public Ability map() {
        return Ability.builder()
                .name("data")
                .info("Получить все землетрясения в виде xlsx.")
                .input(0)
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> {
                    TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(ctx.user().getId());
                    if (telegramUser != null && telegramUser.getEnabled()) {
                        updateTelegramUser(telegramUser, ctx.user(), ctx.chatId());

                        try {
                            getTelegramService().getTelegramClient().execute(SendDocument.builder().chatId(ctx.chatId()).document(new InputFile(DataExcelGenerator.generateExcel(GlobalQuakeClient.instance.getDatabaseService()), "Data_%d.xlsx".formatted(System.currentTimeMillis()))).build());
                            GlobalQuakeClient.instance.getRegistry().counter("ability.used", "name", "data", "user", ctx.user().getId().toString()).increment();
                        } catch (IOException | TelegramApiException e) {
                            Logger.error(e);
                        }
                    } else{
                        sendNotActiveWarning(telegramUser, ctx.user(), ctx.chatId());
                    }
                })
                .build();
    }
}
