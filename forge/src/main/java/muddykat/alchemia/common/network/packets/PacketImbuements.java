package muddykat.alchemia.common.network.packets;

import muddykat.alchemia.common.potion.Imbuements;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public record PacketImbuements(List<Imbuements.Category> categories) {

    private static final String KEY = "categories";

    public static void encode(PacketImbuements msg, FriendlyByteBuf buf) {
        CompoundTag tag = new CompoundTag();
        Imbuements.Category.LIST_CODEC.encodeStart(NbtOps.INSTANCE, msg.categories()).result().ifPresent(encoded -> tag.put(KEY, encoded));
        buf.writeNbt(tag);
    }

    public static PacketImbuements decode(FriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        if (tag == null || !tag.contains(KEY)) return new PacketImbuements(List.of());
        return new PacketImbuements(Imbuements.Category.LIST_CODEC.parse(NbtOps.INSTANCE, tag.get(KEY)).result().orElse(List.of()));
    }

    public static void handle(PacketImbuements msg, Supplier<NetworkEvent.Context> ctx) {
        Imbuements.install(msg.categories());
    }
}
