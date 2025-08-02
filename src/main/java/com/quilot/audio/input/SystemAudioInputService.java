package com.quilot.audio.input;

import com.quilot.exceptions.audio.AudioDeviceException;
import com.quilot.utils.Logger;
import lombok.Getter;
import lombok.Setter;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * A concrete implementation of {@link AudioInputService} that uses the Java Sound API
 * to interact with the system's audio input devices.
 * <p>
 * This class manages device discovery, selection, recording, and format negotiation.
 * It also persists the user's last selected device using Java Preferences.
 */
@Getter
@Setter
public class SystemAudioInputService implements AudioInputService {

    private static final String PREF_NODE_NAME = "com/quilot/audio";
    private static final String PREF_INPUT_DEVICE_KEY = "selectedInputDevice";

    private final Preferences prefs;
    private static final AudioFormat DEFAULT_AUDIO_FORMAT = new AudioFormat(44100, 16, 1, true, false);
    private static final int JOIN_TIMEOUT_MS = 1000;
    private final AtomicBoolean isRecording = new AtomicBoolean(false);
    private final ByteArrayOutputStream recordedAudioBuffer = new ByteArrayOutputStream();

    private AudioFormat audioFormat = DEFAULT_AUDIO_FORMAT;
    private TargetDataLine targetDataLine;
    private Mixer selectedInputMixer;
    private Thread captureThread;
    private AudioDataListener audioDataListener;

    /**
     * Constructs a new SystemAudioInputService.
     * Initializes Java Preferences and attempts to select the last used audio device.
     *
     * @throws RuntimeException if the Java Preferences API cannot be accessed due to security restrictions.
     */
    public SystemAudioInputService() {
        try {
            this.prefs = Preferences.userRoot().node(PREF_NODE_NAME);
        } catch (SecurityException e) {
            Logger.error("Could not access preferences due to security policy.", e);
            throw new RuntimeException("Failed to initialize audio service due to security restrictions.", e);
        }

        String savedDeviceName = loadSavedDeviceName();
        if (savedDeviceName != null) {
            Logger.info("Found saved audio input device: " + savedDeviceName + ". Attempting to select it.");
            try {
                selectInputDevice(savedDeviceName);
            } catch (AudioDeviceException e) {
                Logger.warn("Failed to select saved audio device '" + savedDeviceName + "'. It may be disconnected. " + e.getMessage());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAvailableInputDevices() {
        List<String> deviceNames = new ArrayList<>();
        for (Mixer.Info info : AudioSystem.getMixerInfo()) {
            try {
                Mixer mixer = AudioSystem.getMixer(info);
                if (mixer.isLineSupported(new Line.Info(TargetDataLine.class))) {
                    deviceNames.add(info.getName());
                }
            } catch (IllegalArgumentException | SecurityException e) {
                Logger.warn("Could not query mixer: " + info.getName() + " - " + e.getMessage());
            }
        }
        logDeviceDiscoveryResult(deviceNames);
        return deviceNames;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectInputDevice(String deviceName) throws AudioDeviceException {
        for (Mixer.Info info : AudioSystem.getMixerInfo()) {
            if (info.getName().equals(deviceName)) {
                Mixer mixer = AudioSystem.getMixer(info);
                configureDevice(mixer, deviceName);
                selectedInputMixer = mixer;
                saveSelectedDeviceName(deviceName);
                return; // Success
            }
        }
        throw new AudioDeviceException("Audio input device not found: " + deviceName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startRecording() throws AudioDeviceException {
        if (targetDataLine == null || !targetDataLine.isOpen()) {
            throw new AudioDeviceException("Cannot start recording: no input device selected or line is not open.");
        }
        if (isRecording.get()) {
            Logger.warn("Recording already in progress.");
            return;
        }

        clearRecordedAudioData();
        isRecording.set(true);
        targetDataLine.start();

        captureThread = new Thread(this::captureAudioLoop, "AudioCaptureThread");
        captureThread.start();
        Logger.info("Recording started.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean stopRecording() {
        if (!isRecording.getAndSet(false)) {
            Logger.warn("Recording not active.");
            return false;
        }
        stopAndJoinCaptureThread();
        Logger.info("Recording stopped.");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        stopRecording();
        closeInputLine();
        Logger.info("SystemAudioInputService resources released.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSelectedDeviceName() {
        return selectedInputMixer != null ? selectedInputMixer.getMixerInfo().getName() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDeviceSelected() {
        return targetDataLine != null && targetDataLine.isOpen();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAudioDataListener(AudioDataListener listener) {
        this.audioDataListener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getRecordedAudioData() {
        return recordedAudioBuffer.toByteArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearRecordedAudioData() {
        recordedAudioBuffer.reset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AudioFormat getAudioFormat() {
        return this.audioFormat;
    }

    private void configureDevice(Mixer mixer, String deviceName) throws AudioDeviceException {
        closeInputLine();

        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, DEFAULT_AUDIO_FORMAT);
            targetDataLine = (TargetDataLine) mixer.getLine(info);
            targetDataLine.open(DEFAULT_AUDIO_FORMAT);
            this.audioFormat = DEFAULT_AUDIO_FORMAT;
            Logger.info("Successfully opened device '" + deviceName + "' with default format.");
            return;
        } catch (IllegalArgumentException e) {
            Logger.warn("Default format not supported by '" + deviceName + "'. Searching for a compatible format.");
        } catch (LineUnavailableException | SecurityException e) {
            throw new AudioDeviceException("Cannot open audio line for '" + deviceName + "'", e);
        }

        AudioFormat compatibleFormat = findBestCompatibleFormat(mixer);
        if (compatibleFormat == null) {
            throw new AudioDeviceException("No compatible audio format found for device: " + deviceName);
        }

        try {
            Logger.warn("Using fallback format for '" + deviceName + "': " + compatibleFormat);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, compatibleFormat);
            targetDataLine = (TargetDataLine) mixer.getLine(info);
            targetDataLine.open(compatibleFormat);
            this.audioFormat = compatibleFormat;
        } catch (LineUnavailableException | SecurityException | IllegalArgumentException e) {
            throw new AudioDeviceException("Failed to open audio line for '" + deviceName + "' even with a fallback format.", e);
        }
    }

    private AudioFormat findBestCompatibleFormat(Mixer mixer) {
        AudioFormat bestMatch = null;
        float bestScore = -1f;

        for (Line.Info lineInfo : mixer.getTargetLineInfo()) {
            if (lineInfo instanceof DataLine.Info) {
                for (AudioFormat format : ((DataLine.Info) lineInfo).getFormats()) {
                    if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED || format.getSampleSizeInBits() != 16) {
                        continue;
                    }

                    float sampleRateScore = 1.0f - (Math.abs(format.getSampleRate() - DEFAULT_AUDIO_FORMAT.getSampleRate()) / DEFAULT_AUDIO_FORMAT.getSampleRate());
                    float channelScore = format.getChannels() == DEFAULT_AUDIO_FORMAT.getChannels() ? 1.0f : 0.5f;
                    float currentScore = sampleRateScore * 0.8f + channelScore * 0.2f;

                    if (currentScore > bestScore) {
                        bestScore = currentScore;
                        bestMatch = format;
                    }
                }
            }
        }
        return bestMatch;
    }

    private void closeInputLine() {
        if (targetDataLine != null && targetDataLine.isOpen()) {
            targetDataLine.close();
        }
    }

    private void stopAndJoinCaptureThread() {
        if (targetDataLine != null) {
            targetDataLine.stop();
        }
        if (captureThread != null) {
            try {
                captureThread.join(JOIN_TIMEOUT_MS);
                if (captureThread.isAlive()) {
                    Logger.warn("Audio capture thread did not terminate gracefully.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Logger.error("Interrupted while stopping capture thread.", e);
            }
        }
    }

    private void captureAudioLoop() {
        Logger.info("Audio capture thread started.");
        int bufferSize = (int) audioFormat.getSampleRate() * audioFormat.getFrameSize();
        byte[] buffer = new byte[bufferSize];

        while (isRecording.get()) {
            int bytesRead = targetDataLine.read(buffer, 0, buffer.length);
            if (bytesRead > 0) {
                recordedAudioBuffer.write(buffer, 0, bytesRead);
                notifyAudioListener(buffer, bytesRead);
            }
        }
        Logger.info("Audio capture thread stopped.");
    }

    private void notifyAudioListener(byte[] buffer, int bytesRead) {
        if (audioDataListener != null) {
            byte[] capturedData = new byte[bytesRead];
            System.arraycopy(buffer, 0, capturedData, 0, bytesRead);
            audioDataListener.onAudioDataCaptured(capturedData, bytesRead);
        }
    }

    private void logDeviceDiscoveryResult(List<String> devices) {
        if (devices.isEmpty()) {
            Logger.warn("No audio input devices found.");
        } else {
            Logger.info("Found " + devices.size() + " audio input device(s).");
        }
    }

    private void saveSelectedDeviceName(String deviceName) {
        try {
            prefs.put(PREF_INPUT_DEVICE_KEY, deviceName);
            prefs.flush();
            Logger.info("Saved selected input device '" + deviceName + "' to preferences.");
        } catch (BackingStoreException e) {
            Logger.error("Failed to save selected device to preferences.", e);
        }
    }

    private String loadSavedDeviceName() {
        return prefs.get(PREF_INPUT_DEVICE_KEY, null);
    }
}