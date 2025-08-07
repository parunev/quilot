package com.quilot.ui.builders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link TranscribedAudioPanelBuilder} class.
 */
class TranscribedAudioPanelBuilderTest {

    private TranscribedAudioPanelBuilder panelBuilder;

    @BeforeEach
    void setUp() {
        panelBuilder = new TranscribedAudioPanelBuilder();
    }

    @Test
    @DisplayName("Should build a non-null JPanel")
    void build_ReturnsNonNullPanel() {
        JPanel panel = panelBuilder.build();
        assertNotNull(panel, "The built panel should not be null.");
    }

    @Test
    @DisplayName("JTextArea should be configured correctly")
    void createTranscribedAudioArea_IsConfiguredCorrectly() {
        JTextArea textArea = panelBuilder.getTranscribedAudioArea();
        assertFalse(textArea.isEditable(), "Transcribed audio area should not be editable.");
        assertTrue(textArea.getLineWrap(), "Line wrap should be enabled.");
        assertTrue(textArea.getWrapStyleWord(), "Word wrap style should be enabled.");
    }

    @Test
    @DisplayName("Panel should contain a JScrollPane with the JTextArea")
    void build_HasCorrectComponentHierarchy() {
        JPanel panel = panelBuilder.build();
        JTextArea textArea = panelBuilder.getTranscribedAudioArea();

        Component scrollPaneComponent = ((BorderLayout) panel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        assertNotNull(scrollPaneComponent, "The panel should contain a component in the center.");
        assertInstanceOf(JScrollPane.class, scrollPaneComponent, "The center component should be a JScrollPane.");

        JScrollPane scrollPane = (JScrollPane) scrollPaneComponent;
        assertEquals(textArea, scrollPane.getViewport().getView(), "The JScrollPane should contain the transcribed audio text area.");
    }
}
