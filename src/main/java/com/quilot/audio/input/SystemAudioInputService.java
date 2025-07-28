package com.quilot.audio.input;

import com.quilot.utils.Logger;
import lombok.Getter;
import lombok.Setter;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Concrete implementation of AudioInputService that interacts with the Java Sound API
 * to capture audio from an input device (e.g., microphone, stereo mix).
 * This class handles device discovery, recording, and providing captured audio data.
 */
@Getter
@Setter
public class SystemAudioInputService implements AudioInputService{

    private TargetDataLine targetDataLine;
    private AudioFormat audioFormat;
    private Thread captureThread;
    private final AtomicBoolean isRecording = new AtomicBoolean(false);
    private AudioDataListener audioDataListener;
    private final ByteArrayOutputStream recordedAudioBuffer;

    // Default audio format for capturing audio
    // 44.1kHz, 16-bit, mono, signed, little-endian
    private static final AudioFormat DEFAULT_AUDIO_FORMAT = new AudioFormat(44100, 16, 1, true, false);

    public SystemAudioInputService() {
        Logger.info("SystemAudioInputService initialized.");
        this.audioFormat = DEFAULT_AUDIO_FORMAT; // Initialize with default format
        this.recordedAudioBuffer = new ByteArrayOutputStream(); // Initialize buffer
    }

    @Override
    public List<String> getAvailableInputDevices() {
        List<String> deviceNames = new ArrayList<>();
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixerInfos) {
            Mixer mixer = AudioSystem.getMixer(info);
            try {
                if (mixer.isLineSupported(new Line.Info(TargetDataLine.class))) {
                    deviceNames.add(info.getName());
                }
            } catch (IllegalArgumentException e) {
                Logger.warn("Mixer " + info.getName() + " does not support TargetDataLine as expected: " + e.getMessage());
            }
        }
        if (deviceNames.isEmpty()) {
            Logger.warn("No audio input devices found.");
        } else {
            Logger.info("Found " + deviceNames.size() + " audio input devices.");
        }
        return deviceNames;
    }

    @Override
    public boolean selectInputDevice(String deviceName) {
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixerInfos) {
            if (info.getName().equals(deviceName)) {
                try {
                    closeInputLine(); // Close any previously open line

                    Mixer mixer = AudioSystem.getMixer(info);
                    DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);

                    if (!mixer.isLineSupported(dataLineInfo)) {
                        Logger.error("Selected mixer '" + deviceName + "' does not support the default audio format: " + audioFormat);

                        // Fallback: try to find a compatible format
                        // Get all supported target line infos
                        Line.Info[] supportedLineInfos = mixer.getTargetLineInfo();
                        AudioFormat foundFormat = null;

                        for (Line.Info lineInfo : supportedLineInfos) {
                            if (lineInfo instanceof DataLine.Info dli) {
                                for (AudioFormat format : dli.getFormats()) {

                                    // Prioritize the default format if it's found among supported ones
                                    if (format.matches(DEFAULT_AUDIO_FORMAT)) {
                                        foundFormat = format;
                                        break;
                                    }

                                    // Otherwise, just take the first compatible format that is PCM_SIGNED and 16-bit
                                    if (foundFormat == null && format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED && format.getSampleSizeInBits() == 16) {
                                        foundFormat = format;
                                    }
                                }
                            }
                            if (foundFormat != null) break;
                        }

                        if (foundFormat != null) {
                            Logger.warn("Attempting to use a compatible format for input: " + foundFormat);
                            audioFormat = foundFormat; // Update the format
                            targetDataLine = (TargetDataLine) mixer.getLine(new DataLine.Info(TargetDataLine.class, audioFormat));
                            targetDataLine.open(audioFormat);
                        } else {
                            Logger.error("No compatible formats found for input device: " + deviceName);
                            targetDataLine = null;
                            return false;
                        }
                    } else {
                        targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);
                        targetDataLine.open(audioFormat);
                    }

                    Logger.info("Selected audio input device: " + deviceName);
                    return true;
                } catch (LineUnavailableException e) {
                    Logger.error("Audio line unavailable for input device '" + deviceName + "': " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    Logger.error("Invalid argument when selecting input device '" + deviceName + "': " + e.getMessage());
                } catch (Exception e) {
                    Logger.error("An unexpected error occurred while selecting input device '" + deviceName + "': " + e.getMessage());
                }
            }
        }
        Logger.warn("Could not select audio input device: " + deviceName + ". Device not found or unsupported.");
        targetDataLine = null;
        return false;
    }

    @Override
    public boolean startRecording() {
        if (targetDataLine == null || !targetDataLine.isOpen()) {
            Logger.warn("No input device selected or line not open to start recording.");
            return false;
        }
        if (isRecording.get()) {
            Logger.warn("Recording is already in progress.");
            return true;
        }

        clearRecordedAudioData(); // Clear previous recording before starting new one
        isRecording.set(true);
        targetDataLine.start();

        captureThread = new Thread(() -> {
            Logger.info("Audio capture thread started.");
            int bufferSize = (int) audioFormat.getSampleRate() * audioFormat.getFrameSize();
            byte[] buffer = new byte[bufferSize];

            while (isRecording.get()) {
                int bytesRead = targetDataLine.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    recordedAudioBuffer.write(buffer, 0, bytesRead);
                    if (audioDataListener != null) {
                        byte[] capturedData = new byte[bytesRead];
                        System.arraycopy(buffer, 0, capturedData, 0, bytesRead);
                        audioDataListener.onAudioDataCaptured(capturedData, bytesRead);
                    }
                }
            }
            Logger.info("Audio capture thread stopped.");
        }, "AudioCaptureThread");
        captureThread.start();
        Logger.info("Recording started from input device.");
        return true;
    }

    @Override
    public boolean stopRecording() {
        if (!isRecording.get()) {
            Logger.warn("No recording in progress to stop.");
            return false;
        }
        isRecording.set(false);
        if (targetDataLine != null) {
            targetDataLine.stop();
        }
        if (captureThread != null) {
            try {
                captureThread.join(1000);
                if (captureThread.isAlive()) {
                    Logger.warn("Audio capture thread did not terminate gracefully.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Logger.error("Interruption while waiting for audio capture thread to stop: " + e.getMessage());
            }
        }
        Logger.info("Recording stopped from input device.");
        return true;
    }

    @Override
    public void close() {
        stopRecording(); // Ensure recording is stopped before closing line
        closeInputLine();
        Logger.info("SystemAudioInputService resources released.");
    }

    /**
     * Helper method to safely close the current input line if it's open.
     */
    private void closeInputLine() {
        if (targetDataLine != null && targetDataLine.isOpen()) {
            targetDataLine.close();
            Logger.info("Input line closed.");
        }
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
        recordedAudioBuffer.reset(); // Resets the buffer, effectively clearing it
        Logger.info("Recorded audio data buffer cleared.");
    }

    @Override
    public AudioFormat getAudioFormat() {
        return audioFormat;
    }
}
