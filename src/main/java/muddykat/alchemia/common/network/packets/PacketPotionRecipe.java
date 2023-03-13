package muddykat.alchemia.common.network.packets;

import muddykat.alchemia.common.potion.PotionMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketPotionRecipe {

    private final long seed;

    public PacketPotionRecipe(long seed) {
        this.seed = seed;
    }

    public PacketPotionRecipe(FriendlyByteBuf buf) {
        seed = buf.readLong();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeLong(seed);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            PotionMap.scramble(seed);
        });

        return true;
    }

}
