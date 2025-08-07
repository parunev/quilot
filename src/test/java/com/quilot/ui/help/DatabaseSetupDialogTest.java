package com.quilot.ui.help;

import com.quilot.db.DatabaseManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.swing.*;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

/**
 * Unit tests for the {@link DatabaseSetupDialog}.
 * Uses Mockito to mock the static DatabaseManager.
 */
class DatabaseSetupDialogTest {

    private DatabaseSetupDialog dialog;
    private MockedStatic<DatabaseManager> mockedDatabaseManager;

    @BeforeEach
    void setUp() {
        mockedDatabaseManager = Mockito.mockStatic(DatabaseManager.class);

        dialog = new DatabaseSetupDialog(new JFrame());
    }

    @AfterEach
    void tearDown() {
        mockedDatabaseManager.close();
    }

    @Test
    @DisplayName("Should call setup and save credentials on successful setup")
    void onSetup_WithValidCredentials_CallsDatabaseManager() {
        dialog.getUserField().setText("testuser");
        dialog.getPasswordField().setText("testpass");

        dialog.getSetupButton().doClick();

        mockedDatabaseManager.verify(() -> DatabaseManager.setupDatabaseSchema("testuser", "testpass"), times(1));
        mockedDatabaseManager.verify(() -> DatabaseManager.saveCredentials("testuser", "testpass"), times(1));
    }

    @Test
    @DisplayName("Should not call DatabaseManager if username is empty")
    void onSetup_WithEmptyUsername_DoesNotCallDatabaseManager() {
        dialog.getUserField().setText("");
        dialog.getPasswordField().setText("testpass");

        dialog.getSetupButton().doClick();

        mockedDatabaseManager.verify(() -> DatabaseManager.setupDatabaseSchema(anyString(), anyString()), never());
        mockedDatabaseManager.verify(() -> DatabaseManager.saveCredentials(anyString(), anyString()), never());
    }

    @Test
    @DisplayName("Should not save credentials if schema setup fails")
    void onSetup_WhenSchemaSetupFails_DoesNotSaveCredentials() {
        dialog.getUserField().setText("testuser");
        dialog.getPasswordField().setText("testpass");

        mockedDatabaseManager.when(() -> DatabaseManager.setupDatabaseSchema("testuser", "testpass"))
                .thenThrow(new SQLException("Connection failed"));

        assertDoesNotThrow(() -> dialog.getSetupButton().doClick());

        mockedDatabaseManager.verify(() -> DatabaseManager.saveCredentials(anyString(), anyString()), never());
    }
}
