package com.quilot.ai.settings;

import com.quilot.exceptions.ai.AISettingsException;

/**
 * Defines the contract for managing the persistence of AI configuration settings.
 */
public interface IAISettingsManager {

    /**
     * Loads the AI configuration settings from the persistent store.
     * If no settings are found, it should return a settings object with default values.
     *
     * @return An {@link AIConfigSettings} object populated with loaded or default values.
     */
    AIConfigSettings loadSettings();

    /**
     * Saves the provided AI configuration settings to the persistent store.
     *
     * @param settings The {@link AIConfigSettings} object to save.
     * @throws AISettingsException if saving the settings to the backing store fails.
     */
    void saveSettings(AIConfigSettings settings) throws AISettingsException;

    /**
     * Resets the AI configuration settings to their default values and saves them.
     *
     * @return A new {@link AIConfigSettings} object containing the default values.
     * @throws AISettingsException if saving the default settings fails.
     */
    AIConfigSettings resetToDefaults() throws AISettingsException;
}
