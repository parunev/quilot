package com.quilot.ui.settings;

import com.quilot.ai.settings.AIConfigSettings;
import com.quilot.ai.settings.IAISettingsManager;
import com.quilot.ai.VertexAIService;
import com.quilot.utils.Logger;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;

/**
 * A JDialog for configuring Google Cloud Vertex AI settings.
 * Allows users to input project ID, location, model ID, and generation parameters.
 */
public class AISettingsDialog extends JDialog {

    private final IAISettingsManager settingsManager;
    private final VertexAIService aiService; // To update AI service with new settings

    // UI Components
    private JTextField projectIdField;
    private JTextField locationField;
    private JTextField modelIdField;
    private JFormattedTextField temperatureField;
    private JFormattedTextField maxOutputTokensField;
    private JFormattedTextField topPField;
    private JFormattedTextField topKField;

    private JButton saveButton;
    private JButton loadDefaultsButton;
    private JButton closeButton;

    public AISettingsDialog(JFrame owner, IAISettingsManager settingsManager, VertexAIService aiService) {
        super(owner, "Vertex AI Settings", true); // Modal dialog
        this.settingsManager = settingsManager;
        this.aiService = aiService;

        initComponents();
        addListeners();
        loadSettingsIntoUI(settingsManager.loadSettings()); // Load initial settings on dialog open
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Project ID
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("GCP Project ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 1.0;
        projectIdField = new JTextField(20);
        add(projectIdField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        add(new JLabel("<html><small>Your Google Cloud Project ID.</small></html>"), gbc);

        // Location
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        add(new JLabel("Location (Region):"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weightx = 1.0;
        locationField = new JTextField(20);
        add(locationField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3;
        add(new JLabel("<html><small>e.g., 'us-central1' where your model is deployed.</small></html>"), gbc);

        // Model ID
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        add(new JLabel("Model ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.gridwidth = 2; gbc.weightx = 1.0;
        modelIdField = new JTextField(20);
        add(modelIdField, gbc);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 3;
        add(new JLabel("<html><small>e.g., 'text-bison@001' or your fine-tuned model ID.</small></html>"), gbc);

        // Temperature
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1;
        add(new JLabel("Temperature (0.0 - 1.0):"), gbc);
        gbc.gridx = 1; gbc.gridy = 6; gbc.gridwidth = 2; gbc.weightx = 1.0;
        temperatureField = new JFormattedTextField(NumberFormat.getNumberInstance());
        temperatureField.setColumns(5);
        add(temperatureField, gbc);
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 3;
        add(new JLabel("<html><small>Controls randomness. Lower = more deterministic.</small></html>"), gbc);

        // Max Output Tokens
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 1;
        add(new JLabel("Max Output Tokens:"), gbc);
        gbc.gridx = 1; gbc.gridy = 8; gbc.gridwidth = 2; gbc.weightx = 1.0;
        maxOutputTokensField = new JFormattedTextField(NumberFormat.getIntegerInstance());
        maxOutputTokensField.setColumns(5);
        add(maxOutputTokensField, gbc);
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 3;
        add(new JLabel("<html><small>Maximum number of tokens in the AI's response.</small></html>"), gbc);

        // Top P
        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 1;
        add(new JLabel("Top P (0.0 - 1.0):"), gbc);
        gbc.gridx = 1; gbc.gridy = 10; gbc.gridwidth = 2; gbc.weightx = 1.0;
        topPField = new JFormattedTextField(NumberFormat.getNumberInstance());
        topPField.setColumns(5);
        add(topPField, gbc);
        gbc.gridx = 0; gbc.gridy = 11; gbc.gridwidth = 3;
        add(new JLabel("<html><small>Nucleus sampling. Lower = more focused.</small></html>"), gbc);

        // Top K
        gbc.gridx = 0; gbc.gridy = 12; gbc.gridwidth = 1;
        add(new JLabel("Top K (1 - 40):"), gbc);
        gbc.gridx = 1; gbc.gridy = 12; gbc.gridwidth = 2; gbc.weightx = 1.0;
        topKField = new JFormattedTextField(NumberFormat.getIntegerInstance());
        topKField.setColumns(5);
        add(topKField, gbc);
        gbc.gridx = 0; gbc.gridy = 13; gbc.gridwidth = 3;
        add(new JLabel("<html><small>Top K tokens considered for sampling.</small></html>"), gbc);

        // Buttons
        saveButton = new JButton("Save Settings");
        loadDefaultsButton = new JButton("Load Defaults");
        closeButton = new JButton("Close");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.add(saveButton);
        buttonPanel.add(loadDefaultsButton);
        buttonPanel.add(closeButton);

        gbc.gridx = 0; gbc.gridy = 14; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.EAST;
        add(buttonPanel, gbc);

        pack(); // Adjusts dialog size to fit components
        setLocationRelativeTo(getOwner()); // Center relative to parent frame
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void addListeners() {
        saveButton.addActionListener(_ -> saveSettingsFromUI());
        loadDefaultsButton.addActionListener(_ -> loadDefaultsIntoUI());
        closeButton.addActionListener(_ -> dispose());
    }

    private void loadSettingsIntoUI(AIConfigSettings settings) {
        projectIdField.setText(settings.getProjectId());
        locationField.setText(settings.getLocation());
        modelIdField.setText(settings.getModelId());
        temperatureField.setValue(settings.getTemperature());
        maxOutputTokensField.setValue(settings.getMaxOutputTokens());
        topPField.setValue(settings.getTopP());
        topKField.setValue(settings.getTopK());
        Logger.info("AI settings loaded into UI.");
    }

    private void saveSettingsFromUI() {
        AIConfigSettings settings = new AIConfigSettings();
        try {
            settings.setProjectId(projectIdField.getText().trim());
            settings.setLocation(locationField.getText().trim());
            settings.setModelId(modelIdField.getText().trim());
            settings.setTemperature(((Number) temperatureField.getValue()).doubleValue());
            settings.setMaxOutputTokens(((Number) maxOutputTokensField.getValue()).intValue());
            settings.setTopP(((Number) topPField.getValue()).doubleValue());
            settings.setTopK(((Number) topKField.getValue()).intValue());

            settingsManager.saveSettings(settings); // Save to preferences
            // Re-initialize AI service with new settings
            aiService.setCredentialPath(aiService.getCredentialPath()); // This will trigger client re-init with new settings
            Logger.info("AI settings saved from UI and applied to service.");
            JOptionPane.showMessageDialog(this, "AI settings saved successfully!", "Settings Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            Logger.error("Invalid number format in AI settings: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Invalid number format in settings. Please check your input.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            Logger.error("Error saving AI settings: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Error saving settings: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadDefaultsIntoUI() {
        AIConfigSettings defaultSettings = settingsManager.resetToDefaults(); // Resets and saves defaults
        loadSettingsIntoUI(defaultSettings); // Load the newly reset defaults into the UI
        aiService.setCredentialPath(aiService.getCredentialPath()); // Re-initialize client with current path
        Logger.info("AI settings reset to defaults and applied to service.");
        JOptionPane.showMessageDialog(this, "AI settings reset to defaults!", "Defaults Loaded", JOptionPane.INFORMATION_MESSAGE);
    }
}
