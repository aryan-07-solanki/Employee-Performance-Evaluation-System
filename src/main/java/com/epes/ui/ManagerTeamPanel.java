package com.epes.ui;

import com.epes.model.User;
import com.epes.service.TeamService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/** Manager "Team Management" tab: see the team, add an employee, change a role, remove an employee. */
public class ManagerTeamPanel extends JPanel {

    private final User manager;
    private final TeamService teamService = new TeamService();
    private final DefaultTableModel model = UiHelper.readOnlyModel("ID", "Name", "Email", "Team Role");
    private final JTable table = new JTable(model);
    private List<User> members = new ArrayList<>();

    private final JTextField nameField = new JTextField(10);
    private final JTextField emailField = new JTextField(12);
    private final JPasswordField passwordField = new JPasswordField(8);
    private final JTextField roleField = new JTextField(10);

    public ManagerTeamPanel(User manager) {
        this.manager = manager;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        row1.add(new JLabel("Name"));
        row1.add(nameField);
        row1.add(new JLabel("Email"));
        row1.add(emailField);
        row1.add(new JLabel("Password"));
        row1.add(passwordField);
        row1.add(new JLabel("Team role"));
        row1.add(roleField);
        JButton add = new JButton("Add Employee");
        row1.add(add);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        row2.add(new JLabel("Select a row, then:"));
        JButton changeRole = new JButton("Change Role (use Team role box)");
        JButton remove = new JButton("Remove Employee");
        row2.add(changeRole);
        row2.add(remove);

        JPanel form = new JPanel(new GridLayout(2, 1));
        form.add(row1);
        form.add(row2);
        add(form, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        add.addActionListener(e -> run(() -> teamService.addEmployee(manager,
                nameField.getText(), emailField.getText(), new String(passwordField.getPassword()),
                roleField.getText()), true));
        changeRole.addActionListener(e -> run(() -> teamService.changeRole(selected(), roleField.getText()), false));
        remove.addActionListener(e -> removeSelected());

        load();
    }

    /** @return the employee chosen in the table, or null if no row is selected */
    private User selected() {
        int row = table.getSelectedRow();
        return row < 0 ? null : members.get(table.convertRowIndexToModel(row));
    }

    private void removeSelected() {
        User chosen = selected();
        if (chosen != null) {
            int answer = JOptionPane.showConfirmDialog(this,
                    "Remove " + chosen.getName() + "? Their evaluations, goals and feedback will be deleted too.",
                    "Confirm", JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) {
                return;
            }
        }
        run(() -> teamService.removeEmployee(chosen), false);
    }

    private void load() {
        new SwingWorker<List<User>, Void>() {
            @Override
            protected List<User> doInBackground() throws Exception {
                return teamService.getMembers();
            }

            @Override
            protected void done() {
                try {
                    members = get();
                    model.setRowCount(0);
                    for (User u : members) {
                        model.addRow(new Object[]{u.getId(), u.getName(), u.getEmail(),
                                u.getTeamRole() == null ? "" : u.getTeamRole()});
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    UiHelper.showError(ManagerTeamPanel.this, ex.getCause());
                }
            }
        }.execute();
    }

    /** Runs one team action in a background thread, shows its message and reloads the table. */
    private void run(Callable<String> action, boolean clearForm) {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return action.call();
            }

            @Override
            protected void done() {
                try {
                    UiHelper.showInfo(ManagerTeamPanel.this, get());
                    if (clearForm) {
                        nameField.setText("");
                        emailField.setText("");
                        passwordField.setText("");
                        roleField.setText("");
                    }
                    load();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    UiHelper.showError(ManagerTeamPanel.this, ex.getCause());
                }
            }
        }.execute();
    }
}
