package com.quilot.ui.builders;

import lombok.Getter;

import javax.swing.*;
import java.awt.*;

/**
 * Builds the panel for AI Response display.
 */
@Getter
public class AIResponsePanelBuilder implements ComponentPanelBuilder {

    private final JTextPane responseTextPane;

    public AIResponsePanelBuilder() {
        this.responseTextPane = createResponseTextPane();
    }

    private JTextPane createResponseTextPane() {
        JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        // Set a slightly different background to distinguish it further
        textPane.setBackground(new Color(245, 245, 245));
        textPane.setFont(new Font("SansSerif", Font.PLAIN, 14));
        return textPane;
    }

    @Override
    public JPanel build() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        JLabel titleLabel = new JLabel("AI Response (Your Answer):", SwingConstants.LEFT);
        JScrollPane scrollPane = new JScrollPane(responseTextPane);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }
}
