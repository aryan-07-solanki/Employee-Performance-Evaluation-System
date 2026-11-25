package com.epes.ui;

import com.epes.model.EmployeeSummary;
import com.epes.service.ReportService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/** Manager "Performance Reports" tab: team ranking table, bar chart and CSV export. */
public class ManagerReportsPanel extends JPanel {

    private final ReportService reportService = new ReportService();
    private final BarChartPanel chart = new BarChartPanel("Average score per employee (out of 10)", 10);
    private final DefaultTableModel model = UiHelper.readOnlyModel(
            "Rank", "Employee", "Evaluations", "Average", "Latest", "Goals Done", "Avg Feedback (of 5)");
    private final JButton refresh = new JButton("Refresh");
    private final JButton export = new JButton("Export CSV");
    private List<EmployeeSummary> current = new ArrayList<>();

    public ManagerReportsPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        top.add(refresh);
        top.add(export);
        refresh.addActionListener(e -> load());
        export.addActionListener(e -> exportCsv());

        JScrollPane tablePane = new JScrollPane(new JTable(model));
        tablePane.setPreferredSize(new Dimension(100, 150));

        add(top, BorderLayout.NORTH);
        add(chart, BorderLayout.CENTER);
        add(tablePane, BorderLayout.SOUTH);
        load();
    }

    /** Builds the team report in a background thread, then fills table and chart. */
    private void load() {
        refresh.setEnabled(false);
        new SwingWorker<List<EmployeeSummary>, Void>() {
            @Override
            protected List<EmployeeSummary> doInBackground() throws Exception {
                return reportService.summarizeTeam();
            }

            @Override
            protected void done() {
                try {
                    current = get();
                    model.setRowCount(0);
                    List<String> names = new ArrayList<>();
                    List<Double> averages = new ArrayList<>();
                    int rank = 1;
                    for (EmployeeSummary s : current) {
                        model.addRow(new Object[]{rank++, s.getEmployee().getName(), s.getEvaluationCount(),
                                String.format("%.2f", s.getAverageScore()), String.format("%.2f", s.getLatestScore()),
                                s.getGoalsCompleted() + " / " + s.getGoalsTotal(),
                                String.format("%.2f", s.getAverageFeedbackRating())});
                        names.add(s.getEmployee().getName());
                        averages.add(s.getAverageScore());
                    }
                    chart.setData(names, averages);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    UiHelper.showError(ManagerReportsPanel.this, ex.getCause());
                }
                refresh.setEnabled(true);
            }
        }.execute();
    }

    /** Asks where to save, then writes the CSV file. */
    private void exportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("team-performance-report.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        try {
            reportService.saveToFile(file.toPath(), reportService.teamReportCsv(current));
            UiHelper.showInfo(this, "Report saved to:\n" + file.getAbsolutePath());
        } catch (IOException ex) {
            UiHelper.showError(this, new Exception("Could not save the file: " + ex.getMessage()));
        }
    }
}
