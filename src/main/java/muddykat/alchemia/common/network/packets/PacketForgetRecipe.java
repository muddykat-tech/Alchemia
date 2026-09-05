package muddykat.alchemia.common.network.packets;

import io.netty.buffer.ByteBuf;
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

public record PacketForgetRecipe(String recipe, boolean mainHand) implements CustomPacketPayload {

    public static final Type<PacketForgetRecipe> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Alchemia.MODID, "forget_recipe"));

    public static final StreamCodec<ByteBuf, PacketForgetRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PacketForgetRecipe::recipe,
            ByteBufCodecs.BOOL, PacketForgetRecipe::mainHand,
            PacketForgetRecipe::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketForgetRecipe payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            InteractionHand hand = payload.mainHand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            ItemStack stack = context.player().getItemInHand(hand);
            if (stack.getItem() instanceof ItemAlchemiaGuide) {
                BrewRecipeBook.forget(stack, payload.recipe());
            }
        });
    }
}
