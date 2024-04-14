package globalquake.telegram.data;

import globalquake.core.earthquake.data.Cluster;

public class TelegramClusterInfo extends TelegramAbstractInfo<Cluster> {
    private double level;
    private double lat;
    private double lon;

    @Override
    public void updateWith(Cluster cluster) {
        this.level = cluster.getLevel();
        this.lat = cluster.getRootLat();
        this.lon = cluster.getRootLon();
    }

    @Override
    public boolean equalsTo(Cluster cluster) {
        return (cluster.getLevel() == level) && (cluster.getRootLat() == lat) && (cluster.getRootLon() == lon);
    }
}
