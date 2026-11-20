package com.epes.ui;

import com.epes.model.Feedback;
import com.epes.model.User;
import com.epes.service.FeedbackService;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;

/** Manager "Feedback" tab: a form to give feedback and a table of all feedback given. */
public class ManagerFeedbackPanel extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final User manager;
    private final FeedbackService feedbackService = new FeedbackService();
    private final UserService userService = new UserService();

    private final JComboBox<User> employeeBox = new JComboBox<>();
    private final JTextField commentsField = new JTextField(28);
    private final JComboBox<Integer> ratingBox = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
    private final JButton submitButton = new JButton("Submit Feedback");
    private final DefaultTableModel model =
            UiHelper.readOnlyModel("Employee", "Rating (1-5)", "Comments", "Given By", "Date");

    public ManagerFeedbackPanel(User manager) {
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

        ratingBox.setSelectedItem(5);
        loadEmployees();
        loadFeedback();
    }

    private void buildForm() {
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        form.add(new JLabel("Employee:"));
        form.add(employeeBox);
        form.add(new JLabel("Comments:"));
        form.add(commentsField);
        form.add(new JLabel("Rating:"));
        form.add(ratingBox);
        form.add(submitButton);
        add(form, BorderLayout.NORTH);
        submitButton.addActionListener(e -> submitFeedback());
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
                    UiHelper.showError(ManagerFeedbackPanel.this, ex.getCause());
                }
            }
        }.execute();
    }

    private void loadFeedback() {
        new SwingWorker<List<Feedback>, Void>() {
            @Override
            protected List<Feedback> doInBackground() throws Exception {
                return feedbackService.getAllFeedback();
            }

            @Override
            protected void done() {
                try {
                    model.setRowCount(0);
                    for (Feedback f : get()) {
                        model.addRow(new Object[]{f.getEmployeeName(), f.getRating(), f.getComments(),
                                f.getManagerName(), f.getCreatedAt().format(DATE_FORMAT)});
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    UiHelper.showError(ManagerFeedbackPanel.this, ex.getCause());
                }
            }
        }.execute();
    }

    private void submitFeedback() {
        final User employee = (User) employeeBox.getSelectedItem();
        final String comments = commentsField.getText();
        final int rating = (Integer) ratingBox.getSelectedItem();
        submitButton.setEnabled(false);

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return feedbackService.giveFeedback(manager, employee, comments, rating);
            }

            @Override
            protected void done() {
                try {
                    UiHelper.showInfo(ManagerFeedbackPanel.this, get());
                    commentsField.setText("");
                    loadFeedback();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    UiHelper.showError(ManagerFeedbackPanel.this, ex.getCause());
                }
                submitButton.setEnabled(true);
            }
        }.execute();
    }
}
