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
import com.quilot.exceptions.stt.STTAuthenticationException;
import com.quilot.exceptions.stt.STTException;
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

/**
 * A concrete implementation of {@link SpeechToTextService} that uses the Google Cloud Speech-to-Text API.
 * <p>
 * This class manages the lifecycle of the {@link SpeechClient}, handles streaming recognition requests,
 * and forwards audio data from an {@link com.quilot.audio.input.AudioInputService.AudioDataListener}
 * to the Google Cloud API.
 */
@Getter
public class GoogleCloudSpeechToTextService implements SpeechToTextService, AudioInputService.AudioDataListener {

    private static final int STREAM_RESTART_SECONDS = 270; // Restart stream every 4.5 minutes (well under the 305s limit)

    private SpeechClient speechClient;
    private String credentialPath;
    private boolean isClientInitialized = false;
    private final ISpeechToTextSettingsManager settingsManager;

    private ClientStream<StreamingRecognizeRequest> clientStream;
    private final AtomicBoolean isStreamingActive = new AtomicBoolean(false);
    private ScheduledExecutorService restartExecutor;

    // Store current settings for restarting the stream
    private AudioFormat currentAudioFormat;
    private StreamingRecognitionListener streamingRecognitionListener;

    private ResponseObserver<StreamingRecognizeResponse> responseObserver;

    public GoogleCloudSpeechToTextService(String initialCredentialPath, ISpeechToTextSettingsManager settingsManager) {
        this.settingsManager = Objects.requireNonNull(settingsManager, "Settings manager cannot be null.");
        this.credentialPath = initialCredentialPath;
        Logger.info("GoogleCloudSpeechToTextService instantiated.");
        try {
            initializeClient();
        } catch (STTAuthenticationException e) {
            Logger.warn("Initial STT client initialization failed. The service will remain inactive until configured: " + e.getMessage());
        }
    }

    private void initializeClient() throws STTAuthenticationException {
        if (credentialPath == null || credentialPath.isEmpty()) {
            isClientInitialized = false;
            Logger.warn("Google Cloud credential path is not set. SpeechClient cannot be initialized.");
            return;
        }
        closeClient();
        try (FileInputStream credentialsStream = new FileInputStream(credentialPath)) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
            CredentialsProvider credentialsProvider = FixedCredentialsProvider.create(credentials);
            SpeechSettings speechSettings = SpeechSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();
            this.speechClient = SpeechClient.create(speechSettings);
            isClientInitialized = true;
            Logger.info("Google Cloud SpeechClient created successfully.");
        } catch (IOException e) {
            isClientInitialized = false;
            Logger.error("Failed to read or process Google Cloud credential file.", e);
            throw new STTAuthenticationException("Credential file not found or invalid: " + e.getMessage(), e);
        } catch (Exception e) {
            isClientInitialized = false;
            Logger.error("Unexpected error during SpeechClient initialization.", e);
            throw new STTAuthenticationException("Failed to initialize Google Cloud client, check credentials and project settings.", e);
        }
    }

    public void setCredentialPath(String newCredentialPath) throws STTAuthenticationException {
        if (Objects.equals(this.credentialPath, newCredentialPath)) return;
        this.credentialPath = newCredentialPath;
        Logger.info("Updating Google Cloud credential path to: " + newCredentialPath);
        initializeClient();
    }

    @Override
    public void startStreamingRecognition(AudioFormat audioFormat, StreamingRecognitionListener listener) throws STTException {
        if (!isClientInitialized || speechClient == null) {
            throw new STTException("STT client is not initialized. Please set valid credentials.");
        }
        if (isStreamingActive.get()) {
            Logger.warn("Streaming recognition is already active.");
            return;
        }
        if (audioFormat == null) {
            throw new STTException("An invalid audio format was provided.");
        }

        this.currentAudioFormat = audioFormat;
        this.streamingRecognitionListener = listener;

        isStreamingActive.set(true);
        createAndStartStream();
        startRestartTimer();
    }

    private void createAndStartStream() throws STTException {
        Logger.info("Starting new streaming recognition session...");
        try {
            RecognitionConfigSettings currentSettings = settingsManager.loadSettings();
            StreamingRecognitionConfig streamingConfig = buildStreamingConfig(currentAudioFormat, currentSettings);

            this.responseObserver = new ResponseObserver<>() {
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
                        }
                    }
                }
                @Override
                public void onError(Throwable t) {
                    if (this != GoogleCloudSpeechToTextService.this.responseObserver) {
                        Logger.warn("Ignoring onError from an old, outdated stream observer.");
                        return;
                    }
                    Logger.error("Streaming recognition error.", t);
                    if (streamingRecognitionListener != null) {
                        streamingRecognitionListener.onTranscriptionError(new Exception(t));
                    }
                    closeClientStream();
                }
                @Override
                public void onComplete() {
                    // CHANGE: Check if this is the currently active observer before acting.
                    if (this != GoogleCloudSpeechToTextService.this.responseObserver) {
                        Logger.warn("Ignoring onComplete from an old, outdated stream observer.");
                        return;
                    }
                    Logger.info("Streaming recognition completed by server.");
                    if (streamingRecognitionListener != null) {
                        streamingRecognitionListener.onStreamClosed();
                    }
                    closeClientStream();
                }
            };

            clientStream = speechClient.streamingRecognizeCallable().splitCall(this.responseObserver);
            clientStream.send(StreamingRecognizeRequest.newBuilder().setStreamingConfig(streamingConfig).build());
            Logger.info("Streaming recognition session started successfully.");
        } catch (Exception e) {
            isStreamingActive.set(false);
            closeClientStream();
            Logger.error("Failed to start streaming recognition.", e);
            throw new STTException("Failed to start streaming session: " + e.getMessage(), e);
        }
    }

    private synchronized void restartStream() {
        if (!isStreamingActive.get()) {
            return;
        }

        Logger.info("Proactively restarting STT stream to avoid API timeout...");
        closeClientStream();

        try {
            createAndStartStream();
            startRestartTimer();
        } catch (STTException e) {
            Logger.error("Failed to automatically restart STT stream.", e);
            if (streamingRecognitionListener != null) {
                streamingRecognitionListener.onTranscriptionError(e);
            }
        }
    }

    private void startRestartTimer() {
        if (restartExecutor != null && !restartExecutor.isShutdown()) {
            restartExecutor.shutdownNow();
        }
        restartExecutor = Executors.newSingleThreadScheduledExecutor();
        restartExecutor.schedule(this::restartStream, STREAM_RESTART_SECONDS, TimeUnit.SECONDS);
        Logger.info("STT stream restart scheduled in " + STREAM_RESTART_SECONDS + " seconds.");
    }

    @Override
    public boolean stopStreamingRecognition() {
        if (!isStreamingActive.getAndSet(false)) {
            Logger.warn("No active streaming recognition to stop.");
            return false;
        }
        Logger.info("Stopping streaming recognition session.");
        closeClientStream();
        return true;
    }

    private void closeClientStream() {
        if (clientStream != null) {
            try {
                clientStream.closeSend();
            } catch (Exception e) {
                Logger.error("Error closing client stream send.", e);
            }
            clientStream = null;
        }
        if (restartExecutor != null && !restartExecutor.isShutdown()) {
            restartExecutor.shutdownNow();
            Logger.info("Stream restart timer shut down.");
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
                Logger.error("Error sending audio data to STT stream.", e);
                if (streamingRecognitionListener != null) {
                    streamingRecognitionListener.onTranscriptionError(e);
                }
                stopStreamingRecognition();
            }
        }
    }

    @Override
    public void testCredentials() throws STTAuthenticationException {
        Logger.info("Testing Google Cloud SpeechClient credentials by re-initializing...");
        initializeClient();
        if (!isClientInitialized) {
            throw new STTAuthenticationException("Credentials are not set.", null);
        }
    }

    public void closeClient() {
        stopStreamingRecognition();
        if (speechClient != null) {
            try {
                speechClient.close();
                Logger.info("Google Cloud SpeechClient closed.");
            } catch (Exception e) {
                Logger.error("Error closing Google Cloud SpeechClient.", e);
            }
        }
        isClientInitialized = false;
        speechClient = null;
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
                .setModel(settings.getModel())
                .setEnableAutomaticPunctuation(settings.isEnableAutomaticPunctuation())
                .setEnableWordTimeOffsets(settings.isEnableWordTimeOffsets())
                .setUseEnhanced(settings.isUseEnhanced())
                .setProfanityFilter(settings.isProfanityFilter())
                .setMaxAlternatives(settings.getMaxAlternatives());

        if (settings.isEnableSpeakerDiarization()) {
            configBuilder.setDiarizationConfig(SpeakerDiarizationConfig.newBuilder()
                    .setEnableSpeakerDiarization(true)
                    .setMinSpeakerCount(2)
                    .setMaxSpeakerCount(6)
                    .build());
        }

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
}
