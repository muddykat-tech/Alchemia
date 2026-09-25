package muddykat.alchemia.common.blocks.blockentity;

import com.mojang.serialization.MapCodec;
import muddykat.alchemia.common.blocks.helper.AlchemyMachineParts;
import muddykat.alchemia.common.blocks.helper.MachineOwner;
import muddykat.alchemia.registration.registers.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class BlockAlchemyMachinePart extends Block {

    public static final String ID = "alchemy_machine_part";
    public static final int MAX_INDEX = 15;

#if MC_VER < 26300
    public static final MapCodec<BlockAlchemyMachinePart> CODEC = simpleCodec(BlockAlchemyMachinePart::new);
#endif

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<MachineOwner> OWNER = EnumProperty.create("owner", MachineOwner.class);
    public static final IntegerProperty INDEX = IntegerProperty.create("index", 0, MAX_INDEX);

    public BlockAlchemyMachinePart(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OWNER, MachineOwner.CORE)
                .setValue(INDEX, 0));
    }

#if MC_VER < 26300
    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }
#endif

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OWNER, INDEX);
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return AlchemyMachineParts.isMachineItem(context.getItemInHand()) || super.canBeReplaced(state, context);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        List<AlchemyMachineParts.Placed> cells = AlchemyMachineParts.placed(state.getValue(OWNER), state.getValue(FACING));
        int index = state.getValue(INDEX);
        return index < cells.size() ? cells.get(index).shape() : Shapes.block();
    }

    public static BlockPos ownerPos(BlockState state, BlockPos pos) {
        List<AlchemyMachineParts.Placed> cells = AlchemyMachineParts.placed(state.getValue(OWNER), state.getValue(FACING));
        int index = state.getValue(INDEX);
        return index < cells.size() ? pos.subtract(cells.get(index).offset()) : pos;
    }

    private static boolean isOwner(BlockState ownerState, MachineOwner owner) {
        var holder = BlockRegistry.BLOCK_REGISTRY.get(owner.registryName());
        return holder != null && ownerState.is(holder.get());
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        BlockPos owner = ownerPos(state, pos);
        BlockState ownerState = level.getBlockState(owner);
        return isOwner(ownerState, state.getValue(OWNER)) ? new ItemStack(ownerState.getBlock()) : ItemStack.EMPTY;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            BlockPos owner = ownerPos(state, pos);
            if (isOwner(level.getBlockState(owner), state.getValue(OWNER))) {
                level.destroyBlock(owner, !player.isCreative(), player);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (AlchemyMachineParts.isMachineItem(stack) && AlchemyMachineParts.upgradeSide(stack) == null) {
            return InteractionResult.PASS;
        }

        BlockPos owner = ownerPos(state, pos);
        BlockState ownerState = level.getBlockState(owner);
        if (!isOwner(ownerState, state.getValue(OWNER))) return InteractionResult.PASS;

        return ownerState.useItemOn(stack, level, player, hand,
                new BlockHitResult(hit.getLocation(), hit.getDirection(), owner, hit.isInside()));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        BlockPos owner = ownerPos(state, pos);
        BlockState ownerState = level.getBlockState(owner);
        if (!isOwner(ownerState, state.getValue(OWNER))) return InteractionResult.PASS;

        return ownerState.useWithoutItem(level, player,
                new BlockHitResult(hit.getLocation(), hit.getDirection(), owner, hit.isInside()));
    }
}
