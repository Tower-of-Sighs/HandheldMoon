package cc.sighs.handheldmoon.util;

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
            String t = normalizeArgbHex(s);
            if (t == null) continue;
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
        float[] result = new float[3];
        colorAtInto(stops, t, result);
        return result;
    }

    /** Samples a gradient into a caller-owned RGB buffer. */
    public static void colorAtInto(List<float[]> stops, float t, float[] result) {
        if (stops.isEmpty()) {
            result[0] = 1.0f;
            result[1] = 1.0f;
            result[2] = 1.0f;
            return;
        }
        if (stops.size() == 1) {
            float[] color = stops.get(0);
            result[0] = color[0];
            result[1] = color[1];
            result[2] = color[2];
            return;
        }
        float tt = Math.max(0.0f, Math.min(1.0f, t));
        float pos = tt * (stops.size() - 1);
        int i0 = (int) Math.floor(pos);
        int i1 = Math.min(stops.size() - 1, i0 + 1);
        float w = pos - i0;
        float[] a = stops.get(i0);
        float[] b = stops.get(i1);
        result[0] = a[0] + (b[0] - a[0]) * w;
        result[1] = a[1] + (b[1] - a[1]) * w;
        result[2] = a[2] + (b[2] - a[2]) * w;
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

    public static float[] colorAtWithNoise(List<float[]> stops, float baseT,
                                           float n1, float n2, float n3,
                                           float amplitude) {
        float n = 0.5f + 0.20f * n1 + 0.20f * n2 + 0.10f * n3;
        float wobble = (n - 0.5f) * 2f * amplitude;
        float[] result = new float[3];
        colorAtInto(stops, baseT + wobble, result);
        return result;
    }

    /** Samples a noisy gradient into a caller-owned RGB buffer. */
    public static void colorAtWithNoiseInto(List<float[]> stops, float baseT,
                                             float n1, float n2, float n3,
                                             float amplitude, float[] result) {
        float n = 0.5f + 0.20f * n1 + 0.20f * n2 + 0.10f * n3;
        float wobble = (n - 0.5f) * 2f * amplitude;
        colorAtInto(stops, baseT + wobble, result);
    }

    public static float[] parseColorARGB(String s) {
        String t = normalizeArgbHex(s);
        if (t == null) return new float[]{1.0f, 1.0f, 1.0f};
        try {
            int r = Integer.parseInt(t.substring(2, 4), 16);
            int g = Integer.parseInt(t.substring(4, 6), 16);
            int b = Integer.parseInt(t.substring(6, 8), 16);
            return new float[]{r / 255.0f, g / 255.0f, b / 255.0f};
        } catch (Exception e) {
            return new float[]{1.0f, 1.0f, 1.0f};
        }
    }

    private static String normalizeArgbHex(String source) {
        if (source == null) {
            return null;
        }
        String t = source.trim();
        if (t.isEmpty()) {
            return null;
        }
        if (t.startsWith("#")) {
            t = t.substring(1);
        }
        if (t.length() == 6) {
            t = "FF" + t;
        }
        if (t.length() != 8) {
            return null;
        }
        return t;
    }
}
