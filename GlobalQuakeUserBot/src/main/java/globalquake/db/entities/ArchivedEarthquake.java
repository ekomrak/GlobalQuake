package globalquake.db.entities;

import globalquake.core.Settings;
import globalquake.core.archive.ArchivedQuake;
import globalquake.core.intensity.IntensityScales;
import globalquake.telegram.util.TelegramUtils;
import globalquake.utils.GeoUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

public class ArchivedEarthquake {
    private UUID id;
    private double latitude;
    private double longitude;
    private double depth;
    private LocalDateTime origin;
    private double magnitude;
    private String qualityClass;
    private String region;
    private double maxPga;
    private String maxMmi;
    private double localPga;
    private String localMmi;

    public ArchivedEarthquake() {

    }

    public ArchivedEarthquake(ArchivedQuake archivedQuake) {
        double localDist = GeoUtils.geologicalDistance(archivedQuake.getLat(), archivedQuake.getLon(), -archivedQuake.getDepth(), Settings.homeLat, Settings.homeLon, 0);
        double localPga = GeoUtils.pgaFunction(archivedQuake.getMag(), localDist, archivedQuake.getDepth());

        this.id = archivedQuake.getUuid();
        this.latitude = archivedQuake.getLat();
        this.longitude = archivedQuake.getLon();
        this.depth = archivedQuake.getDepth();
        this.origin = LocalDateTime.ofInstant(Instant.ofEpochMilli(archivedQuake.getOrigin()), ZoneId.of("UTC"));
        this.magnitude = archivedQuake.getMag();
        this.qualityClass = archivedQuake.getQualityClass().toString();
        this.region = archivedQuake.getRegion();
        this.maxPga = archivedQuake.getMaxPGA();
        this.maxMmi = TelegramUtils.formatLevel(IntensityScales.MMI.getLevel(archivedQuake.getMaxPGA()));
        this.localPga = localPga;
        this.localMmi = TelegramUtils.formatLevel(IntensityScales.MMI.getLevel(localPga));
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getDepth() {
        return depth;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }

    public LocalDateTime getOrigin() {
        return origin;
    }

    public void setOrigin(LocalDateTime origin) {
        this.origin = origin;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(double magnitude) {
        this.magnitude = magnitude;
    }

    public String getQualityClass() {
        return qualityClass;
    }

    public void setQualityClass(String qualityClass) {
        this.qualityClass = qualityClass;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public double getMaxPga() {
        return maxPga;
    }

    public void setMaxPga(double maxPga) {
        this.maxPga = maxPga;
    }

    public String getMaxMmi() {
        return maxMmi;
    }

    public void setMaxMmi(String maxMmi) {
        this.maxMmi = maxMmi;
    }

    public double getLocalPga() {
        return localPga;
    }

    public void setLocalPga(double localPga) {
        this.localPga = localPga;
    }

    public String getLocalMmi() {
        return localMmi;
    }

    public void setLocalMmi(String localMmi) {
        this.localMmi = localMmi;
    }
}
