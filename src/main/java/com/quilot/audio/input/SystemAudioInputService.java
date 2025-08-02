package com.quilot.audio.input;

import com.quilot.utils.Logger;
import lombok.Getter;
import lombok.Setter;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;

@Getter
@Setter
public class SystemAudioInputService implements AudioInputService {

    private static final String PREF_NODE_NAME = "com/quilot/audio";
    private static final String PREF_INPUT_DEVICE_KEY = "selectedInputDevice";

    private final Preferences prefs;

    private static final AudioFormat DEFAULT_AUDIO_FORMAT = new AudioFormat(
            44100,
            16,
            1,
            true,
            false);

    private static final int JOIN_TIMEOUT_MS = 1000;

    private final AtomicBoolean isRecording = new AtomicBoolean(false);
    private final ByteArrayOutputStream recordedAudioBuffer = new ByteArrayOutputStream();

    private AudioFormat audioFormat = DEFAULT_AUDIO_FORMAT;
    private TargetDataLine targetDataLine;
    private Mixer selectedInputMixer; // Added to keep track of the selected mixer
    private Thread captureThread;
    private AudioDataListener audioDataListener;

    public SystemAudioInputService() {
        this.prefs = Preferences.userRoot().node(PREF_NODE_NAME);
        Logger.info("SystemAudioInputService initialized. Preferences node: " + PREF_NODE_NAME);

        // Load the saved device on startup
        String savedDeviceName = loadSavedDeviceName();
        if (savedDeviceName != null) {
            Logger.info("Found saved audio input device: " + savedDeviceName + ". Attempting to select it.");
            selectInputDevice(savedDeviceName);
        }
    }

    @Override
    public List<String> getAvailableInputDevices() {
        List<String> deviceNames = new ArrayList<>();
        for (Mixer.Info info : AudioSystem.getMixerInfo()) {
            try {
                Mixer mixer = AudioSystem.getMixer(info);
                if (mixer.isLineSupported(new Line.Info(TargetDataLine.class))) {
                    deviceNames.add(info.getName());
                }
            } catch (IllegalArgumentException e) {
                Logger.warn("Unsupported mixer: " + info.getName() + " - " + e.getMessage());
            }
        }
        logDeviceDiscoveryResult(deviceNames);
        return deviceNames;
    }

    @Override
    public boolean selectInputDevice(String deviceName) {
        for (Mixer.Info info : AudioSystem.getMixerInfo()) {
            if (info.getName().equals(deviceName)) {
                Mixer mixer = AudioSystem.getMixer(info);
                if (configureDevice(mixer, deviceName)) {
                    // Save the successfully selected device to preferences
                    selectedInputMixer = mixer;
                    saveSelectedDeviceName(deviceName);
                    return true;
                }
            }
        }
        Logger.warn("Input device not found or unsupported: " + deviceName);
        targetDataLine = null;
        selectedInputMixer = null;
        return false;
    }

    @Override
    public boolean startRecording() {
        if (!validateRecordingStart()) return false;

        clearRecordedAudioData();
        isRecording.set(true);
        targetDataLine.start();

        captureThread = new Thread(this::captureAudioLoop, "AudioCaptureThread");
        captureThread.start();
        Logger.info("Recording started.");
        return true;
    }

    @Override
    public boolean stopRecording() {
        if (!isRecording.get()) {
            Logger.warn("Recording not active.");
            return false;
        }

        isRecording.set(false);
        stopAndJoinCaptureThread();
        Logger.info("Recording stopped.");
        return true;
    }

    @Override
    public void close() {
        stopRecording();
        closeInputLine();
        Logger.info("SystemAudioInputService resources released.");
    }

    @Override
    public String getSelectedDeviceName() {
        return selectedInputMixer != null ? selectedInputMixer.getMixerInfo().getName() : null;
    }

    @Override
    public boolean isDeviceSelected() {
        return targetDataLine != null && targetDataLine.isOpen();
    }

    @Override
    public void setAudioDataListener(AudioDataListener listener) {
        this.audioDataListener = listener;
    }

    @Override
    public byte[] getRecordedAudioData() {
        return recordedAudioBuffer.toByteArray();
    }

    @Override
    public void clearRecordedAudioData() {
        recordedAudioBuffer.reset();
        Logger.info("Recorded audio buffer cleared.");
    }

    // PRIVATE HELPERS

    private boolean configureDevice(Mixer mixer, String deviceName) {
        closeInputLine();
        try {
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);

            if (!mixer.isLineSupported(dataLineInfo)) {
                Logger.warn("Default format unsupported on " + deviceName);
                audioFormat = findCompatibleFormat(mixer);
                if (audioFormat == null) {
                    Logger.error("No compatible format found for device: " + deviceName);
                    return false;
                }
                Logger.warn("Using fallback format: " + audioFormat);
            }

            targetDataLine = (TargetDataLine) mixer.getLine(new DataLine.Info(TargetDataLine.class, audioFormat));
            targetDataLine.open(audioFormat);
            Logger.info("Selected input device: " + deviceName);
            return true;

        } catch (LineUnavailableException | IllegalArgumentException e) {
            Logger.error("Failed to open audio line for '" + deviceName + "': " + e.getMessage());
            return false;
        }
    }

    private AudioFormat findCompatibleFormat(Mixer mixer) {
        for (Line.Info info : mixer.getTargetLineInfo()) {
            if (info instanceof DataLine.Info dli) {
                for (AudioFormat format : dli.getFormats()) {
                    if (format.matches(DEFAULT_AUDIO_FORMAT)) {
                        return DEFAULT_AUDIO_FORMAT;
                    }
                    if (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED && format.getSampleSizeInBits() == 16) {
                        return format;
                    }
                }
            }
        }
        return null;
    }

    private void closeInputLine() {
        if (targetDataLine != null && targetDataLine.isOpen()) {
            targetDataLine.close();
            Logger.info("Input line closed.");
        }
    }

    private boolean validateRecordingStart() {
        if (targetDataLine == null || !targetDataLine.isOpen()) {
            Logger.warn("Cannot start recording: no input device selected.");
            return false;
        }
        if (isRecording.get()) {
            Logger.warn("Recording already in progress.");
            return true;
        }
        return true;
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
                Logger.error("Interrupted while stopping capture thread: " + e.getMessage());
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
        prefs.put(PREF_INPUT_DEVICE_KEY, deviceName);
        Logger.info("Saved selected input device '" + deviceName + "' to preferences.");
    }

    private String loadSavedDeviceName() {
        return prefs.get(PREF_INPUT_DEVICE_KEY, null);
    }
}
