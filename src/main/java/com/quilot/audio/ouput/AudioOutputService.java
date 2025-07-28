package com.quilot.audio.ouput;

import javax.sound.sampled.AudioFormat;
import java.util.List;

/**
 * Defines the contract for an audio output service.
 * This interface abstracts the details of audio device management,
 * volume control, and sound playback, adhering to the Dependency Inversion Principle.
 */
public interface AudioOutputService {

    /**
     * Retrieves a list of names of available audio output devices on the system.
     * @return A list of device names.
     */
    List<String> getAvailableOutputDevices();

    /**
     * Selects and initializes the specified audio output device.
     * If a device is already selected and open, it will be closed first.
     * @param deviceName The name of the audio device to select.
     * @return true if the device was successfully selected and opened, false otherwise.
     */
    boolean selectOutputDevice(String deviceName);

    /**
     * Sets the master volume for the currently selected audio output device.
     * The volume is a float value between 0.0 (minimum) and 1.0 (maximum).
     * If no device is selected or volume control is not supported, this method does nothing.
     * @param volume A float value representing the desired volume (0.0 to 1.0).
     */
    void setVolume(float volume);

    /**
     * Plays a short test sound through the currently selected audio output device.
     * If no device is selected or the line is not open, the sound will not play.
     */
    void playTestSound();

    /**
     * Plays raw audio data through the currently selected audio output device.
     * @param audioData The byte array containing the audio data to play.
     * @param format The AudioFormat of the provided audio data.
     */
    void playAudioData(byte[] audioData, AudioFormat format);

    /**
     * Closes the currently open audio output line and releases associated resources.
     * This should be called when the application is shutting down or the audio
     * output is no longer needed.
     */
    void close();

    /**
     * Checks if an audio output device is currently selected and its line is open.
     * @return true if a device is selected and open, false otherwise.
     */
    boolean isDeviceSelected();
}
