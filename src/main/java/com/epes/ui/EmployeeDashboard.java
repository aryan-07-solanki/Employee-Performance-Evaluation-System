package com.epes.ui;

import com.epes.model.Employee;

/** Main screen of an employee: one tab for each employee feature. */
public class EmployeeDashboard extends BaseDashboard {

    public EmployeeDashboard(Employee employee) {
        super(employee, new String[]{
                "Performance Tracking", "Feedback History",
                "Personal Goals", "Performance Reports"});
        setTab(0, new PerformanceTrackingPanel(employee));  // Performance Tracking
        setTab(1, new FeedbackHistoryPanel(employee));      // Feedback History
        setTab(2, new MyGoalsPanel(employee));              // Personal Goals
    }
}
