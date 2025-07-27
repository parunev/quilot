package com.quilot.ui;

import com.quilot.utils.Logger;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    // UI Components
    private final JButton startButton;
    private final JButton stopButton;
    private final JTextArea transcribedAudioArea;
    private final JTextArea aiResponseArea;
    private final JTextArea logArea;

    // Managers for specific functionalities
    private InterviewTimerManager timerManager;
    private UIBuilder uiBuilder;

    public MainFrame() {
        // Set up the basic frame properties
        setTitle("Quilot");
        setSize(800, 600); // Initial size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window on the screen

        // Initialize managers
        timerManager = new InterviewTimerManager();
        uiBuilder = new UIBuilder();

        // Get components from UIBuilder
        startButton = uiBuilder.getStartButton();
        stopButton = uiBuilder.getStopButton();
        transcribedAudioArea = uiBuilder.getTranscribedAudioArea();
        aiResponseArea = uiBuilder.getAiResponseArea();
        logArea = uiBuilder.getLogArea();

        // Set up the layout using UIBuilder
        JPanel mainPanel = new JPanel(new GridBagLayout());
        uiBuilder.setupLayout(mainPanel, timerManager); // Pass timerManager to UIBuilder for labels
        add(mainPanel); // Add the main panel to the JFrame

        // Add action listeners to buttons
        addListeners();

        // Log the application start
        Logger.info("AI Interview Copilot UI initialized.");
        appendToLogArea("UI initialized. Ready to start.");
    }

    /**
     * Adds action listeners to the buttons.
     * This is where you'd hook up your core logic later.
     */
    private void addListeners() {
        startButton.addActionListener(_ -> {
            try {
                Logger.info("Start button clicked.");
                appendToLogArea("Starting interview simulation...");
                startButton.setEnabled(false);
                stopButton.setEnabled(true);

                // Start the interview timer via the manager
                timerManager.startInterviewTimer();

                // Placeholder for actual start logic
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

                // Stop the interview timer via the manager
                timerManager.stopInterviewTimer();

                // Placeholder for actual stop logic
                transcribedAudioArea.append("\nInterview ended.");
                aiResponseArea.append("\nAI: 'Thank you for your time.'");
            } catch (Exception ex) {
                Logger.error("Error stopping interview: " + ex.getMessage(), ex);
                appendToLogArea("ERROR: Could not stop interview. Check logs.");
            }
        });
    }

    public void appendToLogArea(String message) {
        // Ensure UI updates are done on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            // Auto-scroll to the bottom
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}
