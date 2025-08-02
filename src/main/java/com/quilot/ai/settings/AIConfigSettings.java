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

    /** The Google Cloud Project ID. */
    @Builder.Default
    String projectId = "your-gcp-project-id";

    /** The Google Cloud location/region (e.g., "europe-west1"). */
    @Builder.Default
    String location = "europe-west1";

    /** The ID of the generative model (e.g., "gemini-1.5-flash"). */
    @Builder.Default
    String modelId = "gemini-1.5-flash";

    /** Controls randomness in generation. Range: 0.0 to 1.0. */
    @Builder.Default
    double temperature = 0.7;

    /** The maximum number of tokens to generate in the response. */
    @Builder.Default
    int maxOutputTokens = 256;

    /** The cumulative probability cutoff for token selection (nucleus sampling). */
    @Builder.Default
    double topP = 0.8;

    /** The number of highest probability tokens to consider for generation. */
    @Builder.Default
    int topK = 40;

}