package com.quilot.utils;

import java.time.format.DateTimeFormatter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

/**
 * Logger utility class for logging messages with different levels to console and a log file.
 * Supports INFO, WARN, and ERROR levels, including logging exceptions with stack traces.
 */
public class Logger {

    private static final String LOG_FILE_NAME = "interview_copilot.log";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Logs an informational message.
     * @param message the message to log
     */
    public static void info(String message) {
        log("INFO", message);
    }

    /**
     * Logs a warning message.
     * @param message the message to log
     */
    public static void warn(String message) {
        log("WARN", message);
    }

    /**
     * Logs an error message.
     * @param message the message to log
     */
    public static void error(String message) {
        log("ERROR", message);
    }

    /**
     * Logs an error message along with a throwable's stack trace.
     * @param message   the error message
     * @param throwable the throwable to log (can be null)
     */
    public static void error(String message, Throwable throwable) {
        String errorMsg = message;
        if (throwable != null) {
            errorMsg += " - " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
        }
        log("ERROR", errorMsg);

        if (throwable != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE_NAME, true))) {
                throwable.printStackTrace(writer);
            } catch (IOException e) {
                System.err.println("Error writing stack trace to log file: " + e.getMessage());
            }
        }
    }

    /**
     * Writes the log entry to both the console and the log file.
     * @param level   the log level (INFO, WARN, ERROR)
     * @param message the message to log
     */
    private static void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String logEntry = String.format("[%s] [%s] %s", timestamp, level, message);

        System.out.println(logEntry);

        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE_NAME, true))) {
            writer.println(logEntry);
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }
}
