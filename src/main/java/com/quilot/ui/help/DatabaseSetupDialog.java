package com.quilot.ui.help;

import com.quilot.db.DatabaseManager;
import com.quilot.utils.Logger;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * A modal dialog for setting up the initial database connection.
 * Prompts the user for their MySQL credentials and runs the schema setup.
 */
public class DatabaseSetupDialog extends JDialog {

    private final JTextField userField;
    private final JPasswordField passwordField;
    private boolean setupSuccessful = false;

    /**
     * Constructs the database setup dialog.
     * @param owner The parent frame.
     */
    public DatabaseSetupDialog(Frame owner) {
        super(owner, "Setup Database Connection", true);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        userField = new JTextField("root", 20);
        passwordField = new JPasswordField(20);
        JButton setupButton = new JButton("Connect & Setup");
        JButton cancelButton = new JButton("Cancel");

        // Layout components
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("MySQL Username:"), gbc);
        gbc.gridx = 1;
        add(userField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("MySQL Password:"), gbc);
        gbc.gridx = 1;
        add(passwordField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(cancelButton);
        buttonPanel.add(setupButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        add(buttonPanel, gbc);

        setupButton.addActionListener(_ -> onSetup());
        cancelButton.addActionListener(_ -> dispose());

        pack();
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    /**
     * Handles the setup button click event.
     * Attempts to create the database schema and save credentials.
     */
    private void onSetup() {
        String user = userField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (user.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // This will create the database and tables using the provided credentials
            DatabaseManager.setupDatabaseSchema(user, password);
            // If successful, save the credentials for future sessions
            DatabaseManager.saveCredentials(user, password);
            setupSuccessful = true;
            JOptionPane.showMessageDialog(this, "Database setup successful! Interview history is now enabled.", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (SQLException ex) {
            Logger.error("Database setup failed.", ex);
            JOptionPane.showMessageDialog(this, "Database setup failed:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            setupSuccessful = false;
        }
    }

    /**
     * Checks if the database setup was completed successfully.
     * @return true if setup was successful, false otherwise.
     */
    public boolean wasSetupSuccessful() {
        return setupSuccessful;
    }
}
