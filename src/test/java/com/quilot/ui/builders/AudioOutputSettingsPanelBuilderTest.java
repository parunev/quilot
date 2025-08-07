package com.quilot.ui.builders;

import com.quilot.audio.ouput.AudioOutputService;
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
 * Unit tests for the {@link AudioOutputSettingsPanelBuilder} class.
 */
@ExtendWith(MockitoExtension.class)
class AudioOutputSettingsPanelBuilderTest {

    @Mock
    private AudioOutputService mockAudioOutputService;

    private AudioOutputSettingsPanelBuilder panelBuilder;

    @Test
    @DisplayName("Should populate combo box when output devices are available")
    void populateOutputDevices_WithAvailableDevices_PopulatesComboBox() {
        List<String> fakeDevices = List.of("Speakers", "Headphones");
        when(mockAudioOutputService.getAvailableOutputDevices()).thenReturn(fakeDevices);

        panelBuilder = new AudioOutputSettingsPanelBuilder(mockAudioOutputService);
        JComboBox<String> comboBox = panelBuilder.getOutputDeviceComboBox();

        assertEquals(2, comboBox.getItemCount());
        assertEquals("Speakers", comboBox.getItemAt(0));
        assertTrue(comboBox.isEnabled());
        assertTrue(panelBuilder.getVolumeSlider().isEnabled());
        assertTrue(panelBuilder.getTestVolumeButton().isEnabled());
    }

    @Test
    @DisplayName("Should show 'No Devices Found' when no output devices are available")
    void populateOutputDevices_WithNoDevices_ShowsMessageAndDisablesComponents() {
        when(mockAudioOutputService.getAvailableOutputDevices()).thenReturn(Collections.emptyList());

        panelBuilder = new AudioOutputSettingsPanelBuilder(mockAudioOutputService);
        JComboBox<String> comboBox = panelBuilder.getOutputDeviceComboBox();

        assertEquals(1, comboBox.getItemCount());
        assertEquals("No Devices Found", comboBox.getItemAt(0));
        assertFalse(comboBox.isEnabled());
        assertFalse(panelBuilder.getVolumeSlider().isEnabled());
        assertFalse(panelBuilder.getTestVolumeButton().isEnabled());
    }

    @Test
    @DisplayName("Should select the previously selected output device if it exists")
    void populateOutputDevices_WithPreselectedDevice_SelectsCorrectItem() {
        List<String> fakeDevices = List.of("Speakers", "Headphones", "Monitor Audio");
        when(mockAudioOutputService.getAvailableOutputDevices()).thenReturn(fakeDevices);
        when(mockAudioOutputService.getSelectedDeviceName()).thenReturn("Headphones");

        panelBuilder = new AudioOutputSettingsPanelBuilder(mockAudioOutputService);
        JComboBox<String> comboBox = panelBuilder.getOutputDeviceComboBox();

        assertEquals("Headphones", comboBox.getSelectedItem());
    }

    @Test
    @DisplayName("Should select the first output device as default if none was preselected")
    void populateOutputDevices_WithNoPreselectedDevice_SelectsFirstItem() throws AudioDeviceException {
        List<String> fakeDevices = List.of("Speakers", "Headphones");
        when(mockAudioOutputService.getAvailableOutputDevices()).thenReturn(fakeDevices);
        when(mockAudioOutputService.getSelectedDeviceName()).thenReturn(null);

        panelBuilder = new AudioOutputSettingsPanelBuilder(mockAudioOutputService);
        JComboBox<String> comboBox = panelBuilder.getOutputDeviceComboBox();

        assertEquals("Speakers", comboBox.getSelectedItem());
        verify(mockAudioOutputService, times(1)).selectOutputDevice("Speakers");
    }

    @Test
    @DisplayName("Should handle exception gracefully if default output device selection fails")
    void populateOutputDevices_WhenDefaultSelectionFails_DoesNotCrash() throws AudioDeviceException {
        List<String> fakeDevices = List.of("Broken Speaker");
        when(mockAudioOutputService.getAvailableOutputDevices()).thenReturn(fakeDevices);
        when(mockAudioOutputService.getSelectedDeviceName()).thenReturn(null);
        doThrow(new AudioDeviceException("Device not available")).when(mockAudioOutputService).selectOutputDevice("Broken Speaker");

        assertDoesNotThrow(() -> {
            panelBuilder = new AudioOutputSettingsPanelBuilder(mockAudioOutputService);
        });
    }
}
