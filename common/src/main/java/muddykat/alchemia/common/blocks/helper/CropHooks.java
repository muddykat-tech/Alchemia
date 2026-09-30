package muddykat.alchemia.common.blocks.helper;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class CropHooks {

    private CropHooks() {
    }

    public static boolean canGrow(ServerLevel level, BlockPos pos, BlockState state, boolean chance) {
#if NEOFORGE
        return net.neoforged.neoforge.common.CommonHooks.canCropGrow(level, pos, state, chance);
#else
        return net.minecraftforge.common.ForgeHooks.onCropsGrowPre(level, pos, state, chance);
#endif
    }

    public static void grown(ServerLevel level, BlockPos pos, BlockState state) {
#if NEOFORGE
        net.neoforged.neoforge.common.CommonHooks.fireCropGrowPost(level, pos, state);
#else
        net.minecraftforge.common.ForgeHooks.onCropsGrowPost(level, pos, state);
#endif
    }

    public static boolean isWaterSource(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.WATER) && level.getFluidState(pos).isSource();
    }
}
