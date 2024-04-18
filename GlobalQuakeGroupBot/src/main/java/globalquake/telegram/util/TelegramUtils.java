package globalquake.telegram.util;

import globalquake.client.data.ClientStation;
import globalquake.core.Settings;
import globalquake.core.earthquake.data.Cluster;
import globalquake.core.earthquake.data.Earthquake;
import globalquake.core.intensity.IntensityScales;
import globalquake.core.intensity.Level;

import java.time.Instant;

public final class TelegramUtils {
    private TelegramUtils() {}

    public static boolean canSend(Earthquake earthquake, double distGCD, double pga) {
        if (Boolean.FALSE.equals(Settings.enableTelegramEarthquakeAlert)) {
            return false;
        }

        double earthquakeThreshold = IntensityScales.INTENSITY_SCALES[Settings.tsEarthquakeIntensityScale].getLevels().get(Settings.tsEarthquakeMinIntensity).getPga();

        return (((earthquake.getMag() >= Settings.tsEarthquakeMinMagnitudeArea1) && (distGCD <= Settings.tsEarthquakeMaxDistArea1)) || ((earthquake.getMag() >= Settings.tsEarthquakeMinMagnitudeArea2) && (distGCD <= Settings.tsEarthquakeMaxDistArea2)) || (pga >= earthquakeThreshold));
    }

    public static boolean canSend(Cluster cluster, double distGCD) {
        if (Boolean.FALSE.equals(Settings.enableTelegramPossibleShakingAlert)) {
            return false;
        }

        return (cluster.getLevel() >= Settings.tsPossibleShakingMinLevel) && (distGCD <= Settings.tsPossibleShakingMaxDist);
    }

    public static boolean canSend(ClientStation clientStation, double distGCD) {
        if (Boolean.FALSE.equals(Settings.enableTelegramStationHighIntensityAlert)) {
            return false;
        }

        return ((clientStation.getMaxRatio60S() >= Settings.tsStationMinIntensity1) && (distGCD <= Settings.tsStationMaxDist1) || (clientStation.getMaxRatio60S() >= Settings.tsStationMinIntensity2) && (distGCD <= Settings.tsStationMaxDist2));
    }

    public static String generateEarthquakeMessage(Earthquake earthquake, double distGCD, double pga) {
        return generateEarthquakeMessage(earthquake, distGCD, pga, false);
    }

    public static String generateEarthquakeMessage(Earthquake earthquake, double distGCD, double pga, boolean test) {
        String quality = "?";
        if (earthquake.getHypocenter().quality != null) {
            quality = earthquake.getHypocenter().quality.getSummary().toString();
        }

        String header = test ? "<b>Выдуманное землетрясение.</b>\n" : "<b>Землетрясение обнаружено.</b>\n";
        return  header +
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

    private static String formatLevel(Level level) {
        if (level == null) {
            return "-";
        } else {
            return level.toString();
        }
    }
}
