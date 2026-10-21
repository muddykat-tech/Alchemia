package muddykat.alchemia.common.network.packets;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.guide.GuidePrimer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record PacketPrimer(List<Identifier> earned, List<String> chapters) implements CustomPacketPayload {

    public static final Type<PacketPrimer> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Alchemia.MODID, "primer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPrimer> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), PacketPrimer::earned,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), PacketPrimer::chapters,
            PacketPrimer::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketPrimer payload, IPayloadContext context) {
        context.enqueueWork(() -> GuidePrimer.install(payload.earned(), payload.chapters()));
    }
}
