package globalquake.core.faults;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.geojson.LineString;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Faults {
    public static final List<GQLine> raw_polygons = new ArrayList<>();

    public static void init() throws IOException {
        parseGeoJson("faults/faults.json", raw_polygons);
    }

    public static void parseGeoJson(String path, List<GQLine> raw) throws IOException {
        URL resource = ClassLoader.getSystemClassLoader().getResource(path);
        if (resource == null) {
            throw new IOException("Unable to load polygons: %s".formatted(path));
        }
        InputStream stream;
        FeatureCollection featureCollection = new ObjectMapper().readValue(stream = resource.openStream(),
                FeatureCollection.class);
        stream.close();

        for (Feature f : featureCollection.getFeatures()) {
            GeoJsonObject o = f.getGeometry();
            if (o instanceof LineString lineString && (raw != null)) {
                raw.add(new GQLine(lineString));
            }
        }
    }
}
