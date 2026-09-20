package muddykat.alchemia.common.network.packets;

import io.netty.buffer.ByteBuf;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.tileentity.container.AlchemicalCauldronMenu;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketNameBrew(String name) implements CustomPacketPayload {

    public static final int MAX_LENGTH = 32;

    public static final Type<PacketNameBrew> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Alchemia.MODID, "name_brew"));

    public static final StreamCodec<ByteBuf, PacketNameBrew> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(MAX_LENGTH), PacketNameBrew::name,
            PacketNameBrew::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketNameBrew payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof AlchemicalCauldronMenu menu) {
                menu.getCauldron().setBrewName(payload.name());
            }
        });
    }
}
