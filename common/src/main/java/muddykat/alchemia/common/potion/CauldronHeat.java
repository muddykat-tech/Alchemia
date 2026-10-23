package muddykat.alchemia.common.potion;

public final class CauldronHeat {

    public static final double PUMP = 0.16;
    public static final double COOL_PER_SECOND = 0.09;
    public static final long HOLD_MS = 350L;
    public static final double STRAIN_GAP = 0.07;
    public static final long BOIL_MS = 450L;
    public static final double BOIL_REST_SHARE = 0.35;
    public static final int SYNC_SCALE = 10000;

    private CauldronHeat() {
    }

    public static double cap(int waterLevel) {
        return Math.max(0.0, Math.min(1.0, (double) waterLevel / BrewBase.MAX_FILL_LEVEL));
    }

    public static double intensity(double heat, int waterLevel) {
        double cap = cap(waterLevel);
        return cap <= 0.0 ? 0.0 : Math.max(0.0, Math.min(1.0, heat / cap));
    }
}
