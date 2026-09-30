package muddykat.alchemia.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

public interface WildGrowth {
    boolean growWild(WorldGenLevel level, BlockPos pos, BlockState plant, RandomSource random);
}
