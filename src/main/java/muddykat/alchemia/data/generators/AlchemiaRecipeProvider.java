package muddykat.alchemia.data.generators;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

import java.util.concurrent.CompletableFuture;

public class AlchemiaRecipeProvider extends RecipeProvider {

    public AlchemiaRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        shapeless(RecipeCategory.TOOLS, ItemRegistry.getItemFromRegistry("mortar_and_pestle"))
                .requires(Items.STICK)
                .requires(Items.STONE)
                .requires(Items.STONE)
                .requires(Items.STONE)
                .unlockedBy("has_stone", has(Items.STONE))
                .save(this.output, recipeKey("mortar_and_pestle"));

        for (Ingredients ingredient : Ingredients.values()) {
            if (ingredient.getType().equals(IngredientType.Mineral)) continue;

            shapeless(RecipeCategory.MISC, ItemRegistry.getSeedByIngredient(ingredient))
                    .requires(ItemRegistry.getItemFromRegistry(ingredient.getRegistryName()))
                    .requires(Items.SHEARS)
                    .unlockedBy("has_" + ingredient.name().toLowerCase() + "_ingredient", has(ItemRegistry.getItemFromRegistry(ingredient.getRegistryName())))
                    .save(this.output, recipeKey(ingredient.name().toLowerCase() + "_seeds_from_ingredient"));

        }
    }

    private static ResourceKey<Recipe<?>> recipeKey(String name) {
        return ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(Alchemia.MODID, name));
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new AlchemiaRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Alchemia Recipes";
        }
    }
}
