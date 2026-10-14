package com.epes.dao;

import com.epes.exception.DatabaseException;
import com.epes.exception.RecordNotFoundException;
import com.epes.model.Employee;
import com.epes.model.Manager;
import com.epes.model.User;
import com.epes.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** All database work for the "users" table (JDBC with PreparedStatement). */
public class UserDAO implements GenericDAO<User> {

    private static final String COLUMNS = "id, name, email, role, team_id, team_role";

    /** Converts one database row into a Manager or an Employee object. */
    private User mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        int team = rs.getInt("team_id");
        Integer teamId = rs.wasNull() ? null : team;
        String teamRole = rs.getString("team_role");

        if ("MANAGER".equals(rs.getString("role"))) {
            return new Manager(id, name, email, teamId, teamRole);
        }
        return new Employee(id, name, email, teamId, teamRole);
    }

    /**
     * Finds the user whose email and password hash match (used for login).
     *
     * @throws RecordNotFoundException if no user matches
     */
    public User findByEmailAndPasswordHash(String email, String passwordHash)
            throws DatabaseException, RecordNotFoundException {
        String sql = "SELECT " + COLUMNS + " FROM users WHERE email = ? AND password = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, passwordHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not check login: " + e.getMessage(), e);
        }
        throw new RecordNotFoundException("No user with this email and password");
    }

    /** Returns all users having the given role ("MANAGER" or "EMPLOYEE"). */
    public List<User> getByRole(String role) throws DatabaseException {
        String sql = "SELECT " + COLUMNS + " FROM users WHERE role = ? ORDER BY name";
        List<User> users = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load users: " + e.getMessage(), e);
        }
        return users;
    }

    @Override
    public void add(User user) throws DatabaseException {
        String sql = "INSERT INTO users (name, email, password, role, team_id, team_role) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getRole());
            if (user.getTeamId() == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, user.getTeamId());
            }
            ps.setString(6, user.getTeamRole());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not add user: " + e.getMessage(), e);
        }
    }

    @Override
    public User getById(int id) throws DatabaseException, RecordNotFoundException {
        String sql = "SELECT " + COLUMNS + " FROM users WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load user: " + e.getMessage(), e);
        }
        throw new RecordNotFoundException("User " + id + " not found");
    }

    @Override
    public List<User> getAll() throws DatabaseException {
        String sql = "SELECT " + COLUMNS + " FROM users ORDER BY id";
        List<User> users = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load users: " + e.getMessage(), e);
        }
        return users;
    }

    @Override
    public void update(User user) throws DatabaseException {
        String sql = "UPDATE users SET name = ?, email = ?, team_id = ?, team_role = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            if (user.getTeamId() == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, user.getTeamId());
            }
            ps.setString(4, user.getTeamRole());
            ps.setInt(5, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not update user: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) throws DatabaseException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not delete user: " + e.getMessage(), e);
        }
    }
}
