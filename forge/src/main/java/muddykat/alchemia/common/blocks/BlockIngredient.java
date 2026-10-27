package muddykat.alchemia.common.blocks;

import muddykat.alchemia.common.blocks.helper.GrowthClimate;
import muddykat.alchemia.common.items.helper.IngredientAlignment;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.world.IngredientHabitat;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockIngredient extends CropBlock {

    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    public static final int NURTURE_PARTICLE_CHANCE = 24;

    private static final VoxelShape[] ROOT_SHAPE_BY_AGE = new VoxelShape[]{
            Block.box(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D),
            Block.box(0.0D, 4.0D, 0.0D, 16.0D, 16.0D, 16.0D),
            Block.box(0.0D, 1.0D, 0.0D, 16.0D, 16.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D)};

    protected final Ingredients ingredientData;
    protected final IngredientType type;

    public BlockIngredient(Ingredients ingredient, IngredientType iType, Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(getAgeProperty(), 0));
        this.ingredientData = ingredient;
        this.type = iType;
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
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return !isMaxAge(state) && GrowthClimate.allows(ingredientData, level, pos);
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
        level.setBlockAndUpdate(pos, state.setValue(getAgeProperty(), ageGrowth));
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
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = IngredientHabitat.of(ingredientData) == IngredientHabitat.CEILING
                ? ROOT_SHAPE_BY_AGE[this.getAge(state) * (ROOT_SHAPE_BY_AGE.length - 1) / this.getMaxAge()]
                : super.getShape(state, level, pos, context);
        Vec3 offset = state.getOffset(level, pos);
        return shape.move(offset.x, offset.y, offset.z);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return nativeSoil(state, level, pos) || nurturedSoil(state, level, pos);
    }

    private boolean isLandPlant() {
        return switch (IngredientHabitat.of(ingredientData)) {
            case SURFACE, NYLIUM, SAND, UNDERGROUND, MAGMA -> true;
            default -> false;
        };
    }

    private static boolean isAnySoil(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(BlockTags.SAND) || state.is(BlockTags.NYLIUM)
                || state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.BASE_STONE_NETHER)
                || state.is(Blocks.MAGMA_BLOCK);
    }

    private boolean nurturedSoil(BlockState soil, BlockGetter level, BlockPos soilPos) {
        return isLandPlant() && isAnySoil(soil) && AncientOak.nurtures(level, soilPos.above());
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!GrowthClimate.allows(ingredientData, level, pos)) return;
        super.randomTick(state, level, pos, random);

        BlockState now = level.getBlockState(pos);
        if (now.is(this) && AncientOak.nurtures(level, pos)) super.randomTick(now, level, pos, random);
    }

    private boolean nativeSoil(BlockState state, BlockGetter level, BlockPos pos) {
        return switch (IngredientHabitat.of(ingredientData)) {
            case SURFACE -> state.is(BlockTags.DIRT);
            case NYLIUM -> state.is(BlockTags.DIRT) || state.is(BlockTags.NYLIUM);
            case SAND -> state.is(BlockTags.SAND);
            case AQUATIC -> isSeabed(state);
            case UNDERGROUND -> state.is(BlockTags.BASE_STONE_OVERWORLD);
            case MAGMA -> isMagmaBed(state, level, pos);
            case TREE -> false;
            case CEILING -> hasRootSupport(level, pos.above());
            case GEODE -> state.is(BlockTags.BASE_STONE_OVERWORLD);
        };
    }

    private static boolean isSeabed(BlockState state) {
        return state.is(BlockTags.SAND) || state.is(BlockTags.DIRT) || state.is(Blocks.GRAVEL) || state.is(Blocks.CLAY);
    }

    private static final int MAGMA_HEAT_RADIUS = 4;

    private static boolean isMagmaBed(BlockState state, BlockGetter level, BlockPos pos) {
        if (state.is(Blocks.MAGMA_BLOCK)) return true;
        if (!state.is(BlockTags.BASE_STONE_OVERWORLD) && !state.is(BlockTags.BASE_STONE_NETHER)) return false;
        return hasHeatNearby(level, pos);
    }

    private static boolean hasHeatNearby(BlockGetter level, BlockPos pos) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int radius = 1; radius <= MAGMA_HEAT_RADIUS; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz))) != radius) continue;
                        cursor.setWithOffset(pos, dx, dy, dz);
                        BlockState nearby = level.getBlockState(cursor);
                        if (nearby.is(Blocks.MAGMA_BLOCK) || nearby.getFluidState().is(FluidTags.LAVA)) return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean hasRootSupport(BlockGetter level, BlockPos pos) {
        if (!level.getBlockState(pos.below()).isAir()) return false;
        BlockPos ceiling = pos.above();
        return level.getBlockState(ceiling).isFaceSturdy(level, ceiling, Direction.DOWN);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();

        if (state.getBlock() == this) {
            IngredientHabitat habitat = IngredientHabitat.of(ingredientData);
            BlockState soil = level.getBlockState(below);

            return switch (habitat) {
                case CEILING -> hasRootSupport(level, pos);
                case AQUATIC -> isSeabed(soil) && level.getFluidState(pos).is(FluidTags.WATER);
                case SURFACE -> soil.canSustainPlant(level, below, Direction.UP, this)
                        || soil.is(BlockTags.DIRT) || nurturedSoil(soil, level, below);
                default -> this.mayPlaceOn(soil, level, below);
            };
        }

        return this.mayPlaceOn(level.getBlockState(below), level, below);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return IngredientHabitat.of(ingredientData) == IngredientHabitat.AQUATIC
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(state);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(NURTURE_PARTICLE_CHANCE) != 0 || !isRandomlyTicking(state) || !AncientOak.nurtures(level, pos)) return;

        level.addParticle(ParticleTypes.HAPPY_VILLAGER,
                pos.getX() + 0.2 + random.nextDouble() * 0.6,
                pos.getY() + 0.2 + random.nextDouble() * 0.5,
                pos.getZ() + 0.2 + random.nextDouble() * 0.6,
                0.0, 0.02, 0.0);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ItemRegistry.getSeedByIngredient(getIngredient());
    }
}
