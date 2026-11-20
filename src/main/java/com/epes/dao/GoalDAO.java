package com.epes.dao;

import com.epes.exception.DatabaseException;
import com.epes.exception.RecordNotFoundException;
import com.epes.model.Goal;
import com.epes.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** All database work for the "goals" table. */
public class GoalDAO implements GenericDAO<Goal> {

    // One SELECT used by every read method; JOIN gives us the names of the people
    private static final String SELECT =
            "SELECT g.id, g.employee_id, e.name AS employee_name, g.set_by, s.name AS set_by_name, "
          + "g.description, g.target_date, g.status "
          + "FROM goals g JOIN users e ON g.employee_id = e.id JOIN users s ON g.set_by = s.id ";

    private Goal mapRow(ResultSet rs) throws SQLException {
        return new Goal(rs.getInt("id"), rs.getInt("employee_id"), rs.getString("employee_name"),
                rs.getInt("set_by"), rs.getString("set_by_name"), rs.getString("description"),
                rs.getDate("target_date").toLocalDate(), rs.getString("status"));
    }

    /** Runs the shared SELECT with an optional "WHERE x = ?" and returns the goals found. */
    private List<Goal> query(String whereClause, Integer param) throws DatabaseException {
        String sql = SELECT + whereClause + " ORDER BY g.target_date, g.id";
        List<Goal> goals = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (param != null) {
                ps.setInt(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    goals.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load goals: " + e.getMessage(), e);
        }
        return goals;
    }

    /** All goals of one employee (used on the employee dashboard). */
    public List<Goal> getByEmployee(int employeeId) throws DatabaseException {
        return query("WHERE g.employee_id = ?", employeeId);
    }

    /** Changes only the status of a goal (PENDING, IN_PROGRESS, COMPLETED). */
    public void updateStatus(int goalId, String status) throws DatabaseException {
        String sql = "UPDATE goals SET status = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, goalId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not update goal status: " + e.getMessage(), e);
        }
    }

    @Override
    public void add(Goal goal) throws DatabaseException {
        String sql = "INSERT INTO goals (employee_id, set_by, description, target_date, status) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, goal.getEmployeeId());
            ps.setInt(2, goal.getSetBy());
            ps.setString(3, goal.getDescription());
            ps.setDate(4, Date.valueOf(goal.getTargetDate()));
            ps.setString(5, goal.getStatus());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not save goal: " + e.getMessage(), e);
        }
    }

    @Override
    public Goal getById(int id) throws DatabaseException, RecordNotFoundException {
        List<Goal> found = query("WHERE g.id = ?", id);
        if (found.isEmpty()) {
            throw new RecordNotFoundException("Goal " + id + " not found");
        }
        return found.get(0);
    }

    @Override
    public List<Goal> getAll() throws DatabaseException {
        return query("", null);
    }

    @Override
    public void update(Goal goal) throws DatabaseException {
        String sql = "UPDATE goals SET description = ?, target_date = ?, status = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, goal.getDescription());
            ps.setDate(2, Date.valueOf(goal.getTargetDate()));
            ps.setString(3, goal.getStatus());
            ps.setInt(4, goal.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not update goal: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) throws DatabaseException {
        String sql = "DELETE FROM goals WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not delete goal: " + e.getMessage(), e);
        }
    }
}
