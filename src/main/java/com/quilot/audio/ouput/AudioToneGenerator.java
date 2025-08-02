package com.quilot.audio.ouput;

import javax.sound.sampled.AudioFormat;

public final class AudioToneGenerator {

    private AudioToneGenerator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }

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
