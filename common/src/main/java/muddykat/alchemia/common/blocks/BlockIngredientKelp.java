package muddykat.alchemia.common.blocks;

import muddykat.alchemia.common.blocks.helper.CropHooks;
import muddykat.alchemia.common.blocks.helper.PlantForm;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class BlockIngredientKelp extends BlockIngredientTall {

    public BlockIngredientKelp(Ingredients ingredient, IngredientType type, Properties properties) {
        super(ingredient, type, properties);
    }

    public Block stalk() {
        return PlantForm.part(ingredientData, PlantForm.STALK);
    }

    @Override
    protected int upperFromAge() {
        return getMaxAge();
    }

    @Override
    protected boolean canPlaceUpper(LevelReader level, BlockPos pos) {
        return CropHooks.isWaterSource(level, pos) || level.getBlockState(pos).is(stalk());
    }

    @Override
    protected boolean hasValidUpper(BlockState state, LevelReader level, BlockPos pos) {
        BlockState above = level.getBlockState(pos.above());
        return super.hasValidUpper(state, level, pos) || above.is(stalk());
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return isLower(state);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!isLower(state)) return;

        BlockState above = level.getBlockState(pos.above());
        if (hasUpper(state) && !isHalf(above, DoubleBlockHalf.UPPER) && canPlaceUpper(level, pos.above())) {
            raiseUpper(level, pos, state);
            return;
        }

        super.randomTick(state, level, pos, random);

        BlockState grown = level.getBlockState(pos);
        if (grown.is(this) && isLower(grown) && hasUpper(grown)) {
            above = level.getBlockState(pos.above());
            if (!isHalf(above, DoubleBlockHalf.UPPER) && canPlaceUpper(level, pos.above())) {
                raiseUpper(level, pos, grown);
            }
        }
    }

    @Override
#if MC_VER >= 26300
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state,
                                net.minecraft.world.level.block.BonemealSource source) {
#else
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
#endif
        if (!isLower(state)) return;

        int age = Math.min(getAge(state) + getBonemealAgeIncrease(level), getMaxAge());
        if (age >= upperFromAge() && !canPlaceUpper(level, pos.above())) age = upperFromAge() - 1;

        BlockState grown = state.setValue(getAgeProperty(), age);
        level.setBlockAndUpdate(pos, grown);
        if (hasUpper(grown) && canPlaceUpper(level, pos.above())) raiseUpper(level, pos, grown);
    }
}
