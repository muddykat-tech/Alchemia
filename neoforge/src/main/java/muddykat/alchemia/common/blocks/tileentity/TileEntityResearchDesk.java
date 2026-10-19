package muddykat.alchemia.common.blocks.tileentity;

import muddykat.alchemia.common.blocks.blockentity.BlockResearchDesk;
import muddykat.alchemia.common.guide.GuideHolder;
import muddykat.alchemia.registration.registers.BlockEntityTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileEntityResearchDesk extends SyncedBlockEntity implements GuideHolder {

    private static final int GLYPHS = 14;
    private static final double BOOK_HEIGHT = 1.05;

    private ItemStack guide = ItemStack.EMPTY;

    public TileEntityResearchDesk(BlockPos pos, BlockState state) {
        super(BlockEntityTypeRegistry.RESEARCH_DESK.get(), pos, state);
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

    public boolean hasGuide() {
        return !guide.isEmpty();
    }

    public void setGuide(ItemStack stack) {
        guide = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            if (state.getBlock() instanceof BlockResearchDesk && state.getValue(BlockResearchDesk.HAS_GUIDE) != hasGuide()) {
                level.setBlock(worldPosition, state.setValue(BlockResearchDesk.HAS_GUIDE, hasGuide()), Block.UPDATE_ALL);
            }
        }
        sync();
    }

    public ItemStack takeGuide() {
        ItemStack taken = guide;
        setGuide(ItemStack.EMPTY);
        return taken;
    }

    @Override
    public void guideChanged() {
        sync();
    }

    public void inscribed(BlockPos from) {
        guideChanged();
        if (!(level instanceof ServerLevel server)) return;

        double x = worldPosition.getX() + 0.5;
        double y = worldPosition.getY() + BOOK_HEIGHT;
        double z = worldPosition.getZ() + 0.5;
        double dx = from.getX() + 0.5 - x;
        double dy = from.getY() + 1.0 - y;
        double dz = from.getZ() + 0.5 - z;

        for (int glyph = 0; glyph < GLYPHS; glyph++) {
            double jitter = 0.35;
            server.sendParticles(ParticleTypes.ENCHANT, x, y, z, 0,
                    dx + (server.getRandom().nextDouble() - 0.5) * jitter,
                    dy + server.getRandom().nextDouble() * jitter,
                    dz + (server.getRandom().nextDouble() - 0.5) * jitter, 1.0);
        }
        server.playSound(null, worldPosition, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 0.8F,
                0.9F + server.getRandom().nextFloat() * 0.2F);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null && !guide.isEmpty()) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, guide);
        }
        guide = ItemStack.EMPTY;
    }
}
