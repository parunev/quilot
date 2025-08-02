package com.quilot.ai.settings;

import com.quilot.utils.Logger;

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
        prefs = Preferences.userRoot().node(PREF_NODE_NAME);
        Logger.info("AISettingsManager initialized.");
    }

    @Override
    public AIConfigSettings loadSettings() {
        AIConfigSettings settings = new AIConfigSettings();
        settings.setProjectId(prefs.get(KEY_PROJECT_ID, settings.getProjectId()));
        settings.setLocation(prefs.get(KEY_LOCATION, settings.getLocation()));
        settings.setModelId(prefs.get(KEY_MODEL_ID, settings.getModelId()));
        settings.setTemperature(prefs.getDouble(KEY_TEMPERATURE, settings.getTemperature()));
        settings.setMaxOutputTokens(prefs.getInt(KEY_MAX_OUTPUT_TOKENS, settings.getMaxOutputTokens()));
        settings.setTopP(prefs.getDouble(KEY_TOP_P, settings.getTopP()));
        settings.setTopK(prefs.getInt(KEY_TOP_K, settings.getTopK()));
        Logger.info("AI settings loaded.");
        return settings;
    }

    @Override
    public void saveSettings(AIConfigSettings settings) {
        prefs.put(KEY_PROJECT_ID, settings.getProjectId());
        prefs.put(KEY_LOCATION, settings.getLocation());
        prefs.put(KEY_MODEL_ID, settings.getModelId());
        prefs.putDouble(KEY_TEMPERATURE, settings.getTemperature());
        prefs.putInt(KEY_MAX_OUTPUT_TOKENS, settings.getMaxOutputTokens());
        prefs.putDouble(KEY_TOP_P, settings.getTopP());
        prefs.putInt(KEY_TOP_K, settings.getTopK());
        Logger.info("AI settings saved.");
    }

    @Override
    public AIConfigSettings resetToDefaults() {
        AIConfigSettings defaultSettings = new AIConfigSettings();
        saveSettings(defaultSettings); // Save defaults
        Logger.info("AI settings reset to defaults.");
        return defaultSettings;
    }
}
