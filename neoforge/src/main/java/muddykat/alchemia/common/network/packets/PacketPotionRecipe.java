package muddykat.alchemia.common.network.packets;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.potion.PotionMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketPotionRecipe(long seed) implements CustomPacketPayload {

    public static final Type<PacketPotionRecipe> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Alchemia.MODID, "potion_recipe"));

    public static final StreamCodec<FriendlyByteBuf, PacketPotionRecipe> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeLong(payload.seed()),
            buffer -> new PacketPotionRecipe(buffer.readLong()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketPotionRecipe payload, IPayloadContext context) {
        context.enqueueWork(() -> PotionMap.scramble(payload.seed()));
    }
}
