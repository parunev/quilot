package com.quilot.ui;

import com.quilot.audio.input.AudioInputService;
import com.quilot.audio.ouput.AudioOutputService;
import com.quilot.ui.builders.*;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;


/**
 * A utility class responsible for initializing and laying out the Swing UI components.
 * This helps in separating UI construction logic from the MainFrame's event handling.
 * It uses the Builder pattern to construct different panels of the UI.
 */
@Getter
public class UIBuilder {

    // Panel Builders
    private final ButtonPanelBuilder buttonPanelBuilder;
    private final AudioOutputSettingsPanelBuilder audioOutputSettingsPanelBuilder;
    private final AudioInputSettingsPanelBuilder audioInputSettingsPanelBuilder;
    private final TranscribedAudioPanelBuilder transcribedAudioPanelBuilder;
    private final AIResponsePanelBuilder aiResponsePanelBuilder;
    private final LogPanelBuilder logPanelBuilder;

    /**
     * Initializes all the specific panel builders.
     * @param audioOutputService The service for audio output.
     * @param audioInputService The service for audio input.
     * @param timerManager The manager for interview timers.
     */
    public UIBuilder(AudioOutputService audioOutputService, AudioInputService audioInputService, InterviewTimerManager timerManager) {
        this.buttonPanelBuilder = new ButtonPanelBuilder();
        this.audioOutputSettingsPanelBuilder = new AudioOutputSettingsPanelBuilder(audioOutputService);
        this.audioInputSettingsPanelBuilder = new AudioInputSettingsPanelBuilder(audioInputService);
        this.transcribedAudioPanelBuilder = new TranscribedAudioPanelBuilder();
        this.aiResponsePanelBuilder = new AIResponsePanelBuilder();
        this.logPanelBuilder = new LogPanelBuilder(timerManager);
    }

    /**
     * Sets up the layout of the UI components on the main panel.
     * This method orchestrates the building of individual panels using their respective builders.
     * @param mainPanel The JPanel to which components will be added.
     */
    public void setupLayout(JPanel mainPanel) {
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add some padding

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Padding between components
        gbc.fill = GridBagConstraints.BOTH; // Components fill their display area

        // Row 0: Main Interview Control Buttons
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 1.0;
        mainPanel.add(buttonPanelBuilder.build(), gbc);

        // Row 1: Audio Output and Input Settings Panels (side-by-side)
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.5; gbc.weighty = 0.1;
        mainPanel.add(audioOutputSettingsPanelBuilder.build(), gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.5; gbc.weighty = 0.1;
        mainPanel.add(audioInputSettingsPanelBuilder.build(), gbc);

        // Row 2: Transcribed Audio and AI Response Panels (side-by-side)
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.weightx = 0.5; gbc.weighty = 0.4;
        mainPanel.add(transcribedAudioPanelBuilder.build(), gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 1; gbc.weightx = 0.5; gbc.weighty = 0.4;
        mainPanel.add(aiResponsePanelBuilder.build(), gbc);

        // Row 3: Application Logs Panel
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.weighty = 0.2;
        mainPanel.add(logPanelBuilder.build(), gbc);
    }

    // Getters for all individual UI components, delegated from their respective builders
    public JButton getStartButton() { return buttonPanelBuilder.getStartButton(); }
    public JButton getStopButton() { return buttonPanelBuilder.getStopButton(); }
    public JTextArea getTranscribedAudioArea() { return transcribedAudioPanelBuilder.getTranscribedAudioArea(); }
    public JTextArea getAiResponseArea() { return aiResponsePanelBuilder.getAiResponseArea(); }
    public JTextArea getLogArea() { return logPanelBuilder.getLogArea(); }
    public JComboBox<String> getOutputDeviceComboBox() { return audioOutputSettingsPanelBuilder.getOutputDeviceComboBox(); }
    public JSlider getVolumeSlider() { return audioOutputSettingsPanelBuilder.getVolumeSlider(); }
    public JButton getTestVolumeButton() { return audioOutputSettingsPanelBuilder.getTestVolumeButton(); }
    public JComboBox<String> getInputDeviceComboBox() { return audioInputSettingsPanelBuilder.getInputDeviceComboBox(); }
    public JButton getStartInputRecordingButton() { return audioInputSettingsPanelBuilder.getStartInputRecordingButton(); }
    public JButton getStopInputRecordingButton() { return audioInputSettingsPanelBuilder.getStopInputRecordingButton(); }
    public JButton getPlayRecordedInputButton() { return audioInputSettingsPanelBuilder.getPlayRecordedInputButton(); }
    public JButton getSetupGuideButton() { return audioInputSettingsPanelBuilder.getSetupGuideButton(); }
    public JButton getCredentialsButton() { return audioInputSettingsPanelBuilder.getCredentialsButton(); }
    public JButton getGoogleCloudSetupGuideButton() { return audioInputSettingsPanelBuilder.getGoogleCloudSetupGuideButton(); }
    public JButton getSttSettingsButton() { return audioInputSettingsPanelBuilder.getSttSettingsButton(); }
}
