package muddykat.alchemia.common.network.packets;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.guide.PrimerSync;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketPrimerUnlock(String chapter) implements CustomPacketPayload {

    public static final Type<PacketPrimerUnlock> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Alchemia.MODID, "primer_unlock"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPrimerUnlock> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(64), PacketPrimerUnlock::chapter,
            PacketPrimerUnlock::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketPrimerUnlock payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) PrimerSync.unlock(player, payload.chapter());
        });
    }
}
