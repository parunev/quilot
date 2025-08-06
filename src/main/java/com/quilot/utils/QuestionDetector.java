package com.quilot.utils;

import java.util.List;
import java.util.Map;

/**
 * A utility class to detect if a given string of text is a question.
 * Uses a simple, rule-based approach that supports multiple languages.
 */
public class QuestionDetector {

    /**
     * A map where keys are language codes (e.g., "en-US") and values are lists
     * of common interrogative words for that language.
     */
    private static final Map<String, List<String>> INTERROGATIVE_WORDS_BY_LANGUAGE;

    // The effectiveness is highly dependent on the quality of the transcription service for a given language!!!!!!!!
    static {
        // Initialize the map for all supported languages
        INTERROGATIVE_WORDS_BY_LANGUAGE = Map.of(
                "en-US", List.of(
                        // Standard interrogatives
                        "what", "who", "when", "where", "why", "how", "which", "whose",
                        // Auxiliary verbs
                        "is", "are", "am", "was", "were",
                        "do", "does", "did",
                        "can", "could", "will", "would", "shall", "should",
                        "may", "might", "must",
                        // Interview command verbs
                        "explain", "describe", "tell", "give", "define", "compare", "contrast"
                ),
                "en-GB", List.of(
                        // Standard interrogatives
                        "what", "who", "when", "where", "why", "how", "which", "whose",
                        // Auxiliary verbs
                        "is", "are", "am", "was", "were",
                        "do", "does", "did",
                        "can", "could", "will", "would", "shall", "should",
                        "may", "might", "must",
                        // Interview command verbs
                        "explain", "describe", "tell", "give", "define", "compare", "contrast"
                ),
                "bg-BG", List.of(
                        "какво", "кой", "кога", "къде", "защо", "как",
                        "е", "са", "съм", "беше", "бяха",
                        "правиш", "прави", "направи",
                        "може", "можеше", "ще", "би",
                        "който", "чия",
                        "обясни", "опиши", "разкажи", "дай", "дефинирай", "сравни"
                )
                // Add other supported languages here
        );
    }

    /**
     * Checks if the given text is likely a question in the specified language.
     * <p>
     * The method uses two main checks:
     * 1. If the text ends with a question mark.
     * 2. If the text starts with a common interrogative word for the given language.
     *
     * @param text The transcribed text to analyze.
     * @param languageCode The BCP-47 language code of the text (e.g., "en-US").
     * @return {@code true} if the text is identified as a question, {@code false} otherwise.
     */
    public boolean isQuestion(String text, String languageCode) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        String trimmedText = text.trim();

        if (trimmedText.endsWith("?")) {
            return true;
        }

        List<String> interrogativeWords = INTERROGATIVE_WORDS_BY_LANGUAGE.get(languageCode);
        if (interrogativeWords == null) {
            return false;
        }

        String[] words = trimmedText.split("\\s+", 2);
        if (words.length > 0) {
            String firstWord = words[0].toLowerCase().replaceAll("[^\\p{L}]", "");
            return interrogativeWords.contains(firstWord);
        }

        return false;
    }
}
