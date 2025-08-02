package com.quilot.audio.input;

import javax.sound.sampled.AudioFormat;
import java.util.List;

/**
 * Defines the contract for an audio input service.
 * This interface abstracts the details of audio device management and recording,
 * adhering to the Dependency Inversion Principle.
 */
public interface AudioInputService {

    /**
     * Retrieves a list of names of available audio input devices on the system.
     * This includes microphones and potentially "stereo mix" or "what you hear"
     * options for capturing system output.
     * @return A list of device names.
     */
    List<String> getAvailableInputDevices();

    /**
     * Selects and initializes the specified audio input device for recording.
     * If a device is already selected and open, it will be closed first.
     * @param deviceName The name of the audio device to select.
     * @return true if the device was successfully selected and opened, false otherwise.
     */
    boolean selectInputDevice(String deviceName);

    /**
     * Starts recording audio from the currently selected input device.
     * @return true if recording successfully started, false otherwise.
     */
    boolean startRecording();

    /**
     * Stops recording audio from the current input device.
     * @return true if recording successfully stopped, false otherwise.
     */
    boolean stopRecording();

    /**
     * Closes the currently open audio input line and releases associated resources.
     * This should be called when the application is shutting down or the audio
     * input is no longer needed.
     */
    void close();

    /**
     * Checks if an audio input device is currently selected and its line is open.
     * @return true if a device is selected and open, false otherwise.
     */
    boolean isDeviceSelected();

    /**
     * Sets a listener to receive captured audio data.
     * @param listener The listener to set.
     */
    void setAudioDataListener(AudioDataListener listener);

    /**
     * Retrieves the currently recorded audio data as a byte array.
     * This data is typically accumulated since the last call to startRecording()
     * or clearRecordedAudioData().
     * @return A byte array containing the recorded audio data. Returns an empty array if no data.
     */
    byte[] getRecordedAudioData();

    /**
     * Clears any accumulated recorded audio data.
     */
    void clearRecordedAudioData();

    /**
     * Gets the AudioFormat currently used by the input line.
     * @return The AudioFormat of the input line, or null if no line is open.
     */
    AudioFormat getAudioFormat();

    /**
     * Gets the name of the currently selected audio input device.
     * @return The device name, or null if no device is selected.
     */
    String getSelectedDeviceName();

    /**
     * Functional interface for receiving captured audio data.
     */
    @FunctionalInterface
    interface AudioDataListener {
        void onAudioDataCaptured(byte[] audioData, int bytesRead);
    }
}
