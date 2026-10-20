package muddykat.alchemia.common.crafting;

import com.google.gson.JsonObject;
import muddykat.alchemia.registration.registers.RecipeSerializerRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public class QuietShapelessRecipe extends ShapelessRecipe {

    public QuietShapelessRecipe(ShapelessRecipe recipe) {
        super(recipe.getId(), recipe.getGroup(), recipe.category(), recipe.getResultItem(RegistryAccess.EMPTY),
                NonNullList.of(Ingredient.EMPTY, recipe.getIngredients().toArray(Ingredient[]::new)));
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

        @Override
        public QuietShapelessRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new QuietShapelessRecipe(RecipeSerializer.SHAPELESS_RECIPE.fromJson(id, json));
        }

        @Override
        public QuietShapelessRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            return new QuietShapelessRecipe(RecipeSerializer.SHAPELESS_RECIPE.fromNetwork(id, buffer));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, QuietShapelessRecipe recipe) {
            RecipeSerializer.SHAPELESS_RECIPE.toNetwork(buffer, recipe);
        }
    }
}
