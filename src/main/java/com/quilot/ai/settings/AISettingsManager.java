package com.quilot.ai.settings;

import com.quilot.exceptions.ai.AISettingsException;
import com.quilot.utils.Logger;

import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Manages the persistence of AI configuration settings using Java's Preferences API.
 */
public class AISettingsManager implements IAISettingsManager {

    private static final String PREF_NODE_NAME = "com/quilot/ai_settings";
    private final Preferences prefs;

    // Preference keys
    private static final String KEY_PROJECT_ID = "projectId";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_MODEL_ID = "modelId";
    private static final String KEY_TEMPERATURE = "temperature";
    private static final String KEY_MAX_OUTPUT_TOKENS = "maxOutputTokens";
    private static final String KEY_TOP_P = "topP";
    private static final String KEY_TOP_K = "topK";

    public AISettingsManager() {
        try {
            prefs = Preferences.userRoot().node(PREF_NODE_NAME);
            Logger.info("AISettingsManager initialized.");
        } catch (SecurityException e) {
            Logger.error("Could not access Java Preferences due to a security policy.", e);
            throw new AISettingsException("Failed to initialize settings manager due to security restrictions.", e);
        }
    }

    @Override
    public AIConfigSettings loadSettings() {
        AIConfigSettings defaults = AIConfigSettings.builder().build();
        return AIConfigSettings.builder()
                .projectId(prefs.get(KEY_PROJECT_ID, defaults.getProjectId()))
                .location(prefs.get(KEY_LOCATION, defaults.getLocation()))
                .modelId(prefs.get(KEY_MODEL_ID, defaults.getModelId()))
                .temperature(prefs.getDouble(KEY_TEMPERATURE, defaults.getTemperature()))
                .maxOutputTokens(prefs.getInt(KEY_MAX_OUTPUT_TOKENS, defaults.getMaxOutputTokens()))
                .topP(prefs.getDouble(KEY_TOP_P, defaults.getTopP()))
                .topK(prefs.getInt(KEY_TOP_K, defaults.getTopK()))
                .build();
    }

    @Override
    public void saveSettings(AIConfigSettings settings) {
        Objects.requireNonNull(settings, "AIConfigSettings object cannot be null.");

        try {
            prefs.put(KEY_PROJECT_ID, settings.getProjectId());
            prefs.put(KEY_LOCATION, settings.getLocation());
            prefs.put(KEY_MODEL_ID, settings.getModelId());
            prefs.putDouble(KEY_TEMPERATURE, settings.getTemperature());
            prefs.putInt(KEY_MAX_OUTPUT_TOKENS, settings.getMaxOutputTokens());
            prefs.putDouble(KEY_TOP_P, settings.getTopP());
            prefs.putInt(KEY_TOP_K, settings.getTopK());

            prefs.flush();
            Logger.info("AI settings saved successfully.");

        } catch (BackingStoreException e) { // persistence errors
            Logger.error("Failed to save settings to the backing store.", e);
            throw new AISettingsException("Could not save settings due to a storage error.", e);
        } catch (NullPointerException | IllegalArgumentException e) { // potential issues with the settings object itself.
            Logger.error("Invalid setting value provided (e.g., null or invalid format).", e);
            throw new AISettingsException("Cannot save settings due to invalid data.", e);
        }
    }

    @Override
    public AIConfigSettings resetToDefaults() {
        AIConfigSettings defaultSettings = AIConfigSettings.builder().build();
        saveSettings(defaultSettings);
        Logger.info("AI settings reset to defaults.");
        return defaultSettings;
    }
}
