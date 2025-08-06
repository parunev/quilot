package com.quilot.db.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Represents a single entry (a line of dialogue) from the 'transcription_entries' table.
 */
@Data
public class TranscriptionEntry {
    private int id;
    private int interviewId;
    private LocalDateTime timestamp;
    private String speaker; // e.g., "Interviewer", "AI"
    private String content;
    private boolean isQuestion;
}
