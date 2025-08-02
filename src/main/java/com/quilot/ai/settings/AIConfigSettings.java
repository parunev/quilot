package com.quilot.ai.settings;

import lombok.Builder;
import lombok.Value;

/**
 * An immutable container for AI configuration settings.
 * <p>
 * This class uses the Builder pattern for safe and readable instantiation.
 * Once created, a settings object cannot be changed, making it thread-safe
 * and preventing unintended modifications.
 */
@Value
@Builder(toBuilder = true)
public class AIConfigSettings {

    @Builder.Default
    String projectId = "your-gcp-project-id"; // IMPORTANT: Replace with actual project ID

    @Builder.Default
    String location = "europe-west1";

    @Builder.Default
    String modelId = "gemini-1.5-flash";

    @Builder.Default
    double temperature = 0.7;

    @Builder.Default
    int maxOutputTokens = 256;

    @Builder.Default
    double topP = 0.8;

    @Builder.Default
    int topK = 40;

}