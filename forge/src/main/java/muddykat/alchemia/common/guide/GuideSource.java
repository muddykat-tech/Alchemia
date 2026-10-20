package muddykat.alchemia.common.guide;

import muddykat.alchemia.common.items.ItemAlchemiaGuide;
import muddykat.alchemia.common.items.ItemRecipePage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record GuideSource(boolean block, boolean mainHand, BlockPos pos) {

    private static final double REACH_BUFFER = 1.0;

    public void write(FriendlyByteBuf buffer) {
        buffer.writeBoolean(block);
        buffer.writeBoolean(mainHand);
        buffer.writeBlockPos(pos);
    }

    public static GuideSource read(FriendlyByteBuf buffer) {
        return new GuideSource(buffer.readBoolean(), buffer.readBoolean(), buffer.readBlockPos());
    }

    public static GuideSource hand(InteractionHand hand) {
        return new GuideSource(false, hand == InteractionHand.MAIN_HAND, BlockPos.ZERO);
    }

    public static GuideSource block(BlockPos pos) {
        return new GuideSource(true, true, pos.immutable());
    }

    public InteractionHand hand() {
        return mainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    public ItemStack resolve(Player player) {
        if (player == null) return ItemStack.EMPTY;
        if (!block) return player.getItemInHand(hand());
        if (!player.canReach(pos, REACH_BUFFER)) return ItemStack.EMPTY;
        return player.level().getBlockEntity(pos) instanceof GuideHolder holder ? holder.guide() : ItemStack.EMPTY;
    }

    public ItemStack guide(Player player) {
        ItemStack stack = resolve(player);
        return stack.getItem() instanceof ItemAlchemiaGuide ? stack : ItemStack.EMPTY;
    }

    public ItemStack page(Player player) {
        ItemStack stack = resolve(player);
        return stack.getItem() instanceof ItemRecipePage ? stack : ItemStack.EMPTY;
    }

    public void changed(Player player) {
        if (block && player.level().getBlockEntity(pos) instanceof GuideHolder holder) holder.guideChanged();
    }
}
