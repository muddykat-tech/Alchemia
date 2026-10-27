package muddykat.alchemia.common.blocks.tileentity.container;

import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.helper.IngredientTool;
import muddykat.alchemia.registration.registers.BlockRegistry;
import muddykat.alchemia.registration.registers.MenuTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.Objects;

public class AlchemicalCauldronMenu extends AbstractContainerMenu {

    public static final int CAULDRON_SLOTS = 2;
    private static final int MENU_ACTIVE_SLOT = 0;
    private static final int MENU_TOOL_SLOT = 1;

    public static final int ACTIVE_SLOT_X = 18;
    public static final int TOOL_SLOT_X = 40;
    public static final int TOOL_SLOT_Y = 184;

    public static final int SLOT_ROW_Y = 184;
    public static final int INVENTORY_X = 105;
    public static final int INVENTORY_Y = 258;
    public static final int HOTBAR_Y = 316;

    private final ContainerData cauldronData;
    private final TileEntityAlchemyCauldron alchemyCauldron;
    private final ContainerLevelAccess containerAccess;

    public AlchemicalCauldronMenu(final int windowId, final Inventory playerInventory, final TileEntityAlchemyCauldron cauldron, ContainerData cauldronData) {
        super(MenuTypeRegistry.ALCHEMICAL_CAULDRON.get(), windowId);
        this.alchemyCauldron = cauldron;
        this.cauldronData = cauldronData;
        this.containerAccess = ContainerLevelAccess.create(cauldron.getLevel(), cauldron.getBlockPos());

        ItemStackHandler handler = cauldron.getInventory();
        addSlot(new SlotItemHandler(handler, TileEntityAlchemyCauldron.ACTIVE_SLOT,
                ACTIVE_SLOT_X + 1, SLOT_ROW_Y + 1) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return cauldron.getWaterLevel() > 0 && TileEntityAlchemyCauldron.isAddable(stack);
            }
        });

        addSlot(new SlotItemHandler(handler, TileEntityAlchemyCauldron.TOOL_SLOT,
                TOOL_SLOT_X + 1, TOOL_SLOT_Y + 1) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return IngredientTool.of(stack) != null;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, INVENTORY_X + column * 18 + 1, INVENTORY_Y + row * 18 + 1));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, INVENTORY_X + column * 18 + 1, HOTBAR_Y + 1));
        }

        addDataSlots(cauldronData);
        if (!playerInventory.player.level().isClientSide()) cauldron.revealFor(playerInventory.player);
    }

    public AlchemicalCauldronMenu(final int windowID, final Inventory playerInventory, final FriendlyByteBuf data) {
        this(windowID, playerInventory, getBlockEntity(playerInventory, data), new SimpleContainerData(TileEntityAlchemyCauldron.DATA_COUNT));
    }

    private static TileEntityAlchemyCauldron getBlockEntity(final Inventory playerInventory, final FriendlyByteBuf data) {
        Objects.requireNonNull(playerInventory, "playerInventory cannot be null");
        Objects.requireNonNull(data, "data cannot be null");
        final BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(data.readBlockPos());
        if (blockEntity instanceof TileEntityAlchemyCauldron cauldron) {
            return cauldron;
        }
        throw new IllegalStateException("Block entity is not correct! " + blockEntity);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (slotIndex < CAULDRON_SLOTS) {
            if (!moveItemStackTo(stack, CAULDRON_SLOTS, this.slots.size(), true)) return ItemStack.EMPTY;
        } else if (alchemyCauldron.getWaterLevel() > 0 && TileEntityAlchemyCauldron.isAddable(stack)) {
            if (!moveItemStackTo(stack, MENU_ACTIVE_SLOT, MENU_ACTIVE_SLOT + 1, false)) return ItemStack.EMPTY;
        } else if (IngredientTool.of(stack) != null) {
            if (!moveItemStackTo(stack, MENU_TOOL_SLOT, MENU_TOOL_SLOT + 1, false)) return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return original;
    }

    public static final int BUTTON_BREW = 0;
    public static final int BUTTON_ADD = 1;
    public static final int BUTTON_APPLY_TOOL = 2;
    public static final int BUTTON_STIR = 3;
    public static final int BUTTON_STIR_FAST = 4;
    public static final int BUTTON_UNQUEUE = 5;
    public static final int BUTTON_APPLY_TOOL_FAST = 64;
    public static final int BUTTON_STIR_MANY = 80;
    public static final int BUTTON_HEAT = 90;
    public static final int MAX_STIRS_PER_CLICK = 5;

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId == BUTTON_BREW) {
            alchemyCauldron.brew(player);
            return true;
        }
        if (buttonId == BUTTON_STIR || buttonId == BUTTON_STIR_FAST) {
            alchemyCauldron.stir(player, buttonId == BUTTON_STIR_FAST ? TileEntityAlchemyCauldron.FAST_STIRS : 1);
            return true;
        }
        if (buttonId > BUTTON_STIR_MANY && buttonId <= BUTTON_STIR_MANY + MAX_STIRS_PER_CLICK) {
            alchemyCauldron.stir(player, buttonId - BUTTON_STIR_MANY);
            return true;
        }
        if (buttonId == BUTTON_HEAT) {
            alchemyCauldron.stoke(player);
            return true;
        }
        if (buttonId == BUTTON_ADD) {
            alchemyCauldron.queueActive(player);
            return true;
        }
        if (buttonId == BUTTON_APPLY_TOOL || buttonId == BUTTON_APPLY_TOOL_FAST) {
            alchemyCauldron.applyTool(player, buttonId == BUTTON_APPLY_TOOL_FAST);
            return true;
        }
        if (buttonId >= BUTTON_UNQUEUE && buttonId < BUTTON_UNQUEUE + TileEntityAlchemyCauldron.MAX_QUEUE) {
            alchemyCauldron.unqueue(player, buttonId - BUTTON_UNQUEUE);
            return true;
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(containerAccess, player, BlockRegistry.BLOCK_REGISTRY.get("alchemical_cauldron").get());
    }

    public ContainerData getCauldronData() {
        return cauldronData;
    }

    public TileEntityAlchemyCauldron getCauldron() {
        return alchemyCauldron;
    }
}
