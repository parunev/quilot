package com.quilot.core;

import com.formdev.flatlaf.FlatLightLaf;
import com.quilot.ui.MainFrame;
import com.quilot.utils.Logger;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        FlatLightLaf.setup();

        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                Logger.error("Failed to initialize MainFrame: " + e.getMessage(), e);
                JOptionPane.showMessageDialog(null,
                        "An error occurred during application startup. Please check logs.",
                        "Application Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
