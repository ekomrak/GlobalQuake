package globalquake.telegram.util;

import globalquake.core.Settings;
import globalquake.core.intensity.IntensityScales;
import globalquake.core.intensity.Level;
import globalquake.db.entities.TelegramUser;
import globalquake.telegram.data.TelegramClusterInfo;
import globalquake.telegram.data.TelegramEarthquakeInfo;
import globalquake.telegram.data.TelegramStationInfo;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class TelegramUtils {
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneId.systemDefault());

    private TelegramUtils() {
    }

    public static boolean canSend(TelegramEarthquakeInfo info, TelegramUser user, double distGCD, double pga) {
        double earthquakeThreshold = IntensityScales.INTENSITY_SCALES[Settings.tsEarthquakeIntensityScale].getLevels().get(user.getTsEarthquakeMinIntensity()).getPga();

        return (((info.getMag() >= user.getTsEarthquakeMinMagnitudeArea1()) && (distGCD <= user.getTsEarthquakeMaxDistArea1())) || ((info.getMag() >= user.getTsEarthquakeMinMagnitudeArea2()) && (distGCD <= user.getTsEarthquakeMaxDistArea2())) || (pga >= earthquakeThreshold));
    }

    public static boolean canSend(TelegramClusterInfo info, TelegramUser user, double distGCD) {
        return (info.getLevel() >= user.getTsPossibleShakingMinLevel()) && (distGCD <= user.getTsPossibleShakingMaxDist());
    }

    public static boolean canSend(TelegramStationInfo info, TelegramUser user, double distGCD) {
        return ((info.getIntensity() >= user.getTsStationMinIntensity1()) && (distGCD <= user.getTsStationMaxDist1()) || (info.getIntensity() >= user.getTsStationMinIntensity2()) && (distGCD <= user.getTsStationMaxDist2()));
    }

    public static String generateEarthquakeMessage(TelegramEarthquakeInfo info, double distGCD, double pga) {
        return generateEarthquakeMessage(info, distGCD, pga, false);
    }

    public static String generateEarthquakeMessage(TelegramEarthquakeInfo info, double distGCD, double pga, boolean test) {
        String header = test ? "<b>Выдуманное землетрясение.</b>\n" : "<b>Землетрясение обнаружено.</b>\n";
        return  header +
                "<b>" + "M%.1f".formatted(info.getMag()) + " " + info.getRegion() + "</b>\n" +
                "Расстояние: %.1f км. Глубина: %.1f км.%n".formatted(distGCD, info.getDepth()) +
                "MMI: " + formatLevel(IntensityScales.MMI.getLevel(pga)) + " / Shindo: " + formatLevel(IntensityScales.SHINDO.getLevel(pga)) + "\n" +
                "Время: " + Settings.formatDateTime(Instant.ofEpochMilli(info.getOrigin())) + "\n" +
                "Класс: " + (info.getQuality().isEmpty() ? "?" : info.getQuality());
    }

    public static String generateClusterMessage(TelegramClusterInfo info, double distGCD) {
        return "<b>Возможное землетрясение обнаружено.</b>\n" +
                "<b>Уровень:" + info.getLevel() + ". Расстояние: " + "%.1f".formatted(distGCD) + " км.</b>\n";
    }

    public static String generateStationMessage(String station, double intensity, double distGCD) {
        return "<b>Высокий уровень датчика.</b>\n" +
                "<b>" + station + "</b>\n" +
                "<b>Уровень: %.1f. Расстояние: %.1f км.</b>%n".formatted(intensity, distGCD);
    }

    public static String booleanToString(boolean boolValue) {
        if (boolValue) {
            return "да";
        } else {
            return "нет";
        }
    }

    public static String formatLevel(Level level) {
        if (level == null) {
            return "-";
        } else {
            return level.toString();
        }
    }
}
