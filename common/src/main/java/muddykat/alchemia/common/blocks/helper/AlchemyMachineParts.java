package muddykat.alchemia.common.blocks.helper;

import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyMachineCore;
import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyMachinePart;
import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyMachineUpgrade;
import muddykat.alchemia.registration.registers.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class AlchemyMachineParts {

    public record Placed(BlockPos offset, VoxelShape shape) {}

    private static final Map<MachineOwner, EnumMap<Direction, List<Placed>>> CACHE = new EnumMap<>(MachineOwner.class);

    static {
        for (MachineOwner owner : MachineOwner.values()) {
            EnumMap<Direction, List<Placed>> byFacing = new EnumMap<>(Direction.class);
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                List<Placed> placed = new ArrayList<>();
                for (AlchemyMachineShapes.Cell cell : owner.cells()) {
                    placed.add(new Placed(rotate(cell.x(), cell.y(), cell.z(), facing), rotate(cell.shape(), facing)));
                }
                byFacing.put(facing, List.copyOf(placed));
            }
            CACHE.put(owner, byFacing);
        }
    }

    private AlchemyMachineParts() {
    }

    public static List<Placed> placed(MachineOwner owner, Direction facing) {
        EnumMap<Direction, List<Placed>> byFacing = CACHE.get(owner);
        List<Placed> placed = byFacing.get(facing);
        return placed != null ? placed : byFacing.get(Direction.NORTH);
    }

    public static BlockPos rotate(int x, int y, int z, Direction facing) {
        return switch (facing) {
            case EAST -> new BlockPos(-z, y, x);
            case SOUTH -> new BlockPos(-x, y, -z);
            case WEST -> new BlockPos(z, y, -x);
            default -> new BlockPos(x, y, z);
        };
    }

    public static VoxelShape rotate(VoxelShape shape, Direction facing) {
        if (facing == Direction.NORTH) return shape;

        VoxelShape rotated = Shapes.empty();
        for (AABB box : shape.toAabbs()) {
            rotated = Shapes.or(rotated, switch (facing) {
                case EAST -> Shapes.box(1 - box.maxZ, box.minY, box.minX, 1 - box.minZ, box.maxY, box.maxX);
                case SOUTH -> Shapes.box(1 - box.maxX, box.minY, 1 - box.maxZ, 1 - box.minX, box.maxY, 1 - box.minZ);
                case WEST -> Shapes.box(box.minZ, box.minY, 1 - box.maxX, box.maxZ, box.maxY, 1 - box.minX);
                default -> Shapes.box(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
            });
        }
        return rotated;
    }

    public static VoxelShape ownShape(MachineOwner owner, Direction facing) {
        for (Placed placed : placed(owner, facing)) {
            if (placed.offset().equals(BlockPos.ZERO)) return placed.shape();
        }
        return Shapes.block();
    }

    public static boolean isMachineItem(ItemStack stack) {
        return stack.getItem() instanceof BlockItem item
                && (item.getBlock() instanceof BlockAlchemyMachineCore
                || item.getBlock() instanceof BlockAlchemyMachineUpgrade);
    }

    public static @Nullable UpgradeSide upgradeSide(ItemStack stack) {
        return stack.getItem() instanceof BlockItem item
                && item.getBlock() instanceof BlockAlchemyMachineUpgrade upgrade ? upgrade.getSide() : null;
    }

    public static boolean hasUpgrade(LevelReader level, BlockPos corePos, Direction facing, UpgradeSide side) {
        BlockState state = level.getBlockState(corePos.relative(side.directionFrom(facing)));
        return state.getBlock() instanceof BlockAlchemyMachineUpgrade upgrade
                && upgrade.getSide() == side
                && state.getValue(BlockAlchemyMachineUpgrade.FACING) == facing;
    }

    public static void dropDependents(Level level, BlockPos corePos, Direction facing, UpgradeSide removed) {
        for (UpgradeSide side : UpgradeSide.values()) {
            if (side.requires() != removed) continue;
            if (hasUpgrade(level, corePos, facing, side)) {
                level.destroyBlock(corePos.relative(side.directionFrom(facing)), true);
            }
        }
    }

    public static InteractionResult placeUpgrade(Level level, BlockPos corePos, Direction facing, ItemStack stack, Player player, InteractionHand hand) {
        UpgradeSide side = upgradeSide(stack);
        if (side == null || !player.mayBuild()) return InteractionResult.PASS;

        Direction offset = side.directionFrom(facing);
        BlockPos target = corePos.relative(offset);
        if (level.getBlockState(target).getBlock() instanceof BlockAlchemyMachineUpgrade) return InteractionResult.FAIL;

        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(target), offset, target, false);
        BlockPlaceContext context = new BlockPlaceContext(level, player, hand, stack, hit);
        if (!context.replacingClickedOnBlock()) return InteractionResult.FAIL;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        return ((BlockItem) stack.getItem()).place(context);
    }

    public static void refresh(Level level, BlockPos corePos, Direction facing) {
        BlockState coreState = level.getBlockState(corePos);
        if (coreState.getBlock() instanceof BlockAlchemyMachineCore
                && coreState.getValue(BlockAlchemyMachineCore.FACING) == facing) {
            place(level, corePos, facing, MachineOwner.CORE);
        }

        for (UpgradeSide side : UpgradeSide.values()) {
            BlockPos upgradePos = corePos.relative(side.directionFrom(facing));
            BlockState upgradeState = level.getBlockState(upgradePos);
            if (upgradeState.getBlock() instanceof BlockAlchemyMachineUpgrade upgrade
                    && upgrade.getSide() == side
                    && upgradeState.getValue(BlockAlchemyMachineUpgrade.FACING) == facing) {
                place(level, upgradePos, facing, MachineOwner.of(side));
            }
        }
    }

    public static void place(Level level, BlockPos pos, Direction facing, MachineOwner owner) {
        if (level.isClientSide()) return;

        Block part = BlockRegistry.BLOCK_REGISTRY.get(BlockAlchemyMachinePart.ID).get();
        List<Placed> cells = placed(owner, facing);
        for (int index = 0; index < cells.size(); index++) {
            BlockPos target = pos.offset(cells.get(index).offset());
            if (target.equals(pos)) continue;
            if (!level.getBlockState(target).canBeReplaced()) continue;

            level.setBlock(target, part.defaultBlockState()
                    .setValue(BlockAlchemyMachinePart.FACING, facing)
                    .setValue(BlockAlchemyMachinePart.OWNER, owner)
                    .setValue(BlockAlchemyMachinePart.INDEX, index), Block.UPDATE_ALL);
        }
    }

    public static void remove(Level level, BlockPos pos, Direction facing, MachineOwner owner) {
        if (level.isClientSide()) return;

        List<Placed> cells = placed(owner, facing);
        for (int index = 0; index < cells.size(); index++) {
            BlockPos target = pos.offset(cells.get(index).offset());
            if (target.equals(pos)) continue;

            BlockState existing = level.getBlockState(target);
            if (!(existing.getBlock() instanceof BlockAlchemyMachinePart)) continue;
            if (existing.getValue(BlockAlchemyMachinePart.OWNER) != owner) continue;
            if (existing.getValue(BlockAlchemyMachinePart.FACING) != facing) continue;
            if (existing.getValue(BlockAlchemyMachinePart.INDEX) != index) continue;

            level.setBlock(target, level.getFluidState(target).createLegacyBlock(), Block.UPDATE_ALL);
        }
    }
}
