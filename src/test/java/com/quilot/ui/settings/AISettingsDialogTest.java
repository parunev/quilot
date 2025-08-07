package com.quilot.ui.settings;

import com.quilot.ai.VertexAIService;
import com.quilot.ai.settings.AIConfigSettings;
import com.quilot.ai.settings.IAISettingsManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link AISettingsDialog}.
 */
@ExtendWith(MockitoExtension.class)
class AISettingsDialogTest {

    @Mock
    private IAISettingsManager mockSettingsManager;
    @Mock
    private VertexAIService mockAiService;

    private AISettingsDialog dialog;

    @BeforeEach
    void setUp() {
        when(mockSettingsManager.loadSettings()).thenReturn(AIConfigSettings.builder().build());

        dialog = new AISettingsDialog(null, mockSettingsManager, mockAiService);
    }

    @Test
    @DisplayName("Should load settings into UI fields correctly")
    void loadSettingsIntoUI_PopulatesFields() {
        AIConfigSettings settings = AIConfigSettings.builder()
                .projectId("test-project-123")
                .temperature(0.95)
                .build();

        // Act
        dialog.loadSettingsIntoUI(settings);

        // Assert
        assertEquals("test-project-123", dialog.getProjectIdField().getText());
        assertEquals(0.95, ((Number) dialog.getTemperatureField().getValue()).doubleValue());
    }

    @Test
    @DisplayName("Save button should save settings and re-initialize service")
    void saveSettingsFromUI_CallsServicesWithCorrectData() {
        dialog.getProjectIdField().setText("new-project-id");
        dialog.getTemperatureField().setValue(0.5);

        ArgumentCaptor<AIConfigSettings> settingsCaptor = ArgumentCaptor.forClass(AIConfigSettings.class);

        dialog.getSaveButton().doClick();

        verify(mockSettingsManager, times(1)).saveSettings(settingsCaptor.capture());

        AIConfigSettings capturedSettings = settingsCaptor.getValue();
        assertEquals("new-project-id", capturedSettings.getProjectId());
        assertEquals(0.5, capturedSettings.getTemperature());

        verify(mockAiService, times(1)).setCredentialPath(any());
    }

    @Test
    @DisplayName("Load Defaults button should reset settings and re-initialize service")
    void loadDefaultsIntoUI_CallsResetAndAppliesDefaults() {
        AIConfigSettings defaultSettings = AIConfigSettings.builder()
                .projectId("default-project")
                .temperature(0.7)
                .build();
        when(mockSettingsManager.resetToDefaults()).thenReturn(defaultSettings);

        dialog.getLoadDefaultsButton().doClick();

        verify(mockSettingsManager, times(1)).resetToDefaults();

        assertEquals("default-project", dialog.getProjectIdField().getText());
        assertEquals(0.7, ((Number) dialog.getTemperatureField().getValue()).doubleValue());

        verify(mockAiService, times(1)).setCredentialPath(any());
    }
}
