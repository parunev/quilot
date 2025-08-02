package com.quilot.stt;

import com.quilot.exceptions.stt.STTSettingsException;
import com.quilot.stt.settings.RecognitionConfigSettings;

/**
 * Defines the contract for managing the persistence of Speech-to-Text (STT) settings.
 */
public interface ISpeechToTextSettingsManager {

    /**
     * Loads the STT configuration settings from the persistent store.
     * If no settings are found, it should return a settings object with default values.
     *
     * @return A {@link RecognitionConfigSettings} object populated with loaded or default values.
     */
    RecognitionConfigSettings loadSettings();

    /**
     * Saves the provided STT configuration settings to the persistent store.
     *
     * @param settings The {@link RecognitionConfigSettings} object to save.
     * @throws STTSettingsException if saving the settings to the backing store fails.
     */
    void saveSettings(RecognitionConfigSettings settings) throws STTSettingsException;

    /**
     * Resets the STT configuration settings to their default values and saves them.
     *
     * @return A new {@link RecognitionConfigSettings} object containing the default values.
     * @throws STTSettingsException if saving the default settings fails.
     */
    RecognitionConfigSettings resetToDefaults() throws STTSettingsException ;
}
