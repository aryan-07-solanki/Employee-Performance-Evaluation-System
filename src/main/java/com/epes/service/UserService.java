package com.epes.service;

import com.epes.dao.UserDAO;
import com.epes.exception.DatabaseException;
import com.epes.model.User;

import java.util.List;

/** Business logic about users (used to fill the "choose employee" drop-down lists). */
public class UserService {

    private final UserDAO userDAO = new UserDAO();

    /** @return every employee, sorted by name */
    public List<User> getEmployees() throws DatabaseException {
        return userDAO.getByRole("EMPLOYEE");
    }
}
