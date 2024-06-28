package muddykat.alchemia.common.network.packets;

import muddykat.alchemia.Alchemia;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TESyncPacket {
    BlockPos pos;
    CompoundTag tag;

    public TESyncPacket(BlockPos pos, CompoundTag tag) {
        this.pos = pos;
        this.tag = tag;
    }

    public static void encode(TESyncPacket object, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(object.pos);
        buffer.writeNbt(object.tag);
    }

    public static TESyncPacket decode(FriendlyByteBuf buffer) {
        return new TESyncPacket(buffer.readBlockPos(), buffer.readNbt());
    }

    public static void consume(TESyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Level world;
            if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT)
                world = Alchemia.proxy.getWorld();
            else {
                if (ctx.get().getSender() == null) return;
                world = ctx.get().getSender().level;
            }

            BlockEntity t = world.getBlockEntity(packet.pos);
            if (t != null) {
                world.getBlockEntity(packet.pos).load(packet.tag);
                world.getBlockEntity(packet.pos).setChanged();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}