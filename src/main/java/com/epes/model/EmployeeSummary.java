package com.epes.model;

import java.util.EnumMap;
import java.util.Map;

/**
 * A ready-made summary of one employee for the reports: evaluation averages,
 * goal progress and feedback rating. Summaries can be sorted best-first (Comparable).
 */
public class EmployeeSummary implements Comparable<EmployeeSummary> {

    private final User employee;
    private final int evaluationCount;
    private final double averageScore;
    private final double latestScore;
    private final Map<Criteria, Double> criteriaAverage = new EnumMap<>(Criteria.class);
    private final int goalsTotal;
    private final int goalsCompleted;
    private final int feedbackCount;
    private final double averageFeedbackRating;

    public EmployeeSummary(User employee, int evaluationCount, double averageScore, double latestScore,
                           Map<Criteria, Double> criteriaAverage, int goalsTotal, int goalsCompleted,
                           int feedbackCount, double averageFeedbackRating) {
        this.employee = employee;
        this.evaluationCount = evaluationCount;
        this.averageScore = averageScore;
        this.latestScore = latestScore;
        this.criteriaAverage.putAll(criteriaAverage);
        this.goalsTotal = goalsTotal;
        this.goalsCompleted = goalsCompleted;
        this.feedbackCount = feedbackCount;
        this.averageFeedbackRating = averageFeedbackRating;
    }

    public User getEmployee() { return employee; }
    public int getEvaluationCount() { return evaluationCount; }
    public double getAverageScore() { return averageScore; }
    public double getLatestScore() { return latestScore; }
    public int getGoalsTotal() { return goalsTotal; }
    public int getGoalsCompleted() { return goalsCompleted; }
    public int getFeedbackCount() { return feedbackCount; }
    public double getAverageFeedbackRating() { return averageFeedbackRating; }

    public double getCriteriaAverage(Criteria criteria) {
        return criteriaAverage.getOrDefault(criteria, 0.0);
    }

    /** Best average score first; same score -> sorted by name. */
    @Override
    public int compareTo(EmployeeSummary other) {
        int byScore = Double.compare(other.averageScore, averageScore);
        return byScore != 0 ? byScore : employee.getName().compareTo(other.employee.getName());
    }
}
