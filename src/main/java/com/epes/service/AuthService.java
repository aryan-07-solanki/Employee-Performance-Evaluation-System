package com.epes.service;

import com.epes.dao.UserDAO;
import com.epes.exception.DatabaseException;
import com.epes.exception.InvalidCredentialsException;
import com.epes.exception.RecordNotFoundException;
import com.epes.exception.ValidationException;
import com.epes.model.User;
import com.epes.util.PasswordUtil;

/** Business logic for logging in: validates input, hashes the password, asks the DAO. */
public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    /**
     * Logs a user in.
     *
     * @return the logged-in Manager or Employee
     * @throws ValidationException         if email/password is empty or the email looks wrong
     * @throws InvalidCredentialsException if no user matches
     * @throws DatabaseException           if the database cannot be reached
     */
    public User login(String email, String password)
            throws ValidationException, InvalidCredentialsException, DatabaseException {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Please enter your email.");
        }
        if (!email.contains("@")) {
            throw new ValidationException("Email must contain '@'.");
        }
        if (password == null || password.isEmpty()) {
            throw new ValidationException("Please enter your password.");
        }
        try {
            return userDAO.findByEmailAndPasswordHash(email.trim(), PasswordUtil.hash(password));
        } catch (RecordNotFoundException e) {
            throw new InvalidCredentialsException("Wrong email or password.");
        }
    }
}
