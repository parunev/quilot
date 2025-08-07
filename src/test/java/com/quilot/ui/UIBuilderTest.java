package com.quilot.ui;

import com.quilot.audio.input.AudioInputService;
import com.quilot.audio.ouput.AudioOutputService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for the {@link UIBuilder} class.
 */
@ExtendWith(MockitoExtension.class)
class UIBuilderTest {

    @Mock
    private AudioOutputService mockAudioOutputService;
    @Mock
    private AudioInputService mockAudioInputService;
    @Mock
    private ElapsedTimerManager mockTimerManager;

    private UIBuilder uiBuilder;

    @BeforeEach
    void setUp() {
        uiBuilder = new UIBuilder(mockAudioOutputService, mockAudioInputService, mockTimerManager);
    }

    @Test
    @DisplayName("Should initialize all panel builders successfully")
    void constructor_InitializesAllPanelBuilders() {
        assertNotNull(uiBuilder.getAudioOutputSettingsPanelBuilder(), "AudioOutputSettingsPanelBuilder should not be null.");
        assertNotNull(uiBuilder.getAudioInputSettingsPanelBuilder(), "AudioInputSettingsPanelBuilder should not be null.");
        assertNotNull(uiBuilder.getTranscribedAudioPanelBuilder(), "TranscribedAudioPanelBuilder should not be null.");
        assertNotNull(uiBuilder.getAiResponsePanelBuilder(), "AIResponsePanelBuilder should not be null.");
        assertNotNull(uiBuilder.getLogPanelBuilder(), "LogPanelBuilder should not be null.");
        assertNotNull(uiBuilder.getStatusBar(), "StatusBar should not be null.");
    }

    @Test
    @DisplayName("Getters for UI components should return non-null objects")
    void getters_ReturnNonNullComponents() {
        assertNotNull(uiBuilder.getOutputDeviceComboBox(), "OutputDeviceComboBox should not be null.");
        assertNotNull(uiBuilder.getVolumeSlider(), "VolumeSlider should not be null.");
        assertNotNull(uiBuilder.getTestVolumeButton(), "TestVolumeButton should not be null.");
        assertNotNull(uiBuilder.getInputDeviceComboBox(), "InputDeviceComboBox should not be null.");
        assertNotNull(uiBuilder.getStartInputRecordingButton(), "StartInputRecordingButton should not be null.");
        assertNotNull(uiBuilder.getStopInputRecordingButton(), "StopInputRecordingButton should not be null.");
        assertNotNull(uiBuilder.getPlayRecordedInputButton(), "PlayRecordedInputButton should not be null.");
        assertNotNull(uiBuilder.getTranscribedAudioArea(), "TranscribedAudioArea should not be null.");
        assertNotNull(uiBuilder.getAiResponseArea(), "AiResponseArea should not be null.");
        assertNotNull(uiBuilder.getLogArea(), "LogArea should not be null.");
    }
}
