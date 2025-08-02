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

    private JCheckBox useEnhancedCheckBox;
    private JCheckBox profanityFilterCheckBox;
    private JSpinner maxAlternativesSpinner;
    private JCheckBox enableSpeakerDiarizationCheckBox;

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
        gbc.anchor = GridBagConstraints.WEST;
        int y = 0;

        // Helper for creating info icons
        final Font infoFont = new Font("SansSerif", Font.BOLD, 12);
        final Color infoColor = Color.GRAY;

        // --- Settings Rows with Info Icons ---
        gbc.gridy = y++;
        gbc.gridx = 0; add(new JLabel("Language Code:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL; languageCodeComboBox = new JComboBox<>(SUPPORTED_LANGUAGES); languageCodeComboBox.setEditable(true); add(languageCodeComboBox, gbc);
        gbc.gridx = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE; add(createInfoIcon("The language of the audio (e.g., 'en-US'). Critical for accuracy.", infoFont, infoColor), gbc);

        gbc.gridy = y++;
        gbc.gridx = 0; add(new JLabel("Recognition Model:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL; modelComboBox = new JComboBox<>(SUPPORTED_MODELS); modelComboBox.setEditable(true); add(modelComboBox, gbc);
        gbc.gridx = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE; add(createInfoIcon("Optimizes recognition for specific audio types like 'phone_call' or 'video'.", infoFont, infoColor), gbc);

        gbc.gridy = y++;
        gbc.gridx = 0; gbc.gridwidth = 2; enableAutomaticPunctuationCheckBox = new JCheckBox("Enable Automatic Punctuation"); add(enableAutomaticPunctuationCheckBox, gbc);
        gbc.gridx = 2; gbc.gridwidth = 1; add(createInfoIcon("Adds punctuation (periods, commas, etc.) to the final transcript.", infoFont, infoColor), gbc);

        gbc.gridy = y++;
        gbc.gridx = 0; gbc.gridwidth = 2; enableInterimTranscription = new JCheckBox("Enable Interim Transcription"); add(enableInterimTranscription, gbc);
        gbc.gridx = 2; gbc.gridwidth = 1; add(createInfoIcon("Shows real-time, partial speech-to-text results as you speak.", infoFont, infoColor), gbc);

        gbc.gridy = y++;
        gbc.gridx = 0; gbc.gridwidth = 3; add(new JSeparator(), gbc);
        gbc.gridwidth = 1;

        gbc.gridy = y++;
        gbc.gridx = 0; gbc.gridwidth = 2; useEnhancedCheckBox = new JCheckBox("Use Enhanced Model"); add(useEnhancedCheckBox, gbc);
        gbc.gridx = 2; gbc.gridwidth = 1; add(createInfoIcon("Use a premium, higher-accuracy model. May affect cost.", infoFont, infoColor), gbc);

        gbc.gridy = y++;
        gbc.gridx = 0; gbc.gridwidth = 2; profanityFilterCheckBox = new JCheckBox("Filter Profanity"); add(profanityFilterCheckBox, gbc);
        gbc.gridx = 2; gbc.gridwidth = 1; add(createInfoIcon("Replaces recognized profane words with asterisks (e.g., 'f***').", infoFont, infoColor), gbc);

        gbc.gridy = y++;
        gbc.gridx = 0; gbc.gridwidth = 2; enableWordTimeOffsetsCheckBox = new JCheckBox("Enable Word Time Offsets"); add(enableWordTimeOffsetsCheckBox, gbc);
        gbc.gridx = 2; gbc.gridwidth = 1; add(createInfoIcon("Provides the start and end time for each transcribed word.", infoFont, infoColor), gbc);

        gbc.gridy = y++;
        gbc.gridx = 0; gbc.gridwidth = 2; enableSingleUtterance = new JCheckBox("Enable Single Utterance"); add(enableSingleUtterance, gbc);
        gbc.gridx = 2; gbc.gridwidth = 1; add(createInfoIcon("Stops transcription automatically after the first detected phrase or sentence.", infoFont, infoColor), gbc);

        gbc.gridy = y++;
        gbc.gridx = 0; gbc.gridwidth = 2; enableSpeakerDiarizationCheckBox = new JCheckBox("Enable Speaker Diarization"); add(enableSpeakerDiarizationCheckBox, gbc);
        gbc.gridx = 2; gbc.gridwidth = 1; add(createInfoIcon("Identifies and labels different speakers in the audio (e.g., 'Speaker 1:').", infoFont, infoColor), gbc);

        gbc.gridy = y++;
        gbc.gridx = 0; add(new JLabel("Max Alternatives:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL; maxAlternativesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 30, 1)); add(maxAlternativesSpinner, gbc);
        gbc.gridx = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE; add(createInfoIcon("How many possible alternative transcriptions to return.", infoFont, infoColor), gbc);

        gbc.gridy = y++;
        gbc.gridx = 0; gbc.gridwidth = 3; add(new JSeparator(), gbc);

        gbc.gridy = y++;
        gbc.gridwidth = 2; add(new JLabel("Speech Contexts (one phrase per line):"), gbc);
        gbc.gridx = 2; gbc.gridwidth = 1; add(createInfoIcon("Provide a list of specific words (like names or jargon) to improve their accuracy.", infoFont, infoColor), gbc);
        y++;

        gbc.gridy = y++;
        gbc.gridx = 0; gbc.gridwidth = 3; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH; speechContextsTextArea = new JTextArea(4, 30); add(new JScrollPane(speechContextsTextArea), gbc);

        // --- Buttons ---
        gbc.gridy = y; gbc.weighty = 0; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.EAST;
        saveButton = new JButton("Save"); loadDefaultsButton = new JButton("Defaults"); closeButton = new JButton("Close");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton); buttonPanel.add(loadDefaultsButton); buttonPanel.add(closeButton);
        add(buttonPanel, gbc);

        pack();
        setLocationRelativeTo(getOwner());
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
        useEnhancedCheckBox.setSelected(settings.isUseEnhanced());
        profanityFilterCheckBox.setSelected(settings.isProfanityFilter());
        maxAlternativesSpinner.setValue(settings.getMaxAlternatives());
        enableSpeakerDiarizationCheckBox.setSelected(settings.isEnableSpeakerDiarization());
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
                .model((String) modelComboBox.getSelectedItem())
                .speechContexts(speechContextsTextArea.getText())
                .enableAutomaticPunctuation(enableAutomaticPunctuationCheckBox.isSelected())
                .enableWordTimeOffsets(enableWordTimeOffsetsCheckBox.isSelected())
                .enableSingleUtterance(enableSingleUtterance.isSelected())
                .interimTranscription(enableInterimTranscription.isSelected())
                .useEnhanced(useEnhancedCheckBox.isSelected())
                .profanityFilter(profanityFilterCheckBox.isSelected())
                .maxAlternatives((Integer) maxAlternativesSpinner.getValue())
                .enableSpeakerDiarization(enableSpeakerDiarizationCheckBox.isSelected())
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

    private JLabel createInfoIcon(String tooltip, Font font, Color color) {
        JLabel infoLabel = new JLabel("(?)");
        infoLabel.setFont(font);
        infoLabel.setForeground(color);
        infoLabel.setToolTipText(tooltip);
        infoLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return infoLabel;
    }
}