package com.epes.model;

/** A manager: evaluates employees, sets goals, gives feedback and manages teams. */
public class Manager extends User {

    public Manager(int id, String name, String email, Integer teamId, String teamRole) {
        super(id, name, email, teamId, teamRole);
    }

    @Override
    public String getRole() {
        return "MANAGER";
    }

    @Override
    public String getDashboardTitle() {
        return "Manager Dashboard";
    }
}
