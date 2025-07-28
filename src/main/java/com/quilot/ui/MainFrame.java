package com.quilot.ui;

import com.quilot.audio.ouput.AudioOutputService;
import com.quilot.audio.ouput.SystemAudioOutputService;
import com.quilot.utils.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainFrame extends JFrame {

    // UI COMPONENTS
    private final JButton startButton;
    private final JButton stopButton;
    private final JTextArea transcribedAudioArea;
    private final JTextArea aiResponseArea;
    private final JTextArea logArea;

    // AUDIO COMPONENTS
    private final JComboBox<String> outputDeviceComboBox;
    private final JSlider volumeSlider;
    private final JButton testVolumeButton;

    // MANAGERS
    private final InterviewTimerManager timerManager;
    private UIBuilder uiBuilder;
    private final AudioOutputService audioOutputService;

    public MainFrame() {

        // FRAME PROPERTIES
        setTitle("Quilot");
        setSize(800, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // MANAGERS
        timerManager = new InterviewTimerManager();
        audioOutputService = new SystemAudioOutputService();
        uiBuilder = new UIBuilder();

        // UI COMPONENTS
        startButton = uiBuilder.getStartButton();
        stopButton = uiBuilder.getStopButton();
        transcribedAudioArea = uiBuilder.getTranscribedAudioArea();
        aiResponseArea = uiBuilder.getAiResponseArea();
        logArea = uiBuilder.getLogArea();

        // UI AUDIO COMPONENTS
        outputDeviceComboBox = uiBuilder.getOutputDeviceComboBox();
        volumeSlider = uiBuilder.getVolumeSlider();
        testVolumeButton = uiBuilder.getTestVolumeButton();

        // LAYOUT
        JPanel mainPanel = new JPanel(new GridBagLayout());
        uiBuilder.setupLayout(mainPanel, timerManager, audioOutputService);
        add(mainPanel);

        addListeners();

        // CLOSE AUDIO ON EXIT
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                audioOutputService.close();
                Logger.info("Application closing. Audio resources released.");
            }
        });

        Logger.info("Quilot UI initialized.");
        appendToLogArea("UI initialized. Ready to start.");
    }


    private void addListeners() {
        startButton.addActionListener(_ -> {
            try {
                Logger.info("Start button clicked.");
                appendToLogArea("Starting interview simulation...");
                startButton.setEnabled(false);
                stopButton.setEnabled(true);

                timerManager.startInterviewTimer();

                transcribedAudioArea.setText("Simulating audio input: 'Hello, tell me about yourself.'");
                aiResponseArea.setText("AI: 'Hello! I'm ready to begin. Please answer the question.'");
            } catch (Exception ex) {
                Logger.error("Error starting interview: " + ex.getMessage(), ex);
                appendToLogArea("ERROR: Could not start interview. Check logs.");
            }
        });

        stopButton.addActionListener(_ -> {
            try {
                Logger.info("Stop button clicked.");
                appendToLogArea("Stopping interview simulation...");
                startButton.setEnabled(true);
                stopButton.setEnabled(false);

                timerManager.stopInterviewTimer();

                transcribedAudioArea.append("\nInterview ended.");
                aiResponseArea.append("\nAI: 'Thank you for your time.'");
            } catch (Exception ex) {
                Logger.error("Error stopping interview: " + ex.getMessage(), ex);
                appendToLogArea("ERROR: Could not stop interview. Check logs.");
            }
        });


        if (outputDeviceComboBox != null) {
            outputDeviceComboBox.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String selectedDevice = (String) e.getItem();
                    if (audioOutputService.selectOutputDevice(selectedDevice)) {
                        appendToLogArea("Selected audio output device: " + selectedDevice);

                        volumeSlider.setEnabled(true);
                        testVolumeButton.setEnabled(true);

                        audioOutputService.setVolume(volumeSlider.getValue() / 100.0f);
                    } else {
                        appendToLogArea("Failed to select audio output device: " + selectedDevice);
                        volumeSlider.setEnabled(false);
                        testVolumeButton.setEnabled(false);
                    }
                }
            });
        } else {
            Logger.error("Error: outputDeviceComboBox is null when attempting to add ItemListener.");
        }


        if (volumeSlider != null) {
            volumeSlider.addChangeListener(e -> {
                JSlider source = (JSlider) e.getSource();
                if (!source.getValueIsAdjusting()) {
                    float volume = source.getValue() / 100.0f;
                    audioOutputService.setVolume(volume);
                }
            });
        } else {
            Logger.error("Error: volumeSlider is null when attempting to add ChangeListener.");
        }

        if (testVolumeButton != null) {
            testVolumeButton.addActionListener(_ -> {
                audioOutputService.playTestSound();
                appendToLogArea("Test sound played.");
            });
        } else {
            Logger.error("Error: testVolumeButton is null when attempting to add ActionListener.");
        }
    }

    public void appendToLogArea(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}
