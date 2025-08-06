package com.quilot.audio.ouput;

import com.quilot.exceptions.audio.AudioDeviceException;
import com.quilot.exceptions.audio.AudioException;
import com.quilot.utils.Logger;
import lombok.Getter;

import javax.sound.sampled.*;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * A concrete implementation of {@link AudioOutputService} that uses the Java Sound API
 * to interact with the system's audio output devices.
 * <p>
 * This class manages device discovery, selection, volume control, and playback of raw audio data.
 * It persists the user's last selected device using Java Preferences.
 */
@Getter
public class SystemAudioOutputService implements AudioOutputService {

    private static final String PREF_NODE_NAME = "com/quilot/audio";
    private static final String PREF_OUTPUT_DEVICE_KEY = "selectedOutputDevice";
    private static final AudioFormat DEFAULT_AUDIO_FORMAT = new AudioFormat(44100, 16, 1, true, false);

    private final Preferences prefs;
    private Mixer selectedOutputMixer;
    private SourceDataLine outputLine;
    private FloatControl masterGainControl;
    private SourceDataLine activePlaybackLine;

    /**
     * Constructs a new SystemAudioOutputService.
     * Initializes Java Preferences and attempts to select the last used audio device.
     *
     * @throws RuntimeException if the Java Preferences API cannot be accessed due to security restrictions.
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAvailableOutputDevices() {
        return AudioDeviceDiscoverer.discoverOutputDevices();
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void playAudioData(byte[] audioData, AudioFormat format) throws AudioException {
        if (selectedOutputMixer == null) {
            throw new AudioDeviceException("No audio output device selected.");
        }

        // Re-open the line to ensure a fresh state for every playback.
        try {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            if (!selectedOutputMixer.isLineSupported(info)) {
                throw new AudioException("The selected device does not support the audio format: " + format);
            }
            activePlaybackLine = (SourceDataLine) selectedOutputMixer.getLine(info);
            activePlaybackLine.open(format);
        } catch (LineUnavailableException e) {
            throw new AudioDeviceException("Audio line is unavailable. It may be in use by another application.", e);
        }

        try {
            activePlaybackLine.start();
            activePlaybackLine.write(audioData, 0, audioData.length);
            activePlaybackLine.drain(); // This will now block correctly.
        } finally {
            // Always ensure the line is stopped and closed after playback.
            stopPlayback();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void stopPlayback() {
        if (activePlaybackLine != null) {
            Logger.info("Stopping and closing active playback line.");
            activePlaybackLine.stop();
            activePlaybackLine.flush();
            activePlaybackLine.close();
            activePlaybackLine = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        stopPlayback(); // Ensure any active line is closed on shutdown.
        Logger.info("SystemAudioOutputService resources released.");
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

    /**
     * {@inheritDoc}
     */
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