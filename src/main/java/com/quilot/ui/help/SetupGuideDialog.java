package com.quilot.ui.help;

import com.quilot.utils.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 *
 * A JDialog that displays a setup guide for Blackhole on macOS,
 * helping users configure their system for loopback audio recording.
 */
public class SetupGuideDialog extends JDialog {

    private static final String RESOURCE_PATH = "/help/blackhole_setup_guide.html";

    public SetupGuideDialog(Frame owner) {
        super(owner, "Blackhole Setup Guide for macOS", true);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setSize(700, 600);
        setLocationRelativeTo(getOwner());

        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.setBackground(UIManager.getColor("Panel.background"));
        textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        String htmlContent = loadHtmlFromResource();
        textPane.setText(Objects.requireNonNullElse(htmlContent, "<html><body><h3>Error loading help content.</h3></body></html>"));

        textPane.addHyperlinkListener(e -> {
            if (e.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (IOException | URISyntaxException | UnsupportedOperationException ex) {
                    Logger.error("Error opening URL: " + ex.getMessage());
                    JOptionPane.showMessageDialog(this,
                            "Could not open link: " + e.getURL(),
                            "Link Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(_ -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private String loadHtmlFromResource() {
        try (InputStream in = getClass().getResourceAsStream(SetupGuideDialog.RESOURCE_PATH)) {
            if (in == null) {
                Logger.error("Resource not found: " + SetupGuideDialog.RESOURCE_PATH);
                return null;
            }

            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Logger.error("Failed to load resource " + SetupGuideDialog.RESOURCE_PATH, e);
            return null;
        }
    }
}
