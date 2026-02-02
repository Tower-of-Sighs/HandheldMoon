package com.sighs.handheldmoon.util;

import java.util.ArrayList;
import java.util.List;

public final class ColorUtils {
    private ColorUtils() {}

    public static List<float[]> parseColorStops(List<? extends String> list) {
        List<float[]> res = new ArrayList<>();
        if (list == null || list.isEmpty()) {
            res.add(new float[]{1.0f, 1.0f, 1.0f});
            return res;
        }
        for (String s : list) {
            if (s == null) continue;
            String t = s.startsWith("#") ? s.substring(1) : s;
            if (t.length() < 8) continue;
            try {
                int r = Integer.parseInt(t.substring(2, 4), 16);
                int g = Integer.parseInt(t.substring(4, 6), 16);
                int b = Integer.parseInt(t.substring(6, 8), 16);
                res.add(new float[]{r / 255.0f, g / 255.0f, b / 255.0f});
            } catch (Exception ignored) {}
        }
        if (res.isEmpty()) res.add(new float[]{1.0f, 1.0f, 1.0f});
        return res;
    }

    public static float[] colorAt(List<float[]> stops, float t) {
        if (stops.isEmpty()) return new float[]{1.0f, 1.0f, 1.0f};
        if (stops.size() == 1) return stops.getFirst();
        float tt = Math.max(0.0f, Math.min(1.0f, t));
        float pos = tt * (stops.size() - 1);
        int i0 = (int) Math.floor(pos);
        int i1 = Math.min(stops.size() - 1, i0 + 1);
        float w = pos - i0;
        float[] a = stops.get(i0);
        float[] b = stops.get(i1);
        return new float[]{
                a[0] + (b[0] - a[0]) * w,
                a[1] + (b[1] - a[1]) * w,
                a[2] + (b[2] - a[2]) * w
        };
    }

    public static float[] averageColor(List<float[]> stops) {
        if (stops.isEmpty()) return new float[]{1.0f, 1.0f, 1.0f};
        float r = 0f, g = 0f, b = 0f;
        for (float[] c : stops) {
            r += c[0];
            g += c[1];
            b += c[2];
        }
        float inv = 1f / stops.size();
        return new float[]{r * inv, g * inv, b * inv};
    }

    public static float[] colorAtWithNoise(List<float[]> stops, float baseT, float thetaNorm, long seed, float amplitude) {
        float n1 = (float) Math.sin(thetaNorm * 7.23 + seed * 0.001);
        float n2 = (float) Math.sin(thetaNorm * 13.69 + seed * 0.002);
        float n3 = (float) Math.sin(thetaNorm * 19.41 + seed * 0.0007);
        float n = 0.5f + 0.20f * n1 + 0.20f * n2 + 0.10f * n3;
        float wobble = (n - 0.5f) * 2f * amplitude;
        float t = Math.max(0.0f, Math.min(1.0f, baseT + wobble));
        return colorAt(stops, t);
    }

    public static float[] parseColorARGB(String s) {
        if (s == null) return new float[]{1.0f, 1.0f, 1.0f};
        String t = s.startsWith("#") ? s.substring(1) : s;
        if (t.length() < 8) return new float[]{1.0f, 1.0f, 1.0f};
        try {
            int r = Integer.parseInt(t.substring(2, 4), 16);
            int g = Integer.parseInt(t.substring(4, 6), 16);
            int b = Integer.parseInt(t.substring(6, 8), 16);
            return new float[]{r / 255.0f, g / 255.0f, b / 255.0f};
        } catch (Exception e) {
            return new float[]{1.0f, 1.0f, 1.0f};
        }
    }
}
