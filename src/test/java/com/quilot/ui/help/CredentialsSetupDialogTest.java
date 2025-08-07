package com.quilot.ui.help;

import com.quilot.exceptions.CredentialStorageException;
import com.quilot.exceptions.stt.STTAuthenticationException;
import com.quilot.stt.GoogleCloudSpeechToTextService;
import com.quilot.utils.CredentialManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.*;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link CredentialsSetupDialog}.
 * Uses Mockito to mock service dependencies and verify interactions.
 */
@ExtendWith(MockitoExtension.class)
class CredentialsSetupDialogTest {

    @Mock
    private CredentialManager mockCredentialManager;
    @Mock
    private GoogleCloudSpeechToTextService mockSpeechToTextService;

    private CredentialsSetupDialog dialog;

    @BeforeEach
    void setUp() {
        dialog = new CredentialsSetupDialog(new JFrame(), mockCredentialManager, mockSpeechToTextService);
    }

    @Test
    @DisplayName("Should call save and setCredentialPath when Save button is clicked with valid path")
    void saveCredentials_WithValidPath_CallsServices() throws CredentialStorageException, STTAuthenticationException {
        String testPath = "/path/to/key.json";

        dialog.getCredentialPathField().setText(testPath);

        dialog.getSaveButton().doClick();

        verify(mockCredentialManager, times(1)).saveGoogleCloudCredentialPath(testPath);

        verify(mockSpeechToTextService, times(1)).setCredentialPath(testPath);
    }

    @Test
    @DisplayName("Should not call services when Save button is clicked with empty path")
    void saveCredentials_WithEmptyPath_DoesNotCallServices() throws CredentialStorageException, STTAuthenticationException {
        dialog.getCredentialPathField().setText("   ");

        dialog.getSaveButton().doClick();

        verify(mockCredentialManager, never()).saveGoogleCloudCredentialPath(anyString());
        verify(mockSpeechToTextService, never()).setCredentialPath(anyString());
    }

    @Test
    @DisplayName("Should call testCredentials when Test button is clicked")
    void testCredentials_CallsService() throws STTAuthenticationException {
        String testPath = "/path/to/key.json";
        dialog.getCredentialPathField().setText(testPath);

        dialog.getTestButton().doClick();

        verify(mockSpeechToTextService, times(1)).testCredentials();
    }

    @Test
    @DisplayName("Should show error message if testCredentials throws an exception")
    void testCredentials_ServiceThrowsException_ShowsDialog() throws STTAuthenticationException {
        String testPath = "/path/to/key.json";
        dialog.getCredentialPathField().setText(testPath);

        doThrow(new STTAuthenticationException("Invalid key", null))
                .when(mockSpeechToTextService).testCredentials();

        dialog.getTestButton().doClick();

        verify(mockSpeechToTextService, times(1)).testCredentials();
    }
}
