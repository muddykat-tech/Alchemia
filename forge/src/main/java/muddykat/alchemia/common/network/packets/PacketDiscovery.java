package muddykat.alchemia.common.network.packets;

import muddykat.alchemia.common.guide.GuideNews;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public record PacketDiscovery(List<GuideNews.Entry> entries) {

    public static void encode(PacketDiscovery msg, FriendlyByteBuf buf) {
        buf.writeCollection(msg.entries(), (buffer, entry) -> entry.write(buffer));
    }

    public static PacketDiscovery decode(FriendlyByteBuf buf) {
        return new PacketDiscovery(buf.readList(GuideNews.Entry::read));
    }

    public static void handle(PacketDiscovery msg, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> muddykat.alchemia.client.gui.DiscoveryToast.show(msg.entries()));
    }
}
