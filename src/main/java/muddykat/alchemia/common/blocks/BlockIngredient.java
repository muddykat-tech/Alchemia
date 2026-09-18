package muddykat.alchemia.common.blocks;

import com.mojang.serialization.MapCodec;
import muddykat.alchemia.common.items.helper.IngredientAlignment;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.world.IngredientHabitat;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.CommonHooks;

public class BlockIngredient extends CropBlock {

    public static final MapCodec<BlockIngredient> CODEC = simpleCodec(properties -> new BlockIngredient(Ingredients.Firebell, IngredientType.Flower, properties));

    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

    private static final VoxelShape[] ROOT_SHAPE_BY_AGE = new VoxelShape[]{
            Block.box(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D),
            Block.box(0.0D, 4.0D, 0.0D, 16.0D, 16.0D, 16.0D),
            Block.box(0.0D, 1.0D, 0.0D, 16.0D, 16.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D)};

    protected final Ingredients ingredientData;
    protected final IngredientType type;

    public BlockIngredient(Ingredients ingredient, IngredientType iType, Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(AGE, 0));
        this.ingredientData = ingredient;
        this.type = iType;
    }

    @Override
    public MapCodec<? extends CropBlock> codec() {
        return CODEC;
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return 3;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return !isMaxAge(state);
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return ingredientData.getPrimaryAlignment() != IngredientAlignment.Fire;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    protected int getBonemealAgeIncrease(Level level) {
        return Mth.nextInt(level.getRandom(), 1, 4);
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int ageGrowth = Math.min(this.getAge(state) + this.getBonemealAgeIncrease(level), getMaxAge());
        level.setBlockAndUpdate(pos, state.setValue(AGE, ageGrowth));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    public Ingredients getIngredient() {
        return ingredientData;
    }

    public IngredientType getIngredientType() {
        return type;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return IngredientHabitat.of(ingredientData) == IngredientHabitat.CEILING
                ? ROOT_SHAPE_BY_AGE[this.getAge(state)]
                : super.getShape(state, level, pos, context);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return switch (IngredientHabitat.of(ingredientData)) {
            case SURFACE -> state.is(BlockTags.SUPPORTS_VEGETATION);
            case SAND -> state.is(BlockTags.SAND);
            case AQUATIC -> isSeabed(state);
            case UNDERGROUND -> state.is(BlockTags.BASE_STONE_OVERWORLD);
            case SULFUR -> isSulfur(state);
            case CEILING -> hasRootSupport(level, pos.above());
            case GEODE -> state.is(BlockTags.BASE_STONE_OVERWORLD);
        };
    }

    private static boolean isSeabed(BlockState state) {
        return state.is(BlockTags.SAND) || state.is(BlockTags.DIRT) || state.is(Blocks.GRAVEL) || state.is(Blocks.CLAY);
    }

    private static boolean isSulfur(BlockState state) {
        return state.is(Blocks.MAGMA_BLOCK) || state.is(Blocks.BASALT);
    }

    public static boolean isRootCeiling(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(BlockTags.BASE_STONE_OVERWORLD);
    }

    private boolean hasRootSupport(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos.below()).isAir() && isRootCeiling(level.getBlockState(pos.above()));
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();

        if (state.getBlock() == this) {
            IngredientHabitat habitat = IngredientHabitat.of(ingredientData);
            BlockState soil = level.getBlockState(below);

            return switch (habitat) {
                case CEILING -> hasRootSupport(level, pos);
                case AQUATIC -> isSeabed(soil) && level.getFluidState(pos).is(FluidTags.WATER);
                case SURFACE -> soil.canSustainPlant(level, below, Direction.UP, state)
                        .toBoolean(soil.is(BlockTags.SUPPORTS_VEGETATION));
                default -> this.mayPlaceOn(soil, level, below);
            };
        }

        return this.mayPlaceOn(level.getBlockState(below), level, below);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!IngredientHabitat.of(ingredientData).isUnderground()) {
            super.randomTick(state, level, pos, random);
            return;
        }

        if (!level.isAreaLoaded(pos, 1)) return;

        int age = this.getAge(state);
        if (age >= this.getMaxAge()) return;

        if (CommonHooks.canCropGrow(level, pos, state, random.nextInt(12) == 0)) {
            level.setBlock(pos, state.setValue(AGE, age + 1), 2);
            CommonHooks.fireCropGrowPost(level, pos, state);
        }
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return IngredientHabitat.of(ingredientData) == IngredientHabitat.AQUATIC
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(state);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ItemRegistry.getSeedByIngredient(getIngredient());
    }
}
