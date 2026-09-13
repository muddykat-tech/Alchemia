package muddykat.alchemia.common.network.packets;

import muddykat.alchemia.common.blocks.tileentity.container.AlchemicalCauldronMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PacketNameBrew(String name) {

    public static final int MAX_LENGTH = 32;

    public static void encode(PacketNameBrew msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.name(), MAX_LENGTH);
    }

    public static PacketNameBrew decode(FriendlyByteBuf buf) {
        return new PacketNameBrew(buf.readUtf(MAX_LENGTH));
    }

    public static void handle(PacketNameBrew msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player == null) return;

        if (player.containerMenu instanceof AlchemicalCauldronMenu menu) {
            menu.getCauldron().setBrewName(msg.name());
        }
    }
}
