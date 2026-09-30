package muddykat.alchemia.common.blocks;

import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.EnumSet;
import java.util.Set;

public class BlockIngredientWild extends BlockIngredient {

    public static final Set<Ingredients> FULLY_GROWN = EnumSet.of(Ingredients.Boombloom, Ingredients.Featherbloom);

    public static final IntegerProperty STAGE = BlockStateProperties.AGE_5;
    public static final BooleanProperty WILD = BooleanProperty.create("wild");

    public BlockIngredientWild(Ingredients ingredient, IngredientType iType, Properties properties) {
        super(ingredient, iType, properties);
        this.registerDefaultState(this.defaultBlockState().setValue(WILD, false));
    }

    public static boolean hasFullGrowth(Ingredients ingredient) {
        return FULLY_GROWN.contains(ingredient);
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return STAGE;
    }

    @Override
    public int getMaxAge() {
        return 5;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE, WILD);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        if (!state.getValue(WILD)) return;

        BlockState grown = level.getBlockState(pos);
        if (grown.is(this) && !grown.getValue(WILD)) {
            level.setBlock(pos, grown.setValue(WILD, true), 2);
        }
    }
}
