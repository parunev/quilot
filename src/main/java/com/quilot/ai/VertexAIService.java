package com.quilot.ai;

import com.google.api.gax.rpc.ApiException;
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
import com.quilot.exceptions.ai.AIException;
import com.quilot.exceptions.ai.AIInitializationException;
import com.quilot.utils.Logger;
import lombok.Data;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private final String ADMIN_PROMPT = """
            Please respond concisely and clearly. \
            Do not include any special characters like : * - or emojis. \
            Avoid extra blank lines or spaces. \
            Keep the response under 300 words.
            """;

    /**
     * Constructor for VertexAIService.
     * @param initialCredentialPath The initial absolute path to the Google Cloud service account JSON key file.
     * @param settingsManager The manager for AI configuration settings.
     */
    public VertexAIService(String initialCredentialPath, IAISettingsManager settingsManager) {
        this.settingsManager = Objects.requireNonNull(settingsManager, "IAISettingsManager cannot be null.");
        this.credentialPath = initialCredentialPath;
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

        closeClient(); // Ensures any existing client is properly closed before re-initializing.

        // Using try-with-resources for FileInputStream to ensure it's always closed.
        try (InputStream credentialsStream = new FileInputStream(credentialPath)) {
            AIConfigSettings currentSettings = settingsManager.loadSettings();
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
                    .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

            this.vertexAI = new VertexAI.Builder()
                    .setProjectId(currentSettings.getProjectId())
                    .setLocation(currentSettings.getLocation())
                    .setCredentials(credentials)
                    .build();

            isClientInitialized = true;
            Logger.info("Vertex AI client created successfully for model: " + currentSettings.getModelId());

        } catch (IOException e) {
            Logger.error("Failed to read or process credential file: " + credentialPath, e);
            isClientInitialized = false;
            throw new AIInitializationException("Error during AI client setup from credentials.", e);

        } catch (Exception e) {
            Logger.error("An unexpected error occurred during Vertex AI client initialization.", e);
            isClientInitialized = false;
            throw new AIInitializationException("An unexpected error occurred during AI client initialization.", e);
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
        Objects.requireNonNull(listener, "AIResponseListener cannot be null.");

        if (!isClientInitialized || vertexAI == null) {
            Logger.error("Vertex AI client is not initialized. Cannot generate response.");
            listener.onError("[AI: Client not initialized. Check credentials and configuration.]");
            return;
        }
        if (prompt == null || prompt.trim().isEmpty()) {
            listener.onError("[AI: Please provide a valid prompt.]");
            return;
        }

        if (!isGenerating.compareAndSet(false, true)) {
            Logger.warn("Another generation request is already in progress. Ignoring new request.");
            listener.onError("[AI: Another request is already being processed.]");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                String combinedPrompt = ADMIN_PROMPT + prompt;

                chatHistory.add(Content.newBuilder()
                        .addParts(Part.newBuilder().setText(combinedPrompt).build())
                        .setRole("user")
                        .build());

                Logger.info("Sending prompt to AI: " + prompt);

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

                ResponseStream<GenerateContentResponse> responseStream = generativeModel.generateContentStream(combinedPrompt);
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
                    listener.onResponse(finalResponse);
                }

            } catch (ApiException e) {
                Logger.error("A Google Cloud API error occurred while generating response.", e);
                listener.onError("[AI Error: Could not connect to the service. Code: " + e.getStatusCode().getCode() + "]");

            } catch (AIException e) {
                Logger.error("An AI service error occurred during response generation.", e);
                listener.onError("[AI Error: A service configuration issue occurred. " + e.getMessage() + "]");

            } catch (Exception e) {
                Logger.error("An unexpected error occurred during response generation.", e);
                listener.onError("[AI Error: An unexpected issue occurred. " + e.getMessage() + "]");

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

    @Override
    public IAISettingsManager getSettingsManager() {
        return settingsManager;
    }

    public void closeClient() {
        if (vertexAI != null) {
            try {
                vertexAI.close();
                Logger.info("Vertex AI client closed.");
            } catch (Exception e) {
                Logger.error("Error closing Vertex AI client: ", e);
            }
        }
        isClientInitialized = false;
        vertexAI = null;
    }
}