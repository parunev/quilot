package com.quilot.ui.help;

import com.quilot.stt.GoogleCloudSpeechToTextService;
import com.quilot.utils.CredentialManager;
import com.quilot.utils.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class CredentialsSetupDialog extends JDialog {

    private final JTextField credentialPathField;
    private final JButton browseButton;
    private final JButton saveButton;
    private final JButton testButton;

    private final CredentialManager credentialManager;
    private final GoogleCloudSpeechToTextService speechToTextService;
    private final JFrame ownerFrame; // Reference to the main frame for centering

    public CredentialsSetupDialog(JFrame owner, CredentialManager credentialManager, GoogleCloudSpeechToTextService speechToTextService) {
        super(owner, "Google Cloud STT Credentials", true); // Modal dialog
        this.ownerFrame = owner;
        this.credentialManager = credentialManager;
        this.speechToTextService = speechToTextService;
        this.credentialPathField = new JTextField(40);
        this.browseButton = new JButton("Browse...");
        this.saveButton = new JButton("Save Credentials");
        this.testButton = new JButton("Test Credentials");

        initComponents();
        addListeners();
        loadSavedCredentialPath();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Label for path
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Service Account Key Path:"), gbc);

        // Text field for path
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        add(credentialPathField, gbc);

        // Browse button
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0;
        add(browseButton, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.add(saveButton);
        buttonPanel.add(testButton);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        add(buttonPanel, gbc);

        pack(); // Adjusts dialog size to fit components
        setLocationRelativeTo(ownerFrame); // Center relative to the main frame
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void addListeners() {
        browseButton.addActionListener(_ -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Google Cloud Service Account JSON Key File");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON Key Files", "json"));

            int returnValue = fileChooser.showOpenDialog(this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                credentialPathField.setText(selectedFile.getAbsolutePath());
                Logger.info("Selected credential file: " + selectedFile.getAbsolutePath());
            }
        });

        saveButton.addActionListener(_ -> {
            String path = credentialPathField.getText().trim();
            if (path.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Credential path cannot be empty.", "Save Error", JOptionPane.ERROR_MESSAGE);
                Logger.warn("Attempted to save empty credential path.");
            } else {
                credentialManager.saveGoogleCloudCredentialPath(path);
                speechToTextService.setCredentialPath(path); // Update the STT service with new path
                JOptionPane.showMessageDialog(this, "Credential path saved successfully.", "Save Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        testButton.addActionListener(_ -> {
            Logger.info("Testing Google Cloud STT credentials...");
            String testResult;
            try {
                // Ensure the STT service is initialized with the current path from the text field
                // This call to setCredentialPath will trigger initializeClient() inside the service
                speechToTextService.setCredentialPath(credentialPathField.getText().trim());

                // Now, call the testCredentials method on the service
                boolean success = speechToTextService.testCredentials();

                if (success) {
                    testResult = "Credentials test successful! SpeechClient initialized.";
                    Logger.info(testResult);
                    JOptionPane.showMessageDialog(this, testResult, "Test Result", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    testResult = "Credentials test failed. Check logs for details (e.g., invalid path, network issues).";
                    Logger.error(testResult);
                    JOptionPane.showMessageDialog(this, testResult, "Test Result", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                testResult = "Credentials test failed unexpectedly: " + ex.getMessage();
                Logger.error(testResult);
                JOptionPane.showMessageDialog(this, testResult, "Test Result", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void loadSavedCredentialPath() {
        String savedPath = credentialManager.loadGoogleCloudCredentialPath();
        credentialPathField.setText(savedPath);
    }
}
