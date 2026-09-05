package muddykat.alchemia.common.blocks;

import muddykat.alchemia.common.items.helper.Ingredients;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BlockMineralBuddingGeneric extends BlockGeneric {

    private static final Direction[] DIRECTIONS = Direction.values();
    private final Ingredients ingredient;
    private final List<Supplier<? extends AmethystClusterBlock>> clusters;

    public BlockMineralBuddingGeneric(Ingredients ingredient, List<Supplier<? extends AmethystClusterBlock>> clusters, Properties properties) {
        super(properties);
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
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) != 0) return;

        Direction direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
        BlockPos blockPos = pos.offset(direction.getUnitVec3i());
        BlockState blockState = level.getBlockState(blockPos);
        List<Block> resolved = getClusters();
        Block nextBlock = null;

        if (canGrowIn(blockState)) {
            nextBlock = resolved.get(0);
        } else if (blockState.getBlock() instanceof AmethystClusterBlock clusterBlock && blockState.getValue(AmethystClusterBlock.FACING) == direction) {
            int nextBlockIndex = resolved.indexOf(clusterBlock) + 1;
            if (nextBlockIndex > 0 && nextBlockIndex < resolved.size()) {
                nextBlock = resolved.get(nextBlockIndex);
            }
        }

        if (nextBlock != null) {
            BlockState toSet = nextBlock.defaultBlockState()
                    .setValue(AmethystClusterBlock.FACING, direction)
                    .setValue(AmethystClusterBlock.WATERLOGGED, blockState.getFluidState().getType() == Fluids.WATER);
            level.setBlockAndUpdate(blockPos, toSet);
        }
    }

    public List<Block> getClusters() {
        return clusters.stream().map(supplier -> (Block) supplier.get()).collect(Collectors.toList());
    }

    public List<BlockState> getClusterStates() {
        return getClusters().stream().map(Block::defaultBlockState).collect(Collectors.toList());
    }

    public Ingredients getIngredient() {
        return ingredient;
    }
}
