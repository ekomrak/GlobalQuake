package globalquake.telegram.data;

import globalquake.core.Settings;
import globalquake.core.earthquake.data.Earthquake;

import java.time.Instant;

public class TelegramEarthquakeInfo extends TelegramAbstractInfo<Earthquake> {
    private double mag;
    private double lat;
    private double lon;
    private double depth;
    private String region;
    private long origin;
    private String originDate;
    private String quality;

    public TelegramEarthquakeInfo(Earthquake earthquake) {
        super(earthquake);
    }

    @Override
    public void updateWith(Earthquake earthquake) {
        this.mag = earthquake.getMag();
        this.lat = earthquake.getLat();
        this.lon = earthquake.getLon();
        this.depth = earthquake.getDepth();
        this.region = earthquake.getRegion();
        this.origin = earthquake.getOrigin();
        this.originDate = Settings.formatDateTime(Instant.ofEpochMilli(origin));
        if (earthquake.getHypocenter() != null) {
            this.quality = earthquake.getHypocenter().quality.getSummary().toString();
        } else {
            this.quality = "";
        }
    }

    @Override
    public boolean equalsTo(Earthquake earthquake) {
        String newQuality = "";
        if (earthquake.getHypocenter() != null) {
            newQuality = earthquake.getHypocenter().quality.getSummary().toString();
        }
        return (earthquake.getMag() == mag) && (earthquake.getLat() == lat) && (earthquake.getLon() == lon) && (earthquake.getDepth() == depth) && (earthquake.getRegion().equals(region)) && (earthquake.getOrigin() == origin) && (newQuality.equals(quality));
    }

    public double getMag() {
        return mag;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public double getDepth() {
        return depth;
    }

    public String getRegion() {
        return region;
    }

    public long getOrigin() {
        return origin;
    }

    public String getOriginDate() {
        return originDate;
    }

    public String getQuality() {
        return quality;
    }
}
