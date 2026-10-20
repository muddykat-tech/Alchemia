package muddykat.alchemia.data.generators;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.AncientOak;
import muddykat.alchemia.common.blocks.blockentity.BlockResearchDesk;
import muddykat.alchemia.common.crafting.MachineBlueprints;
import net.minecraft.tags.ItemTags;
import muddykat.alchemia.common.blocks.helper.UpgradeSide;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.data.AlchemiaItemTags;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;

public class AlchemiaRecipeProvider extends RecipeProvider {

    public AlchemiaRecipeProvider(PackOutput output, java.util.concurrent.CompletableFuture<net.minecraft.core.HolderLookup.Provider> lookup) {
        super(output, lookup);
    }

    @Override
    protected void buildRecipes(RecipeOutput writer) {
        planksFromLog(writer, AncientOak.block(AncientOak.PLANKS), AlchemiaItemTags.ANCIENT_OAK_LOGS, 4);
        woodFromLogs(writer, AncientOak.block(AncientOak.WOOD), AncientOak.block(AncientOak.LOG));
        woodFromLogs(writer, AncientOak.block(AncientOak.STRIPPED_WOOD), AncientOak.block(AncientOak.STRIPPED_LOG));
        generateRecipes(writer, AncientOak.family(), net.minecraft.world.flag.FeatureFlags.VANILLA_SET);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ItemRegistry.getItemFromRegistry("mortar_and_pestle"))
                .requires(Items.STICK)
                .requires(Items.STONE)
                .requires(Items.STONE)
                .requires(Items.STONE)
                .unlockedBy("has_stone", has(Items.STONE))
                .save(writer, recipeId("mortar_and_pestle"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ItemRegistry.getItemFromRegistry("alchemia_guide"))
                .requires(Items.BOOK)
                .requires(AlchemiaItemTags.INGREDIENTS)
                .unlockedBy("has_ingredient", has(AlchemiaItemTags.INGREDIENTS))
                .save(writer, recipeId("alchemia_guide"));

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ItemRegistry.getItemFromRegistry(BlockResearchDesk.ID))
                .pattern("FIB")
                .pattern("SSS")
                .pattern("P P")
                .define('F', Items.FEATHER)
                .define('I', Items.INK_SAC)
                .define('B', Items.BOOK)
                .define('S', ItemTags.WOODEN_SLABS)
                .define('P', ItemTags.PLANKS)
                .unlockedBy("has_guide", has(ItemRegistry.getItemFromRegistry("alchemia_guide")))
                .save(writer, recipeId(BlockResearchDesk.ID));

        blueprint(MachineBlueprints.CORE)
                .unlockedBy("has_cauldron", has(Items.CAULDRON))
                .save(writer, recipeId(MachineBlueprints.CORE_ID));

        for (UpgradeSide side : UpgradeSide.values()) {
            ItemLike mineralItem = ItemRegistry.getItemFromRegistry(MachineBlueprints.UPGRADE_MINERALS.get(side).getRegistryName());
            blueprint(MachineBlueprints.upgrade(side))
                    .showNotification(false)
                    .unlockedBy("has_" + MachineBlueprints.UPGRADE_MINERALS.get(side).getRegistryName(), has(mineralItem))
                    .save(writer, recipeId(side.registryName()));
        }

        ShapedRecipeBuilder.shaped(RecipeCategory.BREWING, ItemRegistry.getItemFromRegistry("potion_phial"), 3)
                .pattern(" G ")
                .pattern("G G")
                .pattern(" G ")
                .define('G', Items.GLASS)
                .unlockedBy("has_glass", has(Items.GLASS))
                .save(writer, recipeId("potion_phial"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BREWING, ItemRegistry.getItemFromRegistry("square_phial"), 3)
                .pattern("GG")
                .pattern("GG")
                .define('G', Items.GLASS)
                .unlockedBy("has_glass", has(Items.GLASS))
                .save(writer, recipeId("square_phial"));

        for (Ingredients ingredient : Ingredients.values()) {
            if (!ingredient.getType().equals(IngredientType.Mineral)) continue;

            String name = ingredient.name().toLowerCase();
            ItemLike crystalBlock = ItemRegistry.getItemFromRegistry(name + "_geode");
            ItemLike crystal = ItemRegistry.getItemFromRegistry(ingredient.getRegistryName());

            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, crystal, CRYSTALS_PER_BLOCK)
                    .requires(crystalBlock)
                    .unlockedBy("has_" + name + "_geode", has(crystalBlock))
                    .save(writer, recipeId(name + "_crystals_from_block"));
        }

        for (Ingredients ingredient : Ingredients.values()) {
            if (ingredient.getType().equals(IngredientType.Mineral)) continue;

            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ItemRegistry.getSeedByIngredient(ingredient))
                    .requires(ItemRegistry.getItemFromRegistry(ingredient.getRegistryName()))
                    .requires(Items.SHEARS)
                    .unlockedBy("has_" + ingredient.name().toLowerCase() + "_ingredient", has(ItemRegistry.getItemFromRegistry(ingredient.getRegistryName())))
                    .save(new QuietRecipeOutput(writer), recipeId(ingredient.name().toLowerCase() + "_seeds_from_ingredient"));
        }
    }

    public static final int CRYSTALS_PER_BLOCK = 4;

    private ShapedRecipeBuilder blueprint(MachineBlueprints.Blueprint blueprint) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.BREWING, ItemRegistry.getItemFromRegistry(blueprint.result()));
        for (String row : blueprint.rows()) builder.pattern(row);
        blueprint.keys().forEach((symbol, item) -> builder.define(symbol, item.get()));
        return builder;
    }

    private static ResourceLocation recipeId(String name) {
        return ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, name);
    }
}
