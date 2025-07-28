package com.quilot.audio.ouput;

import javax.sound.sampled.AudioFormat;

/**
 * A utility class responsible for generating simple audio tones.
 * This separates the concern of sound generation from the audio output service,
 * making the code more modular and potentially allowing for different sound generation
 * strategies in the future.
 */
public class AudioToneGenerator {

    /**
     * Generates a byte array representing a simple sine wave tone.
     * The tone is generated based on the provided audio format, frequency, and duration.
     *
     * @param format The AudioFormat for which to generate the tone.
     * @param frequency The frequency of the sine wave in Hz (e.g., 440 for A4).
     * @param durationMs The duration of the tone in milliseconds.
     * @return A byte array containing the generated audio data.
     */
    public static byte[] generateSineWave(AudioFormat format, int frequency, int durationMs) {
        int sampleRate = (int) format.getSampleRate();
        int frameSize = format.getFrameSize();
        int numBytes = frameSize * sampleRate * durationMs / 1000;
        byte[] buffer = new byte[numBytes];

        if (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED && format.getSampleSizeInBits() == 16) {
            // 16-bit signed PCM (common case)
            for (int i = 0; i < buffer.length / 2; i++) {
                double angle = 2.0 * Math.PI * frequency * i / ((double) sampleRate / 2);
                short sample = (short) (Math.sin(angle) * Short.MAX_VALUE);

                // Convert short to little-endian bytes
                buffer[2 * i] = (byte) (sample & 0xFF);
                buffer[2 * i + 1] = (byte) ((sample >> 8) & 0xFF);
            }
        } else if (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED && format.getSampleSizeInBits() == 8) {
            // 8-bit signed PCM
            for (int i = 0; i < buffer.length; i++) {
                double angle = 2.0 * Math.PI * frequency * i / sampleRate;
                buffer[i] = (byte) (Math.sin(angle) * 127);
            }
        } else {
            // Fallback for unsupported formats, might not sound as clean
            // Log a warning or throw an exception for production code
            System.err.println("Warning: Tone generation for this AudioFormat is not fully optimized. Using basic 8-bit generation.");
            for (int i = 0; i < buffer.length; i++) {
                double angle = 2.0 * Math.PI * frequency * i / sampleRate;
                buffer[i] = (byte) (Math.sin(angle) * 127);
            }
        }
        return buffer;
    }
}
