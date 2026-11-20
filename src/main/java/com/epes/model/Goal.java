package com.epes.model;

import java.time.LocalDate;

/** A goal for an employee, set either by a manager or by the employee. */
public class Goal {

    private int id;
    private int employeeId;
    private String employeeName;   // filled when read from the database (JOIN)
    private int setBy;
    private String setByName;      // filled when read from the database (JOIN)
    private String description;
    private LocalDate targetDate;
    private String status;         // PENDING, IN_PROGRESS or COMPLETED

    /** Full constructor, used when a goal is read from the database. */
    public Goal(int id, int employeeId, String employeeName, int setBy, String setByName,
                String description, LocalDate targetDate, String status) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.setBy = setBy;
        this.setByName = setByName;
        this.description = description;
        this.targetDate = targetDate;
        this.status = status;
    }

    /** Constructor for a brand-new goal (not saved yet, status starts as PENDING). */
    public Goal(int employeeId, int setBy, String description, LocalDate targetDate) {
        this(0, employeeId, null, setBy, null, description, targetDate, "PENDING");
    }

    public int getId() { return id; }
    public int getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public int getSetBy() { return setBy; }
    public String getSetByName() { return setByName; }
    public String getDescription() { return description; }
    public LocalDate getTargetDate() { return targetDate; }
    public String getStatus() { return status; }

    public void setDescription(String description) { this.description = description; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }
    public void setStatus(String status) { this.status = status; }
}
