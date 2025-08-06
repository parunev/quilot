package com.quilot.ui;

import com.quilot.audio.input.AudioInputService;
import com.quilot.audio.ouput.AudioOutputService;
import com.quilot.ui.builders.*;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;


/**
 * A utility class responsible for initializing and laying out the Swing UI components.
 * <p>
 * This class encapsulates the UI construction logic, separating it from the event handling
 * and application logic in the {@link MainFrame}. It uses a composition of more specific
 * panel builders (e.g., {@link AudioInputSettingsPanelBuilder}) to construct the final UI.
 */
@Getter
public class UIBuilder {

    // Panel Builders
    private final AudioOutputSettingsPanelBuilder audioOutputSettingsPanelBuilder;
    private final AudioInputSettingsPanelBuilder audioInputSettingsPanelBuilder;
    private final TranscribedAudioPanelBuilder transcribedAudioPanelBuilder;
    private final AIResponsePanelBuilder aiResponsePanelBuilder;
    private final LogPanelBuilder logPanelBuilder;
    private final StatusBar statusBar;

    public UIBuilder(AudioOutputService audioOutputService,
                     AudioInputService audioInputService,
                     ElapsedTimerManager timerManager) {
        this.audioOutputSettingsPanelBuilder = new AudioOutputSettingsPanelBuilder(audioOutputService);
        this.audioInputSettingsPanelBuilder = new AudioInputSettingsPanelBuilder(audioInputService);
        this.transcribedAudioPanelBuilder = new TranscribedAudioPanelBuilder();
        this.aiResponsePanelBuilder = new AIResponsePanelBuilder();
        this.logPanelBuilder = new LogPanelBuilder(timerManager);
        this.statusBar = new StatusBar();
    }

    /**
     * Adds a component to the main panel with specified GridBagConstraints parameters.
     */
    private void addComponent(JPanel panel, Component comp, int x, int y,
                              int gridWidth, double weightX, double weightY) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = gridWidth;
        gbc.weightx = weightX;
        gbc.weighty = weightY;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(comp, gbc);
    }

    /**
     * Sets up the layout of the UI components on the main panel.
     * @param mainPanel The JPanel to which components will be added.
     */
    public void setupLayout(JPanel mainPanel) {
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding

        // Row 1: Audio Output and Input Settings Panels side-by-side
        addComponent(mainPanel, audioOutputSettingsPanelBuilder.build(), 0, 1, 1, 0.5, 0.1);
        addComponent(mainPanel, audioInputSettingsPanelBuilder.build(), 1, 1, 1, 0.5, 0.1);

        // Row 2: Transcribed Audio and AI Response Panels side-by-side
        addComponent(mainPanel, transcribedAudioPanelBuilder.build(), 0, 2, 1, 0.5, 0.4);
        addComponent(mainPanel, aiResponsePanelBuilder.build(), 1, 2, 1, 0.5, 0.4);

        // Row 3: Application Logs Panel (full width)
        addComponent(mainPanel, logPanelBuilder.build(), 0, 3, 2, 1.0, 0.2);

        // Row 4: Status Bar
        addComponent(mainPanel, statusBar, 0, 4, 2, 1.0, 0.0);
    }

    // Getters for Audio Output Settings Panel
    public JComboBox<String> getOutputDeviceComboBox() { return audioOutputSettingsPanelBuilder.getOutputDeviceComboBox(); }
    public JSlider getVolumeSlider() { return audioOutputSettingsPanelBuilder.getVolumeSlider(); }
    public JButton getTestVolumeButton() { return audioOutputSettingsPanelBuilder.getTestVolumeButton(); }

    // Getters for Audio Input Settings Panel
    public JComboBox<String> getInputDeviceComboBox() { return audioInputSettingsPanelBuilder.getInputDeviceComboBox(); }
    public JButton getStartInputRecordingButton() { return audioInputSettingsPanelBuilder.getStartInputRecordingButton(); }
    public JButton getStopInputRecordingButton() { return audioInputSettingsPanelBuilder.getStopInputRecordingButton(); }
    public JButton getPlayRecordedInputButton() { return audioInputSettingsPanelBuilder.getPlayRecordedInputButton(); }

    // Getters for Transcribed Audio and AI Response Panels
    public JTextArea getTranscribedAudioArea() { return transcribedAudioPanelBuilder.getTranscribedAudioArea(); }
    public JTextArea getAiResponseArea() { return aiResponsePanelBuilder.getResponseTextArea(); }

    // Getter for Log Panel
    public JTextArea getLogArea() { return logPanelBuilder.getLogArea(); }
}
