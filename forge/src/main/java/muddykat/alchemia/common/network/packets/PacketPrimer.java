package muddykat.alchemia.common.network.packets;

import muddykat.alchemia.common.guide.GuidePrimer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public record PacketPrimer(List<ResourceLocation> earned, List<String> chapters) {

    public static void encode(PacketPrimer msg, FriendlyByteBuf buf) {
        buf.writeCollection(msg.earned(), FriendlyByteBuf::writeResourceLocation);
        buf.writeCollection(msg.chapters(), FriendlyByteBuf::writeUtf);
    }

    public static PacketPrimer decode(FriendlyByteBuf buf) {
        return new PacketPrimer(buf.readList(FriendlyByteBuf::readResourceLocation), buf.readList(FriendlyByteBuf::readUtf));
    }

    public static void handle(PacketPrimer msg, Supplier<NetworkEvent.Context> ctx) {
        GuidePrimer.install(msg.earned(), msg.chapters());
    }
}
