package com.quilot.ui.builders;

import lombok.Getter;

import javax.swing.*;
import java.awt.*;

/**
 * Builds the panel for AI Response display.
 */
@Getter
public class AIResponsePanelBuilder implements ComponentPanelBuilder{

    private final JTextArea aiResponseArea;

    public AIResponsePanelBuilder() {
        this.aiResponseArea = new JTextArea(10, 40);
        setupComponentProperties();
    }

    private void setupComponentProperties() {
        aiResponseArea.setEditable(false);
        aiResponseArea.setLineWrap(true);
        aiResponseArea.setWrapStyleWord(true);
    }

    @Override
    public JPanel build() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("AI Response (Your Answer):", SwingConstants.LEFT), BorderLayout.NORTH);
        panel.add(new JScrollPane(aiResponseArea), BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        return panel;
    }
}
