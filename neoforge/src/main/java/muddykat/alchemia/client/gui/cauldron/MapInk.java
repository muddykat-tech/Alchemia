package muddykat.alchemia.client.gui.cauldron;

public final class MapInk {

    public static final int PATH_RGB = 0x3D2616;
    public static final int PATH_ALPHA = 191;
    public static final int PATH_THICKNESS = 2;
    public static final int GHOST_RGB = 0x2B2015;
    public static final int PENDING_RGB = 0x1E1710;
    public static final int BLOCKED_RGB = MapSurface.DEAD_RGB;
    public static final int UNKNOWN_RGB = 0x7A6A50;

    private MapInk() {
    }

    public static int mix(int from, int to, double amount) {
        int r = (int) Math.round(((from >> 16) & 0xFF) * (1 - amount) + ((to >> 16) & 0xFF) * amount);
        int g = (int) Math.round(((from >> 8) & 0xFF) * (1 - amount) + ((to >> 8) & 0xFF) * amount);
        int b = (int) Math.round((from & 0xFF) * (1 - amount) + (to & 0xFF) * amount);
        return (r << 16) | (g << 8) | b;
    }
}
