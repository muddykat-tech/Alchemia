package muddykat.alchemia.data.generators;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.helper.UpgradeSide;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.data.AlchemiaItemTags;
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
import net.minecraft.world.level.ItemLike;

import java.util.Map;
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

        shapeless(RecipeCategory.TOOLS, ItemRegistry.getItemFromRegistry("alchemia_guide"))
                .requires(Items.BOOK)
                .requires(AlchemiaItemTags.INGREDIENTS)
                .unlockedBy("has_ingredient", has(AlchemiaItemTags.INGREDIENTS))
                .save(this.output, recipeKey("alchemia_guide"));

        shaped(RecipeCategory.BREWING, ItemRegistry.getItemFromRegistry("alchemy_machine_core"))
                .pattern("ISI")
                .pattern("GCG")
                .pattern("III")
                .define('I', Items.IRON_INGOT)
                .define('S', Items.BREWING_STAND)
                .define('G', Items.GLASS)
                .define('C', Items.CAULDRON)
                .unlockedBy("has_cauldron", has(Items.CAULDRON))
                .save(this.output, recipeKey("alchemy_machine_core"));

        for (UpgradeSide side : UpgradeSide.values()) {
            Ingredients mineral = UPGRADE_MINERALS.get(side);
            ItemLike mineralItem = ItemRegistry.getItemFromRegistry(mineral.getRegistryName());

            shaped(RecipeCategory.BREWING, ItemRegistry.getItemFromRegistry(side.registryName()))
                    .pattern("GBG")
                    .pattern("CMC")
                    .pattern("CCC")
                    .define('G', Items.GLASS)
                    .define('B', Items.GLASS_BOTTLE)
                    .define('C', Items.COPPER_INGOT)
                    .define('M', mineralItem)
                    .unlockedBy("has_" + mineral.getRegistryName(), has(mineralItem))
                    .save(this.output, recipeKey(side.registryName()));
        }

        for (Ingredients ingredient : Ingredients.values()) {
            if (ingredient.getType().equals(IngredientType.Mineral)) continue;

            shapeless(RecipeCategory.MISC, ItemRegistry.getSeedByIngredient(ingredient))
                    .requires(ItemRegistry.getItemFromRegistry(ingredient.getRegistryName()))
                    .requires(Items.SHEARS)
                    .unlockedBy("has_" + ingredient.name().toLowerCase() + "_ingredient", has(ItemRegistry.getItemFromRegistry(ingredient.getRegistryName())))
                    .save(this.output, recipeKey(ingredient.name().toLowerCase() + "_seeds_from_ingredient"));
        }
    }

    private static final Map<UpgradeSide, Ingredients> UPGRADE_MINERALS = Map.of(
            UpgradeSide.TOP, Ingredients.Cloud_Crystal,
            UpgradeSide.LEFT, Ingredients.Earth_Pyrite,
            UpgradeSide.RIGHT, Ingredients.Fire_Citrine);

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
