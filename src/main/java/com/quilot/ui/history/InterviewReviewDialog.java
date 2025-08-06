package com.quilot.ui.history;

import com.quilot.audio.ouput.AudioOutputService;
import com.quilot.db.dao.InterviewDao;
import com.quilot.db.model.Interview;
import com.quilot.db.model.TranscriptionEntry;
import com.quilot.utils.Logger;

import javax.sound.sampled.AudioFormat;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * A dialog to review the details of a single past interview.
 */
public class InterviewReviewDialog extends JDialog {

    private final Interview interview;
    private final InterviewDao interviewDao;
    private final AudioOutputService audioOutputService;
    private final JTextArea transcriptArea;
    private final JButton playAudioButton;
    private final JButton stopAudioButton;

    public InterviewReviewDialog(Dialog owner, Interview interview, InterviewDao interviewDao, AudioOutputService audioOutputService) {
        super(owner, "Review: " + interview.getTitle(), true);
        this.interview = interview;
        this.interviewDao = interviewDao;
        this.audioOutputService = audioOutputService;
        this.transcriptArea = new JTextArea();
        this.playAudioButton = new JButton("Play Full Audio Recording");
        this.stopAudioButton = new JButton("Stop Playback");

        initComponents();
        loadTranscription();
    }

    private void initComponents() {
        setSize(700, 800);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(10, 10));

        transcriptArea.setEditable(false);
        transcriptArea.setLineWrap(true);
        transcriptArea.setWrapStyleWord(true);
        transcriptArea.setFont(new Font("SansSerif", Font.PLAIN, 14));

        playAudioButton.addActionListener(_ -> playFullAudio());
        stopAudioButton.addActionListener(_ -> audioOutputService.stopPlayback());
        stopAudioButton.setEnabled(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(playAudioButton);
        buttonPanel.add(stopAudioButton);

        add(new JScrollPane(transcriptArea), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadTranscription() {
        try {
            List<TranscriptionEntry> entries = interviewDao.getTranscriptionEntriesForInterview(interview.getId());
            StringBuilder sb = new StringBuilder();
            for (TranscriptionEntry entry : entries) {
                sb.append(String.format("[%s] %s:\n", entry.getTimestamp().toLocalTime(), entry.getSpeaker()));
                sb.append(entry.getContent()).append("\n\n");
            }
            transcriptArea.setText(sb.toString());
            transcriptArea.setCaretPosition(0); // Scroll to top
        } catch (SQLException e) {
            Logger.error("Failed to load transcription for interview " + interview.getId(), e);
            transcriptArea.setText("Error: Could not load transcription.");
        }
    }

    private void playFullAudio() {
        playAudioButton.setEnabled(false);
        playAudioButton.setText("Playing...");
        stopAudioButton.setEnabled(true);

        // Use a standard Thread for more direct control over the blocking audio call.
        new Thread(() -> {
            try {
                Logger.info("Playback thread started: Fetching audio for interview ID " + interview.getId());
                Interview fullInterview = interviewDao.getInterviewById(interview.getId());
                byte[] audioData = (fullInterview != null) ? fullInterview.getFullAudio() : null;

                if (audioData != null && audioData.length > 0) {
                    Logger.info("Audio data found. Starting playback...");
                    AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
                    // This is a blocking call that will run until the audio is finished or stopped.
                    audioOutputService.playAudioData(audioData, format);
                    Logger.info("Playback finished successfully.");
                } else {
                    throw new Exception("No audio data found for this interview in the database.");
                }
            } catch (Exception e) {
                // Since we are on a background thread, show any errors on the Event Dispatch Thread.
                SwingUtilities.invokeLater(() -> {
                    Logger.error("Failed to play audio for interview " + interview.getId(), e);
                    JOptionPane.showMessageDialog(
                            InterviewReviewDialog.this,
                            "Could not play audio:\n" + e.getMessage(),
                            "Playback Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                });
            } finally {
                // Always reset the button states on the Event Dispatch Thread when the task is complete.
                SwingUtilities.invokeLater(() -> {
                    playAudioButton.setText("Play Full Audio Recording");
                    playAudioButton.setEnabled(true);
                    stopAudioButton.setEnabled(false);
                });
            }
        }).start();
    }
}
