package com.epes.service;

import com.epes.dao.FeedbackDAO;
import com.epes.exception.DatabaseException;
import com.epes.exception.ValidationException;
import com.epes.model.Feedback;
import com.epes.model.User;

import java.util.List;

/** Business logic for feedback: checks the input, then asks FeedbackDAO to save or load. */
public class FeedbackService {

    private static final int MAX_COMMENTS = 500;
    private final FeedbackDAO feedbackDAO = new FeedbackDAO();

    /**
     * A manager gives feedback to an employee.
     *
     * @return the confirmation message to show on the screen
     */
    public String giveFeedback(User manager, User employee, String comments, int rating)
            throws ValidationException, DatabaseException {
        if (employee == null) {
            throw new ValidationException("Please select an employee.");
        }
        if (comments == null || comments.trim().isEmpty()) {
            throw new ValidationException("Please write your comments.");
        }
        if (comments.trim().length() > MAX_COMMENTS) {
            throw new ValidationException("Comments are too long (max " + MAX_COMMENTS + " letters).");
        }
        if (rating < 1 || rating > 5) {
            throw new ValidationException("Rating must be between 1 and 5.");
        }
        feedbackDAO.add(new Feedback(employee.getId(), manager.getId(), comments.trim(), rating));
        return "Feedback submitted successfully for " + employee.getName() + ".";
    }

    /** @return all feedback, newest first (for the manager) */
    public List<Feedback> getAllFeedback() throws DatabaseException {
        return feedbackDAO.getAll();
    }

    /** @return the feedback received by one employee, newest first */
    public List<Feedback> getFeedbackFor(int employeeId) throws DatabaseException {
        return feedbackDAO.getByEmployee(employeeId);
    }
}
