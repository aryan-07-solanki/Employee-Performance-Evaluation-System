package com.epes.service;

import com.epes.dao.GoalDAO;
import com.epes.exception.DatabaseException;
import com.epes.exception.ValidationException;
import com.epes.model.Goal;
import com.epes.model.User;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Business logic for goals: checks the input, then asks GoalDAO to save or load. */
public class GoalService {

    private static final int MAX_DESCRIPTION = 255;
    private final GoalDAO goalDAO = new GoalDAO();

    /** Checks description and date text; returns the parsed date. */
    private LocalDate validate(String description, String dateText) throws ValidationException {
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Please enter the goal description.");
        }
        if (description.trim().length() > MAX_DESCRIPTION) {
            throw new ValidationException("Description is too long (max " + MAX_DESCRIPTION + " letters).");
        }
        LocalDate date;
        try {
            date = LocalDate.parse(dateText == null ? "" : dateText.trim());
        } catch (DateTimeParseException e) {
            throw new ValidationException("Target date must look like 2026-12-31 (year-month-day).");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new ValidationException("Target date cannot be in the past.");
        }
        return date;
    }

    /**
     * A manager sets a goal for an employee.
     *
     * @return the confirmation message to show on the screen
     */
    public String setGoalForEmployee(User manager, User employee, String description, String dateText)
            throws ValidationException, DatabaseException {
        if (employee == null) {
            throw new ValidationException("Please select an employee.");
        }
        LocalDate date = validate(description, dateText);
        goalDAO.add(new Goal(employee.getId(), manager.getId(), description.trim(), date));
        return "Goal set successfully for " + employee.getName() + ".";
    }

    /**
     * An employee adds a personal goal for themselves.
     *
     * @return the confirmation message to show on the screen
     */
    public String addPersonalGoal(User employee, String description, String dateText)
            throws ValidationException, DatabaseException {
        LocalDate date = validate(description, dateText);
        goalDAO.add(new Goal(employee.getId(), employee.getId(), description.trim(), date));
        return "Personal goal added successfully.";
    }

    /** @return all goals, earliest target date first (Comparator) */
    public List<Goal> getAllGoals() throws DatabaseException {
        List<Goal> goals = new ArrayList<>(goalDAO.getAll());
        goals.sort(Comparator.comparing(Goal::getTargetDate).thenComparing(Goal::getId));
        return goals;
    }

    /** @return the goals of one employee, earliest target date first */
    public List<Goal> getGoalsOf(int employeeId) throws DatabaseException {
        List<Goal> goals = new ArrayList<>(goalDAO.getByEmployee(employeeId));
        goals.sort(Comparator.comparing(Goal::getTargetDate).thenComparing(Goal::getId));
        return goals;
    }

    /** Changes the status of a goal. */
    public String updateStatus(int goalId, String status) throws DatabaseException {
        goalDAO.updateStatus(goalId, status);
        return "Goal status updated to " + status + ".";
    }
}
