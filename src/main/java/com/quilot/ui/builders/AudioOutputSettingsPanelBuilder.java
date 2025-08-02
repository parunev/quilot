package com.quilot.ui.builders;

import com.quilot.audio.ouput.AudioOutputService;
import com.quilot.exceptions.audio.AudioDeviceException;
import com.quilot.utils.Logger;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Builds the panel for Audio Output Settings.
 * This class has been updated to correctly select the previously saved audio device.
 */
@Getter
public class AudioOutputSettingsPanelBuilder implements ComponentPanelBuilder {

    private final JComboBox<String> outputDeviceComboBox;
    private final JSlider volumeSlider;
    private final JButton testVolumeButton;

    private final AudioOutputService audioOutputService;

    public AudioOutputSettingsPanelBuilder(AudioOutputService audioOutputService) {
        this.audioOutputService = audioOutputService;

        outputDeviceComboBox = new JComboBox<>();
        volumeSlider = new JSlider(0, 100, 70);
        testVolumeButton = new JButton("Test Volume");

        configureVolumeSlider();
        populateOutputDevices();
    }

    private void configureVolumeSlider() {
        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setMinorTickSpacing(5);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
    }

    private void populateOutputDevices() {
        List<String> devices = audioOutputService.getAvailableOutputDevices();
        outputDeviceComboBox.removeAllItems(); // Clear any existing items

        if (devices.isEmpty()) {
            outputDeviceComboBox.addItem("No Devices Found");
            outputDeviceComboBox.setEnabled(false);
            volumeSlider.setEnabled(false);
            testVolumeButton.setEnabled(false);
        } else {
            devices.forEach(outputDeviceComboBox::addItem);

            String selectedDevice = audioOutputService.getSelectedDeviceName();
            if (selectedDevice != null && devices.contains(selectedDevice)) {
                outputDeviceComboBox.setSelectedItem(selectedDevice);
            } else {
                String defaultDevice = devices.getFirst();
                outputDeviceComboBox.setSelectedItem(defaultDevice);

                try {
                    audioOutputService.selectOutputDevice(defaultDevice);
                } catch (AudioDeviceException e) {
                    Logger.warn("Could not auto-select the default audio output device '" + defaultDevice + "': " + e.getMessage());
                }
            }
        }
    }

    @Override
    public JPanel build() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Audio Output Settings (Interviewer Voice)"));
        GridBagConstraints gbc = createDefaultGbc();

        addOutputDeviceRow(panel, gbc);
        addVolumeSliderRow(panel, gbc);
        addTestButtonRow(panel, gbc);

        return panel;
    }

    private GridBagConstraints createDefaultGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private void addOutputDeviceRow(JPanel panel, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("Output Device:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(outputDeviceComboBox, gbc);
    }

    private void addVolumeSliderRow(JPanel panel, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Volume:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(volumeSlider, gbc);
    }

    private void addTestButtonRow(JPanel panel, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(testVolumeButton, gbc);
    }
}
