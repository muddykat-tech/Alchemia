package muddykat.alchemia.common.effect;

public final class PortalTimeline {

    public static final int[] HIT_TICKS = {1, 9, 17};
    public static final float[] HIT_PITCH = {1.2F, 0.8F, 0.45F};
    public static final float[] HIT_VOLUME = {0.35F, 0.5F, 0.75F};
    public static final int SHATTER = HIT_TICKS.length - 1;
    public static final int TEAR_START = HIT_TICKS[SHATTER];
    public static final int OPEN_TICKS = 32;
    public static final int FRAME_FROM = 2;
    public static final int FRAME_TICKS = 9;
    public static final int HOLE_FROM = 12;
    public static final int HOLE_TICKS = 9;
    public static final int SLAM_TICK = HOLE_FROM + HOLE_TICKS + 2;
    public static final int UNHIT_FROM = 30;
    public static final int UNHIT_SPACING = 8;
    public static final int COLLAPSE_TICKS = UNHIT_FROM + SHATTER * UNHIT_SPACING + 8;
    public static final double HALF_WIDTH = 0.9;
    public static final double HALF_HEIGHT = 1.35;

    private PortalTimeline() {
    }

    public static boolean crackle(int collapse) {
        return collapse >= FRAME_FROM && collapse < SLAM_TICK && (collapse - FRAME_FROM) % 2 == 0;
    }

    public static int unhitTick(int level) {
        return UNHIT_FROM + (SHATTER - level) * UNHIT_SPACING;
    }
}
