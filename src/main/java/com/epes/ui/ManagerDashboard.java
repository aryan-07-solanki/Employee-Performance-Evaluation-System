package com.epes.ui;

import com.epes.model.Manager;

/** Main screen of a manager: one tab for each manager feature. */
public class ManagerDashboard extends BaseDashboard {

    public ManagerDashboard(Manager manager) {
        super(manager, new String[]{
                "Performance Evaluation", "Goal Setting", "Feedback",
                "Performance Reports", "Team Management"});
    }
}
