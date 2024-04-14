package globalquake.telegram.abilities;

import globalquake.telegram.TelegramService;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;

import static org.telegram.telegrambots.abilitybots.api.objects.Locality.USER;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.ADMIN;

public class TestAbility extends AbstractAbility {
    public TestAbility(TelegramService telegramService) {
        super(telegramService);
    }

    public Ability test() {
        return Ability.builder()
                .name("test")
                .info("Отправить тестовое уведомление.")
                .input(0)
                .locality(USER)
                .privacy(ADMIN)
                .action(ctx -> getTelegramService().sendTestEarthquake())
                .build();
    }
}
