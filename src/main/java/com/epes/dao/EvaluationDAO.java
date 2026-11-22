package com.epes.dao;

import com.epes.exception.DatabaseException;
import com.epes.exception.RecordNotFoundException;
import com.epes.model.Criteria;
import com.epes.model.Evaluation;
import com.epes.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** All database work for the "evaluations" table. */
public class EvaluationDAO implements GenericDAO<Evaluation> {

    private static final String SELECT =
            "SELECT e.id, e.employee_id, u.name AS employee_name, e.manager_id, m.name AS manager_name, "
          + "e.quality, e.teamwork, e.punctuality, e.productivity, e.communication, "
          + "e.total_score, e.eval_date "
          + "FROM evaluations e JOIN users u ON e.employee_id = u.id JOIN users m ON e.manager_id = m.id ";

    private Evaluation mapRow(ResultSet rs) throws SQLException {
        Map<Criteria, Integer> scores = new EnumMap<>(Criteria.class);
        for (Criteria c : Criteria.values()) {
            scores.put(c, rs.getInt(c.getColumn()));
        }
        return new Evaluation(rs.getInt("id"), rs.getInt("employee_id"), rs.getString("employee_name"),
                rs.getInt("manager_id"), rs.getString("manager_name"), scores,
                rs.getDouble("total_score"), rs.getDate("eval_date").toLocalDate());
    }

    /** Runs the shared SELECT with an optional "WHERE x = ?"; newest evaluation comes first. */
    private List<Evaluation> query(String whereClause, Integer param) throws DatabaseException {
        String sql = SELECT + whereClause + " ORDER BY e.eval_date DESC, e.id DESC";
        List<Evaluation> list = new ArrayList<>();
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
            throw new DatabaseException("Could not load evaluations: " + e.getMessage(), e);
        }
        return list;
    }

    /** All evaluations of one employee. */
    public List<Evaluation> getByEmployee(int employeeId) throws DatabaseException {
        return query("WHERE e.employee_id = ?", employeeId);
    }

    @Override
    public void add(Evaluation ev) throws DatabaseException {
        String sql = "INSERT INTO evaluations (employee_id, manager_id, quality, teamwork, punctuality, "
                + "productivity, communication, total_score, eval_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ev.getEmployeeId());
            ps.setInt(2, ev.getManagerId());
            int position = 3;
            for (Criteria c : Criteria.values()) {      // same order as the columns above
                ps.setInt(position++, ev.getScore(c));
            }
            ps.setDouble(8, ev.getTotalScore());
            ps.setDate(9, Date.valueOf(ev.getEvalDate()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not save evaluation: " + e.getMessage(), e);
        }
    }

    @Override
    public Evaluation getById(int id) throws DatabaseException, RecordNotFoundException {
        List<Evaluation> found = query("WHERE e.id = ?", id);
        if (found.isEmpty()) {
            throw new RecordNotFoundException("Evaluation " + id + " not found");
        }
        return found.get(0);
    }

    @Override
    public List<Evaluation> getAll() throws DatabaseException {
        return query("", null);
    }

    @Override
    public void update(Evaluation ev) throws DatabaseException {
        String sql = "UPDATE evaluations SET quality = ?, teamwork = ?, punctuality = ?, productivity = ?, "
                + "communication = ?, total_score = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            int position = 1;
            for (Criteria c : Criteria.values()) {
                ps.setInt(position++, ev.getScore(c));
            }
            ps.setDouble(6, ev.getTotalScore());
            ps.setInt(7, ev.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not update evaluation: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) throws DatabaseException {
        String sql = "DELETE FROM evaluations WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not delete evaluation: " + e.getMessage(), e);
        }
    }
}
