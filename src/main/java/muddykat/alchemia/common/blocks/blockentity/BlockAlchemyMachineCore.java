package muddykat.alchemia.common.blocks.blockentity;

import com.mojang.serialization.MapCodec;
import muddykat.alchemia.common.blocks.helper.AlchemyMachineParts;
import muddykat.alchemia.common.blocks.helper.MachineOwner;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyMachineCore;
import muddykat.alchemia.registration.registers.BlockEntityTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class BlockAlchemyMachineCore extends EntityBlockGeneric {

    public static final MapCodec<BlockAlchemyMachineCore> CODEC = simpleCodec(BlockAlchemyMachineCore::new);

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public BlockAlchemyMachineCore(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityTypeRegistry.ALCHEMY_MACHINE_CORE.get().create(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AlchemyMachineParts.ownShape(MachineOwner.CORE, state.getValue(FACING));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        AlchemyMachineParts.place(level, pos, state.getValue(FACING), MachineOwner.CORE);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        AlchemyMachineParts.remove(level, pos, state.getValue(FACING), MachineOwner.CORE);
        AlchemyMachineParts.refresh(level, pos, state.getValue(FACING));
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return (tickLevel, pos, tickState, blockEntity) -> ((TileEntityAlchemyMachineCore) blockEntity).tick();
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, net.minecraft.world.phys.BlockHitResult hitResult) {
        if (AlchemyMachineParts.upgradeSide(stack) != null) {
            InteractionResult placed = AlchemyMachineParts.placeUpgrade(level, pos, state.getValue(FACING), stack, player, hand);
            return placed.consumesAction() ? placed : openMenu(level, pos, player);
        }
        if (AlchemyMachineParts.isMachineItem(stack)) return InteractionResult.PASS;
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.phys.BlockHitResult hitResult) {
        return openMenu(level, pos, player);
    }

    public static InteractionResult openMenu(Level level, BlockPos pos, Player player) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof TileEntityAlchemyMachineCore core) {
            serverPlayer.openMenu(core, pos);
        }
        return InteractionResult.SUCCESS;
    }
}
