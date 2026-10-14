package com.epes;

import com.epes.ui.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Entry point of the application: opens the Login screen. */
public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // the default look is fine if the system look is not available
        }
        // Swing screens must be created on the Swing thread
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
