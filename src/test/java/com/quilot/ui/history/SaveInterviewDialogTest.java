package com.quilot.ui.history;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the {@link SaveInterviewDialog}.
 */
class SaveInterviewDialogTest {

    @Test
    @DisplayName("Should return new title when user saves with valid input")
    void getInterviewTitle_WhenUserSavesNewTitle_ReturnsNewTitle() {
        String defaultTitle = "Interview - 2025-08-07";
        SaveInterviewDialog dialog = new SaveInterviewDialog(null, defaultTitle);

        dialog.getTitleField().setText("Interview with Google");
        dialog.getSaveButton().doClick();

        assertEquals("Interview with Google", dialog.getInterviewTitle());
    }

    @Test
    @DisplayName("Should return default title when user saves with empty input")
    void getInterviewTitle_WhenUserSavesEmptyTitle_ReturnsDefaultTitle() {
        String defaultTitle = "Interview - 2025-08-07";
        SaveInterviewDialog dialog = new SaveInterviewDialog(null, defaultTitle);

        dialog.getTitleField().setText("   ");
        dialog.getSaveButton().doClick();

        assertEquals(defaultTitle, dialog.getInterviewTitle());
    }

    @Test
    @DisplayName("Should return default title when user cancels")
    void getInterviewTitle_WhenUserCancels_ReturnsDefaultTitle() {
        String defaultTitle = "Interview - 2025-08-07";
        SaveInterviewDialog dialog = new SaveInterviewDialog(null, defaultTitle);

        dialog.getTitleField().setText("A different title");

        assertEquals(defaultTitle, dialog.getInterviewTitle());
    }
}
