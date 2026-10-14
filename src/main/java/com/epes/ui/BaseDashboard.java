package com.epes.ui;

import com.epes.model.User;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

/**
 * Common frame for both dashboards: a header with the user's name, a Logout
 * button and one tab per feature. ManagerDashboard and EmployeeDashboard extend it.
 */
public abstract class BaseDashboard extends JFrame {

    protected final JTabbedPane tabs = new JTabbedPane();

    protected BaseDashboard(User user, String[] tabNames) {
        setTitle(user.getDashboardTitle());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        JLabel welcome = new JLabel("  Welcome, " + user.getName() + "  |  " + user.getDashboardTitle());
        welcome.setFont(new Font("SansSerif", Font.BOLD, 16));
        welcome.setForeground(Color.WHITE);

        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(15, 61, 94));
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        header.add(welcome, BorderLayout.CENTER);
        header.add(logout, BorderLayout.EAST);

        for (String name : tabNames) {
            tabs.addTab(name, placeholder(name));
        }

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    /** Temporary panel; each feature will replace it with a real table/chart later. */
    private JPanel placeholder(String name) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(name + " - coming soon", SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }
}
