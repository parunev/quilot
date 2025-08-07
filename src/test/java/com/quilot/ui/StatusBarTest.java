package com.quilot.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the {@link StatusBar} class.
 */
class StatusBarTest {

    private StatusBar statusBar;
    private JLabel statusLabel;

    @BeforeEach
    void setUp() throws InvocationTargetException, InterruptedException {
        SwingUtilities.invokeAndWait(() -> {
            statusBar = new StatusBar();
            statusLabel = (JLabel) statusBar.getComponent(0);
        });
    }

    @Test
    @DisplayName("Should initialize with default 'Ready' text and INFO color")
    void constructor_InitializesWithDefaultState() {
        assertEquals("Ready", statusLabel.getText());
        assertEquals(Color.WHITE, statusLabel.getForeground());
    }

    @Test
    @DisplayName("setStatus with SUCCESS type should set green text")
    void setStatus_SuccessType_SetsGreenColor() throws InterruptedException {
        statusBar.setStatus("Operation successful", StatusBar.StatusType.SUCCESS);

        Thread.sleep(100); // Small delay for invokeLater to run
        assertEquals("Operation successful", statusLabel.getText());
        assertEquals(new Color(130, 200, 130), statusLabel.getForeground());
    }

    @Test
    @DisplayName("setStatus with ERROR type should set red text")
    void setStatus_ErrorType_SetsRedColor() throws InterruptedException {
        statusBar.setStatus("An error occurred", StatusBar.StatusType.ERROR);

        Thread.sleep(100);
        assertEquals("An error occurred", statusLabel.getText());
        assertEquals(new Color(255, 100, 100), statusLabel.getForeground());
    }

    @Test
    @DisplayName("setStatus with INFO type should set white text")
    void setStatus_InfoType_SetsWhiteColor() throws InterruptedException {
        statusBar.setStatus("An error occurred", StatusBar.StatusType.ERROR);
        Thread.sleep(100);

        statusBar.setStatus("Back to normal", StatusBar.StatusType.INFO);

        Thread.sleep(100);
        assertEquals("Back to normal", statusLabel.getText());
        assertEquals(Color.WHITE, statusLabel.getForeground());
    }
}
