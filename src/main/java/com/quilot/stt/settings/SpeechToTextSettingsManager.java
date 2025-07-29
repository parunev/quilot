package com.quilot.stt.settings;

import com.quilot.stt.ISpeechToTextSettingsManager;
import com.quilot.utils.Logger;

import java.util.prefs.Preferences;

/**
 * Manages the persistence of Speech-to-Text (STT) configuration settings
 * using Java's Preferences API.
 */
public class SpeechToTextSettingsManager implements ISpeechToTextSettingsManager {

    private static final String PREF_NODE_NAME = "com/quilot/stt_settings";
    private final Preferences prefs;

    // Preference keys
    private static final String KEY_LANGUAGE_CODE = "languageCode";
    private static final String KEY_AUTO_PUNCTUATION = "enableAutomaticPunctuation";
    private static final String KEY_WORD_TIME_OFFSETS = "enableWordTimeOffsets";
    private static final String KEY_MODEL = "model";
    private static final String KEY_SPEECH_CONTEXTS = "speechContexts";
    private static final String KEY_ENABLE_SINGLE_UTTERANCE = "enableSingleUtterance";
    private static final String KEY_ENABLE_INTERIM_TRANSCRIPTION = "enableInterimTranscription";

    public SpeechToTextSettingsManager() {
        prefs = Preferences.userRoot().node(PREF_NODE_NAME);
        Logger.info("SpeechToTextSettingsManager initialized.");
    }

    @Override
    public RecognitionConfigSettings loadSettings() {
        RecognitionConfigSettings settings = new RecognitionConfigSettings();
        settings.setLanguageCode(prefs.get(KEY_LANGUAGE_CODE, settings.getLanguageCode()));
        settings.setEnableAutomaticPunctuation(prefs.getBoolean(KEY_AUTO_PUNCTUATION, settings.isEnableAutomaticPunctuation()));
        settings.setEnableWordTimeOffsets(prefs.getBoolean(KEY_WORD_TIME_OFFSETS, settings.isEnableWordTimeOffsets()));
        settings.setModel(prefs.get(KEY_MODEL, settings.getModel()));
        settings.setSpeechContexts(prefs.get(KEY_SPEECH_CONTEXTS, settings.getSpeechContexts()));
        settings.setEnableSingleUtterance(prefs.getBoolean(KEY_ENABLE_SINGLE_UTTERANCE, settings.isEnableSingleUtterance()));
        settings.setInterimTranscription(prefs.getBoolean(KEY_ENABLE_INTERIM_TRANSCRIPTION, settings.isInterimTranscription()));
        Logger.info("STT settings loaded.");
        return settings;
    }

    @Override
    public void saveSettings(RecognitionConfigSettings settings) {
        prefs.put(KEY_LANGUAGE_CODE, settings.getLanguageCode());
        prefs.putBoolean(KEY_AUTO_PUNCTUATION, settings.isEnableAutomaticPunctuation());
        prefs.putBoolean(KEY_WORD_TIME_OFFSETS, settings.isEnableWordTimeOffsets());
        prefs.put(KEY_MODEL, settings.getModel());
        prefs.put(KEY_SPEECH_CONTEXTS, settings.getSpeechContexts());
        prefs.putBoolean(KEY_ENABLE_SINGLE_UTTERANCE, settings.isEnableSingleUtterance());
        prefs.putBoolean(KEY_ENABLE_INTERIM_TRANSCRIPTION, settings.isInterimTranscription());
        Logger.info("STT settings saved.");
    }

    @Override
    public RecognitionConfigSettings resetToDefaults() {
        RecognitionConfigSettings defaultSettings = new RecognitionConfigSettings();
        saveSettings(defaultSettings);
        Logger.info("STT settings reset to defaults.");
        return defaultSettings;
    }
}
