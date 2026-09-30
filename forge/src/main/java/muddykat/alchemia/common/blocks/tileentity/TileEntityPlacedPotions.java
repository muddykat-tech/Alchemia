package muddykat.alchemia.common.blocks.tileentity;

import muddykat.alchemia.common.potion.PotionGlasses;
import muddykat.alchemia.registration.registers.BlockEntityTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

public class TileEntityPlacedPotions extends SyncedBlockEntity {

    private final List<ItemStack> potions = new ArrayList<>();

    public TileEntityPlacedPotions(BlockPos pos, BlockState state) {
        super(BlockEntityTypeRegistry.PLACED_POTIONS.get(), pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        potions.clear();
        ListTag list = tag.getList("Potions", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            ItemStack potion = ItemStack.of(list.getCompound(i));
            if (!potion.isEmpty()) potions.add(potion);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag list = new ListTag();
        for (ItemStack potion : potions) list.add(potion.save(new CompoundTag()));
        tag.put("Potions", list);
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

    public void dropContents() {
        if (level == null) return;
        for (ItemStack potion : potions) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), potion);
        }
        potions.clear();
    }
}
