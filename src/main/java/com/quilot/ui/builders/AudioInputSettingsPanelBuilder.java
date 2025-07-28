package com.quilot.ui.builders;

import com.quilot.audio.input.AudioInputService;
import com.quilot.utils.Logger;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Builds the panel for Audio Input Settings.
 */
@Getter
public class AudioInputSettingsPanelBuilder implements ComponentPanelBuilder{

    private final JComboBox<String> inputDeviceComboBox;
    private final JButton startInputRecordingButton;
    private final JButton stopInputRecordingButton;
    private final JButton playRecordedInputButton;

    private final AudioInputService audioInputService;

    public AudioInputSettingsPanelBuilder(AudioInputService audioInputService) {
        this.audioInputService = audioInputService;
        this.inputDeviceComboBox = new JComboBox<>();
        this.startInputRecordingButton = new JButton("Start Input Capture");
        this.stopInputRecordingButton = new JButton("Stop Input Capture");
        this.playRecordedInputButton = new JButton("Play Recorded Input");

        setupComponentProperties();
    }

    private void setupComponentProperties() {
        stopInputRecordingButton.setEnabled(false); // Disable stop input button initially
        playRecordedInputButton.setEnabled(false); // Disable play recorded input button initially
    }

    @Override
    public JPanel build() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Audio Input Settings (Interviewer Voice Transcription)"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;

        panel.add(new JLabel("Input Device:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;

        List<String> inputDevices = audioInputService.getAvailableInputDevices();
        Logger.info("Available Input Devices detected by SystemAudioInputService: " + inputDevices);

        for (String device : inputDevices) {
            inputDeviceComboBox.addItem(device);
        }

        if (!inputDevices.isEmpty()) {
            inputDeviceComboBox.setSelectedIndex(0);
            audioInputService.selectInputDevice(inputDevices.getFirst());
        } else {
            inputDeviceComboBox.addItem("No Devices Found");
            inputDeviceComboBox.setEnabled(false);
            startInputRecordingButton.setEnabled(false);
            stopInputRecordingButton.setEnabled(false);
            playRecordedInputButton.setEnabled(false);
        }

        panel.add(inputDeviceComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        JPanel inputButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        inputButtonsPanel.add(startInputRecordingButton);
        inputButtonsPanel.add(stopInputRecordingButton);
        inputButtonsPanel.add(playRecordedInputButton);

        panel.add(inputButtonsPanel, gbc);
        return panel;
    }
}
