package com.epes.model;

import java.time.LocalDateTime;

/** Feedback (comments + rating from 1 to 5) given by a manager to an employee. */
public class Feedback {

    private int id;
    private int employeeId;
    private String employeeName;   // filled when read from the database (JOIN)
    private int managerId;
    private String managerName;    // filled when read from the database (JOIN)
    private String comments;
    private int rating;
    private LocalDateTime createdAt;

    /** Full constructor, used when feedback is read from the database. */
    public Feedback(int id, int employeeId, String employeeName, int managerId, String managerName,
                    String comments, int rating, LocalDateTime createdAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.managerId = managerId;
        this.managerName = managerName;
        this.comments = comments;
        this.rating = rating;
        this.createdAt = createdAt;
    }

    /** Constructor for brand-new feedback (not saved yet). */
    public Feedback(int employeeId, int managerId, String comments, int rating) {
        this(0, employeeId, null, managerId, null, comments, rating, null);
    }

    public int getId() { return id; }
    public int getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public int getManagerId() { return managerId; }
    public String getManagerName() { return managerName; }
    public String getComments() { return comments; }
    public int getRating() { return rating; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setComments(String comments) { this.comments = comments; }
    public void setRating(int rating) { this.rating = rating; }
}
