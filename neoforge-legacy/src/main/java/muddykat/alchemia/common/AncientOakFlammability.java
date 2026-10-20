package muddykat.alchemia.common;

import muddykat.alchemia.common.blocks.AncientOak;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;

public final class AncientOakFlammability {

    private AncientOakFlammability() {
    }

    public static void register() {
        FireBlock fire = (FireBlock) Blocks.FIRE;
        for (String log : AncientOak.LOGS) fire.setFlammable(AncientOak.block(log), 5, 5);
        for (String wood : new String[]{AncientOak.PLANKS, AncientOak.STAIRS, AncientOak.SLAB, AncientOak.FENCE, AncientOak.FENCE_GATE}) {
            fire.setFlammable(AncientOak.block(wood), 5, 20);
        }
        fire.setFlammable(AncientOak.block(AncientOak.LEAVES), 30, 60);
    }
}
