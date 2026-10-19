package muddykat.alchemia.common.blocks;

import muddykat.alchemia.common.blocks.helper.CropHooks;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
#if MC_VER >= 26102
import net.minecraft.world.level.ScheduledTickAccess;
#else
import net.minecraft.world.InteractionHand;
#endif

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockIngredientPatch extends BlockIngredient implements WildGrowth, SimpleWaterloggedBlock {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final Direction[] FACES = {Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    private static final Map<Direction, BooleanProperty> FACE_PROPERTIES = new EnumMap<>(Direction.class);

    static {
        FACE_PROPERTIES.put(Direction.DOWN, BlockStateProperties.DOWN);
        FACE_PROPERTIES.put(Direction.NORTH, BlockStateProperties.NORTH);
        FACE_PROPERTIES.put(Direction.EAST, BlockStateProperties.EAST);
        FACE_PROPERTIES.put(Direction.SOUTH, BlockStateProperties.SOUTH);
        FACE_PROPERTIES.put(Direction.WEST, BlockStateProperties.WEST);
    }

    public static final int GREENERY_AGE = 2;
    public static final int GROWTH_CHANCE = 6;
    public static final int BLOOM_CHANCE = 12;
    public static final int SPREAD_CHANCE = 3;
    public static final int SPREAD_ATTEMPTS = 4;
    public static final int WATER_REACH = 3;
    public static final int WATER_DROP = 2;
    public static final int MAX_SUBMERSION = 2;
    public static final int BONEMEAL_REACH = 2;
    public static final int BLOOM_CLEARING = 2;
    public static final int MIN_CLEARING_GREENERY = 8;
    public static final int CROWD_RADIUS = 4;
    public static final int MAX_CROWD = 32;
    public static final int WILD_WALL_CHANCE = 3;
    public static final int MIN_BLOOM_LIGHT = 9;
    public static final int MIN_PICKED = 1;
    public static final int MAX_PICKED = 2;

    private static final double[] HEIGHT_BY_AGE = {4.0D, 4.0D, 5.0D, 7.0D};
    private static final double[] INSET_BY_AGE = {2.0D, 0.0D, 0.0D, 1.0D};

    private final Map<BlockState, VoxelShape> shapes = new HashMap<>();

    public BlockIngredientPatch(Ingredients ingredient, IngredientType type, Properties properties) {
        super(ingredient, type, properties);
        BlockState state = this.defaultBlockState().setValue(WATERLOGGED, false);
        for (Direction face : FACES) state = state.setValue(faceProperty(face), face == Direction.DOWN);
        this.registerDefaultState(state);

        for (BlockState possible : this.getStateDefinition().getPossibleStates()) {
            shapes.put(possible, shapeOf(possible));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WATERLOGGED);
        for (Direction face : FACES) builder.add(faceProperty(face));
    }

    public static BooleanProperty faceProperty(Direction face) {
        return FACE_PROPERTIES.get(face);
    }

    public static boolean hasFace(BlockState state, Direction face) {
        return state.getValue(faceProperty(face));
    }

    private static boolean hasAnyFace(BlockState state) {
        for (Direction face : FACES) {
            if (hasFace(state, face)) return true;
        }
        return false;
    }

    private BlockState bare(boolean waterlogged) {
        BlockState state = defaultBlockState().setValue(WATERLOGGED, waterlogged);
        for (Direction face : FACES) state = state.setValue(faceProperty(face), false);
        return state;
    }

    public boolean isBlooming(BlockState state) {
        return isMaxAge(state);
    }

    private VoxelShape shapeOf(BlockState state) {
        int age = getAge(state);
        double height = HEIGHT_BY_AGE[age];
        double inset = INSET_BY_AGE[age];
        double far = 16.0D - inset;

        VoxelShape shape = Shapes.empty();
        for (Direction face : FACES) {
            if (!hasFace(state, face)) continue;
            VoxelShape part = switch (face) {
                case NORTH -> Block.box(inset, inset, 0.0D, far, far, height);
                case SOUTH -> Block.box(inset, inset, 16.0D - height, far, far, 16.0D);
                case WEST -> Block.box(0.0D, inset, inset, height, far, far);
                case EAST -> Block.box(16.0D - height, inset, inset, 16.0D, far, far);
                default -> Block.box(inset, 0.0D, inset, far, height, far);
            };
            shape = Shapes.or(shape, part);
        }
        return shape;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.getOrDefault(state, Shapes.empty());
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return isBankSoil(state);
    }

    public static boolean isBankSoil(BlockState state) {
#if MC_VER >= 26102
        if (state.is(BlockTags.SUPPORTS_VEGETATION)) return true;
#endif
        return state.is(BlockTags.DIRT) || state.is(BlockTags.SAND) || state.is(Blocks.GRAVEL) || state.is(Blocks.CLAY)
                || state.is(Blocks.COBBLESTONE) || state.is(Blocks.MOSSY_COBBLESTONE);
    }

    public static boolean canAttach(BlockGetter level, BlockPos pos, Direction face) {
        BlockPos support = pos.relative(face);
        BlockState supportState = level.getBlockState(support);
        if (!isBankSoil(supportState)) return false;
        return face == Direction.DOWN || supportState.isFaceSturdy(level, support, face.getOpposite());
    }

    private static boolean hasWaterNearby(BlockGetter level, BlockPos pos) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = -WATER_DROP; dy <= 0; dy++) {
            for (int dx = -WATER_REACH; dx <= WATER_REACH; dx++) {
                for (int dz = -WATER_REACH; dz <= WATER_REACH; dz++) {
                    cursor.setWithOffset(pos, dx, dy, dz);
                    if (level.getFluidState(cursor).is(FluidTags.WATER)) return true;
                }
            }
        }
        return false;
    }

    private static boolean isWaterSource(BlockGetter level, BlockPos pos) {
        return level.getFluidState(pos).getType() == Fluids.WATER;
    }

    private static boolean tooDeep(BlockGetter level, BlockPos pos) {
        for (int above = 1; above <= MAX_SUBMERSION; above++) {
            if (!level.getFluidState(pos.above(above)).is(FluidTags.WATER)) return false;
        }
        return true;
    }

    private static boolean isWet(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(WATERLOGGED) ? !tooDeep(level, pos) : hasWaterNearby(level, pos);
    }

    private static boolean couldHold(BlockGetter level, BlockPos pos) {
        for (Direction face : FACES) {
            if (canAttach(level, pos, face)) return hasWaterNearby(level, pos);
        }
        return false;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (!state.is(this)) return couldHold(level, pos);
        if (!hasAnyFace(state)) return false;
        for (Direction face : FACES) {
            if (hasFace(state, face) && !canAttach(level, pos, face)) return false;
        }
        return isWet(state, level, pos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState existing = level.getBlockState(pos);
        BlockState base = existing.is(this) ? existing : bare(isWaterSource(level, pos));

        for (Direction looking : context.getNearestLookingDirections()) {
            if (!faceAllowed(looking) || hasFace(base, looking) || !canAttach(level, pos, looking)) continue;
            return base.setValue(faceProperty(looking), true);
        }
        return null;
    }

    private static boolean faceAllowed(Direction face) {
        return face != Direction.UP;
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        if (!context.getItemInHand().is(this.asItem())) return super.canBeReplaced(state, context);
        for (Direction face : FACES) {
            if (!hasFace(state, face) && canAttach(context.getLevel(), context.getClickedPos(), face)) return true;
        }
        return false;
    }

    private BlockState reshape(BlockState state, LevelReader level, BlockPos pos) {
        BlockState kept = state;
        for (Direction face : FACES) {
            if (hasFace(kept, face) && !canAttach(level, pos, face)) kept = kept.setValue(faceProperty(face), false);
        }
        if (hasAnyFace(kept)) return kept;
        return kept.getValue(WATERLOGGED) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
    }

#if MC_VER >= 26102
    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
                                     BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (state.getValue(WATERLOGGED)) ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        return reshape(state, level, pos);
    }
#else
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor level,
                                  BlockPos pos, BlockPos neighbourPos) {
        if (state.getValue(WATERLOGGED)) level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        return reshape(state, level, pos);
    }
#endif

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluid) {
        BlockState wilted = isBlooming(state) ? state.setValue(getAgeProperty(), GREENERY_AGE) : state;
        return SimpleWaterloggedBlock.super.placeLiquid(level, pos, wilted, fluid);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isAreaLoaded(pos, CROWD_RADIUS)) return;

        if (!isWet(state, level, pos)) {
            level.removeBlock(pos, false);
            return;
        }

        tend(state, level, pos, random);

        BlockState now = level.getBlockState(pos);
        if (now.is(this) && AncientOak.nurtures(level, pos)) tend(now, level, pos, random);
    }

    private void tend(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = getAge(state);
        if (age < GREENERY_AGE) {
            if (CropHooks.canGrow(level, pos, state, random.nextInt(GROWTH_CHANCE) == 0)) {
                level.setBlock(pos, state.setValue(getAgeProperty(), age + 1), 2);
                CropHooks.grown(level, pos, state);
            }
            return;
        }

        if (random.nextInt(SPREAD_CHANCE) == 0) spread(level, pos, random);

        if (random.nextInt(BLOOM_CHANCE) == 0 && level.getRawBrightness(pos, 0) >= MIN_BLOOM_LIGHT && mayBloom(state, level, pos)) {
            level.setBlock(pos, state.setValue(getAgeProperty(), getMaxAge()), 2);
        }
    }

    private boolean isGreenery(BlockState state) {
        return state.is(this) && getAge(state) == GREENERY_AGE;
    }

    private boolean mayBloom(BlockState state, LevelAccessor level, BlockPos pos) {
        return getAge(state) == GREENERY_AGE && !state.getValue(WATERLOGGED) && hasClearing(level, pos);
    }

    private boolean hasClearing(LevelAccessor level, BlockPos pos) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int greenery = 0;

        for (int dx = -BLOOM_CLEARING; dx <= BLOOM_CLEARING; dx++) {
            for (int dz = -BLOOM_CLEARING; dz <= BLOOM_CLEARING; dz++) {
                if (dx == 0 && dz == 0) continue;

                boolean covered = false;
                boolean open = false;
                for (int dy = -1; dy <= 1; dy++) {
                    BlockState nearby = level.getBlockState(cursor.setWithOffset(pos, dx, dy, dz));
                    if (isGreenery(nearby)) covered = true;
                    else if (nearby.is(this) || (nearby.isAir() && couldHold(level, cursor.immutable()))) open = true;
                }

                if (covered) greenery++;
                else if (open) return false;
            }
        }
        return greenery >= MIN_CLEARING_GREENERY;
    }

    private int crowd(LevelAccessor level, BlockPos pos) {
        int count = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -CROWD_RADIUS; dx <= CROWD_RADIUS; dx++) {
                for (int dz = -CROWD_RADIUS; dz <= CROWD_RADIUS; dz++) {
                    if (level.getBlockState(cursor.setWithOffset(pos, dx, dy, dz)).is(this)) count++;
                }
            }
        }
        return count;
    }

    private boolean spread(ServerLevel level, BlockPos pos, RandomSource random) {
        if (crowd(level, pos) >= MAX_CROWD) return false;

        for (int attempt = 0; attempt < SPREAD_ATTEMPTS; attempt++) {
            BlockPos target = pos.offset(random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1);
            Direction face = FACES[random.nextInt(FACES.length)];
            BlockState there = level.getBlockState(target);

            if (there.is(this)) {
                if (hasFace(there, face) || !canAttach(level, target, face)) continue;
                level.setBlock(target, there.setValue(faceProperty(face), true), 2);
                return true;
            }

            boolean submerged = there.is(Blocks.WATER) && isWaterSource(level, target);
            if (!there.isAir() && !submerged) continue;
            if (!canAttach(level, target, face) || (submerged ? tooDeep(level, target) : !hasWaterNearby(level, target))) continue;

            level.setBlock(target, bare(submerged).setValue(faceProperty(face), true), 2);
            return true;
        }
        return false;
    }

    private InteractionResult pick(BlockState state, Level level, BlockPos pos, Player player) {
        if (!isBlooming(state)) return InteractionResult.PASS;

        if (!level.isClientSide()) {
            int count = MIN_PICKED + level.getRandom().nextInt(MAX_PICKED - MIN_PICKED + 1);
            popResource(level, pos, new ItemStack(ItemRegistry.getItemFromRegistry(ingredientData.getRegistryName()), count));
            if (level.getRandom().nextBoolean()) popResource(level, pos, new ItemStack(ItemRegistry.getSeedByIngredient(ingredientData)));
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS,
                    1.0F, 0.8F + level.getRandom().nextFloat() * 0.4F);

            BlockState picked = state.setValue(getAgeProperty(), GREENERY_AGE);
            level.setBlock(pos, picked, 2);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, picked));
        }

#if MC_VER >= 26102
        return InteractionResult.SUCCESS;
#else
        return InteractionResult.sidedSuccess(level.isClientSide());
#endif
    }

#if MC_VER >= 26102
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        InteractionResult result = pick(state, level, pos, player);
        return result == InteractionResult.PASS ? super.useWithoutItem(state, level, pos, player, hitResult) : result;
    }
#else
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        InteractionResult result = pick(state, level, pos, player);
        return result == InteractionResult.PASS ? super.use(state, level, pos, player, hand, hitResult) : result;
    }
#endif

    private void fertilise(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int age = getAge(state);
        if (age < GREENERY_AGE) {
            level.setBlock(pos, state.setValue(getAgeProperty(), age + 1), 2);
        } else if (mayBloom(state, level, pos)) {
            level.setBlock(pos, state.setValue(getAgeProperty(), getMaxAge()), 2);
        } else {
            spread(level, pos, random);
        }
    }

    private void fertilisePatch(ServerLevel level, RandomSource random, BlockPos origin) {
        List<BlockPos> patch = new ArrayList<>();
        for (BlockPos nearby : BlockPos.betweenClosed(origin.offset(-BONEMEAL_REACH, -1, -BONEMEAL_REACH),
                origin.offset(BONEMEAL_REACH, 1, BONEMEAL_REACH))) {
            if (!nearby.equals(origin) && level.getBlockState(nearby).is(this)) patch.add(nearby.immutable());
        }
        Collections.shuffle(patch, new java.util.Random(random.nextLong()));
        patch.add(0, origin);

        for (BlockPos pos : patch) {
            BlockState state = level.getBlockState(pos);
            if (state.is(this)) fertilise(level, random, pos, state);
        }
    }

#if MC_VER >= 26300
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, net.minecraft.world.level.block.BonemealSource source) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, net.minecraft.world.level.block.BonemealSource source) {
        fertilisePatch(level, random, pos);
    }
#elif MC_VER >= 26102
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        fertilisePatch(level, random, pos);
    }
#else
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        fertilisePatch(level, random, pos);
    }
#endif

    @Override
    public boolean growWild(WorldGenLevel level, BlockPos pos, BlockState plant, RandomSource random) {
        BlockState state = bare(isWaterSource(level, pos));
        for (Direction face : FACES) {
            if (!canAttach(level, pos, face)) continue;
            if (face == Direction.DOWN || random.nextInt(WILD_WALL_CHANCE) == 0) state = state.setValue(faceProperty(face), true);
        }
        if (!hasAnyFace(state) || !isWet(state, level, pos)) return false;

        state = state.setValue(getAgeProperty(), random.nextInt(4) == 0 ? 1 : GREENERY_AGE);
        if (random.nextInt(6) == 0 && mayBloom(state, level, pos)) state = state.setValue(getAgeProperty(), getMaxAge());

        level.setBlock(pos, state, 2);
        return true;
    }
}
