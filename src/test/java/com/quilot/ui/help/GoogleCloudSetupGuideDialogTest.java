package com.quilot.ui.help;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link GoogleCloudSetupGuideDialog} class.
 */
class GoogleCloudSetupGuideDialogTest {

    private GoogleCloudSetupGuideDialog dialog;

    @BeforeEach
    void setUp() {
        dialog = new GoogleCloudSetupGuideDialog(null);
    }

    @Test
    @DisplayName("Should initialize components without errors")
    void initComponents_CreatesComponentsSuccessfully() {
        assertNotNull(dialog, "The dialog should be successfully instantiated.");
    }

    @Test
    @DisplayName("JTextPane should be configured to be non-editable")
    void textPane_IsNonEditable() {
        JTextPane textPane = findTextPane(dialog);
        assertNotNull(textPane, "A JTextPane should exist in the dialog.");
        assertFalse(textPane.isEditable(), "The text pane for the guide should not be editable.");
    }

    @Test
    @DisplayName("JTextPane should have a hyperlink listener attached")
    void textPane_HasHyperlinkListener() {
        JTextPane textPane = findTextPane(dialog);
        assertNotNull(textPane, "A JTextPane should exist in the dialog.");
        assertTrue(textPane.getHyperlinkListeners().length > 0, "The text pane should have a hyperlink listener.");
    }

    /**
     * A helper method to find the JTextPane component within the dialog's hierarchy.
     * @param container The container to search within.
     * @return The found JTextPane, or null if it's not found.
     */
    private JTextPane findTextPane(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JTextPane) {
                return (JTextPane) comp;
            }

            if (comp instanceof JScrollPane) {
                Component view = ((JScrollPane) comp).getViewport().getView();
                if (view instanceof JTextPane) {
                    return (JTextPane) view;
                }
            }
            if (comp instanceof Container) {
                JTextPane found = findTextPane((Container) comp);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
