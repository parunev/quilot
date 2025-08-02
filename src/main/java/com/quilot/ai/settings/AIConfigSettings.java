package com.quilot.ai.settings;

import lombok.Data;

@   Data
public class AIConfigSettings {
    private String projectId;
    private String location; // e.g., "us-central1"
    private String modelId; // The ID of the deployed model (e.g., "text-bison@001" or a fine-tuned model ID)
    private double temperature;
    private int maxOutputTokens;
    private double topP;
    private int topK;

    public AIConfigSettings() {
        // Set default values for a generic text generation model
        this.projectId = "your-gcp-project-id"; // IMPORTANT: Replace with actual project ID
        this.location = "europe-west1";
        this.modelId = "gemini-2.5-flash";
        this.temperature = 0.7;
        this.maxOutputTokens = 256;
        this.topP = 0.8;
        this.topK = 40;
    }
}