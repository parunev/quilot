package com.quilot.audio.ouput;

import com.quilot.utils.Logger;
import lombok.Getter;
import lombok.Setter;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * Concrete implementation of AudioOutputService that interacts with the Java Sound API.
 * This class manages the selected audio output device, its volume, and plays sounds.
 * It delegates device discovery and tone generation to dedicated utility classes.
 */
@Getter
@Setter
public class SystemAudioOutputService implements AudioOutputService{

    private Mixer selectedOutputMixer;
    private FloatControl masterGainControl;
    private SourceDataLine outputLine;

    // Default audio format for opening the line
    // 44.1kHz, 16-bit, mono, signed, little-endian
    private static final AudioFormat DEFAULT_AUDIO_FORMAT = new AudioFormat(44100, 16, 1, true, false);

    public SystemAudioOutputService() {
        Logger.info("SystemAudioOutputService initialized.");
    }

    @Override
    public List<String> getAvailableOutputDevices() {
        // Delegate device discovery to a separate utility class
        return AudioDeviceDiscoverer.discoverOutputDevices();
    }

    @Override
    public boolean selectOutputDevice(String deviceName) {
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixerInfos) {
            if (info.getName().equals(deviceName)) {
                try {
                    closeOutputLine(); // Close any previously open line

                    this.selectedOutputMixer = AudioSystem.getMixer(info); // this. for clarity
                    DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, DEFAULT_AUDIO_FORMAT);

                    if (!selectedOutputMixer.isLineSupported(dataLineInfo)) {
                        Logger.error("Selected mixer '" + deviceName + "' does not support the default audio format: " + DEFAULT_AUDIO_FORMAT, null);

                        // Fallback: try to find a compatible format if the default isn't supported
                        DataLine.Info supportedDataLineInfo = (DataLine.Info) selectedOutputMixer.getLineInfo();
                        AudioFormat[] supportedFormats = supportedDataLineInfo.getFormats();
                        if (supportedFormats.length > 0) {
                            Logger.warn("Attempting to use first supported format: " + supportedFormats[0]);
                            this.outputLine = (SourceDataLine) selectedOutputMixer.getLine(new DataLine.Info(SourceDataLine.class, supportedFormats[0]));
                            outputLine.open(supportedFormats[0]);
                        } else {
                            Logger.error("No compatible formats found for device: " + deviceName, null);
                            this.selectedOutputMixer = null;
                            this.outputLine = null;
                            this.masterGainControl = null;
                            return false;
                        }
                    } else {
                        this.outputLine = (SourceDataLine) selectedOutputMixer.getLine(dataLineInfo);
                        outputLine.open(DEFAULT_AUDIO_FORMAT);
                    }

                    outputLine.start();
                    initializeVolumeControl(); // Attempt to get master gain control

                    Logger.info("Selected audio output device: " + deviceName);
                    return true;
                } catch (LineUnavailableException e) {
                    Logger.error("Audio line unavailable for device '" + deviceName + "': " + e.getMessage(), e);
                } catch (IllegalArgumentException e) {
                    Logger.error("Invalid argument when selecting device '" + deviceName + "': " + e.getMessage(), e);
                } catch (Exception e) {
                    Logger.error("An unexpected error occurred while selecting device '" + deviceName + "': " + e.getMessage(), e);
                }
            }
        }
        Logger.warn("Could not select audio output device: " + deviceName + ". Device not found or unsupported.");
        selectedOutputMixer = null;
        outputLine = null;
        masterGainControl = null;
        return false;
    }

    /**
     * Attempts to acquire the Master Gain control for the current output line.
     */
    private void initializeVolumeControl() {
        if (outputLine != null && outputLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            masterGainControl = (FloatControl) outputLine.getControl(FloatControl.Type.MASTER_GAIN);
            Logger.info("Master Gain control found for the current device.");
        } else {
            masterGainControl = null;
            Logger.warn("Master Gain control not supported for the current device.");
        }
    }

    @Override
    public void setVolume(float volume) {
        if (masterGainControl != null) {
            // Clamp volume between 0.0 and 1.0
            volume = Math.max(0.0f, Math.min(1.0f, volume));

            float min = masterGainControl.getMinimum();
            float max = masterGainControl.getMaximum();

            // Convert 0-1.0 float to decibel range
            float gain = min + (max - min) * volume;
            masterGainControl.setValue(gain);
            Logger.info("Set volume to: " + String.format("%.2f", volume * 100) + "% (Gain: " + String.format("%.2f", gain) + " dB)");
        } else {
            Logger.warn("Volume control not available for the current device. Cannot set volume.");
        }
    }

    @Override
    public void playTestSound() {
        if (!isDeviceSelected()) {
            Logger.warn("No output device selected or line not open to play test sound.");
            return;
        }

        Logger.info("Playing test sound...");
        try {
            // Delegate tone generation to AudioToneGenerator
            byte[] buffer = AudioToneGenerator.generateSineWave(outputLine.getFormat(), 440, 500); // 440 Hz for 500ms
            outputLine.write(buffer, 0, buffer.length);
            outputLine.drain();
            Logger.info("Test sound played.");
        } catch (Exception e) {
            Logger.error("Error playing test sound: " + e.getMessage(), e);
        }
    }

    @Override
    public void playAudioData(byte[] audioData, AudioFormat format) {
        if (outputLine == null || !outputLine.isOpen()) {
            Logger.warn("Output line not open. Cannot play audio data.");
            return;
        }

        try (AudioInputStream audioInputStream = new AudioInputStream(new ByteArrayInputStream(audioData), format, audioData.length / format.getFrameSize())) {
            AudioInputStream playbackStream = audioInputStream;
            AudioFormat targetFormat = outputLine.getFormat();

            // Check if the input format matches the output line's format. If not, attempt conversion.
            if (!format.matches(targetFormat)) {
                if (AudioSystem.isConversionSupported(targetFormat, format)) {
                    playbackStream = AudioSystem.getAudioInputStream(targetFormat, audioInputStream);
                    Logger.info("Converted audio format from " + format + " to " + targetFormat + " for playback.");
                } else {
                    Logger.error("Audio format conversion from " + format + " to " + targetFormat + " is not supported. Cannot play audio data.");
                    return;
                }
            }

            int bytesRead;
            byte[] buffer = new byte[outputLine.getBufferSize()];
            while ((bytesRead = playbackStream.read(buffer, 0, buffer.length)) != -1) {
                if (bytesRead > 0) {
                    outputLine.write(buffer, 0, bytesRead);
                }
            }

            // Ensure all data is played
            outputLine.drain();
            Logger.info("Audio data played successfully.");
        } catch (Exception e) {
            Logger.error("Error playing audio data: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        closeOutputLine();
        Logger.info("SystemAudioOutputService resources released.");
    }

    /**
     * Helper method to safely close the current output line if it's open.
     */
    private void closeOutputLine() {
        if (outputLine != null && outputLine.isOpen()) {
            outputLine.stop();
            outputLine.close();
            Logger.info("Output line closed.");
        }
    }

    @Override
    public boolean isDeviceSelected() {
        return outputLine != null && outputLine.isOpen();
    }
}
