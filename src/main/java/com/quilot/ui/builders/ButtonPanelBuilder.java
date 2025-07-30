package com.quilot.ui.builders;

import lombok.Getter;

import javax.swing.*;
import java.awt.*;

/**
 * Builds the panel containing the main Start/Stop Interview buttons.
 */
@Getter
public class ButtonPanelBuilder implements ComponentPanelBuilder {

    private final JButton startButton;
    private final JButton stopButton;

    public ButtonPanelBuilder() {
        this.startButton = createButton("Start Interview", true);
        this.stopButton = createButton("Stop Interview", false);
    }

    private JButton createButton(String text, boolean enabled) {
        JButton button = new JButton(text);
        button.setEnabled(enabled);
        return button;
    }

    @Override
    public JPanel build() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        panel.add(startButton);
        panel.add(stopButton);
        return panel;
    }
}
