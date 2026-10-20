package muddykat.alchemia.common.blocks.tileentity;

import muddykat.alchemia.common.blocks.blockentity.BlockPlacedGuide;
import muddykat.alchemia.common.guide.GuideHolder;
import muddykat.alchemia.registration.registers.BlockEntityTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;

public class TileEntityPlacedGuide extends SyncedBlockEntity implements GuideHolder {

    private ItemStack guide = ItemStack.EMPTY;

    public TileEntityPlacedGuide(BlockPos pos, BlockState state) {
        super(BlockEntityTypeRegistry.PLACED_GUIDE.get(), pos, state);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        guide = tag.contains("Guide") ? ItemStack.parseOptional(registries, tag.getCompound("Guide")) : ItemStack.EMPTY;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!guide.isEmpty()) tag.put("Guide", guide.saveOptional(registries));
    }

    @Override
    public ItemStack guide() {
        return guide;
    }

    public void setGuide(ItemStack stack) {
        guide = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
        guideChanged();
    }

    public ItemStack takeGuide() {
        ItemStack taken = guide;
        guide = ItemStack.EMPTY;
        sync();
        return taken;
    }

    @Override
    public void guideChanged() {
        if (level != null && !level.isClientSide() && !guide.isEmpty()) {
            BlockState state = getBlockState();
            BlockState sized = BlockPlacedGuide.sizedFor(state, guide);
            if (sized != state) level.setBlock(worldPosition, sized, Block.UPDATE_ALL);
        }
        sync();
    }

    public void dropGuide() {
        if (level != null && !guide.isEmpty()) {
            Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.2, worldPosition.getZ() + 0.5, guide);
        }
        guide = ItemStack.EMPTY;
    }
}
