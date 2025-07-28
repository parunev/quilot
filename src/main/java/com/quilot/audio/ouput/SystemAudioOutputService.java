package com.quilot.audio.ouput;

import com.quilot.utils.Logger;

import javax.sound.sampled.*;
import java.util.List;

public class SystemAudioOutputService implements AudioOutputService{

    private Mixer selectedOutputMixer;
    private FloatControl masterGainControl;
    private SourceDataLine outputLine;

    // 44.1kHz, 16-bit, mono, signed, little-endian
    private static final AudioFormat DEFAULT_AUDIO_FORMAT = new AudioFormat(44100, 16, 1, true, false);

    public SystemAudioOutputService() {
        Logger.info("SystemAudioOutputService initialized.");
    }

    @Override
    public List<String> getAvailableOutputDevices() {
        return AudioDeviceDiscoverer.discoverOutputDevices();
    }

    @Override
    public boolean selectOutputDevice(String deviceName) {
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixerInfos) {
            if (info.getName().equals(deviceName)) {
                try {
                    closeOutputLine();

                    selectedOutputMixer = AudioSystem.getMixer(info);
                    DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, DEFAULT_AUDIO_FORMAT);

                    if (!selectedOutputMixer.isLineSupported(dataLineInfo)) {
                        Logger.error("Selected mixer '" + deviceName + "' does not support the default audio format: " + DEFAULT_AUDIO_FORMAT, null);
                        DataLine.Info supportedDataLineInfo = (DataLine.Info) selectedOutputMixer.getLineInfo();
                        AudioFormat[] supportedFormats = supportedDataLineInfo.getFormats();
                        if (supportedFormats.length > 0) {
                            Logger.warn("Attempting to use first supported format: " + supportedFormats[0]);
                            outputLine = (SourceDataLine) selectedOutputMixer.getLine(new DataLine.Info(SourceDataLine.class, supportedFormats[0]));
                            outputLine.open(supportedFormats[0]);
                        } else {
                            Logger.error("No compatible formats found for device: " + deviceName, null);
                            selectedOutputMixer = null;
                            outputLine = null;
                            masterGainControl = null;
                            return false;
                        }
                    } else {
                        outputLine = (SourceDataLine) selectedOutputMixer.getLine(dataLineInfo);
                        outputLine.open(DEFAULT_AUDIO_FORMAT);
                    }

                    outputLine.start();
                    initializeVolumeControl();

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
            volume = Math.max(0.0f, Math.min(1.0f, volume));

            float min = masterGainControl.getMinimum();
            float max = masterGainControl.getMaximum();

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
            byte[] buffer = AudioToneGenerator.generateSineWave(outputLine.getFormat(), 440, 500);

            outputLine.write(buffer, 0, buffer.length);
            outputLine.drain();
            Logger.info("Test sound played.");
        } catch (Exception e) {
            Logger.error("Error playing test sound: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        closeOutputLine();
        Logger.info("SystemAudioOutputService resources released.");
    }

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
