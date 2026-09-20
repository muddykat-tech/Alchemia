package muddykat.alchemia.common.network.packets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import muddykat.alchemia.common.potion.BrewBase;
import muddykat.alchemia.common.potion.BrewBases;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public record PacketBrewBases(List<BrewBase> bases) {

    public static final int MAX_LENGTH = 1 << 20;

    public static void encode(PacketBrewBases msg, FriendlyByteBuf buf) {
        JsonElement json = BrewBase.LIST_CODEC.encodeStart(JsonOps.INSTANCE, msg.bases())
                .result().orElseGet(JsonArray::new);
        buf.writeUtf(json.toString(), MAX_LENGTH);
    }

    public static PacketBrewBases decode(FriendlyByteBuf buf) {
        JsonElement json = JsonParser.parseString(buf.readUtf(MAX_LENGTH));
        return new PacketBrewBases(BrewBase.LIST_CODEC.parse(JsonOps.INSTANCE, json)
                .result().orElseGet(List::of));
    }

    public static void handle(PacketBrewBases msg, Supplier<NetworkEvent.Context> ctx) {
        BrewBases.install(msg.bases());
    }
}
