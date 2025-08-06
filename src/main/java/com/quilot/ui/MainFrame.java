package com.quilot.ui;

import com.quilot.ai.IAIService;
import com.quilot.ai.VertexAIService;
import com.quilot.ai.settings.AISettingsManager;
import com.quilot.audio.input.AudioInputService;
import com.quilot.audio.input.SystemAudioInputService;
import com.quilot.audio.ouput.AudioOutputService;
import com.quilot.audio.ouput.SystemAudioOutputService;
import com.quilot.db.DatabaseManager;
import com.quilot.db.dao.InterviewDao;
import com.quilot.exceptions.audio.AudioDeviceException;
import com.quilot.exceptions.audio.AudioException;
import com.quilot.exceptions.stt.STTException;
import com.quilot.stt.GoogleCloudSpeechToTextService;
import com.quilot.stt.ISpeechToTextSettingsManager;
import com.quilot.stt.SpeechToTextService;
import com.quilot.stt.settings.RecognitionConfigSettings;
import com.quilot.stt.settings.SpeechToTextSettingsManager;
import com.quilot.ui.help.CredentialsSetupDialog;
import com.quilot.ui.help.DatabaseSetupDialog;
import com.quilot.ui.help.GoogleCloudSetupGuideDialog;
import com.quilot.ui.help.SetupGuideDialog;
import com.quilot.ui.history.InterviewHistoryDialog;
import com.quilot.ui.settings.AISettingsDialog;
import com.quilot.ui.settings.STTSettingsDialog;
import com.quilot.utils.CredentialManager;
import com.quilot.utils.Logger;
import com.quilot.utils.QuestionDetector;
import lombok.Getter;

import javax.sound.sampled.AudioFormat;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

/**
 * The main user interface frame for the application.
 * <p>
 * This class serves as the central hub of the application, responsible for:
 * <ul>
 * <li>Initializing all core services (AI, STT, Audio I/O, Database).</li>
 * <li>Constructing the main UI layout using a {@link UIBuilder}.</li>
 * <li>Binding all event listeners for user interactions.</li>
 * <li>Handling graceful shutdown of services when the window is closed.</li>
 * <li>Orchestrating the flow of data between the UI and the backend services.</li>
 * </ul>
 * It is designed to handle startup failures gracefully and guide the user
 * through initial configuration.
 */
@Getter
public class MainFrame extends JFrame {

    // --- UI Components ---
    private final JTextArea transcribedAudioArea;
    private final JTextArea aiResponseArea;
    private final JTextArea logArea;
    private final JComboBox<String> outputDeviceComboBox;
    private final JSlider volumeSlider;
    private final JButton testVolumeButton;
    private final JComboBox<String> inputDeviceComboBox;
    private final JButton startInputRecordingButton;
    private final JButton stopInputRecordingButton;
    private final JButton playRecordedInputButton;

    // --- Services and Managers ---
    private final ElapsedTimerManager timerManager;
    private final AudioOutputService audioOutputService;
    private final AudioInputService audioInputService;
    private final SpeechToTextService speechToTextService;
    private final CredentialManager credentialManager;
    private final ISpeechToTextSettingsManager sttSettingsManager;
    private final IAIService aiService;
    private final InterviewDao interviewDao;
    private final QuestionDetector questionDetector;

    // --- State Management ---
    private int currentInterviewId = -1; // -1 indicates no active recording session
    private boolean askForDatabaseSetup = true;

    /**
     * Constructs the MainFrame.
     * This constructor orchestrates the initialization of all backend services and UI components.
     */
    public MainFrame() {
        // Initialize services first
        this.timerManager = new ElapsedTimerManager();
        this.credentialManager = new CredentialManager();
        this.audioOutputService = new SystemAudioOutputService();
        this.audioInputService = new SystemAudioInputService();
        this.sttSettingsManager = new SpeechToTextSettingsManager();
        this.interviewDao = new InterviewDao();
        this.questionDetector = new QuestionDetector();

        // Initialize services that depend on others
        String savedCredentialPath = credentialManager.loadGoogleCloudCredentialPath();
        this.aiService = new VertexAIService(savedCredentialPath, new AISettingsManager());
        this.speechToTextService = new GoogleCloudSpeechToTextService(savedCredentialPath, sttSettingsManager);

        // Build the UI
        UIBuilder uiBuilder = new UIBuilder(audioOutputService, audioInputService, timerManager);
        this.transcribedAudioArea = uiBuilder.getTranscribedAudioArea();
        this.aiResponseArea = uiBuilder.getAiResponseArea();
        this.logArea = uiBuilder.getLogArea();
        this.outputDeviceComboBox = uiBuilder.getOutputDeviceComboBox();
        this.volumeSlider = uiBuilder.getVolumeSlider();
        this.testVolumeButton = uiBuilder.getTestVolumeButton();
        this.inputDeviceComboBox = uiBuilder.getInputDeviceComboBox();
        this.startInputRecordingButton = uiBuilder.getStartInputRecordingButton();
        this.stopInputRecordingButton = uiBuilder.getStopInputRecordingButton();
        this.playRecordedInputButton = uiBuilder.getPlayRecordedInputButton();

        initializeFrame(uiBuilder);
        bindListeners();

        Logger.info("Quilot UI initialized.");
        appendToLogArea("UI initialized. Ready to start.");
        performPostStartupChecks();
    }

    /**
     * Sets up the main JFrame properties and layout.
     * @param uiBuilder The UI builder containing the main panel.
     */
    private void initializeFrame(UIBuilder uiBuilder) {
        setTitle("Quilot");
        setMinimumSize(new Dimension(1200, 800));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setJMenuBar(createMenuBar());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        uiBuilder.setupLayout(mainPanel);
        add(mainPanel);
        pack();
    }

    /**
     * Binds all event listeners for the application.
     */
    private void bindListeners() {
        audioInputService.setAudioDataListener((audioData, bytesRead) ->
                ((GoogleCloudSpeechToTextService) speechToTextService).onAudioDataCaptured(audioData, bytesRead));
        addAudioOutputListeners();
        addAudioInputListeners();
        addWindowListeners();
    }

    /**
     * Binds listeners for audio output components.
     */
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

    /**
     * Binds listeners for audio input components. This is the main interaction hub.
     */
    private void addAudioInputListeners() {
        inputDeviceComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                handleInputDeviceSelection((String) e.getItem());
            }
        });

        startInputRecordingButton.addActionListener(_ -> handleStartRecording());
        stopInputRecordingButton.addActionListener(_ -> handleStopRecording());
        playRecordedInputButton.addActionListener(_ -> handlePlayRecordedAudio());
    }

    /**
     * Handles the logic for starting a recording session.
     */
    private void handleStartRecording() {
        if (promptForDatabaseSetupIfNeeded()) {
            startRecordingAndTranscription();
        }
    }

    /**
     * Prompts the user to set up the database if it's their first time, and they haven't declined.
     * @return true if the process can continue.
     */
    private boolean promptForDatabaseSetupIfNeeded() {
        if (askForDatabaseSetup && !DatabaseManager.isDatabaseEnabled()) {
            int response = JOptionPane.showConfirmDialog(this,
                    "Would you like to save interview recordings to a local database?\nThis allows you to review past sessions.",
                    "Enable Interview History", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (response == JOptionPane.YES_OPTION) {
                DatabaseSetupDialog setupDialog = new DatabaseSetupDialog(this);
                setupDialog.setVisible(true);
                if (!setupDialog.wasSetupSuccessful()) {
                    askForDatabaseSetup = false; // Don't ask again this session
                    appendToLogArea("Database setup was not completed. Proceeding without saving.");
                }
            } else {
                askForDatabaseSetup = false; // User declined, don't ask again
                appendToLogArea("Interview history disabled for this session.");
            }
        }
        return true;
    }

    /**
     * Starts the audio capture and speech-to-text transcription stream.
     */
    private void startRecordingAndTranscription() {
        try {
            if (DatabaseManager.isDatabaseEnabled()) {
                createNewInterviewRecord();
            }
            audioInputService.startRecording();
            appendToLogArea("Started capturing audio from input device.");
            timerManager.startElapsedTimer();
            updateAudioInputButtonStates(true);

            speechToTextService.startStreamingRecognition(audioInputService.getAudioFormat(), new SpeechToTextService.StreamingRecognitionListener() {
                @Override
                public void onTranscriptionResult(String transcription, boolean isFinal) {
                    if (isFinal) {
                        handleFinalTranscription(transcription);
                    } else {
                        updateInterimTranscription(transcription);
                    }
                }
                @Override
                public void onTranscriptionError(Exception error) {
                    String errorMessage = "A transcription error occurred: " + error.getMessage();
                    appendToLogArea(errorMessage);
                    aiResponseArea.append("STT (Error): " + errorMessage + "\n");
                }
            });
        } catch (AudioDeviceException | STTException ex) {
            String errorType = (ex instanceof AudioDeviceException) ? "Recording Error" : "Speech-to-Text Error";
            appendToLogArea("Failed to start session: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Could not start session:\n" + ex.getMessage(), errorType, JOptionPane.ERROR_MESSAGE);
            updateAudioInputButtonStates(false);
        }
    }

    /**
     * Handles a final transcription result from the STT service.
     * @param transcription The final transcribed text.
     */
    private void handleFinalTranscription(String transcription) {
        SwingUtilities.invokeLater(() -> {
            transcribedAudioArea.append("Interviewer (Final): '" + transcription + "'\n");
            transcribedAudioArea.setCaretPosition(transcribedAudioArea.getDocument().getLength());

            RecognitionConfigSettings settings = sttSettingsManager.loadSettings();
            String currentLanguage = settings.getLanguageCode();
            boolean isQuestion = questionDetector.isQuestion(transcription, currentLanguage);

            if (currentInterviewId != -1) {
                saveTranscriptionEntry("Interviewer", transcription, isQuestion);
            }

            if (!settings.isEnableQuestionDetection() || isQuestion) {
                sendToAiService(transcription);
            } else {
                appendToLogArea("Non-question detected. Ignoring for AI response.");
            }
        });
    }

    /**
     * Sends a transcription to the AI service and handles the response.
     * @param transcription The text to send.
     */
    private void sendToAiService(String transcription) {
        aiService.generateResponse(transcription, new IAIService.AIResponseListener() {
            @Override
            public void onResponse(String aiResponse) {
                SwingUtilities.invokeLater(() -> {
                    aiResponseArea.append("AI (Response): '" + aiResponse + "'\n");
                    aiResponseArea.setCaretPosition(aiResponseArea.getDocument().getLength());
                    if (currentInterviewId != -1) {
                        saveTranscriptionEntry("AI", aiResponse, false);
                    }
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
    }

    /**
     * Creates a new interview record in the database and stores its ID.
     */
    private void createNewInterviewRecord() {
        try {
            String title = "Interview - " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            currentInterviewId = interviewDao.createNewInterview(title);
            appendToLogArea("Started new interview session. Saving to database with ID: " + currentInterviewId);
        } catch (SQLException e) {
            currentInterviewId = -1;
            appendToLogArea("WARNING: Could not create interview record in database. History will not be saved. Error: " + e.getMessage());
        }
    }

    /**
     * Saves a single line of dialogue to the database for the current interview.
     * @param speaker The speaker ("Interviewer" or "AI").
     * @param content The text content.
     * @param isQuestion Whether the content was detected as a question.
     */
    private void saveTranscriptionEntry(String speaker, String content, boolean isQuestion) {
        try {
            interviewDao.addTranscriptionEntry(currentInterviewId, speaker, content, isQuestion);
        } catch (SQLException e) {
            appendToLogArea("DB_ERROR: Failed to save transcription entry: " + e.getMessage());
        }
    }

    /**
     * Handles the logic for stopping a recording session.
     */
    private void handleStopRecording() {
        if (audioInputService.stopRecording()) {
            appendToLogArea("Stopped capturing audio from input device.");
            timerManager.stopElapsedTimer();
            updateAudioInputButtonStates(false);

            if (currentInterviewId != -1) {
                try {
                    byte[] recordedData = audioInputService.getRecordedAudioData();
                    interviewDao.saveFullAudio(currentInterviewId, recordedData);
                    appendToLogArea("Full audio recording saved for interview ID: " + currentInterviewId);
                } catch (SQLException e) {
                    appendToLogArea("DB_ERROR: Failed to save full audio recording: " + e.getMessage());
                } finally {
                    currentInterviewId = -1; // End the session
                }
            }
        }
        speechToTextService.stopStreamingRecognition();
    }

    /**
     * Handles the logic for playing back the most recently recorded audio.
     */
    private void handlePlayRecordedAudio() {
        byte[] recordedData = audioInputService.getRecordedAudioData();
        AudioFormat format = audioInputService.getAudioFormat();
        if (recordedData != null && recordedData.length > 0 && format != null) {
            appendToLogArea("Playing recorded input audio...");
            try {
                audioOutputService.playAudioData(recordedData, format);
                audioInputService.clearRecordedAudioData();
                playRecordedInputButton.setEnabled(false);
                appendToLogArea("Recorded input audio played and cleared.");
            } catch (AudioException ex) {
                appendToLogArea("Failed to play recorded audio: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Could not play recorded audio.\n" + ex.getMessage(), "Playback Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            appendToLogArea("No recorded audio data to play.");
            playRecordedInputButton.setEnabled(false);
        }
    }

    /**
     * Handles the selection of a new audio input device from the dropdown.
     * @param selectedDevice The name of the device to select.
     */
    private void handleInputDeviceSelection(String selectedDevice) {
        try {
            audioInputService.selectInputDevice(selectedDevice);
            appendToLogArea("Selected audio input device: " + selectedDevice);
        } catch (AudioDeviceException ex) {
            appendToLogArea("Failed to select audio input device: " + selectedDevice + " - " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Could not open audio device: " + selectedDevice + "\nIt may be in use by another application or disconnected.", "Audio Device Error", JOptionPane.ERROR_MESSAGE);
        }
        updateAudioInputButtonStates(false);
    }

    /**
     * Updates the enabled state of the audio input control buttons.
     * @param isRecording true if recording is active, false otherwise.
     */
    private void updateAudioInputButtonStates(boolean isRecording) {
        startInputRecordingButton.setEnabled(!isRecording);
        stopInputRecordingButton.setEnabled(isRecording);
        playRecordedInputButton.setEnabled(!isRecording && audioInputService.getRecordedAudioData() != null && audioInputService.getRecordedAudioData().length > 0);
    }

    /**
     * Updates the display of interim (non-final) transcription results.
     * @param transcription The interim text.
     */
    private void updateInterimTranscription(String transcription) {
        SwingUtilities.invokeLater(() -> {
            String existingText = transcribedAudioArea.getText();
            int lastNewline = existingText.lastIndexOf('\n');
            if (lastNewline != -1 && existingText.substring(lastNewline + 1).startsWith("Interviewer (Interim):")) {
                transcribedAudioArea.replaceRange("Interviewer (Interim): '" + transcription + "'", lastNewline + 1, existingText.length());
            } else {
                transcribedAudioArea.append("Interviewer (Interim): '" + transcription + "'\n");
            }
            transcribedAudioArea.setCaretPosition(transcribedAudioArea.getDocument().getLength());
        });
    }

    /**
     * Binds a listener to handle the window closing event for graceful shutdown.
     */
    private void addWindowListeners() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                audioOutputService.close();
                audioInputService.close();
                DatabaseManager.closeConnection();
                if (speechToTextService instanceof GoogleCloudSpeechToTextService) {
                    ((GoogleCloudSpeechToTextService) speechToTextService).closeClient();
                }
                if (aiService instanceof VertexAIService) {
                    ((VertexAIService) aiService).closeClient();
                }
                Logger.info("Application closing. All resources released.");
            }
        });
    }

    /**
     * Creates and configures the main menu bar for the application.
     * @return The configured JMenuBar.
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // --- File Menu ---
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(_ -> System.exit(0));
        fileMenu.add(exitItem);

        // --- View Menu ---
        JMenu viewMenu = new JMenu("View");
        JMenuItem historyItem = new JMenuItem("Interview History...");
        historyItem.addActionListener(_ -> openHistoryDialog());
        viewMenu.add(historyItem);

        // --- Settings Menu ---
        JMenu settingsMenu = getJMenu();

        // --- Help Menu ---
        JMenu helpMenu = new JMenu("Help");
        JMenuItem googleGuideItem = new JMenuItem("Google Cloud Setup Guide");
        googleGuideItem.addActionListener(_ -> new GoogleCloudSetupGuideDialog(this).setVisible(true));
        JMenuItem macosGuideItem = new JMenuItem("macOS Audio Setup Guide");
        macosGuideItem.addActionListener(_ -> new SetupGuideDialog(this).setVisible(true));
        helpMenu.add(googleGuideItem);
        helpMenu.add(macosGuideItem);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(settingsMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private JMenu getJMenu() {
        JMenu settingsMenu = new JMenu("Settings");
        JMenuItem aiSettingsItem = new JMenuItem("AI Settings...");
        aiSettingsItem.addActionListener(_ -> openAiSettingsDialog());
        JMenuItem sttSettingsItem = new JMenuItem("STT Settings...");
        sttSettingsItem.addActionListener(_ -> openSttSettingsDialog());
        JMenuItem credentialsItem = new JMenuItem("Credentials...");
        credentialsItem.addActionListener(_ -> openCredentialsDialog());
        settingsMenu.add(aiSettingsItem);
        settingsMenu.add(sttSettingsItem);
        settingsMenu.addSeparator();
        settingsMenu.add(credentialsItem);
        return settingsMenu;
    }

    /**
     * Opens the AI settings dialog.
     */
    private void openAiSettingsDialog() {
        AISettingsDialog dialog = new AISettingsDialog(this, aiService.getSettingsManager(), (VertexAIService) aiService);
        dialog.setVisible(true);
    }

    /**
     * Opens the STT settings dialog.
     */
    private void openSttSettingsDialog() {
        STTSettingsDialog dialog = new STTSettingsDialog(this, sttSettingsManager, (GoogleCloudSpeechToTextService) speechToTextService);
        dialog.setVisible(true);
    }

    /**
     * Opens the credentials setup dialog.
     */
    private void openCredentialsDialog() {
        CredentialsSetupDialog dialog = new CredentialsSetupDialog(this, credentialManager, (GoogleCloudSpeechToTextService) speechToTextService);
        dialog.setVisible(true);
    }

    /**
     * Opens the interview history dialog.
     */
    private void openHistoryDialog() {
        if (DatabaseManager.isDatabaseEnabled()) {
            InterviewHistoryDialog historyDialog = new InterviewHistoryDialog(this, interviewDao, audioOutputService);
            historyDialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                    "The interview history feature is currently disabled.\n" +
                            "Please start a recording to enable and set up the database.",
                    "Feature Disabled",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Performs checks after the UI is visible, such as prompting for initial configuration.
     */
    private void performPostStartupChecks() {
        SwingUtilities.invokeLater(() -> {
            boolean isAiServiceReady = ((VertexAIService) aiService).isClientInitialized();
            boolean isSttServiceReady = ((GoogleCloudSpeechToTextService) speechToTextService).isClientInitialized();

            if (!isAiServiceReady || !isSttServiceReady) {
                JOptionPane.showMessageDialog(this,
                        "Welcome! To get started, please set your Google Cloud credentials via the Settings menu.",
                        "Configuration Needed",
                        JOptionPane.INFORMATION_MESSAGE);
                openCredentialsDialog();
            }
        });
    }

    /**
     * Appends a message to the application's log text area in a thread-safe manner.
     * @param message The string message to append.
     */
    public void appendToLogArea(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}