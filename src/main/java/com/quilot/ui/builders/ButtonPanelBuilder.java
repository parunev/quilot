package com.quilot.ui.builders;

import lombok.Getter;

import javax.swing.*;
import java.awt.*;

/**
 * Builds the panel containing the main Start/Stop Interview buttons.
 */
@Getter
public class ButtonPanelBuilder implements ComponentPanelBuilder{

    private final JButton startButton;
    private final JButton stopButton;

    public ButtonPanelBuilder() {
        this.startButton = new JButton("Start Interview");
        this.stopButton = new JButton("Stop Interview");
        this.stopButton.setEnabled(false); // Disable stop button initially
    }

    @Override
    public JPanel build() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        panel.add(startButton);
        panel.add(stopButton);
        return panel;
    }
}
