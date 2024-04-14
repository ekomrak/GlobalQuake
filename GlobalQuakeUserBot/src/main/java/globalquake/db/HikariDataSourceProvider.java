package globalquake.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import globalquake.core.Settings;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class HikariDataSourceProvider {
    private final HikariDataSource hikariDataSource;

    public HikariDataSourceProvider() {
        String jdbcUrl = Settings.jdbcUrl;
        String username = Settings.pgUsername;
        String dbPassword = Settings.pgPassword;

        HikariConfig hikariConfig = new HikariConfig();

        Map<String, String> jdbcUrlParamsMap = null;
        if (jdbcUrl.contains("?")) {
            String jdbcUrlParams = jdbcUrl.split("\\?")[1];
            if (jdbcUrlParams != null && !jdbcUrlParams.isEmpty()) {
                jdbcUrlParamsMap = Arrays.stream(jdbcUrlParams.split("&"))
                        .map(kv -> kv.split("="))
                        .collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]));
            }
        }
        String schemaName = null;
        if (jdbcUrlParamsMap != null && jdbcUrlParamsMap.containsKey("currentSchema")) {
            schemaName = jdbcUrlParamsMap.get("currentSchema");
        }

        hikariConfig.setJdbcUrl(jdbcUrl);
        if (username != null) {
            hikariConfig.setUsername(username);
        }
        if (dbPassword != null) {
            hikariConfig.setPassword(dbPassword);
        }
        if (schemaName != null) {
            hikariConfig.setSchema(schemaName);
        }

        hikariConfig.addDataSourceProperty( "cachePrepStmts" , "true" );
        hikariConfig.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        hikariConfig.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        hikariDataSource = new HikariDataSource(hikariConfig);
    }

    public HikariDataSource getHikariDataSource() {
        return hikariDataSource;
    }
}
