package com.epes.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Creates connections to the MySQL database (JDBC).
 * The URL, user and password are read from the file "db.properties"
 * so that the real password is never written inside the source code.
 */
public class DBConnection {

    private static final String CONFIG_FILE = "db.properties";
    private static final Properties PROPS = loadProperties();

    // Private constructor: this class is only used through its static method
    private DBConnection() { }

    /** Reads db.properties from the project folder (with safe defaults). */
    private static Properties loadProperties() {
        Properties p = new Properties();
        p.setProperty("db.url", "jdbc:mysql://localhost:3306/epes_db");
        p.setProperty("db.user", "root");
        p.setProperty("db.password", "");
        try (InputStream in = new FileInputStream(CONFIG_FILE)) {
            p.load(in);
        } catch (IOException e) {
            System.out.println("Warning: could not read " + CONFIG_FILE
                    + " - create it by copying db.properties.example");
        }
        return p;
    }

    /**
     * Opens a new connection to the database.
     * Callers should use try-with-resources so the connection closes automatically.
     *
     * @return an open JDBC Connection
     * @throws SQLException if MySQL is off or the URL/user/password is wrong
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                PROPS.getProperty("db.url"),
                PROPS.getProperty("db.user"),
                PROPS.getProperty("db.password"));
    }
}
