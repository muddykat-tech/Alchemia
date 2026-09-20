package muddykat.alchemia.common.network.packets;

import muddykat.alchemia.common.items.ItemAlchemiaGuide;
import muddykat.alchemia.common.potion.BrewRecipeBook;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PacketForgetRecipe(String recipe, boolean mainHand) {

    public static void encode(PacketForgetRecipe msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.recipe());
        buf.writeBoolean(msg.mainHand());
    }

    public static PacketForgetRecipe decode(FriendlyByteBuf buf) {
        return new PacketForgetRecipe(buf.readUtf(), buf.readBoolean());
    }

    public static void handle(PacketForgetRecipe msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player == null) return;

        InteractionHand hand = msg.mainHand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof ItemAlchemiaGuide) {
            BrewRecipeBook.forget(stack, msg.recipe());
        }
    }
}
