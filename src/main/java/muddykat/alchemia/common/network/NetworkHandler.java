package muddykat.alchemia.common.network;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.network.packets.PacketBrewBases;
import muddykat.alchemia.common.network.packets.PacketPotionRecipe;
import muddykat.alchemia.common.network.packets.PacketDiscover;
import muddykat.alchemia.common.network.packets.PacketForgetRecipe;
import muddykat.alchemia.common.network.packets.PacketNameBrew;
import muddykat.alchemia.common.network.packets.PacketSelectRecipe;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1.0";

    public static void register(final RegisterPayloadHandlersEvent event) {
        event.registrar(PROTOCOL_VERSION)
                .playToClient(PacketBrewBases.TYPE, PacketBrewBases.STREAM_CODEC, PacketBrewBases::handle)
                .playToClient(PacketPotionRecipe.TYPE, PacketPotionRecipe.STREAM_CODEC, PacketPotionRecipe::handle)
                .playToServer(PacketSelectRecipe.TYPE, PacketSelectRecipe.STREAM_CODEC, PacketSelectRecipe::handle)
                .playToServer(PacketForgetRecipe.TYPE, PacketForgetRecipe.STREAM_CODEC, PacketForgetRecipe::handle)
                .playToServer(PacketDiscover.TYPE, PacketDiscover.STREAM_CODEC, PacketDiscover::handle)
                .playToServer(PacketNameBrew.TYPE, PacketNameBrew.STREAM_CODEC, PacketNameBrew::handle);
    }

    public static void sendToPlayer(CustomPacketPayload payload, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, payload);
    }
}
