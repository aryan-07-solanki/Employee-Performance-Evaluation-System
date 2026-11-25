package com.epes.service;

import com.epes.exception.DatabaseException;
import com.epes.model.Criteria;
import com.epes.model.Evaluation;
import com.epes.model.EmployeeSummary;
import com.epes.model.Feedback;
import com.epes.model.Goal;
import com.epes.model.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Builds the performance reports (per employee and for the whole team) and saves them to files. */
public class ReportService {

    private final EvaluationService evaluationService = new EvaluationService();
    private final GoalService goalService = new GoalService();
    private final FeedbackService feedbackService = new FeedbackService();
    private final UserService userService = new UserService();

    /** Collects evaluations, goals and feedback of one employee into one summary. */
    public EmployeeSummary summarize(User employee) throws DatabaseException {
        List<Evaluation> evaluations = evaluationService.getProgressOf(employee.getId()); // oldest first
        List<Goal> goals = goalService.getGoalsOf(employee.getId());
        List<Feedback> feedback = feedbackService.getFeedbackFor(employee.getId());

        Map<Criteria, Double> criteriaAverage = new EnumMap<>(Criteria.class);
        for (Criteria c : Criteria.values()) {
            double sum = 0;
            for (Evaluation ev : evaluations) {
                sum += ev.getScore(c);
            }
            criteriaAverage.put(c, evaluations.isEmpty() ? 0 : round(sum / evaluations.size()));
        }

        double totalSum = 0;
        for (Evaluation ev : evaluations) {
            totalSum += ev.getTotalScore();
        }
        double average = evaluations.isEmpty() ? 0 : round(totalSum / evaluations.size());
        double latest = evaluations.isEmpty() ? 0 : evaluations.get(evaluations.size() - 1).getTotalScore();

        int completed = 0;
        for (Goal g : goals) {
            if ("COMPLETED".equals(g.getStatus())) {
                completed++;
            }
        }

        double ratingSum = 0;
        for (Feedback f : feedback) {
            ratingSum += f.getRating();
        }
        double ratingAverage = feedback.isEmpty() ? 0 : round(ratingSum / feedback.size());

        return new EmployeeSummary(employee, evaluations.size(), average, latest, criteriaAverage,
                goals.size(), completed, feedback.size(), ratingAverage);
    }

    /**
     * Summaries of all employees, best first. Each employee is processed in its own thread
     * (a thread pool), so the team report loads faster than one-by-one.
     */
    public List<EmployeeSummary> summarizeTeam() throws DatabaseException {
        List<User> employees = userService.getEmployees();
        ExecutorService pool = Executors.newFixedThreadPool(Math.max(1, Math.min(4, employees.size())));
        List<Future<EmployeeSummary>> futures = new ArrayList<>();
        for (User employee : employees) {
            futures.add(pool.submit(() -> summarize(employee)));
        }
        List<EmployeeSummary> result = new ArrayList<>();
        try {
            for (Future<EmployeeSummary> future : futures) {
                result.add(future.get());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DatabaseException("Report loading was interrupted", e);
        } catch (ExecutionException e) {
            throw new DatabaseException("Could not build the report: " + e.getCause().getMessage(), e.getCause());
        } finally {
            pool.shutdown();
        }
        Collections.sort(result);   // uses EmployeeSummary.compareTo (best first)
        return result;
    }

    /** Text report of one employee. */
    public String employeeReportText(EmployeeSummary s) {
        StringBuilder sb = new StringBuilder();
        sb.append("PERFORMANCE REPORT\n");
        sb.append("Employee:   ").append(s.getEmployee().getName()).append('\n');
        sb.append("Role:       ").append(s.getEmployee().getTeamRole() == null ? "-" : s.getEmployee().getTeamRole()).append('\n');
        sb.append("Generated:  ").append(LocalDate.now()).append("\n\n");
        sb.append("Evaluations done: ").append(s.getEvaluationCount()).append('\n');
        if (s.getEvaluationCount() == 0) {
            sb.append("No evaluation yet, so there are no scores.\n");
        } else {
            sb.append(String.format("Average score:    %.2f / 10 (%s)%n", s.getAverageScore(),
                    evaluationService.ratingWord(s.getAverageScore())));
            sb.append(String.format("Latest score:     %.2f / 10%n%n", s.getLatestScore()));
            sb.append("Average per criteria:\n");
            for (Criteria c : Criteria.values()) {
                sb.append(String.format("  %-14s %.2f%n", c.getLabel() + ":", s.getCriteriaAverage(c)));
            }
        }
        sb.append(String.format("%nGoals completed: %d of %d%n", s.getGoalsCompleted(), s.getGoalsTotal()));
        if (s.getFeedbackCount() == 0) {
            sb.append("Feedback received: none yet\n");
        } else {
            sb.append(String.format("Feedback received: %d (average rating %.2f / 5)%n",
                    s.getFeedbackCount(), s.getAverageFeedbackRating()));
        }
        return sb.toString();
    }

    /** CSV text of the whole team report (can be opened in Excel). */
    public String teamReportCsv(List<EmployeeSummary> summaries) {
        StringBuilder sb = new StringBuilder();
        sb.append("Rank,Employee,Evaluations,Average Score,Latest Score,Goals Completed,Goals Total,Avg Feedback Rating\n");
        int rank = 1;
        for (EmployeeSummary s : summaries) {
            sb.append(rank++).append(',')
              .append('"').append(s.getEmployee().getName()).append('"').append(',')
              .append(s.getEvaluationCount()).append(',')
              .append(String.format("%.2f", s.getAverageScore())).append(',')
              .append(String.format("%.2f", s.getLatestScore())).append(',')
              .append(s.getGoalsCompleted()).append(',')
              .append(s.getGoalsTotal()).append(',')
              .append(String.format("%.2f", s.getAverageFeedbackRating())).append('\n');
        }
        return sb.toString();
    }

    /** Writes text to a file. synchronized: two threads must not write the same file at once. */
    public synchronized void saveToFile(Path path, String content) throws IOException {
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    }

    private static double round(double value) {
        return Math.round(value * 100) / 100.0;
    }
}
