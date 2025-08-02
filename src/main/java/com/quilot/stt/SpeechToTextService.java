package com.quilot.stt;

import com.quilot.exceptions.stt.STTAuthenticationException;
import com.quilot.exceptions.stt.STTException;
import com.quilot.utils.Logger;

import javax.sound.sampled.AudioFormat;

/**
 * Defines the contract for a Speech-to-Text (STT) service.
 * This interface abstracts the details of converting audio data into text.
 */
public interface SpeechToTextService {

    /**
     * Starts a continuous streaming recognition session.
     * Audio data should be fed to the service's implementation of AudioDataListener.
     *
     * @param audioFormat The AudioFormat of the audio data that will be streamed.
     * @param listener A listener to receive transcription results in real-time.
     * @throws STTException if the streaming session cannot be started (e.g., client not initialized, invalid format).
     */
    void startStreamingRecognition(AudioFormat audioFormat, StreamingRecognitionListener listener) throws STTException;

    /**
     * Stops the current continuous streaming recognition session.
     *
     * @return true if streaming was active and successfully stopped, false otherwise.
     */
    boolean stopStreamingRecognition();

    /**
     * Tests the credentials and initialization of the STT service client.
     * This method attempts a lightweight operation to verify connectivity and authentication.
     *
     * @throws STTAuthenticationException if the credentials are not set, invalid, or cannot be used to connect.
     * @throws STTException for other non-authentication related failures.
     */
    void testCredentials() throws STTAuthenticationException, STTException;

    /**
     * Listener interface for receiving real-time transcription results from a streaming STT service.
     */
    @FunctionalInterface
    interface StreamingRecognitionListener {

        /**
         * Called when a new transcription result (partial or final) is available.
         *
         * @param transcription The transcribed text.
         * @param isFinal True if this is a final transcription result, false for interim.
         */
        void onTranscriptionResult(String transcription, boolean isFinal);

        /**
         * Called when an error occurs during the streaming recognition.
         *
         * @param error The exception that occurred during the stream.
         */
        default void onTranscriptionError(Exception error) {
            Logger.error("Streaming STT Error: " + error.getMessage(), error);
        }

        /**
         * Called when the streaming recognition session is closed by the server.
         */
        default void onStreamClosed() {
            Logger.info("Streaming STT session closed.");
        }
    }
}