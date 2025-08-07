package com.quilot.ui.history;

import com.quilot.audio.ouput.AudioOutputService;
import com.quilot.db.dao.InterviewDao;
import com.quilot.db.model.Interview;
import com.quilot.db.model.TranscriptionEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link InterviewReviewDialog}.
 */
@ExtendWith(MockitoExtension.class)
class InterviewReviewDialogTest {

    @Mock
    private Interview mockInterview;
    @Mock
    private InterviewDao mockInterviewDao;
    @Mock
    private AudioOutputService mockAudioOutputService;

    private InterviewReviewDialog dialog;

    @BeforeEach
    void setUp() throws SQLException {
        when(mockInterview.getId()).thenReturn(1);
        when(mockInterview.getTitle()).thenReturn("Mock Interview Title");

        TranscriptionEntry entry1 = new TranscriptionEntry();
        entry1.setSpeaker("Interviewer");
        entry1.setContent("Hello there.");
        entry1.setTimestamp(LocalDateTime.of(2025, 8, 7, 10, 30, 0));

        TranscriptionEntry entry2 = new TranscriptionEntry();
        entry2.setSpeaker("AI");
        entry2.setContent("Hello! How can I help you?");
        entry2.setTimestamp(LocalDateTime.of(2025, 8, 7, 10, 30, 5));

        when(mockInterviewDao.getTranscriptionEntriesForInterview(1)).thenReturn(List.of(entry1, entry2));

        dialog = new InterviewReviewDialog(null, mockInterview, mockInterviewDao, mockAudioOutputService);
    }

    @Test
    @DisplayName("Should load and format transcription entries correctly on initialization")
    void loadTranscription_Success_PopulatesTextArea() {
        JTextArea transcriptArea = dialog.getTranscriptArea();

        String expectedText = "[10:30] Interviewer:\nHello there.\n\n[10:30:05] AI:\nHello! How can I help you?\n\n";
        assertEquals(expectedText, transcriptArea.getText());
    }

    @Test
    @DisplayName("Should display an error message if DAO fails to load transcriptions")
    void loadTranscription_DaoThrowsException_ShowsErrorMessage() throws SQLException {
        when(mockInterviewDao.getTranscriptionEntriesForInterview(1)).thenThrow(new SQLException("Test DB Error"));

        dialog = new InterviewReviewDialog(null, mockInterview, mockInterviewDao, mockAudioOutputService);

        JTextArea transcriptArea = dialog.getTranscriptArea();
        assertTrue(transcriptArea.getText().contains("Error: Could not load transcription."),
                "The text area should display an error message.");
    }
}
