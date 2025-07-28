package com.quilot.ui.builders;

import lombok.Getter;

import javax.swing.*;
import java.awt.*;

/**
 * Builds the panel for Transcribed Audio display.
 */
@Getter
public class TranscribedAudioPanelBuilder implements ComponentPanelBuilder{

    private final JTextArea transcribedAudioArea;

    public TranscribedAudioPanelBuilder() {
        this.transcribedAudioArea = new JTextArea(10, 40);
        setupComponentProperties();
    }

    private void setupComponentProperties() {
        transcribedAudioArea.setEditable(false);
        transcribedAudioArea.setLineWrap(true);
        transcribedAudioArea.setWrapStyleWord(true);
    }

    @Override
    public JPanel build() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Transcribed Audio (Interviewer Voice):", SwingConstants.LEFT), BorderLayout.NORTH);
        panel.add(new JScrollPane(transcribedAudioArea), BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        return panel;
    }
}
