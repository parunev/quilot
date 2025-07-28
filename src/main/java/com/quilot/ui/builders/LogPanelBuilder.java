package com.quilot.ui.builders;

import com.quilot.ui.InterviewTimerManager;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;

/**
 * Builds the panel for Application Logs and Timers.
 */
@Getter
public class LogPanelBuilder implements ComponentPanelBuilder{

    private final JTextArea logArea;
    private final InterviewTimerManager timerManager;

    public LogPanelBuilder(InterviewTimerManager timerManager) {
        this.timerManager = timerManager;
        this.logArea = new JTextArea(8, 40);
        setupComponentProperties();
    }

    private void setupComponentProperties() {
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
    }

    @Override
    public JPanel build() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel logHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        logHeaderPanel.add(new JLabel("Application Logs:"));
        logHeaderPanel.add(timerManager.getCurrentTimeLabel());
        logHeaderPanel.add(timerManager.getElapsedTimeLabel());

        panel.add(logHeaderPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        return panel;
    }
}
