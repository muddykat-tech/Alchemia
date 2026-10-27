package muddykat.alchemia.common.blocks.tileentity.cauldron;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import muddykat.alchemia.common.utility.ValueInput;
import muddykat.alchemia.common.utility.ValueOutput;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

public final class CauldronInventory {

    public static final int ACTIVE_SLOT = 0;
    public static final int TOOL_SLOT = 3;

    private static final int RETIRED_REDSTONE_SLOT = 1;
    private static final int RETIRED_GUNPOWDER_SLOT = 2;
    private static final int SLOT_COUNT = 4;
    private static final int LEGACY_SLOT_COUNT = 8;
    private static final int LEGACY_REDSTONE_SLOT = 6;
    private static final int LEGACY_GUNPOWDER_SLOT = 7;

    private final int maxQueue;
    private ItemStackHandler slots = create();

    public CauldronInventory(int maxQueue) {
        this.maxQueue = maxQueue;
    }

    public ItemStackHandler slots() {
        return slots;
    }

    public void load(ValueInput input, List<ItemStack> legacyQueue) {
        input.child("Inventory").ifPresent(saved -> {
            slots.deserializeNBT(saved.tag());
            slots = resize(slots, legacyQueue);
        });
    }

    public void save(ValueOutput output) {
        output.tag().put("Inventory", slots.serializeNBT());
    }

    public ItemStack active() {
        return stackIn(ACTIVE_SLOT);
    }

    public ItemStack tool() {
        return stackIn(TOOL_SLOT);
    }

    public void setActive(ItemStack stack) {
        put(ACTIVE_SLOT, stack);
    }

    public void setTool(ItemStack stack) {
        put(TOOL_SLOT, stack);
    }

    public List<ItemStack> contents() {
        List<ItemStack> contents = new java.util.ArrayList<>();
        for (int slot = 0; slot < slots.getSlots(); slot++) contents.add(slots.getStackInSlot(slot).copy());
        return contents;
    }

    public boolean returnRetired(Level level, BlockPos pos) {
        boolean changed = false;
        for (int slot : new int[]{RETIRED_REDSTONE_SLOT, RETIRED_GUNPOWDER_SLOT}) {
            ItemStack leftover = slots.getStackInSlot(slot);
            if (leftover.isEmpty()) continue;

            slots.setStackInSlot(slot, ItemStack.EMPTY);
            leftover = leftover.copy();
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, leftover);
            changed = true;
        }
        return changed;
    }

    private ItemStack stackIn(int slot) {
        return slots.getStackInSlot(slot).copy();
    }

    private void put(int slot, ItemStack stack) {
        slots.setStackInSlot(slot, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
    }

    private ItemStackHandler resize(ItemStackHandler saved, List<ItemStack> queue) {
        if (saved.getSlots() == SLOT_COUNT) return saved;

        ItemStackHandler resized = create();
        if (saved.getSlots() < LEGACY_SLOT_COUNT) {
            for (int slot = 0; slot < Math.min(saved.getSlots(), SLOT_COUNT); slot++) {
                resized.setStackInSlot(slot, saved.getStackInSlot(slot));
            }
            return resized;
        }

        resized.setStackInSlot(ACTIVE_SLOT, saved.getStackInSlot(0));
        resized.setStackInSlot(RETIRED_REDSTONE_SLOT, saved.getStackInSlot(LEGACY_REDSTONE_SLOT));
        resized.setStackInSlot(RETIRED_GUNPOWDER_SLOT, saved.getStackInSlot(LEGACY_GUNPOWDER_SLOT));

        for (int slot = 1; slot < LEGACY_REDSTONE_SLOT; slot++) {
            ItemStack stack = saved.getStackInSlot(slot);
            for (int copy = 0; copy < stack.getCount() && queue.size() < maxQueue; copy++) {
                queue.add(stack.copyWithCount(1));
            }
        }
        return resized;
    }

    private static ItemStackHandler create() {
        return new ItemStackHandler(SLOT_COUNT) {
            @Override
            public int getSlotLimit(int slot) {
                if (slot == RETIRED_REDSTONE_SLOT || slot == RETIRED_GUNPOWDER_SLOT) return 0;
                return slot == ACTIVE_SLOT ? 64 : 1;
            }
        };
    }
}
