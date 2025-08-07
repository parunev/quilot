package com.quilot.ui.settings;

import com.quilot.stt.GoogleCloudSpeechToTextService;
import com.quilot.stt.ISpeechToTextSettingsManager;
import com.quilot.stt.settings.RecognitionConfigSettings;
import com.quilot.stt.settings.SttLanguageData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link STTSettingsDialog}.
 */
@ExtendWith(MockitoExtension.class)
class STTSettingsDialogTest {

    @Mock
    private ISpeechToTextSettingsManager mockSettingsManager;
    @Mock
    private GoogleCloudSpeechToTextService mockSpeechToTextService;

    private STTSettingsDialog dialog;

    @BeforeEach
    void setUp() {
        when(mockSettingsManager.loadSettings()).thenReturn(RecognitionConfigSettings.builder().build());

        dialog = new STTSettingsDialog(null, mockSettingsManager, mockSpeechToTextService);
    }

    @Test
    @DisplayName("Should dynamically disable features for Bulgarian (bg-BG)")
    void updateFeatureSupport_ForBulgarian_DisablesUnsupportedFeatures() {
        SttLanguageData.Language bulgarian = SttLanguageData.getLanguages().stream()
                .filter(lang -> lang.code().equals("bg-BG"))
                .findFirst()
                .orElseThrow();

        dialog.getLanguageCodeComboBox().setSelectedItem(bulgarian);

        assertFalse(dialog.getEnableAutomaticPunctuationCheckBox().isEnabled(), "Automatic Punctuation should be disabled for Bulgarian.");
        assertTrue(dialog.getEnableSpeakerDiarizationCheckBox().isEnabled(), "Diarization should be enabled for Bulgarian.");
    }

    @Test
    @DisplayName("Should dynamically enable features for US English (en-US)")
    void updateFeatureSupport_ForUsEnglish_EnablesSupportedFeatures() {
        SttLanguageData.Language english = SttLanguageData.getLanguages().stream()
                .filter(lang -> lang.code().equals("en-US"))
                .findFirst()
                .orElseThrow();

        dialog.getLanguageCodeComboBox().setSelectedItem(english);

        assertTrue(dialog.getEnableAutomaticPunctuationCheckBox().isEnabled(), "Automatic Punctuation should be enabled for US English.");
        assertTrue(dialog.getEnableSpeakerDiarizationCheckBox().isEnabled(), "Diarization should be enabled for US English.");
    }

    @Test
    @DisplayName("Save button should save settings with correct values")
    void saveSettingsFromUI_CallsManagerWithCorrectData() throws Exception {
        SttLanguageData.Language english = SttLanguageData.getLanguages().stream()
                .filter(lang -> lang.code().equals("en-US"))
                .findFirst()
                .orElseThrow();
        dialog.getLanguageCodeComboBox().setSelectedItem(english);
        dialog.getEnableAutomaticPunctuationCheckBox().setSelected(true);

        ArgumentCaptor<RecognitionConfigSettings> settingsCaptor = ArgumentCaptor.forClass(RecognitionConfigSettings.class);

        dialog.getSaveButton().doClick();

        verify(mockSettingsManager, times(1)).saveSettings(settingsCaptor.capture());

        RecognitionConfigSettings capturedSettings = settingsCaptor.getValue();
        assertEquals("en-US", capturedSettings.getLanguageCode());
        assertTrue(capturedSettings.isEnableAutomaticPunctuation());
    }

    @Test
    @DisplayName("Load Defaults button should call resetToDefaults on manager")
    void loadDefaultsIntoUI_CallsResetOnManager() throws Exception {
        when(mockSettingsManager.resetToDefaults()).thenReturn(RecognitionConfigSettings.builder().build());

        dialog.getLoadDefaultsButton().doClick();

        verify(mockSettingsManager, times(1)).resetToDefaults();
        verify(mockSpeechToTextService, times(1)).setCredentialPath(any());
    }
}
