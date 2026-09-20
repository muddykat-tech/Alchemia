package muddykat.alchemia.common.network.packets;

import io.netty.buffer.ByteBuf;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.items.ItemAlchemiaGuide;
import muddykat.alchemia.registration.registers.DataComponentRegistry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record PacketDiscover(boolean ingredient, String name, boolean mainHand) implements CustomPacketPayload {

    public static final Type<PacketDiscover> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Alchemia.MODID, "discover"));

    public static final StreamCodec<ByteBuf, PacketDiscover> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, PacketDiscover::ingredient,
            ByteBufCodecs.STRING_UTF8, PacketDiscover::name,
            ByteBufCodecs.BOOL, PacketDiscover::mainHand,
            PacketDiscover::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketDiscover payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            InteractionHand hand = payload.mainHand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            ItemStack stack = context.player().getItemInHand(hand);
            if (!(stack.getItem() instanceof ItemAlchemiaGuide)) return;

            DataComponentType<List<String>> component = payload.ingredient()
                    ? DataComponentRegistry.DISCOVERED_INGREDIENTS.get()
                    : DataComponentRegistry.DISCOVERED_RECIPES.get();

            List<String> names = new ArrayList<>(stack.getOrDefault(component, List.of()));
            if (names.contains(payload.name())) return;

            names.add(payload.name());
            names.sort(null);
            stack.set(component, List.copyOf(names));
        });
    }
}
