package muddykat.alchemia.client.effect;

import net.minecraft.util.Mth;

public final class ARGB {

    private ARGB() {
    }

    public static int alpha(int color) {
        return color >>> 24;
    }

    public static int red(int color) {
        return color >> 16 & 0xFF;
    }

    public static int green(int color) {
        return color >> 8 & 0xFF;
    }

    public static int blue(int color) {
        return color & 0xFF;
    }

    public static int color(int alpha, int red, int green, int blue) {
        return (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
    }

    public static int color(int red, int green, int blue) {
        return color(255, red, green, blue);
    }

    public static int color(int alpha, int rgb) {
        return alpha << 24 | rgb & 0xFFFFFF;
    }

    public static int color(float alpha, int rgb) {
        return channel(alpha) << 24 | rgb & 0xFFFFFF;
    }

    public static int opaque(int color) {
        return color | 0xFF000000;
    }

    public static int colorFromFloat(float alpha, float red, float green, float blue) {
        return color(channel(alpha), channel(red), channel(green), channel(blue));
    }

    public static int srgbLerp(float delta, int from, int to) {
        return color(Mth.lerpInt(delta, alpha(from), alpha(to)), Mth.lerpInt(delta, red(from), red(to)),
                Mth.lerpInt(delta, green(from), green(to)), Mth.lerpInt(delta, blue(from), blue(to)));
    }

    public static int scaleRGB(int color, float scale) {
        return color(alpha(color), Mth.clamp((int) (red(color) * scale), 0, 255), Mth.clamp((int) (green(color) * scale), 0, 255),
                Mth.clamp((int) (blue(color) * scale), 0, 255));
    }

    private static int channel(float value) {
        return Mth.floor(value * 255.0F);
    }
}
