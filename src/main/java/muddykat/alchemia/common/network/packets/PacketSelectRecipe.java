package muddykat.alchemia.common.network.packets;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.items.ItemAlchemiaGuide;
import muddykat.alchemia.common.potion.BrewRecipeBook;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import io.netty.buffer.ByteBuf;

public record PacketSelectRecipe(String recipe, boolean mainHand) implements CustomPacketPayload {

    public static final Type<PacketSelectRecipe> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Alchemia.MODID, "select_recipe"));

    public static final StreamCodec<ByteBuf, PacketSelectRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PacketSelectRecipe::recipe,
            ByteBufCodecs.BOOL, PacketSelectRecipe::mainHand,
            PacketSelectRecipe::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSelectRecipe payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            InteractionHand hand = payload.mainHand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            ItemStack stack = context.player().getItemInHand(hand);
            if (stack.getItem() instanceof ItemAlchemiaGuide) {
                BrewRecipeBook.select(stack, payload.recipe().isEmpty() ? null : payload.recipe());
            }
        });
    }
}
