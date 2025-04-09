package com.eliza.messenger.util;

import android.util.Log;

/**
 * Centralized logging utility for the Eliza Messenger app.
 * Provides consistent logging across the application with configurable log levels.
 */
public class Logger {
    private static final boolean LOGGING_ENABLED = true;
    private static final int LOG_LEVEL = Log.VERBOSE; // Set to Log.ERROR in production

    /**
     * Log a verbose message.
     *
     * @param tag Used to identify the source of a log message
     * @param message The message to log
     */
    public static void v(String tag, String message) {
        if (LOGGING_ENABLED && LOG_LEVEL <= Log.VERBOSE) {
            Log.v(tag, message);
        }
    }

    /**
     * Log a debug message.
     *
     * @param tag Used to identify the source of a log message
     * @param message The message to log
     */
    public static void d(String tag, String message) {
        if (LOGGING_ENABLED && LOG_LEVEL <= Log.DEBUG) {
            Log.d(tag, message);
        }
    }

    /**
     * Log an info message.
     *
     * @param tag Used to identify the source of a log message
     * @param message The message to log
     */
    public static void i(String tag, String message) {
        if (LOGGING_ENABLED && LOG_LEVEL <= Log.INFO) {
            Log.i(tag, message);
        }
    }

    /**
     * Log a warning message.
     *
     * @param tag Used to identify the source of a log message
     * @param message The message to log
     */
    public static void w(String tag, String message) {
        if (LOGGING_ENABLED && LOG_LEVEL <= Log.WARN) {
            Log.w(tag, message);
        }
    }

    /**
     * Log an error message.
     *
     * @param tag Used to identify the source of a log message
     * @param message The message to log
     */
    public static void e(String tag, String message) {
        if (LOGGING_ENABLED && LOG_LEVEL <= Log.ERROR) {
            Log.e(tag, message);
        }
    }

    /**
     * Log an error message with an exception.
     *
     * @param tag Used to identify the source of a log message
     * @param message The message to log
     * @param throwable The exception to log
     */
    public static void e(String tag, String message, Throwable throwable) {
        if (LOGGING_ENABLED && LOG_LEVEL <= Log.ERROR) {
            Log.e(tag, message, throwable);
        }
    }

    /**
     * Log a message that will be written to the log regardless of log level.
     *
     * @param tag Used to identify the source of a log message
     * @param message The message to log
     */
    public static void wtf(String tag, String message) {
        if (LOGGING_ENABLED) {
            Log.wtf(tag, message);
        }
    }

    /**
     * Log a message that will be written to the log regardless of log level with an exception.
     *
     * @param tag Used to identify the source of a log message
     * @param message The message to log
     * @param throwable The exception to log
     */
    public static void wtf(String tag, String message, Throwable throwable) {
        if (LOGGING_ENABLED) {
            Log.wtf(tag, message, throwable);
        }
    }
}
