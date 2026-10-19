package muddykat.alchemia.common.network.packets;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.potion.Imbuements;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record PacketImbuements(List<Imbuements.Category> categories) implements CustomPacketPayload {

    public static final Type<PacketImbuements> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Alchemia.MODID, "imbuements"));

    public static final StreamCodec<FriendlyByteBuf, PacketImbuements> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecTrusted(Imbuements.Category.LIST_CODEC), PacketImbuements::categories,
            PacketImbuements::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketImbuements payload, IPayloadContext context) {
        context.enqueueWork(() -> Imbuements.install(payload.categories()));
    }
}
