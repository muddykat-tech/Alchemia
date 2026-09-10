package muddykat.alchemia.common.blocks.tileentity.container;

import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyMachineCore;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyMachineUpgrade;
import muddykat.alchemia.registration.registers.BlockRegistry;
import muddykat.alchemia.registration.registers.MenuTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

import java.util.List;
import java.util.Objects;

public class AlchemyMachineMenu extends AbstractContainerMenu {

    public static final int POTION_X = 50;
    public static final int POTION_Y = 20;
    public static final int SLOT_PITCH = 18;

    public static final int INPUT_X = 55;
    public static final int INPUT_Y = 54;
    public static final int FUEL_X = 55;
    public static final int FUEL_Y = 90;
    public static final int OUTPUT_X = 119;
    public static final int OUTPUT_Y = 72;

    public static final int INVENTORY_X = 15;
    public static final int INVENTORY_Y = 119;
    public static final int HOTBAR_Y = 178;

    private final TileEntityAlchemyMachineCore core;
    private final ContainerData machineData;
    private final ContainerLevelAccess containerAccess;
    private final int potionSlots;

    public AlchemyMachineMenu(final int windowId, final Inventory playerInventory, final TileEntityAlchemyMachineCore core, ContainerData machineData) {
        super(MenuTypeRegistry.ALCHEMY_MACHINE.get(), windowId);
        this.core = core;
        this.machineData = machineData;
        this.containerAccess = ContainerLevelAccess.create(core.getLevel(), core.getBlockPos());

        ItemStacksResourceHandler handler = core.getInventory();

        addSlot(new ResourceHandlerSlot(handler, handler::set, TileEntityAlchemyMachineCore.INPUT_SLOT, INPUT_X, INPUT_Y));

        addSlot(new ResourceHandlerSlot(handler, handler::set, TileEntityAlchemyMachineCore.FUEL_SLOT, FUEL_X, FUEL_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return core.getLevel() != null && core.getLevel().fuelValues().isFuel(stack);
            }
        });

        addSlot(new ResourceHandlerSlot(handler, handler::set, TileEntityAlchemyMachineCore.OUTPUT_SLOT, OUTPUT_X, OUTPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        List<TileEntityAlchemyMachineUpgrade> upgrades = core.attachedUpgrades();
        int added = 0;
        for (TileEntityAlchemyMachineUpgrade upgrade : upgrades) {
            ItemStacksResourceHandler potions = upgrade.getPotions();
            for (int slot = 0; slot < potions.size(); slot++) {
                addSlot(new ResourceHandlerSlot(potions, potions::set, slot, POTION_X + added * SLOT_PITCH, POTION_Y) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return TileEntityAlchemyMachineUpgrade.isPotion(stack);
                    }
                });
                added++;
            }
        }
        this.potionSlots = added;

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, INVENTORY_X + column * SLOT_PITCH, INVENTORY_Y + row * SLOT_PITCH));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, INVENTORY_X + column * SLOT_PITCH, HOTBAR_Y));
        }

        addDataSlots(machineData);
    }

    public AlchemyMachineMenu(final int windowID, final Inventory playerInventory, final RegistryFriendlyByteBuf data) {
        this(windowID, playerInventory, getBlockEntity(playerInventory, data),
                new SimpleContainerData(TileEntityAlchemyMachineCore.DATA_COUNT));
    }

    private static TileEntityAlchemyMachineCore getBlockEntity(final Inventory playerInventory, final RegistryFriendlyByteBuf data) {
        Objects.requireNonNull(playerInventory, "playerInventory cannot be null");
        Objects.requireNonNull(data, "data cannot be null");
        final BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(data.readBlockPos());
        if (blockEntity instanceof TileEntityAlchemyMachineCore machine) {
            return machine;
        }
        throw new IllegalStateException("Block entity is not correct! " + blockEntity);
    }

    public int getPotionSlots() {
        return potionSlots;
    }

    public int machineSlotCount() {
        return TileEntityAlchemyMachineCore.CORE_SLOT_COUNT + potionSlots;
    }

    public TileEntityAlchemyMachineCore getCore() {
        return core;
    }

    public int getLitTime() {
        return machineData.get(0);
    }

    public int getLitDuration() {
        return machineData.get(1);
    }

    public int getCookTime() {
        return machineData.get(2);
    }

    public int getCookDuration() {
        int duration = machineData.get(3);
        return duration == 0 ? TileEntityAlchemyMachineCore.COOK_DURATION : duration;
    }

    public boolean isLit() {
        return getLitTime() > 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        int machineSlots = machineSlotCount();
        int potionStart = TileEntityAlchemyMachineCore.CORE_SLOT_COUNT;

        if (slotIndex < machineSlots) {
            if (!moveItemStackTo(stack, machineSlots, this.slots.size(), true)) return ItemStack.EMPTY;
        } else if (potionSlots > 0 && TileEntityAlchemyMachineUpgrade.isPotion(stack)) {
            if (!moveItemStackTo(stack, potionStart, potionStart + potionSlots, false)) return ItemStack.EMPTY;
        } else if (core.getLevel() != null && core.getLevel().fuelValues().isFuel(stack)) {
            if (!moveItemStackTo(stack, TileEntityAlchemyMachineCore.FUEL_SLOT, TileEntityAlchemyMachineCore.FUEL_SLOT + 1, false)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(stack, TileEntityAlchemyMachineCore.INPUT_SLOT, TileEntityAlchemyMachineCore.INPUT_SLOT + 1, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(containerAccess, player, BlockRegistry.BLOCK_REGISTRY.get("alchemy_machine_core").get());
    }
}
