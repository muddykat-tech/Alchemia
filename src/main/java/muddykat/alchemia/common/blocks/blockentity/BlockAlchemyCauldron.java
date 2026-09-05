package muddykat.alchemia.common.blocks.blockentity;

import com.mojang.serialization.MapCodec;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.registration.registers.BlockEntityTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class BlockAlchemyCauldron extends EntityBlockGeneric {

    public static final MapCodec<BlockAlchemyCauldron> CODEC = simpleCodec(BlockAlchemyCauldron::new);

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape SHAPE = buildShape();

    public BlockAlchemyCauldron(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityTypeRegistry.ALCHEMICAL_CAULDRON.get().create(pos, state);
    }

    private static VoxelShape buildShape() {
        VoxelShape cut = Shapes.join(box(-1.0, 4.0, -1.0, 17, 16, 17),
                Shapes.or(
                        box(-1.0, 0.0, -1.0, 0, 14.0, 17), new VoxelShape[]{
                                box(16.0, 0.0, -1.0, 17, 14.0, 17),
                                box(-1.0, 0.0, -1.0, 17, 14.0, 0),
                                box(-1.0, 0.0, 16.0, 17, 14.0, 17),

                                box(-1, 4, -1, 1, 16, 1),
                                box(15, 4, 15, 17, 16, 17),
                                box(15, 4, -1, 17, 16, 1),
                                box(-1, 4, 15, 1, 16, 17),

                                box(0, 4, 0, 16, 5, 1),
                                box(0, 4, 0, 1, 5, 16),
                                box(15, 4, 0, 16, 5, 16),
                                box(0, 4, 15, 16, 5, 16),

                                box(1.0, 5.0, 1.0, 15.0, 16.0, 15.0)}), BooleanOp.ONLY_FIRST);

        VoxelShape additions = Shapes.join(box(0, 15, 0, 1, 16, 1),
                Shapes.or(box(15, 15, 15, 16, 16, 16), new VoxelShape[]{
                        box(0, 15, 15, 1, 16, 16),
                        box(15, 15, 0, 16, 16, 1),
                        box(7, 0, -5, 10, 25, -2),
                        box(7, 0, 18, 10, 25, 21)
                }), BooleanOp.OR);

        return Shapes.join(cut, additions, BooleanOp.OR);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (tickLevel, pos, tickState, blockEntity) -> ((TileEntityAlchemyCauldron) blockEntity).tick();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof TileEntityAlchemyCauldron cauldron) {
            return cauldron.onActivated(state, pos, player, InteractionHand.MAIN_HAND);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof TileEntityAlchemyCauldron cauldron) {
            return cauldron.onActivated(state, pos, player, hand);
        }
        return InteractionResult.SUCCESS;
    }
}
