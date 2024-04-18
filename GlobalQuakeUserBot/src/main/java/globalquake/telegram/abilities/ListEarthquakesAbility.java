package globalquake.telegram.abilities;

import globalquake.client.GlobalQuakeClient;
import globalquake.core.Settings;
import globalquake.core.intensity.IntensityScales;
import globalquake.db.entities.ArchivedEarthquake;
import globalquake.db.entities.TelegramUser;
import globalquake.telegram.TelegramService;
import globalquake.telegram.util.EventImageDrawer;
import globalquake.telegram.util.TelegramUtils;
import globalquake.utils.GeoUtils;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.tinylog.Logger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.telegram.telegrambots.abilitybots.api.objects.Locality.USER;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.PUBLIC;

public class ListEarthquakesAbility extends AbstractAbility {
    public ListEarthquakesAbility(TelegramService telegramService) {
        super(telegramService);
    }

    public Ability listearthquakes() {
        return Ability.builder()
                .name("list")
                .info("Вывести информацию о последних 5 землетрясений.")
                .input(0)
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> {
                    TelegramUser telegramUser = GlobalQuakeClient.instance.getDatabaseService().findUserById(ctx.user().getId());
                    if (telegramUser != null && telegramUser.getEnabled()) {
                        updateTelegramUser(telegramUser, ctx.user(), ctx.chatId());


                        List<ArchivedEarthquake> earthquakes = GlobalQuakeClient.instance.getDatabaseService().listLastEarthquakes(telegramUser.getHomeLat(), telegramUser.getHomeLon(), 1000, 5);

                        StringBuilder text = new StringBuilder("Последние 5 землетрясений в радиусе 1000 км.:\n\n");
                        boolean first = true;
                        for (ArchivedEarthquake earthquake : earthquakes) {
                            LocalDateTime origin = earthquake.getOrigin();
                            ZoneId oldZone = ZoneId.of("UTC");
                            ZoneId newZone = ZoneId.of(Settings.timezoneStr);
                            LocalDateTime newDateTime = origin.atZone(oldZone).withZoneSameInstant(newZone).toLocalDateTime();

                            double distGCD = GeoUtils.greatCircleDistance(earthquake.getLatitude(), earthquake.getLongitude(), telegramUser.getHomeLat(), telegramUser.getHomeLon());
                            double dist = GeoUtils.geologicalDistance(earthquake.getLatitude(), earthquake.getLongitude(), -earthquake.getDepth(), telegramUser.getHomeLat(), telegramUser.getHomeLon(), 0);
                            double pga = GeoUtils.pgaFunction(earthquake.getMagnitude(), dist, earthquake.getDepth());

                            if (!first) {
                                text.append("\n");
                            }
                            text.append("*Дата: %s*%n".formatted(Settings.formatDateTime(newDateTime)) +
                                    "M%.1f %s%n".formatted(earthquake.getMagnitude(), earthquake.getRegion()) +
                                    "Расстояние: %.1f км. Глубина: %.1f км.%n".formatted(distGCD, earthquake.getDepth()) +
                                    "MMI: %s / Shindo: %s%n".formatted(TelegramUtils.formatLevel(IntensityScales.MMI.getLevel(pga)), TelegramUtils.formatLevel(IntensityScales.SHINDO.getLevel(pga))) +
                                    "Класс: %s%n".formatted(earthquake.getQualityClass()));
                            first = false;
                        }

                        Optional<Message> message = getTelegramService().getSilent().sendMd((text.toString()), ctx.chatId());
                        if (!earthquakes.isEmpty() && message.isPresent()) {
                            try {
                                if (Boolean.TRUE.equals(telegramUser.getSendImageAsAPhoto())) {
                                    getTelegramService().getTelegramClient().execute(SendPhoto.builder().chatId(ctx.chatId()).replyToMessageId(message.get().getMessageId()).photo(new InputFile(EventImageDrawer.drawEventsImage(telegramUser, earthquakes), "list_%d.png".formatted(System.currentTimeMillis()))).build());
                                } else {
                                    getTelegramService().getTelegramClient().execute(SendDocument.builder().chatId(ctx.chatId()).replyToMessageId(message.get().getMessageId()).document(new InputFile(EventImageDrawer.drawEventsImage(telegramUser, earthquakes), "list_%d.png".formatted(System.currentTimeMillis()))).build());
                                }
                            } catch (TelegramApiException | IOException e) {
                                Logger.error(e);
                            }
                        }
                        GlobalQuakeClient.instance.getRegistry().counter("ability.used", "name", "list", "user", ctx.user().getId().toString()).increment();
                    } else {
                        sendNotActiveWarning(telegramUser, ctx.user(), ctx.chatId());
                    }
                })
                .build();
    }
}
