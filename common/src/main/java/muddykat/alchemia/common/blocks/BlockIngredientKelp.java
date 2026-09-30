package muddykat.alchemia.common.blocks;

import muddykat.alchemia.common.blocks.helper.CropHooks;
import muddykat.alchemia.common.blocks.helper.PlantForm;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockIngredientKelp extends BlockIngredient implements WildGrowth {

    public static final int MIN_HEIGHT = 3;
    public static final int HEIGHT_RANGE = 6;
    public static final int GROWTH_CHANCE = 5;

    public BlockIngredientKelp(Ingredients ingredient, IngredientType type, Properties properties) {
        super(ingredient, type, properties);
    }

    public Block stalk() {
        return PlantForm.part(ingredientData, PlantForm.STALK);
    }

    public static int maxHeight(BlockPos base) {
        return MIN_HEIGHT + (int) Math.floorMod(Mth.getSeed(base), (long) HEIGHT_RANGE);
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

        if (random.nextInt(GROWTH_CHANCE) == 0) growStalk(level, pos, 3);
    }

    public boolean growStalk(LevelAccessor level, BlockPos base, int flags) {
        Block stalk = stalk();
        BlockPos.MutableBlockPos top = base.mutable();
        int height = 1;

        while (level.getBlockState(top.above()).is(stalk)) {
            top.move(Direction.UP);
            height++;
        }

        if (height >= maxHeight(base)) return false;

        BlockPos next = top.above();
        if (!CropHooks.isWaterSource(level, next)) return false;

        level.setBlock(next, stalk.defaultBlockState(), flags);
        return true;
    }

    @Override
    public boolean growWild(WorldGenLevel level, BlockPos pos, BlockState plant, RandomSource random) {
        BlockState base = plant.setValue(getAgeProperty(), random.nextInt(getMaxAge() + 1));
        if (!CropHooks.isWaterSource(level, pos) || !base.canSurvive(level, pos)) return false;

        level.setBlock(pos, base, 2);
        if (isMaxAge(base)) {
            int segments = random.nextInt(maxHeight(pos));
            for (int segment = 0; segment < segments; segment++) {
                if (!growStalk(level, pos, 2)) break;
            }
        }
        return true;
    }
}
