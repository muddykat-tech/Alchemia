package muddykat.alchemia.common.network;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.network.packets.PacketBrewBases;
import muddykat.alchemia.common.network.packets.PacketDiscover;
import muddykat.alchemia.common.network.packets.PacketForgetRecipe;
import muddykat.alchemia.common.network.packets.PacketNameBrew;
import muddykat.alchemia.common.network.packets.PacketPotionRecipe;
import muddykat.alchemia.common.network.packets.PacketSelectRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1.0";

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

        CHANNEL.messageBuilder(PacketSelectRecipe.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(PacketSelectRecipe::encode)
                .decoder(PacketSelectRecipe::decode)
                .consumerMainThread(PacketSelectRecipe::handle)
                .add();

        CHANNEL.messageBuilder(PacketForgetRecipe.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(PacketForgetRecipe::encode)
                .decoder(PacketForgetRecipe::decode)
                .consumerMainThread(PacketForgetRecipe::handle)
                .add();

        CHANNEL.messageBuilder(PacketDiscover.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(PacketDiscover::encode)
                .decoder(PacketDiscover::decode)
                .consumerMainThread(PacketDiscover::handle)
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
