package muddykat.alchemia.common.blocks.tileentity;

import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyMachineCore;
import muddykat.alchemia.common.blocks.helper.UpgradeSide;
import muddykat.alchemia.common.blocks.tileentity.container.AlchemyMachineMenu;
import muddykat.alchemia.common.crafting.MagnumOpus;
import muddykat.alchemia.common.crafting.PotionEffectIngredient;
import muddykat.alchemia.common.utility.TextUtils;
import muddykat.alchemia.registration.registers.BlockEntityTypeRegistry;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TileEntityAlchemyMachineCore extends SyncedBlockEntity implements MenuProvider {

    public static final int INPUT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int CORE_SLOT_COUNT = 3;

    public static final int COOK_DURATION = 200;
    public static final int DATA_COUNT = 4;

    private ItemStacksResourceHandler inventory;
    private int litTime;
    private int litDuration;
    private int cookTime;

    protected final ContainerData machineData;

    public record PotionRef(TileEntityAlchemyMachineUpgrade upgrade, int slot, ItemStack stack) {}

    public record Match(MagnumOpus.Stage stage, int[] assignment, List<PotionRef> available, ItemStack result) {}

    public TileEntityAlchemyMachineCore(BlockPos pos, BlockState state) {
        this(BlockEntityTypeRegistry.ALCHEMY_MACHINE_CORE.get(), pos, state);
    }

    public TileEntityAlchemyMachineCore(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        inventory = createHandler();
        machineData = createIntArray();
    }

    private ItemStacksResourceHandler createHandler() {
        return new ItemStacksResourceHandler(CORE_SLOT_COUNT) {
            @Override
            protected void onContentsChanged(int index, ItemStack previousContents) {
                setChanged();
            }
        };
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.child("Inventory").ifPresent(saved -> inventory.deserialize(saved));
        litTime = input.getIntOr("litTime", 0);
        litDuration = input.getIntOr("litDuration", 0);
        cookTime = input.getIntOr("cookTime", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        inventory.serialize(output.child("Inventory"));
        output.putInt("litTime", litTime);
        output.putInt("litDuration", litDuration);
        output.putInt("cookTime", cookTime);
    }

    public ItemStacksResourceHandler getInventory() {
        return inventory;
    }

    public ContainerData getMachineData() {
        return machineData;
    }

    public boolean isLit() {
        return litTime > 0;
    }

    public ItemStack getStack(int slot) {
        return inventory.getResource(slot).toStack(inventory.getAmountAsInt(slot));
    }

    public @Nullable TileEntityAlchemyMachineUpgrade getUpgrade(UpgradeSide side) {
        if (level == null) return null;
        Direction facing = getBlockState().getValue(BlockAlchemyMachineCore.FACING);
        BlockPos target = worldPosition.relative(side.directionFrom(facing));
        return level.getBlockEntity(target) instanceof TileEntityAlchemyMachineUpgrade upgrade
                && upgrade.getSide() == side
                && upgrade.facing() == facing ? upgrade : null;
    }

    public List<TileEntityAlchemyMachineUpgrade> attachedUpgrades() {
        List<TileEntityAlchemyMachineUpgrade> attached = new ArrayList<>();
        for (UpgradeSide side : UpgradeSide.values()) {
            TileEntityAlchemyMachineUpgrade upgrade = getUpgrade(side);
            if (upgrade != null) attached.add(upgrade);
        }
        return attached;
    }

    public int potionSlotCount() {
        int total = 0;
        for (TileEntityAlchemyMachineUpgrade upgrade : attachedUpgrades()) total += upgrade.slotCount();
        return total;
    }

    private List<PotionRef> gatherPotions() {
        List<PotionRef> refs = new ArrayList<>();
        for (TileEntityAlchemyMachineUpgrade upgrade : attachedUpgrades()) {
            for (int slot = 0; slot < upgrade.slotCount(); slot++) {
                ItemStack stack = upgrade.getStack(slot);
                if (!stack.isEmpty()) refs.add(new PotionRef(upgrade, slot, stack));
            }
        }
        return refs;
    }

    public @Nullable Match findMatch() {
        List<PotionRef> available = gatherPotions();
        if (available.isEmpty()) return null;

        ItemStack input = getStack(INPUT_SLOT);

        for (MagnumOpus.Stage stage : MagnumOpus.STAGES) {
            if (stage.potionCount() != available.size()) continue;
            if (!inputMatches(stage, input)) continue;

            int[] assignment = matchPotions(stage.potions(), available);
            if (assignment == null) continue;

            ItemStack result = new ItemStack(ItemRegistry.getItemFromRegistry(stage.result()));
            if (!canOutput(result)) continue;

            return new Match(stage, assignment, available, result);
        }
        return null;
    }

    private boolean inputMatches(MagnumOpus.Stage stage, ItemStack input) {
        if (stage.previous() == null) return input.isEmpty();
        return !input.isEmpty() && input.is(ItemRegistry.getItemFromRegistry(stage.previous()).asItem());
    }

    private static int @Nullable [] matchPotions(List<PotionEffectIngredient> required, List<PotionRef> available) {
        int[] assignment = new int[required.size()];
        Arrays.fill(assignment, -1);
        return assign(0, required, available, assignment, new boolean[available.size()]) ? assignment : null;
    }

    private static boolean assign(int index, List<PotionEffectIngredient> required, List<PotionRef> available,
                                  int[] assignment, boolean[] used) {
        if (index == required.size()) return true;
        PotionEffectIngredient ingredient = required.get(index);
        for (int candidate = 0; candidate < available.size(); candidate++) {
            if (used[candidate] || !ingredient.test(available.get(candidate).stack())) continue;
            used[candidate] = true;
            assignment[index] = candidate;
            if (assign(index + 1, required, available, assignment, used)) return true;
            used[candidate] = false;
            assignment[index] = -1;
        }
        return false;
    }

    private boolean canOutput(ItemStack result) {
        ItemStack existing = getStack(OUTPUT_SLOT);
        if (existing.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(existing, result)) return false;
        return existing.getCount() + result.getCount() <= existing.getMaxStackSize();
    }

    public void tick() {
        if (level == null || level.isClientSide()) return;

        boolean wasLit = isLit();
        boolean changed = false;

        if (isLit()) {
            litTime--;
            changed = true;
        }

        Match match = findMatch();

        if (match != null) {
            if (!isLit() && consumeFuel()) changed = true;

            if (isLit()) {
                cookTime++;
                if (cookTime >= COOK_DURATION) {
                    craft(match);
                    cookTime = 0;
                }
                changed = true;
            } else if (cookTime != 0) {
                cookTime = 0;
                changed = true;
            }
        } else if (cookTime != 0) {
            cookTime = 0;
            changed = true;
        }

        if (wasLit != isLit()) changed = true;
        if (changed) setChanged();
    }

    private boolean consumeFuel() {
        if (level == null) return false;
        ItemStack fuel = getStack(FUEL_SLOT);
        if (fuel.isEmpty()) return false;

        int duration = level.fuelValues().burnDuration(fuel);
        if (duration <= 0) return false;

        setSlotAmount(FUEL_SLOT, inventory.getAmountAsInt(FUEL_SLOT) - 1);
        litTime = duration;
        litDuration = duration;
        return true;
    }

    private void craft(Match match) {
        for (int index : match.assignment()) {
            PotionRef ref = match.available().get(index);
            ref.upgrade().clearSlot(ref.slot());
        }

        if (match.stage().previous() != null) {
            setSlotAmount(INPUT_SLOT, inventory.getAmountAsInt(INPUT_SLOT) - 1);
        }

        ItemStack existing = getStack(OUTPUT_SLOT);
        if (existing.isEmpty()) {
            inventory.set(OUTPUT_SLOT, ItemResource.of(match.result()), match.result().getCount());
        } else {
            inventory.set(OUTPUT_SLOT, inventory.getResource(OUTPUT_SLOT), existing.getCount() + match.result().getCount());
        }

        sync();
    }

    private void setSlotAmount(int slot, int amount) {
        if (amount <= 0) {
            inventory.set(slot, ItemResource.EMPTY, 0);
        } else {
            inventory.set(slot, inventory.getResource(slot), amount);
        }
    }

    private ContainerData createIntArray() {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> TileEntityAlchemyMachineCore.this.litTime;
                    case 1 -> TileEntityAlchemyMachineCore.this.litDuration;
                    case 2 -> TileEntityAlchemyMachineCore.this.cookTime;
                    case 3 -> COOK_DURATION;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> TileEntityAlchemyMachineCore.this.litTime = value;
                    case 1 -> TileEntityAlchemyMachineCore.this.litDuration = value;
                    case 2 -> TileEntityAlchemyMachineCore.this.cookTime = value;
                }
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null) Containers.dropContents(level, pos, inventory.copyToList());
    }

    @Override
    public Component getDisplayName() {
        return TextUtils.getTranslation("container.alchemy_machine_core");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new AlchemyMachineMenu(id, playerInventory, this, machineData);
    }
}
