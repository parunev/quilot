package com.quilot.ui;

import com.quilot.audio.input.AudioInputService;
import com.quilot.audio.input.SystemAudioInputService;
import com.quilot.audio.ouput.AudioOutputService;
import com.quilot.audio.ouput.SystemAudioOutputService;
import com.quilot.stt.GoogleCloudSpeechToTextService;
import com.quilot.stt.SpeechToTextService;
import com.quilot.stt.ISpeechToTextSettingsManager;
import com.quilot.stt.settings.SpeechToTextSettingsManager;
import com.quilot.ui.help.CredentialsSetupDialog;
import com.quilot.ui.help.GoogleCloudSetupGuideDialog;
import com.quilot.ui.help.SetupGuideDialog;
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
    private final JButton startButton;
    private final JButton stopButton;
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

    // Managers for specific functionalities
    private final InterviewTimerManager timerManager;
    private final UIBuilder uiBuilder;
    private final AudioOutputService audioOutputService;
    private final AudioInputService audioInputService;
    private final SpeechToTextService speechToTextService;
    private final CredentialManager credentialManager;
    private final ISpeechToTextSettingsManager sttSettingsManager;

    /**
     * Constructor for the MainFrame.
     * Initializes the UI components and sets up the layout.
     */
    public MainFrame() {

        // FRAME PROPERTIES
        setTitle("Quilot");
        setSize(1200, 800); // Increased size to accommodate new input panel
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // MANAGERS
        timerManager = new InterviewTimerManager();
        audioOutputService = new SystemAudioOutputService();
        audioInputService = new SystemAudioInputService();
        this.credentialManager = new CredentialManager();
        this.sttSettingsManager = new SpeechToTextSettingsManager();

        // Load credential path at startup and initialize STT service
        String savedCredentialPath = credentialManager.loadGoogleCloudCredentialPath();
        // Pass the settings manager to the STT service
        this.speechToTextService = new GoogleCloudSpeechToTextService(savedCredentialPath, sttSettingsManager);

        uiBuilder = new UIBuilder(audioOutputService, audioInputService, timerManager);

        // UI COMPONENTS
        this.startButton = uiBuilder.getStartButton();
        this.stopButton = uiBuilder.getStopButton();
        this.transcribedAudioArea = uiBuilder.getTranscribedAudioArea();
        this.aiResponseArea = uiBuilder.getAiResponseArea();
        this.logArea = uiBuilder.getLogArea();

        // UI AUDIO COMPONENTS (Output)
        this.outputDeviceComboBox = uiBuilder.getOutputDeviceComboBox();
        this.volumeSlider = uiBuilder.getVolumeSlider();
        this.testVolumeButton = uiBuilder.getTestVolumeButton();

        // UI AUDIO COMPONENTS (Input)
        this.inputDeviceComboBox = uiBuilder.getInputDeviceComboBox();
        this.startInputRecordingButton = uiBuilder.getStartInputRecordingButton();
        this.stopInputRecordingButton = uiBuilder.getStopInputRecordingButton();
        this.playRecordedInputButton = uiBuilder.getPlayRecordedInputButton();
        this.setupGuideButton = uiBuilder.getSetupGuideButton();
        this.credentialsButton = uiBuilder.getCredentialsButton();
        this.googleCloudSetupGuideButton = uiBuilder.getGoogleCloudSetupGuideButton();
        this.sttSettingsButton = uiBuilder.getSttSettingsButton();

        // Set up audio data listener for input service
        // Set up audio data listener for input service
        audioInputService.setAudioDataListener((audioData, bytesRead) -> {
            // In streaming mode, audio data is sent directly to the STT service's stream
            // The STT service itself manages the streaming recognition lifecycle
            ((GoogleCloudSpeechToTextService) speechToTextService).onAudioDataCaptured(audioData, bytesRead);
        });

        // LAYOUT
        JPanel mainPanel = new JPanel(new GridBagLayout());
        uiBuilder.setupLayout(mainPanel); // UIBuilder now handles passing its own dependencies
        add(mainPanel);

        // Add all listeners in dedicated methods
        addMainControlListeners();
        addAudioOutputListeners();
        addAudioInputListeners();
        addWindowListeners();
        addHelpListeners();
        addSettingsListeners();
        addSTTSettingsListeners();

        Logger.info("Quilot UI initialized.");
        appendToLogArea("UI initialized. Ready to start.");
    }


    /**
     * Adds listeners for the main Start/Stop interview buttons.
     */
    private void addMainControlListeners() {
        startButton.addActionListener(_ -> {
            try {
                Logger.info("Start button clicked.");
                appendToLogArea("Starting interview simulation...");
                startButton.setEnabled(false);
                stopButton.setEnabled(true);

                timerManager.startInterviewTimer();

                String interviewerQuestion = "Hello, welcome to the interview. Please tell me about yourself.";
                aiResponseArea.setText("AI (Interviewer): '" + interviewerQuestion + "'");

                transcribedAudioArea.setText("Interviewer (Transcribed): '" + interviewerQuestion + "'");
            } catch (Exception ex) {
                Logger.error("Error starting interview: " + ex.getMessage());
                appendToLogArea("ERROR: Could not start interview. Check logs.");
            }
        });

        stopButton.addActionListener(_ -> {
            try {
                Logger.info("Stop button clicked.");
                appendToLogArea("Stopping interview simulation...");
                startButton.setEnabled(true);
                stopButton.setEnabled(false);

                timerManager.stopInterviewTimer();

                // Stop any active audio input recording
                audioInputService.stopRecording();
                startInputRecordingButton.setEnabled(true);
                stopInputRecordingButton.setEnabled(false);
                playRecordedInputButton.setEnabled(true); // Enable play button after stopping recording

                transcribedAudioArea.append("\nInterview ended.");
                aiResponseArea.append("\nAI (Interviewer): 'Thank you for your time.'");
            }
            catch (Exception ex) {
                Logger.error("Error stopping interview: " + ex.getMessage());
                appendToLogArea("ERROR: Could not stop interview. Check logs.");
            }
        });
    }

    /**
     * Adds listeners for audio output device selection, volume control, and test sound.
     */
    private void addAudioOutputListeners() {
        outputDeviceComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selectedDevice = (String) e.getItem();
                if (audioOutputService.selectOutputDevice(selectedDevice)) {
                    appendToLogArea("Selected audio output device: " + selectedDevice);
                    volumeSlider.setEnabled(true);
                    testVolumeButton.setEnabled(true);
                    audioOutputService.setVolume(volumeSlider.getValue() / 100.0f);
                } else {
                    appendToLogArea("Failed to select audio output device: " + selectedDevice);
                    volumeSlider.setEnabled(false);
                    testVolumeButton.setEnabled(false);
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
            audioOutputService.playTestSound();
            appendToLogArea("Test sound played.");
        });
    }

    /**
     * Adds listeners for audio input device selection, recording controls, and playback of recorded audio.
     */
    private void addAudioInputListeners() {
        inputDeviceComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selectedDevice = (String) e.getItem();
                if (audioInputService.selectInputDevice(selectedDevice)) {
                    appendToLogArea("Selected audio input device: " + selectedDevice);
                    startInputRecordingButton.setEnabled(true);
                    stopInputRecordingButton.setEnabled(false);
                    playRecordedInputButton.setEnabled(false);
                } else {
                    appendToLogArea("Failed to select audio input device: " + selectedDevice);
                    startInputRecordingButton.setEnabled(false);
                    stopInputRecordingButton.setEnabled(false);
                    playRecordedInputButton.setEnabled(false);
                }
            }
        });

        startInputRecordingButton.addActionListener(_ -> {
            // Start both audio input recording and STT streaming
            if (audioInputService.startRecording()) {
                appendToLogArea("Started capturing audio from input device.");
                startInputRecordingButton.setEnabled(false);
                stopInputRecordingButton.setEnabled(true);
                playRecordedInputButton.setEnabled(false); // Disable play button while recording

                if (!speechToTextService.startStreamingRecognition(audioInputService.getAudioFormat(), new SpeechToTextService.StreamingRecognitionListener() {
                    private StringBuilder currentTranscription = new StringBuilder(); // FOR NOW NOT FINAL (WILL BE USED FOR AI PROBABLY)
                    private String lastInterimText = "";

                    @Override
                    public void onTranscriptionResult(String transcription, boolean isFinal) {
                        SwingUtilities.invokeLater(() -> {
                            if (isFinal) {
                                currentTranscription.append(transcription).append(" "); // ONLY ONE USAGE
                                transcribedAudioArea.append("Interviewer (Final): '" + transcription.trim() + "'\n");

                                // Clear last interim
                                lastInterimText = "";
                            } else {
                                // Remove last interim and append updated interim
                                String currentText = transcribedAudioArea.getText();
                                if (!lastInterimText.isEmpty() && currentText.endsWith(lastInterimText)) {
                                    transcribedAudioArea.setText(currentText.substring(0, currentText.length() - lastInterimText.length()));
                                }
                                lastInterimText = "Interviewer (Interim): '" + transcription + "'\n";
                                transcribedAudioArea.append(lastInterimText);
                            }

                            transcribedAudioArea.setCaretPosition(transcribedAudioArea.getDocument().getLength());
                        });
                    }

                    @Override
                    public void onTranscriptionError(String errorMessage) {
                        SwingUtilities.invokeLater(() -> {
                            transcribedAudioArea.append("Interviewer (STT Error): " + errorMessage + "\n");
                            transcribedAudioArea.setCaretPosition(transcribedAudioArea.getDocument().getLength());
                        });
                        appendToLogArea("STT Streaming Error: " + errorMessage);
                    }

                    @Override
                    public void onStreamClosed() {
                        appendToLogArea("STT Streaming session closed.");
                    }
                })) {
                    appendToLogArea("Failed to start STT streaming recognition.");
                    audioInputService.stopRecording(); // Stop audio input if STT streaming fails
                    startInputRecordingButton.setEnabled(true);
                    stopInputRecordingButton.setEnabled(false);
                }
            } else {
                appendToLogArea("Failed to start audio input capture.");
            }
        });

        stopInputRecordingButton.addActionListener(_ -> {
            // Stop both audio input recording and STT streaming
            if (audioInputService.stopRecording()) {
                appendToLogArea("Stopped capturing audio from input device.");
                startInputRecordingButton.setEnabled(true);
                stopInputRecordingButton.setEnabled(false);
                // Enable play button only if there's actual data recorded
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
                audioOutputService.playAudioData(recordedData, format);
                audioInputService.clearRecordedAudioData(); // Clear after playing
                playRecordedInputButton.setEnabled(false); // Disable after playing/clearing
                appendToLogArea("Recorded input audio played and cleared.");
            } else {
                appendToLogArea("No recorded audio data to play.");
                playRecordedInputButton.setEnabled(false);
            }
        });
    }

    /**
     * Adds a window listener to ensure audio resources are closed on application exit.
     */
    private void addWindowListeners() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                audioOutputService.close();
                audioInputService.close(); // Close input audio resources
                // Close the STT client if it has a close method
                if (speechToTextService instanceof GoogleCloudSpeechToTextService) {
                    ((GoogleCloudSpeechToTextService) speechToTextService).closeClient();
                }
                Logger.info("Application closing. Audio resources released.");
            }
        });
    }

    /**
     * Adds listeners for help-related actions (e.g., showing setup guides).
     */
    private void addHelpListeners() {
        setupGuideButton.addActionListener(_ -> {
            Logger.info("Setup Guide button clicked. Displaying Blackhole setup guide.");
            SetupGuideDialog dialog = new SetupGuideDialog(this); // 'this' refers to MainFrame (owner)
            dialog.setVisible(true);
        });

        googleCloudSetupGuideButton.addActionListener(_ -> { // Listener for the new Google Cloud Setup Guide button
            Logger.info("Google Cloud Setup Guide button clicked. Displaying Google Cloud setup guide.");
            GoogleCloudSetupGuideDialog dialog = new GoogleCloudSetupGuideDialog(this);
            dialog.setVisible(true);
        });
    }

    /**
     * Adds listeners for settings-related actions (e.g., showing credentials dialogs).
     */
    private void addSettingsListeners() {
        credentialsButton.addActionListener(_ -> {
            Logger.info("STT Credentials button clicked. Displaying credentials dialog.");
            // Pass credentialManager and speechToTextService to the dialog
            CredentialsSetupDialog dialog = new CredentialsSetupDialog(this, credentialManager, (GoogleCloudSpeechToTextService) speechToTextService);
            dialog.setVisible(true);
        });
    }

    /**
     * Adds listeners for STT settings actions.
     */
    private void addSTTSettingsListeners() {
        sttSettingsButton.addActionListener(_ -> {
            Logger.info("STT Settings button clicked. Displaying STT settings dialog.");
            // Pass the settings manager and the STT service (to update its internal config)
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
