package muddykat.alchemia.common.blocks.blockentity;

import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.items.ItemAlchemiaGuide;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.registration.registers.BlockEntityTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.InteractionResult.*;

public class BlockAlchemyCauldron extends EntityBlockGeneric {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public BlockAlchemyCauldron() {
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return BlockEntityTypeRegistry.ALCHEMICAL_CAULDRON.get().create(pPos, pState);
    }

    // Not the nicest way to store the voxel data (collision and outline)
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        VoxelShape CUT_SHAPE = Shapes.join(box(-1.0, 4.0, -1.0, 17, 16, 17),
                Shapes.or(
                        // Cut section from main cube, these four are the Main sides, under the lip of the cauldron
                        box(-1.0, 0.0, -1.0, 0, 14.0, 17), new VoxelShape[]{
                                box(16.0, 0.0, -1.0, 17, 14.0, 17),
                                box(-1.0, 0.0, -1.0, 17, 14.0, 0),
                                box(-1.0, 0.0, 16.0, 17, 14.0, 17),

                                // This section is for the lip corners removes a corner strip down the side
                                box(-1, 4, -1, 1, 16, 1),
                                box(15, 4, 15, 17, 16, 17),
                                box(15, 4, -1, 17, 16, 1),
                                box(-1, 4, 15, 1, 16, 17),

                                box(0, 4, 0, 16, 5, 1),
                                box(0, 4, 0, 1, 5, 16),
                                box(15, 4, 0, 16, 5, 16),
                                box(0, 4, 15, 16, 5, 16),

                                box(1.0, 5.0, 1.0, 15.0, 16.0, 15.0)}), BooleanOp.ONLY_FIRST);

        // Adds a pixel in the top lip corners and posts
        VoxelShape ADDITIONS = Shapes.join( box(0, 15, 0, 1, 16, 1),
                               Shapes.or(   box(15, 15, 15, 16, 16, 16), new VoxelShape[]{
                                            box(0, 15, 15, 1, 16, 16),
                                            box(15, 15, 0, 16, 16, 1),
                                            // Posts
                                       box(7, 0, -5, 10, 25, -2),
                                       box(7, 0, 18, 10, 25, 21)
                                           }), BooleanOp.OR);

        return Shapes.join(CUT_SHAPE, ADDITIONS, BooleanOp.OR);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRot) {
        return pState.setValue(FACING, pRot.rotate(pState.getValue(FACING)));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return new BlockEntityTicker<T>() {
            @Override
            public void tick(Level level, BlockPos pos, BlockState state, T tile) {
                ((TileEntityAlchemyCauldron)tile).tick();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult pHit) {

            BlockEntity tileEntity = level.getBlockEntity(pos);
            if(tileEntity instanceof TileEntityAlchemyCauldron cauldron)
            {
                return cauldron.onActivated(state, pos, player, hand);
            }

        return SUCCESS;
    }
}
