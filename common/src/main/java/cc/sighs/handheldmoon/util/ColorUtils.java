package cc.sighs.handheldmoon.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ColorUtils {
    private ColorUtils() {}

    public static List<float[]> parseColorStops(List<? extends String> list) {
        List<float[]> res = new ArrayList<>();
        if (list == null || list.isEmpty()) {
            res.add(new float[]{1.0f, 1.0f, 1.0f});
            return res;
        }
        for (String s : list) {
            String t = normalizeRgbaHex(s);
            if (t == null) continue;
            try {
                int r = Integer.parseInt(t.substring(0, 2), 16);
                int g = Integer.parseInt(t.substring(2, 4), 16);
                int b = Integer.parseInt(t.substring(4, 6), 16);
                res.add(new float[]{r / 255.0f, g / 255.0f, b / 255.0f});
            } catch (RuntimeException ignored) {
                // Keep malformed device stops out of the gradient.
            }
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

    /** Parses an {@code RRGGBBAA} device color into RGB floats; alpha is ignored. */
    public static float[] parseColorARGB(String s) {
        String t = normalizeRgbaHex(s);
        if (t == null) return new float[]{1.0f, 1.0f, 1.0f};
        try {
            int r = Integer.parseInt(t.substring(0, 2), 16);
            int g = Integer.parseInt(t.substring(2, 4), 16);
            int b = Integer.parseInt(t.substring(4, 6), 16);
            return new float[]{r / 255.0f, g / 255.0f, b / 255.0f};
        } catch (RuntimeException ignored) {
            return new float[]{1.0f, 1.0f, 1.0f};
        }
    }

    /** Parses a Web/CSS hex color ({@code #RRGGBB} or {@code #RRGGBBAA}). */
    public static Color parseJavaColor(String source) {
        Color color = tryParseWebColor(source);
        return color != null ? color : DEFAULT_COLOR;
    }

    /** Parses the legacy AARRGGBB format used by existing device settings. */
    private static Color parseArgbColor(String source) {
        Color color = tryParseArgbColor(source);
        return color != null ? color : DEFAULT_COLOR;
    }

    private static Color tryParseArgbColor(String source) {
        String value = source == null ? "" : source.trim();
        if (value.startsWith("#")) {
            value = value.substring(1);
        } else if (value.startsWith("0x") || value.startsWith("0X")) {
            value = value.substring(2);
        }
        try {
            if (value.length() == 8) {
                int alpha = Integer.parseInt(value.substring(0, 2), 16);
                Color rgb = Color.decode("#" + value.substring(2));
                return new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), alpha);
            }
            if (value.length() == 6) {
                Color rgb = Color.decode("#" + value);
                return new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), 255);
            }
        } catch (RuntimeException ignored) {
            return null;
        }
        return null;
    }

    /** Normalizes an {@code RRGGBBAA} hex color (alpha last, no '#'). */
    private static String normalizeRgbaHex(String source) {
        if (source == null) {
            return null;
        }
        String value = source.trim();
        if (value.isEmpty()) {
            return null;
        }
        if (value.startsWith("#")) {
            value = value.substring(1);
        }
        if (value.length() == 6) {
            value = value + "FF";
        }
        return value.length() == 8 ? value : null;
    }

    private static Color tryParseWebColor(String source) {
        String value = source == null ? "" : source.trim();
        if (value.startsWith("#")) {
            value = value.substring(1);
        } else if (value.startsWith("0x") || value.startsWith("0X")) {
            value = value.substring(2);
        }
        if (value.length() == 3 || value.length() == 4) {
            StringBuilder expanded = new StringBuilder(value.length() * 2);
            for (int i = 0; i < value.length(); i++) {
                char channel = value.charAt(i);
                expanded.append(channel).append(channel);
            }
            value = expanded.toString();
        }
        try {
            if (value.length() == 8) {
                int alpha = Integer.parseInt(value.substring(6, 8), 16);
                Color rgb = Color.decode("#" + value.substring(0, 6));
                return new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), alpha);
            }
            if (value.length() == 6) {
                Color rgb = Color.decode("#" + value);
                return new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), 255);
            }
        } catch (RuntimeException ignored) {
            return null;
        }
        return null;
    }

    /** Returns a canonical Web/CSS {@code #RRGGBBAA} string for profile storage. */
    public static String normalizeWebColor(String source) {
        return formatWebColor(parseJavaColor(source));
    }

    /** Converts a legacy AARRGGBB setting to canonical Web/CSS form. */
    public static String argbToWebColor(String source) {
        return formatWebColor(parseArgbColor(source));
    }

    /** Converts a Web/CSS color to canonical uppercase RRGGBBAA form (alpha last, no '#'). */
    public static String webColorToRgbaHex(String source) {
        Color color = parseJavaColor(source);
        return String.format(Locale.ROOT, "%02X%02X%02X%02X",
                color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    /** Converts a Web/CSS color to canonical uppercase AARRGGBB form. */
    public static String webColorToArgb(String source) {
        Color color = parseJavaColor(source);
        return String.format(Locale.ROOT, "%02X%02X%02X%02X",
                color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
    }

    /** Returns a canonical uppercase AARRGGBB string for legacy settings. */
    public static String normalizeColorARGB(String source) {
        Color color = parseArgbColor(source);
        return String.format(Locale.ROOT, "%02X%02X%02X%02X",
                color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
    }

    private static String formatWebColor(Color color) {
        return String.format(Locale.ROOT, "#%02X%02X%02X%02X",
                color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    /** Returns RGBA floats for a Web/CSS color, retaining its trailing alpha. */
    public static float[] parseColorRGBAWithAlpha(String source) {
        Color color = parseJavaColor(source);
        return new float[]{
                color.getRed() / 255.0f,
                color.getGreen() / 255.0f,
                color.getBlue() / 255.0f,
                color.getAlpha() / 255.0f
        };
    }

    /**
     * @deprecated colors are stored as {@code RRGGBBAA}; use {@link #parseColorRGBAWithAlpha(String)}.
     */
    @Deprecated
    public static float[] parseColorARGBWithAlpha(String source) {
        return parseColorRGBAWithAlpha(source);
    }

    private static float[] rgb(Color color) {
        return new float[]{
                color.getRed() / 255.0f,
                color.getGreen() / 255.0f,
                color.getBlue() / 255.0f
        };
    }

    private static final Color DEFAULT_COLOR = new Color(255, 255, 255, 255);
}
