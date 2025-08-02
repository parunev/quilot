package com.quilot.ai;

import com.quilot.ai.settings.IAISettingsManager;
import com.quilot.utils.Logger;

/**
 * Defines the contract for an Artificial Intelligence (AI) service.
 * This interface abstracts the details of interacting with an AI model for text generation,
 * adhering to the Dependency Inversion Principle.
 */
public interface IAIService {

    /**
     * Generates a text response from the AI based on a given prompt.
     * This method is asynchronous and delivers results via a listener.
     *
     * @param prompt   The user's input or question for the AI.
     * @param listener A listener to receive the AI's response in real-time.
     */
    void generateResponse(String prompt, AIResponseListener listener);

    /**
     * Clears the conversation history of the AI, starting a new context.
     */
    void clearHistory();

    /**
     * Returns the settings manager associated with this AI service.
     * @return The IAISettingsManager instance.
     */
    IAISettingsManager getSettingsManager();

    /**
     * Listener interface for receiving AI responses.
     */
    @FunctionalInterface
    interface AIResponseListener {
        /**
         * Called when the AI generates a response.
         * @param response The AI's generated text response.
         */
        void onResponse(String response);

        /**
         * Called when an error occurs during AI response generation.
         * @param errorMessage A description of the error.
         */
        default void onError(String errorMessage) {
            Logger.error("AI Service Error: " + errorMessage);
        }
    }
}
