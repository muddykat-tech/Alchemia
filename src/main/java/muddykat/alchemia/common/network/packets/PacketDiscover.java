package muddykat.alchemia.common.network.packets;

import muddykat.alchemia.common.items.ItemAlchemiaGuide;
import muddykat.alchemia.common.utility.StackComponent;
import muddykat.alchemia.common.utility.StackData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record PacketDiscover(boolean ingredient, String name, boolean mainHand) {

    public static void encode(PacketDiscover msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.ingredient());
        buf.writeUtf(msg.name());
        buf.writeBoolean(msg.mainHand());
    }

    public static PacketDiscover decode(FriendlyByteBuf buf) {
        return new PacketDiscover(buf.readBoolean(), buf.readUtf(), buf.readBoolean());
    }

    public static void handle(PacketDiscover msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player == null) return;

        InteractionHand hand = msg.mainHand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof ItemAlchemiaGuide)) return;

        StackComponent<List<String>> component = msg.ingredient()
                ? StackData.DISCOVERED_INGREDIENTS
                : StackData.DISCOVERED_RECIPES;

        List<String> names = new ArrayList<>(component.getOrDefault(stack, List.of()));
        if (names.contains(msg.name())) return;

        names.add(msg.name());
        names.sort(null);
        component.set(stack, List.copyOf(names));
    }
}
