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

    private static final PrintWriter writer = createPrintWriter();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (writer != null) {
                writer.close();
            }
        }));
    }

    private static PrintWriter createPrintWriter() {
        try {
            return new PrintWriter(new FileWriter(LOG_FILE_NAME, true), true);
        } catch (IOException e) {
            System.err.println("CRITICAL: Failed to open log file for writing: " + e.getMessage());
            return null;
        }
    }

    public static void info(String message) {
        log("INFO", message, null);
    }

    public static void warn(String message) {
        log("WARN", message, null);
    }

    public static void error(String message) {
        log("ERROR", message, null);
    }

    public static void error(String message, Throwable throwable) {
        log("ERROR", message, throwable);
    }

    private static synchronized void log(String level, String message, Throwable throwable) {
        if (writer == null) {
            System.err.println("Logger is not initialized. Cannot write log.");
            return;
        }

        String timestamp = LocalDateTime.now().format(FORMATTER);
        String caller = getCallerInfo();
        String logEntry = String.format("[%s] [%s] [%s] %s", timestamp, level, caller, message);

        System.out.println(logEntry);
        writer.println(logEntry);

        if (throwable != null) {
            throwable.printStackTrace(System.out);
            throwable.printStackTrace(writer);
        }
    }

    private static String getCallerInfo() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();

        for (int i = 3; i < stack.length; i++) {
            StackTraceElement element = stack[i];
            String className = element.getClassName();
            if (!className.equals(Logger.class.getName())) {
                String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
                return simpleClassName + "." + element.getMethodName() + "()";
            }
        }

        return "Unknown";
    }
}
