package com.quilot.audio.ouput;

import com.quilot.utils.Logger;
import lombok.Getter;
import lombok.Setter;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.prefs.Preferences;

@Getter
@Setter
public class SystemAudioOutputService implements AudioOutputService{

    private static final String PREF_NODE_NAME = "com/quilot/audio";
    private static final String PREF_OUTPUT_DEVICE_KEY = "selectedOutputDevice";

    private final Preferences prefs;
    private Mixer selectedOutputMixer;
    private FloatControl masterGainControl;
    private SourceDataLine outputLine;

    private static final AudioFormat DEFAULT_AUDIO_FORMAT = new AudioFormat
            (44100,
                    16,
                    1, true,
                    false);

    public SystemAudioOutputService() {
        this.prefs = Preferences.userRoot().node(PREF_NODE_NAME);
        Logger.info("SystemAudioOutputService initialized. Preferences node: " + PREF_NODE_NAME);

        // Load the saved device on startup
        String savedDeviceName = loadSavedDeviceName();
        if (savedDeviceName != null) {
            Logger.info("Found saved audio device: " + savedDeviceName + ". Attempting to select it.");
            selectOutputDevice(savedDeviceName);
        }
    }

    @Override
    public List<String> getAvailableOutputDevices() {
        return AudioDeviceDiscoverer.discoverOutputDevices();
    }

    @Override
    public boolean selectOutputDevice(String deviceName) {
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixerInfos) {
            if (info.getName().equals(deviceName)) {
                try {
                    closeOutputLine();

                    selectedOutputMixer = AudioSystem.getMixer(info);
                    DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, DEFAULT_AUDIO_FORMAT);

                    if (!selectedOutputMixer.isLineSupported(dataLineInfo)) {
                        Logger.error("Mixer '" + deviceName + "' does not support default format: " + DEFAULT_AUDIO_FORMAT);

                        DataLine.Info[] lineInfos = (DataLine.Info[]) selectedOutputMixer.getSourceLineInfo();
                        AudioFormat fallbackFormat = null;

                        for (DataLine.Info lineInfo : lineInfos) {
                            AudioFormat[] formats = lineInfo.getFormats();
                            if (formats != null && formats.length > 0) {
                                fallbackFormat = formats[0];
                                break;
                            }
                        }

                        if (fallbackFormat != null) {
                            Logger.warn("Using fallback format " + fallbackFormat + " for mixer " + deviceName);
                            outputLine = (SourceDataLine) selectedOutputMixer.getLine(new DataLine.Info(SourceDataLine.class, fallbackFormat));
                            outputLine.open(fallbackFormat);
                        } else {
                            Logger.error("No compatible audio formats found for mixer: " + deviceName);
                            resetState();
                            return false;
                        }
                    } else {
                        outputLine = (SourceDataLine) selectedOutputMixer.getLine(dataLineInfo);
                        outputLine.open(DEFAULT_AUDIO_FORMAT);
                    }

                    outputLine.start();
                    initializeVolumeControl();

                    // Save the successfully selected device to preferences
                    saveSelectedDeviceName(deviceName);
                    Logger.info("Selected audio output device: " + deviceName);
                    return true;

                } catch (LineUnavailableException e) {
                    Logger.error("Line unavailable for mixer '" + deviceName + "': " + e.getMessage(), e);
                } catch (IllegalArgumentException e) {
                    Logger.error("Illegal argument for mixer '" + deviceName + "': " + e.getMessage(), e);
                } catch (Exception e) {
                    Logger.error("Unexpected error selecting mixer '" + deviceName + "': " + e.getMessage(), e);
                }
            }
        }

        Logger.warn("Audio output device '" + deviceName + "' not found or unsupported.");
        resetState();
        return false;
    }

    private void resetState() {
        selectedOutputMixer = null;
        outputLine = null;
        masterGainControl = null;
    }

    private void initializeVolumeControl() {
        if (outputLine != null && outputLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            masterGainControl = (FloatControl) outputLine.getControl(FloatControl.Type.MASTER_GAIN);
            Logger.info("Master Gain control initialized.");
        } else {
            masterGainControl = null;
            Logger.warn("Master Gain control not supported by current output line.");
        }
    }

    @Override
    public void setVolume(float volume) {
        if (masterGainControl != null) {
            float clampedVolume = Math.max(0.0f, Math.min(1.0f, volume));
            float min = masterGainControl.getMinimum();
            float max = masterGainControl.getMaximum();
            float gain = min + (max - min) * clampedVolume;

            masterGainControl.setValue(gain);
            Logger.info(String.format("Volume set to %.2f%% (Gain: %.2f dB)", clampedVolume * 100, gain));
        } else {
            Logger.warn("Volume control unavailable; cannot set volume.");
        }
    }

    @Override
    public void playTestSound() {
        if (!isDeviceSelected()) {
            Logger.warn("No audio device selected or output line not open.");
            return;
        }

        Logger.info("Playing test tone (440Hz, 500ms).");
        try {
            byte[] tone = AudioToneGenerator.generateSineWave(outputLine.getFormat(), 440, 500);
            outputLine.write(tone, 0, tone.length);
            outputLine.drain();
            Logger.info("Test tone playback complete.");
        } catch (Exception e) {
            Logger.error("Error during test tone playback: " + e.getMessage(), e);
        }
    }

    @Override
    public void playAudioData(byte[] audioData, AudioFormat format) {
        if (outputLine == null || !outputLine.isOpen()) {
            Logger.warn("Output line is not open. Cannot play audio data.");
            return;
        }

        try (AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(audioData), format, audioData.length / format.getFrameSize())) {

            AudioInputStream playbackStream = ais;
            AudioFormat targetFormat = outputLine.getFormat();

            if (!format.matches(targetFormat)) {
                if (AudioSystem.isConversionSupported(targetFormat, format)) {
                    playbackStream = AudioSystem.getAudioInputStream(targetFormat, ais);
                    Logger.info("Converted audio format from " + format + " to " + targetFormat);
                } else {
                    Logger.error("Audio format conversion unsupported: " + format + " to " + targetFormat);
                    return;
                }
            }

            byte[] buffer = new byte[outputLine.getBufferSize()];
            int bytesRead;

            while ((bytesRead = playbackStream.read(buffer)) != -1) {
                if (bytesRead > 0) {
                    outputLine.write(buffer, 0, bytesRead);
                }
            }

            outputLine.drain();
            Logger.info("Audio data playback completed.");

        } catch (IOException e) {
            Logger.error("IOException during audio playback: " + e.getMessage(), e);
        } catch (Exception e) {
            Logger.error("Unexpected error during audio playback: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        closeOutputLine();
        Logger.info("SystemAudioOutputService closed.");
    }

    private void closeOutputLine() {
        if (outputLine != null && outputLine.isOpen()) {
            outputLine.stop();
            outputLine.flush();
            outputLine.close();
            Logger.info("Output line closed.");
        }
    }

    @Override
    public boolean isDeviceSelected() {
        return outputLine != null && outputLine.isOpen();
    }

    private void saveSelectedDeviceName(String deviceName) {
        prefs.put(PREF_OUTPUT_DEVICE_KEY, deviceName);
        Logger.info("Saved selected device '" + deviceName + "' to preferences.");
    }

    private String loadSavedDeviceName() {
        return prefs.get(PREF_OUTPUT_DEVICE_KEY, null);
    }

    @Override
    public String getSelectedDeviceName() {
        return selectedOutputMixer != null ? selectedOutputMixer.getMixerInfo().getName() : null;
    }
}