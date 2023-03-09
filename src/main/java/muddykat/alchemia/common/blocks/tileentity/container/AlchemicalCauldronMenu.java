package muddykat.alchemia.common.blocks.tileentity.container;

import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.items.ItemAlchemiaGuide;
import muddykat.alchemia.registration.registers.BlockRegister;
import muddykat.alchemia.registration.registers.MenuTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.blamejared.crafttweaker.natives.loot.ExpandLootContext.getTileEntity;

public class AlchemicalCauldronMenu extends RecipeBookMenu<RecipeWrapper> {

    private final ContainerData cauldronData;
    private final TileEntityAlchemyCauldron alchemyCauldron;
    private final ContainerLevelAccess containerAccess;
    public AlchemicalCauldronMenu(final int windowId, final Inventory playerInventory, final TileEntityAlchemyCauldron cauldron, ContainerData cauldronData) {
        super(MenuTypeRegistry.ALCHEMICAL_CAULDRON.get(), windowId);
        this.alchemyCauldron = cauldron;
        this.cauldronData = cauldronData;
        this.containerAccess = ContainerLevelAccess.create(cauldron.getLevel(), cauldron.getBlockPos());
    }
    public AlchemicalCauldronMenu(final int windowID, final Inventory playerInventory, final FriendlyByteBuf data){
        this(windowID, playerInventory, getTileEntity(playerInventory, data), new SimpleContainerData(4));
    }
    @Override
    public void fillCraftSlotsStackedContents(StackedContents pItemHelper) {

    }

    @Override
    public void clearCraftingContent() {

    }

    @Override
    public boolean recipeMatches(Recipe<? super RecipeWrapper> pRecipe) {
        return false;
    }

    @Override
    public int getResultSlotIndex() {
        return 0;
    }

    @Override
    public int getGridWidth() {
        return 0;
    }

    @Override
    public int getGridHeight() {
        return 0;
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return null;
    }

    @Override
    public boolean shouldMoveToInventory(int pSlotIndex) {
        return false;
    }

    private static TileEntityAlchemyCauldron getTileEntity(final Inventory playerInventory, final FriendlyByteBuf data) {
        Objects.requireNonNull(playerInventory, "playerInventory cannot be null");
        Objects.requireNonNull(data, "data cannot be null");
        final BlockEntity tileAtPos = playerInventory.player.level.getBlockEntity(data.readBlockPos());
        if (tileAtPos instanceof TileEntityAlchemyCauldron) {
            return (TileEntityAlchemyCauldron) tileAtPos;
        }
        throw new IllegalStateException("Tile entity is not correct! " + tileAtPos);
    }

    @Override
    public boolean stillValid(@NotNull Player playerIn) {
        return stillValid(containerAccess, playerIn, BlockRegister.BLOCK_REGISTRY.get("alchemical_cauldron").get());
    }

    public ContainerData getCauldronData() {
        return cauldronData;
    }

    public TileEntityAlchemyCauldron getCauldron() {
        return alchemyCauldron;
    }
}
