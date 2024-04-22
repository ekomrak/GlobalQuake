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
import java.util.UUID;

public class DatabaseService {
    private final Jdbi jdbi;
    private final Cache<Long, TelegramUser> usersCache;
    private final Cache<UsersCacheListType, List<TelegramUser>> usersListsCache;
    private final Cache<CountCacheListType, Integer> countCache;
    //private final Cache<EarthquakeCacheListType, List<ArchivedEarthquake>> earthquakeListCache;

    public DatabaseService() {
        usersCache = Caffeine.newBuilder().build();
        usersListsCache = Caffeine.newBuilder().build();
        countCache = Caffeine.newBuilder().build();
        //earthquakeListCache = Caffeine.newBuilder().build();
        HikariDataSourceProvider hikariDataSourceProvider = new HikariDataSourceProvider();
        jdbi = Jdbi.create(hikariDataSourceProvider.getHikariDataSource()).installPlugin(new SqlObjectPlugin()).installPlugin(new CaffeineCachePlugin());
    }

    public TelegramUser findUserById(Long userId) {
        return usersCache.get(userId, aLong -> jdbi.withExtension(UsersDao.class, extension -> extension.findUserById(userId)));
    }

    public List<TelegramUser> listActiveUsers() {
        return usersListsCache.get(UsersCacheListType.ALL_ACTIVE_USERS, usersCacheListType -> jdbi.withExtension(UsersDao.class, UsersDao::listActiveUsers));
    }

    public List<TelegramUser> listUsersWithEarthquakeAlert() {
        return usersListsCache.get(UsersCacheListType.USERS_WITH_EARTHQUAKE_ALERT, usersCacheListType -> jdbi.withExtension(UsersDao.class, UsersDao::listUsersWithEarthquakeAlert));
    }

    public List<TelegramUser> listUsersWithClusterAlert() {
        return usersListsCache.get(UsersCacheListType.USERS_WITH_CLUSTER_ALERT, usersCacheListType -> jdbi.withExtension(UsersDao.class, UsersDao::listUsersWithClusterAlert));
    }

    public List<TelegramUser> listUsersWithStationAlert() {
        return usersListsCache.get(UsersCacheListType.USERS_WITH_STATION_ALERT, usersCacheListType -> jdbi.withExtension(UsersDao.class, UsersDao::listUsersWithStationAlert));
    }

    public void updateTelegramUser(TelegramUser user) {
        jdbi.useExtension(UsersDao.class, extension -> extension.updateTelegramUser(user));
        invalidateUser(user.getId());
        invalidateAllUsersLists();
    }

    public void insertTelegramUser(TelegramUser user) {
        jdbi.useExtension(UsersDao.class, extension -> extension.insertTelegramUser(user));
        invalidateAllCountLists();
    }

    public int countAllUsers() {
        return countCache.get(CountCacheListType.ALL, cacheListType -> jdbi.withExtension(UsersDao.class, UsersDao::countAllUsers));
    }

    public int countActiveUsers() {
        return countCache.get(CountCacheListType.ACTIVE, cacheListType -> jdbi.withExtension(UsersDao.class, UsersDao::countActiveUsers));
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

    public ArchivedEarthquake findArchivedEarthquakeById(UUID id) {
        return jdbi.withExtension(EarthquakeDao.class, extension -> extension.findArchivedEarthquakeById(id));
    }

    public void invalidateUser(Long userId) {
        usersCache.invalidate(userId);
    }

    public void invalidateUsersListCache(UsersCacheListType usersCacheListType) {
        usersListsCache.invalidate(usersCacheListType);
    }

    public void invalidateCountCache(CountCacheListType cacheListType) {
        countCache.invalidate(cacheListType);
    }

    public void invalidateAllUsersLists() {
        usersListsCache.invalidateAll();
    }

    public void invalidateAllCountLists() {
        countCache.invalidateAll();
    }

    public void invalidateAllCaches() {
        usersCache.invalidateAll();
        invalidateAllUsersLists();
        invalidateAllCountLists();
    }
}
