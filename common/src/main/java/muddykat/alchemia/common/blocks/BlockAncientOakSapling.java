package muddykat.alchemia.common.blocks;

import muddykat.alchemia.common.world.AncientOakShape;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
#if MC_VER >= 26102
import net.minecraft.world.level.block.grower.TreeGrower;
#else
import net.minecraft.world.level.block.grower.OakTreeGrower;
#endif

public class BlockAncientOakSapling extends SaplingBlock {

    public BlockAncientOakSapling(Properties properties) {
#if MC_VER >= 26102
        super(TreeGrower.OAK, properties);
#else
        super(new OakTreeGrower(), properties);
#endif
    }

    @Override
    public void advanceTree(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        if (state.getValue(STAGE) == 0) {
            level.setBlock(pos, state.cycle(STAGE), 4);
            return;
        }
        AncientOakShape.growFromSapling(level, pos, state, random);
    }
}
