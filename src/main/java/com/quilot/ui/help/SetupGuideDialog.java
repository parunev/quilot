package com.quilot.ui.help;

import com.quilot.utils.Logger;

import javax.swing.*;
import java.awt.*;

/**
 *
 * A JDialog that displays a setup guide for Blackhole on macOS,
 * helping users configure their system for loopback audio recording.
 */
public class SetupGuideDialog extends JDialog {

    private static final String GUIDE_TEXT =
            "<html>" +
                    "<h2>macOS: Setting up Blackhole for System Audio Recording</h2>" +
                    "<p>To transcribe the interviewer's voice (audio output from your computer), " +
                    "you need to capture the system's playback audio. On macOS, this typically requires " +
                    "a virtual audio device like <b>Blackhole</b>.</p>" +
                    "<p>Follow these steps to configure Blackhole:</p>" +
                    "<ol>" +
                    "<li><b>Install Blackhole:</b><br>" +
                    "    If you haven't already, download and install Blackhole 2ch from GitHub: " +
                    "    <a href=\"https://github.com/ExistentialAudio/BlackHole/releases\">https://github.com/ExistentialAudio/BlackHole/releases</a><br>" +
                    "    (Choose the `BlackHole 2ch.pkg` installer.)</li>" +
                    "<li><b>Open Audio MIDI Setup:</b><br>" +
                    "    Go to `Applications` &gt; `Utilities` &gt; `Audio MIDI Setup`.</li>" +
                    "<li><b>Create a Multi-Output Device:</b><br>" +
                    "    This allows audio to go to both Blackhole (for recording) and your speakers/headphones (for listening).<br>" +
                    "    <ul>" +
                    "        <li>In the bottom-left corner, click the `+` button.</li>" +
                    "        <li>Select `Create Multi-Output Device`.</li>" +
                    "        <li>In the right-hand panel (for your new Multi-Output Device):</li>" +
                    "        <ul>" +
                    "            <li>Check the `Use` checkbox next to `Blackhole 2ch`.</li>" +
                    "            <li>Check the `Use` checkbox next to your actual <b>built-in output</b> or your <b>headphones/speakers</b> (e.g., 'MacBook Pro Speakers', 'External Headphones').</li>" +
                    "            <li><b>Crucially:</b> Select your <b>built-in output</b> or your <b>headphones/speakers</b> as the `Master Device`. This ensures your system volume controls work correctly.</li>" +
                    "            <li>(Optional) Double-click the new device name and rename it (e.g., 'System Audio + Blackhole') for clarity.</li>" +
                    "        </ul>" +
                    "    </ul>" +
                    "</li>" +
                    "<li><b>Set System Output to Your New Multi-Output Device:</b><br>" +
                    "    <ul>" +
                    "        <li>Go to `System Settings` (or `System Preferences` on older macOS) &gt; `Sound` &gt; `Output`.</li>" +
                    "        <li>Select your newly created `Multi-Output Device` (e.g., 'System Audio + Blackhole') as your default sound output.</li>" +
                    "        <li>Now, any sound your Mac plays (including this application's interviewer voice) will go to both your listening device AND Blackhole.</li>" +
                    "    </ul>" +
                    "</li>" +
                    "<li><b>Configure This Application's Input:</b><br>" +
                    "    <ul>" +
                    "        <li>Restart this application if it was running during the setup.</li>" +
                    "        <li>In the 'Audio Input Settings' panel, open the 'Input Device' dropdown.</li>" +
                    "        <li>You should now see `Blackhole 2ch` listed as an available input device. Select it.</li>" +
                    "    </ul>" +
                    "</li>" +
                    "<li><b>Test Recording:</b><br>" +
                    "    Click 'Start Input Capture' in this application, play some system audio (e.g., the 'Test Sound' from the Output Settings), then 'Stop Input Capture', and finally 'Play Recorded Input' to verify.</li>" +
                    "</ol>" +
                    "<p>If you encounter issues, ensure Blackhole is installed, and check your macOS Sound settings for disabled or hidden devices. Feel free to contact me at parunev@gmail.com</p>" +
                    "</html>";

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
        textPane.setText(GUIDE_TEXT);
        textPane.setEditable(false);
        textPane.setBackground(UIManager.getColor("Panel.background"));
        textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        // Make links clickable
        textPane.addHyperlinkListener(e -> {
            if (e.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (Exception ex) {
                    Logger.error("Error opening URL: " + ex.getMessage());
                    JOptionPane.showMessageDialog(this, "Could not open link: " + e.getURL(), "Link Error", JOptionPane.ERROR_MESSAGE);
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
}
