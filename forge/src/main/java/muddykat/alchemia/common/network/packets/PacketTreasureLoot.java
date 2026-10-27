package muddykat.alchemia.common.network.packets;

import muddykat.alchemia.client.effect.TreasureVision;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public record PacketTreasureLoot(List<Long> positions, List<Integer> carts) {

    public static void encode(PacketTreasureLoot msg, FriendlyByteBuf buf) {
        buf.writeCollection(msg.positions(), FriendlyByteBuf::writeVarLong);
        buf.writeCollection(msg.carts(), FriendlyByteBuf::writeVarInt);
    }

    public static PacketTreasureLoot decode(FriendlyByteBuf buf) {
        return new PacketTreasureLoot(buf.readList(FriendlyByteBuf::readVarLong), buf.readList(FriendlyByteBuf::readVarInt));
    }

    public static void handle(PacketTreasureLoot msg, Supplier<NetworkEvent.Context> ctx) {
        TreasureVision.loot(msg.positions(), msg.carts());
    }
}
