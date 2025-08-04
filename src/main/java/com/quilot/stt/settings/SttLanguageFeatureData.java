package com.quilot.stt.settings;

import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * A utility class that holds data about which features are supported
 * for each language in the STT service, based on Google's documentation.
 */
@NoArgsConstructor
public final class SttLanguageFeatureData {

    /**
     * A record to hold the supported features for a language.
     */
    public record FeatureSupport(
            boolean hasAutomaticPunctuation,
            boolean hasDiarization,
            boolean hasWordConfidence,
            boolean hasModelAdaptation
    ) {}

    /**
     * A map where the key is the language code (e.g., "en-US") and the value
     * is an object detailing the supported features for that language.
     * This data is derived from the provided documentation screenshots.
     */
    private static final Map<String, FeatureSupport> FEATURES_BY_LANGUAGE = Map.of(
            "en-US", new FeatureSupport(true, true, true, true),
            "en-GB", new FeatureSupport(true, false, true, true),
            "bg-BG", new FeatureSupport(false, true, true, true)
    );

    private static final FeatureSupport NO_SUPPORT = new FeatureSupport(false, false, false, false);

    /**
     * Gets the feature support details for a given language.
     *
     * @param languageCode The BCP-47 code for the language (e.g., "en-US").
     * @return A {@link FeatureSupport} object. Returns a "no support" object if the language is not found.
     */
    public static FeatureSupport getSupportFor(String languageCode) {
        return FEATURES_BY_LANGUAGE.getOrDefault(languageCode, NO_SUPPORT);
    }
}
