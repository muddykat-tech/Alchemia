package muddykat.alchemia.common.blocks;

import muddykat.alchemia.common.items.helper.IngredientAlignment;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.registration.registers.ItemRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.common.IPlantable;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class BlockIngredient extends CropBlock implements BonemealableBlock, IPlantable {

    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    protected final Ingredients ingredientData;
    protected final IngredientType type;
    public BlockIngredient(Ingredients ingredient, IngredientType iType) {
        super(Properties.copy(Blocks.WHEAT));
        this.registerDefaultState(this.defaultBlockState().setValue(AGE, 0));
        this.ingredientData = ingredient;
        this.type = iType;

    }
    public @NotNull IntegerProperty getAgeProperty() {
        return AGE;
    }

    public int getMaxAge() {
        return 3;
    }

    public boolean isMaxAge(BlockState state) {
        return state.getValue(this.getAgeProperty()) >= this.getMaxAge();
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter pLevel, BlockPos pPos, BlockState pState, boolean pIsClient) {
        return !isMaxAge(pState);
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return ingredientData.getPrimaryAlignment() != IngredientAlignment.Fire;
    }

    @Override
    public boolean isBonemealSuccess(Level pLevel, Random pRandom, BlockPos pPos, BlockState pState) {
        return true;
    }

    protected int getAge(BlockState state) {
        return state.getValue(this.getAgeProperty());
    }

    protected int getBonemealAgeIncrease(Level level) {
        return Mth.nextInt(level.random, 1, 4);
    }

    @Override
    public void performBonemeal(ServerLevel level, Random rand, BlockPos pos, BlockState state) {
        int ageGrowth = Math.min(this.getAge(state) + this.getBonemealAgeIncrease(level), 7);
        if (ageGrowth <= this.getMaxAge()) {
            level.setBlockAndUpdate(pos, state.setValue(AGE, ageGrowth));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(AGE);
    }

    public Ingredients getIngredient(){
        return ingredientData;
    }

    public IngredientType getIngredientType() {
        return type;
    }

    @Override
    protected boolean mayPlaceOn(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        switch(type) {
            case Flower, Herb -> {
                return ingredientData.getPrimaryAlignment() == IngredientAlignment.Fire ? pState.is(BlockTags.SAND) : pState.is(BlockTags.DIRT) || pState.is(Blocks.FARMLAND);
            }
            case Mushroom -> {
                return pState.is(BlockTags.BASE_STONE_OVERWORLD) && pState.getLightEmission(pLevel, pPos) <= 5;
            }
            case Root -> {
                Block source = Blocks.WATER;
                if(ingredientData.getPrimaryAlignment() == IngredientAlignment.Fire){
                    source = Blocks.LAVA;
                }
                return pLevel.getBlockState(pPos.below()).is(Blocks.AIR) && pLevel.getBlockState(pPos.above()).is(BlockTags.DIRT) && pLevel.getBlockState(pPos.above(2)).is(source);
            }
            case Mineral -> {
                return pState.is(BlockTags.BASE_STONE_OVERWORLD);
            }
        }
        return false;
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        BlockPos blockpos = pPos.below();



        if (pState.getBlock() == this) //Forge: This function is called during world gen and placement, before this block is set, so if we are not 'here' then assume it's the pre-check.
            switch(type){
                case Root -> {
                    Block source = Blocks.WATER;
                    if(ingredientData.getPrimaryAlignment() == IngredientAlignment.Fire){
                        source = Blocks.LAVA;
                    }
                    return pLevel.getBlockState(pPos.below()).is(Blocks.AIR) && pLevel.getBlockState(pPos.above()).is(BlockTags.DIRT) && pLevel.getBlockState(pPos.above(2)).is(source);
                }
                case Mushroom -> {
                    return pLevel.getBlockState(blockpos.below()).is(BlockTags.BASE_STONE_OVERWORLD);
                }
                default -> {
                    if(ingredientData.getPrimaryAlignment() == IngredientAlignment.Fire) {
                        return pLevel.getBlockState(blockpos).is(BlockTags.SAND);
                    }
                    return pLevel.getBlockState(blockpos).canSustainPlant(pLevel, blockpos, Direction.UP, this);
                }
            }
        return this.mayPlaceOn(pLevel.getBlockState(blockpos), pLevel, blockpos);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ItemRegister.getSeedByIngredient(getIngredient());
    }
    @Override
    public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
        return new ItemStack(this.getBaseSeedId());
    }
}
