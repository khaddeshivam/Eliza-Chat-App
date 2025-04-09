package com.eliza.messenger.util;

import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateUtils {
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public static String formatCallTime(Timestamp timestamp) {
        if (timestamp == null) return "";
        Date date = timestamp.toDate();
        return TIME_FORMAT.format(date);
    }

    public static String formatCallDuration(long durationInSeconds) {
        if (durationInSeconds < 60) {
            return durationInSeconds + "s";
        } else {
            long minutes = TimeUnit.SECONDS.toMinutes(durationInSeconds);
            long seconds = durationInSeconds - TimeUnit.MINUTES.toSeconds(minutes);
            return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
        }
    }

    public static String formatDate(Timestamp timestamp) {
        if (timestamp == null) return "";
        Date date = timestamp.toDate();
        return DATE_FORMAT.format(date);
    }
}