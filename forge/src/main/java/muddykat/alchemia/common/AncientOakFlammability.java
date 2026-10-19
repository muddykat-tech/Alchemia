package muddykat.alchemia.common;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.AncientOak;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;

import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Method;

public final class AncientOakFlammability {

    private AncientOakFlammability() {
    }

    public static void register() {
        try {
            Method setFlammable = ObfuscationReflectionHelper.findMethod(FireBlock.class, "m_53444_", Block.class, int.class, int.class);
            setFlammable.setAccessible(true);
            FireBlock fire = (FireBlock) Blocks.FIRE;

            for (String log : AncientOak.LOGS) setFlammable.invoke(fire, AncientOak.block(log), 5, 5);
            for (String wood : new String[]{AncientOak.PLANKS, AncientOak.STAIRS, AncientOak.SLAB, AncientOak.FENCE, AncientOak.FENCE_GATE}) {
                setFlammable.invoke(fire, AncientOak.block(wood), 5, 20);
            }
            setFlammable.invoke(fire, AncientOak.block(AncientOak.LEAVES), 30, 60);
        } catch (ReflectiveOperationException error) {
            Alchemia.LOGGER.warn("Could not make ancient oak flammable", error);
        }
    }
}
