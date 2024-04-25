package globalquake.telegram.util;

import globalquake.core.Settings;
import globalquake.core.analysis.Event;
import globalquake.core.earthquake.data.Cluster;
import globalquake.core.earthquake.data.Hypocenter;
import globalquake.core.regions.GQPolygon;
import globalquake.core.regions.Regions;
import globalquake.telegram.data.TelegramEarthquakeInfo;
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

public final class EventImageDrawer {
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static double scroll = 1.5;
    private static final Color oceanC = new Color(7, 37, 48);
    private static final Color landC = new Color(15, 47, 68);
    private static final Color borderC = new Color(153, 153, 153);

    public static InputStream drawEarthquakeImage(TelegramEarthquakeInfo info, Cluster cluster, Hypocenter hypocenter) throws IOException {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = img.createGraphics();
        drawCommonPart(g, info.getLat(), info.getLon());

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

    public static InputStream drawEventImage(double eventLat, double eventLon) throws IOException {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = img.createGraphics();
        drawCommonPart(g, eventLat, eventLon);

        g.dispose();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", os);

        return new ByteArrayInputStream(os.toByteArray());
    }

    private static void drawCommonPart(Graphics2D g, double eventLat, double eventLon) {
        g.setColor(oceanC);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        for (GQPolygon polygon : Regions.raw_polygonsUHD) {
            Polygon awt = new Polygon();
            boolean add = false;
            for (int i = 0; i < polygon.getSize(); i++) {
                double lat = polygon.getLats()[i];
                double lon = polygon.getLons()[i];
                double x = getX(lon, eventLon);
                double y = getY(lat, eventLat);

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

        {
            double x = getX(eventLon, eventLon);
            double y = getY(eventLat, eventLat);
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

        {
            double x = getX(Settings.homeLon, eventLon);
            double y = getY(Settings.homeLat, eventLat);
            double r = 12;
            Line2D.Double line1 = new Line2D.Double(x - r, y, x + r, y);
            Line2D.Double line2 = new Line2D.Double(x, y + r, x, y - r);
            g.setColor(Color.magenta);
            g.setStroke(new BasicStroke(3f));
            g.draw(line1);
            g.draw(line2);
        }
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
