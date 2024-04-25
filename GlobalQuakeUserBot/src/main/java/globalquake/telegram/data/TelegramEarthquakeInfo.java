package globalquake.telegram.data;

import globalquake.core.earthquake.data.Earthquake;

public class TelegramEarthquakeInfo extends TelegramAbstractInfo<Earthquake> {
    private double mag;
    private double lat;
    private double lon;
    private double depth;
    private String region;
    private long origin;
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
        if (earthquake.getHypocenter() != null) {
            this.quality = earthquake.getHypocenter().quality.getSummary().toString();
        } else {
            this.quality = "";
        }
    }

    @Override
    public boolean equalsTo(Earthquake earthquake) {
        return (earthquake.getMag() == mag) && (earthquake.getLat() == lat) && (earthquake.getLon() == lon) && (earthquake.getDepth() == depth) && (earthquake.getRegion().equals(region)) && (earthquake.getOrigin() == origin) && ((earthquake.getHypocenter() == null && "".equals(quality)) || (earthquake.getHypocenter() != null && earthquake.getHypocenter().quality.getSummary().toString().equals(quality)));
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

    public String getQuality() {
        return quality;
    }
}
