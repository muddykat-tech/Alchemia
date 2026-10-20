package muddykat.alchemia.common.network.packets;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.potion.AlchemiaEffect;
import muddykat.alchemia.common.potion.AlchemiaEffects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record PacketEffects(List<AlchemiaEffect> effects) implements CustomPacketPayload {

    public static final Type<PacketEffects> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Alchemia.MODID, "effects"));

    public static final StreamCodec<FriendlyByteBuf, PacketEffects> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecTrusted(AlchemiaEffect.LIST_CODEC), PacketEffects::effects,
            PacketEffects::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketEffects payload, IPayloadContext context) {
        context.enqueueWork(() -> AlchemiaEffects.install(payload.effects()));
    }
}
