package muddykat.alchemia.data.generators;

import muddykat.alchemia.common.crafting.MachineBlueprints;

import net.minecraft.tags.ItemTags;
import muddykat.alchemia.common.blocks.blockentity.BlockResearchDesk;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.AncientOak;
import net.minecraft.world.flag.FeatureFlags;
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

#if MC_VER >= 26300
    public AlchemiaRecipeProvider(net.minecraft.data.worldgen.BootstrapContext<Recipe<?>> recipeOutput,
                                  net.minecraft.data.worldgen.BootstrapContext<net.minecraft.advancements.Advancement> advancementOutput) {
        super(recipeOutput, advancementOutput);
    }
#else
    public AlchemiaRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }
#endif

    @Override
    protected void buildRecipes() {
        planksFromLog(AncientOak.block(AncientOak.PLANKS), AlchemiaItemTags.ANCIENT_OAK_LOGS, 4);
        woodFromLogs(AncientOak.block(AncientOak.WOOD), AncientOak.block(AncientOak.LOG));
        woodFromLogs(AncientOak.block(AncientOak.STRIPPED_WOOD), AncientOak.block(AncientOak.STRIPPED_LOG));
        generateRecipes(AncientOak.family(), FeatureFlags.VANILLA_SET);

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

        shaped(RecipeCategory.DECORATIONS, ItemRegistry.getItemFromRegistry(BlockResearchDesk.ID))
                .pattern("FIB")
                .pattern("SSS")
                .pattern("P P")
                .define('F', Items.FEATHER)
                .define('I', Items.INK_SAC)
                .define('B', Items.BOOK)
                .define('S', ItemTags.WOODEN_SLABS)
                .define('P', ItemTags.PLANKS)
                .unlockedBy("has_guide", has(ItemRegistry.getItemFromRegistry("alchemia_guide")))
                .save(this.output, recipeKey(BlockResearchDesk.ID));

        blueprint(MachineBlueprints.CORE)
                .unlockedBy("has_cauldron", has(Items.CAULDRON))
                .save(this.output, recipeKey(MachineBlueprints.CORE_ID));

        for (UpgradeSide side : UpgradeSide.values()) {
            ItemLike mineralItem = ItemRegistry.getItemFromRegistry(MachineBlueprints.UPGRADE_MINERALS.get(side).getRegistryName());
            blueprint(MachineBlueprints.upgrade(side))
                    .showNotification(false)
                    .unlockedBy("has_" + MachineBlueprints.UPGRADE_MINERALS.get(side).getRegistryName(), has(mineralItem))
                    .save(this.output, recipeKey(side.registryName()));
        }

        shaped(RecipeCategory.BREWING, ItemRegistry.getItemFromRegistry("potion_phial"), 3)
                .pattern(" G ")
                .pattern("G G")
                .pattern(" G ")
                .define('G', Items.GLASS)
                .unlockedBy("has_glass", has(Items.GLASS))
                .save(this.output, recipeKey("potion_phial"));

        shaped(RecipeCategory.BREWING, ItemRegistry.getItemFromRegistry("square_phial"), 3)
                .pattern("GG")
                .pattern("GG")
                .define('G', Items.GLASS)
                .unlockedBy("has_glass", has(Items.GLASS))
                .save(this.output, recipeKey("square_phial"));

        for (Ingredients ingredient : Ingredients.values()) {
            if (!ingredient.getType().equals(IngredientType.Mineral)) continue;

            String name = ingredient.name().toLowerCase();
            ItemLike crystalBlock = ItemRegistry.getItemFromRegistry(name + "_geode");
            ItemLike crystal = ItemRegistry.getItemFromRegistry(ingredient.getRegistryName());

            shapeless(RecipeCategory.MISC, crystal, CRYSTALS_PER_BLOCK)
                    .requires(crystalBlock)
                    .unlockedBy("has_" + name + "_geode", has(crystalBlock))
                    .save(this.output, recipeKey(name + "_crystals_from_block"));
        }

        for (Ingredients ingredient : Ingredients.values()) {
            if (ingredient.getType().equals(IngredientType.Mineral)) continue;

            shapeless(RecipeCategory.MISC, ItemRegistry.getSeedByIngredient(ingredient))
                    .requires(ItemRegistry.getItemFromRegistry(ingredient.getRegistryName()))
                    .requires(Items.SHEARS)
                    .unlockedBy("has_" + ingredient.name().toLowerCase() + "_ingredient", has(ItemRegistry.getItemFromRegistry(ingredient.getRegistryName())))
                    .save(new QuietRecipeOutput(this.output), recipeKey(ingredient.name().toLowerCase() + "_seeds_from_ingredient"));
        }
    }

    public static final int CRYSTALS_PER_BLOCK = 4;


    private net.minecraft.data.recipes.ShapedRecipeBuilder blueprint(MachineBlueprints.Blueprint blueprint) {
        var builder = shaped(RecipeCategory.BREWING, ItemRegistry.getItemFromRegistry(blueprint.result()));
        for (String row : blueprint.rows()) builder.pattern(row);
        blueprint.keys().forEach((symbol, item) -> builder.define(symbol, item.get()));
        return builder;
    }

    private static ResourceKey<Recipe<?>> recipeKey(String name) {
        return ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(Alchemia.MODID, name));
    }

#if MC_VER >= 26300
    public static net.minecraft.core.registries.MultiRegistryBootstrap bootstrap() {
        return RecipeProvider.asBootstrap(AlchemiaRecipeProvider::new);
    }
#else
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
#endif
}
