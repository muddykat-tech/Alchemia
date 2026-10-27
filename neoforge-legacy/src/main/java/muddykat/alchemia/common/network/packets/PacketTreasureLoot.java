package muddykat.alchemia.common.network.packets;

import io.netty.buffer.ByteBuf;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.client.effect.TreasureVision;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record PacketTreasureLoot(List<Long> positions, List<Integer> carts) implements CustomPacketPayload {

    public static final Type<PacketTreasureLoot> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, "treasure_loot"));

    public static final StreamCodec<ByteBuf, PacketTreasureLoot> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG.apply(ByteBufCodecs.list()), PacketTreasureLoot::positions,
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), PacketTreasureLoot::carts,
            PacketTreasureLoot::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketTreasureLoot payload, IPayloadContext context) {
        context.enqueueWork(() -> TreasureVision.loot(payload.positions(), payload.carts()));
    }
}
