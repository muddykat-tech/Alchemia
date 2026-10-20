package muddykat.alchemia.common.crafting;

import com.mojang.serialization.MapCodec;
import muddykat.alchemia.registration.registers.RecipeSerializerRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public class QuietShapelessRecipe extends ShapelessRecipe {

    public QuietShapelessRecipe(ShapelessRecipe recipe) {
        super(recipe.getGroup(), recipe.category(), recipe.getResultItem(RegistryAccess.EMPTY), recipe.getIngredients());
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializerRegistry.QUIET_SHAPELESS.get();
    }

    public static class Serializer implements RecipeSerializer<QuietShapelessRecipe> {

        private static final MapCodec<QuietShapelessRecipe> CODEC = RecipeSerializer.SHAPELESS_RECIPE.codec()
                .xmap(QuietShapelessRecipe::new, recipe -> recipe);

        private static final StreamCodec<RegistryFriendlyByteBuf, QuietShapelessRecipe> STREAM_CODEC = RecipeSerializer.SHAPELESS_RECIPE.streamCodec()
                .map(QuietShapelessRecipe::new, recipe -> recipe);

        @Override
        public MapCodec<QuietShapelessRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, QuietShapelessRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
