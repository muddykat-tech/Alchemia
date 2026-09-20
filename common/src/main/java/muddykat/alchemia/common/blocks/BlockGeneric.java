package muddykat.alchemia.common.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;

public class BlockGeneric extends Block {
    public static final MapCodec<BlockGeneric> CODEC = simpleCodec(BlockGeneric::new);

    public BlockGeneric(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }
}
