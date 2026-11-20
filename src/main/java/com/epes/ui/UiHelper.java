package com.epes.ui;

import com.epes.exception.DatabaseException;
import com.epes.exception.ValidationException;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;

/** Small helpers shared by all screens, so the panels stay short. */
public final class UiHelper {

    private UiHelper() { }

    /** A table model whose cells cannot be edited by typing. */
    public static DefaultTableModel readOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    /** Shows a success message box. */
    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Shows the right kind of message box for each exception type. */
    public static void showError(Component parent, Throwable error) {
        if (error instanceof ValidationException) {
            JOptionPane.showMessageDialog(parent, error.getMessage(), "Please check your input",
                    JOptionPane.WARNING_MESSAGE);
        } else if (error instanceof DatabaseException) {
            JOptionPane.showMessageDialog(parent, "Database problem: " + error.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(parent, "Unexpected error: " + error,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
