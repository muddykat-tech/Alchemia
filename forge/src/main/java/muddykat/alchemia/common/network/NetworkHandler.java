package muddykat.alchemia.common.network;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.network.packets.PacketBrewBases;
import muddykat.alchemia.common.network.packets.PacketDiscovery;
import muddykat.alchemia.common.network.packets.PacketGreatWork;
import muddykat.alchemia.common.network.packets.PacketGuideAction;
import muddykat.alchemia.common.network.packets.PacketImbuements;
import muddykat.alchemia.common.network.packets.PacketNameBrew;
import muddykat.alchemia.common.network.packets.PacketPotionRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1.4";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Alchemia.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private static int nextId = 0;

    public static void register() {
        CHANNEL.messageBuilder(PacketBrewBases.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketBrewBases::encode)
                .decoder(PacketBrewBases::decode)
                .consumerMainThread(PacketBrewBases::handle)
                .add();

        CHANNEL.messageBuilder(PacketPotionRecipe.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketPotionRecipe::encode)
                .decoder(PacketPotionRecipe::decode)
                .consumerMainThread(PacketPotionRecipe::handle)
                .add();

        CHANNEL.messageBuilder(PacketImbuements.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketImbuements::encode)
                .decoder(PacketImbuements::decode)
                .consumerMainThread(PacketImbuements::handle)
                .add();

        CHANNEL.messageBuilder(PacketGreatWork.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketGreatWork::encode)
                .decoder(PacketGreatWork::decode)
                .consumerMainThread(PacketGreatWork::handle)
                .add();

        CHANNEL.messageBuilder(PacketDiscovery.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketDiscovery::encode)
                .decoder(PacketDiscovery::decode)
                .consumerMainThread(PacketDiscovery::handle)
                .add();

        CHANNEL.messageBuilder(PacketGuideAction.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(PacketGuideAction::encode)
                .decoder(PacketGuideAction::decode)
                .consumerMainThread(PacketGuideAction::handle)
                .add();

        CHANNEL.messageBuilder(PacketNameBrew.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(PacketNameBrew::encode)
                .decoder(PacketNameBrew::decode)
                .consumerMainThread(PacketNameBrew::handle)
                .add();
    }

    public static void sendToPlayer(Object payload, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
    }

    public static void sendToServer(Object payload) {
        CHANNEL.sendToServer(payload);
    }
}
