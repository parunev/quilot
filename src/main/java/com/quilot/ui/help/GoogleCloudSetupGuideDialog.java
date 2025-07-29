package com.quilot.ui.help;

import com.quilot.utils.Logger;

import javax.swing.*;
import java.awt.*;

/**
 * A JDialog that displays a setup guide for Google Cloud Speech-to-Text API.
 * This guides users through creating a project, enabling the API, and setting up credentials.
 */
public class GoogleCloudSetupGuideDialog extends JDialog {

    private static final String GUIDE_TEXT =
            "<html>" +
                    "<h2>Google Cloud Speech-to-Text API Setup Guide</h2>" +
                    "<p>To enable speech-to-text transcription for the interviewer's voice, " +
                    "you need to set up the Google Cloud Speech-to-Text API.</p>" +
                    "<p>Follow these steps:</p>" +
                    "<ol>" +
                    "<li><b>Create a Google Cloud Project:</b><br>" +
                    "    If you don't have one, create a new project in the Google Cloud Console:<br>" +
                    "    <a href=\"https://console.cloud.google.com/projectcreate\">https://console.cloud.google.com/projectcreate</a></li>" +
                    "<li><b>Enable the Speech-to-Text API:</b><br>" +
                    "    Navigate to the APIs & Services Dashboard and enable the 'Cloud Speech-to-Text API' for your project:<br>" +
                    "    <a href=\"https://console.cloud.google.com/apis/library/speech.googleapis.com\">https://console.cloud.google.com/apis/library/speech.googleapis.com</a></li>" +
                    "<li><b>Create a Service Account Key:</b><br>" +
                    "    This JSON key file will be used by the application to authenticate with Google Cloud.<br>" +
                    "    <ul>" +
                    "        <li>Go to `APIs & Services` &gt; `Credentials` in the Google Cloud Console.</li>" +
                    "        <li>Click `CREATE CREDENTIALS` &gt; `Service account`.</li>" +
                    "        <li>Follow the prompts to create a new service account. Grant it the `Cloud Speech-to-Text User` role.</li>" +
                    "        <li>In the final step, make sure to select `JSON` as the key type and click `CREATE`. This will download a JSON file to your computer.</li>" +
                    "        <li><b>Remember where you save this file!</b> You will need its full path in the application settings.</li>" +
                    "    </ul>" +
                    "</li>" +
                    "<li><b>Install Google Cloud Client Libraries:</b><br>" +
                    "    You need to add the Google Cloud Speech-to-Text client library as a dependency to your Java project.<br>" +
                    "    Add the following to your `pom.xml` (Maven) or `build.gradle` (Gradle) file and refresh your project dependencies:<br>" +
                    "    <pre style='background-color:#eee; padding: 5px;'><b>Maven (pom.xml):</b><br>" +
                    "&lt;dependencyManagement&gt;<br>" +
                    "    &lt;dependencies&gt;<br>" +
                    "        &lt;dependency&gt;<br>" +
                    "            &lt;groupId&gt;com.google.cloud&lt;/groupId&gt;<br>" +
                    "            &lt;artifactId&gt;google-cloud-bom&lt;/artifactId&gt;<br>" +
                    "            &lt;version&gt;0.207.0&lt;/version&gt; &lt;!-- Use a recent stable version of the BOM --&gt;<br>" +
                    "            &lt;type&gt;pom&lt;/type&gt;<br>" +
                    "            &lt;scope&gt;import&lt;/scope&gt;<br>" +
                    "        &lt;/dependency&gt;<br>" +
                    "    &lt;/dependencies&gt;<br>" +
                    "&lt;/dependencyManagement&gt;<br>" +
                    "<br>" +
                    "&lt;dependency&gt;<br>" +
                    "    &lt;groupId&gt;com.google.cloud&lt;/groupId&gt;<br>" +
                    "    &lt;artifactId&gt;google-cloud-speech&lt;/artifactId&gt;<br>" +
                    "&lt;/dependency&gt;<br>" +
                    "&lt;dependency&gt;<br>" +
                    "    &lt;groupId&gt;io.grpc&lt;/groupId&gt;<br>" +
                    "    &lt;artifactId&gt;grpc-netty-shaded&lt;/artifactId&gt;<br>" +
                    "    &lt;version&gt;1.60.0&lt;/version&gt; &lt;!-- Compatible with BOM 0.207.0 --&gt;<br>" +
                    "&lt;/dependency&gt;<br>" +
                    "<br><b>Gradle (build.gradle):</b><br>" +
                    "implementation platform('com.google.cloud:google-cloud-bom:0.207.0')<br>" +
                    "implementation 'com.google.cloud:google-cloud-speech'<br>" +
                    "implementation 'io.grpc:grpc-netty-shaded:1.60.0'</pre>" +
                    "</li>" +
                    "<li><b>Configure Credentials in Application:</b><br>" +
                    "    <ul>" +
                    "        <li>In the main application window, click the 'STT Credentials' button in the 'Audio Input Settings' panel.</li>" +
                    "        <li>In the 'Google Cloud STT Credentials' dialog, click 'Browse' and select the JSON key file you downloaded.</li>" +
                    "        <li>Click 'Save Credentials'.</li>" +
                    "        <li>(Optional) Click 'Test Credentials' to verify the setup.</li>" +
                    "    </ul>" +
                    "</li>" +
                    "</ol>" +
                    "<p>Once configured, the application will attempt to use this API for transcription.</p>" +
                    "</html>";

    public GoogleCloudSetupGuideDialog(Frame owner) {
        super(owner, "Google Cloud STT Setup Guide", true); // Modal dialog
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setSize(800, 650); // Adjusted size for more content
        setLocationRelativeTo(getOwner());

        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setText(GUIDE_TEXT);
        textPane.setEditable(false);
        textPane.setBackground(UIManager.getColor("Panel.background"));
        textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

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