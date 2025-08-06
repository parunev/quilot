package com.quilot.db;

import com.quilot.utils.Logger;
import lombok.NoArgsConstructor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

/**
 * Manages the connection to the MySQL database and handles initial schema setup.
 * This class now dynamically handles credentials provided by the user.
 */
@NoArgsConstructor
public class DatabaseManager {

    // DATABASE CONNECTION DETAILS
    private static final String DB_HOST = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "quilot_interviews";
    private static final String DB_URL = DB_HOST + DB_NAME;

    // PREFERENCES FOR SAVING CREDENTIALS
    private static final String PREF_NODE_NAME = "com/quilot/db";
    private static final String PREF_DB_USER = "dbUser";
    private static final String PREF_DB_PASSWORD = "dbPassword";
    private static final String PREF_DB_ENABLED = "dbEnabled";

    private static Connection connection;
    private static String currentUser;
    private static String currentPassword;


    /**
     * Gets the active database connection using stored or provided credentials.
     *
     * @return The active {@link Connection} object.
     * @throws SQLException if a database access error occurs or credentials are not set.
     */
    public static Connection getConnection() throws SQLException {
        if (!isDatabaseEnabled()) {
            throw new SQLException("Database feature is not enabled by the user.");
        }
        if (currentUser == null || currentPassword == null) {
            loadCredentials();
        }
        if (currentUser == null) {
            throw new SQLException("Database credentials are not set.");
        }

        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, currentUser, currentPassword);
                Logger.info("Successfully connected to the database.");
            } catch (ClassNotFoundException e) {
                Logger.error("MySQL JDBC Driver not found!", e);
                throw new SQLException("Database driver not found.", e);
            }
        }
        return connection;
    }

    /**
     * Closes the database connection if it is open.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    Logger.info("Database connection closed.");
                }
            } catch (SQLException e) {
                Logger.error("Failed to close the database connection.", e);
            }
        }
    }

    /**
     * Reads the schema.sql file and executes it to set up the database and tables.
     * This method uses the provided credentials to connect to the server.
     *
     * @param user The MySQL username.
     * @param password The MySQL password.
     * @throws SQLException if the schema setup fails.
     */
    public static void setupDatabaseSchema(String user, String password) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_HOST, user, password);
             Statement stmt = conn.createStatement()) {

            Logger.info("Reading database schema script...");
            InputStream is = DatabaseManager.class.getResourceAsStream("/schema.sql");
            if (is == null) {
                Logger.error("Could not find schema.sql in resources. Make sure it's in src/main/resources.");
                throw new SQLException("Database schema file not found.");
            }

            String sqlScript;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                sqlScript = reader.lines().collect(Collectors.joining("\n"));
            }

            String[] statements = sqlScript.split(";");

            Logger.info("Executing database schema setup...");
            for (String statement : statements) {
                if (statement.trim().isEmpty()) {
                    continue;
                }
                stmt.execute(statement);
            }
            Logger.info("Database schema created successfully.");
        } catch (Exception e) {
            Logger.error("Failed to execute database schema setup.", e);
            throw new SQLException("Could not set up database schema. Please check credentials and server status.", e);
        }
    }

    /**
     * Saves the user's database credentials and enabled status to preferences.
     *
     * @param user The username to save.
     * @param password The password to save.
     */
    public static void saveCredentials(String user, String password) {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE_NAME);
        prefs.put(PREF_DB_USER, user);
        prefs.put(PREF_DB_PASSWORD, password); // Note: Storing passwords in preferences is not secure for production apps.
        prefs.putBoolean(PREF_DB_ENABLED, true);
        currentUser = user;
        currentPassword = password;
        Logger.info("Database credentials and enabled status saved.");
    }

    /**
     * Loads database credentials from preferences into the static fields.
     */
    public static void loadCredentials() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE_NAME);
        currentUser = prefs.get(PREF_DB_USER, null);
        currentPassword = prefs.get(PREF_DB_PASSWORD, null);
    }

    /**
     * Checks if the user has previously enabled the database feature.
     * @return true if the database feature is enabled, false otherwise.
     */
    public static boolean isDatabaseEnabled() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE_NAME);
        return prefs.getBoolean(PREF_DB_ENABLED, false);
    }
}