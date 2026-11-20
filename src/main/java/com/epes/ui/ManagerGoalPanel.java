package com.epes.ui;

import com.epes.model.Goal;
import com.epes.model.User;
import com.epes.service.GoalService;
import com.epes.service.UserService;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;

/** Manager "Goal Setting" tab: a form to set a goal and a table of all goals. */
public class ManagerGoalPanel extends JPanel {

    private final User manager;
    private final GoalService goalService = new GoalService();
    private final UserService userService = new UserService();

    private final JComboBox<User> employeeBox = new JComboBox<>();
    private final JTextField descriptionField = new JTextField(26);
    private final JTextField dateField = new JTextField(LocalDate.now().plusDays(30).toString(), 9);
    private final JButton setButton = new JButton("Set Goal");
    private final DefaultTableModel model =
            UiHelper.readOnlyModel("Employee", "Goal", "Target Date", "Status", "Set By");

    public ManagerGoalPanel(User manager) {
        this.manager = manager;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildForm();
        add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);

        employeeBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof User) {
                    setText(((User) value).getName());
                }
                return this;
            }
        });

        loadEmployees();
        loadGoals();
    }

    private void buildForm() {
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        form.add(new JLabel("Employee:"));
        form.add(employeeBox);
        form.add(new JLabel("Goal:"));
        form.add(descriptionField);
        form.add(new JLabel("Target date (yyyy-mm-dd):"));
        form.add(dateField);
        form.add(setButton);
        add(form, BorderLayout.NORTH);
        setButton.addActionListener(e -> setGoal());
    }

    /** Fills the employee drop-down in a background thread. */
    private void loadEmployees() {
        new SwingWorker<List<User>, Void>() {
            @Override
            protected List<User> doInBackground() throws Exception {
                return userService.getEmployees();
            }

            @Override
            protected void done() {
                try {
                    for (User u : get()) {
                        employeeBox.addItem(u);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    UiHelper.showError(ManagerGoalPanel.this, ex.getCause());
                }
            }
        }.execute();
    }

    /** Reloads the goals table in a background thread. */
    private void loadGoals() {
        new SwingWorker<List<Goal>, Void>() {
            @Override
            protected List<Goal> doInBackground() throws Exception {
                return goalService.getAllGoals();
            }

            @Override
            protected void done() {
                try {
                    model.setRowCount(0);
                    for (Goal g : get()) {
                        model.addRow(new Object[]{g.getEmployeeName(), g.getDescription(),
                                g.getTargetDate(), g.getStatus(), g.getSetByName()});
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    UiHelper.showError(ManagerGoalPanel.this, ex.getCause());
                }
            }
        }.execute();
    }

    /** Saves the goal in a background thread and shows the confirmation message. */
    private void setGoal() {
        final User employee = (User) employeeBox.getSelectedItem();
        final String description = descriptionField.getText();
        final String date = dateField.getText();
        setButton.setEnabled(false);

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return goalService.setGoalForEmployee(manager, employee, description, date);
            }

            @Override
            protected void done() {
                try {
                    UiHelper.showInfo(ManagerGoalPanel.this, get());
                    descriptionField.setText("");
                    loadGoals();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    UiHelper.showError(ManagerGoalPanel.this, ex.getCause());
                }
                setButton.setEnabled(true);
            }
        }.execute();
    }
}
