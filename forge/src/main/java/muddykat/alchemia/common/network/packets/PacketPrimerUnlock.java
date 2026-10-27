package muddykat.alchemia.common.network.packets;

import muddykat.alchemia.common.guide.PrimerSync;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PacketPrimerUnlock(String chapter) {

    private static final int MAX_LENGTH = 64;

    public static void encode(PacketPrimerUnlock msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.chapter(), MAX_LENGTH);
    }

    public static PacketPrimerUnlock decode(FriendlyByteBuf buf) {
        return new PacketPrimerUnlock(buf.readUtf(MAX_LENGTH));
    }

    public static void handle(PacketPrimerUnlock msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player != null) PrimerSync.unlock(player, msg.chapter());
    }
}
