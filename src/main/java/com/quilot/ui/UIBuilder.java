package com.quilot.ui;

import com.quilot.audio.ouput.AudioOutputService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class UIBuilder {

    private final JButton startButton = new JButton("Start Interview");
    private final JButton stopButton = new JButton("Stop Interview");
    private final JTextArea transcribedAudioArea = new JTextArea(10, 40);
    private final JTextArea aiResponseArea = new JTextArea(10, 40);
    private final JTextArea logArea = new JTextArea(8, 40);

    // audio output components
    private final JComboBox<String> outputDeviceComboBox = new JComboBox<>();
    private final JSlider volumeSlider = new JSlider(0, 100, 70);
    private final JButton testVolumeButton = new JButton("Test Volume");

    public UIBuilder() {
        setupComponentProperties();
    }

    private void setupComponentProperties() {
        stopButton.setEnabled(false);

        transcribedAudioArea.setEditable(false);
        transcribedAudioArea.setLineWrap(true);
        transcribedAudioArea.setWrapStyleWord(true);

        aiResponseArea.setEditable(false);
        aiResponseArea.setLineWrap(true);
        aiResponseArea.setWrapStyleWord(true);

        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setMinorTickSpacing(5);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
    }

    public void setupLayout(JPanel mainPanel, InterviewTimerManager timerManager, AudioOutputService audioOutputService) {
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;

        // BUTTONS
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        mainPanel.add(buttonPanel, gbc);

        // AUDIO OUTPUT SETTINGS
        JPanel audioSettingsPanel = new JPanel(new GridBagLayout());
        audioSettingsPanel.setBorder(BorderFactory.createTitledBorder("Audio Output Settings"));
        GridBagConstraints audioGbc = new GridBagConstraints();
        audioGbc.insets = new Insets(2, 5, 2, 5);
        audioGbc.fill = GridBagConstraints.HORIZONTAL;

        audioGbc.gridx = 0;
        audioGbc.gridy = 0;
        audioGbc.weightx = 0;
        audioSettingsPanel.add(new JLabel("Output Device:"), audioGbc);

        audioGbc.gridx = 1;
        audioGbc.gridy = 0;
        audioGbc.weightx = 1.0;

        List<String> devices = audioOutputService.getAvailableOutputDevices();
        for (String device : devices) {
            outputDeviceComboBox.addItem(device);
        }
        if (!devices.isEmpty()) {
            outputDeviceComboBox.setSelectedIndex(0);
            audioOutputService.selectOutputDevice(devices.getFirst());
        } else {
            outputDeviceComboBox.addItem("No Devices Found");
            outputDeviceComboBox.setEnabled(false);
            volumeSlider.setEnabled(false);
            testVolumeButton.setEnabled(false);
        }
        audioSettingsPanel.add(outputDeviceComboBox, audioGbc);

        audioGbc.gridx = 0;
        audioGbc.gridy = 1;
        audioGbc.weightx = 0;
        audioSettingsPanel.add(new JLabel("Volume:"), audioGbc);

        audioGbc.gridx = 1;
        audioGbc.gridy = 1;
        audioGbc.weightx = 1.0;
        audioSettingsPanel.add(volumeSlider, audioGbc);

        audioGbc.gridx = 0;
        audioGbc.gridy = 2;
        audioGbc.gridwidth = 2;
        audioGbc.anchor = GridBagConstraints.CENTER;
        audioGbc.fill = GridBagConstraints.NONE;
        audioSettingsPanel.add(testVolumeButton, audioGbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.1;
        mainPanel.add(audioSettingsPanel, gbc);


        // TRANSCRIBED AUDIO AREA
        JPanel transcribedPanel = new JPanel(new BorderLayout());
        transcribedPanel.add(new JLabel("Transcribed Audio (Input):", SwingConstants.LEFT), BorderLayout.NORTH);
        transcribedPanel.add(new JScrollPane(transcribedAudioArea), BorderLayout.CENTER);
        transcribedPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        gbc.weighty = 0.4;
        mainPanel.add(transcribedPanel, gbc);

        // AI RESPONSE AREA
        JPanel aiResponsePanel = new JPanel(new BorderLayout());
        aiResponsePanel.add(new JLabel("AI Response (Output):", SwingConstants.LEFT), BorderLayout.NORTH);
        aiResponsePanel.add(new JScrollPane(aiResponseArea), BorderLayout.CENTER);
        aiResponsePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 0.5;
        mainPanel.add(aiResponsePanel, gbc);

        // LOG AREA + TIMERS
        JPanel logPanel = new JPanel(new BorderLayout());
        JPanel logHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        logHeaderPanel.add(new JLabel("Application Logs"));
        logHeaderPanel.add(timerManager.getCurrentTimeLabel());
        logHeaderPanel.add(timerManager.getElapsedTimeLabel());

        logPanel.add(logHeaderPanel, BorderLayout.NORTH);
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        logPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.2;
        mainPanel.add(logPanel, gbc);
    }

    public JButton getStartButton() {
        return startButton;
    }

    public JButton getStopButton() {
        return stopButton;
    }

    public JTextArea getTranscribedAudioArea() {
        return transcribedAudioArea;
    }

    public JTextArea getAiResponseArea() {
        return aiResponseArea;
    }

    public JTextArea getLogArea() {
        return logArea;
    }

    public JComboBox<String> getOutputDeviceComboBox() {
        return outputDeviceComboBox;
    }

    public JSlider getVolumeSlider() {
        return volumeSlider;
    }

    public JButton getTestVolumeButton() {
        return testVolumeButton;
    }
}
