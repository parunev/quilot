package com.quilot.audio.ouput;

import javax.sound.sampled.AudioFormat;

/**
 * A final utility class for generating simple audio tones, such as a sine wave.
 * This class is used for creating test sounds. It cannot be instantiated.
 */
public final class AudioToneGenerator {

    private AudioToneGenerator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }

    /**
     * Generates a byte array representing a sine wave with the specified parameters.
     *
     * @param format The {@link AudioFormat} of the desired output tone (e.g., sample rate, bit depth).
     * @param frequency The frequency of the sine wave in Hertz (e.g., 440 for A4).
     * @param durationMs The duration of the tone in milliseconds.
     * @return A byte array containing the raw PCM audio data for the sine wave.
     * @throws IllegalArgumentException if any of the parameters are invalid.
     * @throws UnsupportedOperationException if the audio format's encoding or bit depth is not supported.
     */
    public static byte[] generateSineWave(AudioFormat format, int frequency, int durationMs) {
        if (format == null || frequency <= 0 || durationMs <= 0) {
            throw new IllegalArgumentException("Invalid parameters for tone generation.");
        }

        int sampleRate = (int) format.getSampleRate();
        int frameSize = format.getFrameSize();
        int numSamples = sampleRate * durationMs / 1000;
        int numBytes = numSamples * frameSize;
        byte[] buffer = new byte[numBytes];

        if (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED) {
            switch (format.getSampleSizeInBits()) {
                case 16 -> fill16BitPCM(buffer, frequency, sampleRate);
                case 8 -> fill8BitPCM(buffer, frequency, sampleRate);
                default -> throw new UnsupportedOperationException("Unsupported sample size: " + format.getSampleSizeInBits());
            }
        } else {
            throw new UnsupportedOperationException("Unsupported audio encoding: " + format.getEncoding());
        }


        return buffer;
    }

    private static void fill16BitPCM(byte[] buffer, int frequency, int sampleRate) {
        for (int i = 0; i < buffer.length / 2; i++) {
            double angle = 2.0 * Math.PI * frequency * i / sampleRate;
            short sample = (short) (Math.sin(angle) * Short.MAX_VALUE);

            buffer[2 * i] = (byte) (sample & 0xFF);
            buffer[2 * i + 1] = (byte) ((sample >> 8) & 0xFF);
        }
    }

    private static void fill8BitPCM(byte[] buffer, int frequency, int sampleRate) {
        for (int i = 0; i < buffer.length; i++) {
            double angle = 2.0 * Math.PI * frequency * i / sampleRate;
            buffer[i] = (byte) (Math.sin(angle) * 127);
        }
    }
}
