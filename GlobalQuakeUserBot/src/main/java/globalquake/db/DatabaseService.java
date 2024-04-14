package globalquake.db;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import globalquake.db.dao.EarthquakeDao;
import globalquake.db.dao.UsersDao;
import globalquake.db.entities.ArchivedEarthquake;
import globalquake.db.entities.TelegramUser;
import org.jdbi.v3.cache.caffeine.CaffeineCachePlugin;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.util.List;

public class DatabaseService {
    private final Jdbi jdbi;
    private final Cache<Long, TelegramUser> usersCache;
    private final Cache<CacheListType, List<TelegramUser>> usersListsCache;

    public DatabaseService() {
        usersCache = Caffeine.newBuilder().build();
        usersListsCache = Caffeine.newBuilder().build();
        HikariDataSourceProvider hikariDataSourceProvider = new HikariDataSourceProvider();
        jdbi = Jdbi.create(hikariDataSourceProvider.getHikariDataSource()).installPlugin(new SqlObjectPlugin()).installPlugin(new CaffeineCachePlugin());
    }

    public TelegramUser findUserById(Long userId) {
        return usersCache.get(userId, aLong -> jdbi.withExtension(UsersDao.class, extension -> extension.findUserById(userId)));
    }

    public List<TelegramUser> listActiveUsers() {
        return usersListsCache.get(CacheListType.ALL_ACTIVE_USERS, cacheListType -> jdbi.withExtension(UsersDao.class, UsersDao::listActiveUsers));
    }

    public List<TelegramUser> listUsersWithEarthquakeAlert() {
        return usersListsCache.get(CacheListType.USERS_WITH_EARTHQUAKE_ALERT, cacheListType -> jdbi.withExtension(UsersDao.class, UsersDao::listUsersWithEarthquakeAlert));
    }

    public List<TelegramUser> listUsersWithClusterAlert() {
        return usersListsCache.get(CacheListType.USERS_WITH_CLUSTER_ALERT, cacheListType -> jdbi.withExtension(UsersDao.class, UsersDao::listUsersWithClusterAlert));
    }

    public List<TelegramUser> listUsersWithStationAlert() {
        return usersListsCache.get(CacheListType.USERS_WITH_STATION_ALERT, cacheListType -> jdbi.withExtension(UsersDao.class, UsersDao::listUsersWithStationAlert));
    }

    public void updateTelegramUser(TelegramUser user) {
        jdbi.useExtension(UsersDao.class, extension -> extension.updateTelegramUser(user));
        invalidateUser(user.getId());
        invalidateAllLists();
    }

    public void insertTelegramUser(TelegramUser user) {
        jdbi.useExtension(UsersDao.class, extension -> extension.insertTelegramUser(user));
    }

    public int countAllUsers() {
        return jdbi.withExtension(UsersDao.class, UsersDao::countAllUsers);
    }

    public int countActiveUsers() {
        return jdbi.withExtension(UsersDao.class, UsersDao::countActiveUsers);
    }

    public int getUserOrder(Long id) {
        return jdbi.withExtension(UsersDao.class, extension -> extension.getUserOrder(id));
    }

    public void insertEarthquake(ArchivedEarthquake earthquake) {
        jdbi.useExtension(EarthquakeDao.class, extension -> extension.insertEarthquake(earthquake));
    }

    public List<ArchivedEarthquake> listLastEarthquakes(double latitude, double longitude, double radius, int limit) {
        return jdbi.withExtension(EarthquakeDao.class, extension -> extension.listLastEarthquakes(latitude, longitude, radius, limit));
    }

    public void invalidateUser(Long userId) {
        usersCache.invalidate(userId);
    }

    public void invalidateList(CacheListType cacheListType) {
        usersListsCache.invalidate(cacheListType);
    }

    public void invalidateAllLists() {
        usersListsCache.invalidateAll();
    }

    public void invalidateAllCaches() {
        usersCache.invalidateAll();
        invalidateAllLists();
    }
}
