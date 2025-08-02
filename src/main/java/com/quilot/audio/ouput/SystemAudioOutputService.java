package com.quilot.audio.ouput;

import com.quilot.exceptions.audio.AudioDeviceException;
import com.quilot.exceptions.audio.AudioException;
import com.quilot.utils.Logger;
import lombok.Getter;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

@Getter
public class SystemAudioOutputService implements AudioOutputService {

    private static final String PREF_NODE_NAME = "com/quilot/audio";
    private static final String PREF_OUTPUT_DEVICE_KEY = "selectedOutputDevice";
    private static final AudioFormat DEFAULT_AUDIO_FORMAT = new AudioFormat(44100, 16, 1, true, false);

    private final Preferences prefs;
    private Mixer selectedOutputMixer;
    private SourceDataLine outputLine;
    private FloatControl masterGainControl;

    public SystemAudioOutputService() {
        try {
            this.prefs = Preferences.userRoot().node(PREF_NODE_NAME);
        } catch (SecurityException e) {
            Logger.error("Could not access preferences due to security policy.", e);
            throw new RuntimeException("Failed to initialize audio service due to security restrictions.", e);
        }

        String savedDeviceName = loadSavedDeviceName();
        if (savedDeviceName != null) {
            Logger.info("Found saved audio device: " + savedDeviceName + ". Attempting to select it.");
            try {
                selectOutputDevice(savedDeviceName);
            } catch (AudioDeviceException e) {
                Logger.warn("Failed to select saved audio device '" + savedDeviceName + "': " + e.getMessage());
            }
        }
    }

    @Override
    public List<String> getAvailableOutputDevices() {
        return AudioDeviceDiscoverer.discoverOutputDevices();
    }

    @Override
    public void selectOutputDevice(String deviceName) throws AudioDeviceException {
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixerInfos) {
            if (info.getName().equals(deviceName)) {
                Mixer mixer = AudioSystem.getMixer(info);
                if (mixer.isLineSupported(new Line.Info(SourceDataLine.class))) {
                    configureDevice(mixer, deviceName);
                    selectedOutputMixer = mixer;
                    saveSelectedDeviceName(deviceName);
                    return;
                }
            }
        }
        throw new AudioDeviceException("Audio output device not found or not supported: " + deviceName);
    }

    private void configureDevice(Mixer mixer, String deviceName) throws AudioDeviceException {
        closeOutputLine();
        try {
            outputLine = (SourceDataLine) mixer.getLine(new DataLine.Info(SourceDataLine.class, DEFAULT_AUDIO_FORMAT));
            outputLine.open(DEFAULT_AUDIO_FORMAT);
        } catch (IllegalArgumentException | LineUnavailableException e) {
            Logger.warn("Default format not supported by '" + deviceName + "'. Searching for a compatible format.");

            AudioFormat bestFormat = findBestOutputFormat(mixer);
            if (bestFormat == null) {
                throw new AudioDeviceException("No compatible output format found for device: " + deviceName);
            }
            try {
                outputLine = (SourceDataLine) mixer.getLine(new DataLine.Info(SourceDataLine.class, bestFormat));
                outputLine.open(bestFormat);
                Logger.info("Using fallback format: " + bestFormat);
            } catch (LineUnavailableException | SecurityException ex) {
                throw new AudioDeviceException("Failed to open line for '" + deviceName + "' even with fallback format.", ex);
            }
        }

        outputLine.start();
        initializeVolumeControl();
    }

    private AudioFormat findBestOutputFormat(Mixer mixer) {
        AudioFormat bestMatch = null;
        float bestScore = -1f;

        for (Line.Info lineInfo : mixer.getSourceLineInfo()) {
            if (lineInfo instanceof DataLine.Info) {
                for (AudioFormat format : ((DataLine.Info) lineInfo).getFormats()) {

                    if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED || format.getSampleSizeInBits() < 16) {
                        continue;
                    }

                    float sampleRateScore = 1.0f - (Math.abs(format.getSampleRate() - DEFAULT_AUDIO_FORMAT.getSampleRate()) / DEFAULT_AUDIO_FORMAT.getSampleRate());
                    if (sampleRateScore > bestScore) {
                        bestScore = sampleRateScore;
                        bestMatch = format;
                    }
                }
            }
        }

        return bestMatch;
    }

    @Override
    public void setVolume(float volume) {
        if (masterGainControl != null) {
            float clampedVolume = Math.max(0.0f, Math.min(1.0f, volume));

            float min = masterGainControl.getMinimum();
            float max = masterGainControl.getMaximum();
            float range = max - min;

            float gain = min + (range * clampedVolume);
            masterGainControl.setValue(gain);
        } else {
            Logger.warn("Volume control unavailable; cannot set volume.");
        }
    }

    @Override
    public void playTestSound() throws AudioDeviceException {
        ensureDeviceIsReady();

        Logger.info("Playing test tone (440Hz, 500ms).");
        try {
            byte[] tone = AudioToneGenerator.generateSineWave(outputLine.getFormat(), 440, 500);
            outputLine.write(tone, 0, tone.length);
            outputLine.drain();
            Logger.info("Test tone playback complete.");
        } catch (UnsupportedOperationException e) {
            throw new AudioDeviceException("Could not generate test tone for the current audio format.", e);
        }
    }

    @Override
    public void playAudioData(byte[] audioData, AudioFormat format) throws AudioException {
        ensureDeviceIsReady();

        AudioFormat targetFormat = outputLine.getFormat();
        try (AudioInputStream sourceStream = new AudioInputStream(new ByteArrayInputStream(audioData), format, audioData.length / format.getFrameSize())) {
            AudioInputStream playbackStream = sourceStream;
            if (!format.matches(targetFormat)) {
                if (AudioSystem.isConversionSupported(targetFormat, format)) {
                    playbackStream = AudioSystem.getAudioInputStream(targetFormat, sourceStream);
                } else {
                    throw new AudioException("Audio format conversion from " + format + " to " + targetFormat + " is not supported.");
                }
            }

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = playbackStream.read(buffer)) != -1) {
                outputLine.write(buffer, 0, bytesRead);
            }
            outputLine.drain();
        } catch (IOException e) {
            throw new AudioException("Failed to read audio data for playback.", e);
        }
    }

    @Override
    public void close() {
        closeOutputLine();
        Logger.info("SystemAudioOutputService closed.");
    }

    private void closeOutputLine() {
        if (outputLine != null) {
            outputLine.stop();
            outputLine.flush();
            outputLine.close();
            outputLine = null;
        }
    }

    private void initializeVolumeControl() {
        if (outputLine != null && outputLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            masterGainControl = (FloatControl) outputLine.getControl(FloatControl.Type.MASTER_GAIN);
        } else {
            masterGainControl = null;
            Logger.warn("Master Gain control not supported by current output line.");
        }
    }

    @Override
    public String getSelectedDeviceName() {
        return selectedOutputMixer != null ? selectedOutputMixer.getMixerInfo().getName() : null;
    }

    private void saveSelectedDeviceName(String deviceName) {
        try {
            prefs.put(PREF_OUTPUT_DEVICE_KEY, deviceName);
            prefs.flush();
        } catch (BackingStoreException e) {
            Logger.error("Failed to save selected output device to preferences.", e);
        }
    }

    private String loadSavedDeviceName() {
        return prefs.get(PREF_OUTPUT_DEVICE_KEY, null);
    }

    private void ensureDeviceIsReady() throws AudioDeviceException {
        if (outputLine == null || !outputLine.isOpen()) {
            throw new AudioDeviceException("No audio output device is selected or the line is not open.");
        }
    }
}