package muddykat.alchemia.common.blocks;

import muddykat.alchemia.registration.registers.ParticleTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
#if MC_VER >= 26102
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
#else
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
#endif

public class BlockAncientOakLeaves extends
#if MC_VER >= 26102
        TintedParticleLeavesBlock {
#else
        LeavesBlock {
#endif

    public static final float LEAF_FALL_CHANCE = 0.02F;

    public BlockAncientOakLeaves(Properties properties) {
#if MC_VER >= 26102
        super(LEAF_FALL_CHANCE, properties);
#else
        super(properties);
#endif
    }

#if MC_VER >= 26102
    @Override
    protected void spawnFallingLeavesParticle(Level level, BlockPos pos, RandomSource random) {
        dropLeaf(level, pos, random, level.getClientLeafTintColor(pos));
    }
#else
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextFloat() >= LEAF_FALL_CHANCE) return;

        BlockPos below = pos.below();
        if (isFaceFull(level.getBlockState(below).getCollisionShape(level, below), Direction.UP)) return;
        dropLeaf(level, pos, random, BiomeColors.getAverageFoliageColor(level, pos));
    }
#endif

    private static void dropLeaf(Level level, BlockPos pos, RandomSource random, int tint) {
        level.addParticle(ParticleTypeRegistry.ANCIENT_OAK_LEAF.get(),
                pos.getX() + random.nextDouble(), pos.getY() - 0.05, pos.getZ() + random.nextDouble(),
                ((tint >> 16) & 255) / 255.0, ((tint >> 8) & 255) / 255.0, (tint & 255) / 255.0);
    }
}
