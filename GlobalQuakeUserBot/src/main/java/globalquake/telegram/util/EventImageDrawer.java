package globalquake.telegram.util;

import globalquake.core.Settings;
import globalquake.core.analysis.Event;
import globalquake.core.earthquake.data.Cluster;
import globalquake.core.earthquake.data.Hypocenter;
import globalquake.core.regions.GQPolygon;
import globalquake.core.regions.Regions;
import globalquake.db.entities.ArchivedEarthquake;
import globalquake.db.entities.TelegramUser;
import globalquake.telegram.data.TelegramEarthquakeInfo;
import globalquake.ui.globalquake.feature.FeatureEarthquake;
import globalquake.utils.Scale;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;


public final class EventImageDrawer {
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static double scroll = 1.5;
    private static final Color oceanC = new Color(7, 37, 48);
    private static final Color landC = new Color(15, 47, 68);
    private static final Color borderC = new Color(153, 153, 153);

    public static InputStream drawEarthquakeImage(TelegramUser user, TelegramEarthquakeInfo info, Cluster cluster, Hypocenter hypocenter) throws IOException {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = img.createGraphics();
        drawCommonPart(g, info.getLat(), info.getLon());
        drawEventPoint(g, info.getLat(), info.getLon(), info.getLat(), info.getLon());
        drawDetails(g, info.getLat(), info.getLon(), Instant.ofEpochMilli(info.getOrigin()).atZone(ZoneId.systemDefault()).toLocalDateTime(), info.getLat(), info.getLon(), info.getDepth(), info.getMag());
        drawHome(g, user, info.getLat(), info.getLon());

        g.setStroke(new BasicStroke(1f));
        for (Event event : cluster.getAssignedEvents().values()) {
            double x = getX(event.report.lon(), info.getLon());
            double y = getY(event.report.lat(), info.getLat());
            double r = 12;
            g.setColor(Scale.getColorRatio(event.getMaxVelocity(hypocenter.magnitudeType)));
            Ellipse2D.Double ell1 = new Ellipse2D.Double(x - r / 2, y - r / 2, r, r);
            g.fill(ell1);
        }

        g.dispose();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", os);

        return new ByteArrayInputStream(os.toByteArray());
    }

    public static InputStream drawEventImage(TelegramUser user, double eventLat, double eventLon) throws IOException {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = img.createGraphics();
        drawCommonPart(g, eventLat, eventLon);
        drawEventPoint(g, eventLat, eventLon, eventLat, eventLon);
        drawHome(g, user, eventLat, eventLon);

        g.dispose();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", os);

        return new ByteArrayInputStream(os.toByteArray());
    }

    public static InputStream drawEventsImage(TelegramUser user, List<ArchivedEarthquake> archivedEarthquakeList) throws IOException {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = img.createGraphics();
        drawCommonPart(g, user.getHomeLat(), user.getHomeLon());
        for (ArchivedEarthquake archivedEarthquake : archivedEarthquakeList) {
            drawEventPoint(g, archivedEarthquake.getLatitude(), archivedEarthquake.getLongitude(), user.getHomeLat(), user.getHomeLon());
            LocalDateTime origin = archivedEarthquake.getOrigin();
            ZoneId oldZone = ZoneId.of("UTC");
            ZoneId newZone = ZoneId.of(Settings.timezoneStr);
            LocalDateTime newDateTime = origin.atZone(oldZone).withZoneSameInstant(newZone).toLocalDateTime();
            drawDetails(g, user.getHomeLat(), user.getHomeLon(), newDateTime, archivedEarthquake.getLatitude(), archivedEarthquake.getLongitude(), archivedEarthquake.getDepth(), archivedEarthquake.getMagnitude());
        }
        drawHome(g, user, user.getHomeLat(), user.getHomeLon());

        g.dispose();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", os);

        return new ByteArrayInputStream(os.toByteArray());
    }

    private static void drawCommonPart(Graphics2D g, double centerLat, double centerLon) {
        g.setColor(oceanC);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        for (GQPolygon polygon : Regions.raw_polygonsUHD) {
            java.awt.Polygon awt = new java.awt.Polygon();
            boolean add = false;
            for (int i = 0; i < polygon.getSize(); i++) {
                double lat = polygon.getLats()[i];
                double lon = polygon.getLons()[i];
                double x = getX(lon, centerLon);
                double y = getY(lat, centerLat);

                if (!add && isOnScreen(x, y)) {
                    add = true;
                }
                awt.addPoint((int) x, (int) y);
            }
            if (add) {
                g.setColor(landC);
                g.fill(awt);
                g.setColor(borderC);
                g.draw(awt);
            }
        }
    }

    private static void drawEventPoint(Graphics2D g, double eventLat, double eventLon, double centerLat, double centerLon) {
        double x = getX(eventLon, centerLon);
        double y = getY(eventLat, centerLat);
        double r = 12;
        Line2D.Double line1 = new Line2D.Double(x - r, y - r, x + r, y + r);
        Line2D.Double line2 = new Line2D.Double(x - r, y + r, x + r, y - r);
        g.setColor(Color.white);
        g.setStroke(new BasicStroke(8f));
        g.draw(line1);
        g.draw(line2);
        g.setColor(Color.orange);
        g.setStroke(new BasicStroke(6f));
        g.draw(line1);
        g.draw(line2);
    }

    private static void drawHome(Graphics2D g, TelegramUser user, double centerLat, double centerLon) {
        double x = getX(user.getHomeLon(), centerLon);
        double y = getY(user.getHomeLat(), centerLat);
        double r = 12;
        Line2D.Double line1 = new Line2D.Double(x - r, y, x + r, y);
        Line2D.Double line2 = new Line2D.Double(x, y + r, x, y - r);
        g.setColor(Color.magenta);
        g.setStroke(new BasicStroke(3f));
        g.draw(line1);
        g.draw(line2);

        g.setFont(new Font("Calibri", Font.PLAIN, 13));
        String str = "Home";
        g.drawString(str, (int) (x - g.getFontMetrics().stringWidth(str) * 0.5), (int) (y - 20));
    }

    private static void drawDetails(Graphics2D graphics, double centerLat, double centerLon, LocalDateTime origin, double eventLat, double eventLon, double depth, double magnitude) {
        graphics.setFont(new Font("Calibri", Font.PLAIN, 13));

        String str = "M%.1f  %s".formatted(magnitude, Settings.getSelectedDistanceUnit().format(depth, 1));
        double x = getX(eventLon, centerLon);
        double y = getY(eventLat, centerLat) - 40;
        graphics.setColor(FeatureEarthquake.getCrossColor(magnitude));
        graphics.drawString(str, (int) (x - graphics.getFontMetrics().stringWidth(str) * 0.5), (int) y);

        y+=15;

        graphics.setColor(Color.white);
        str = "%s".formatted(Settings.formatDateTime(origin));
        graphics.drawString(str, (int) (x - graphics.getFontMetrics().stringWidth(str) * 0.5), (int) y);
    }

    private static boolean isOnScreen(double x, double y) {
        return x >= 0 && y >= 0 && x < WIDTH && y < HEIGHT;
    }

    private static double getX(double lon, double centerLon) {
        return (lon - centerLon) / (scroll / 100.0) + (WIDTH * 0.5);
    }

    private static double getY(double lat, double centerLat) {
        return (centerLat - lat) / (scroll / (300 - 200 * Math.cos(0.5 * Math.toRadians(centerLat + lat))))
                + (HEIGHT * 0.5);
    }
}
