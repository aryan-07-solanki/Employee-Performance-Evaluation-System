package com.epes.model;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

/**
 * One performance evaluation of an employee: a score for every Criteria,
 * the average (total score) and the date. Evaluations can be sorted by date
 * because the class implements Comparable.
 */
public class Evaluation implements Comparable<Evaluation> {

    private final int id;
    private final int employeeId;
    private final String employeeName;   // filled when read from the database (JOIN)
    private final int managerId;
    private final String managerName;    // filled when read from the database (JOIN)
    private final Map<Criteria, Integer> scores = new EnumMap<>(Criteria.class);
    private final double totalScore;
    private final LocalDate evalDate;

    /** Full constructor, used when an evaluation is read from the database. */
    public Evaluation(int id, int employeeId, String employeeName, int managerId, String managerName,
                      Map<Criteria, Integer> scores, double totalScore, LocalDate evalDate) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.managerId = managerId;
        this.managerName = managerName;
        this.scores.putAll(scores);
        this.totalScore = totalScore;
        this.evalDate = evalDate;
    }

    /** Constructor for a brand-new evaluation: the average is calculated and the date is today. */
    public Evaluation(int employeeId, int managerId, Map<Criteria, Integer> scores) {
        this(0, employeeId, null, managerId, null, scores, average(scores), LocalDate.now());
    }

    /** @return the average of all scores, rounded to 2 decimals (0 if there are no scores) */
    public static double average(Map<Criteria, Integer> scores) {
        if (scores.isEmpty()) {
            return 0;
        }
        int sum = 0;
        for (int value : scores.values()) {
            sum += value;
        }
        return Math.round((double) sum / scores.size() * 100) / 100.0;
    }

    public int getId() { return id; }
    public int getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public int getManagerId() { return managerId; }
    public String getManagerName() { return managerName; }
    public double getTotalScore() { return totalScore; }
    public LocalDate getEvalDate() { return evalDate; }

    /** @return the score given for one criteria (0 if missing) */
    public int getScore(Criteria criteria) {
        return scores.getOrDefault(criteria, 0);
    }

    /** Oldest evaluation first (ties broken by id). */
    @Override
    public int compareTo(Evaluation other) {
        int byDate = evalDate.compareTo(other.evalDate);
        return byDate != 0 ? byDate : Integer.compare(id, other.id);
    }
}
