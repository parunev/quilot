package com.quilot.ui.builders;

import lombok.Getter;

import javax.swing.*;
import java.awt.*;

/**
 * Builds the panel for AI Response display.
 */
@Getter
public class AIResponsePanelBuilder implements ComponentPanelBuilder {

    private final JTextArea responseTextArea;

    public AIResponsePanelBuilder() {
        this.responseTextArea = createResponseTextArea();
    }

    private JTextArea createResponseTextArea() {
        JTextArea textArea = new JTextArea(10, 40);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        return textArea;
    }

    @Override
    public JPanel build() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        JLabel titleLabel = new JLabel("AI Response (Your Answer):", SwingConstants.LEFT);
        JScrollPane scrollPane = new JScrollPane(responseTextArea);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }
}
