package muddykat.alchemia.common.blocks;

import muddykat.alchemia.common.items.helper.Ingredients;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class BlockMineralBuddingGeneric extends BlockGeneric {

    private static final Direction[] DIRECTIONS = Direction.values();
    private final Ingredients ingredient;
    private final List<? extends AmethystClusterBlock> clusters;

    public BlockMineralBuddingGeneric(Ingredients ingredient, List<? extends AmethystClusterBlock> clusters) {
        super(Properties.copy(Blocks.BUDDING_AMETHYST));
        assert !clusters.isEmpty();
        this.clusters = clusters;
        this.ingredient = ingredient;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    public static boolean canGrowIn(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) && state.getFluidState().getAmount() == 8;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, Random random) {
        if (random.nextInt(5) == 0) {
            Direction direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
            BlockPos blockPos = pos.offset(direction.getNormal());
            BlockState blockState = world.getBlockState(blockPos);
            Block blockStateBlock = blockState.getBlock();
            AmethystClusterBlock nextBlock = null;

            if (canGrowIn(blockState)) {
                nextBlock = clusters.get(0);
            } else if (blockStateBlock instanceof AmethystClusterBlock clusterBlock && blockState.getValue(AmethystClusterBlock.FACING) == direction) {
                if (clusters.contains(clusterBlock)) {
                    int nextBlockIndex = clusters.indexOf(clusterBlock) + 1;
                    if (nextBlockIndex < clusters.size()) {
                        nextBlock = clusters.get(nextBlockIndex);
                    }
                }
            }

            if (nextBlock != null) {
                BlockState toSet = nextBlock.defaultBlockState()
                        .setValue(AmethystClusterBlock.FACING, direction)
                        .setValue(AmethystClusterBlock.WATERLOGGED, blockState.getFluidState().getType() == Fluids.WATER);
                world.setBlockAndUpdate(blockPos, toSet);
            }
        }
    }

    public List<Block> getClusters() {
        return Collections.unmodifiableList(clusters);
    }

    public List<BlockState> getClusterStates() {
        return this.getClusters().stream()
                .map((Block::defaultBlockState))
                .collect(Collectors.toList());
    }

    public Ingredients getIngredient() {
        return ingredient;
    }
}
