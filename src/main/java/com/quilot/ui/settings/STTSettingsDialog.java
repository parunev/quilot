package com.quilot.ui.settings;


import com.quilot.exceptions.stt.STTAuthenticationException;
import com.quilot.exceptions.stt.STTSettingsException;
import com.quilot.stt.GoogleCloudSpeechToTextService;
import com.quilot.stt.ISpeechToTextSettingsManager;
import com.quilot.stt.settings.RecognitionConfigSettings;
import com.quilot.utils.Logger;

import javax.swing.*;
import java.awt.*;

/**
 * A JDialog for configuring Google Cloud Speech-to-Text (STT) recognition settings.
 * Allows users to fine-tune transcription parameters.
 */
public class STTSettingsDialog extends JDialog {

    private final ISpeechToTextSettingsManager settingsManager;
    private final GoogleCloudSpeechToTextService speechToTextService; // To update service with new settings

    // UI Components for settings
    private JComboBox<String> languageCodeComboBox;
    private JCheckBox enableAutomaticPunctuationCheckBox;
    private JCheckBox enableWordTimeOffsetsCheckBox;
    private JComboBox<String> modelComboBox;
    private JTextArea speechContextsTextArea;
    private JCheckBox enableSingleUtterance;
    private JCheckBox enableInterimTranscription;

    private JButton saveButton;
    private JButton loadDefaultsButton;
    private JButton closeButton;

    // Supported models and languages (can be expanded)
    private static final String[] SUPPORTED_LANGUAGES = {"en-US", "en-GB", "es-ES", "fr-FR", "de-DE", "ja-JP"};
    private static final String[] SUPPORTED_MODELS = {"default", "command_and_search", "phone_call", "video", "latest_long", "latest_short"};


    public STTSettingsDialog(JFrame owner, ISpeechToTextSettingsManager settingsManager, GoogleCloudSpeechToTextService speechToTextService) {
        super(owner, "Google Cloud STT Settings", true);
        this.settingsManager = settingsManager;
        this.speechToTextService = speechToTextService;

        initComponents();
        addListeners();
        loadSettingsIntoUI(settingsManager.loadSettings());
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Language Code
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Language Code:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        languageCodeComboBox = new JComboBox<>(SUPPORTED_LANGUAGES);
        languageCodeComboBox.setEditable(true); // Allow custom entry
        add(languageCodeComboBox, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        add(new JLabel("<html><small>e.g., en-US (US English), en-GB (British English)</small></html>"), gbc);


        // Automatic Punctuation
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        enableAutomaticPunctuationCheckBox = new JCheckBox("Enable Automatic Punctuation");
        add(enableAutomaticPunctuationCheckBox, gbc);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        add(new JLabel("<html><small>Adds commas, periods, question marks, etc.</small></html>"), gbc);


        // Word Time Offsets
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        enableWordTimeOffsetsCheckBox = new JCheckBox("Enable Word Time Offsets");
        add(enableWordTimeOffsetsCheckBox, gbc);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        add(new JLabel("<html><small>Provides start and end times for each word.</small></html>"), gbc);


        // Model
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1;
        add(new JLabel("Recognition Model:"), gbc);
        gbc.gridx = 1; gbc.gridy = 6; gbc.weightx = 1.0;
        modelComboBox = new JComboBox<>(SUPPORTED_MODELS);
        modelComboBox.setEditable(true); // Allow custom entry
        add(modelComboBox, gbc);
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        add(new JLabel("<html><small>Optimized for different audio types (e.g., 'phone_call' for call center audio).</small></html>"), gbc);


        // Speech Contexts (Phrases)
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        add(new JLabel("Speech Contexts (one phrase per line to boost accuracy):"), gbc);
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 2; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        speechContextsTextArea = new JTextArea(5, 30); // 5 rows, 30 cols
        speechContextsTextArea.setLineWrap(true);
        speechContextsTextArea.setWrapStyleWord(true);
        add(new JScrollPane(speechContextsTextArea), gbc);
        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 2; gbc.weighty = 0; // Reset weighty
        add(new JLabel("<html><small>Custom words/phrases to improve recognition for domain-specific terms.</small></html>"), gbc);


        // Utterance
        gbc.gridx = 0; gbc.gridy = 11; gbc.gridwidth = 1;
        enableSingleUtterance = new JCheckBox("Enable Single Utterance");
        add(enableSingleUtterance, gbc);
        gbc.gridx = 0; gbc.gridy = 12; gbc.gridwidth = 2;
        add(new JLabel("<html><small>An utterance is a piece of spoken language with a start and an end. Utterances can be whole sentences or just single words used in communication. </small></html>"), gbc);

        // Interim Description
        gbc.gridx = 0; gbc.gridy = 14; gbc.gridwidth = 1;
        enableInterimTranscription = new JCheckBox("Enable Interim Transcription");
        add(enableInterimTranscription, gbc);
        gbc.gridx = 0; gbc.gridy = 15; gbc.gridwidth = 2;
        add(new JLabel("<html><small>Interim transcription shows real-time, partial speech-to-text results before the final transcription is confirmed.</small></html>"), gbc);


        // Buttons
        saveButton = new JButton("Save Settings");
        loadDefaultsButton = new JButton("Load Defaults");
        closeButton = new JButton("Close");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.add(saveButton);
        buttonPanel.add(loadDefaultsButton);
        buttonPanel.add(closeButton);

        gbc.gridx = 0; gbc.gridy = 18; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.EAST;
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

    private void loadSettingsIntoUI(RecognitionConfigSettings settings) {
        setComboBoxSelectedItem(languageCodeComboBox, settings.getLanguageCode());
        setComboBoxSelectedItem(modelComboBox, settings.getModel());

        enableAutomaticPunctuationCheckBox.setSelected(settings.isEnableAutomaticPunctuation());
        enableWordTimeOffsetsCheckBox.setSelected(settings.isEnableWordTimeOffsets());
        speechContextsTextArea.setText(settings.getSpeechContexts());
        enableSingleUtterance.setSelected(settings.isEnableSingleUtterance());
        enableInterimTranscription.setSelected(settings.isInterimTranscription());

        Logger.info("STT settings loaded into UI.");
    }

    private void saveSettingsFromUI() {
        try {
            RecognitionConfigSettings settings = getRecognitionConfigSettingsFromUI();
            settingsManager.saveSettings(settings);

            speechToTextService.setCredentialPath(speechToTextService.getCredentialPath());

            Logger.info("STT settings saved from UI and applied to service.");
            JOptionPane.showMessageDialog(this, "STT settings saved successfully!", "Settings Saved", JOptionPane.INFORMATION_MESSAGE);

        } catch (STTSettingsException e) {
            Logger.error("Failed to save STT settings.", e);
            JOptionPane.showMessageDialog(this, "Could not save settings: " + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        } catch (STTAuthenticationException e) {
            Logger.error("Failed to re-initialize STT service with new settings.", e);
            JOptionPane.showMessageDialog(this, "Settings saved, but failed to apply to service: " + e.getMessage(), "Service Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private RecognitionConfigSettings getRecognitionConfigSettingsFromUI() {
        return RecognitionConfigSettings.builder()
                .languageCode((String) languageCodeComboBox.getSelectedItem())
                .enableAutomaticPunctuation(enableAutomaticPunctuationCheckBox.isSelected())
                .enableWordTimeOffsets(enableWordTimeOffsetsCheckBox.isSelected())
                .model((String) modelComboBox.getSelectedItem())
                .speechContexts(speechContextsTextArea.getText())
                .enableSingleUtterance(enableSingleUtterance.isSelected())
                .interimTranscription(enableInterimTranscription.isSelected())
                .build();
    }

    private void loadDefaultsIntoUI() {
        try {
            RecognitionConfigSettings defaultSettings = settingsManager.resetToDefaults();
            loadSettingsIntoUI(defaultSettings);

            speechToTextService.setCredentialPath(speechToTextService.getCredentialPath());

            JOptionPane.showMessageDialog(this, "STT settings reset to defaults!", "Defaults Loaded", JOptionPane.INFORMATION_MESSAGE);
        } catch (STTSettingsException | STTAuthenticationException e) {
            Logger.error("Failed to load and apply default STT settings.", e);
            JOptionPane.showMessageDialog(this, "Could not reset settings: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setComboBoxSelectedItem(JComboBox<String> comboBox, String item) {
        if (item == null || item.isEmpty()) {
            comboBox.setSelectedIndex(0);
            return;
        }
        boolean found = false;
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            if (item.equals(comboBox.getItemAt(i))) {
                comboBox.setSelectedItem(item);
                found = true;
                break;
            }
        }
        if (!found && comboBox.isEditable()) {
            comboBox.addItem(item);
            comboBox.setSelectedItem(item);
        } else if (!found && !comboBox.isEditable()) {
            Logger.warn("Attempted to set unsupported non-editable combo box item: " + item + ". Falling back to default.");
            comboBox.setSelectedIndex(0);
        }
    }
}