package muddykat.alchemia.data.generators;

import com.google.gson.JsonObject;
import muddykat.alchemia.registration.registers.RecipeSerializerRegistry;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public record QuietRecipeOutput(Consumer<FinishedRecipe> output) implements Consumer<FinishedRecipe> {

    @Override
    public void accept(FinishedRecipe recipe) {
        output.accept(new FinishedRecipe() {
            @Override
            public void serializeRecipeData(JsonObject json) {
                recipe.serializeRecipeData(json);
            }

            @Override
            public ResourceLocation getId() {
                return recipe.getId();
            }

            @Override
            public RecipeSerializer<?> getType() {
                return RecipeSerializerRegistry.QUIET_SHAPELESS.get();
            }

            @Override
            public @Nullable JsonObject serializeAdvancement() {
                return recipe.serializeAdvancement();
            }

            @Override
            public @Nullable ResourceLocation getAdvancementId() {
                return recipe.getAdvancementId();
            }
        });
    }
}
