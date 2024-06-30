package muddykat.alchemia.data.generators;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class CraftingRecipes {

    public static void register(Consumer<FinishedRecipe> consumer) {
        recipesVanillaAlternatives(consumer);
    }

    private static void recipesVanillaAlternatives(Consumer<FinishedRecipe> consumer) {
        for(Ingredients ingredient : Ingredients.values())
        {
            if(ingredient.getType().equals(IngredientType.Mineral)) continue;
            ShapelessRecipeBuilder.shapeless(ItemRegistry.getSeedByIngredient(ingredient))
                    .requires(ItemRegistry.getItemFromRegistry(ingredient.getRegistryName()))
                    .requires(Items.SHEARS)
                    .unlockedBy("has_" + ingredient.name() + "_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(ItemRegistry.getItemFromRegistry(ingredient.getRegistryName())))
                    .save(consumer, new ResourceLocation(Alchemia.MODID, ingredient.name().toLowerCase() + "_seeds_from_ingredient"));

            ShapelessRecipeBuilder.shapeless(ItemRegistry.getItemFromRegistry(ingredient.getCrushedRegistryName()))
                    .requires(ItemRegistry.getItemFromRegistry(ingredient.getRegistryName()))
                    .unlockedBy("has_" + ingredient.name() + "_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(ItemRegistry.getItemFromRegistry(ingredient.getCrushedRegistryName())))
                    .save(consumer, new ResourceLocation(Alchemia.MODID, ingredient.name().toLowerCase() + "_crushed_from_ingredient"));
        }
    }
}
