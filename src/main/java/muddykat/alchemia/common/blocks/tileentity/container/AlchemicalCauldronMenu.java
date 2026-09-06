package muddykat.alchemia.common.blocks.tileentity.container;

import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.registration.registers.BlockRegistry;
import muddykat.alchemia.registration.registers.MenuTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

import java.util.Objects;

public class AlchemicalCauldronMenu extends AbstractContainerMenu {

    public static final int INGREDIENT_SLOTS = 6;
    public static final int CAULDRON_SLOTS = 8;

    public static final int SPECIAL_SLOT_X = 57;
    public static final int SPECIAL_SLOT_GAP = 18;

    private static final int SLOT_ROW_X = 101;
    private static final int SLOT_ROW_Y = 181;
    private static final int INVENTORY_X = 74;
    private static final int INVENTORY_Y = 222;
    private static final int HOTBAR_Y = 280;

    private final ContainerData cauldronData;
    private final TileEntityAlchemyCauldron alchemyCauldron;
    private final ContainerLevelAccess containerAccess;

    public AlchemicalCauldronMenu(final int windowId, final Inventory playerInventory, final TileEntityAlchemyCauldron cauldron, ContainerData cauldronData) {
        super(MenuTypeRegistry.ALCHEMICAL_CAULDRON.get(), windowId);
        this.alchemyCauldron = cauldron;
        this.cauldronData = cauldronData;
        this.containerAccess = ContainerLevelAccess.create(cauldron.getLevel(), cauldron.getBlockPos());

        ItemStacksResourceHandler handler = cauldron.getInventory();
        for (int slot = 0; slot < INGREDIENT_SLOTS; slot++) {
            addSlot(new ResourceHandlerSlot(handler, handler::set, slot, SLOT_ROW_X + slot * 18 + 1, SLOT_ROW_Y + 1) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return TileEntityAlchemyCauldron.isBrewingInput(stack);
                }
            });
        }

        addSlot(new ResourceHandlerSlot(handler, handler::set, TileEntityAlchemyCauldron.REDSTONE_SLOT,
                SPECIAL_SLOT_X + 1, SLOT_ROW_Y + 1) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.REDSTONE);
            }
        });

        addSlot(new ResourceHandlerSlot(handler, handler::set, TileEntityAlchemyCauldron.GUNPOWDER_SLOT,
                SPECIAL_SLOT_X + SPECIAL_SLOT_GAP + 1, SLOT_ROW_Y + 1) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.GUNPOWDER);
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
    }

    public AlchemicalCauldronMenu(final int windowID, final Inventory playerInventory, final RegistryFriendlyByteBuf data) {
        this(windowID, playerInventory, getBlockEntity(playerInventory, data), new SimpleContainerData(3));
    }

    private static TileEntityAlchemyCauldron getBlockEntity(final Inventory playerInventory, final RegistryFriendlyByteBuf data) {
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
        } else if (TileEntityAlchemyCauldron.isBrewingInput(stack)) {
            if (!moveItemStackTo(stack, 0, INGREDIENT_SLOTS, false)) return ItemStack.EMPTY;
        } else if (stack.is(Items.REDSTONE)) {
            if (!moveItemStackTo(stack, INGREDIENT_SLOTS, INGREDIENT_SLOTS + 1, false)) return ItemStack.EMPTY;
        } else if (stack.is(Items.GUNPOWDER)) {
            if (!moveItemStackTo(stack, INGREDIENT_SLOTS + 1, CAULDRON_SLOTS, false)) return ItemStack.EMPTY;
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

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId == BUTTON_BREW) {
            alchemyCauldron.commitQueue(player);
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
