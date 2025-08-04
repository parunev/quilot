package com.quilot.stt.settings;

import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A final utility class that holds data about languages and models supported by the STT service.
 * This provides a centralized, clean source of truth for populating UI components.
 * This class cannot be instantiated.
 */
@NoArgsConstructor
public final class SttLanguageData {

    /**
     * A simple record to encapsulate a language's code and its user-friendly name.
     * The {@code toString()} method is overridden to provide a clean display name for UI components.
     *
     * @param code The BCP-47 language code (e.g., "en-US").
     * @param name The user-friendly display name (e.g., "English (United States)").
     */
    public record Language(String code, String name) {
        @Override
        public @NonNull String toString() {
            return name;
        }
    }

    private static final Map<Language, List<String>> SUPPORTED_LANGUAGES = new TreeMap<>(Comparator.comparing(a -> a.name));

    static {
        List<String> enUsModels = List.of("default", "latest_long", "latest_short", "command_and_search", "phone_call", "telephony", "telephony_short", "video", "medical_conversation", "medical_dictation");
        List<String> enGbModels = List.of("default", "latest_long", "latest_short", "command_and_search", "phone_call", "telephony", "telephony_short");
        List<String> bgBgModels = List.of("default", "latest_long", "latest_short", "command_and_search");

        SUPPORTED_LANGUAGES.put(new Language("en-US", "English (United States)"), enUsModels);
        SUPPORTED_LANGUAGES.put(new Language("en-GB", "English (United Kingdom)"), enGbModels);
        SUPPORTED_LANGUAGES.put(new Language("bg-BG", "Bulgarian (Bulgaria)"), bgBgModels);
    }

    /**
     * Gets a sorted list of all supported languages.
     * @return A {@link List} of {@link Language} objects.
     */
    public static List<Language> getLanguages() {
        return SUPPORTED_LANGUAGES.keySet().stream().toList();
    }

    /**
     * Gets the list of supported recognition models for a given language.
     * @param lang The {@link Language} for which to retrieve models.
     * @return A {@link List} of model identifier strings.
     */
    public static List<String> getModels(Language lang) {
        return SUPPORTED_LANGUAGES.getOrDefault(lang, List.of("default"));
    }
}
