package com.quilot.ui.help;

import com.quilot.exceptions.CredentialStorageException;
import com.quilot.exceptions.stt.STTAuthenticationException;
import com.quilot.stt.GoogleCloudSpeechToTextService;
import com.quilot.utils.CredentialManager;
import com.quilot.utils.Logger;
import lombok.Getter;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

@Getter
public class CredentialsSetupDialog extends JDialog {

    private final JTextField credentialPathField;
    private final JButton browseButton;
    private final JButton saveButton;
    private final JButton testButton;

    private final CredentialManager credentialManager;
    private final GoogleCloudSpeechToTextService speechToTextService;
    private final JFrame ownerFrame;

    public CredentialsSetupDialog(JFrame owner, CredentialManager credentialManager, GoogleCloudSpeechToTextService speechToTextService) {
        super(owner, "Google Cloud STT Credentials", true);
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

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Service Account Key Path:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        add(credentialPathField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        add(browseButton, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.add(saveButton);
        buttonPanel.add(testButton);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        add(buttonPanel, gbc);

        pack();
        setLocationRelativeTo(ownerFrame);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void addListeners() {
        browseButton.addActionListener(_ -> openFileChooser());
        saveButton.addActionListener(_ -> saveCredentials());
        testButton.addActionListener(_ -> testCredentials());
    }

    private void openFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Google Cloud Service Account JSON Key File");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("JSON Key Files", "json"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            credentialPathField.setText(selectedFile.getAbsolutePath());
            Logger.info("Selected credential file: " + selectedFile.getAbsolutePath());
        }
    }

    private void saveCredentials() {
        String path = credentialPathField.getText().trim();
        if (path.isEmpty()) {
            showErrorMessage("Credential path cannot be empty.", "Save Error");
            Logger.warn("Attempted to save empty credential path.");
            return;
        }

        try {
            credentialManager.saveGoogleCloudCredentialPath(path);
            speechToTextService.setCredentialPath(path);
            showInfoMessage("Credential path saved and applied successfully.", "Save Success");
        } catch (CredentialStorageException | STTAuthenticationException e) {
            Logger.error("Failed to apply new credential path.", e);
            showErrorMessage("Saved path, but failed to apply it to the service:\n" + e.getMessage(), "Credential Error");
        }
    }

    private void testCredentials() {
        Logger.info("Testing Google Cloud STT credentials...");
        String path = credentialPathField.getText().trim();
        if (path.isEmpty()) {
            showWarningMessage("Please provide a credential path before testing.", "Input Error");
            return;
        }

        try {
            speechToTextService.setCredentialPath(path);
            speechToTextService.testCredentials();

            showInfoMessage("Credentials test successful! The service is ready.", "Test Success");
            Logger.info("Credentials test successful.");
        } catch (STTAuthenticationException e) {
            String errorMessage = "Authentication Failed: " + e.getMessage();
            showErrorMessage(errorMessage, "Test Failed");
            Logger.error(errorMessage, e.getCause());
        } catch (Exception e) {
            String errorMessage = "An unexpected error occurred during testing: " + e.getMessage();
            showErrorMessage(errorMessage, "Test Error");
            Logger.error(errorMessage, e);
        }
    }

    private void loadSavedCredentialPath() {
        String savedPath = credentialManager.loadGoogleCloudCredentialPath();
        credentialPathField.setText(savedPath);
    }

    private void showInfoMessage(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showErrorMessage(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void showWarningMessage(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
    }
}
