package muddykat.alchemia.client.gui;

public final class CubeIcon {

    public static final int SIZE = 48;

    private static final float PITCH = (float) Math.toRadians(30.0);
    private static final float YAW = (float) Math.toRadians(45.0);
    private static final float PIXELS_PER_UNIT = SIZE * 0.3125F;
    private static final float TOP_LIGHT = 1.0F;
    private static final float LEFT_LIGHT = 0.8F;
    private static final float RIGHT_LIGHT = 0.6F;

    private CubeIcon() {
    }

    public static int[] render(int[] texture, int width, int height) {
        int frame = Math.min(width, height);
        float[] view = {(float) (Math.cos(PITCH) * Math.sin(YAW)), (float) Math.sin(PITCH), (float) (Math.cos(PITCH) * Math.cos(YAW))};
        float[] right = {(float) Math.cos(YAW), 0.0F, (float) -Math.sin(YAW)};
        float[] up = {view[1] * right[2], view[2] * right[0] - view[0] * right[2], -view[1] * right[0]};

        int[] out = new int[SIZE * SIZE];
        for (int py = 0; py < SIZE; py++) {
            for (int px = 0; px < SIZE; px++) {
                float sx = (px + 0.5F - SIZE / 2.0F) / PIXELS_PER_UNIT;
                float sy = (SIZE / 2.0F - py - 0.5F) / PIXELS_PER_UNIT;
                float[] origin = new float[3];
                for (int axis = 0; axis < 3; axis++) origin[axis] = sx * right[axis] + sy * up[axis] + 4.0F * view[axis];
                out[py * SIZE + px] = trace(origin, view, texture, width, frame);
            }
        }
        return out;
    }

    private static int trace(float[] origin, float[] view, int[] texture, int width, int frame) {
        float near = -Float.MAX_VALUE;
        float far = Float.MAX_VALUE;
        int face = -1;
        for (int axis = 0; axis < 3; axis++) {
            float direction = -view[axis];
            float a = (-1.0F - origin[axis]) / direction;
            float b = (1.0F - origin[axis]) / direction;
            float enter = Math.min(a, b);
            if (enter > near) {
                near = enter;
                face = axis;
            }
            far = Math.min(far, Math.max(a, b));
        }
        if (near > far) return 0;

        float x = origin[0] - view[0] * near;
        float y = origin[1] - view[1] * near;
        float z = origin[2] - view[2] * near;
        float u;
        float v;
        float light;
        switch (face) {
            case 1 -> {
                u = (x + 1.0F) / 2.0F;
                v = (z + 1.0F) / 2.0F;
                light = TOP_LIGHT;
            }
            case 2 -> {
                u = (x + 1.0F) / 2.0F;
                v = (1.0F - y) / 2.0F;
                light = LEFT_LIGHT;
            }
            default -> {
                u = (1.0F - z) / 2.0F;
                v = (1.0F - y) / 2.0F;
                light = RIGHT_LIGHT;
            }
        }

        int sampleX = Math.min(frame - 1, Math.max(0, (int) (u * frame)));
        int sampleY = Math.min(frame - 1, Math.max(0, (int) (v * frame)));
        int pixel = texture[sampleY * width + sampleX];
        if ((pixel >>> 24) == 0) return 0;
        int red = Math.round((pixel >> 16 & 0xFF) * light);
        int green = Math.round((pixel >> 8 & 0xFF) * light);
        int blue = Math.round((pixel & 0xFF) * light);
        return pixel & 0xFF000000 | red << 16 | green << 8 | blue;
    }
}
