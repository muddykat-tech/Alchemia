package muddykat.alchemia.common.blocks;

import muddykat.alchemia.common.blocks.helper.CropHooks;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockIngredientShelf extends BlockIngredient {

    public static final int GROWTH_CHANCE = 5;

    private final VoxelShape[][] shapes = new VoxelShape[4][];

    public BlockIngredientShelf(Ingredients ingredient, IngredientType type, Properties properties) {
        super(ingredient, type, properties);
        this.registerDefaultState(this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));

        for (Direction facing : Direction.Plane.HORIZONTAL) {
            VoxelShape[] byAge = new VoxelShape[getMaxAge() + 1];
            for (int age = 0; age <= getMaxAge(); age++) byAge[age] = shelfShape(facing, age);
            shapes[facing.get2DDataValue()] = byAge;
        }
    }

    private static VoxelShape shelfShape(Direction facing, int age) {
        double depth = 4.0D + age * 3.0D;
        double half = 3.0D + age;
        double bottom = 6.0D;
        double top = 10.0D;

        return switch (facing) {
            case SOUTH -> Block.box(8.0D - half, bottom, 0.0D, 8.0D + half, top, depth);
            case WEST -> Block.box(16.0D - depth, bottom, 8.0D - half, 16.0D, top, 8.0D + half);
            case EAST -> Block.box(0.0D, bottom, 8.0D - half, depth, top, 8.0D + half);
            default -> Block.box(8.0D - half, bottom, 16.0D - depth, 8.0D + half, top, 16.0D);
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    public BlockState facing(Direction side) {
        return defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, side);
    }

    public static Direction getFacing(BlockState state) {
        return state.getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes[getFacing(state).get2DDataValue()][getAge(state)];
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        return face.getAxis().isHorizontal() ? facing(face) : null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) ? getFacing(state) : Direction.NORTH;
        Block host = level.getBlockState(pos.relative(facing.getOpposite())).getBlock();
        return host instanceof BlockInfectedLog log && log.getFungus() == ingredientData;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return !isMaxAge(state);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (isMaxAge(state) || !level.isAreaLoaded(pos, 1)) return;

        if (CropHooks.canGrow(level, pos, state, random.nextInt(GROWTH_CHANCE) == 0)) {
            level.setBlock(pos, state.setValue(getAgeProperty(), getAge(state) + 1), 2);
            CropHooks.grown(level, pos, state);
        }
    }
}
