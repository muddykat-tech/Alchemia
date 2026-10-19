package muddykat.alchemia.common.blocks.tileentity;

import muddykat.alchemia.common.blocks.blockentity.BlockPlacedGuide;
import muddykat.alchemia.common.guide.GuideHolder;
import muddykat.alchemia.registration.registers.BlockEntityTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileEntityPlacedGuide extends SyncedBlockEntity implements GuideHolder {

    private ItemStack guide = ItemStack.EMPTY;

    public TileEntityPlacedGuide(BlockPos pos, BlockState state) {
        super(BlockEntityTypeRegistry.PLACED_GUIDE.get(), pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        guide = input.read("Guide", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!guide.isEmpty()) output.store("Guide", ItemStack.CODEC, guide);
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

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null && !guide.isEmpty()) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5, guide);
        }
        guide = ItemStack.EMPTY;
    }
}
