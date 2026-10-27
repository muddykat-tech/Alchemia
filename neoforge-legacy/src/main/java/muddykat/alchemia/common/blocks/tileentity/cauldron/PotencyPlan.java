package muddykat.alchemia.common.blocks.tileentity.cauldron;

import net.minecraft.world.effect.MobEffectInstance;

import java.util.List;

public record PotencyPlan(List<MobEffectInstance> effects, List<String> order) {

    public static final String REDSTONE_TOKEN = "redstone";

    public int redstone() {
        int charges = 0;
        for (String token : order) {
            if (token.equals(REDSTONE_TOKEN)) charges++;
        }
        return charges;
    }
}
