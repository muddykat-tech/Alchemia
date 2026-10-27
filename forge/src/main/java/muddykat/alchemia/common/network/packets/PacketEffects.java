package muddykat.alchemia.common.network.packets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import muddykat.alchemia.common.potion.AlchemiaEffect;
import muddykat.alchemia.common.potion.AlchemiaEffects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public record PacketEffects(List<AlchemiaEffect> effects) {

    public static final int MAX_LENGTH = 1 << 20;

    public static void encode(PacketEffects msg, FriendlyByteBuf buf) {
        JsonElement json = AlchemiaEffect.LIST_CODEC.encodeStart(JsonOps.INSTANCE, msg.effects())
                .result().orElseGet(JsonArray::new);
        buf.writeUtf(json.toString(), MAX_LENGTH);
    }

    public static PacketEffects decode(FriendlyByteBuf buf) {
        JsonElement json = JsonParser.parseString(buf.readUtf(MAX_LENGTH));
        return new PacketEffects(AlchemiaEffect.LIST_CODEC.parse(JsonOps.INSTANCE, json)
                .result().orElseGet(List::of));
    }

    public static void handle(PacketEffects msg, Supplier<NetworkEvent.Context> ctx) {
        AlchemiaEffects.install(msg.effects());
    }
}
