package muddykat.alchemia.common.network.packets;

import muddykat.alchemia.common.items.ItemAlchemiaGuide;
import muddykat.alchemia.common.potion.BrewRecipeBook;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PacketSelectRecipe(String recipe, boolean mainHand) {

    public static void encode(PacketSelectRecipe msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.recipe());
        buf.writeBoolean(msg.mainHand());
    }

    public static PacketSelectRecipe decode(FriendlyByteBuf buf) {
        return new PacketSelectRecipe(buf.readUtf(), buf.readBoolean());
    }

    public static void handle(PacketSelectRecipe msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player == null) return;

        InteractionHand hand = msg.mainHand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof ItemAlchemiaGuide) {
            BrewRecipeBook.select(stack, msg.recipe().isEmpty() ? null : msg.recipe());
        }
    }
}
