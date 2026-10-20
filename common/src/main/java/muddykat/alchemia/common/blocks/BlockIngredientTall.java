package muddykat.alchemia.common.blocks;

import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockIngredientTall extends BlockIngredient implements WildGrowth {

    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final int UPPER_FROM_AGE = 2;

    private static final VoxelShape[] LOWER_SHAPE_BY_AGE = new VoxelShape[]{
            Block.box(3.0D, 0.0D, 3.0D, 13.0D, 6.0D, 13.0D),
            Block.box(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D),
            Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D),
            Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D)};

    private static final VoxelShape[] UPPER_SHAPE_BY_AGE = new VoxelShape[]{
            Block.box(2.0D, 0.0D, 2.0D, 14.0D, 6.0D, 14.0D),
            Block.box(2.0D, 0.0D, 2.0D, 14.0D, 6.0D, 14.0D),
            Block.box(1.0D, 0.0D, 1.0D, 15.0D, 10.0D, 15.0D),
            Block.box(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D)};

    public BlockIngredientTall(Ingredients ingredient, IngredientType type, Properties properties) {
        super(ingredient, type, properties);
        this.registerDefaultState(this.defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HALF);
    }

    public static boolean isLower(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER;
    }

    public boolean hasUpper(BlockState state) {
        return getAge(state) >= UPPER_FROM_AGE;
    }

    protected boolean isHalf(BlockState state, DoubleBlockHalf half) {
        return state.is(this) && state.getValue(HALF) == half;
    }

    protected BlockState shareGrowth(BlockState from, BlockState to) {
        return to.setValue(getAgeProperty(), from.getValue(getAgeProperty()));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int stage = getAge(state) * (LOWER_SHAPE_BY_AGE.length - 1) / getMaxAge();
        return isLower(state) ? LOWER_SHAPE_BY_AGE[stage] : UPPER_SHAPE_BY_AGE[stage];
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (!state.is(this)) return super.canSurvive(state, level, pos);
        if (!isLower(state)) return isHalf(level.getBlockState(pos.below()), DoubleBlockHalf.LOWER);
        if (!super.canSurvive(state, level, pos)) return false;
        return !hasUpper(state) || isHalf(level.getBlockState(pos.above()), DoubleBlockHalf.UPPER);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return isLower(state) && super.isRandomlyTicking(state);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!isLower(state)) return;
        if (getAge(state) + 1 >= UPPER_FROM_AGE && !hasUpper(state) && !level.isEmptyBlock(pos.above())) return;
        super.randomTick(state, level, pos, random);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!state.is(this)) return;

        if (isLower(state)) {
            if (hasUpper(state)) raiseUpper(level, pos, state);
            return;
        }

        BlockPos below = pos.below();
        BlockState lower = level.getBlockState(below);
        if (!isHalf(lower, DoubleBlockHalf.LOWER)) return;

        BlockState synced = shareGrowth(state, lower);
        if (synced != lower) level.setBlock(below, synced, 2);
    }

    private void raiseUpper(Level level, BlockPos pos, BlockState lower) {
        BlockPos above = pos.above();
        BlockState current = level.getBlockState(above);
        BlockState upper = lower.setValue(HALF, DoubleBlockHalf.UPPER);

        if (isHalf(current, DoubleBlockHalf.UPPER)) {
            BlockState synced = shareGrowth(lower, current);
            if (synced != current) level.setBlock(above, synced, 2);
        } else if (current.isAir()) {
            level.setBlock(above, upper, 3);
        } else {
            level.setBlock(pos, lower.setValue(getAgeProperty(), UPPER_FROM_AGE - 1), 2);
        }
    }

#if MC_VER >= 12100
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        keepLowerFromDropping(level, pos, state, player);
        return super.playerWillDestroy(level, pos, state, player);
    }
#else
    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        keepLowerFromDropping(level, pos, state, player);
        super.playerWillDestroy(level, pos, state, player);
    }
#endif

    private void keepLowerFromDropping(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.isClientSide() || !player.isCreative() || isLower(state)) return;

        BlockPos below = pos.below();
        if (isHalf(level.getBlockState(below), DoubleBlockHalf.LOWER)) {
            level.setBlock(below, Blocks.AIR.defaultBlockState(), 35);
        }
    }

    protected BlockState wildState(BlockState plant, int age, RandomSource random) {
        return plant.setValue(getAgeProperty(), age).setValue(HALF, DoubleBlockHalf.LOWER);
    }

    @Override
    public boolean growWild(WorldGenLevel level, BlockPos pos, BlockState plant, RandomSource random) {
        BlockState seedling = plant.setValue(getAgeProperty(), 0).setValue(HALF, DoubleBlockHalf.LOWER);
        if (!seedling.canSurvive(level, pos)) return false;

        int age = random.nextInt(getMaxAge() + 1);
        if (!level.isEmptyBlock(pos.above())) age = Math.min(age, UPPER_FROM_AGE - 1);

        BlockState lower = wildState(plant, age, random);
        level.setBlock(pos, lower, 2);
        if (hasUpper(lower)) level.setBlock(pos.above(), lower.setValue(HALF, DoubleBlockHalf.UPPER), 2);
        return true;
    }
}
