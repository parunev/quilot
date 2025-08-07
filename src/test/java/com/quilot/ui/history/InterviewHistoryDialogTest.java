package com.quilot.ui.history;

import com.quilot.audio.ouput.AudioOutputService;
import com.quilot.db.dao.InterviewDao;
import com.quilot.db.model.Interview;
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
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link InterviewHistoryDialog}.
 */
@ExtendWith(MockitoExtension.class)
class InterviewHistoryDialogTest {

    @Mock
    private InterviewDao mockInterviewDao;
    @Mock
    private AudioOutputService mockAudioOutputService;

    private InterviewHistoryDialog dialog;

    @Test
    @DisplayName("Should load and display interviews when DAO returns data")
    void loadInterviews_WithData_PopulatesList() throws SQLException {
        // Arrange
        Interview interview1 = new Interview();
        interview1.setId(1);
        interview1.setTitle("Test Interview 1");
        interview1.setInterviewDate(LocalDateTime.now());

        Interview interview2 = new Interview();
        interview2.setId(2);
        interview2.setTitle("Test Interview 2");
        interview2.setInterviewDate(LocalDateTime.now());

        when(mockInterviewDao.getAllInterviews()).thenReturn(List.of(interview1, interview2));

        // Act
        dialog = new InterviewHistoryDialog(null, mockInterviewDao, mockAudioOutputService);
        JList<Interview> list = dialog.getInterviewList();

        // Assert
        assertEquals(2, list.getModel().getSize(), "The list should contain two interviews.");
    }

    @Test
    @DisplayName("Should show an empty list when DAO throws an exception")
    void loadInterviews_WhenDaoFails_ShowsEmptyList() throws SQLException {
        when(mockInterviewDao.getAllInterviews()).thenThrow(new SQLException("Database connection failed"));

        dialog = new InterviewHistoryDialog(null, mockInterviewDao, mockAudioOutputService);
        JList<Interview> list = dialog.getInterviewList();

        assertEquals(0, list.getModel().getSize(), "The list should be empty when the DAO throws an exception.");
    }

    @Test
    @DisplayName("Delete button should not call DAO if no item is selected")
    void deleteSelectedInterview_NoSelection_DoesNotCallDao() throws SQLException {
        when(mockInterviewDao.getAllInterviews()).thenReturn(List.of(new Interview()));
        dialog = new InterviewHistoryDialog(null, mockInterviewDao, mockAudioOutputService);

        dialog.getInterviewList().clearSelection();

        dialog.getDeleteButton().doClick();

        verify(mockInterviewDao, never()).deleteInterview(anyInt());
    }
}
