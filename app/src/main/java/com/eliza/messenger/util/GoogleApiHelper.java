package com.eliza.messenger.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.util.Log;
import androidx.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class GoogleApiHelper {
    private static final String TAG = "GoogleApiHelper";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Check if Google Play Services is available and properly configured
     * @param context The application context
     * @return true if Google Play Services is available, false otherwise
     */
    public static boolean checkPlayServices(@NonNull Context context) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
        
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                if (context instanceof Activity) {
                    apiAvailability.showErrorDialogFragment(
                        (Activity) context,
                        resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST,
                        dialog -> {
                            // User clicked on the dialog
                            Log.d(TAG, "User clicked on Google Play Services dialog");
                        });
                }
            } else {
                Log.e(TAG, "This device is not supported by Google Play Services. Error code: " + resultCode);
                android.widget.Toast.makeText(context, 
                    "This device is not supported by Google Play Services. Please update it from Play Store.", 
                    android.widget.Toast.LENGTH_LONG).show();
            }
            return false;
        }
        return true;
    }

    /**
     * Handle security exceptions that may occur during Google Play Services operations
     * @param activity The activity context
     * @param e The security exception
     */
    public static void handleSecurityException(Activity activity, SecurityException e) {
        Log.e(TAG, "Security exception occurred: " + e.getMessage());
        android.widget.Toast.makeText(activity, 
            "Permission denied. Please grant required permissions.", 
            android.widget.Toast.LENGTH_LONG).show();
    }

    /**
     * Handle activity result for Google Play Services operations
     * @param requestCode The request code
     * @param resultCode The result code
     * @param data The intent data
     * @return true if the result was handled, false otherwise
     */
    public static boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLAY_SERVICES_RESOLUTION_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "Google Play Services resolved successfully");
                return true;
            } else {
                Log.d(TAG, "Google Play Services resolution failed");
                return true;
            }
        }
        return false;
    }

    /**
     * Clear Google Play Services cache and attempt to refresh connection
     * @param context The application context
     */
    public static void clearGooglePlayServicesCache(@NonNull Context context) {
        try {
            // Make Google Play Services show the setup wizard
            int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
            if (resultCode != ConnectionResult.SUCCESS) {
                if (GoogleApiAvailability.getInstance().isUserResolvableError(resultCode)) {
                    GoogleApiAvailability.getInstance().showErrorNotification(context, resultCode);
                    Log.d(TAG, "Showing Google Play Services error notification to help user resolve the issue");
                }
            }
            
            // Try to make the device reconnect to Google services
            try {
                // Force a refresh of Google Play Services
                context.getPackageManager().getPackageInfo("com.google.android.gms", 0);
                Log.d(TAG, "Attempting to refresh Google Play Services connection");
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Google Play Services package not found");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error refreshing Google Play Services: " + e.getMessage());
        }
    }

    /**
     * Check if the device is compatible with Google Play Services
     * @param context The application context
     * @return true if device is compatible, false otherwise
     */
    public static boolean isDeviceCompatible(@NonNull Context context) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        return apiAvailability.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
    }

    /**
     * Get the error message for a Google Play Services error code
     * @param context The application context
     * @param errorCode The error code from GoogleApiAvailability
     * @return The error message
     */
    public static String getErrorString(@NonNull Context context, int errorCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        return apiAvailability.getErrorString(errorCode);
    }
}
