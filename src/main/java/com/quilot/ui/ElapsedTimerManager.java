package com.quilot.ui;

import com.quilot.utils.Logger;
import lombok.Getter;

import javax.swing.*;
import java.time.Duration;

/**
 * Manages an elapsed time display for the UI.
 * This class encapsulates the timer logic and the {@link JLabel} used for displaying
 * the formatted time, ensuring that UI updates are performed on the Swing Event Dispatch Thread.
 */
@Getter
public class ElapsedTimerManager {

    private static final String ELAPSED_TIME_PREFIX = "Elapsed Time: ";

    private final JLabel elapsedTimeLabel;

    private Timer elapsedTimeTimer;
    private long elapsedStartTimeMillis;

    public ElapsedTimerManager() {
        elapsedTimeLabel = new JLabel(ELAPSED_TIME_PREFIX + "00:00:00");
    }


    /**
     * Starts the elapsed timer.
     * Records the start time and updates elapsed time every second.
     */
    public void startElapsedTimer() {
        elapsedStartTimeMillis = System.currentTimeMillis();

        if (elapsedTimeTimer != null && elapsedTimeTimer.isRunning()) {
            elapsedTimeTimer.stop();
        }

        elapsedTimeTimer = new Timer(1000, _ -> updateElapsedTimeLabel());
        elapsedTimeTimer.start();
        Logger.info("Elapsed time timer started.");
    }

    private void updateElapsedTimeLabel() {
        Duration elapsed = Duration.ofMillis(System.currentTimeMillis() - elapsedStartTimeMillis);
        long hours = elapsed.toHours();
        long minutes = elapsed.toMinutesPart();
        long seconds = elapsed.toSecondsPart();

        String formattedElapsed = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        elapsedTimeLabel.setText(ELAPSED_TIME_PREFIX + formattedElapsed);
    }

    /**
     * Stops the elapsed interview timer and resets the elapsed time display.
     */
    public void stopElapsedTimer() {
        if (elapsedTimeTimer != null && elapsedTimeTimer.isRunning()) {
            elapsedTimeTimer.stop();
            Logger.info("Elapsed time timer stopped.");
        }
    }
}
