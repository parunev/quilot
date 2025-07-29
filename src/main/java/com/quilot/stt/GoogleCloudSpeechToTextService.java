package com.quilot.stt;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.*;
import com.google.cloud.speech.v1.SpeechContext;
import com.google.protobuf.ByteString;
import com.quilot.audio.input.AudioInputService;
import com.quilot.stt.settings.RecognitionConfigSettings;
import com.quilot.utils.Logger;
import lombok.Getter;

import javax.sound.sampled.AudioFormat;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Concrete implementation of ISpeechToTextService for Google Cloud Speech-to-Text.
 * This class handles live-streaming audio transcription using the StreamingRecognize API.
 */
@Getter
public class GoogleCloudSpeechToTextService implements SpeechToTextService, AudioInputService.AudioDataListener {

    private SpeechClient speechClient;
    private String credentialPath;
    private boolean isClientInitialized = false;
    private final ISpeechToTextSettingsManager settingsManager;

    private ClientStream<StreamingRecognizeRequest> clientStream;
    private StreamingRecognitionListener streamingRecognitionListener;
    private final AtomicBoolean isStreamingActive = new AtomicBoolean(false);
    private ScheduledExecutorService executorService;

    /**
     * Constructor for GoogleCloudSpeechToTextService.
     * @param initialCredentialPath The initial absolute path to the Google Cloud service account JSON key file (can be empty).
     * @param settingsManager The manager for STT recognition configuration settings.
     */
    public GoogleCloudSpeechToTextService(String initialCredentialPath, ISpeechToTextSettingsManager settingsManager) {
        this.credentialPath = initialCredentialPath;
        this.settingsManager = settingsManager;
        Logger.info("GoogleCloudSpeechToTextService instantiated. Client initialization deferred.");
        initializeClient();
    }

    /**
     * Attempts to initialize the Google Cloud SpeechClient.
     * This method should be called when the credential path is set or updated.
     */
    private void initializeClient() {
        if (credentialPath == null || credentialPath.isEmpty()) {
            Logger.warn("Google Cloud credential path is not set. SpeechClient cannot be initialized.");
            isClientInitialized = false;
            return;
        }

        closeClient();

        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialPath));
            CredentialsProvider credentialsProvider = FixedCredentialsProvider.create(credentials);

            SpeechSettings speechSettings = SpeechSettings.newBuilder()
                    .setCredentialsProvider(credentialsProvider)
                    .build();

            this.speechClient = SpeechClient.create(speechSettings);
            isClientInitialized = true;
            Logger.info("Google Cloud SpeechClient created successfully using provided credentials.");
        } catch (IOException e) {
            Logger.error("Failed to create Google Cloud SpeechClient from credentials: " + e.getMessage());
            isClientInitialized = false;
        } catch (Exception e) {
            Logger.error("An unexpected error occurred during SpeechClient initialization: " + e.getMessage());
            isClientInitialized = false;
        }
    }

    /**
     * Sets a new credential path and attempts to re-initialize the client.
     * This method is public so it can be called from the CredentialsDialog.
     * @param newCredentialPath The new path to the JSON key file.
     */
    public void setCredentialPath(String newCredentialPath) {
        this.credentialPath = newCredentialPath;
        Logger.info("Updating Google Cloud credential path to: " + newCredentialPath);
        initializeClient();
    }

    @Override
    public boolean startStreamingRecognition(AudioFormat audioFormat, StreamingRecognitionListener listener) {
        if (!isClientInitialized || speechClient == null) {
            Logger.error("Google Cloud SpeechClient is not initialized. Cannot start streaming recognition.");
            if (listener != null) listener.onTranscriptionError("[STT Client Not Initialized. Please set credentials.]");
            return false;
        }
        if (isStreamingActive.get()) {
            Logger.warn("Streaming recognition is already active.");
            return true;
        }
        if (audioFormat == null) {
            Logger.error("AudioFormat is null. Cannot start streaming recognition.");
            if (listener != null) listener.onTranscriptionError("[Error: Invalid audio format for streaming.]");
            return false;
        }

        this.streamingRecognitionListener = listener;
        isStreamingActive.set(true);

        Logger.info("Starting new streaming recognition session...");

        try {
            RecognitionConfigSettings currentSettings = settingsManager.loadSettings();

            RecognitionConfig.AudioEncoding encoding = RecognitionConfig.AudioEncoding.LINEAR16;
            if (audioFormat.getEncoding() == AudioFormat.Encoding.ULAW) {
                encoding = RecognitionConfig.AudioEncoding.MULAW;
            }

            RecognitionConfig.Builder recognitionConfigBuilder = RecognitionConfig.newBuilder()
                    .setEncoding(encoding)
                    .setSampleRateHertz((int) audioFormat.getSampleRate())
                    .setLanguageCode(currentSettings.getLanguageCode())
                    .setEnableAutomaticPunctuation(currentSettings.isEnableAutomaticPunctuation())
                    .setEnableWordTimeOffsets(currentSettings.isEnableWordTimeOffsets())
                    .setModel(currentSettings.getModel());

            List<SpeechContext> speechContexts = currentSettings.getSpeechContextsAsList().stream()
                    .map(phrase -> SpeechContext.newBuilder().addPhrases(phrase).build())
                    .collect(Collectors.toList());
            recognitionConfigBuilder.addAllSpeechContexts(speechContexts);


            StreamingRecognitionConfig streamingConfig = StreamingRecognitionConfig.newBuilder()
                    .setConfig(recognitionConfigBuilder.build())
                    .setInterimResults(currentSettings.isInterimTranscription())
                    .setSingleUtterance(currentSettings.isEnableSingleUtterance())
                    .build();

            ResponseObserver<StreamingRecognizeResponse> responseObserver = new ResponseObserver<>() {
                @Override
                public void onStart(StreamController controller) {
                    Logger.info("Streaming recognition response observer started.");
                }

                @Override
                public void onResponse(StreamingRecognizeResponse response) {
                    if (streamingRecognitionListener != null) {
                        if (response.getResultsList().isEmpty()) {
                            // This can happen for interim responses with no speech yet
                            return;
                        }

                        // Get the first result
                        StreamingRecognitionResult result = response.getResultsList().getFirst();
                        if (result.getAlternativesList().isEmpty()) return;

                        // Get the first alternative (most likely transcription)
                        SpeechRecognitionAlternative alternative = result.getAlternativesList().getFirst();

                        String transcription = alternative.getTranscript();
                        boolean isFinal = result.getIsFinal();

                        streamingRecognitionListener.onTranscriptionResult(transcription, isFinal);
                        Logger.info(String.format("Streaming STT Result: \"%s\" (Final: %b)", transcription, isFinal));
                    }
                }

                @Override
                public void onError(Throwable t) {
                    isStreamingActive.set(false);
                    Logger.error("Streaming recognition error: " + t.getMessage());
                    if (streamingRecognitionListener != null)
                        streamingRecognitionListener.onTranscriptionError(t.getMessage());
                    closeClientStream(); // Ensure stream is closed on error
                }

                @Override
                public void onComplete() {
                    isStreamingActive.set(false);
                    Logger.info("Streaming recognition completed.");
                    if (streamingRecognitionListener != null)
                        streamingRecognitionListener.onStreamClosed();
                    closeClientStream(); // Ensure stream is closed on completion
                }
            };

            // Start the bidirectional stream
            clientStream = speechClient.streamingRecognizeCallable().splitCall(responseObserver);

            // Send the first request with the streaming config
            clientStream.send(StreamingRecognizeRequest.newBuilder().setStreamingConfig(streamingConfig).build());
            Logger.info("Streaming recognition session started successfully.");

            // Schedule a task to send empty audio data or close stream if inactive
            // This is a workaround for some streaming APIs that might time out without continuous data
            executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.scheduleAtFixedRate(() -> {
                if (isStreamingActive.get() && clientStream != null) {
                    clientStream.send(StreamingRecognizeRequest.newBuilder().setAudioContent(ByteString.EMPTY).build());
                }
            }, 30, 30, TimeUnit.SECONDS); // Send every 30 seconds

            return true;
        } catch (Exception e) {
            isStreamingActive.set(false);
            Logger.error("Failed to start streaming recognition: " + e.getMessage());
            if (listener != null) listener.onTranscriptionError("Failed to start streaming: " + e.getMessage());
            closeClientStream();
            return false;
        }
    }

    @Override
    public boolean stopStreamingRecognition() {
        if (!isStreamingActive.get() || clientStream == null) {
            Logger.warn("No active streaming recognition to stop.");
            return false;
        }
        isStreamingActive.set(false);
        Logger.info("Stopping streaming recognition session.");
        closeClientStream();
        return true;
    }

    /**
     * Closes the gRPC client stream and shuts down the executor service.
     */
    private void closeClientStream() {
        if (clientStream != null) {
            try {
                clientStream.closeSend(); // Signal end of client stream
            } catch (Exception e) {
                Logger.error("Error closing client stream send: " + e.getMessage());
            }
            clientStream = null;
        }
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
            Logger.info("Executor service for streaming recognition shut down.");
        }
    }

    @Override
    public boolean testCredentials() {
        Logger.info("Attempting to test Google Cloud SpeechClient credentials...");
        initializeClient();
        return isClientInitialized && speechClient != null;
    }

    /**
     * Closes the Google Cloud SpeechClient permanently.
     */
    public void closeClient() {
        stopStreamingRecognition(); // Ensure streaming is stopped first
        if (speechClient != null) {
            try {
                speechClient.close();
                Logger.info("Google Cloud SpeechClient closed.");
            } catch (Exception e) {
                Logger.error("Error closing Google Cloud SpeechClient: " + e.getMessage());
            }
        }
        isClientInitialized = false;
        speechClient = null;
    }

    @Override
    public void onAudioDataCaptured(byte[] audioData, int bytesRead) {
        if (isStreamingActive.get() && clientStream != null) {
            try {
                clientStream.send(StreamingRecognizeRequest.newBuilder()
                        .setAudioContent(ByteString.copyFrom(audioData, 0, bytesRead))
                        .build());
                // Logger.info("Sent " + bytesRead + " bytes to STT stream."); // Too verbose, uncomment for deep debug
            } catch (Exception e) {
                Logger.error("Error sending audio data to STT stream: " + e.getMessage());
                if (streamingRecognitionListener != null) streamingRecognitionListener.onTranscriptionError("Audio stream error: " + e.getMessage());
                stopStreamingRecognition();
            }
        }
    }
}
