package com.epes.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Creates connections to the MySQL database (JDBC).
 * Every DAO class calls DBConnection.getConnection() whenever it needs the database.
 */
public class DBConnection {

    // ====== CHANGE THESE 3 LINES TO MATCH YOUR MYSQL ======
    private static final String URL = "jdbc:mysql://localhost:3306/epes_db";
    private static final String USER = "root";
    private static final String PASSWORD = "you";
    // =======================================================

    // Private constructor: this class is only used through its static method
    private DBConnection() { }

    /**
     * Opens a new connection to the database.
     * Callers should use try-with-resources so the connection closes automatically.
     *
     * @return an open JDBC Connection
     * @throws SQLException if MySQL is off or the URL/user/password is wrong
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
