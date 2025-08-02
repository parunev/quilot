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
import com.quilot.stt.GoogleCloudSpeechToTextService;
import com.quilot.stt.SpeechToTextService;
import com.quilot.stt.ISpeechToTextSettingsManager;
import com.quilot.stt.settings.SpeechToTextSettingsManager;
import com.quilot.ui.help.CredentialsSetupDialog;
import com.quilot.ui.help.GoogleCloudSetupGuideDialog;
import com.quilot.ui.help.SetupGuideDialog;
import com.quilot.ui.settings.AISettingsDialog;
import com.quilot.ui.settings.STTSettingsDialog;
import com.quilot.utils.CredentialManager;
import com.quilot.utils.Logger;
import lombok.Getter;

import javax.sound.sampled.AudioFormat;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * The main user interface frame for the AI Interview Copilot.
 * This class now primarily focuses on setting up the frame,
 * orchestrating UI components via UIBuilder, and handling user interactions.
 */
@Getter
public class MainFrame extends JFrame {

    // UI COMPONENTS
    private final JTextArea transcribedAudioArea;
    private final JTextArea aiResponseArea;
    private final JTextArea logArea;

    // Audio output components
    private final JComboBox<String> outputDeviceComboBox;
    private final JSlider volumeSlider;
    private final JButton testVolumeButton;

    // Audio input components
    private final JComboBox<String> inputDeviceComboBox;
    private final JButton startInputRecordingButton;
    private final JButton stopInputRecordingButton;
    private final JButton playRecordedInputButton;
    private final JButton setupGuideButton;
    private final JButton credentialsButton;
    private final JButton googleCloudSetupGuideButton;
    private final JButton sttSettingsButton;
    private final JButton aiSettingsButton;

    // Managers for specific functionalities
    private final ElapsedTimerManager timerManager;
    private final UIBuilder uiBuilder;
    private final AudioOutputService audioOutputService;
    private final AudioInputService audioInputService;
    private final SpeechToTextService speechToTextService;
    private final CredentialManager credentialManager;
    private final ISpeechToTextSettingsManager sttSettingsManager;
    private final IAIService aiService;

    /**
     * Constructor for the MainFrame.
     * Initializes the UI components and sets up the layout.
     */
    public MainFrame() {

        // FRAME PROPERTIES
        setTitle("Quilot");
        pack();
        setMinimumSize(new Dimension(1200, 800));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // MANAGERS
        timerManager = new ElapsedTimerManager();
        audioOutputService = new SystemAudioOutputService();
        audioInputService = new SystemAudioInputService();
        credentialManager = new CredentialManager();
        sttSettingsManager = new SpeechToTextSettingsManager();
        this.aiService = new VertexAIService(credentialManager.loadGoogleCloudCredentialPath(), new AISettingsManager());

        // Load credential path and initialize STT service
        String savedCredentialPath = credentialManager.loadGoogleCloudCredentialPath();
        speechToTextService = new GoogleCloudSpeechToTextService(savedCredentialPath, sttSettingsManager);

        uiBuilder = new UIBuilder(audioOutputService, audioInputService, timerManager);

        // UI COMPONENTS
        transcribedAudioArea = uiBuilder.getTranscribedAudioArea();
        aiResponseArea = uiBuilder.getAiResponseArea();
        logArea = uiBuilder.getLogArea();

        // AUDIO OUTPUT COMPONENTS
        outputDeviceComboBox = uiBuilder.getOutputDeviceComboBox();
        volumeSlider = uiBuilder.getVolumeSlider();
        testVolumeButton = uiBuilder.getTestVolumeButton();

        // AUDIO INPUT COMPONENTS
        inputDeviceComboBox = uiBuilder.getInputDeviceComboBox();
        startInputRecordingButton = uiBuilder.getStartInputRecordingButton();
        stopInputRecordingButton = uiBuilder.getStopInputRecordingButton();
        playRecordedInputButton = uiBuilder.getPlayRecordedInputButton();
        setupGuideButton = uiBuilder.getSetupGuideButton();
        credentialsButton = uiBuilder.getCredentialsButton();
        googleCloudSetupGuideButton = uiBuilder.getGoogleCloudSetupGuideButton();
        sttSettingsButton = uiBuilder.getSttSettingsButton();
        aiSettingsButton = uiBuilder.getAiSettingsButton();

        // Audio input data listener
        audioInputService.setAudioDataListener((audioData, bytesRead) ->
                ((GoogleCloudSpeechToTextService) speechToTextService).onAudioDataCaptured(audioData, bytesRead));

        // LAYOUT
        JPanel mainPanel = new JPanel(new GridBagLayout());
        uiBuilder.setupLayout(mainPanel);
        add(mainPanel);

        // Add listeners
        addAudioOutputListeners();
        addAudioInputListeners();
        addWindowListeners();
        addHelpListeners();
        addSettingsListeners();
        addSTTSettingsListeners();
        addAISettingsListeners();

        Logger.info("Quilot UI initialized.");
        appendToLogArea("UI initialized. Ready to start.");
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

                if (!speechToTextService.startStreamingRecognition(audioInputService.getAudioFormat(), new SpeechToTextService.StreamingRecognitionListener() {
                    private final StringBuilder currentInterimTranscription = new StringBuilder();
                    private String lastFinalTranscription = "";

                    @Override
                    public void onTranscriptionResult(String transcription, boolean isFinal) {
                        SwingUtilities.invokeLater(() -> {
                            if (isFinal) {
                                transcribedAudioArea.append("Interviewer (Final): '" + transcription + "'\n");
                                lastFinalTranscription = transcription;
                                currentInterimTranscription.setLength(0);
                                transcribedAudioArea.setCaretPosition(transcribedAudioArea.getDocument().getLength());

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
                })) {
                    appendToLogArea("Failed to start STT streaming recognition.");
                    audioInputService.stopRecording();
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