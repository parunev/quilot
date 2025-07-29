package com.quilot.audio.ouput;

import com.quilot.utils.Logger;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AudioDeviceDiscoverer {

    private AudioDeviceDiscoverer() {
        throw new UnsupportedOperationException("AudioDeviceDiscoverer is a utility class.");
    }

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
        } catch (IllegalArgumentException e) {
            Logger.warn("Mixer '" + mixerInfo.getName() + "' does not support SourceDataLine: " + e.getMessage());
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
