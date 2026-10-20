package muddykat.alchemia.common.network.packets;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.guide.GuideNews;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record PacketDiscovery(List<GuideNews.Entry> entries) implements CustomPacketPayload {

    public static final Type<PacketDiscovery> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, "discovery"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketDiscovery> STREAM_CODEC = StreamCodec.composite(
            GuideNews.Entry.STREAM_CODEC.apply(ByteBufCodecs.list()), PacketDiscovery::entries,
            PacketDiscovery::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketDiscovery payload, IPayloadContext context) {
        context.enqueueWork(() -> muddykat.alchemia.client.gui.DiscoveryToast.show(payload.entries()));
    }
}
