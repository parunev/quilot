package com.quilot.ai.settings;

/**
 * Defines the contract for managing AI service configuration settings.
 * This interface allows for loading, saving, and retrieving AI-specific parameters.
 */
public interface IAISettingsManager {

    /**
     * Loads the current AI settings.
     * @return The loaded AIConfigSettings object.
     */
    AIConfigSettings loadSettings();

    /**
     * Saves the provided AI settings.
     * @param settings The AIConfigSettings object to save.
     */
    void saveSettings(AIConfigSettings settings);

    /**
     * Resets the AI settings to their default values.
     * @return The default AIConfigSettings object.
     */
    AIConfigSettings resetToDefaults();
}
