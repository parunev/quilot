package com.quilot.stt;

import com.quilot.stt.settings.RecognitionConfigSettings;

/**
 * Defines the contract for managing Speech-to-Text (STT) configuration settings.
 * This interface allows for loading, saving, and retrieving STT-specific parameters.
 */
public interface ISpeechToTextSettingsManager {

    /**
     * Loads the current STT settings.
     * @return The loaded RecognitionConfigSettings object.
     */
    RecognitionConfigSettings loadSettings();

    /**
     * Saves the provided STT settings.
     * @param settings The RecognitionConfigSettings object to save.
     */
    void saveSettings(RecognitionConfigSettings settings);

    /**
     * Resets the STT settings to their default values.
     * @return The default RecognitionConfigSettings object.
     */
    RecognitionConfigSettings resetToDefaults();
}
