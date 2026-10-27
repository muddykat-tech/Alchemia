package muddykat.alchemia.common.blocks.tileentity.cauldron;

import muddykat.alchemia.common.potion.CauldronHeat;
import net.minecraft.world.entity.player.Player;
import javax.annotation.Nullable;

import java.util.UUID;

public final class CauldronBoiler {

    public static final double TICK_SECONDS = 0.05;

    private static final int HOLD_TICKS = (int) (CauldronHeat.HOLD_MS / 50L);
    private static final int BOIL_TICKS = (int) (CauldronHeat.BOIL_MS / 50L);

    private double heat;
    private long holdUntil;
    private int boilTicks;
    private @Nullable UUID lastHeater;

    public double heat() {
        return heat;
    }

    public int boilTicks() {
        return boilTicks;
    }

    public boolean boiling() {
        return boilTicks > 0;
    }

    public double intensity(int waterLevel) {
        return CauldronHeat.intensity(heat, waterLevel);
    }

    public @Nullable UUID lastHeater() {
        return lastHeater;
    }

    public boolean pump(long now, int waterLevel, @Nullable Player player) {
        double cap = CauldronHeat.cap(waterLevel);
        heat = Math.min(cap, heat + CauldronHeat.PUMP);
        holdUntil = now + HOLD_TICKS;
        if (player != null) lastHeater = player.getUUID();
        return heat >= cap - 1.0E-6;
    }

    public void boil(int waterLevel) {
        boilTicks = BOIL_TICKS;
        heat = CauldronHeat.cap(waterLevel);
    }

    public void strain(int waterLevel) {
        heat = Math.max(0.0, CauldronHeat.cap(waterLevel) - CauldronHeat.STRAIN_GAP);
    }

    public void douse() {
        heat = 0.0;
        boilTicks = 0;
    }

    public void tick(long now, int waterLevel) {
        double cap = CauldronHeat.cap(waterLevel);
        if (boilTicks > 0) {
            if (--boilTicks == 0) {
                heat = cap * CauldronHeat.BOIL_REST_SHARE;
                holdUntil = now + HOLD_TICKS;
            }
        } else if (now >= holdUntil) {
            heat = Math.max(0.0, heat - CauldronHeat.COOL_PER_SECOND * TICK_SECONDS);
        }
        heat = Math.min(heat, cap);
    }
}
