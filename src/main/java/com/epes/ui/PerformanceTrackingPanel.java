package com.epes.ui;

import com.epes.model.Criteria;
import com.epes.model.Evaluation;
import com.epes.model.User;
import com.epes.service.EvaluationService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import java.util.concurrent.ExecutionException;

/** Employee "Performance Tracking" tab: a progress chart and the list of own evaluations. */
public class PerformanceTrackingPanel extends JPanel {

    private final User employee;
    private final EvaluationService evaluationService = new EvaluationService();
    private final LineChartPanel chart = new LineChartPanel();
    private final JLabel summaryLabel = new JLabel(" ");
    private final DefaultTableModel model = UiHelper.readOnlyModel(
            "Date", "Quality", "Teamwork", "Punctuality", "Productivity", "Communication", "Total");

    public PerformanceTrackingPanel(User employee) {
        this.employee = employee;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> loadProgress());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        top.add(refresh);
        top.add(summaryLabel);

        JScrollPane tablePane = new JScrollPane(new JTable(model));
        tablePane.setPreferredSize(new Dimension(100, 150));

        add(top, BorderLayout.NORTH);
        add(chart, BorderLayout.CENTER);
        add(tablePane, BorderLayout.SOUTH);
        loadProgress();
    }

    /** Loads this employee's evaluations in a background thread and updates chart, table and summary. */
    private void loadProgress() {
        new SwingWorker<List<Evaluation>, Void>() {
            @Override
            protected List<Evaluation> doInBackground() throws Exception {
                return evaluationService.getProgressOf(employee.getId());   // oldest first
            }

            @Override
            protected void done() {
                try {
                    List<Evaluation> list = get();
                    chart.setData(list);
                    model.setRowCount(0);
                    for (int i = list.size() - 1; i >= 0; i--) {            // table: newest first
                        Evaluation ev = list.get(i);
                        model.addRow(new Object[]{ev.getEvalDate(),
                                ev.getScore(Criteria.QUALITY), ev.getScore(Criteria.TEAMWORK),
                                ev.getScore(Criteria.PUNCTUALITY), ev.getScore(Criteria.PRODUCTIVITY),
                                ev.getScore(Criteria.COMMUNICATION), String.format("%.2f", ev.getTotalScore())});
                    }
                    summaryLabel.setText(describe(list));
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    UiHelper.showError(PerformanceTrackingPanel.this, ex.getCause());
                }
            }
        }.execute();
    }

    /** One sentence: latest score and whether it went up or down since the previous evaluation. */
    private String describe(List<Evaluation> list) {
        if (list.isEmpty()) {
            return "No evaluations yet.";
        }
        Evaluation last = list.get(list.size() - 1);
        String text = String.format("Latest score: %.2f / 10 (%s)", last.getTotalScore(),
                evaluationService.ratingWord(last.getTotalScore()));
        if (list.size() > 1) {
            double change = last.getTotalScore() - list.get(list.size() - 2).getTotalScore();
            text += String.format("  |  %s %.2f since the previous evaluation",
                    change >= 0 ? "up" : "down", Math.abs(change));
        }
        return text;
    }
}
