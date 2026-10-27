package muddykat.alchemia.common.network.packets;

import io.netty.buffer.ByteBuf;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.effect.TeleportViewState;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketTeleportView(int phase, float chargeRate) implements CustomPacketPayload {

    public static final Type<PacketTeleportView> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, "teleport_view"));

    public static final StreamCodec<ByteBuf, PacketTeleportView> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PacketTeleportView::phase,
            ByteBufCodecs.FLOAT, PacketTeleportView::chargeRate,
            PacketTeleportView::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketTeleportView payload, IPayloadContext context) {
        context.enqueueWork(() -> TeleportViewState.setPhase(payload.phase(), payload.chargeRate()));
    }
}
