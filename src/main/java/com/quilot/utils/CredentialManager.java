package com.quilot.utils;

import java.util.prefs.Preferences;

/**
 * Manages saving and loading of credentials using Java Preferences API.
 */
public class CredentialManager {

    private static final String PREF_NODE_NAME = "com/quilot";
    private static final String GOOGLE_CREDENTIAL_PATH_KEY = "googleCloudCredentialPath";

    private final Preferences prefs;

    public CredentialManager() {
        prefs = Preferences.userRoot().node(PREF_NODE_NAME);
        Logger.info("CredentialManager initialized.");
    }

    /**
     * Saves the path to Google Cloud credential file.
     * @param path the file path to save; must not be null or empty.
     */
    public void saveGoogleCloudCredentialPath(String path) {
        if (path == null || path.isEmpty()) {
            Logger.warn("Attempted to save empty or null Google Cloud credential path; operation skipped.");
            return;
        }
        prefs.put(GOOGLE_CREDENTIAL_PATH_KEY, path);
        Logger.info("Google Cloud credential path saved: " + path);
    }

    /**
     * Loads the stored Google Cloud credential path.
     * @return the credential path, or empty string if none stored.
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
