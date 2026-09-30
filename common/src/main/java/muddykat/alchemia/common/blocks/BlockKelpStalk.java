package muddykat.alchemia.common.blocks;

import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.registration.registers.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
#if MC_VER >= 26102
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ScheduledTickAccess;
#else
import net.minecraft.world.level.LevelAccessor;
#endif
#if MC_VER >= 26102 && MC_VER < 26300
import com.mojang.serialization.MapCodec;
#endif

public class BlockKelpStalk extends Block {

#if MC_VER >= 26102 && MC_VER < 26300
    public static final MapCodec<BlockKelpStalk> CODEC = simpleCodec(properties -> new BlockKelpStalk(Ingredients.Flameweed, properties));
#endif

    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    private final Ingredients ingredient;

    public BlockKelpStalk(Ingredients ingredient, Properties properties) {
        super(properties);
        this.ingredient = ingredient;
    }

#if MC_VER >= 26102 && MC_VER < 26300
    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }
#endif

    public Ingredients getIngredient() {
        return ingredient;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return Fluids.WATER.getSource(false);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(this) || below.is(BlockRegistry.getBlock(ingredient).get());
    }

#if MC_VER >= 26102
    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
                                     BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        return state.canSurvive(level, pos) ? state : Blocks.AIR.defaultBlockState();
    }
#else
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor level,
                                  BlockPos pos, BlockPos neighbourPos) {
        return state.canSurvive(level, pos) ? state : Blocks.AIR.defaultBlockState();
    }
#endif
}
