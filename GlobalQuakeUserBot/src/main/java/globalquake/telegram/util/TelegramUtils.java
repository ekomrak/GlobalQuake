package globalquake.telegram.util;

import globalquake.client.data.ClientStation;
import globalquake.core.Settings;
import globalquake.core.earthquake.data.Cluster;
import globalquake.core.earthquake.data.Earthquake;
import globalquake.core.intensity.IntensityScales;
import globalquake.core.intensity.Level;
import globalquake.db.entities.TelegramUser;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class TelegramUtils {
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneId.systemDefault());

    private TelegramUtils() {
    }

    public static boolean canSend(Earthquake earthquake, TelegramUser user, double distGCD, double pga) {
        double earthquakeThreshold = IntensityScales.INTENSITY_SCALES[Settings.tsEarthquakeIntensityScale].getLevels().get(user.getTsEarthquakeMinIntensity()).getPga();

        return (((earthquake.getMag() >= user.getTsEarthquakeMinMagnitudeArea1()) && (distGCD <= user.getTsEarthquakeMaxDistArea1())) || ((earthquake.getMag() >= user.getTsEarthquakeMinMagnitudeArea2()) && (distGCD <= user.getTsEarthquakeMaxDistArea2())) || (pga >= earthquakeThreshold));
    }

    public static boolean canSend(Cluster cluster, TelegramUser user, double distGCD) {
        return (cluster.getLevel() >= user.getTsPossibleShakingMinLevel()) && (distGCD <= user.getTsPossibleShakingMaxDist());
    }

    public static boolean canSend(ClientStation clientStation, TelegramUser user, double distGCD) {
        return ((clientStation.getMaxRatio60S() >= user.getTsStationMinIntensity1()) && (distGCD <= user.getTsStationMaxDist1()) || (clientStation.getMaxRatio60S() >= user.getTsStationMinIntensity2()) && (distGCD <= user.getTsStationMaxDist2()));
    }

    public static String generateEarthquakeMessage(Earthquake earthquake, double distGCD, double pga) {
        String quality = "?";
        if (earthquake.getHypocenter().quality != null) {
            quality = earthquake.getHypocenter().quality.getSummary().toString();
        }

        return "<b>Землетрясение обнаружено.</b>\n" +
                "<b>" + "M%.1f".formatted(earthquake.getMag()) + " " + earthquake.getRegion() + "</b>\n" +
                "Расстояние: %.1f км. Глубина: %.1f км.%n".formatted(distGCD, earthquake.getDepth()) +
                "MMI: " + formatLevel(IntensityScales.MMI.getLevel(pga)) + " / Shindo: " + formatLevel(IntensityScales.SHINDO.getLevel(pga)) + "\n" +
                "Время: " + Settings.formatDateTime(Instant.ofEpochMilli(earthquake.getOrigin())) + "\n" +
                "Класс: " + quality;
    }

    public static String generateClusterMessage(Cluster cluster, double distGCD) {
        return "<b>Возможное землетрясение обнаружено.</b>\n" +
                "<b>Уровень:" + cluster.getLevel() + ". Расстояние: " + "%.1f".formatted(distGCD) + " км.</b>\n";
    }

    public static String generateStationMessage(ClientStation station, double distGCD) {
        return "<b>Высокий уровень датчика.</b>\n" +
                "<b>" + station + "</b>\n" +
                "<b>Уровень: %.1f. Расстояние: %.1f км.</b>%n".formatted(station.getMaxRatio60S(), distGCD);
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
