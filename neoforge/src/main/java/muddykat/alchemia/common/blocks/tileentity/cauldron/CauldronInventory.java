package muddykat.alchemia.common.blocks.tileentity.cauldron;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

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
    private ItemStacksResourceHandler slots = create();

    public CauldronInventory(int maxQueue) {
        this.maxQueue = maxQueue;
    }

    public ItemStacksResourceHandler slots() {
        return slots;
    }

    public void load(ValueInput input, List<ItemStack> legacyQueue) {
        input.child("Inventory").ifPresent(saved -> {
            slots.deserialize(saved);
            slots = resize(slots, legacyQueue);
        });
    }

    public void save(ValueOutput output) {
        slots.serialize(output.child("Inventory"));
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
        return slots.copyToList();
    }

    public boolean returnRetired(Level level, BlockPos pos) {
        boolean changed = false;
        for (int slot : new int[]{RETIRED_REDSTONE_SLOT, RETIRED_GUNPOWDER_SLOT}) {
            int amount = slots.getAmountAsInt(slot);
            if (amount <= 0) continue;

            ItemStack leftover = slots.getResource(slot).toStack(amount);
            slots.set(slot, ItemResource.EMPTY, 0);
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, leftover);
            changed = true;
        }
        return changed;
    }

    private ItemStack stackIn(int slot) {
        int amount = slots.getAmountAsInt(slot);
        return amount <= 0 ? ItemStack.EMPTY : slots.getResource(slot).toStack(amount);
    }

    private void put(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            slots.set(slot, ItemResource.EMPTY, 0);
        } else {
            slots.set(slot, ItemResource.of(stack), stack.getCount());
        }
    }

    private ItemStacksResourceHandler resize(ItemStacksResourceHandler saved, List<ItemStack> queue) {
        if (saved.size() == SLOT_COUNT) return saved;

        ItemStacksResourceHandler resized = create();
        if (saved.size() < LEGACY_SLOT_COUNT) {
            for (int slot = 0; slot < Math.min(saved.size(), SLOT_COUNT); slot++) {
                resized.set(slot, saved.getResource(slot), saved.getAmountAsInt(slot));
            }
            return resized;
        }

        resized.set(ACTIVE_SLOT, saved.getResource(0), saved.getAmountAsInt(0));
        resized.set(RETIRED_REDSTONE_SLOT, saved.getResource(LEGACY_REDSTONE_SLOT), saved.getAmountAsInt(LEGACY_REDSTONE_SLOT));
        resized.set(RETIRED_GUNPOWDER_SLOT, saved.getResource(LEGACY_GUNPOWDER_SLOT), saved.getAmountAsInt(LEGACY_GUNPOWDER_SLOT));

        for (int slot = 1; slot < LEGACY_REDSTONE_SLOT; slot++) {
            ItemResource resource = saved.getResource(slot);
            if (resource.isEmpty()) continue;

            int amount = saved.getAmountAsInt(slot);
            for (int copy = 0; copy < amount && queue.size() < maxQueue; copy++) {
                queue.add(resource.toStack(1));
            }
        }
        return resized;
    }

    private static ItemStacksResourceHandler create() {
        return new ItemStacksResourceHandler(SLOT_COUNT) {
            @Override
            protected int getCapacity(int index, ItemResource resource) {
                if (index == RETIRED_REDSTONE_SLOT || index == RETIRED_GUNPOWDER_SLOT) return 0;
                return index == ACTIVE_SLOT ? 64 : 1;
            }
        };
    }
}
