package com.quilot.ui.settings;

import com.quilot.exceptions.stt.STTAuthenticationException;
import com.quilot.exceptions.stt.STTSettingsException;
import com.quilot.stt.GoogleCloudSpeechToTextService;
import com.quilot.stt.ISpeechToTextSettingsManager;
import com.quilot.stt.settings.RecognitionConfigSettings;
import com.quilot.stt.settings.SttLanguageData;
import com.quilot.stt.settings.SttLanguageFeatureData;
import com.quilot.utils.Logger;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.Objects;

/**
 * A JDialog for configuring Google Cloud Speech-to-Text (STT) recognition settings.
 * Allows users to fine-tune transcription parameters.
 */
@Getter
public class STTSettingsDialog extends JDialog {

    private final ISpeechToTextSettingsManager settingsManager;
    private final GoogleCloudSpeechToTextService speechToTextService;

    // UI Components
    private JComboBox<SttLanguageData.Language> languageCodeComboBox;
    private JComboBox<String> modelComboBox;
    private JCheckBox enableAutomaticPunctuationCheckBox;
    private JCheckBox enableWordTimeOffsetsCheckBox;
    private JCheckBox enableSpeakerDiarizationCheckBox;
    private JCheckBox enableQuestionDetectionCheckBox;
    private JLabel speechContextsLabel;
    private JTextArea speechContextsTextArea;
    private JCheckBox enableSingleUtterance;
    private JCheckBox enableInterimTranscription;
    private JCheckBox useEnhancedCheckBox;
    private JCheckBox profanityFilterCheckBox;
    private JSpinner maxAlternativesSpinner;
    private JButton saveButton, loadDefaultsButton, closeButton;

    public STTSettingsDialog(JFrame owner, ISpeechToTextSettingsManager settingsManager, GoogleCloudSpeechToTextService speechToTextService) {
        super(owner, "Google Cloud STT Settings", true);
        this.settingsManager = settingsManager;
        this.speechToTextService = speechToTextService;

        setResizable(false);

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

        // Initialize Components
        languageCodeComboBox = new JComboBox<>(SttLanguageData.getLanguages().toArray(new SttLanguageData.Language[0]));
        modelComboBox = new JComboBox<>();
        enableAutomaticPunctuationCheckBox = new JCheckBox("Enable Automatic Punctuation");
        enableInterimTranscription = new JCheckBox("Enable Interim Transcription");
        useEnhancedCheckBox = new JCheckBox("Use Enhanced Model");
        profanityFilterCheckBox = new JCheckBox("Filter Profanity");
        enableWordTimeOffsetsCheckBox = new JCheckBox("Enable Word Time Offsets");
        enableSingleUtterance = new JCheckBox("Enable Single Utterance");
        enableSpeakerDiarizationCheckBox = new JCheckBox("Enable Speaker Diarization");
        enableQuestionDetectionCheckBox = new JCheckBox("Enable Question Detection (Cost Saver)");
        maxAlternativesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 30, 1));
        speechContextsLabel = new JLabel("Speech Contexts (one phrase per line):");
        speechContextsTextArea = new JTextArea(4, 30);
        saveButton = new JButton("Save");
        loadDefaultsButton = new JButton("Defaults");
        closeButton = new JButton("Close");

        // Layout
        gbc.gridy = y++;
        gbc.gridx = 0; add(new JLabel("Language:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL; add(languageCodeComboBox, gbc);
        gbc.gridx = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE; add(createInfoIcon("The language of the audio. Affects which features are available.", infoFont, infoColor), gbc);

        gbc.gridy = y++;
        gbc.gridx = 0; add(new JLabel("Model:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL; add(modelComboBox, gbc);
        gbc.gridx = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE; add(createInfoIcon("Optimizes recognition for specific audio types like 'phone_call'.", infoFont, infoColor), gbc);

        gbc.gridy = y++;
        gbc.gridx = 0; gbc.gridwidth = 2; add(enableAutomaticPunctuationCheckBox, gbc);
        gbc.gridx = 2; gbc.gridwidth = 1; add(createInfoIcon("Adds punctuation (periods, commas, etc.) to the final transcript.", infoFont, infoColor), gbc);

        gbc.gridy = y++;
        gbc.gridx = 0; gbc.gridwidth = 2; add(enableInterimTranscription, gbc);
        gbc.gridx = 2; gbc.gridwidth = 1; add(createInfoIcon("Shows real-time, partial speech-to-text results as you speak.", infoFont, infoColor), gbc);

        gbc.gridy = y++;
        gbc.gridx = 0; gbc.gridwidth = 3; add(new JSeparator(), gbc);
        gbc.gridwidth = 1;

        gbc.gridy = y++;
        gbc.gridx = 0; gbc.gridwidth = 2; add(useEnhancedCheckBox, gbc);
        gbc.gridx = 2; gbc.gridwidth = 1; add(createInfoIcon("Use a premium, higher-accuracy model. May affect cost.", infoFont, infoColor), gbc);

        gbc.gridy = y++;
        gbc.gridx = 0; gbc.gridwidth = 2; add(profanityFilterCheckBox, gbc);
        gbc.gridx = 2; gbc.gridwidth = 1; add(createInfoIcon("Replaces recognized profane words with asterisks (e.g., 'f***').", infoFont, infoColor), gbc);

        gbc.gridy = y++;
        gbc.gridx = 0; gbc.gridwidth = 2; add(enableWordTimeOffsetsCheckBox, gbc);
        gbc.gridx = 2; gbc.gridwidth = 1; add(createInfoIcon("Provides the start and end time for each transcribed word.", infoFont, infoColor), gbc);

        gbc.gridy = y++;
        gbc.gridx = 0; gbc.gridwidth = 2; add(enableSingleUtterance, gbc);
        gbc.gridx = 2; gbc.gridwidth = 1; add(createInfoIcon("Stops transcription automatically after the first detected phrase or sentence.", infoFont, infoColor), gbc);

        gbc.gridy = y++;
        gbc.gridx = 0; gbc.gridwidth = 2; add(enableSpeakerDiarizationCheckBox, gbc);
        gbc.gridx = 2; gbc.gridwidth = 1; add(createInfoIcon("Identifies and labels different speakers in the audio (e.g., 'Speaker 1:').", infoFont, infoColor), gbc);

        gbc.gridy = y++;
        gbc.gridx = 0; gbc.gridwidth = 2; add(enableQuestionDetectionCheckBox, gbc);
        gbc.gridx = 2; gbc.gridwidth = 1; add(createInfoIcon("Only sends detected questions to the AI, ignoring other speech to reduce cost.", infoFont, infoColor), gbc);

        gbc.gridy = y++;
        gbc.gridx = 0; gbc.gridwidth = 3; gbc.insets = new Insets(0, 5, 5, 5);
        JLabel warningLabel = new JLabel("<html><small><i>Note: Accuracy is highest for languages with full punctuation support (e.g., en-US).</i></small></html>");
        warningLabel.setForeground(Color.GRAY);
        add(warningLabel, gbc);
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridy = y++;
        gbc.gridwidth = 1;
        gbc.gridx = 0; add(new JLabel("Max Alternatives:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL; add(maxAlternativesSpinner, gbc);
        gbc.gridx = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE; add(createInfoIcon("How many possible alternative transcriptions to return.", infoFont, infoColor), gbc);

        gbc.gridy = y++;
        gbc.gridx = 0; gbc.gridwidth = 3; add(new JSeparator(), gbc);

        gbc.gridy = y++;
        gbc.gridwidth = 2; add(speechContextsLabel, gbc);
        gbc.gridx = 2; gbc.gridwidth = 1; add(createInfoIcon("Provide a list of specific words (like names or jargon) to improve their accuracy.", infoFont, infoColor), gbc);

        gbc.gridy = y++;
        gbc.gridx = 0; gbc.gridwidth = 3; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH; add(new JScrollPane(speechContextsTextArea), gbc);

        gbc.gridy = y++;
        gbc.weighty = 0; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.EAST;
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

        languageCodeComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateModelComboBox();
                updateFeatureSupport();
            }
        });
    }

    private void updateModelComboBox() {
        SttLanguageData.Language selectedLanguage = (SttLanguageData.Language) languageCodeComboBox.getSelectedItem();
        if (selectedLanguage == null) return;

        String previouslySelectedModel = (String) modelComboBox.getSelectedItem();
        modelComboBox.removeAllItems();
        List<String> models = SttLanguageData.getModels(selectedLanguage);
        models.forEach(modelComboBox::addItem);

        if (previouslySelectedModel != null && models.contains(previouslySelectedModel)) {
            modelComboBox.setSelectedItem(previouslySelectedModel);
        }
    }

    private void updateFeatureSupport() {
        SttLanguageData.Language selectedLanguage = (SttLanguageData.Language) languageCodeComboBox.getSelectedItem();
        if (selectedLanguage == null) return;

        SttLanguageFeatureData.FeatureSupport support = SttLanguageFeatureData.getSupportFor(selectedLanguage.code());

        enableAutomaticPunctuationCheckBox.setEnabled(support.hasAutomaticPunctuation());
        if (!support.hasAutomaticPunctuation()) enableAutomaticPunctuationCheckBox.setSelected(false);

        enableSpeakerDiarizationCheckBox.setEnabled(support.hasDiarization());
        if (!support.hasDiarization()) enableSpeakerDiarizationCheckBox.setSelected(false);

        enableWordTimeOffsetsCheckBox.setEnabled(support.hasWordConfidence());
        if (!support.hasWordConfidence()) enableWordTimeOffsetsCheckBox.setSelected(false);

        speechContextsLabel.setEnabled(support.hasModelAdaptation());
        speechContextsTextArea.setEnabled(support.hasModelAdaptation());
        if (!support.hasModelAdaptation()) speechContextsTextArea.setText("");
    }

    private void loadSettingsIntoUI(RecognitionConfigSettings settings) {
        SttLanguageData.getLanguages().stream()
                .filter(lang -> lang.code().equals(settings.getLanguageCode()))
                .findFirst()
                .ifPresent(languageCodeComboBox::setSelectedItem);

        updateModelComboBox();
        updateFeatureSupport();

        setComboBoxSelectedItem(modelComboBox, settings.getModel());

        if (enableAutomaticPunctuationCheckBox.isEnabled()) {
            enableAutomaticPunctuationCheckBox.setSelected(settings.isEnableAutomaticPunctuation());
        }
        if (enableWordTimeOffsetsCheckBox.isEnabled()) {
            enableWordTimeOffsetsCheckBox.setSelected(settings.isEnableWordTimeOffsets());
        }
        if (enableSpeakerDiarizationCheckBox.isEnabled()) {
            enableSpeakerDiarizationCheckBox.setSelected(settings.isEnableSpeakerDiarization());
        }
        if (speechContextsTextArea.isEnabled()) {
            speechContextsTextArea.setText(settings.getSpeechContexts());
        }
        enableQuestionDetectionCheckBox.setSelected(settings.isEnableQuestionDetection());
        useEnhancedCheckBox.setSelected(settings.isUseEnhanced());
        profanityFilterCheckBox.setSelected(settings.isProfanityFilter());
        maxAlternativesSpinner.setValue(settings.getMaxAlternatives());
        enableSingleUtterance.setSelected(settings.isEnableSingleUtterance());
        enableInterimTranscription.setSelected(settings.isInterimTranscription());
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
        SttLanguageData.Language selectedLanguage = (SttLanguageData.Language) languageCodeComboBox.getSelectedItem();
        String langCode = (selectedLanguage != null) ? selectedLanguage.code() : "en-US";

        return RecognitionConfigSettings.builder()
                .languageCode(langCode)
                .model((String) Objects.requireNonNullElse(modelComboBox.getSelectedItem(), "default"))
                .enableAutomaticPunctuation(enableAutomaticPunctuationCheckBox.isSelected() && enableAutomaticPunctuationCheckBox.isEnabled())
                .enableWordTimeOffsets(enableWordTimeOffsetsCheckBox.isSelected() && enableWordTimeOffsetsCheckBox.isEnabled())
                .enableSpeakerDiarization(enableSpeakerDiarizationCheckBox.isSelected() && enableSpeakerDiarizationCheckBox.isEnabled())
                .speechContexts(speechContextsTextArea.isEnabled() ? speechContextsTextArea.getText() : "")
                .enableQuestionDetection(enableQuestionDetectionCheckBox.isSelected())
                .useEnhanced(useEnhancedCheckBox.isSelected())
                .profanityFilter(profanityFilterCheckBox.isSelected())
                .maxAlternatives((Integer) maxAlternativesSpinner.getValue())
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

    private JLabel createInfoIcon(String tooltip, Font font, Color color) {
        JLabel infoLabel = new JLabel("(?)");
        infoLabel.setFont(font);
        infoLabel.setForeground(color);
        infoLabel.setToolTipText(tooltip);
        infoLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return infoLabel;
    }
}