package com.quilot.stt.settings;

import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class RecognitionConfigSettings {
    private String languageCode;
    private boolean enableAutomaticPunctuation;
    private boolean enableWordTimeOffsets;
    private String model;
    private String speechContexts;
    private boolean enableSingleUtterance;
    private boolean interimTranscription;

    public RecognitionConfigSettings() {
        // Set default values
        this.languageCode = "en-US";
        this.enableAutomaticPunctuation = true;
        this.enableWordTimeOffsets = false;
        this.model = "default";
        this.speechContexts = "";
        this.enableSingleUtterance = false;
        this.interimTranscription = true;
    }

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