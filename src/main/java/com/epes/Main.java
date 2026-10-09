package com.epes;

import com.epes.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Entry point of the application.
 * For now it only tests the database connection by listing all users.
 * Later this will open the Login screen.
 */
public class Main {

    public static void main(String[] args) {
        String sql = "SELECT id, name, email, role FROM users";

        // try-with-resources closes the connection automatically
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("Connected to database successfully!");
            while (rs.next()) {
                System.out.println(rs.getInt("id") + " | " + rs.getString("name")
                        + " | " + rs.getString("email") + " | " + rs.getString("role"));
            }

        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
    }
}
