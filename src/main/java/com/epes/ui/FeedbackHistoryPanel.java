package com.epes.ui;

import com.epes.model.Feedback;
import com.epes.model.User;
import com.epes.service.FeedbackService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;

/** Employee "Feedback History" tab: all feedback received from managers. */
public class FeedbackHistoryPanel extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final User employee;
    private final FeedbackService feedbackService = new FeedbackService();
    private final JLabel summaryLabel = new JLabel(" ");
    private final DefaultTableModel model =
            UiHelper.readOnlyModel("From", "Rating (1-5)", "Comments", "Date");

    public FeedbackHistoryPanel(User employee) {
        this.employee = employee;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> loadFeedback());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        top.add(refresh);
        top.add(summaryLabel);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
        loadFeedback();
    }

    /** Loads this employee's feedback in a background thread. */
    private void loadFeedback() {
        new SwingWorker<List<Feedback>, Void>() {
            @Override
            protected List<Feedback> doInBackground() throws Exception {
                return feedbackService.getFeedbackFor(employee.getId());
            }

            @Override
            protected void done() {
                try {
                    List<Feedback> list = get();
                    model.setRowCount(0);
                    int total = 0;
                    for (Feedback f : list) {
                        model.addRow(new Object[]{f.getManagerName(), f.getRating(), f.getComments(),
                                f.getCreatedAt().format(DATE_FORMAT)});
                        total += f.getRating();
                    }
                    if (list.isEmpty()) {
                        summaryLabel.setText("No feedback received yet.");
                    } else {
                        summaryLabel.setText(String.format("%d feedback(s), average rating %.1f / 5",
                                list.size(), (double) total / list.size()));
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    UiHelper.showError(FeedbackHistoryPanel.this, ex.getCause());
                }
            }
        }.execute();
    }
}
