package com.eliza.messenger.util;

import android.util.Log;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.StorageException;

/**
 * Specialized logger for Firebase-related operations.
 * Provides detailed error information for Firebase exceptions.
 */
public class FirebaseLogger {
    private static final String TAG = "FirebaseLogger";

    /**
     * Log a Firebase Authentication error with detailed information.
     *
     * @param tag Used to identify the source of a log message
     * @param message The message to log
     * @param exception The Firebase exception
     */
    public static void logAuthError(String tag, String message, FirebaseAuthException exception) {
        String errorCode = exception.getErrorCode();
        String errorMessage = getAuthErrorMessage(errorCode);
        
        Logger.e(tag, message + " - Code: " + errorCode + " - " + errorMessage, exception);
    }

    /**
     * Log a Firebase Firestore error with detailed information.
     *
     * @param tag Used to identify the source of a log message
     * @param message The message to log
     * @param exception The Firebase exception
     */
    public static void logFirestoreError(String tag, String message, FirebaseFirestoreException exception) {
        String code = exception.getCode().toString();
        
        Logger.e(tag, message + " - Code: " + code, exception);
    }

    /**
     * Log a Firebase Storage error with detailed information.
     *
     * @param tag Used to identify the source of a log message
     * @param message The message to log
     * @param exception The Firebase exception
     */
    public static void logStorageError(String tag, String message, StorageException exception) {
        int errorCode = exception.getErrorCode();
        String errorMessage = getStorageErrorMessage(errorCode);
        
        Logger.e(tag, message + " - Code: " + errorCode + " - " + errorMessage, exception);
    }

    /**
     * Log a general Firebase error.
     *
     * @param tag Used to identify the source of a log message
     * @param message The message to log
     * @param exception The Firebase exception
     */
    public static void logError(String tag, String message, FirebaseException exception) {
        Logger.e(tag, message, exception);
    }

    /**
     * Get a human-readable error message for Firebase Authentication error codes.
     *
     * @param errorCode The Firebase Auth error code
     * @return A human-readable error message
     */
    private static String getAuthErrorMessage(String errorCode) {
        switch (errorCode) {
            case "ERROR_INVALID_CUSTOM_TOKEN":
                return "The custom token format is incorrect or the token is invalid.";
            case "ERROR_CUSTOM_TOKEN_MISMATCH":
                return "The custom token corresponds to a different audience.";
            case "ERROR_INVALID_CREDENTIAL":
                return "The credential is malformed or has expired.";
            case "ERROR_INVALID_EMAIL":
                return "The email address is badly formatted.";
            case "ERROR_WRONG_PASSWORD":
                return "The password is invalid.";
            case "ERROR_USER_MISMATCH":
                return "The supplied credentials do not correspond to the previously signed in user.";
            case "ERROR_REQUIRES_RECENT_LOGIN":
                return "This operation requires recent authentication. Log in again before retrying.";
            case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
                return "An account already exists with the same email address but different sign-in credentials.";
            case "ERROR_EMAIL_ALREADY_IN_USE":
                return "The email address is already in use by another account.";
            case "ERROR_CREDENTIAL_ALREADY_IN_USE":
                return "This credential is already associated with a different user account.";
            case "ERROR_USER_DISABLED":
                return "The user account has been disabled by an administrator.";
            case "ERROR_USER_TOKEN_EXPIRED":
                return "The user's credential has expired. The user must sign in again.";
            case "ERROR_USER_NOT_FOUND":
                return "There is no user record corresponding to this identifier.";
            case "ERROR_INVALID_USER_TOKEN":
                return "The user's credential is no longer valid. The user must sign in again.";
            case "ERROR_OPERATION_NOT_ALLOWED":
                return "This operation is not allowed. Enable the service in the Firebase console.";
            case "ERROR_WEAK_PASSWORD":
                return "The password is too weak.";
            case "ERROR_MISSING_EMAIL":
                return "An email address must be provided.";
            case "ERROR_MISSING_PHONE_NUMBER":
                return "A phone number must be provided.";
            case "ERROR_INVALID_PHONE_NUMBER":
                return "The phone number is not valid.";
            case "ERROR_MISSING_VERIFICATION_CODE":
                return "The verification code is missing.";
            case "ERROR_INVALID_VERIFICATION_CODE":
                return "The verification code is invalid.";
            case "ERROR_MISSING_VERIFICATION_ID":
                return "The verification ID is missing.";
            case "ERROR_INVALID_VERIFICATION_ID":
                return "The verification ID is invalid.";
            case "ERROR_SESSION_EXPIRED":
                return "The SMS code has expired. Please re-send the verification code to try again.";
            case "ERROR_QUOTA_EXCEEDED":
                return "The SMS quota for the project has been exceeded.";
            case "ERROR_APP_NOT_AUTHORIZED":
                return "This app is not authorized to use Firebase Authentication.";
            case "CONFIGURATION_NOT_FOUND":
                return "Firebase Phone Auth is not properly configured. Check Firebase console settings.";
            default:
                return "An unknown error occurred: " + errorCode;
        }
    }

    /**
     * Get a human-readable error message for Firebase Storage error codes.
     *
     * @param errorCode The Firebase Storage error code
     * @return A human-readable error message
     */
    private static String getStorageErrorMessage(int errorCode) {
        switch (errorCode) {
            case StorageException.ERROR_OBJECT_NOT_FOUND:
                return "Object not found.";
            case StorageException.ERROR_BUCKET_NOT_FOUND:
                return "Bucket not found.";
            case StorageException.ERROR_PROJECT_NOT_FOUND:
                return "Project not found.";
            case StorageException.ERROR_QUOTA_EXCEEDED:
                return "Quota exceeded.";
            case StorageException.ERROR_NOT_AUTHENTICATED:
                return "User not authenticated.";
            case StorageException.ERROR_NOT_AUTHORIZED:
                return "User not authorized.";
            case StorageException.ERROR_RETRY_LIMIT_EXCEEDED:
                return "Retry limit exceeded.";
            case StorageException.ERROR_INVALID_CHECKSUM:
                return "Invalid checksum.";
            case StorageException.ERROR_CANCELED:
                return "Operation canceled.";
            default:
                return "Unknown error: " + errorCode;
        }
    }
}
