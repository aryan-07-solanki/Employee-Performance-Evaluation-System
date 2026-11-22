package com.epes.service;

import com.epes.dao.EvaluationDAO;
import com.epes.exception.DatabaseException;
import com.epes.exception.ValidationException;
import com.epes.model.Criteria;
import com.epes.model.Evaluation;
import com.epes.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Business logic for evaluations: checks the scores, saves them and writes the report text. */
public class EvaluationService {

    private static final int MIN_SCORE = 1;
    private static final int MAX_SCORE = 10;
    private final EvaluationDAO evaluationDAO = new EvaluationDAO();

    /**
     * A manager evaluates an employee.
     *
     * @param scores one score (1 to 10) for each Criteria
     * @return the evaluation report to show on the screen
     */
    public String evaluate(User manager, User employee, Map<Criteria, Integer> scores)
            throws ValidationException, DatabaseException {
        if (employee == null) {
            throw new ValidationException("Please select an employee.");
        }
        for (Criteria c : Criteria.values()) {
            Integer value = scores.get(c);
            if (value == null || value < MIN_SCORE || value > MAX_SCORE) {
                throw new ValidationException(c.getLabel() + " must be a score from "
                        + MIN_SCORE + " to " + MAX_SCORE + ".");
            }
        }
        Evaluation evaluation = new Evaluation(employee.getId(), manager.getId(), scores);
        evaluationDAO.add(evaluation);
        return "Evaluation saved successfully.\n\n" + buildReport(employee.getName(), manager.getName(), evaluation);
    }

    /** Builds the text of the evaluation report (employee, scores, total and a rating word). */
    public String buildReport(String employeeName, String managerName, Evaluation ev) {
        StringBuilder sb = new StringBuilder();
        sb.append("EVALUATION REPORT\n");
        sb.append("Employee:      ").append(employeeName).append('\n');
        sb.append("Evaluated by:  ").append(managerName).append('\n');
        sb.append("Date:          ").append(ev.getEvalDate()).append("\n\n");
        for (Criteria c : Criteria.values()) {
            sb.append(String.format("%-14s %2d / 10%n", c.getLabel() + ":", ev.getScore(c)));
        }
        sb.append(String.format("%nTotal score: %.2f / 10  (%s)", ev.getTotalScore(), ratingWord(ev.getTotalScore())));
        return sb.toString();
    }

    /** Turns a number into a simple word. */
    public String ratingWord(double total) {
        if (total >= 9) {
            return "Outstanding";
        } else if (total >= 7) {
            return "Good";
        } else if (total >= 5) {
            return "Satisfactory";
        }
        return "Needs improvement";
    }

    /** @return all evaluations, newest first (for the manager's table) */
    public List<Evaluation> getAllEvaluations() throws DatabaseException {
        return evaluationDAO.getAll();
    }

    /** @return the evaluations of one employee, oldest first (the order a progress chart needs) */
    public List<Evaluation> getProgressOf(int employeeId) throws DatabaseException {
        List<Evaluation> list = new ArrayList<>(evaluationDAO.getByEmployee(employeeId));
        Collections.sort(list);   // uses Evaluation.compareTo (oldest first)
        return list;
    }
}
