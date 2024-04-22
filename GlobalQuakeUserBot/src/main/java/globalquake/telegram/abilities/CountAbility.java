package globalquake.telegram.abilities;

import globalquake.client.GlobalQuakeClient;
import globalquake.telegram.TelegramService;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;

import static org.telegram.telegrambots.abilitybots.api.objects.Locality.USER;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.ADMIN;

public class CountAbility extends AbstractAbility {
    public CountAbility(TelegramService telegramService) {
        super(telegramService);
    }

    public Ability count() {
        return Ability.builder()
                .name("count")
                .info("Вывести количество пользователей.")
                .input(0)
                .locality(USER)
                .privacy(ADMIN)
                .action(ctx -> {
                    Integer activeUsers = GlobalQuakeClient.instance.getDatabaseService().countActiveUsers();
                    Integer allUsers = GlobalQuakeClient.instance.getDatabaseService().countAllUsers();
                    getTelegramService().getSilent().send("Активных пользователей: %d (%.0f%%)%nВсего пользователей: %d".formatted(activeUsers, ((float) activeUsers / (float) allUsers) * 100, allUsers), ctx.chatId());
                    GlobalQuakeClient.instance.getRegistry().counter("ability.used", "name", "count", "user", ctx.user().getId().toString()).increment();
                })
                .build();
    }
}
