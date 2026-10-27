package muddykat.alchemia.common.effect;

public final class TeleportViewState {

    public static final int CHARGE = 0;
    public static final int OPEN = 1;
    public static final int HOLD = 2;
    public static final int REVEAL = 3;
    public static final int OPEN_TICKS = 12;

    private static volatile int phase = -1;
    private static volatile float chargeRate = 1.0F;
    private static volatile int revision;

    private TeleportViewState() {
    }

    public static int phase() {
        return phase;
    }

    public static int revision() {
        return revision;
    }

    public static float chargeRate() {
        return chargeRate;
    }

    public static void setPhase(int phase, float chargeRate) {
        TeleportViewState.phase = phase;
        TeleportViewState.chargeRate = chargeRate;
        revision++;
    }
}
