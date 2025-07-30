package com.quilot.ui.builders;

import com.quilot.ui.InterviewTimerManager;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;

/**
 * Builds the panel for Application Logs and Timers.
 */
@Getter
public class LogPanelBuilder implements ComponentPanelBuilder {

    private final JTextArea logArea;
    private final InterviewTimerManager timerManager;

    public LogPanelBuilder(InterviewTimerManager timerManager) {
        this.timerManager = timerManager;
        this.logArea = createLogArea();
    }

    private JTextArea createLogArea() {
        JTextArea area = new JTextArea(8, 40);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    private JPanel buildHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        headerPanel.add(new JLabel("Application Logs:"));
        headerPanel.add(timerManager.getCurrentTimeLabel());
        headerPanel.add(timerManager.getElapsedTimeLabel());
        return headerPanel;
    }

    @Override
    public JPanel build() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buildHeaderPanel(), BorderLayout.NORTH);
        panel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        return panel;
    }
}
