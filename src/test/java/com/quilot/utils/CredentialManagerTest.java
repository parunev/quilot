package com.quilot.utils;

import com.quilot.exceptions.CredentialStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link CredentialManager} class.
 * Uses an in-memory Preferences implementation for isolated testing.
 */
class CredentialManagerTest {

    private Preferences inMemoryPreferences;
    private CredentialManager credentialManager;

    @BeforeEach
    void setUp() {
        inMemoryPreferences = Preferences.userRoot().node("com/quilot/test/credentials");
        credentialManager = new CredentialManager(inMemoryPreferences);
    }

    @Test
    @DisplayName("Should save and load a credential path successfully")
    void saveAndLoadGoogleCloudCredentialPath_Success() throws CredentialStorageException, BackingStoreException {
        String testPath = "/path/to/credentials.json";
        credentialManager.saveGoogleCloudCredentialPath(testPath);
        String loadedPath = credentialManager.loadGoogleCloudCredentialPath();
        assertEquals(testPath, loadedPath, "The loaded path should match the saved path.");
        inMemoryPreferences.clear();
    }

    @Test
    @DisplayName("Should return an empty string if no path is saved")
    void loadGoogleCloudCredentialPath_NoPathSaved_ReturnsEmptyString() {
        String loadedPath = credentialManager.loadGoogleCloudCredentialPath();
        assertTrue(loadedPath.isEmpty(), "Should return an empty string when no path is set.");
    }

    @Test
    @DisplayName("Should throw NullPointerException when saving a null path")
    void saveGoogleCloudCredentialPath_NullPath_ThrowsException() {
        assertThrows(NullPointerException.class, () -> credentialManager.saveGoogleCloudCredentialPath(null),
                "Saving a null path should throw a NullPointerException.");
    }
}