package com.quilot.db.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Represents a single interview session record from the 'interviews' table.
 */
@Data
public class Interview {
    private int id;
    private String title;
    private LocalDateTime interviewDate;
    private byte[] fullAudio;
}
