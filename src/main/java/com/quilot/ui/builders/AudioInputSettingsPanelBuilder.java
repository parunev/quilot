package com.quilot.ui.builders;

import com.quilot.audio.input.AudioInputService;
import com.quilot.exceptions.audio.AudioDeviceException;
import com.quilot.utils.Logger;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.util.List;

@Getter
public class AudioInputSettingsPanelBuilder implements ComponentPanelBuilder {

    private final JComboBox<String> inputDeviceComboBox;
    private final JButton startInputRecordingButton;
    private final JButton stopInputRecordingButton;
    private final JButton playRecordedInputButton;
    private final JButton setupGuideButton;
    private final JButton credentialsButton;
    private final JButton googleCloudSetupGuideButton;
    private final JButton sttSettingsButton;
    private final JButton aiSettingsButton;

    private final AudioInputService audioInputService;

    public AudioInputSettingsPanelBuilder(AudioInputService audioInputService) {
        this.audioInputService = audioInputService;

        inputDeviceComboBox = new JComboBox<>();
        startInputRecordingButton = new JButton("Start Input Capture");
        stopInputRecordingButton = new JButton("Stop Input Capture");
        playRecordedInputButton = new JButton("Play Recorded Input");
        setupGuideButton = new JButton("Setup Guide (macOS)");
        credentialsButton = new JButton("STT Credentials");
        googleCloudSetupGuideButton = new JButton("Google Cloud Setup Guide");
        sttSettingsButton = new JButton("STT Settings");
        this.aiSettingsButton = new JButton("AI Settings");

        configureInitialButtonStates();
        populateInputDevices();
    }

    private void configureInitialButtonStates() {
        stopInputRecordingButton.setEnabled(false);
        playRecordedInputButton.setEnabled(false);
    }

    private void populateInputDevices() {
        List<String> devices = audioInputService.getAvailableInputDevices();
        inputDeviceComboBox.removeAllItems(); // Clear any existing items
        Logger.info("Available Input Devices detected by SystemAudioInputService: " + devices);

        if (devices.isEmpty()) {
            inputDeviceComboBox.addItem("No Devices Found");
            inputDeviceComboBox.setEnabled(false);
            startInputRecordingButton.setEnabled(false);
        } else {
            devices.forEach(inputDeviceComboBox::addItem);
            inputDeviceComboBox.setEnabled(true);
            startInputRecordingButton.setEnabled(true);

            String selectedDeviceName = audioInputService.getSelectedDeviceName();
            if (selectedDeviceName != null && devices.contains(selectedDeviceName)) {
                inputDeviceComboBox.setSelectedItem(selectedDeviceName);
            } else if (!devices.isEmpty()) {
                String defaultDevice = devices.getFirst();
                inputDeviceComboBox.setSelectedItem(defaultDevice);
                try {
                    audioInputService.selectInputDevice(defaultDevice);
                } catch (AudioDeviceException e) {
                    Logger.warn("Could not auto-select the default audio device '" + defaultDevice + "': " + e.getMessage());
                }
            }
        }
    }

    @Override
    public JPanel build() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Audio Input Settings (Interviewer Voice Transcription)"));
        GridBagConstraints gbc = createDefaultGbc();

        addInputDeviceRow(panel, gbc);
        addRecordingButtonsRow(panel, gbc);
        addHelpButtonsRow(panel, gbc);

        return panel;
    }

    private GridBagConstraints createDefaultGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        return gbc;
    }

    private void addInputDeviceRow(JPanel panel, GridBagConstraints gbc) {
        panel.add(new JLabel("Input Device:"), gbc);

        gbc.gridx = 1;
        panel.add(inputDeviceComboBox, gbc);
    }

    private void addRecordingButtonsRow(JPanel panel, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.add(startInputRecordingButton);
        buttonPanel.add(stopInputRecordingButton);
        buttonPanel.add(playRecordedInputButton);

        panel.add(buttonPanel, gbc);
    }

    private void addHelpButtonsRow(JPanel panel, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;

        JPanel helpPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        helpPanel.add(setupGuideButton);
        helpPanel.add(credentialsButton);
        helpPanel.add(googleCloudSetupGuideButton);
        helpPanel.add(sttSettingsButton);
        helpPanel.add(aiSettingsButton);

        panel.add(helpPanel, gbc);
    }
}