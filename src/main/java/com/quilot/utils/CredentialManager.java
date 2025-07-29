package com.quilot.utils;

import java.util.prefs.Preferences;

/**
 * Manages the persistence of application credentials, specifically the Google Cloud
 * service account key file path. Uses Java's Preferences API for simple, cross-platform storage.
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
     * Saves the provided Google Cloud service account key file path.
     * @param path The absolute path to the JSON key file.
     */
    public void saveGoogleCloudCredentialPath(String path) {
        prefs.put(GOOGLE_CREDENTIAL_PATH_KEY, path);
        Logger.info("Google Cloud credential path saved: " + path);
    }

    /**
     * Loads the previously saved Google Cloud service account key file path.
     * @return The saved path, or an empty string if not found.
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
