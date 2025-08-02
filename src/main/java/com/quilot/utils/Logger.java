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

    private static void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String caller = getCallerInfo();
        String logEntry = String.format("[%s] [%s] [%s] %s", timestamp, level, caller, message);

        System.out.println(logEntry);

        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE_NAME, true))) {
            writer.println(logEntry);
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }

    /**
     * Retrieves the class and method name from which the log method was called.
     * @return the class and method in the format ClassName.methodName()
     */
    private static String getCallerInfo() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();

        for (int i = 2; i < stack.length; i++) {
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
