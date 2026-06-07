package com.retrohub.launcher.util;

import java.util.concurrent.TimeUnit;

public class TimeUtils {
    private TimeUtils() {}

    /** Format seconds → "12h 34m", "5m 12s", or "—" if zero. */
    public static String formatPlayTime(long seconds) {
        if (seconds <= 0) return "—";
        long hours = TimeUnit.SECONDS.toHours(seconds);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds) - hours * 60;
        long secs = seconds - hours * 3600 - minutes * 60;
        if (hours > 0) return hours + "h " + minutes + "m";
        if (minutes > 0) return minutes + "m " + secs + "s";
        return secs + "s";
    }

    /** Short form: "12h", "34m", "—" — for compact cards. */
    public static String formatPlayTimeShort(long seconds) {
        if (seconds <= 0) return "—";
        long hours = TimeUnit.SECONDS.toHours(seconds);
        if (hours > 0) return hours + "h";
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        if (minutes > 0) return minutes + "m";
        return seconds + "s";
    }
}
