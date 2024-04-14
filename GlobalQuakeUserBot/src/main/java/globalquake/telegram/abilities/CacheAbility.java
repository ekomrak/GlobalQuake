package globalquake.telegram.abilities;

import globalquake.client.GlobalQuakeClient;
import globalquake.telegram.TelegramService;
import globalquake.db.CacheListType;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;

import static org.telegram.telegrambots.abilitybots.api.objects.Locality.USER;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.ADMIN;

public class CacheAbility extends AbstractAbility {
    public CacheAbility(TelegramService telegramService) {
        super(telegramService);
    }

    public Ability cache() {
        return Ability.builder()
                .name("cache")
                .info("Сбросить кеш.")
                .input(1)
                .locality(USER)
                .privacy(ADMIN)
                .action(ctx -> {
                    String input = ctx.firstArg();
                    if ("all".equalsIgnoreCase(input)) {
                        GlobalQuakeClient.instance.getDatabaseService().invalidateAllCaches();
                        getTelegramService().getSilent().send("Все кеши сброшены.", ctx.chatId());
                    } else if ("list".equalsIgnoreCase(input)) {
                        GlobalQuakeClient.instance.getDatabaseService().invalidateAllLists();
                        getTelegramService().getSilent().send("Все списки сброшены.", ctx.chatId());
                    } else {
                        CacheListType cacheListType = CacheListType.findByName(input);
                        if (cacheListType != null) {
                            GlobalQuakeClient.instance.getDatabaseService().invalidateList(cacheListType);
                            getTelegramService().getSilent().send("Кеш сброшен.", ctx.chatId());
                        } else {
                            try {
                                long userId = Long.parseLong(input);
                                GlobalQuakeClient.instance.getDatabaseService().invalidateUser(userId);
                                getTelegramService().getSilent().send("Кеш сброшен.", ctx.chatId());
                            } catch (NumberFormatException e) {
                                getTelegramService().getSilent().send("Кеш не найден.", ctx.chatId());
                            }
                        }
                    }
                    GlobalQuakeClient.instance.getRegistry().counter("ability.used", "name", "cache", "user", ctx.user().getId().toString()).increment();
                })
                .build();
    }
}
