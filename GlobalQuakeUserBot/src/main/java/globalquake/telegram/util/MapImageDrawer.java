package globalquake.telegram.util;

import globalquake.client.ClientSocketStatus;
import globalquake.client.GlobalQuakeClient;
import globalquake.core.GlobalQuake;
import globalquake.core.Settings;
import globalquake.core.earthquake.data.Cluster;
import globalquake.core.earthquake.data.Earthquake;
import globalquake.core.earthquake.data.Hypocenter;
import globalquake.core.earthquake.data.MagnitudeReading;
import globalquake.core.earthquake.quality.QualityClass;
import globalquake.core.geo.taup.TauPTravelTimeCalculator;
import globalquake.core.intensity.IntensityScales;
import globalquake.core.intensity.Level;
import globalquake.core.regions.Regions;
import globalquake.intensity.ShakeMap;
import globalquake.db.entities.TelegramUser;
import globalquake.ui.globalquake.feature.*;
import globalquake.ui.globe.GlobeRenderer;
import globalquake.ui.globe.RenderProperties;
import globalquake.ui.globe.feature.FeatureGeoPolygons;
import globalquake.utils.GeoUtils;
import globalquake.utils.Scale;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

public class MapImageDrawer {
    public static final String maxIntStr = "Max. Intensity";
    public static final Color GRAY_COLOR = new Color(20, 20, 20);
    private static final Color BLUE_COLOR = new Color(20, 20, 160);
    public static final DecimalFormat f4d = new DecimalFormat("0.0000", new DecimalFormatSymbols(Locale.ENGLISH));
    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;
    private static double scroll = 0.1;
    private final GlobeRenderer renderer;
    private final FeatureHomeLoc featureHomeLoc;
    public static MapImageDrawer instance;

    public MapImageDrawer() {
        instance = this;
        renderer = new GlobeRenderer();

        renderer.addFeature(new FeatureGeoPolygons(Regions.raw_polygonsUHDFiltered, 0, 0.25));
        renderer.addFeature(new FeatureGeoPolygons(Regions.raw_polygonsUS, 0, 0.5));
        renderer.addFeature(new FeatureGeoPolygons(Regions.raw_polygonsAK, 0, 0.5));
        renderer.addFeature(new FeatureGeoPolygons(Regions.raw_polygonsJP, 0, 0.5));
        renderer.addFeature(new FeatureGeoPolygons(Regions.raw_polygonsNZ, 0, 0.5));
        renderer.addFeature(new FeatureGeoPolygons(Regions.raw_polygonsHW, 0, 0.5));
        renderer.addFeature(new FeatureGeoPolygons(Regions.raw_polygonsIT, 0, 0.20));

        renderer.addFeature(new FeatureShakemap());
        renderer.addFeature(new FeatureGlobalStation(GlobalQuake.instance.getStationManager().getStations()));
        renderer.addFeature(new FeatureArchivedEarthquake(GlobalQuake.instance.getArchive().getArchivedQuakes()));
        renderer.addFeature(new FeatureEarthquake(GlobalQuake.instance.getEarthquakeAnalysis().getEarthquakes()));
        renderer.addFeature(new FeatureCluster(GlobalQuake.instance.getClusterAnalysis().getClusters()));
        renderer.addFeature(new FeatureCities());
        featureHomeLoc = new FeatureHomeLoc();
        renderer.addFeature(featureHomeLoc);
    }

    public InputStream drawMap(TelegramUser user) throws IOException {
        renderer.updateCamera(new RenderProperties(WIDTH, HEIGHT, user.getHomeLat(), user.getHomeLon(), scroll));
        featureHomeLoc.setPlaceholders(user.getHomeLat(), user.getHomeLon());

        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = img.createGraphics();

        renderer.render(g, renderer.getRenderProperties());

        drawEarthquakesBox(g, 0, 0);

        if (Boolean.TRUE.equals(Settings.displayAlertBox)) {
            drawAlertsBox(g);
        }

        if (Boolean.TRUE.equals(Settings.displayTime)) {
            g.setFont(new Font("Calibri", Font.BOLD, 24));
            g.setColor(Color.gray);

            String str = "----/--/-- --:--:--";
            if (GlobalQuake.instance.getSeedlinkReader() != null) {
                long time = GlobalQuake.instance.currentTimeMillis();

                if (time != 0) {
                    str = Settings.formatDateTime(Instant.ofEpochMilli(time));
                }

                if (GlobalQuake.instance.currentTimeMillis() - time < 1000 * 120) {
                    g.setColor(Color.white);
                }

                if (GlobalQuakeClient.instance.getClientSocket().getStatus() != ClientSocketStatus.CONNECTED) {
                    g.setColor(Color.red);
                }
            }

            g.drawString(str, WIDTH - g.getFontMetrics().stringWidth(str) - 6, HEIGHT - 9);
        }

        g.setFont(new Font("Calibri", Font.BOLD, 24));
        g.setColor(Color.gray);

        String str = "https://t.me/%s".formatted(Settings.telegramBotUsername);
        g.drawString(str, WIDTH - g.getFontMetrics().stringWidth(str) - 6, HEIGHT - g.getFontMetrics().getHeight() - 15);

        g.dispose();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", os);

        return new ByteArrayInputStream(os.toByteArray());
    }

    private void drawEarthquakesBox(Graphics2D g, int x, int y) {
        List<Earthquake> quakes = GlobalQuake.instance.getEarthquakeAnalysis().getEarthquakes().stream().filter(earthquake -> {
            double distGCD = GeoUtils.greatCircleDistance(earthquake.getLat(), earthquake.getLon(), Settings.homeLat, Settings.homeLon);
            return (((earthquake.getMag() >= Settings.tsEarthquakeMinMagnitudeArea1) && (distGCD <= Settings.tsEarthquakeMaxDistArea1)) || ((earthquake.getMag() >= Settings.tsEarthquakeMinMagnitudeArea2) && (distGCD <= Settings.tsEarthquakeMaxDistArea2)) || !Settings.enableLimitedEarthquakes);
        }).toList();
        int displayedQuake = quakes.isEmpty() ? -1 : (int) ((System.currentTimeMillis() / 5000) % (quakes.size()));

        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setStroke(new BasicStroke(1f));
        String string = "No Earthquakes Detected";

        int baseWidth = g.getFontMetrics().stringWidth(string) + 12;

        Earthquake quake = null;
        try {
            if (!quakes.isEmpty()) {
                quake = quakes.get(displayedQuake);
            }
        } catch (Exception ignored) {
        }

        int xOffset = 0;
        int baseHeight = quake == null ? 24 : 132;

        ShakeMap shakeMap = quake == null ? null : GlobalQuakeClient.instance.getShakemapService().getShakeMaps().get(quake.getUuid());

        Level level = shakeMap == null ? null : IntensityScales.getIntensityScale().getLevel(shakeMap.getMaxPGA());
        Color levelColor = level == null ? Color.gray : level.getColor();

        Font regionFont = new Font("Calibri", Font.BOLD, 18);
        Font quakeFont = new Font("Calibri", Font.BOLD, 22);

        String quakeString = null;

        if (quake != null) {
            quakeString = "%s Earthquake detected".formatted(quake.magnitudeFormatted());
            xOffset = getIntensityBoxWidth(g) + 4;
            g.setFont(regionFont);
            baseWidth = Math.max(baseWidth + xOffset, g.getFontMetrics().stringWidth(quake.getRegion()) + xOffset + 10);

            g.setFont(quakeFont);
            baseWidth = Math.max(baseWidth, g.getFontMetrics().stringWidth(quakeString) + 120);

            g.setColor(levelColor);
        } else {
            g.setColor(new Color(0, 90, 192));
        }

        RoundRectangle2D mainRect = new RoundRectangle2D.Float(0, 0, baseWidth, baseHeight, 10, 10);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.fill(mainRect);
        g.setColor(GRAY_COLOR);
        g.fillRect(x + 2, y + 26, baseWidth - 4, baseHeight - 28);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        if (quake == null) {
            g.setColor(Color.white);
            g.drawString(string, x + 6, y + 19);
        } else {
            drawIntensityBox(g, level, 4, y + 28, baseHeight - 32);

            Cluster cluster = quake.getCluster();
            if (cluster != null) {
                Hypocenter hypocenter = cluster.getPreviousHypocenter();
                if (hypocenter != null) {
                    g.setFont(new Font("Calibri", Font.BOLD, 18));
                    String str;

                    if (quakes.size() > 1) {

                        str = (displayedQuake + 1) + "/" + quakes.size();
                        int _x = x + baseWidth - 5 - g.getFontMetrics().stringWidth(str);

                        RoundRectangle2D rectNum = new RoundRectangle2D.Float(_x - 3, y + 3, g.getFontMetrics().stringWidth(str) + 6, 20, 10, 10);
                        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g.setColor(new Color(0, 0, 0, 100));
                        g.fill(rectNum);

                        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                        g.setColor(isDark(levelColor) ? Color.white : Color.black);
                        g.drawString(str, _x, y + 19);
                    }

                    g.setFont(quakeFont);
                    g.setColor(isDark(levelColor) ? Color.white : Color.black);
                    g.drawString(quakeString, x + 3, y + 21);

                    String sim = GlobalQuake.instance.isSimulation() ? " (Simulated)":"";

                    g.setColor(Color.white);
                    g.setFont(regionFont);
                    g.drawString(quake.getRegion(), x + xOffset + 3, y + 44);

                    g.setColor(GlobalQuake.instance.isSimulation() ? Color.orange : Color.white);
                    g.drawString("%s%s".formatted(Settings.formatDateTime(Instant.ofEpochMilli(quake.getOrigin())), sim), x + xOffset + 3, y + 66);

                    g.setColor(Color.white);
                    g.setFont(new Font("Calibri", Font.BOLD, 16));
                    g.drawString("lat: " + f4d.format(quake.getLat()) + " lon: " + f4d.format(quake.getLon()), x + xOffset + 3, y + 85);
                    g.drawString("Depth: %s %s".formatted(
                                    Settings.getSelectedDistanceUnit().format(quake.getDepth(), 1),
                                    hypocenter.depthFixed ? "(fixed)" : ""),
                            x + xOffset + 3, y + 104);
                    str = "Revision no. " + quake.getRevisionID();
                    g.drawString(str, x + xOffset + 3, y + 123);

                    if (hypocenter.quality != null) {
                        QualityClass summaryQuality = hypocenter.quality.getSummary();

                        drawAccuracyBox(g, true, "Quality: ", x + baseWidth + 2, y + 122, summaryQuality.toString(), summaryQuality.getColor());
                    }
                }
            }
        }
    }

    private void drawAlertsBox(Graphics2D g) {
        Earthquake quake = null;
        double maxPGA = 0.0;
        double distGC = 0;

        int secondsP = 0;
        int secondsS = 0;

        // Select quake to be displayed

        for (Earthquake earthquake : GlobalQuake.instance.getEarthquakeAnalysis().getEarthquakes()) {
            double _dist = GeoUtils.geologicalDistance(earthquake.getLat(), earthquake.getLon(), -earthquake.getDepth(), Settings.homeLat, Settings.homeLon, 0);
            double pga = GeoUtils.pgaFunction(earthquake.getMag(), _dist, earthquake.getDepth());
            if (pga > maxPGA) {
                maxPGA = pga;

                double _distGC = GeoUtils.greatCircleDistance(earthquake.getLat(), earthquake.getLon(), Settings.homeLat, Settings.homeLon);
                double age = (GlobalQuake.instance.currentTimeMillis() - earthquake.getOrigin()) / 1000.0;

                double pTravel = (TauPTravelTimeCalculator.getPWaveTravelTime(earthquake.getDepth(),
                        TauPTravelTimeCalculator.toAngle(_distGC)));
                double sTravel = (TauPTravelTimeCalculator.getSWaveTravelTime(earthquake.getDepth(),
                        TauPTravelTimeCalculator.toAngle(_distGC)));

                int _secondsP = (int) Math.ceil(pTravel - age);
                int _secondsS = (int) Math.ceil(sTravel - age);

                if (_secondsS < -60 * 5) {
                    continue; // S wave already passed
                }

                if (pga > IntensityScales.INTENSITY_SCALES[Settings.shakingLevelScale].getLevels().get(Settings.shakingLevelIndex).getPga()) {
                    quake = earthquake;
                    distGC = _distGC;
                    secondsS = sTravel >= 0 ? Math.max(0, _secondsS) : 0;
                    secondsP = pTravel >= 0 ? Math.max(0, _secondsP) : 0;
                }
            }
        }

        if (quake == null) {
            return;
        }

        int width = 400;
        int x = WIDTH / 2 - width / 2;
        int height;

        Color color;

        String str;

        g.setFont(new Font("Calibri", Font.BOLD, 16));

        height = 136;
        color = new Color(0, 90, 192);
        g.setFont(new Font("Calibri", Font.BOLD, 22));
        str = distGC <= 200 ? "Earthquake detected nearby!" : "Earthquake detected!";

        if (maxPGA >= IntensityScales.INTENSITY_SCALES[Settings.shakingLevelScale].getLevels().get(Settings.shakingLevelIndex).getPga()) {
            color = new Color(255, 200, 0);
            str = "Shaking is expected!";
        }

        if (maxPGA >= IntensityScales.INTENSITY_SCALES[Settings.strongShakingLevelScale].getLevels().get(Settings.strongShakingLevelIndex).getPga()) {
            color = new Color(200, 50, 0);
            str = "Strong shaking is expected!";
        }

        int y = HEIGHT - height;

        RoundRectangle2D.Double rect = new RoundRectangle2D.Double(x, y, width, height, 10, 10);
        g.setColor(color);
        g.fill(rect);

        Rectangle2D.Double rect2 = new Rectangle2D.Double(x + 2, y + 28, width - 4, height - 30);
        g.setColor(Color.black);
        g.fill(rect2);

        g.setColor(isDark(color) ? Color.white : Color.black);
        g.drawString(str, x + width / 2 - g.getFontMetrics().stringWidth(str) / 2, y + g.getFont().getSize());

        Level level = IntensityScales.getIntensityScale().getLevel(maxPGA);

        drawIntensityBox(g, level, x + 4, y + 30, height - 34);
        g.setFont(new Font("Calibri", Font.BOLD, 16));
        drawAccuracyBox(g, true, "", x + width + 2, y + 46, "%s".formatted(quake.magnitudeFormatted()), Scale.getColorEasily(quake.getMag() / 8.0));

        int intW = getIntensityBoxWidth(g);
        int _x = x + intW + 8;

        g.setColor(Color.white);
        g.setFont(new Font("Calibri", Font.BOLD, 17));

        str = "Distance: %s".formatted(Settings.getSelectedDistanceUnit().format(distGC, 1));
        g.drawString(str, _x, y + 48);
        str = "Depth: %s".formatted(Settings.getSelectedDistanceUnit().format(quake.getDepth(), 1));
        g.drawString(str, _x, y + 72);

        drawAccuracyBox(g, false, "P Wave arrival: ", x + intW + 15, y + 96, "%ds".formatted(secondsP), secondsP == 0 ? Color.gray : new Color(0, 100, 220));
        drawAccuracyBox(g, false, "S Wave arrival: ", x + intW + 15, y + 122, "%ds".formatted(secondsS), secondsS == 0 ? Color.gray : new Color(255, 50, 0));

        Path2D path = new Path2D.Double();
        int s = 70;
        path.moveTo(x + width - s - 6, y + height - 6);
        path.lineTo(x + width - 6, y + height - 6);
        path.lineTo(x + width - s / 2.0 - 6, y + height - s * 0.8 - 6);
        path.closePath();

        g.setColor(color);
        g.fill(path);

        g.setColor(Color.white);
        g.draw(path);

        g.setColor(isDark(color) ? Color.white : Color.black);
        g.setFont(new Font("Calibri", Font.BOLD, 36));
        g.drawString("!", x + width - s / 2 - g.getFontMetrics().stringWidth("!") / 2 - 6, y + height - 16);
    }

    private static void drawIntensityBox(Graphics2D g, Level level, int x, int y, int height) {
        int width = getIntensityBoxWidth(g);
        RoundRectangle2D.Double rectShindo = new RoundRectangle2D.Double(x, y, width, height, 10, 10);
        g.setStroke(new BasicStroke(1f));
        Color col = BLUE_COLOR;

        if (level != null) {
            col = level.getColor();
            col = new Color(
                    (int) (col.getRed() * IntensityScales.getIntensityScale().getDarkeningFactor()),
                    (int) (col.getGreen() * IntensityScales.getIntensityScale().getDarkeningFactor()),
                    (int) (col.getBlue() * IntensityScales.getIntensityScale().getDarkeningFactor()));
        }

        g.setColor(col);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.fill(rectShindo);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        g.setColor(Color.white);
        g.setFont(new Font("Calibri", Font.BOLD, 10));
        g.drawString(maxIntStr, x + 2, y + 12);
        String str1 = "Estimated";
        g.drawString(str1, x + (int) (width * 0.5 - 0.5 * g.getFontMetrics().stringWidth(str1)), y + 26);

        String str3 = "-";
        if (level != null) {
            str3 = level.getName();
        }

        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, height / 2));
        int x3 = x + (int) (width * 0.5 - 0.5 * g.getFontMetrics().stringWidth(str3));

        int w3 = g.getFontMetrics().stringWidth(str3);
        g.drawString(str3, x3, y + height / 2 + 22);

        if (level != null && level.getSuffix() != null) {
            g.setColor(Color.white);
            g.setFont(new Font("Arial", Font.PLAIN, 36));
            g.drawString(level.getSuffix(), x3 + w3 / 2 + 12, y + 50);
        }

        g.setColor(Color.white);
        g.setFont(new Font("Calibri", Font.BOLD, 11));
        String str = IntensityScales.getIntensityScale().getNameShort();
        g.drawString(str, x + (int) (width * 0.5 - 0.5 * g.getFontMetrics().stringWidth(str)), y + height - 4);
    }

    public static int getIntensityBoxWidth(Graphics2D g) {
        g.setFont(new Font("Calibri", Font.BOLD, 10));
        return g.getFontMetrics().stringWidth(maxIntStr) + 6;
    }

    public static boolean isDark(Color color) {
        double darkness = 1 - (0.6 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
        return darkness >= 0.5;
    }

    public static void drawAccuracyBox(Graphics2D g, boolean alignRight, String str, int x, int y, String v, Color color) {
        g.setColor(Color.white);

        int space = 6;
        int pad = 6;
        int size1 = g.getFontMetrics().stringWidth(str);
        int size2 = g.getFontMetrics().stringWidth(v);
        int size = size1 + size2 + space + pad + 3;

        int _x = alignRight ? x - size : x;

        g.drawString(str, _x, y);

        RoundRectangle2D.Double rect = new RoundRectangle2D.Double(
                _x + space + size1 - pad / 2.0,
                y - g.getFont().getSize() + 1,
                size2 + pad,
                g.getFont().getSize() + 4, 10, 10);
        g.setColor(color);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.fill(rect);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        g.setColor(isDark(color) ? Color.white : Color.black);
        g.drawString(v, _x + size1 + space, y + g.getFont().getSize() / 2 - 7);
    }

    private static int drawMags(Graphics2D g, Earthquake quake, int y) {
        g.setStroke(new BasicStroke(1f));

        String str = "Magnitude";
        g.setFont(new Font("Calibri", Font.BOLD, 12));

        int startX = 16;
        int hh = 200;
        int ww = g.getFontMetrics().stringWidth(str) - 12;

        g.setColor(Color.black);
        g.fillRect(startX - 20, y - 20, ww + 20, hh + 20);
        g.fillRect(startX - 20, y - 20, ww + 32, 24);

        g.setColor(Color.white);
        g.drawRect(startX, y, ww, hh);

        g.drawString(str, 10, y - 5);


        for (int mag = 1; mag <= 9; mag++) {
            double y0 = y + hh * (10 - mag) / 10.0;
            g.setColor(Color.white);
            g.setFont(new Font("Calibri", Font.BOLD, 12));
            g.drawString(mag + "", startX - g.getFontMetrics().stringWidth(mag + "") - 5, (int) (y0 + 5));
            g.draw(new Line2D.Double(startX, y0, startX + 4, y0));
            g.draw(new Line2D.Double(startX + ww - 4, y0, startX + ww, y0));
        }

        Hypocenter hypocenter = quake.getHypocenter();
        List<MagnitudeReading> mags = hypocenter.mags;

        if (mags != null) {
            int[] bins = new int[100];

            for (MagnitudeReading magnitudeReading : mags) {
                int bin = (int) (magnitudeReading.magnitude() * 10.0);
                if (bin >= 0 && bin < 100) {
                    bins[bin]++;
                }
            }

            int max = 1;

            for (int count : bins) {
                if (count > max) {
                    max = count;
                }
            }

            for (int i = 0; i < bins.length; i++) {
                int n = bins[i];
                if (n == 0) {
                    continue;
                }
                double mag = i / 10.0;
                double y0 = y + hh * (10 - mag) / 10;
                double y1 = y + hh * (10 - (mag + 0.1)) / 10;
                double w = Math.min(ww, (n / (double) max) * ww);
                g.setColor(Scale.getColorEasily(mag / 8.0));
                g.fill(new Rectangle2D.Double(startX + 1, y1, w, y0 - y1));
            }
        }

        return ww;
    }
}
