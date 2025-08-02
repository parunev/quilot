package com.quilot.audio.input;

import com.quilot.exceptions.audio.AudioDeviceException;

import javax.sound.sampled.AudioFormat;
import java.util.List;

/**
 * Defines the contract for an audio input service.
 * This interface abstracts the details of audio device management and recording.
 */
public interface AudioInputService {

    /**
     * Retrieves a list of names of available audio input devices (e.g., microphones).
     * @return A {@link List} of device names. Returns an empty list if no devices are found.
     */
    List<String> getAvailableInputDevices();

    /**
     * Selects and initializes the specified audio input device for recording.
     * If another device is already open, it will be closed first.
     *
     * @param deviceName The name of the audio device to select.
     * @throws AudioDeviceException if the device cannot be found, opened, or configured.
     */
    void selectInputDevice(String deviceName) throws AudioDeviceException;

    /**
     * Starts recording audio from the currently selected input device.
     *
     * @throws AudioDeviceException if no device is selected or the device line cannot be started.
     */
    void startRecording() throws AudioDeviceException;

    /**
     * Stops recording audio from the current input device.
     *
     * @return true if recording was active and successfully stopped, false otherwise.
     */
    boolean stopRecording();

    /**
     * Closes the currently open audio input line and releases its resources.
     * This should be called on application shutdown.
     */
    void close();

    /**
     * Checks if an audio input device is currently selected and its line is open.
     * @return true if a device is selected and open, false otherwise.
     */
    boolean isDeviceSelected();

    /**
     * Sets a listener to receive captured audio data in real-time.
     *
     * @param listener The listener to be notified with audio data.
     */
    void setAudioDataListener(AudioDataListener listener);

    /**
     * Retrieves all audio data recorded since recording was last started.
     *
     * @return A byte array containing the recorded audio data. Returns an empty array if no data was captured.
     */
    byte[] getRecordedAudioData();

    /**
     * Clears any accumulated recorded audio data from the internal buffer.
     */
    void clearRecordedAudioData();

    /**
     * Gets the {@link AudioFormat} currently used by the input line.
     *
     * @return The AudioFormat of the input line, or null if no line is open.
     */
    AudioFormat getAudioFormat();

    /**
     * Gets the name of the currently selected audio input device.
     *
     * @return The device name, or null if no device is selected.
     */
    String getSelectedDeviceName();

    /**
     * A functional interface for a listener that receives captured audio data.
     */
    @FunctionalInterface
    interface AudioDataListener {

        /**
         * Called when a chunk of audio data is captured from the input device.
         *
         * @param audioData The byte array containing the captured audio data.
         * @param bytesRead The number of bytes read into the buffer.
         */
        void onAudioDataCaptured(byte[] audioData, int bytesRead);
    }
}
