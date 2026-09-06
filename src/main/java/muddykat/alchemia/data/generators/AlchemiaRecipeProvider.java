package muddykat.alchemia.data.generators;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.crafting.PotionEffectIngredient;
import net.minecraft.world.effect.MobEffects;
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

        magnumOpus();

        for (Ingredients ingredient : Ingredients.values()) {
            if (ingredient.getType().equals(IngredientType.Mineral)) continue;

            shapeless(RecipeCategory.MISC, ItemRegistry.getSeedByIngredient(ingredient))
                    .requires(ItemRegistry.getItemFromRegistry(ingredient.getRegistryName()))
                    .requires(Items.SHEARS)
                    .unlockedBy("has_" + ingredient.name().toLowerCase() + "_ingredient", has(ItemRegistry.getItemFromRegistry(ingredient.getRegistryName())))
                    .save(this.output, recipeKey(ingredient.name().toLowerCase() + "_seeds_from_ingredient"));
        }
    }

    private void magnumOpus() {
        stage("nigredo", null,
                PotionEffectIngredient.of(MobEffects.SLOWNESS, 3),
                PotionEffectIngredient.of(MobEffects.RESISTANCE, 3),
                PotionEffectIngredient.of(MobEffects.POISON, 3),
                PotionEffectIngredient.of(MobEffects.STRENGTH, 3),
                PotionEffectIngredient.of(MobEffects.BLINDNESS, 3));

        stage("albedo", "nigredo",
                PotionEffectIngredient.of(MobEffects.INVISIBILITY, 3),
                PotionEffectIngredient.of(MobEffects.SPEED, 3),
                PotionEffectIngredient.of(MobEffects.NIGHT_VISION, 3),
                PotionEffectIngredient.of(MobEffects.HASTE, 3),
                PotionEffectIngredient.of(MobEffects.LEVITATION, 3),
                PotionEffectIngredient.of(MobEffects.DOLPHINS_GRACE, 3));

        stage("citrinitas", "albedo",
                PotionEffectIngredient.of(MobEffects.GLOWING, 3),
                PotionEffectIngredient.of(MobEffects.FIRE_RESISTANCE, 3),
                PotionEffectIngredient.of(MobEffects.CONDUIT_POWER, 3),
                PotionEffectIngredient.of(MobEffects.BAD_OMEN, 3),
                PotionEffectIngredient.of(MobEffects.INSTANT_DAMAGE, 3));

        stage("rubedo", "citrinitas",
                PotionEffectIngredient.of(MobEffects.SATURATION, 3),
                PotionEffectIngredient.of(MobEffects.REGENERATION, 3),
                PotionEffectIngredient.of(MobEffects.ABSORPTION, 3),
                PotionEffectIngredient.of(MobEffects.WITHER, 3),
                PotionEffectIngredient.of(MobEffects.HEALTH_BOOST, 3));

        stage("philosopher_stone", "rubedo",
                PotionEffectIngredient.of(MobEffects.LUCK, 3),
                PotionEffectIngredient.of(MobEffects.NAUSEA, 3),
                PotionEffectIngredient.ofAll(
                        PotionEffectIngredient.need(MobEffects.POISON, 1),
                        PotionEffectIngredient.need(MobEffects.FIRE_RESISTANCE, 1),
                        PotionEffectIngredient.need(MobEffects.INSTANT_DAMAGE, 1),
                        PotionEffectIngredient.need(MobEffects.SLOW_FALLING, 1),
                        PotionEffectIngredient.need(MobEffects.SLOWNESS, 1)),
                PotionEffectIngredient.ofAll(
                        PotionEffectIngredient.need(MobEffects.ABSORPTION, 1),
                        PotionEffectIngredient.need(MobEffects.GLOWING, 1),
                        PotionEffectIngredient.need(MobEffects.INSTANT_HEALTH, 1),
                        PotionEffectIngredient.need(MobEffects.HEALTH_BOOST, 1),
                        PotionEffectIngredient.need(MobEffects.DOLPHINS_GRACE, 1)),
                PotionEffectIngredient.ofAll(
                        PotionEffectIngredient.need(MobEffects.BAD_OMEN, 1),
                        PotionEffectIngredient.need(MobEffects.WITHER, 1),
                        PotionEffectIngredient.need(MobEffects.WEAKNESS, 1),
                        PotionEffectIngredient.need(MobEffects.INVISIBILITY, 1),
                        PotionEffectIngredient.need(MobEffects.INSTANT_DAMAGE, 1)),
                PotionEffectIngredient.ofAll(
                        PotionEffectIngredient.need(MobEffects.ABSORPTION, 2),
                        PotionEffectIngredient.need(MobEffects.FIRE_RESISTANCE, 1),
                        PotionEffectIngredient.need(MobEffects.RESISTANCE, 2)));
    }

    private void stage(String result, String previous, PotionEffectIngredient... potions) {
        var builder = shapeless(RecipeCategory.BREWING, ItemRegistry.getItemFromRegistry(result));

        if (previous != null) {
            builder.requires(ItemRegistry.getItemFromRegistry(previous));
            builder.unlockedBy("has_" + previous, has(ItemRegistry.getItemFromRegistry(previous)));
        } else {
            builder.unlockedBy("has_ingredient", has(AlchemiaItemTags.INGREDIENTS));
        }

        for (PotionEffectIngredient potion : potions) {
            builder.requires(potion.toVanilla());
        }
        builder.save(this.output, recipeKey(result));
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
