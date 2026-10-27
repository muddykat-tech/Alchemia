package muddykat.alchemia.common.blocks.helper;

import muddykat.alchemia.client.ClientHooks;
import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyMachineCore;
import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyMachineUpgrade;
import muddykat.alchemia.common.blocks.blockentity.BlockRuinedMachine;
import muddykat.alchemia.common.guide.GuideMastery;
import muddykat.alchemia.registration.registers.BlockRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
#if MC_VER >= 26100
import org.jspecify.annotations.Nullable;
#else
import javax.annotation.Nullable;
#endif

import java.util.List;

public final class MachineRepair {

    public enum Outcome {
        PASS,
        SUCCESS
    }

    public static final List<MachineOwner> ORDER = List.of(MachineOwner.CORE, MachineOwner.RIGHT, MachineOwner.LEFT, MachineOwner.TOP);
    public static final IntegerProperty REPAIR = IntegerProperty.create("repair", 0, 3);

    private static final List<Item> CORE_STEPS = List.of(Items.BRICK, Items.IRON_INGOT, Items.COPPER_INGOT, Items.GLASS);
    private static final List<Item> UPGRADE_STEPS = List.of(Items.IRON_INGOT, Items.COPPER_INGOT, Items.GLASS);

    public record Target(BlockPos pos, MachineOwner owner, Item needed) {
    }

    private record Plan(@Nullable Target target, @Nullable Component blocked) {
    }

    private MachineRepair() {
    }

    public static List<Item> steps(MachineOwner owner) {
        return owner == MachineOwner.CORE ? CORE_STEPS : UPGRADE_STEPS;
    }

    public static BlockPos corePos(MachineOwner owner, BlockPos pos, Direction facing) {
        UpgradeSide side = sideOf(owner);
        return side == null ? pos : pos.relative(side.directionFrom(facing).getOpposite());
    }

    public static BlockPos partPos(MachineOwner owner, BlockPos corePos, Direction facing) {
        UpgradeSide side = sideOf(owner);
        return side == null ? corePos : corePos.relative(side.directionFrom(facing));
    }

    public static @Nullable UpgradeSide sideOf(MachineOwner owner) {
        return switch (owner) {
            case CORE -> null;
            case RIGHT -> UpgradeSide.RIGHT;
            case LEFT -> UpgradeSide.LEFT;
            case TOP -> UpgradeSide.TOP;
        };
    }

    public static boolean isRuinedItem(ItemStack stack) {
        return stack.getItem() instanceof BlockItem item && item.getBlock() instanceof BlockRuinedMachine;
    }

    public static @Nullable UpgradeSide ruinedSide(ItemStack stack) {
        return stack.getItem() instanceof BlockItem item && item.getBlock() instanceof BlockRuinedMachine ruined ? ruined.side() : null;
    }

    private static boolean ruinedAt(LevelReader level, BlockPos pos, MachineOwner owner, Direction facing) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof BlockRuinedMachine ruined && ruined.owner() == owner
                && state.getValue(BlockRuinedMachine.FACING) == facing;
    }

    private static boolean restoredAt(LevelReader level, BlockPos pos, MachineOwner owner, Direction facing) {
        BlockState state = level.getBlockState(pos);
        if (owner == MachineOwner.CORE) {
            return state.getBlock() instanceof BlockAlchemyMachineCore && state.getValue(BlockAlchemyMachineCore.FACING) == facing;
        }
        return state.getBlock() instanceof BlockAlchemyMachineUpgrade upgrade && upgrade.getSide() == sideOf(owner)
                && state.getValue(BlockAlchemyMachineUpgrade.FACING) == facing;
    }

    public static boolean coreAt(LevelReader level, BlockPos corePos, Direction facing) {
        return ruinedAt(level, corePos, MachineOwner.CORE, facing) || restoredAt(level, corePos, MachineOwner.CORE, facing);
    }

    private static Plan plan(LevelReader level, BlockPos corePos, Direction facing) {
        for (MachineOwner owner : ORDER) {
            BlockPos at = partPos(owner, corePos, facing);
            if (ruinedAt(level, at, owner, facing)) {
                Component blocked = blocker(level, corePos, facing, owner);
                if (blocked != null) return new Plan(null, blocked);
                int step = Math.min(level.getBlockState(at).getValue(REPAIR), steps(owner).size() - 1);
                return new Plan(new Target(at, owner, steps(owner).get(step)), null);
            }
        }
        return new Plan(null, null);
    }

    private static @Nullable Component blocker(LevelReader level, BlockPos corePos, Direction facing, MachineOwner owner) {
        if (owner == MachineOwner.CORE) return null;
        if (!restoredAt(level, corePos, MachineOwner.CORE, facing)) return needs(MachineOwner.CORE);
        if (owner == MachineOwner.TOP && !restoredAt(level, partPos(MachineOwner.LEFT, corePos, facing), MachineOwner.LEFT, facing)) {
            return needs(MachineOwner.LEFT);
        }
        return null;
    }

    private static Component needs(MachineOwner owner) {
        return Component.translatable("alchemia.machine.repair.needs", Component.translatable("block.alchemia." + owner.registryName()));
    }

    public static Outcome use(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (!(state.getBlock() instanceof BlockRuinedMachine ruined)) return Outcome.PASS;
        Direction facing = state.getValue(BlockRuinedMachine.FACING);
        BlockPos corePos = corePos(ruined.owner(), pos, facing);

        if (ruinedSide(held) != null) return attach(held, level, corePos, facing, player, hand);
        if (isRuinedItem(held) || AlchemyMachineParts.isMachineItem(held)) return Outcome.PASS;

        Plan plan = plan(level, corePos, facing);
        if (plan.blocked() != null) {
            if (!held.isEmpty()) return Outcome.PASS;
            tell(player, plan.blocked());
            return Outcome.SUCCESS;
        }

        Target target = plan.target();
        if (target == null) return Outcome.PASS;

        if (held.isEmpty()) {
            if (level.isClientSide()) ClientHooks.repairHint(target.pos(), new ItemStack(target.needed()));
            if (!target.pos().equals(pos)) tell(player, needs(target.owner()));
            return Outcome.SUCCESS;
        }
        if (!held.is(target.needed())) return Outcome.PASS;
        if (!target.pos().equals(pos)) {
            if (level.isClientSide()) ClientHooks.repairHint(target.pos(), new ItemStack(target.needed()));
            tell(player, needs(target.owner()));
            return Outcome.SUCCESS;
        }
        if (level.isClientSide()) return Outcome.SUCCESS;

        if (!player.getAbilities().instabuild) held.shrink(1);
        advance(level, target, facing, player);
        return Outcome.SUCCESS;
    }

    private static void advance(Level level, Target target, Direction facing, Player player) {
        BlockState state = level.getBlockState(target.pos());
        int done = state.getValue(REPAIR) + 1;
        Vec3 centre = Vec3.atCenterOf(target.pos());

        if (done < steps(target.owner()).size()) {
            level.setBlock(target.pos(), state.setValue(REPAIR, done), Block.UPDATE_ALL);
            level.playSound(null, target.pos(), SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 0.7F, 0.9F + done * 0.1F);
            if (level instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.WAX_ON, centre.x, centre.y + 0.4, centre.z, 10, 0.45, 0.4, 0.45, 0.02);
            }
            return;
        }

        restore(level, target.pos(), target.owner(), facing);
        level.playSound(null, target.pos(), SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.6F, 1.2F);
        level.playSound(null, target.pos(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 1.0F);
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.HAPPY_VILLAGER, centre.x, centre.y + 0.5, centre.z, 24, 0.6, 0.5, 0.6, 0.0);
            server.sendParticles(ParticleTypes.WAX_ON, centre.x, centre.y + 0.5, centre.z, 20, 0.6, 0.5, 0.6, 0.05);
        }
        if (player instanceof ServerPlayer serverPlayer) GuideMastery.restored(serverPlayer, target.owner());
    }

    private static void restore(Level level, BlockPos pos, MachineOwner owner, Direction facing) {
        BlockState restored;
        if (owner == MachineOwner.CORE) {
            restored = BlockRegistry.BLOCK_REGISTRY.get(owner.registryName()).get().defaultBlockState()
                    .setValue(BlockAlchemyMachineCore.FACING, facing).setValue(BlockAlchemyMachineCore.LIT, false);
        } else {
            restored = BlockRegistry.BLOCK_REGISTRY.get(owner.registryName()).get().defaultBlockState()
                    .setValue(BlockAlchemyMachineUpgrade.FACING, facing);
        }
        level.setBlock(pos, restored, Block.UPDATE_ALL);
        AlchemyMachineParts.place(level, pos, facing, owner);
        AlchemyMachineParts.refresh(level, corePos(owner, pos, facing), facing);
    }

    public static Outcome attach(ItemStack held, Level level, BlockPos corePos, Direction facing, Player player, InteractionHand hand) {
        UpgradeSide side = ruinedSide(held);
        if (side == null || !player.mayBuild()) return Outcome.PASS;

        Direction offset = side.directionFrom(facing);
        BlockPos target = corePos.relative(offset);
        BlockState existing = level.getBlockState(target);
        if (existing.getBlock() instanceof BlockRuinedMachine || existing.getBlock() instanceof BlockAlchemyMachineUpgrade) return Outcome.PASS;

        BlockPlaceContext context = new BlockPlaceContext(level, player, hand, held, new BlockHitResult(Vec3.atCenterOf(target), offset, target, false));
        if (!context.replacingClickedOnBlock()) return Outcome.PASS;
        if (level.isClientSide()) return Outcome.SUCCESS;
        return ((BlockItem) held.getItem()).place(context).consumesAction() ? Outcome.SUCCESS : Outcome.PASS;
    }

    public static boolean attachedFor(LevelReader level, BlockPos pos, UpgradeSide side, Direction facing) {
        BlockPos corePos = pos.relative(side.directionFrom(facing).getOpposite());
        if (!coreAt(level, corePos, facing)) return false;
        UpgradeSide required = side.requires();
        if (required == null) return true;
        BlockPos requiredPos = corePos.relative(required.directionFrom(facing));
        MachineOwner owner = MachineOwner.of(required);
        return ruinedAt(level, requiredPos, owner, facing) || restoredAt(level, requiredPos, owner, facing);
    }

    private static void tell(Player player, Component message) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
#if MC_VER >= 26100
        serverPlayer.sendOverlayMessage(message.copy().withStyle(ChatFormatting.GOLD));
#else
        serverPlayer.displayClientMessage(message.copy().withStyle(ChatFormatting.GOLD), true);
#endif
    }
}
