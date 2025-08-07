package com.quilot.ui.builders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link AIResponsePanelBuilder} class.
 */
class AIResponsePanelBuilderTest {

    private AIResponsePanelBuilder panelBuilder;

    @BeforeEach
    void setUp() {
        panelBuilder = new AIResponsePanelBuilder();
    }

    @Test
    @DisplayName("Should build a non-null JPanel")
    void build_ReturnsNonNullPanel() {
        JPanel panel = panelBuilder.build();
        assertNotNull(panel, "The built panel should not be null.");
    }

    @Test
    @DisplayName("Should contain a JTextPane that is not editable")
    void build_ContainsNonEditableTextPane() {
        JTextPane textPane = panelBuilder.getResponseTextPane();
        assertFalse(textPane.isEditable(), "The response text pane should not be editable.");
    }

    @Test
    @DisplayName("Panel should contain a JScrollPane which contains the JTextPane")
    void build_HasCorrectComponentHierarchy() {
        JPanel panel = panelBuilder.build();
        JTextPane textPane = panelBuilder.getResponseTextPane();

        Component[] components = panel.getComponents();
        JScrollPane scrollPane = null;
        for (Component comp : components) {
            if (comp instanceof JScrollPane) {
                scrollPane = (JScrollPane) comp;
                break;
            }
        }

        assertNotNull(scrollPane, "The panel should contain a JScrollPane.");
        assertEquals(textPane, scrollPane.getViewport().getView(), "The JScrollPane should contain the response text pane.");
    }

    @Test
    @DisplayName("JTextPane should have the specified custom background color")
    void createResponseTextPane_HasCustomBackgroundColor() {
        JTextPane textPane = panelBuilder.getResponseTextPane();
        assertEquals(new Color(245, 245, 245), textPane.getBackground(), "The background color should be the specified light gray.");
    }
}
