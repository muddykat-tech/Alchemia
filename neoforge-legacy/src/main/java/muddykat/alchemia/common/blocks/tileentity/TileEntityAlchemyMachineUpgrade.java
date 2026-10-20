package muddykat.alchemia.common.blocks.tileentity;

import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyMachineCore;
import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyMachineUpgrade;
import muddykat.alchemia.common.blocks.helper.UpgradeSide;
import muddykat.alchemia.registration.registers.BlockEntityTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.ItemStackHandler;
import javax.annotation.Nullable;

public class TileEntityAlchemyMachineUpgrade extends SyncedBlockEntity {

    private final UpgradeSide side;
    private final ItemStackHandler potions;

    public TileEntityAlchemyMachineUpgrade(BlockPos pos, BlockState state) {
        super(BlockEntityTypeRegistry.ALCHEMY_MACHINE_UPGRADE.get(), pos, state);
        this.side = state.getBlock() instanceof BlockAlchemyMachineUpgrade upgrade ? upgrade.getSide() : UpgradeSide.RIGHT;
        this.potions = new ItemStackHandler(this.side.potionSlots()) {
            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                sync();
            }
        };
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Potions")) potions.deserializeNBT(registries, tag.getCompound("Potions"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Potions", potions.serializeNBT(registries));
    }

    public UpgradeSide getSide() {
        return side;
    }

    public ItemStackHandler getPotions() {
        return potions;
    }

    public int slotCount() {
        return potions.getSlots();
    }

    public Direction facing() {
        return getBlockState().getValue(BlockAlchemyMachineUpgrade.FACING);
    }

    public ItemStack getStack(int slot) {
        return potions.getStackInSlot(slot);
    }

    public void clearSlot(int slot) {
        potions.setStackInSlot(slot, ItemStack.EMPTY);
    }

    public @Nullable TileEntityAlchemyMachineCore getCore() {
        if (level == null) return null;
        Direction facing = facing();
        BlockPos corePos = worldPosition.relative(side.directionFrom(facing).getOpposite());
        return level.getBlockEntity(corePos) instanceof TileEntityAlchemyMachineCore core
                && core.getBlockState().getValue(BlockAlchemyMachineCore.FACING) == facing ? core : null;
    }

    public static boolean isPotion(ItemStack stack) {
        return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION);
    }

    public int slotForHit(BlockHitResult hit) {
        for (int slot = 0; slot < potions.getSlots(); slot++) {
            if (potions.getStackInSlot(slot).isEmpty()) return slot;
        }
        return -1;
    }

    public InteractionResult insertPotion(Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!isPotion(held)) return InteractionResult.PASS;

        int slot = slotForHit(hit);
        if (slot < 0) return InteractionResult.PASS;

        if (level != null && !level.isClientSide()) {
            potions.setStackInSlot(slot, held.copyWithCount(1));
            held.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    public void dropContents(BlockPos pos) {
        if (level == null) return;
        NonNullList<ItemStack> contents = NonNullList.withSize(potions.getSlots(), ItemStack.EMPTY);
        for (int slot = 0; slot < potions.getSlots(); slot++) contents.set(slot, potions.getStackInSlot(slot));
        Containers.dropContents(level, pos, contents);
    }

    @Override
    public InteractionResult onActivated(BlockState state, BlockPos pos, Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }
}
