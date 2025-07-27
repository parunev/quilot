package com.quilot.ui;

import com.quilot.utils.Logger;

import javax.swing.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Manages the current time and elapsed interview time display.
 * This class encapsulates the timer logic and the JLabel components for time display.
 */
public class InterviewTimerManager {

    private JLabel currentTimeLabel;
    private JLabel elapsedTimeLabel;

    private Timer currentTimeUpdateTimer;
    private Timer elapsedTimeUpdateTimer;
    private long interviewStartTimeMillis;

    /**
     * Initializes the time display labels and starts the current time timer.
     */
    public InterviewTimerManager() {
        currentTimeLabel = new JLabel("Current Time: --:--:--");
        elapsedTimeLabel = new JLabel("Elapsed: 00:00:00");
        startCurrentTimeTimer(); // Start updating current time immediately
    }

    /**
     * Returns the JLabel displaying the current time.
     * @return The JLabel for current time.
     */
    public JLabel getCurrentTimeLabel() {
        return currentTimeLabel;
    }

    /**
     * Returns the JLabel displaying the elapsed time.
     * @return The JLabel for elapsed time.
     */
    public JLabel getElapsedTimeLabel() {
        return elapsedTimeLabel;
    }

    /**
     * Starts a timer to update the current time label every second.
     */
    private void startCurrentTimeTimer() {
        currentTimeUpdateTimer = new Timer(1000, _ -> {
            // Update current time label
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            currentTimeLabel.setText("Current Time: " + time);
        });
        currentTimeUpdateTimer.start();
        Logger.info("Current time update timer started.");
    }

    /**
     * Starts the elapsed time timer.
     * This method should be called when the interview begins.
     */
    public void startInterviewTimer() {
        interviewStartTimeMillis = System.currentTimeMillis();

        if (elapsedTimeUpdateTimer != null) {
            elapsedTimeUpdateTimer.stop(); // Stop any existing timer first
        }

        elapsedTimeUpdateTimer = new Timer(1000, _ -> {
            long elapsedMillis = System.currentTimeMillis() - interviewStartTimeMillis;
            Duration duration = Duration.ofMillis(elapsedMillis);

            long hours = duration.toHours();
            long minutes = duration.toMinutes() % 60;
            long seconds = duration.getSeconds() % 60;

            String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            elapsedTimeLabel.setText("Elapsed: " + formattedTime);
        });

        elapsedTimeUpdateTimer.start();
        Logger.info("Elapsed time timer started for interview.");
    }

    /**
     * Stops the elapsed time timer and resets the elapsed time label.
     * This method should be called when the interview ends.
     */
    public void stopInterviewTimer() {
        if (elapsedTimeUpdateTimer != null) {
            elapsedTimeUpdateTimer.stop();
            Logger.info("Elapsed time timer stopped.");
        }
        elapsedTimeLabel.setText("Elapsed: 00:00:00");
    }
}
