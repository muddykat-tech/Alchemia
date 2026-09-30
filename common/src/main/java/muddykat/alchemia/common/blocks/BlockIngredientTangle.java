package muddykat.alchemia.common.blocks;

import muddykat.alchemia.common.blocks.helper.CropHooks;
import muddykat.alchemia.common.blocks.helper.PlantForm;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

public class BlockIngredientTangle extends BlockIngredient implements WildGrowth {

    public static final int REACH = 4;
    public static final int MAX_BRANCH_NEIGHBOURS = 3;
    public static final int SPREAD_CHANCE = 3;
    public static final int WILD_SPREAD_ATTEMPTS = 24;

    public BlockIngredientTangle(Ingredients ingredient, IngredientType type, Properties properties) {
        super(ingredient, type, properties);
    }

    public BlockTangleBranch branch() {
        return (BlockTangleBranch) PlantForm.part(ingredientData, PlantForm.BRANCH);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!isMaxAge(state)) {
            super.randomTick(state, level, pos, random);
            return;
        }

        if (random.nextInt(SPREAD_CHANCE) == 0) spread(level, pos, random, 3);
    }

    public boolean spread(LevelAccessor level, BlockPos node, RandomSource random, int flags) {
        BlockTangleBranch branch = branch();
        BlockPos current = node;
        int distance = 0;

        while (distance < REACH) {
            BlockPos next = current.relative(Direction.getRandom(random));
            BlockState there = level.getBlockState(next);

            if (there.is(branch)) {
                if (there.getValue(BlockTangleBranch.DISTANCE) != distance + 1) return false;
                current = next;
                distance++;
                continue;
            }

            if (!canBranchInto(level, next)) return false;

            level.setBlock(next, branch.defaultBlockState().setValue(BlockTangleBranch.DISTANCE, distance + 1), flags);
            return true;
        }

        return false;
    }

    private boolean canBranchInto(BlockGetter level, BlockPos pos) {
        if (!CropHooks.isWaterSource(level, pos)) return false;

        int links = 0;
        for (Direction direction : Direction.values()) {
            BlockPos neighbour = pos.relative(direction);
            BlockState state = level.getBlockState(neighbour);
            if (!isNetwork(state)) continue;

            links++;
            if (state.is(branch()) && links(level, neighbour) >= MAX_BRANCH_NEIGHBOURS) return false;
        }

        return links <= MAX_BRANCH_NEIGHBOURS;
    }

    private int links(BlockGetter level, BlockPos pos) {
        int links = 0;
        for (Direction direction : Direction.values()) {
            if (isNetwork(level.getBlockState(pos.relative(direction)))) links++;
        }
        return links;
    }

    private boolean isNetwork(BlockState state) {
        return state.is(this) || state.is(branch());
    }

    @Override
    public boolean growWild(WorldGenLevel level, BlockPos pos, BlockState plant, RandomSource random) {
        BlockState node = plant.setValue(getAgeProperty(), random.nextInt(getMaxAge() + 1));
        if (!CropHooks.isWaterSource(level, pos) || !node.canSurvive(level, pos)) return false;

        level.setBlock(pos, node, 2);
        if (isMaxAge(node)) {
            for (int attempt = 0; attempt < WILD_SPREAD_ATTEMPTS; attempt++) spread(level, pos, random, 2);
        }
        return true;
    }
}
