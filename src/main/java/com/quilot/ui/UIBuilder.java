package com.quilot.ui;

import javax.swing.*;
import java.awt.*;

public class UIBuilder {

    private JButton startButton;
    private JButton stopButton;
    private JTextArea transcribedAudioArea;
    private JTextArea aiResponseArea;
    private JTextArea logArea;

    /**
     * Initializes all the Swing UI components.
     */
    public UIBuilder() {
        initComponents();
    }

    /**
     * Initializes all the Swing UI components.
     */
    private void initComponents() {
        startButton = new JButton("Start Interview");
        stopButton = new JButton("Stop Interview");
        stopButton.setEnabled(false); // disable stop button initially

        transcribedAudioArea = new JTextArea(10, 40); // rows, columns
        transcribedAudioArea.setEditable(false); // user restricted
        transcribedAudioArea.setLineWrap(true);
        transcribedAudioArea.setWrapStyleWord(true);

        aiResponseArea = new JTextArea(10, 40);
        aiResponseArea.setEditable(false);
        aiResponseArea.setLineWrap(true);
        aiResponseArea.setWrapStyleWord(true);

        logArea = new JTextArea(8, 40);
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
    }

    /**
     * Sets up the layout of the UI components on the main panel.
     * @param mainPanel The JPanel to which components will be added.
     * @param timerManager The InterviewTimerManager instance to get time labels.
     */
    public void setupLayout(JPanel mainPanel, InterviewTimerManager timerManager) {
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // padding

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // padding between components
        gbc.fill = GridBagConstraints.BOTH;

        // --- Buttons Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5)); // centered buttons
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // two columns
        gbc.weightx = 1.0; // horizontal expansion
        mainPanel.add(buttonPanel, gbc);

        // Transcribed Audio Area
        JPanel transcribedPanel = new JPanel(new BorderLayout());
        transcribedPanel.add(new JLabel("Transcribed Audio (Input):", SwingConstants.LEFT), BorderLayout.NORTH);
        transcribedPanel.add(new JScrollPane(transcribedAudioArea), BorderLayout.CENTER);
        transcribedPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0)); // padding

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1; // columns
        gbc.weightx = 0.5; // width
        gbc.weighty = 0.4; // vertical space
        mainPanel.add(transcribedPanel, gbc);

        // AI Response Area
        JPanel aiResponsePanel = new JPanel(new BorderLayout());
        aiResponsePanel.add(new JLabel("AI Response (Output):", SwingConstants.LEFT), BorderLayout.NORTH);
        aiResponsePanel.add(new JScrollPane(aiResponseArea), BorderLayout.CENTER);
        aiResponsePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0)); // button padding

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.5; // width
        mainPanel.add(aiResponsePanel, gbc);

        // Log Area + Timers
        JPanel logPanel = new JPanel(new BorderLayout());
        JPanel logHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        logHeaderPanel.add(new JLabel("Application Logs:"));
        logHeaderPanel.add(timerManager.getCurrentTimeLabel()); // current time from manager
        logHeaderPanel.add(timerManager.getElapsedTimeLabel()); // elapsed time from manager

        logPanel.add(logHeaderPanel, BorderLayout.NORTH);
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        logPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0)); // padding

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2; // Span across two columns
        gbc.weightx = 1.0; // Full width
        gbc.weighty = 0.2; // Allocate vertical space
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
}
