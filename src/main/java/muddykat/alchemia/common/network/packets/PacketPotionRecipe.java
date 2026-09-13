package muddykat.alchemia.common.network.packets;

import muddykat.alchemia.common.potion.PotionMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PacketPotionRecipe(long seed) {

    public static void encode(PacketPotionRecipe msg, FriendlyByteBuf buf) {
        buf.writeLong(msg.seed());
    }

    public static PacketPotionRecipe decode(FriendlyByteBuf buf) {
        return new PacketPotionRecipe(buf.readLong());
    }

    public static void handle(PacketPotionRecipe msg, Supplier<NetworkEvent.Context> ctx) {
        PotionMap.scramble(msg.seed());
    }
}
