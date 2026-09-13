package muddykat.alchemia.common.blocks.blockentity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import muddykat.alchemia.common.blocks.helper.AlchemyMachineParts;
import muddykat.alchemia.common.blocks.helper.MachineOwner;
import muddykat.alchemia.common.blocks.helper.UpgradeSide;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyMachineCore;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyMachineUpgrade;
import muddykat.alchemia.registration.registers.BlockEntityTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class BlockAlchemyMachineUpgrade extends EntityBlockGeneric {

    public static final StringRepresentable.EnumCodec<UpgradeSide> SIDE_CODEC = StringRepresentable.fromEnum(UpgradeSide::values);

    public static final MapCodec<BlockAlchemyMachineUpgrade> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SIDE_CODEC.fieldOf("side").forGetter(BlockAlchemyMachineUpgrade::getSide),
            propertiesCodec()
    ).apply(instance, BlockAlchemyMachineUpgrade::new));

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    private final UpgradeSide side;

    public BlockAlchemyMachineUpgrade(UpgradeSide side, Properties properties) {
        super(properties);
        this.side = side;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public UpgradeSide getSide() {
        return side;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityTypeRegistry.ALCHEMY_MACHINE_UPGRADE.get().create(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction aligned = findCoreFacing(context.getLevel(), context.getClickedPos());
        Direction facing = aligned != null ? aligned : context.getHorizontalDirection().getOpposite();
        return this.defaultBlockState().setValue(FACING, facing);
    }

    public BlockPos corePos(BlockPos pos, Direction facing) {
        return pos.relative(side.directionFrom(facing).getOpposite());
    }

    public @Nullable Direction findCoreFacing(LevelReader level, BlockPos pos) {
        for (Direction candidate : Direction.Plane.HORIZONTAL) {
            BlockState coreState = level.getBlockState(corePos(pos, candidate));
            if (coreState.getBlock() instanceof BlockAlchemyMachineCore
                    && coreState.getValue(BlockAlchemyMachineCore.FACING) == candidate) {
                return candidate;
            }
        }
        return null;
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
        return AlchemyMachineParts.ownShape(MachineOwner.of(side), state.getValue(FACING));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        AlchemyMachineParts.place(level, pos, state.getValue(FACING), MachineOwner.of(side));
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof TileEntityAlchemyMachineUpgrade upgrade) {
                upgrade.dropContents(pos);
            }
            Direction facing = state.getValue(FACING);
            BlockPos corePos = corePos(pos, facing);
            AlchemyMachineParts.remove(level, pos, facing, MachineOwner.of(side));
            AlchemyMachineParts.dropDependents(level, corePos, facing, side);
            AlchemyMachineParts.refresh(level, corePos, facing);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (AlchemyMachineParts.upgradeSide(stack) != null) {
            BlockPos corePos = corePos(pos, state.getValue(FACING));
            BlockState coreState = level.getBlockState(corePos);
            if (!(coreState.getBlock() instanceof BlockAlchemyMachineCore)
                    || coreState.getValue(BlockAlchemyMachineCore.FACING) != state.getValue(FACING)) {
                return InteractionResult.PASS;
            }
            InteractionResult placed = AlchemyMachineParts.placeUpgrade(level, corePos, state.getValue(FACING), stack, player, hand);
            return placed.consumesAction() ? placed : BlockAlchemyMachineCore.openMenu(level, corePos, player);
        }
        if (AlchemyMachineParts.isMachineItem(stack)) return InteractionResult.PASS;

        if (level.getBlockEntity(pos) instanceof TileEntityAlchemyMachineUpgrade upgrade) {
            InteractionResult inserted = upgrade.insertPotion(player, hand, hitResult);
            if (inserted != InteractionResult.PASS) return inserted;

            TileEntityAlchemyMachineCore core = upgrade.getCore();
            if (core != null) return BlockAlchemyMachineCore.openMenu(level, core.getBlockPos(), player);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof TileEntityAlchemyMachineUpgrade upgrade) {
            TileEntityAlchemyMachineCore core = upgrade.getCore();
            if (core != null) return BlockAlchemyMachineCore.openMenu(level, core.getBlockPos(), player);
        }
        return InteractionResult.PASS;
    }
}
