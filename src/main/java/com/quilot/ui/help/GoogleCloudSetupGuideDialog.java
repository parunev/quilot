package com.quilot.ui.help;

import com.quilot.utils.Logger;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

/**
 * A JDialog that displays a setup guide for Google Cloud Speech-to-Text API.
 * This guides users through creating a project, enabling the API, and setting up credentials.
 */
public class GoogleCloudSetupGuideDialog extends JDialog {

    private static final String RESOURCE_PATH = "/help/google_cloud_stt_setup_guide.html";

    public GoogleCloudSetupGuideDialog(Frame owner) {
        super(owner, "Google Cloud STT Setup Guide", true);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setSize(800, 650);
        setMinimumSize(new Dimension(600, 400));
        setResizable(true);
        setLocationRelativeTo(getOwner());

        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setText(loadHtmlGuide());
        textPane.setEditable(false);
        textPane.setBackground(UIManager.getColor("Panel.background"));
        textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        textPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (IOException | URISyntaxException | UnsupportedOperationException ex) {
                    Logger.error("Error opening URL: " + ex.getMessage());
                    JOptionPane.showMessageDialog(this,
                            "Could not open link:\n" + e.getURL() + "\n\nReason: " + ex.getMessage(),
                            "Link Error", JOptionPane.ERROR_MESSAGE);
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

    private String loadHtmlGuide() {
        try (InputStream is = getClass().getResourceAsStream(GoogleCloudSetupGuideDialog.RESOURCE_PATH)) {
            if (is == null) {
                Logger.error("Could not find google_cloud_stt_setup_guide.html resource");
                return "<html><body><p>Setup guide not found.</p></body></html>";
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Logger.error("Failed to load setup guide HTML: " + e.getMessage());
            return "<html><body><p>Error loading setup guide.</p></body></html>";
        }
    }
}