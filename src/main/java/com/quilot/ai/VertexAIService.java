package com.quilot.ai;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseStream;
import com.quilot.ai.settings.AIConfigSettings;
import com.quilot.ai.settings.IAISettingsManager;
import com.quilot.utils.Logger;
import lombok.Data;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * * Concrete implementation of IAIService that interacts with Google Cloud Vertex AI.
 * This service handles sending prompts and receiving AI-generated responses,
 * including conversation history management and configurable generation parameters.
 */
@Data
public class VertexAIService implements IAIService {

    private VertexAI vertexAI;
    private String credentialPath;
    private boolean isClientInitialized = false;
    private final IAISettingsManager settingsManager;
    private final AtomicBoolean isGenerating = new AtomicBoolean(false);

    private List<Content> chatHistory;

    /**
     * Constructor for VertexAIService.
     * @param initialCredentialPath The initial absolute path to the Google Cloud service account JSON key file.
     * @param settingsManager The manager for AI configuration settings.
     */
    public VertexAIService(String initialCredentialPath, IAISettingsManager settingsManager) {
        this.credentialPath = initialCredentialPath;
        this.settingsManager = settingsManager;
        this.chatHistory = new ArrayList<>();
        Logger.info("VertexAIService initialized. Client initialization deferred.");
        initializeClient();
    }

    /**
     * Attempts to initialize the Google Cloud VertexAI client.
     */
    private void initializeClient() {
        if (credentialPath == null || credentialPath.isEmpty()) {
            Logger.warn("Google Cloud credential path is not set. Vertex AI client cannot be initialized.");
            isClientInitialized = false;
            return;
        }

        closeClient();

        try {
            AIConfigSettings currentSettings = settingsManager.loadSettings();
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialPath))
                    .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

            this.vertexAI = new VertexAI.Builder()
                    .setProjectId(currentSettings.getProjectId())
                    .setLocation(currentSettings.getLocation())
                    .setCredentials(credentials)
                    .build();

            isClientInitialized = true;
            Logger.info("Vertex AI GenerativeModel client created successfully for model: " + currentSettings.getModelId());
        } catch (Exception e) {
            Logger.error("An unexpected error occurred during Vertex AI client initialization: " + e.getMessage());
            isClientInitialized = false;
        }
    }

    /**
     * Sets a new credential path and attempts to re-initialize the client.
     * @param newCredentialPath The new path to the JSON key file.
     */
    public void setCredentialPath(String newCredentialPath) {
        this.credentialPath = newCredentialPath;
        Logger.info("Updating Vertex AI credential path to: " + newCredentialPath);
        initializeClient();
    }

    @Override
    public void generateResponse(String prompt, AIResponseListener listener) {
        if (!isClientInitialized || vertexAI == null) {
            Logger.error("Vertex AI client is not initialized. Cannot generate response.");
            if (listener != null) listener.onError("[AI: Vertex AI client not initialized. Please set credentials.]");
            return;
        }
        if (listener == null) {
            Logger.error("AIResponseListener cannot be null.");
            return;
        }
        if (prompt == null || prompt.trim().isEmpty()) {
            listener.onResponse("[AI: Please provide a valid prompt.]");
            return;
        }

        if (!isGenerating.compareAndSet(false, true)) {
            Logger.warn("Another request is already being processed. Ignoring this one.");
            return;
        }

        String adminPrompt = """
            Please respond concisely and clearly. \
            Do not include any special characters like : * - or emojis. \
            Avoid extra blank lines or spaces. \
            Keep the response under 300 words.
            """;

        String combinedPrompt = adminPrompt + prompt;

        chatHistory.add(Content.newBuilder()
                .addParts(Part.newBuilder().setText(combinedPrompt).build())
                .setRole("user")
                .build());

        Logger.info("Sending prompt to AI: " + prompt);

        CompletableFuture.runAsync(() -> {
            try {
                AIConfigSettings currentSettings = settingsManager.loadSettings();

                GenerationConfig generationConfig = GenerationConfig.newBuilder()
                        .setTemperature((float) currentSettings.getTemperature())
                        .setMaxOutputTokens(currentSettings.getMaxOutputTokens())
                        .setTopP((float) currentSettings.getTopP())
                        .setTopK(currentSettings.getTopK())
                        .build();

                GenerativeModel generativeModel = new GenerativeModel.Builder()
                        .setModelName(currentSettings.getModelId())
                        .setVertexAi(vertexAI)
                        .setGenerationConfig(generationConfig)
                        .build();

                ResponseStream<GenerateContentResponse> responseStream =
                        generativeModel.generateContentStream(combinedPrompt);

                StringBuilder aiResponseBuilder = new StringBuilder();

                responseStream.forEach(response -> {
                    Logger.info("Response: " + response);
                    if (response.getCandidatesCount() > 0) {
                        var candidate = response.getCandidates(0);
                        if (candidate.hasContent() && candidate.getContent().getPartsCount() > 0) {
                            String text = candidate.getContent().getParts(0).getText();
                            aiResponseBuilder.append(text);
                        } else {
                            Logger.warn("Candidate content or parts missing.");
                        }
                    } else {
                        Logger.warn("No candidates in response.");
                    }
                });

                String finalResponse = aiResponseBuilder.toString();
                if (finalResponse.isEmpty()) {
                    Logger.warn("Received empty response from Vertex AI.");
                    listener.onResponse("[AI: No response generated.]");
                } else {
                    chatHistory.add(Content.newBuilder()
                            .addParts(Part.newBuilder().setText(finalResponse).build())
                            .setRole("model")
                            .build());
                    Logger.info("AI responded: " + finalResponse);
                    listener.onResponse(finalResponse); // ✅ Only once
                }

            } catch (Exception e) {
                Logger.error("Error calling Vertex AI: " + e.getMessage());
                listener.onError("[AI Error: " + e.getMessage() + "]");
            } finally {
                isGenerating.set(false);
            }
        });

    }

    @Override
    public void clearHistory() {
        this.chatHistory.clear();
        Logger.info("AI conversation history cleared.");
    }

    /**
     * Returns the settings manager associated with this AI service.
     * @return The IAISettingsManager instance.
     */
    @Override
    public IAISettingsManager getSettingsManager() {
        return settingsManager;
    }

    /**
     * Closes the Vertex AI client permanently.
     */
    public void closeClient() {
        if (vertexAI != null) {
            try {
                vertexAI.close();
                Logger.info("Vertex AI client closed.");
            } catch (Exception e) {
                Logger.error("Error closing Vertex AI client: " + e.getMessage());
            }
        }
        isClientInitialized = false;
        vertexAI = null;
    }
}