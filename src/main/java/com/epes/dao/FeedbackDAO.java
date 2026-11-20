package com.epes.dao;

import com.epes.exception.DatabaseException;
import com.epes.exception.RecordNotFoundException;
import com.epes.model.Feedback;
import com.epes.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** All database work for the "feedback" table. */
public class FeedbackDAO implements GenericDAO<Feedback> {

    private static final String SELECT =
            "SELECT f.id, f.employee_id, e.name AS employee_name, f.manager_id, m.name AS manager_name, "
          + "f.comments, f.rating, f.created_at "
          + "FROM feedback f JOIN users e ON f.employee_id = e.id JOIN users m ON f.manager_id = m.id ";

    private Feedback mapRow(ResultSet rs) throws SQLException {
        return new Feedback(rs.getInt("id"), rs.getInt("employee_id"), rs.getString("employee_name"),
                rs.getInt("manager_id"), rs.getString("manager_name"), rs.getString("comments"),
                rs.getInt("rating"), rs.getTimestamp("created_at").toLocalDateTime());
    }

    /** Runs the shared SELECT with an optional "WHERE x = ?"; newest feedback comes first. */
    private List<Feedback> query(String whereClause, Integer param) throws DatabaseException {
        String sql = SELECT + whereClause + " ORDER BY f.created_at DESC, f.id DESC";
        List<Feedback> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (param != null) {
                ps.setInt(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load feedback: " + e.getMessage(), e);
        }
        return list;
    }

    /** All feedback received by one employee. */
    public List<Feedback> getByEmployee(int employeeId) throws DatabaseException {
        return query("WHERE f.employee_id = ?", employeeId);
    }

    @Override
    public void add(Feedback feedback) throws DatabaseException {
        String sql = "INSERT INTO feedback (employee_id, manager_id, comments, rating) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, feedback.getEmployeeId());
            ps.setInt(2, feedback.getManagerId());
            ps.setString(3, feedback.getComments());
            ps.setInt(4, feedback.getRating());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not save feedback: " + e.getMessage(), e);
        }
    }

    @Override
    public Feedback getById(int id) throws DatabaseException, RecordNotFoundException {
        List<Feedback> found = query("WHERE f.id = ?", id);
        if (found.isEmpty()) {
            throw new RecordNotFoundException("Feedback " + id + " not found");
        }
        return found.get(0);
    }

    @Override
    public List<Feedback> getAll() throws DatabaseException {
        return query("", null);
    }

    @Override
    public void update(Feedback feedback) throws DatabaseException {
        String sql = "UPDATE feedback SET comments = ?, rating = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, feedback.getComments());
            ps.setInt(2, feedback.getRating());
            ps.setInt(3, feedback.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not update feedback: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) throws DatabaseException {
        String sql = "DELETE FROM feedback WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not delete feedback: " + e.getMessage(), e);
        }
    }
}
