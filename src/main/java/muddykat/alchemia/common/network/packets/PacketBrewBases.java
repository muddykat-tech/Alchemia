package muddykat.alchemia.common.network.packets;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.potion.BrewBase;
import muddykat.alchemia.common.potion.BrewBases;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record PacketBrewBases(List<BrewBase> bases) implements CustomPacketPayload {

    public static final Type<PacketBrewBases> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Alchemia.MODID, "brew_bases"));

    public static final StreamCodec<FriendlyByteBuf, PacketBrewBases> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecTrusted(BrewBase.LIST_CODEC), PacketBrewBases::bases,
            PacketBrewBases::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketBrewBases payload, IPayloadContext context) {
        context.enqueueWork(() -> BrewBases.install(payload.bases()));
    }
}
