package com.quilot.stt;

import com.quilot.exceptions.stt.STTException;
import com.quilot.utils.Logger;

import javax.sound.sampled.AudioFormat;

/**
 * Defines the contract for a Speech-to-Text (STT) service.
 * This interface abstracts the details of converting audio data into text,
 * adhering to the Dependency Inversion Principle.
 */
public interface SpeechToTextService {

    /**
     * CHANGE: Now throws a specific exception if starting the stream fails.
     */
    void startStreamingRecognition(AudioFormat audioFormat, StreamingRecognitionListener listener) throws STTException;

    /**
     * Stops the current continuous streaming recognition session.
     * @return true if stopping was clean, false if issues occurred.
     */
    boolean stopStreamingRecognition();

    /**
     * CHANGE: Now throws a specific exception for auth failures, making it more informative.
     */
    void testCredentials() throws STTException;

    /**
     * Listener interface for receiving real-time transcription results.
     */
    @FunctionalInterface
    interface StreamingRecognitionListener {
        void onTranscriptionResult(String transcription, boolean isFinal);

        /**
         * CHANGE: Now provides the full Exception object for better error details.
         */
        default void onTranscriptionError(Exception error) {
            Logger.error("Streaming STT Error: " + error.getMessage(), error);
        }

        default void onStreamClosed() {
            Logger.info("Streaming STT session closed.");
        }
    }
}