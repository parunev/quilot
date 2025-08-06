package com.quilot.audio.ouput;

import com.quilot.exceptions.audio.AudioDeviceException;
import com.quilot.exceptions.audio.AudioException;

import javax.sound.sampled.AudioFormat;
import java.util.List;

/**
 * Defines the contract for an audio output service.
 * This interface abstracts the details of audio device management, volume control, and sound playback.
 */
public interface AudioOutputService {

    /**
     * Retrieves a list of names of available audio output devices (e.g., speakers, headphones).
     * @return A {@link List} of device names. Returns an empty list if no devices are found.
     */
    List<String> getAvailableOutputDevices();

    /**
     * Selects and initializes the specified audio output device.
     * If another device is already open, it will be closed first.
     *
     * @param deviceName The name of the audio device to select.
     * @throws AudioDeviceException if the device cannot be found, opened, or configured.
     */
    void selectOutputDevice(String deviceName) throws AudioDeviceException;

    /**
     * Sets the master volume for the currently selected audio output device.
     *
     * @param volume A float value between 0.0 (mute) and 1.0 (maximum).
     */
    void setVolume(float volume);

    /**
     * Plays a short, pre-defined test sound through the currently selected device.
     *
     * @throws AudioDeviceException if no device is selected or if the test tone cannot be played.
     */
    void playTestSound() throws AudioDeviceException;

    /**
     * Plays raw audio data through the currently selected audio output device.
     * This method will attempt to convert the audio data format if it does not match
     * the format supported by the output device.
     *
     * @param audioData The byte array containing the audio data to play.
     * @param format The {@link AudioFormat} of the provided audio data.
     * @throws AudioException if no device is selected or if a playback error occurs (e.g., format conversion failure).
     */
    void playAudioData(byte[] audioData, AudioFormat format) throws AudioException;

    /**
     * Immediately stops any audio that is currently playing.
     * If no audio is playing, this method does nothing.
     */
    void stopPlayback();

    /**
     * Closes the currently open audio output line and releases its resources.
     * This should be called on application shutdown.
     */
    void close();

    /**
     * Gets the name of the currently selected audio output device.
     *
     * @return The device name, or null if no device is selected.
     */
    String getSelectedDeviceName();
}
