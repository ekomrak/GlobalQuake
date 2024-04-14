package globalquake.telegram;

import globalquake.core.Settings;
import io.micrometer.influx.InfluxConfig;

public class GQInfluxConfig implements InfluxConfig {
    @Override
    public String org() {
        return Settings.influxOrg;
    }

    @Override
    public String bucket() {
        return Settings.influxBucket;
    }

    @Override
    public String token() {
        return Settings.influxToken;
    }

    @Override
    public String get(String key) {
        return null;
    }
}
