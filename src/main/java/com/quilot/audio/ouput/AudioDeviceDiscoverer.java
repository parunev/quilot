package com.quilot.audio.ouput;

import com.quilot.utils.Logger;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class responsible solely for discovering available audio output devices.
 * This adheres to the Single Responsibility Principle by separating device discovery
 * from device management and playback.
 */
public class AudioDeviceDiscoverer {

    /**
     * Discovers and returns a list of names of available audio output devices.
     * A device is considered an "output device" if it supports a SourceDataLine.
     * @return A List of String containing the names of available output devices.
     */
    public static List<String> discoverOutputDevices() {
        List<String> deviceNames = new ArrayList<>();
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();

        for (Mixer.Info info : mixerInfos) {
            Mixer mixer = AudioSystem.getMixer(info);

            try {
                if (mixer.isLineSupported(new Line.Info(SourceDataLine.class))) {
                    deviceNames.add(info.getName());
                }
            } catch (IllegalArgumentException e) {
                // This can happen if the Line.Info is not properly constructed or supported
                Logger.warn("Mixer " + info.getName() + " does not support SourceDataLine as expected: " + e.getMessage());
            }
        }

        if (deviceNames.isEmpty()) {
            Logger.warn("No audio output devices found.");
        } else {
            Logger.info("Found " + deviceNames.size() + " audio output devices.");
        }

        return deviceNames;
    }
}
