package muddykat.alchemia.common.blocks.tileentity;

import muddykat.alchemia.common.potion.PotionGlasses;
import muddykat.alchemia.registration.registers.BlockEntityTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

public class TileEntityPlacedPotions extends SyncedBlockEntity {

    private final List<ItemStack> potions = new ArrayList<>();

    public TileEntityPlacedPotions(BlockPos pos, BlockState state) {
        super(BlockEntityTypeRegistry.PLACED_POTIONS.get(), pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        potions.clear();
        potions.addAll(input.read("Potions", ItemStack.CODEC.listOf()).orElse(List.of()));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("Potions", ItemStack.CODEC.listOf(), List.copyOf(potions));
    }

    public List<ItemStack> contents() {
        return new ArrayList<>(potions);
    }

    public void setContents(List<ItemStack> contents) {
        potions.clear();
        potions.addAll(contents);
        sync();
    }

    public OptionalInt liquidColor(int slot) {
        if (slot >= potions.size() || !PotionGlasses.isPotion(potions.get(slot))) return OptionalInt.empty();
        return OptionalInt.of(PotionGlasses.liquidColor(potions.get(slot)));
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level == null) return;
        for (ItemStack potion : potions) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), potion);
        }
        potions.clear();
    }
}
