package muddykat.alchemia.client.gui;

public final class ARGB {

    private ARGB() {
    }

    public static int color(int alpha, int rgb) {
        return (alpha << 24) | (rgb & 0xFFFFFF);
    }

    public static int opaque(int rgb) {
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }
}
