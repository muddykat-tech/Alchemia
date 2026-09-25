package muddykat.alchemia.common.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;

public class BlockGeneric extends Block {
#if MC_VER < 26300
    public static final MapCodec<BlockGeneric> CODEC = simpleCodec(BlockGeneric::new);
#endif

    public BlockGeneric(Properties properties) {
        super(properties);
    }

#if MC_VER < 26300
    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }
#endif
}
