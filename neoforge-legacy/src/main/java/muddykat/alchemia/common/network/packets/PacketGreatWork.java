package muddykat.alchemia.common.network.packets;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.crafting.MagnumOpus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record PacketGreatWork(List<MagnumOpus.Stage> stages) implements CustomPacketPayload {

    public static final Type<PacketGreatWork> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, "great_work"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketGreatWork> STREAM_CODEC = StreamCodec.composite(
            MagnumOpus.Stage.STREAM_CODEC.apply(ByteBufCodecs.list()), PacketGreatWork::stages,
            PacketGreatWork::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketGreatWork payload, IPayloadContext context) {
        context.enqueueWork(() -> MagnumOpus.install(payload.stages()));
    }
}
