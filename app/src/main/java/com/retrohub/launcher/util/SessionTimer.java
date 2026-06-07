package com.retrohub.launcher.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.retrohub.launcher.data.repository.GameRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks active play sessions in SharedPreferences. On resume of the main activity,
 * any finished session is committed to Room as added play time.
 */
public class SessionTimer {
    private static final String PREFS = "session_timer";
    private static final String PREFIX = "session_start_";

    private SessionTimer() {}

    public static void start(Context context, int gameId) {
        prefs(context).edit().putLong(PREFIX + gameId, System.currentTimeMillis()).apply();
    }

    /** Flush all open sessions, adding their elapsed time into Room. */
    public static void flush(Context context, GameRepository repo) {
        SharedPreferences sp = prefs(context);
        Map<String, ?> all = sp.getAll();
        if (all.isEmpty()) return;
        long now = System.currentTimeMillis();
        SharedPreferences.Editor editor = sp.edit();
        Map<Integer, Long> toAdd = new HashMap<>();
        for (Map.Entry<String, ?> entry : all.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith(PREFIX)) continue;
            if (!(entry.getValue() instanceof Long)) continue;
            long start = (Long) entry.getValue();
            int gameId;
            try {
                gameId = Integer.parseInt(key.substring(PREFIX.length()));
            } catch (NumberFormatException e) {
                editor.remove(key);
                continue;
            }
            long elapsedSeconds = Math.max(0, (now - start) / 1000L);
            if (elapsedSeconds > 0) toAdd.put(gameId, elapsedSeconds);
            editor.remove(key);
        }
        editor.apply();
        for (Map.Entry<Integer, Long> e : toAdd.entrySet()) {
            repo.addPlayTime(e.getKey(), e.getValue());
        }
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
