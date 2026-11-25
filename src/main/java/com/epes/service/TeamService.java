package com.epes.service;

import com.epes.dao.UserDAO;
import com.epes.exception.DatabaseException;
import com.epes.exception.RecordNotFoundException;
import com.epes.exception.ValidationException;
import com.epes.model.Employee;
import com.epes.model.User;
import com.epes.util.PasswordUtil;

import java.util.List;

/** Business logic for team management: add, change the role of, and remove employees. */
public class TeamService {

    private static final String EMAIL_PATTERN = "^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$";
    private final UserDAO userDAO = new UserDAO();

    /** @return all employees (sorted by name) */
    public List<User> getMembers() throws DatabaseException {
        return userDAO.getByRole("EMPLOYEE");
    }

    /**
     * Adds a new employee to the manager's team.
     * synchronized, so two clicks at the same time cannot create the same email twice.
     */
    public synchronized String addEmployee(User manager, String name, String email, String password, String teamRole)
            throws ValidationException, DatabaseException {
        name = name == null ? "" : name.trim();
        email = email == null ? "" : email.trim().toLowerCase();
        teamRole = teamRole == null ? "" : teamRole.trim();
        if (name.isEmpty() || name.length() > 100) {
            throw new ValidationException("Please enter the employee name (maximum 100 characters).");
        }
        if (!email.matches(EMAIL_PATTERN)) {
            throw new ValidationException("Please enter a valid email, for example ravi@epes.com");
        }
        if (password == null || password.length() < 6) {
            throw new ValidationException("The password must have at least 6 characters.");
        }
        if (teamRole.isEmpty()) {
            throw new ValidationException("Please enter the team role, for example Developer.");
        }
        for (User existing : userDAO.getAll()) {
            if (existing.getEmail().equalsIgnoreCase(email)) {
                throw new ValidationException("A user with this email already exists.");
            }
        }
        Employee employee = new Employee(0, name, email, manager.getTeamId(), teamRole);
        employee.setPasswordHash(PasswordUtil.hash(password));
        userDAO.add(employee);
        return name + " was added to the team.";
    }

    /** Changes the team role (e.g. Developer -> Senior Developer) of an employee. */
    public String changeRole(User employee, String newRole) throws ValidationException, DatabaseException {
        if (employee == null) {
            throw new ValidationException("Please select an employee in the table.");
        }
        if (newRole == null || newRole.trim().isEmpty()) {
            throw new ValidationException("Please enter the new team role.");
        }
        employee.setTeamRole(newRole.trim());
        userDAO.update(employee);
        return "Role of " + employee.getName() + " is now " + newRole.trim() + ".";
    }

    /** Removes an employee (his evaluations, goals and feedback are deleted by the database too). */
    public String removeEmployee(User employee) throws ValidationException, DatabaseException {
        if (employee == null) {
            throw new ValidationException("Please select an employee in the table.");
        }
        try {
            userDAO.getById(employee.getId());   // make sure the employee still exists
        } catch (RecordNotFoundException e) {
            throw new ValidationException("This employee was already removed.");
        }
        userDAO.delete(employee.getId());
        return employee.getName() + " was removed from the team.";
    }
}
