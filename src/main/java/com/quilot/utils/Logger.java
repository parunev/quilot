package com.quilot.utils;

import java.time.format.DateTimeFormatter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public class Logger {

    private static final String LOG_FILE_NAME = "interview_copilot.log";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

    public static void info(String message) {
        log("INFO", message);
    }

    public static void warn(String message) {
        log("WARN", message);
    }

    public static void error(String message) {
        log("ERROR", message);
    }


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
}
