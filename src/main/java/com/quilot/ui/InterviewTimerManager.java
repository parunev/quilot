package com.quilot.ui;

import com.quilot.utils.Logger;
import lombok.Getter;

import javax.swing.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Manages the current time and elapsed interview time display.
 * This class encapsulates the timer logic and the JLabel components for time display.
 */
@Getter
public class InterviewTimerManager {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String CURRENT_TIME_PREFIX = "Current Time: ";
    private static final String ELAPSED_TIME_PREFIX = "Elapsed: ";

    private final JLabel currentTimeLabel;
    private final JLabel elapsedTimeLabel;

    private Timer currentTimeTimer;
    private Timer elapsedTimeTimer;
    private long interviewStartTimeMillis;

    public InterviewTimerManager() {
        currentTimeLabel = new JLabel(CURRENT_TIME_PREFIX + "--:--:--");
        elapsedTimeLabel = new JLabel(ELAPSED_TIME_PREFIX + "00:00:00");
        startCurrentTimeTimer();
    }

    private void startCurrentTimeTimer() {
        currentTimeTimer = new Timer(1000, _ ->
                currentTimeLabel.setText(CURRENT_TIME_PREFIX + LocalDateTime.now().format(TIME_FORMATTER))
        );
        currentTimeTimer.start();
        Logger.info("Current time update timer started.");
    }

    /**
     * Starts the elapsed interview timer.
     * Records the start time and updates elapsed time every second.
     */
    public void startInterviewTimer() {
        interviewStartTimeMillis = System.currentTimeMillis();

        if (elapsedTimeTimer != null && elapsedTimeTimer.isRunning()) {
            elapsedTimeTimer.stop();
        }

        elapsedTimeTimer = new Timer(1000, _ -> updateElapsedTimeLabel());
        elapsedTimeTimer.start();
        Logger.info("Elapsed time timer started for interview.");
    }

    private void updateElapsedTimeLabel() {
        Duration elapsed = Duration.ofMillis(System.currentTimeMillis() - interviewStartTimeMillis);
        long hours = elapsed.toHours();
        long minutes = elapsed.toMinutesPart();
        long seconds = elapsed.toSecondsPart();

        String formattedElapsed = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        elapsedTimeLabel.setText(ELAPSED_TIME_PREFIX + formattedElapsed);
    }

    /**
     * Stops the elapsed interview timer and resets the elapsed time display.
     */
    public void stopInterviewTimer() {
        if (elapsedTimeTimer != null && elapsedTimeTimer.isRunning()) {
            elapsedTimeTimer.stop();
            Logger.info("Elapsed time timer stopped.");
        }
        elapsedTimeLabel.setText(ELAPSED_TIME_PREFIX + "00:00:00");
    }

}
