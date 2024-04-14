package globalquake.ui.globalquake.feature;

class HomeLocationPlaceholder implements LocationPlaceholder {
    private final double homeLat;
    private final double homeLon;

    public HomeLocationPlaceholder(double homeLat, double homeLon) {
        this.homeLat = homeLat;
        this.homeLon = homeLon;
    }

    public double getLat() {
        return homeLat;
    }

    public double getLon() {
        return homeLon;
    }
}
