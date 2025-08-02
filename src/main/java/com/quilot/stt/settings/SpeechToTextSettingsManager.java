package com.quilot.stt.settings;

import com.quilot.exceptions.stt.STTSettingsException;
import com.quilot.stt.ISpeechToTextSettingsManager;
import com.quilot.utils.Logger;

import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class SpeechToTextSettingsManager implements ISpeechToTextSettingsManager {

    private static final String PREF_NODE_NAME = "com/quilot/stt_settings";
    private final Preferences prefs;

    private static final String KEY_LANGUAGE_CODE = "languageCode";
    private static final String KEY_AUTO_PUNCTUATION = "enableAutomaticPunctuation";
    private static final String KEY_WORD_TIME_OFFSETS = "enableWordTimeOffsets";
    private static final String KEY_MODEL = "model";
    private static final String KEY_SPEECH_CONTEXTS = "speechContexts";
    private static final String KEY_ENABLE_SINGLE_UTTERANCE = "enableSingleUtterance";
    private static final String KEY_ENABLE_INTERIM_TRANSCRIPTION = "enableInterimTranscription";
    private static final String KEY_USE_ENHANCED = "useEnhanced";
    private static final String KEY_PROFANITY_FILTER = "profanityFilter";
    private static final String KEY_MAX_ALTERNATIVES = "maxAlternatives";
    private static final String KEY_ENABLE_SPEAKER_DIARIZATION = "enableSpeakerDiarization";

    public SpeechToTextSettingsManager() {
        try {
            this.prefs = Preferences.userRoot().node(PREF_NODE_NAME);
            Logger.info("SpeechToTextSettingsManager initialized.");
        } catch (SecurityException e) {
            Logger.error("Could not access Java Preferences due to a security policy.", e);
            throw new RuntimeException("Failed to initialize STT settings manager due to security restrictions.", e);
        }
    }

    @Override
    public RecognitionConfigSettings loadSettings() {
        RecognitionConfigSettings defaults = RecognitionConfigSettings.builder().build();

        return RecognitionConfigSettings.builder()
                .languageCode(prefs.get(KEY_LANGUAGE_CODE, defaults.getLanguageCode()))
                .enableAutomaticPunctuation(prefs.getBoolean(KEY_AUTO_PUNCTUATION, defaults.isEnableAutomaticPunctuation()))
                .enableWordTimeOffsets(prefs.getBoolean(KEY_WORD_TIME_OFFSETS, defaults.isEnableWordTimeOffsets()))
                .model(prefs.get(KEY_MODEL, defaults.getModel()))
                .speechContexts(prefs.get(KEY_SPEECH_CONTEXTS, defaults.getSpeechContexts()))
                .enableSingleUtterance(prefs.getBoolean(KEY_ENABLE_SINGLE_UTTERANCE, defaults.isEnableSingleUtterance()))
                .interimTranscription(prefs.getBoolean(KEY_ENABLE_INTERIM_TRANSCRIPTION, defaults.isInterimTranscription()))
                .useEnhanced(prefs.getBoolean(KEY_USE_ENHANCED, defaults.isUseEnhanced()))
                .profanityFilter(prefs.getBoolean(KEY_PROFANITY_FILTER, defaults.isProfanityFilter()))
                .maxAlternatives(prefs.getInt(KEY_MAX_ALTERNATIVES, defaults.getMaxAlternatives()))
                .enableSpeakerDiarization(prefs.getBoolean(KEY_ENABLE_SPEAKER_DIARIZATION, defaults.isEnableSpeakerDiarization()))
                .build();
    }

    @Override
    public void saveSettings(RecognitionConfigSettings settings) throws STTSettingsException {
        Objects.requireNonNull(settings, "RecognitionConfigSettings object cannot be null.");

        try {
            prefs.put(KEY_LANGUAGE_CODE, settings.getLanguageCode());
            prefs.putBoolean(KEY_AUTO_PUNCTUATION, settings.isEnableAutomaticPunctuation());
            prefs.putBoolean(KEY_WORD_TIME_OFFSETS, settings.isEnableWordTimeOffsets());
            prefs.put(KEY_MODEL, settings.getModel());
            prefs.put(KEY_SPEECH_CONTEXTS, settings.getSpeechContexts());
            prefs.putBoolean(KEY_ENABLE_SINGLE_UTTERANCE, settings.isEnableSingleUtterance());
            prefs.putBoolean(KEY_ENABLE_INTERIM_TRANSCRIPTION, settings.isInterimTranscription());
            prefs.putBoolean(KEY_USE_ENHANCED, settings.isUseEnhanced());
            prefs.putBoolean(KEY_PROFANITY_FILTER, settings.isProfanityFilter());
            prefs.putInt(KEY_MAX_ALTERNATIVES, settings.getMaxAlternatives());
            prefs.putBoolean(KEY_ENABLE_SPEAKER_DIARIZATION, settings.isEnableSpeakerDiarization());

            prefs.flush();
            Logger.info("STT settings saved successfully.");
        } catch (BackingStoreException e) {
            Logger.error("Failed to save STT settings to the backing store.", e);
            throw new STTSettingsException("Could not save STT settings due to a storage error.", e);
        }
    }

    @Override
    public RecognitionConfigSettings resetToDefaults() throws STTSettingsException {
        RecognitionConfigSettings defaultSettings = RecognitionConfigSettings.builder().build();
        saveSettings(defaultSettings);
        Logger.info("STT settings reset to defaults.");
        return defaultSettings;
    }
}