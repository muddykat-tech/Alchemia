package muddykat.alchemia.common.network.packets;

import muddykat.alchemia.common.effect.TeleportViewState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PacketTeleportView(int phase, float chargeRate) {

    public static void encode(PacketTeleportView msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.phase());
        buf.writeFloat(msg.chargeRate());
    }

    public static PacketTeleportView decode(FriendlyByteBuf buf) {
        return new PacketTeleportView(buf.readVarInt(), buf.readFloat());
    }

    public static void handle(PacketTeleportView msg, Supplier<NetworkEvent.Context> ctx) {
        TeleportViewState.setPhase(msg.phase(), msg.chargeRate());
    }
}
