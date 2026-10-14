package com.epes.model;

/**
 * Abstract parent of every person who can log in.
 * Manager and Employee extend this class (inheritance) and give their own
 * answers to the abstract methods (polymorphism).
 */
public abstract class User {

    private int id;
    private String name;
    private String email;
    private String passwordHash;   // only used while adding a new user
    private Integer teamId;        // null if the user is not in a team
    private String teamRole;       // e.g. Developer, Tester

    protected User(int id, String name, String email, Integer teamId, String teamRole) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.teamId = teamId;
        this.teamRole = teamRole;
    }

    /** @return "MANAGER" or "EMPLOYEE" (same text as the database role column) */
    public abstract String getRole();

    /** @return the title shown on top of this user's dashboard */
    public abstract String getDashboardTitle();

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Integer getTeamId() { return teamId; }
    public String getTeamRole() { return teamRole; }
    public String getPasswordHash() { return passwordHash; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setTeamId(Integer teamId) { this.teamId = teamId; }
    public void setTeamRole(String teamRole) { this.teamRole = teamRole; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    @Override
    public String toString() {
        return name + " (" + getRole() + ")";
    }
}
