package muddykat.alchemia.common.blocks.blockentity;

import muddykat.alchemia.common.blocks.helper.AlchemyMachineParts;
import muddykat.alchemia.common.blocks.helper.MachineOwner;
import muddykat.alchemia.common.blocks.helper.MachineRepair;
import muddykat.alchemia.common.blocks.helper.UpgradeSide;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
#if MC_VER >= 26100
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;
#else
import javax.annotation.Nullable;
#endif

public class BlockRuinedMachine extends Block {

#if MC_VER >= 26100 && MC_VER < 26300
    public static final StringRepresentable.EnumCodec<MachineOwner> OWNER_CODEC = StringRepresentable.fromEnum(MachineOwner::values);
    public static final MapCodec<BlockRuinedMachine> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            OWNER_CODEC.fieldOf("owner").forGetter(BlockRuinedMachine::owner),
            propertiesCodec()
    ).apply(instance, BlockRuinedMachine::new));
#endif

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    private final MachineOwner owner;

    public BlockRuinedMachine(MachineOwner owner, Properties properties) {
        super(properties);
        this.owner = owner;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(MachineRepair.REPAIR, 0));
    }

    public MachineOwner owner() {
        return owner;
    }

    public @Nullable UpgradeSide side() {
        return switch (owner) {
            case CORE -> null;
            case RIGHT -> UpgradeSide.RIGHT;
            case LEFT -> UpgradeSide.LEFT;
            case TOP -> UpgradeSide.TOP;
        };
    }

#if MC_VER >= 26100 && MC_VER < 26300
    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }
#endif

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, MachineRepair.REPAIR);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        UpgradeSide side = side();
        Direction facing = side == null ? null : findCoreFacing(context.getLevel(), context.getClickedPos());
        return defaultBlockState().setValue(FACING, facing != null ? facing : context.getHorizontalDirection().getOpposite());
    }

    public @Nullable Direction findCoreFacing(LevelReader level, BlockPos pos) {
        UpgradeSide side = side();
        if (side == null) return null;
        for (Direction candidate : Direction.Plane.HORIZONTAL) {
            if (MachineRepair.coreAt(level, pos.relative(side.directionFrom(candidate).getOpposite()), candidate)) return candidate;
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        AlchemyMachineParts.place(level, pos, state.getValue(FACING), owner);
    }

#if MC_VER >= 26100
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return MachineRepair.use(stack, state, level, pos, player, hand) == MachineRepair.Outcome.SUCCESS
                ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return MachineRepair.use(ItemStack.EMPTY, state, level, pos, player, InteractionHand.MAIN_HAND) == MachineRepair.Outcome.SUCCESS
                ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }
#elif MC_VER >= 12100
    @Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return MachineRepair.use(stack, state, level, pos, player, hand) == MachineRepair.Outcome.SUCCESS
                ? net.minecraft.world.ItemInteractionResult.sidedSuccess(level.isClientSide())
                : net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return MachineRepair.use(ItemStack.EMPTY, state, level, pos, player, InteractionHand.MAIN_HAND) == MachineRepair.Outcome.SUCCESS
                ? InteractionResult.sidedSuccess(level.isClientSide()) : InteractionResult.PASS;
    }
#else
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return MachineRepair.use(player.getItemInHand(hand), state, level, pos, player, hand) == MachineRepair.Outcome.SUCCESS
                ? InteractionResult.sidedSuccess(level.isClientSide()) : InteractionResult.PASS;
    }
#endif

    @Override
#if MC_VER >= 12100
    protected BlockState rotate(BlockState state, Rotation rotation) {
#else
    public BlockState rotate(BlockState state, Rotation rotation) {
#endif
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
#if MC_VER >= 12100
    protected BlockState mirror(BlockState state, Mirror mirror) {
#else
    public BlockState mirror(BlockState state, Mirror mirror) {
#endif
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
#if MC_VER >= 12100
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
#else
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
#endif
        return AlchemyMachineParts.ownShape(owner, state.getValue(FACING));
    }

#if MC_VER >= 26100
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        collapse(state, level, pos);
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }
#else
    @Override
#if MC_VER >= 12100
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
#else
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
#endif
        if (!state.is(newState.getBlock())) collapse(state, level, pos);
        super.onRemove(state, level, pos, newState, isMoving);
    }
#endif

    private void collapse(BlockState state, Level level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        AlchemyMachineParts.remove(level, pos, facing, owner);
        UpgradeSide side = side();
        BlockPos core = side == null ? pos : pos.relative(side.directionFrom(facing).getOpposite());
        settle(level, core, facing, MachineOwner.CORE);
        for (UpgradeSide other : UpgradeSide.values()) {
            settle(level, core.relative(other.directionFrom(facing)), facing, MachineOwner.of(other));
        }
    }

    private static void settle(Level level, BlockPos pos, Direction facing, MachineOwner owner) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof BlockRuinedMachine ruined && ruined.owner == owner && state.getValue(FACING) == facing) {
            AlchemyMachineParts.place(level, pos, facing, owner);
        }
    }
}
