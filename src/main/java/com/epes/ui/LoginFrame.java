package com.epes.ui;

import com.epes.exception.DatabaseException;
import com.epes.exception.InvalidCredentialsException;
import com.epes.exception.ValidationException;
import com.epes.model.Employee;
import com.epes.model.Manager;
import com.epes.model.User;
import com.epes.service.AuthService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.util.concurrent.ExecutionException;

/** The login screen. The database check runs in a background thread (SwingWorker). */
public class LoginFrame extends JFrame {

    private final AuthService authService = new AuthService();
    private final JTextField emailField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JButton loginButton = new JButton("Login");
    private final JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);

    public LoginFrame() {
        setTitle("Employee Performance Evaluation System - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 330);
        setLocationRelativeTo(null);
        buildScreen();
    }

    /** Creates all the labels, text fields and the button. */
    private void buildScreen() {
        JLabel title = new JLabel("Employee Performance Evaluation", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));

        JPanel form = new JPanel(new GridLayout(4, 1, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
        form.add(new JLabel("Email"));
        form.add(emailField);
        form.add(new JLabel("Password"));
        form.add(passwordField);

        statusLabel.setForeground(Color.RED);

        JPanel bottom = new JPanel(new GridLayout(2, 1, 5, 5));
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 40, 20, 40));
        bottom.add(loginButton);
        bottom.add(statusLabel);

        add(title, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        loginButton.addActionListener(e -> doLogin());
        getRootPane().setDefaultButton(loginButton);   // pressing Enter = click Login
    }

    /** Starts the login in a background thread so the window never freezes. */
    private void doLogin() {
        final String email = emailField.getText();
        final String password = new String(passwordField.getPassword());

        loginButton.setEnabled(false);
        statusLabel.setForeground(Color.DARK_GRAY);
        statusLabel.setText("Checking...");

        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                return authService.login(email, password);   // runs off the screen thread
            }

            @Override
            protected void done() {
                try {
                    openDashboard(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    showError(ex.getCause());
                }
                loginButton.setEnabled(true);
            }
        }.execute();
    }

    /** Polymorphism: a Manager gets the manager screen, an Employee the employee screen. */
    private void openDashboard(User user) {
        if (user instanceof Manager) {
            new ManagerDashboard((Manager) user).setVisible(true);
        } else {
            new EmployeeDashboard((Employee) user).setVisible(true);
        }
        dispose();   // close the login window
    }

    /** Shows a friendly message for each kind of exception. */
    private void showError(Throwable error) {
        statusLabel.setForeground(Color.RED);
        if (error instanceof ValidationException || error instanceof InvalidCredentialsException) {
            statusLabel.setText(error.getMessage());
        } else if (error instanceof DatabaseException) {
            statusLabel.setText("Database error - is MySQL running?");
            System.out.println(error.getMessage());
        } else {
            statusLabel.setText("Unexpected error: " + error);
        }
    }
}
