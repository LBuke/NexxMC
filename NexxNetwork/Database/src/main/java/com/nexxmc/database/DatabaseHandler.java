package com.nexxmc.database;

import com.google.common.base.Preconditions;
import com.zaxxer.hikari.HikariDataSource;

import javax.annotation.Nullable;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

/**
 * Created at 25/02/2019
 *
 * @author Luke Bingham
 */
public class DatabaseHandler implements Database {

    //The conf MySQL driver
    private static final String DEFAULT_MYSQL_DRIVER = "org.mariadb.jdbc.Driver";

    //The database credentials
    private DatabaseCredentials dbCreds;

    //Data source connection pool from HikariCP
    private final HikariDataSource hikariSource = new HikariDataSource();

    // NOTE: HikariCP performs best at fixed pool size, minIdle=maxConns
    // https://github.com/brettwooldridge/HikariCP

    //How many minimum idle connections should we always have (2)
    protected int minIdle = 30;

    //How many max connections should exist in pool (2)
    protected int maxPoolSize = 100;

    //How long, in millis, we stop waiting for new connection (15 secs)
    protected int connectionTimeoutMs = 15 * 1000;

    //How long, in millis, before connections timeout (45 secs)
    protected int idleTimeoutMs = 45 * 1000;

    //How long, in millis, this connection can be alive for (30 mins)
    protected int maxLifetimeMs = 30 * 60 * 1000;

    //How long, in millis, can a connection be gone from a pool (4 secs)
    protected int leakDetectionThresholdMs = 4 * 1000;

    //The ping alive query
    protected String connectionTestQuery = "SELECT 1";

    //Should the connection cache prepared statements
    protected boolean cachePreparedStatements = true;

    //Number of prepared statements to cache per connection
    protected int preparedStatementCache = 250;

    //Max number of prepared statements to have
    protected int maxPreparedStatementCache = 2048;

    /**
     * Initialize the handler with the specified database credentials.
     * <p>
     * Sets up the configuration for the connection pool and conf settings.
     * </p>
     *
     * @param dbCreds - the credentials for the database
     * @param driver  - the driver class
     */
    public void init(DatabaseCredentials dbCreds, String driver) {
        this.dbCreds = dbCreds;

        // set the driver name for the connection driver
        hikariSource.setDriverClassName(driver);

        // assume host/port combo together, or could just be without port
        String connURL = dbCreds.getHost();

        // if a port is defined
        if (dbCreds.getPort() > 0) {
            connURL = dbCreds.getHost() + ":" + dbCreds.getPort();
        }

        // set the jdbc url, note the character encoding
        // https://stackoverflow.com/questions/3040597/jdbc-character-encoding
        //autoReconnect=true&useSSL=false
        hikariSource.setJdbcUrl("jdbc:mariadb://" + connURL + "/" + dbCreds.getName() + "?characterEncoding=UTF-8&autoReconnect=true&useSSL=false&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&allowMultiQueries=true");
//        hikariSource.setJdbcUrl("jdbc:mariadb://" + connURL + "/" + dbCreds.getName() + "?characterEncoding=UTF-8&autoReconnect=true&useSSL=false");

        // set user/pass
        hikariSource.setUsername(dbCreds.getUser());
        hikariSource.setPassword(dbCreds.getPass());

        /** General conf settings for hikari */

        // works best when minIdle=maxPoolSize
        hikariSource.setMinimumIdle(minIdle);
        hikariSource.setMaximumPoolSize(maxPoolSize);

        // how long to wait, for a new connection
        hikariSource.setConnectionTimeout(connectionTimeoutMs);

        // how long before idle connection is destroyed
        hikariSource.setIdleTimeout(idleTimeoutMs);

        // how long can a connection exist
        hikariSource.setMaxLifetime(maxLifetimeMs);

        // how long connection is away from a pool before saying uh oh
        hikariSource.setLeakDetectionThreshold(leakDetectionThresholdMs);

        // test query to confirm alive
        hikariSource.setConnectionTestQuery(connectionTestQuery);

        // should we cache prepared statements
        hikariSource.addDataSourceProperty("cachePrepStmts", "" + cachePreparedStatements);

        // the size of the prepared statement cache
        hikariSource.addDataSourceProperty("prepStmtCacheSize", "" + preparedStatementCache);

        // the maximum cache limit
        hikariSource.addDataSourceProperty("prepStmtCacheSqlLimit", "" + maxPreparedStatementCache);

        // MUST set log writer
        try {
            hikariSource.setLogWriter(new PrintWriter(System.out));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize the database handler given the credentials.
     *
     * @param credentials - the login details to this database
     */
    protected void init(DatabaseCredentials credentials) {
        this.init(credentials, DEFAULT_MYSQL_DRIVER);
    }

    /**
     * Load the settings for HikariCP from the yaml config and stores them
     * locally in the object, then initializes the database handler.
     */
    public void init(@Nullable File file) {

        Properties properties = new Properties();
        if (file != null) {
            try (InputStream inputStream = new FileInputStream(Objects.requireNonNull(file))) {
                properties.load(inputStream);
            } catch (IOException e) {
//            e.printStackTrace(); TODO: Fix me
            }
        } else {
            System.out.println("[NexxDatabase] Credentials File not found, Using defaults.");
        }

        Preconditions.checkNotNull(properties);

        String host = properties.getProperty("host", "localhost");
        int port = Integer.parseInt(properties.getProperty("port", "3306"));
        String dbName = properties.getProperty("database", "nexxmc");
        String username = properties.getProperty("user", "root");
        String password = properties.getProperty("password", "nRsPh2wmPTv3WA7J8ML5KNR4");

        // connection stats
        int minIdle = Integer.parseInt(properties.getProperty("min-idle", "30"));
        int maxConns = Integer.parseInt(properties.getProperty("max-conn", "100"));

        // load local fields
        this.minIdle = minIdle < 0 ? 1 : minIdle;
        this.maxPoolSize = Math.max(maxConns, 1);

        // create database credentials
        DatabaseCredentials creds = new DatabaseCredentials(host, port, dbName, username, password);

        // initialize hikari cp
        init(creds);
    }

    /**
     * Close HikariCP connection pool, and all the connections.
     * <p>
     * Note: This should be called whenever the plugin turns off!
     * </p>
     */
    public void close() {
        if (!hikariSource.isClosed()) {
            hikariSource.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DatabaseCredentials getCredentials() {
        return dbCreds;
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public Connection getConnection() {
        try {
            return hikariSource.getConnection();
        } catch (SQLException e) {
            System.out.println("[DatabaseHandler] Unable to grab a connection from the connection pool!");
            e.printStackTrace();
        }

        return null;
    }
}
