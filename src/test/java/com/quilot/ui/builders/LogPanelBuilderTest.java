package com.quilot.ui.builders;

import com.quilot.ui.ElapsedTimerManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link LogPanelBuilder} class.
 */
@ExtendWith(MockitoExtension.class)
class LogPanelBuilderTest {

    @Mock
    private ElapsedTimerManager mockTimerManager;

    private LogPanelBuilder panelBuilder;

    @BeforeEach
    void setUp() {
        // No stubbing is needed here anymore, as it's handled per-test.
    }

    @Test
    @DisplayName("Should build a non-null JPanel")
    void build_ReturnsNonNullPanel() {
        when(mockTimerManager.getElapsedTimeLabel()).thenReturn(new JLabel());
        panelBuilder = new LogPanelBuilder(mockTimerManager);

        JPanel panel = panelBuilder.build();

        assertNotNull(panel, "The built panel should not be null.");
    }

    @Test
    @DisplayName("Log area JTextArea should be configured correctly")
    void createLogArea_IsConfiguredCorrectly() {
        panelBuilder = new LogPanelBuilder(mockTimerManager);

        JTextArea logArea = panelBuilder.getLogArea();

        assertFalse(logArea.isEditable(), "Log area should not be editable.");
        assertTrue(logArea.getLineWrap(), "Line wrap should be enabled.");
        assertTrue(logArea.getWrapStyleWord(), "Word wrap style should be enabled.");
    }

    @Test
    @DisplayName("Panel should contain the timer label from the manager")
    void build_ContainsTimerLabel() {
        JLabel fakeTimerLabel = new JLabel("00:00:00");
        when(mockTimerManager.getElapsedTimeLabel()).thenReturn(fakeTimerLabel);
        panelBuilder = new LogPanelBuilder(mockTimerManager);

        JPanel panel = panelBuilder.build();
        Component headerPanel = ((BorderLayout)panel.getLayout()).getLayoutComponent(BorderLayout.NORTH);

        assertNotNull(headerPanel, "Header panel should exist.");
        assertInstanceOf(JPanel.class, headerPanel, "Header should be a JPanel.");

        Component[] headerComponents = ((JPanel) headerPanel).getComponents();
        boolean timerLabelFound = false;
        for (Component comp : headerComponents) {
            if (comp == fakeTimerLabel) { // Check for the exact mock object
                timerLabelFound = true;
                break;
            }
        }
        assertTrue(timerLabelFound, "The header panel should contain the elapsed time label from the timer manager.");
    }
}
