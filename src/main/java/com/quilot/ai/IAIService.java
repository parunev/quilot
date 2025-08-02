package com.quilot.ai;

import com.quilot.ai.settings.IAISettingsManager;
import com.quilot.utils.Logger;

/**
 * Defines the contract for an AI interaction service.
 * This interface abstracts the logic for sending prompts to an AI model,
 * managing conversation history, and handling responses asynchronously.
 */
public interface IAIService {

    /**
     * Sends a prompt to the AI model and generates a response asynchronously.
     * The result, whether successful or an error, is communicated through the provided listener.
     *
     * @param prompt The user-provided text prompt to send to the AI.
     * @param listener The callback listener to handle the AI's response or any errors.
     */
    void generateResponse(String prompt, AIResponseListener listener);

    /**
     * Clears the entire conversation history maintained by the service.
     */
    void clearHistory();

    /**
     * Retrieves the settings manager associated with this AI service.
     *
     * @return The {@link IAISettingsManager} instance used by this service.
     */
    IAISettingsManager getSettingsManager();

    /**
     * A listener interface for receiving asynchronous responses from the AI service.
     */
    @FunctionalInterface
    interface AIResponseListener {
        /**
         * Called when the AI service successfully generates a response.
         *
         * @param aiResponse The complete, generated text response from the AI model.
         */
        void onResponse(String aiResponse);

        /**
         * Called when an error occurs during the AI response generation.
         *
         * @param errorMessage A user-friendly message describing the error.
         */
        default void onError(String errorMessage) {
            Logger.error("AI Service Error: " + errorMessage);
        }
    }
}
