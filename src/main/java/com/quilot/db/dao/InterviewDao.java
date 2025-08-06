package com.quilot.db.dao;

import com.quilot.db.DatabaseManager;
import com.quilot.db.model.Interview;
import com.quilot.db.model.TranscriptionEntry;
import com.quilot.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for handling all database operations related to interviews.
 */
public class InterviewDao {

    /**
     * Creates a new interview record in the database.
     *
     * @param title The initial title for the interview (can be null).
     * @return The generated ID of the new interview record.
     * @throws SQLException if a database access error occurs.
     */
    public int createNewInterview(String title) throws SQLException {
        String sql = "INSERT INTO interviews (title, interview_date) VALUES (?, ?)";
        int generatedId = -1;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, title);
            pstmt.setTimestamp(2, Timestamp.valueOf(java.time.LocalDateTime.now()));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                        Logger.info("Created new interview record with ID: " + generatedId);
                    }
                }
            }
        }
        return generatedId;
    }

    /**
     * Adds a new transcription entry linked to an interview.
     *
     * @param interviewId The ID of the interview this entry belongs to.
     * @param speaker The speaker of the content (e.g., "Interviewer", "AI").
     * @param content The transcribed text or AI response.
     * @param isQuestion True if the content is a question, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public void addTranscriptionEntry(int interviewId, String speaker, String content, boolean isQuestion) throws SQLException {
        String sql = "INSERT INTO transcription_entries (interview_id, speaker, content, is_question, timestamp) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, interviewId);
            pstmt.setString(2, speaker);
            pstmt.setString(3, content);
            pstmt.setBoolean(4, isQuestion);
            pstmt.setTimestamp(5, Timestamp.valueOf(java.time.LocalDateTime.now()));

            pstmt.executeUpdate();
        }
    }

    /**
     * Updates an existing interview record with the full recorded audio.
     *
     * @param interviewId The ID of the interview to update.
     * @param fullAudioData The byte array of the complete audio recording.
     * @throws SQLException if a database access error occurs.
     */
    public void saveFullAudio(int interviewId, byte[] fullAudioData) throws SQLException {
        String sql = "UPDATE interviews SET full_audio = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBytes(1, fullAudioData);
            pstmt.setInt(2, interviewId);

            pstmt.executeUpdate();
            Logger.info("Saved full audio recording for interview ID: " + interviewId);
        }
    }

    /**
     * Retrieves a list of all interviews from the database, ordered by most recent first.
     * This method only fetches the ID, title, and date to keep the list lightweight.
     *
     * @return A List of Interview objects.
     * @throws SQLException if a database access error occurs.
     */
    public List<Interview> getAllInterviews() throws SQLException {
        String sql = "SELECT id, title, interview_date FROM interviews ORDER BY interview_date DESC";
        List<Interview> interviews = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Interview interview = new Interview();
                interview.setId(rs.getInt("id"));
                interview.setTitle(rs.getString("title"));
                interview.setInterviewDate(rs.getTimestamp("interview_date").toLocalDateTime());
                interviews.add(interview);
            }
        }
        return interviews;
    }

    /**
     * Retrieves all transcription entries for a specific interview, ordered by time.
     *
     * @param interviewId The ID of the interview to fetch entries for.
     * @return A List of TranscriptionEntry objects.
     * @throws SQLException if a database access error occurs.
     */
    public List<TranscriptionEntry> getTranscriptionEntriesForInterview(int interviewId) throws SQLException {
        String sql = "SELECT * FROM transcription_entries WHERE interview_id = ? ORDER BY timestamp ASC";
        List<TranscriptionEntry> entries = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, interviewId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TranscriptionEntry entry = new TranscriptionEntry();
                    entry.setId(rs.getInt("id"));
                    entry.setInterviewId(rs.getInt("interview_id"));
                    entry.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                    entry.setSpeaker(rs.getString("speaker"));
                    entry.setContent(rs.getString("content"));
                    entry.setQuestion(rs.getBoolean("is_question"));
                    entries.add(entry);
                }
            }
        }
        return entries;
    }

    /**
     * Retrieves a single, complete interview record, including the full audio data.
     *
     * @param interviewId The ID of the interview to fetch.
     * @return An Interview object, or null if not found.
     * @throws SQLException if a database access error occurs.
     */
    public Interview getInterviewById(int interviewId) throws SQLException {
        String sql = "SELECT * FROM interviews WHERE id = ?";
        Interview interview = null;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, interviewId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    interview = new Interview();
                    interview.setId(rs.getInt("id"));
                    interview.setTitle(rs.getString("title"));
                    interview.setInterviewDate(rs.getTimestamp("interview_date").toLocalDateTime());
                    interview.setFullAudio(rs.getBytes("full_audio"));
                }
            }
        }
        return interview;
    }

    /**
     * Deletes an interview and all its associated transcription entries from the database.
     * The deletion cascades due to the foreign key constraint.
     *
     * @param interviewId The ID of the interview to delete.
     * @throws SQLException if a database access error occurs.
     */
    public void deleteInterview(int interviewId) throws SQLException {
        String sql = "DELETE FROM interviews WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, interviewId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                Logger.info("Successfully deleted interview with ID: " + interviewId);
            } else {
                Logger.warn("No interview found with ID: " + interviewId + " to delete.");
            }
        }
    }
}