package com.quilot.ui;

import com.quilot.ai.IAIService;
import com.quilot.ai.VertexAIService;
import com.quilot.ai.settings.AISettingsManager;
import com.quilot.audio.input.AudioInputService;
import com.quilot.audio.input.SystemAudioInputService;
import com.quilot.audio.ouput.AudioOutputService;
import com.quilot.audio.ouput.SystemAudioOutputService;
import com.quilot.exceptions.audio.AudioDeviceException;
import com.quilot.exceptions.audio.AudioException;
import com.quilot.exceptions.stt.STTException;
import com.quilot.stt.GoogleCloudSpeechToTextService;
import com.quilot.stt.SpeechToTextService;
import com.quilot.stt.ISpeechToTextSettingsManager;
import com.quilot.stt.settings.RecognitionConfigSettings;
import com.quilot.stt.settings.SpeechToTextSettingsManager;
import com.quilot.ui.help.CredentialsSetupDialog;
import com.quilot.ui.help.GoogleCloudSetupGuideDialog;
import com.quilot.ui.help.SetupGuideDialog;
import com.quilot.ui.settings.AISettingsDialog;
import com.quilot.ui.settings.STTSettingsDialog;
import com.quilot.utils.CredentialManager;
import com.quilot.utils.Logger;
import com.quilot.utils.QuestionDetector;
import lombok.Getter;

import javax.sound.sampled.AudioFormat;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * The main user interface frame for the application.
 * <p>
 * This class serves as the central hub of the application, responsible for:
 * <ul>
 * <li>Initializing all core services (AI, STT, Audio I/O).</li>
 * <li>Constructing the main UI layout using a {@link UIBuilder}.</li>
 * <li>Binding all event listeners for user interactions.</li>
 * <li>Handling graceful shutdown of services when the window is closed.</li>
 * <li>Orchestrating the flow of data between the UI and the backend services.</li>
 * </ul>
 * It is designed to handle startup failures gracefully by guiding the user
 * through the initial configuration process if necessary.
 */
@Getter
public class MainFrame extends JFrame {

    // UI Components
    private JTextArea transcribedAudioArea;
    private JTextArea aiResponseArea;
    private JTextArea logArea;
    private JComboBox<String> outputDeviceComboBox;
    private JSlider volumeSlider;
    private JButton testVolumeButton;
    private JComboBox<String> inputDeviceComboBox;
    private JButton startInputRecordingButton;
    private JButton stopInputRecordingButton;
    private JButton playRecordedInputButton;
    private JButton setupGuideButton;
    private JButton credentialsButton;
    private JButton googleCloudSetupGuideButton;
    private JButton sttSettingsButton;
    private JButton aiSettingsButton;

    // Services and Managers
    private ElapsedTimerManager timerManager;
    private AudioOutputService audioOutputService;
    private AudioInputService audioInputService;
    private SpeechToTextService speechToTextService;
    private CredentialManager credentialManager;
    private ISpeechToTextSettingsManager sttSettingsManager;
    private IAIService aiService;
    private QuestionDetector questionDetector;

    /**
     * Constructs the MainFrame.
     * This constructor orchestrates the initialization of all backend services and UI components.
     * If a critical service fails to initialize, it displays a fatal error dialog and exits.
     * If non-critical services (like AI or STT) are unconfigured, it prompts the user
     * to set them up after the UI is visible.
     */
    public MainFrame() {
        initializeServices();
        initializeUI();
        bindListeners();

        Logger.info("Quilot UI initialized.");
        appendToLogArea("UI initialized. Ready to start.");

        performPostStartupChecks();
    }

    private void performPostStartupChecks() {
        SwingUtilities.invokeLater(() -> {
            boolean isAiServiceReady = ((VertexAIService) aiService).isClientInitialized();
            boolean isSttServiceReady = ((GoogleCloudSpeechToTextService) speechToTextService).isClientInitialized();

            if (!isAiServiceReady || !isSttServiceReady) {
                JOptionPane.showMessageDialog(this,
                        "Welcome! To get started, please set your Google Cloud credentials.",
                        "Configuration Needed",
                        JOptionPane.INFORMATION_MESSAGE);

                CredentialsSetupDialog dialog = new CredentialsSetupDialog(this, credentialManager, (GoogleCloudSpeechToTextService) speechToTextService);
                dialog.setVisible(true);
            }
        });
    }

    private void initializeServices() {
        timerManager = new ElapsedTimerManager();
        credentialManager = new CredentialManager();

        audioOutputService = new SystemAudioOutputService();
        audioInputService = new SystemAudioInputService();
        sttSettingsManager = new SpeechToTextSettingsManager();
        questionDetector = new QuestionDetector();

        String savedCredentialPath = credentialManager.loadGoogleCloudCredentialPath();

        aiService = new VertexAIService(savedCredentialPath, new AISettingsManager());
        speechToTextService = new GoogleCloudSpeechToTextService(savedCredentialPath, sttSettingsManager);
    }

    private void initializeUI() {
        setTitle("Quilot");
        setMinimumSize(new Dimension(1200, 800));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        UIBuilder uiBuilder = new UIBuilder(audioOutputService, audioInputService, timerManager);

        transcribedAudioArea = uiBuilder.getTranscribedAudioArea();
        aiResponseArea = uiBuilder.getAiResponseArea();
        logArea = uiBuilder.getLogArea();
        outputDeviceComboBox = uiBuilder.getOutputDeviceComboBox();
        volumeSlider = uiBuilder.getVolumeSlider();
        testVolumeButton = uiBuilder.getTestVolumeButton();
        inputDeviceComboBox = uiBuilder.getInputDeviceComboBox();
        startInputRecordingButton = uiBuilder.getStartInputRecordingButton();
        stopInputRecordingButton = uiBuilder.getStopInputRecordingButton();
        playRecordedInputButton = uiBuilder.getPlayRecordedInputButton();
        setupGuideButton = uiBuilder.getSetupGuideButton();
        credentialsButton = uiBuilder.getCredentialsButton();
        googleCloudSetupGuideButton = uiBuilder.getGoogleCloudSetupGuideButton();
        sttSettingsButton = uiBuilder.getSttSettingsButton();
        aiSettingsButton = uiBuilder.getAiSettingsButton();

        JPanel mainPanel = new JPanel(new GridBagLayout());
        uiBuilder.setupLayout(mainPanel);
        add(mainPanel);
        pack();
    }

    private void bindListeners() {
        audioInputService.setAudioDataListener((audioData, bytesRead) ->
                ((GoogleCloudSpeechToTextService) speechToTextService).onAudioDataCaptured(audioData, bytesRead));

        addAudioOutputListeners();
        addAudioInputListeners();
        addWindowListeners();
        addHelpListeners();
        addSettingsListeners();
        addSTTSettingsListeners();
        addAISettingsListeners();
    }

    private void addAISettingsListeners() {
        aiSettingsButton.addActionListener(_ -> {
            Logger.info("AI Settings button clicked. Displaying AI settings dialog.");
            AISettingsDialog dialog = new AISettingsDialog(this, aiService.getSettingsManager(), (VertexAIService) aiService);
            dialog.setVisible(true);
        });
    }

    private void addAudioOutputListeners() {
        outputDeviceComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selectedDevice = (String) e.getItem();
                try {
                    audioOutputService.selectOutputDevice(selectedDevice);
                    appendToLogArea("Selected audio output device: " + selectedDevice);
                    volumeSlider.setEnabled(true);
                    testVolumeButton.setEnabled(true);
                    audioOutputService.setVolume(volumeSlider.getValue() / 100.0f);
                } catch (AudioDeviceException ex) {
                    appendToLogArea("Failed to select audio output device: " + selectedDevice);
                    volumeSlider.setEnabled(false);
                    testVolumeButton.setEnabled(false);
                    JOptionPane.showMessageDialog(this,
                            "Could not open audio output device: " + selectedDevice + "\nIt may be in use or disconnected.",
                            "Audio Device Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        volumeSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (!source.getValueIsAdjusting()) {
                float volume = source.getValue() / 100.0f;
                audioOutputService.setVolume(volume);
            }
        });

        testVolumeButton.addActionListener(_ -> {
            try {
                audioOutputService.playTestSound();
                appendToLogArea("Test sound played.");
            } catch (AudioDeviceException e) {
                appendToLogArea("Failed to play test sound: " + e.getMessage());
                JOptionPane.showMessageDialog(this,
                        "Could not play test sound. Please ensure an output device is selected.",
                        "Playback Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // --- ENTIRE METHOD REPLACED ---
    private void addAudioInputListeners() {
        inputDeviceComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selectedDevice = (String) e.getItem();
                try {
                    audioInputService.selectInputDevice(selectedDevice);
                    appendToLogArea("Selected audio input device: " + selectedDevice);
                } catch (AudioDeviceException ex) {
                    appendToLogArea("Failed to select audio input device: " + selectedDevice + " - " + ex.getMessage());
                    JOptionPane.showMessageDialog(this,
                            "Could not open audio device: " + selectedDevice + "\nIt may be in use by another application or disconnected.",
                            "Audio Device Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                updateAudioInputButtonStates();
            }
        });

        startInputRecordingButton.addActionListener(_ -> {
            try {
                audioInputService.startRecording();
                appendToLogArea("Started capturing audio from input device.");
                timerManager.startElapsedTimer();
                startInputRecordingButton.setEnabled(false);
                stopInputRecordingButton.setEnabled(true);
                playRecordedInputButton.setEnabled(false);

                try {
                    speechToTextService.startStreamingRecognition(audioInputService.getAudioFormat(), new SpeechToTextService.StreamingRecognitionListener() {
                        @Override
                        public void onTranscriptionResult(String transcription, boolean isFinal) {
                            SwingUtilities.invokeLater(() -> {
                                if (isFinal) {
                                    transcribedAudioArea.append("Interviewer (Final): '" + transcription + "'\n");
                                    transcribedAudioArea.setCaretPosition(transcribedAudioArea.getDocument().getLength());

                                    RecognitionConfigSettings settings = sttSettingsManager.loadSettings();
                                    String currentLanguage = sttSettingsManager.loadSettings().getLanguageCode();

                                    if (!settings.isEnableQuestionDetection() || questionDetector.isQuestion(transcription, currentLanguage)) {

                                        if (settings.isEnableQuestionDetection()) {
                                            appendToLogArea("Question detected. Sending to AI...");
                                        } else {
                                            appendToLogArea("Question detection disabled. Sending all final transcripts to AI...");
                                        }

                                        aiService.generateResponse(transcription, new IAIService.AIResponseListener() {
                                            @Override
                                            public void onResponse(String aiResponse) {
                                                SwingUtilities.invokeLater(() -> {
                                                    aiResponseArea.append("AI (Response): '" + aiResponse + "'\n");
                                                    aiResponseArea.setCaretPosition(aiResponseArea.getDocument().getLength());
                                                });
                                            }

                                            @Override
                                            public void onError(String errorMessage) {
                                                SwingUtilities.invokeLater(() -> {
                                                    aiResponseArea.append("AI (Error): " + errorMessage + "\n");
                                                    aiResponseArea.setCaretPosition(aiResponseArea.getDocument().getLength());
                                                });
                                                appendToLogArea("AI Response Error: " + errorMessage);
                                            }
                                        });
                                    } else {
                                        appendToLogArea("Non-question detected. Ignoring for AI response.");
                                    }
                                } else {
                                    String existingText = transcribedAudioArea.getText();
                                    int lastNewline = existingText.lastIndexOf('\n');
                                    if (lastNewline != -1 && existingText.substring(lastNewline + 1).startsWith("Interviewer (Interim):")) {
                                        transcribedAudioArea.replaceRange("Interviewer (Interim): '" + transcription + "'", lastNewline + 1, existingText.length());
                                    } else {
                                        transcribedAudioArea.append("Interviewer (Interim): '" + transcription + "'\n");
                                    }
                                    transcribedAudioArea.setCaretPosition(transcribedAudioArea.getDocument().getLength());
                                }
                            });
                        }

                        @Override
                        public void onTranscriptionError(Exception error) {
                            String errorMessage = "A transcription error occurred: " + error.getMessage();
                            appendToLogArea(errorMessage);
                            aiResponseArea.append("STT (Error): " + errorMessage + "\n");
                        }
                    });
                } catch (STTException ex) {
                    appendToLogArea("Failed to start STT streaming recognition: " + ex.getMessage());
                    JOptionPane.showMessageDialog(this,
                            "Could not start transcription service:\n" + ex.getMessage() + "\nPlease check credentials and network connection.",
                            "Speech-to-Text Error",
                            JOptionPane.ERROR_MESSAGE);
                    audioInputService.stopRecording(); // Stop audio input if STT streaming fails
                    startInputRecordingButton.setEnabled(true);
                    stopInputRecordingButton.setEnabled(false);
                }
            } catch (AudioDeviceException ex) {
                appendToLogArea("Failed to start audio input capture: " + ex.getMessage());
                JOptionPane.showMessageDialog(this,
                        "Could not start recording.\nPlease ensure your microphone is properly connected and not in use.",
                        "Recording Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        stopInputRecordingButton.addActionListener(_ -> {
            if (audioInputService.stopRecording()) {
                appendToLogArea("Stopped capturing audio from input device.");
                timerManager.stopElapsedTimer();
                startInputRecordingButton.setEnabled(true);
                stopInputRecordingButton.setEnabled(false);
                playRecordedInputButton.setEnabled(audioInputService.getRecordedAudioData().length > 0);
            } else {
                appendToLogArea("Failed to stop audio input capture.");
            }

            if (speechToTextService.stopStreamingRecognition()) {
                appendToLogArea("Stopped STT streaming recognition.");
            } else {
                appendToLogArea("Failed to stop STT streaming recognition.");
            }
        });

        playRecordedInputButton.addActionListener(_ -> {
            byte[] recordedData = audioInputService.getRecordedAudioData();
            AudioFormat format = audioInputService.getAudioFormat();
            if (recordedData.length > 0 && format != null) {
                appendToLogArea("Playing recorded input audio...");
                try {
                    audioOutputService.playAudioData(recordedData, format);
                    audioInputService.clearRecordedAudioData();
                    playRecordedInputButton.setEnabled(false);
                    appendToLogArea("Recorded input audio played and cleared.");
                } catch (AudioException ex) {
                    appendToLogArea("Failed to play recorded audio: " + ex.getMessage());
                    JOptionPane.showMessageDialog(this,
                            "Could not play recorded audio.\n" + ex.getMessage(),
                            "Playback Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                appendToLogArea("No recorded audio data to play.");
                playRecordedInputButton.setEnabled(false);
            }
        });
    }

    private void updateAudioInputButtonStates() {
        boolean deviceIsSelected = audioInputService.isDeviceSelected();
        startInputRecordingButton.setEnabled(deviceIsSelected);
        stopInputRecordingButton.setEnabled(false);
        playRecordedInputButton.setEnabled(false);
    }

    private void addWindowListeners() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                audioOutputService.close();
                audioInputService.close();
                if (speechToTextService instanceof GoogleCloudSpeechToTextService) {
                    ((GoogleCloudSpeechToTextService) speechToTextService).closeClient();
                }
                if (aiService instanceof VertexAIService) {
                    ((VertexAIService) aiService).closeClient();
                }
                Logger.info("Application closing. Audio resources released.");
            }
        });
    }

    private void addHelpListeners() {
        setupGuideButton.addActionListener(_ -> {
            Logger.info("Setup Guide button clicked. Displaying Blackhole setup guide.");
            SetupGuideDialog dialog = new SetupGuideDialog(this);
            dialog.setVisible(true);
        });

        googleCloudSetupGuideButton.addActionListener(_ -> {
            Logger.info("Google Cloud Setup Guide button clicked. Displaying Google Cloud setup guide.");
            GoogleCloudSetupGuideDialog dialog = new GoogleCloudSetupGuideDialog(this);
            dialog.setVisible(true);
        });
    }

    private void addSettingsListeners() {
        credentialsButton.addActionListener(_ -> {
            Logger.info("STT Credentials button clicked. Displaying credentials dialog.");
            CredentialsSetupDialog dialog = new CredentialsSetupDialog(this, credentialManager, (GoogleCloudSpeechToTextService) speechToTextService);
            dialog.setVisible(true);
        });
    }

    private void addSTTSettingsListeners() {
        sttSettingsButton.addActionListener(_ -> {
            Logger.info("STT Settings button clicked. Displaying STT settings dialog.");
            STTSettingsDialog dialog = new STTSettingsDialog(this, sttSettingsManager, (GoogleCloudSpeechToTextService) speechToTextService);
            dialog.setVisible(true);
        });
    }

    public void appendToLogArea(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}