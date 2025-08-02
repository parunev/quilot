package com.quilot.audio.ouput;

import com.quilot.utils.Logger;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A final utility class for discovering available audio output devices on the system.
 * This class cannot be instantiated.
 */
public final class AudioDeviceDiscoverer {

    private AudioDeviceDiscoverer() {
        throw new UnsupportedOperationException("AudioDeviceDiscoverer is a utility class.");
    }

    /**
     * Scans the system for all available audio output devices.
     * An output device is identified as a mixer that supports a {@link SourceDataLine}.
     *
     * @return A {@link List} of the names of all found output devices. Returns an empty list if none are found.
     */
    public static List<String> discoverOutputDevices() {
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        if (mixerInfos == null || mixerInfos.length == 0) {
            Logger.warn("No mixers found in system.");
            return Collections.emptyList();
        }

        List<String> outputDevices = new ArrayList<>();

        for (Mixer.Info info : mixerInfos) {
            if (isOutputDevice(info)) {
                outputDevices.add(info.getName());
            }
        }

        logDiscoverySummary(outputDevices);
        return outputDevices;
    }

    private static boolean isOutputDevice(Mixer.Info mixerInfo) {
        try {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            return mixer.isLineSupported(new Line.Info(SourceDataLine.class));
        } catch (IllegalArgumentException | SecurityException e) {
            Logger.warn("Could not query mixer '" + mixerInfo.getName() + "': " + e.getMessage());
            return false;
        }
    }

    private static void logDiscoverySummary(List<String> devices) {
        if (devices.isEmpty()) {
            Logger.warn("No audio output devices found.");
        } else {
            Logger.info("Discovered " + devices.size() + " audio output device(s).");
        }
    }
}
