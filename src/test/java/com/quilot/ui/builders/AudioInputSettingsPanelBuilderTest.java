package com.quilot.ui.builders;

import com.quilot.audio.input.AudioInputService;
import com.quilot.exceptions.audio.AudioDeviceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link AudioInputSettingsPanelBuilder} class.
 */
@ExtendWith(MockitoExtension.class)
class AudioInputSettingsPanelBuilderTest {

    @Mock
    private AudioInputService mockAudioInputService;

    private AudioInputSettingsPanelBuilder panelBuilder;

    @Test
    @DisplayName("Should populate combo box when devices are available")
    void populateInputDevices_WithAvailableDevices_PopulatesComboBox() {
        List<String> fakeDevices = List.of("Microphone A", "Microphone B");
        when(mockAudioInputService.getAvailableInputDevices()).thenReturn(fakeDevices);

        panelBuilder = new AudioInputSettingsPanelBuilder(mockAudioInputService);
        JComboBox<String> comboBox = panelBuilder.getInputDeviceComboBox();

        assertEquals(2, comboBox.getItemCount());
        assertEquals("Microphone A", comboBox.getItemAt(0));
        assertTrue(comboBox.isEnabled());
        assertTrue(panelBuilder.getStartInputRecordingButton().isEnabled());
    }

    @Test
    @DisplayName("Should show 'No Devices Found' when no devices are available")
    void populateInputDevices_WithNoDevices_ShowsMessageAndDisablesComponents() {
        when(mockAudioInputService.getAvailableInputDevices()).thenReturn(Collections.emptyList());

        panelBuilder = new AudioInputSettingsPanelBuilder(mockAudioInputService);
        JComboBox<String> comboBox = panelBuilder.getInputDeviceComboBox();

        assertEquals(1, comboBox.getItemCount());
        assertEquals("No Devices Found", comboBox.getItemAt(0));
        assertFalse(comboBox.isEnabled());
        assertFalse(panelBuilder.getStartInputRecordingButton().isEnabled());
    }

    @Test
    @DisplayName("Should select the previously selected device if it exists")
    void populateInputDevices_WithPreselectedDevice_SelectsCorrectItem() {
        List<String> fakeDevices = List.of("Microphone A", "Microphone B", "Microphone C");
        when(mockAudioInputService.getAvailableInputDevices()).thenReturn(fakeDevices);
        when(mockAudioInputService.getSelectedDeviceName()).thenReturn("Microphone B");

        panelBuilder = new AudioInputSettingsPanelBuilder(mockAudioInputService);
        JComboBox<String> comboBox = panelBuilder.getInputDeviceComboBox();

        assertEquals("Microphone B", comboBox.getSelectedItem());
    }

    @Test
    @DisplayName("Should select the first device as default if none was preselected")
    void populateInputDevices_WithNoPreselectedDevice_SelectsFirstItem() throws AudioDeviceException {
        List<String> fakeDevices = List.of("Microphone A", "Microphone B");
        when(mockAudioInputService.getAvailableInputDevices()).thenReturn(fakeDevices);
        when(mockAudioInputService.getSelectedDeviceName()).thenReturn(null);

        panelBuilder = new AudioInputSettingsPanelBuilder(mockAudioInputService);
        JComboBox<String> comboBox = panelBuilder.getInputDeviceComboBox();

        assertEquals("Microphone A", comboBox.getSelectedItem());
        verify(mockAudioInputService, times(1)).selectInputDevice("Microphone A");
    }

    @Test
    @DisplayName("Should handle exception gracefully if default device selection fails")
    void populateInputDevices_WhenDefaultSelectionFails_DoesNotCrash() throws AudioDeviceException {
        List<String> fakeDevices = List.of("Faulty Microphone");
        when(mockAudioInputService.getAvailableInputDevices()).thenReturn(fakeDevices);
        when(mockAudioInputService.getSelectedDeviceName()).thenReturn(null);

        doThrow(new AudioDeviceException("Device is unplugged")).when(mockAudioInputService).selectInputDevice("Faulty Microphone");

        assertDoesNotThrow(() -> {
            panelBuilder = new AudioInputSettingsPanelBuilder(mockAudioInputService);
        });
    }
}
