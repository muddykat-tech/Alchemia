package muddykat.alchemia.common.network.packets;

import muddykat.alchemia.common.crafting.MagnumOpus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public record PacketGreatWork(List<MagnumOpus.Stage> stages) {

    public static void encode(PacketGreatWork msg, FriendlyByteBuf buf) {
        buf.writeCollection(msg.stages(), (buffer, stage) -> stage.write(buffer));
    }

    public static PacketGreatWork decode(FriendlyByteBuf buf) {
        return new PacketGreatWork(buf.readList(MagnumOpus.Stage::read));
    }

    public static void handle(PacketGreatWork msg, Supplier<NetworkEvent.Context> ctx) {
        MagnumOpus.install(msg.stages());
    }
}
