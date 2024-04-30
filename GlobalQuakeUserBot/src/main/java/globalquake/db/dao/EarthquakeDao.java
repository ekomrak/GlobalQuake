package globalquake.db.dao;

import globalquake.db.entities.ArchivedEarthquake;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.UUID;


public interface EarthquakeDao {
    @SqlUpdate("INSERT INTO \"earthquake\" (id, latitude, longitude, depth, origin, magnitude, quality_class, region, max_pga, max_mmi, local_pga, local_mmi) VALUES (:id, :latitude, :longitude, :depth, :origin, :magnitude, :qualityClass, :region, :maxPga, :maxMmi, :localPga, :localMmi)")
    void insertEarthquake(@BindBean ArchivedEarthquake earthquake);

    @SqlQuery("SELECT * FROM \"earthquake\" ORDER BY origin")
    @RegisterBeanMapper(ArchivedEarthquake.class)
    List<ArchivedEarthquake> listAllEarthquakes();

    @SqlQuery("SELECT * FROM \"earthquake\" WHERE greatcircledistance(latitude, longitude, ?, ?) <= ? order by origin desc limit ?")
    @RegisterBeanMapper(ArchivedEarthquake.class)
    List<ArchivedEarthquake> listLastEarthquakes(double latitude, double longitude, double radius, int limit);

    @SqlQuery("SELECT * FROM \"earthquake\" WHERE id = ?")
    @RegisterBeanMapper(ArchivedEarthquake.class)
    ArchivedEarthquake findArchivedEarthquakeById(UUID id);
}
