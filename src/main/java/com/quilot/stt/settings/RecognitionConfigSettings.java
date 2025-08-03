package com.quilot.stt.settings;

import lombok.Builder;
import lombok.Value;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An immutable container for Google Cloud Speech-to-Text recognition settings.
 * Uses the Builder pattern for safe and readable construction.
 */
@Value
@Builder(toBuilder = true)
public class RecognitionConfigSettings {

    /** The BCP-47 language code for transcription (e.g., "en-US"). */
    @Builder.Default
    String languageCode = "en-US";

    /** If true, adds punctuation to recognition results. */
    @Builder.Default
    boolean enableAutomaticPunctuation = true;

    /** If true, provides the start and end time offsets for each transcribed word. */
    @Builder.Default
    boolean enableWordTimeOffsets = false;

    /** The recognition model to use (e.g., "default", "phone_call"). */
    @Builder.Default
    String model = "default";

    /** A list of phrases to improve recognition accuracy, separated by newlines. */
    @Builder.Default
    String speechContexts = "";

    /** If true, the recognizer will stop after the first detected utterance. */
    @Builder.Default
    boolean enableSingleUtterance = false;

    /** If true, provides real-time, partial transcription results. */
    @Builder.Default
    boolean interimTranscription = true;

    /** If true, uses a premium, higher-accuracy model (may affect cost). */
    @Builder.Default
    boolean useEnhanced = false;

    /** If true, replaces recognized profane words with asterisks. */
    @Builder.Default
    boolean profanityFilter = true;

    /** The maximum number of alternative transcriptions to return. */
    @Builder.Default
    int maxAlternatives = 1;

    /** If true, attempts to identify and label different speakers in the audio. */
    @Builder.Default
    boolean enableSpeakerDiarization = false;

    /** If true, only sends detected questions to the AI to reduce cost. */
    @Builder.Default
    boolean enableQuestionDetection = false;

    /**
     * A derived property that parses the speechContexts string into a list of phrases.
     * @return A {@link List} of speech context phrases.
     */
    public List<String> getSpeechContextsAsList() {
        if (speechContexts == null || speechContexts.trim().isEmpty()) {
            return List.of();
        }

        return Arrays.stream(speechContexts.split("\\R"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}