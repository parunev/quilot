package com.quilot.stt;

import javax.sound.sampled.AudioFormat;

import com.quilot.utils.Logger;

/**
 * Defines the contract for a Speech-to-Text (STT) service.
 * This interface abstracts the details of converting audio data into text,
 * adhering to the Dependency Inversion Principle.
 */
public interface SpeechToTextService {

    /**
     * Starts a continuous streaming recognition session.
     * Audio data should be fed via the registered AudioDataListener in IAudioInputService,
     * which will then forward to this streaming service.
     * @param audioFormat The AudioFormat of the audio data that will be streamed.
     * @param listener A listener to receive transcription results in real-time.
     * @return true if streaming successfully started, false otherwise.
     */
    boolean startStreamingRecognition(AudioFormat audioFormat, StreamingRecognitionListener listener);

    /**
     * Stops the current continuous streaming recognition session.
     * @return true if streaming successfully stopped, false otherwise.
     */
    boolean stopStreamingRecognition();

    /**
     * Tests the credentials and initialization of the STT service client.
     * This method should attempt a lightweight operation to verify connectivity and authentication.
     * @return true if the client is successfully initialized and can connect, false otherwise.
     */
    boolean testCredentials();

    /**
     * Listener interface for receiving real-time transcription results from a streaming STT service.
     */
    @FunctionalInterface
    interface StreamingRecognitionListener {
        /**
         * Called when a new transcription result (partial or final) is available.
         * @param transcription The transcribed text.
         * @param isFinal True if this is a final transcription result, false for interim.
         */
        void onTranscriptionResult(String transcription, boolean isFinal);

        /**
         * Called when an error occurs during streaming recognition.
         * @param errorMessage A description of the error.
         */
        default void onTranscriptionError(String errorMessage) {
            Logger.error("Streaming STT Error: " + errorMessage);
        }

        /**
         * Called when the streaming recognition session is closed.
         */
        default void onStreamClosed() {
            Logger.info("Streaming STT session closed.");
        }
    }
}
