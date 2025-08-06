package com.quilot.ui;

import javax.swing.*;
import java.awt.*;

/**
 * A simple status bar component for the bottom of the application window.
 * It displays a single line of text and ensures updates are thread-safe.
 */
public class StatusBar extends JPanel {

    /**
     * Defines the type of status message to display, which controls its color.
     */
    public enum StatusType {
        INFO,
        SUCCESS,
        ERROR
    }

    private final JLabel statusLabel;

    private static final Color COLOR_INFO = Color.WHITE;
    private static final Color COLOR_SUCCESS = new Color(130, 200, 130); // A pleasant green
    private static final Color COLOR_ERROR = new Color(255, 100, 100); // A clear red

    /**
     * Constructs a new StatusBar.
     */
    public StatusBar() {
        setLayout(new BorderLayout());
        setBackground(new Color(60, 63, 65));
        setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        statusLabel = new JLabel("Ready");
        statusLabel.setPreferredSize(new Dimension(100, 20));
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusLabel.setForeground(COLOR_INFO);

        add(statusLabel, BorderLayout.CENTER);
    }

    /**
     * Sets the text and color displayed in the status bar based on the message type.
     * This method is thread-safe and can be called from any thread.
     *
     * @param status The new status message to display.
     * @param type The type of the message (INFO, SUCCESS, ERROR).
     */
    public void setStatus(String status, StatusType type) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(status);
            switch (type) {
                case SUCCESS -> statusLabel.setForeground(COLOR_SUCCESS);
                case ERROR -> statusLabel.setForeground(COLOR_ERROR);
                default -> statusLabel.setForeground(COLOR_INFO);
            }
        });
    }
}