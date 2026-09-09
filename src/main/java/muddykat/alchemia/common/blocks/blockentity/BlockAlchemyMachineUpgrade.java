package muddykat.alchemia.common.blocks.blockentity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import muddykat.alchemia.common.blocks.helper.UpgradeSide;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyMachineCore;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyMachineUpgrade;
import muddykat.alchemia.registration.registers.BlockEntityTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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

    private @Nullable Direction findCoreFacing(LevelReader level, BlockPos pos) {
        for (Direction candidate : Direction.Plane.HORIZONTAL) {
            BlockPos corePos = pos.relative(side.directionFrom(candidate).getOpposite());
            BlockState coreState = level.getBlockState(corePos);
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
        return Shapes.block();
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
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
