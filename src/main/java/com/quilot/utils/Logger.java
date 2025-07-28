package com.quilot.utils;

import java.time.format.DateTimeFormatter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

/**
 * Logger
 * This class demonstrates basic file I/O and exception handling.
 * It logs messages to both the console and a specified log file.
 */
public class Logger {

    private static final String LOG_FILE_NAME = "interview_copilot.log";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Logs an informational message.
     * @param message The message to log.
     */
    public static void info(String message) {
        log("INFO", message);
    }

    /**
     * Logs a warning message.
     * @param message The message to log.
     */
    public static void warn(String message) {
        log("WARN", message);
    }

    /**
     * Logs an error message. This overload is for errors where no specific Throwable object is available.
     * @param message The message to log.
     */
    public static void error(String message) {
        log("ERROR", message);
    }

    /**
     * Logs an advanced error message.
     * @param message The message to log.
     */
    public static void error(String message, Throwable throwable) {
        log("ERROR", message + (throwable != null ? " - " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage() : ""));
        if (throwable != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE_NAME, true))) {
                throwable.printStackTrace(writer);
            } catch (IOException e) {
                System.err.println("Error writing stack trace to log file: " + e.getMessage());
            }
        }
    }

    /**
     * The core logging method. Formats the message with timestamp and level,
     * then writes it to console and file.
     * @param level The log level (e.g., INFO, WARN, ERROR).
     * @param message The message content.
     */
    private static void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String logEntry = String.format("[%s] [%s] %s", timestamp, level, message);

        // Log to console
        System.out.println(logEntry);

        // Log to file
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE_NAME, true))) {
            writer.println(logEntry);
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }
}
