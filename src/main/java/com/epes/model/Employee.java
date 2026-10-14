package com.epes.model;

/** An employee: tracks own performance, reads feedback and sets personal goals. */
public class Employee extends User {

    public Employee(int id, String name, String email, Integer teamId, String teamRole) {
        super(id, name, email, teamId, teamRole);
    }

    @Override
    public String getRole() {
        return "EMPLOYEE";
    }

    @Override
    public String getDashboardTitle() {
        return "Employee Dashboard";
    }
}
