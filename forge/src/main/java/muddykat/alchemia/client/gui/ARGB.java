package muddykat.alchemia.client.gui;

public final class ARGB {

    private ARGB() {
    }

    public static int color(int alpha, int rgb) {
        return (alpha << 24) | (rgb & 0xFFFFFF);
    }

    public static int alpha(int argb) {
        return argb >>> 24;
    }

    public static int opaque(int rgb) {
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }

    public static int srgbLerp(float delta, int from, int to) {
        int red = Math.round(((from >> 16) & 255) + (((to >> 16) & 255) - ((from >> 16) & 255)) * delta);
        int green = Math.round(((from >> 8) & 255) + (((to >> 8) & 255) - ((from >> 8) & 255)) * delta);
        int blue = Math.round((from & 255) + ((to & 255) - (from & 255)) * delta);
        int alpha = Math.round((from >>> 24) + ((to >>> 24) - (from >>> 24)) * delta);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}
