package com.epes.ui;

import com.epes.model.Criteria;
import com.epes.model.EmployeeSummary;
import com.epes.model.User;
import com.epes.service.ReportService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/** Employee "Performance Reports" tab: own report text, criteria chart and a Save button. */
public class EmployeeReportsPanel extends JPanel {

    private final User employee;
    private final ReportService reportService = new ReportService();
    private final JTextArea reportArea = new JTextArea();
    private final BarChartPanel chart = new BarChartPanel("My average per criteria (out of 10)", 10);

    public EmployeeReportsPanel(User employee) {
        this.employee = employee;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        reportArea.setEditable(false);
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        reportArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JButton refresh = new JButton("Refresh");
        JButton save = new JButton("Save report as text file");
        refresh.addActionListener(e -> load());
        save.addActionListener(e -> saveReport());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        top.add(refresh);
        top.add(save);

        JPanel center = new JPanel(new GridLayout(1, 2, 10, 0));
        center.add(new JScrollPane(reportArea));
        center.add(chart);

        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        load();
    }

    private void load() {
        new SwingWorker<EmployeeSummary, Void>() {
            @Override
            protected EmployeeSummary doInBackground() throws Exception {
                return reportService.summarize(employee);
            }

            @Override
            protected void done() {
                try {
                    EmployeeSummary summary = get();
                    reportArea.setText(reportService.employeeReportText(summary));
                    reportArea.setCaretPosition(0);
                    List<String> labels = new ArrayList<>();
                    List<Double> values = new ArrayList<>();
                    for (Criteria c : Criteria.values()) {
                        labels.add(c.getLabel());
                        values.add(summary.getCriteriaAverage(c));
                    }
                    chart.setData(labels, values);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    UiHelper.showError(EmployeeReportsPanel.this, ex.getCause());
                }
            }
        }.execute();
    }

    private void saveReport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("my-performance-report.txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        try {
            reportService.saveToFile(file.toPath(), reportArea.getText());
            UiHelper.showInfo(this, "Report saved to:\n" + file.getAbsolutePath());
        } catch (IOException ex) {
            UiHelper.showError(this, new Exception("Could not save the file: " + ex.getMessage()));
        }
    }
}
