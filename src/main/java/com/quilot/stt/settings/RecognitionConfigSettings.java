package com.quilot.stt.settings;

import lombok.Builder;
import lombok.Value;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Value
@Builder(toBuilder = true)
public class RecognitionConfigSettings {

    @Builder.Default
    String languageCode = "en-US";

    @Builder.Default
    boolean enableAutomaticPunctuation = true;

    @Builder.Default
    boolean enableWordTimeOffsets = false;

    @Builder.Default
    String model = "default";

    @Builder.Default
    String speechContexts = "";

    @Builder.Default
    boolean enableSingleUtterance = false;

    @Builder.Default
    boolean interimTranscription = true;

    @Builder.Default
    boolean useEnhanced = false;

    @Builder.Default
    boolean profanityFilter = true;

    @Builder.Default
    int maxAlternatives = 1;

    @Builder.Default
    boolean enableSpeakerDiarization = false;

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