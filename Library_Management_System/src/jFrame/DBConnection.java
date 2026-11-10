package jFrame;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Creates short-lived JDBC connections using runtime configuration.
 *
 * Configure DB_URL, DB_USER, and DB_PASSWORD in the process environment, or
 * use the matching db.url, db.user, and db.password JVM system properties.
 */
public final class DBConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/library_management_system";
    private static final String DEFAULT_USER = "library_app";

    private DBConnection() {
        // Utility class.
    }

    public static Connection getConnection() throws SQLException {
        String url = setting("DB_URL", "db.url", DEFAULT_URL);
        String user = setting("DB_USER", "db.user", DEFAULT_USER);
        String password = setting("DB_PASSWORD", "db.password", null);

        if (password == null || password.isBlank()) {
            throw new SQLException(
                    "Database password is not configured. Set DB_PASSWORD "
                    + "or the db.password JVM property.");
        }

        return DriverManager.getConnection(url, user, password);
    }

    private static String setting(String environmentName, String propertyName,
            String defaultValue) {
        String value = System.getenv(environmentName);
        if (value == null || value.isBlank()) {
            value = System.getProperty(propertyName);
        }
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }
}
