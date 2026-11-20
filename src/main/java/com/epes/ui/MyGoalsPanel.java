package com.epes.ui;

import com.epes.model.Goal;
import com.epes.model.User;
import com.epes.service.GoalService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/** Employee "Personal Goals" tab: add own goals, see all goals, update their status. */
public class MyGoalsPanel extends JPanel {

    private final User employee;
    private final GoalService goalService = new GoalService();
    private List<Goal> currentGoals = new ArrayList<>();   // same order as the table rows

    private final JTextField descriptionField = new JTextField(26);
    private final JTextField dateField = new JTextField(LocalDate.now().plusDays(30).toString(), 9);
    private final JButton addButton = new JButton("Add Goal");
    private final JComboBox<String> statusBox =
            new JComboBox<>(new String[]{"PENDING", "IN_PROGRESS", "COMPLETED"});
    private final JButton statusButton = new JButton("Update Selected Goal's Status");
    private final DefaultTableModel model =
            UiHelper.readOnlyModel("Goal", "Target Date", "Status", "Set By");
    private final JTable table = new JTable(model);

    public MyGoalsPanel(User employee) {
        this.employee = employee;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        form.add(new JLabel("My goal:"));
        form.add(descriptionField);
        form.add(new JLabel("Target date (yyyy-mm-dd):"));
        form.add(dateField);
        form.add(addButton);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        bottom.add(new JLabel("Select a goal in the table, then choose status:"));
        bottom.add(statusBox);
        bottom.add(statusButton);

        add(form, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addGoal());
        statusButton.addActionListener(e -> updateStatus());
        loadGoals();
    }

    /** Loads this employee's goals in a background thread. */
    private void loadGoals() {
        new SwingWorker<List<Goal>, Void>() {
            @Override
            protected List<Goal> doInBackground() throws Exception {
                return goalService.getGoalsOf(employee.getId());
            }

            @Override
            protected void done() {
                try {
                    currentGoals = get();
                    model.setRowCount(0);
                    for (Goal g : currentGoals) {
                        model.addRow(new Object[]{g.getDescription(), g.getTargetDate(),
                                g.getStatus(), g.getSetByName()});
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    UiHelper.showError(MyGoalsPanel.this, ex.getCause());
                }
            }
        }.execute();
    }

    private void addGoal() {
        final String description = descriptionField.getText();
        final String date = dateField.getText();
        addButton.setEnabled(false);

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return goalService.addPersonalGoal(employee, description, date);
            }

            @Override
            protected void done() {
                try {
                    UiHelper.showInfo(MyGoalsPanel.this, get());
                    descriptionField.setText("");
                    loadGoals();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    UiHelper.showError(MyGoalsPanel.this, ex.getCause());
                }
                addButton.setEnabled(true);
            }
        }.execute();
    }

    private void updateStatus() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentGoals.size()) {
            UiHelper.showInfo(this, "Please click a goal in the table first.");
            return;
        }
        final int goalId = currentGoals.get(row).getId();
        final String status = (String) statusBox.getSelectedItem();

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return goalService.updateStatus(goalId, status);
            }

            @Override
            protected void done() {
                try {
                    UiHelper.showInfo(MyGoalsPanel.this, get());
                    loadGoals();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    UiHelper.showError(MyGoalsPanel.this, ex.getCause());
                }
            }
        }.execute();
    }
}
