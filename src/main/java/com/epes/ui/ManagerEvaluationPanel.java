package com.epes.ui;

import com.epes.model.Criteria;
import com.epes.model.Evaluation;
import com.epes.model.User;
import com.epes.service.EvaluationService;
import com.epes.service.UserService;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/** Manager "Performance Evaluation" tab: score an employee on 5 criteria and see all evaluations. */
public class ManagerEvaluationPanel extends JPanel {

    private final User manager;
    private final EvaluationService evaluationService = new EvaluationService();
    private final UserService userService = new UserService();

    private final JComboBox<User> employeeBox = new JComboBox<>();
    private final Map<Criteria, JSpinner> spinners = new EnumMap<>(Criteria.class);
    private final JButton evaluateButton = new JButton("Evaluate");
    private final DefaultTableModel model = UiHelper.readOnlyModel(
            "Employee", "Quality", "Teamwork", "Punctuality", "Productivity", "Communication",
            "Total", "Date", "Evaluated By");

    public ManagerEvaluationPanel(User manager) {
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
        loadEvaluations();
    }

    private void buildForm() {
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        row1.add(new JLabel("Employee:"));
        row1.add(employeeBox);
        row1.add(evaluateButton);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        row2.add(new JLabel("Scores (1 to 10):"));
        for (Criteria c : Criteria.values()) {
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(7, 1, 10, 1));
            spinners.put(c, spinner);
            row2.add(new JLabel(c.getLabel()));
            row2.add(spinner);
        }

        JPanel form = new JPanel(new GridLayout(2, 1));
        form.add(row1);
        form.add(row2);
        add(form, BorderLayout.NORTH);
        evaluateButton.addActionListener(e -> evaluate());
    }

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
                    UiHelper.showError(ManagerEvaluationPanel.this, ex.getCause());
                }
            }
        }.execute();
    }

    private void loadEvaluations() {
        new SwingWorker<List<Evaluation>, Void>() {
            @Override
            protected List<Evaluation> doInBackground() throws Exception {
                return evaluationService.getAllEvaluations();
            }

            @Override
            protected void done() {
                try {
                    model.setRowCount(0);
                    for (Evaluation ev : get()) {
                        model.addRow(new Object[]{ev.getEmployeeName(),
                                ev.getScore(Criteria.QUALITY), ev.getScore(Criteria.TEAMWORK),
                                ev.getScore(Criteria.PUNCTUALITY), ev.getScore(Criteria.PRODUCTIVITY),
                                ev.getScore(Criteria.COMMUNICATION),
                                String.format("%.2f", ev.getTotalScore()), ev.getEvalDate(),
                                ev.getManagerName()});
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    UiHelper.showError(ManagerEvaluationPanel.this, ex.getCause());
                }
            }
        }.execute();
    }

    /** Reads the five scores, saves the evaluation in a background thread and shows the report. */
    private void evaluate() {
        final User employee = (User) employeeBox.getSelectedItem();
        final Map<Criteria, Integer> scores = new EnumMap<>(Criteria.class);
        for (Criteria c : Criteria.values()) {
            scores.put(c, (Integer) spinners.get(c).getValue());
        }
        evaluateButton.setEnabled(false);

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return evaluationService.evaluate(manager, employee, scores);
            }

            @Override
            protected void done() {
                try {
                    UiHelper.showInfo(ManagerEvaluationPanel.this, get());
                    loadEvaluations();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    UiHelper.showError(ManagerEvaluationPanel.this, ex.getCause());
                }
                evaluateButton.setEnabled(true);
            }
        }.execute();
    }
}
