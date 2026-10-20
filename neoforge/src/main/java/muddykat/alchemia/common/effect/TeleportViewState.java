package muddykat.alchemia.common.effect;

public final class TeleportViewState {

    public static final int CHARGE = 0;
    public static final int OPEN = 1;
    public static final int HOLD = 2;
    public static final int REVEAL = 3;
    public static final int OPEN_TICKS = 12;
    public static final int REVEAL_TICKS = 24;

    private static volatile int phase = -1;
    private static volatile int revision;

    private TeleportViewState() {
    }

    public static int phase() {
        return phase;
    }

    public static int revision() {
        return revision;
    }

    public static void setPhase(int phase) {
        TeleportViewState.phase = phase;
        revision++;
    }
}
