package com.quilot.utils;

import com.quilot.exceptions.CredentialStorageException;

import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Manages saving and loading of credentials using Java Preferences API.
 */
public class CredentialManager {

    private static final String PREF_NODE_NAME = "com/quilot";
    private static final String GOOGLE_CREDENTIAL_PATH_KEY = "googleCloudCredentialPath";

    private final Preferences prefs;

    public CredentialManager() {
        try {
            this.prefs = Preferences.userRoot().node(PREF_NODE_NAME);
            Logger.info("CredentialManager initialized.");
        } catch (SecurityException e) {
            Logger.error("Could not access Java Preferences due to a security policy.", e);
            throw new RuntimeException("Failed to initialize CredentialManager due to security restrictions.", e);
        }
    }

    /**
     * A package-private constructor for testing purposes.
     * Allows injecting a mock or in-memory Preferences object.
     *
     * @param prefs The Preferences object to use.
     */
    CredentialManager(Preferences prefs) {
        this.prefs = prefs;
    }

    /**
     * Saves the path to Google Cloud credential file.
     * @param path the file path to save; must not be null.
     */
    public void saveGoogleCloudCredentialPath(String path) throws CredentialStorageException {
        Objects.requireNonNull(path, "Credential path cannot be null.");

        try {
            prefs.put(GOOGLE_CREDENTIAL_PATH_KEY, path);
            prefs.flush();
            Logger.info("Google Cloud credential path saved: " + path);
        } catch (BackingStoreException e) {
            Logger.error("Failed to save credential path to the backing store.", e);
            throw new CredentialStorageException("Could not save credential path due to a storage error.", e);
        }
    }

    /**
     * Loads the stored Google Cloud credential path.
     * @return the credential path, or an empty string if none is stored.
     */
    public String loadGoogleCloudCredentialPath() {
        String path = prefs.get(GOOGLE_CREDENTIAL_PATH_KEY, "");
        if (path.isEmpty()) {
            Logger.warn("No Google Cloud credential path found in preferences.");
        } else {
            Logger.info("Loaded Google Cloud credential path: " + path);
        }
        return path;
    }
}
