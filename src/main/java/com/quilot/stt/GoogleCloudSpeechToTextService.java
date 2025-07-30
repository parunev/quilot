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
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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

    public GoogleCloudSpeechToTextService(String initialCredentialPath, ISpeechToTextSettingsManager settingsManager) {
        this.credentialPath = initialCredentialPath;
        this.settingsManager = settingsManager;
        Logger.info("GoogleCloudSpeechToTextService instantiated. Client initialization deferred.");
        initializeClient();
    }

    private void initializeClient() {
        if (credentialPath == null || credentialPath.isEmpty()) {
            Logger.warn("Google Cloud credential path is not set. SpeechClient cannot be initialized.");
            isClientInitialized = false;
            return;
        }

        if (speechClient != null && isClientInitialized) return;

        closeClient();

        try (FileInputStream credentialsStream = new FileInputStream(credentialPath)) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
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
            Logger.error("Unexpected error during SpeechClient initialization: " + e.getMessage());
            isClientInitialized = false;
        }
    }

    public void setCredentialPath(String newCredentialPath) {
        if (Objects.equals(this.credentialPath, newCredentialPath)) return;
        this.credentialPath = newCredentialPath;
        Logger.info("Updating Google Cloud credential path to: " + newCredentialPath);
        initializeClient();
    }

    @Override
    public boolean startStreamingRecognition(AudioFormat audioFormat, StreamingRecognitionListener listener) {
        if (!isClientInitialized || speechClient == null) {
            Logger.error("Google Cloud SpeechClient not initialized. Cannot start streaming recognition.");
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
            StreamingRecognitionConfig streamingConfig = buildStreamingConfig(audioFormat, currentSettings);

            ResponseObserver<StreamingRecognizeResponse> responseObserver = new ResponseObserver<>() {
                @Override
                public void onStart(StreamController controller) {
                    Logger.info("Streaming recognition response observer started.");
                }

                @Override
                public void onResponse(StreamingRecognizeResponse response) {
                    if (streamingRecognitionListener != null && !response.getResultsList().isEmpty()) {
                        StreamingRecognitionResult result = response.getResultsList().getFirst();
                        if (!result.getAlternativesList().isEmpty()) {
                            String transcription = result.getAlternativesList().getFirst().getTranscript();
                            streamingRecognitionListener.onTranscriptionResult(transcription, result.getIsFinal());
                            Logger.info(String.format("Streaming STT Result: \"%s\" (Final: %b)", transcription, result.getIsFinal()));
                        }
                    }
                }

                @Override
                public void onError(Throwable t) {
                    isStreamingActive.set(false);
                    Logger.error("Streaming recognition error: " + t.getMessage());
                    if (streamingRecognitionListener != null)
                        streamingRecognitionListener.onTranscriptionError(t.getMessage());
                    closeClientStream();
                }

                @Override
                public void onComplete() {
                    isStreamingActive.set(false);
                    Logger.info("Streaming recognition completed.");
                    if (streamingRecognitionListener != null)
                        streamingRecognitionListener.onStreamClosed();
                    closeClientStream();
                }
            };

            clientStream = speechClient.streamingRecognizeCallable().splitCall(responseObserver);
            clientStream.send(StreamingRecognizeRequest.newBuilder().setStreamingConfig(streamingConfig).build());

            executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.scheduleAtFixedRate(() -> {
                if (isStreamingActive.get() && clientStream != null) {
                    clientStream.send(StreamingRecognizeRequest.newBuilder().setAudioContent(ByteString.EMPTY).build());
                }
            }, 30, 30, TimeUnit.SECONDS);

            Logger.info("Streaming recognition session started successfully.");
            return true;
        } catch (Exception e) {
            isStreamingActive.set(false);
            Logger.error("Failed to start streaming recognition: " + e.getMessage());
            if (listener != null) listener.onTranscriptionError("Failed to start streaming: " + e.getMessage());
            closeClientStream();
            return false;
        }
    }

    private StreamingRecognitionConfig buildStreamingConfig(AudioFormat audioFormat, RecognitionConfigSettings settings) {
        RecognitionConfig.AudioEncoding encoding = RecognitionConfig.AudioEncoding.LINEAR16;
        if (audioFormat.getEncoding() == AudioFormat.Encoding.ULAW) {
            encoding = RecognitionConfig.AudioEncoding.MULAW;
        }

        RecognitionConfig.Builder configBuilder = RecognitionConfig.newBuilder()
                .setEncoding(encoding)
                .setSampleRateHertz((int) audioFormat.getSampleRate())
                .setLanguageCode(settings.getLanguageCode())
                .setEnableAutomaticPunctuation(settings.isEnableAutomaticPunctuation())
                .setEnableWordTimeOffsets(settings.isEnableWordTimeOffsets())
                .setModel(settings.getModel());

        List<SpeechContext> contexts = settings.getSpeechContextsAsList().stream()
                .map(phrase -> SpeechContext.newBuilder().addPhrases(phrase).build())
                .collect(Collectors.toList());
        configBuilder.addAllSpeechContexts(contexts);

        return StreamingRecognitionConfig.newBuilder()
                .setConfig(configBuilder.build())
                .setInterimResults(settings.isInterimTranscription())
                .setSingleUtterance(settings.isEnableSingleUtterance())
                .build();
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

    private void closeClientStream() {
        if (clientStream != null) {
            try {
                clientStream.closeSend();
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
    public void onAudioDataCaptured(byte[] audioData, int bytesRead) {
        if (isStreamingActive.get() && clientStream != null) {
            try {
                clientStream.send(StreamingRecognizeRequest.newBuilder()
                        .setAudioContent(ByteString.copyFrom(audioData, 0, bytesRead))
                        .build());
            } catch (Exception e) {
                Logger.error("Error sending audio data to STT stream: " + e.getMessage());
                if (streamingRecognitionListener != null)
                    streamingRecognitionListener.onTranscriptionError("Audio stream error: " + e.getMessage());
                stopStreamingRecognition();
            }
        }
    }

    @Override
    public boolean testCredentials() {
        Logger.info("Attempting to test Google Cloud SpeechClient credentials...");
        initializeClient();
        return isClientInitialized && speechClient != null;
    }

    public void closeClient() {
        stopStreamingRecognition();
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
}
