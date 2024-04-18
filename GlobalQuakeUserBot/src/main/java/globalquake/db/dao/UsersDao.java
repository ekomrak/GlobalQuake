package globalquake.db.dao;

import globalquake.db.entities.TelegramUser;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface UsersDao {
    @SqlQuery("SELECT * FROM \"users\" WHERE enabled = true ORDER BY \"order_id\"")
    @RegisterBeanMapper(TelegramUser.class)
    List<TelegramUser> listActiveUsers();

    @SqlQuery("SELECT * FROM \"users\" WHERE enabled = true AND enable_telegram_earthquake_alert = true ORDER BY \"order_id\"")
    @RegisterBeanMapper(TelegramUser.class)
    List<TelegramUser> listUsersWithEarthquakeAlert();

    @SqlQuery("SELECT * FROM \"users\" WHERE enabled = true AND enable_telegram_possible_shaking_alert = true ORDER BY \"order_id\"")
    @RegisterBeanMapper(TelegramUser.class)
    List<TelegramUser> listUsersWithClusterAlert();

    @SqlQuery("SELECT * FROM \"users\" WHERE enabled = true AND enable_telegram_station_high_intensity_alert = true ORDER BY \"order_id\"")
    @RegisterBeanMapper(TelegramUser.class)
    List<TelegramUser> listUsersWithStationAlert();

    @SqlQuery("SELECT * FROM \"users\" WHERE id = ?")
    @RegisterBeanMapper(TelegramUser.class)
    TelegramUser findUserById(Long id);

    @SqlUpdate("INSERT INTO \"users\" (id, chat_id, first_name, last_name, user_name, language_code, premium, home_lat, home_lon, enable_telegram_earthquake_alert, ts_earthquake_min_magnitude_area1, ts_earthquake_max_dist_area1, ts_earthquake_min_magnitude_area2, ts_earthquake_max_dist_area2, ts_earthquake_min_intensity, enable_telegram_possible_shaking_alert, ts_possible_shaking_min_level, ts_possible_shaking_max_dist, enable_telegram_station_high_intensity_alert, ts_station_min_intensity_1, ts_station_max_dist_1, ts_station_min_intensity_2, ts_station_max_dist_2, enabled, subscription_date, enable_telegram_earthquake_location, enable_telegram_earthquake_image, enable_telegram_earthquake_map, enable_telegram_possible_shaking_location, enable_telegram_possible_shaking_image, enable_telegram_possible_shaking_map, enable_telegram_station_high_intensity_location, enable_telegram_station_high_intensity_image, enable_telegram_station_high_intensity_map, send_image_as_a_photo, send_map_as_a_photo) VALUES (:id, :chatId, :firstName, :lastName, :userName, :languageCode, :premium, :homeLat, :homeLon, :enableTelegramEarthquakeAlert, :tsEarthquakeMinMagnitudeArea1, :tsEarthquakeMaxDistArea1, :tsEarthquakeMinMagnitudeArea2, :tsEarthquakeMaxDistArea2, :tsEarthquakeMinIntensity, :enableTelegramPossibleShakingAlert, :tsPossibleShakingMinLevel, :tsPossibleShakingMaxDist, :enableTelegramStationHighIntensityAlert, :tsStationMinIntensity1, :tsStationMaxDist1, :tsStationMinIntensity2, :tsStationMaxDist2, :enabled, :subscriptionDate, :enableTelegramEarthquakeLocation, :enableTelegramEarthquakeImage, :enableTelegramEarthquakeMap, :enableTelegramPossibleShakingLocation, :enableTelegramPossibleShakingImage, :enableTelegramPossibleShakingMap, :enableTelegramStationHighIntensityLocation, :enableTelegramStationHighIntensityImage, :enableTelegramStationHighIntensityMap, :sendImageAsAPhoto, :sendMapAsAPhoto)")
    void insertTelegramUser(@BindBean TelegramUser user);

    @SqlUpdate("UPDATE \"users\" SET chat_id = :chatId, first_name = :firstName, last_name = :lastName, user_name = :userName, language_code = :languageCode, premium = :premium, home_lat = :homeLat, home_lon = :homeLon, enable_telegram_earthquake_alert = :enableTelegramEarthquakeAlert, ts_earthquake_min_magnitude_area1 = :tsEarthquakeMinMagnitudeArea1, ts_earthquake_max_dist_area1 = :tsEarthquakeMaxDistArea1, ts_earthquake_min_magnitude_area2 = :tsEarthquakeMinMagnitudeArea2, ts_earthquake_max_dist_area2 = :tsEarthquakeMaxDistArea2, ts_earthquake_min_intensity = :tsEarthquakeMinIntensity, enable_telegram_possible_shaking_alert = :enableTelegramPossibleShakingAlert, ts_possible_shaking_min_level = :tsPossibleShakingMinLevel, ts_possible_shaking_max_dist = :tsPossibleShakingMaxDist, enable_telegram_station_high_intensity_alert = :enableTelegramStationHighIntensityAlert, ts_station_min_intensity_1 = :tsStationMinIntensity1, ts_station_max_dist_1 = :tsStationMaxDist1, ts_station_min_intensity_2 = :tsStationMinIntensity2, ts_station_max_dist_2 = :tsStationMaxDist2, enabled = :enabled, updated_date = :updatedDate, enable_telegram_earthquake_location = :enableTelegramEarthquakeLocation, enable_telegram_earthquake_image = :enableTelegramEarthquakeImage, enable_telegram_earthquake_map = :enableTelegramEarthquakeMap, enable_telegram_possible_shaking_location = :enableTelegramPossibleShakingLocation, enable_telegram_possible_shaking_image = :enableTelegramPossibleShakingImage, enable_telegram_possible_shaking_map = :enableTelegramPossibleShakingMap, enable_telegram_station_high_intensity_location = :enableTelegramStationHighIntensityLocation, enable_telegram_station_high_intensity_image = :enableTelegramStationHighIntensityImage, enable_telegram_station_high_intensity_map = :enableTelegramStationHighIntensityMap, send_image_as_a_photo = :sendImageAsAPhoto, send_map_as_a_photo = :sendMapAsAPhoto WHERE id = :id")
    void updateTelegramUser(@BindBean TelegramUser user);

    @SqlQuery("SELECT count(1) FROM \"users\"")
    Integer countAllUsers();

    @SqlQuery("SELECT count(1) FROM \"users\" WHERE enabled = true")
    Integer countActiveUsers();

    @SqlQuery("SELECT count(1) from \"users\" where enabled = true and order_id <= (select order_id from \"users\" where id = ?)")
    Integer getUserOrder(Long id);
}
