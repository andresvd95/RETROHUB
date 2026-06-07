package com.retrohub.launcher.util;

import android.graphics.Color;

public class ColorUtils {
    private ColorUtils() {}

    public static int parseHex(String hex, int fallback) {
        if (hex == null || hex.isEmpty()) return fallback;
        try {
            return Color.parseColor(hex);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    /** Apply alpha (0..255) to a color. */
    public static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    /** Apply alpha as a 0..1 fraction. */
    public static int withAlphaFraction(int color, float fraction) {
        return withAlpha(color, Math.round(fraction * 255f));
    }
}
