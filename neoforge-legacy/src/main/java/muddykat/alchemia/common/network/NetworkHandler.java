package muddykat.alchemia.common.network;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.network.packets.PacketDiscovery;
import muddykat.alchemia.common.network.packets.PacketEffects;
import muddykat.alchemia.common.network.packets.PacketGreatWork;
import muddykat.alchemia.common.network.packets.PacketPrimer;
import muddykat.alchemia.common.network.packets.PacketPrimerUnlock;
import muddykat.alchemia.common.network.packets.PacketImbuements;
import muddykat.alchemia.common.network.packets.PacketBrewBases;
import muddykat.alchemia.common.network.packets.PacketPotionRecipe;
import muddykat.alchemia.common.network.packets.PacketGuideAction;
import muddykat.alchemia.common.network.packets.PacketNameBrew;
import muddykat.alchemia.common.network.packets.PacketTeleportView;
import muddykat.alchemia.common.network.packets.PacketTreasureLoot;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1.7";

    public static void register(final RegisterPayloadHandlersEvent event) {
        event.registrar(PROTOCOL_VERSION)
                .playToClient(PacketEffects.TYPE, PacketEffects.STREAM_CODEC, PacketEffects::handle)
                .playToClient(PacketBrewBases.TYPE, PacketBrewBases.STREAM_CODEC, PacketBrewBases::handle)
                .playToClient(PacketImbuements.TYPE, PacketImbuements.STREAM_CODEC, PacketImbuements::handle)
                .playToClient(PacketGreatWork.TYPE, PacketGreatWork.STREAM_CODEC, PacketGreatWork::handle)
                .playToClient(PacketPrimer.TYPE, PacketPrimer.STREAM_CODEC, PacketPrimer::handle)
                .playToClient(PacketDiscovery.TYPE, PacketDiscovery.STREAM_CODEC, PacketDiscovery::handle)
                .playToClient(PacketTeleportView.TYPE, PacketTeleportView.STREAM_CODEC, PacketTeleportView::handle)
                .playToClient(PacketTreasureLoot.TYPE, PacketTreasureLoot.STREAM_CODEC, PacketTreasureLoot::handle)
                .playToClient(PacketPotionRecipe.TYPE, PacketPotionRecipe.STREAM_CODEC, PacketPotionRecipe::handle)
                .playToServer(PacketGuideAction.TYPE, PacketGuideAction.STREAM_CODEC, PacketGuideAction::handle)
                .playToServer(PacketNameBrew.TYPE, PacketNameBrew.STREAM_CODEC, PacketNameBrew::handle)
                .playToServer(PacketPrimerUnlock.TYPE, PacketPrimerUnlock.STREAM_CODEC, PacketPrimerUnlock::handle);
    }

    public static void sendToPlayer(CustomPacketPayload payload, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, payload);
    }
}
