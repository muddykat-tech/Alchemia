package muddykat.alchemia.common;

import muddykat.alchemia.common.items.helper.IngredientAlignment;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.world.WildHerbGeneration;
import muddykat.alchemia.registration.registers.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

public class WorldEventHandler {

    @SubscribeEvent
    public static void cauldronCreation(BlockEvent.EntityPlaceEvent event){
        if(event.getEntity() instanceof Player){
            BlockState block = event.getPlacedBlock();
            if(block.getBlock() instanceof CauldronBlock){
                BlockState blockBelow = event.getPlacedAgainst();
                if(blockBelow.is(Blocks.CAMPFIRE)) {
                    BlockPos pos = event.getPos();
                    event.getWorld().setBlock(pos, Blocks.AIR.defaultBlockState(), 1 | 8);
                    event.getWorld().setBlock(pos.below(), BlockRegistry.BLOCK_REGISTRY.get("alchemical_cauldron").get().defaultBlockState(), 1);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRecipeUpdate(PlayerEvent.ItemCraftedEvent event)
    {
        boolean shouldReturnShears = false;
        ItemStack shears = new ItemStack(Items.SHEARS);
        Container items = event.getInventory();
        for(int i = 0; i < items.getContainerSize(); i++)
        {
            ItemStack foundItem = items.getItem(i);
            if(foundItem.getItem().equals(Items.SHEARS)){
                shears = foundItem;
                shouldReturnShears = true;
            }
        }

        if(shouldReturnShears)
        {
            shears.setDamageValue(shears.getDamageValue()+1);
            event.getPlayer().getInventory().add(shears);
        }
    }

    @SubscribeEvent
    public static void onBiomeLoad(BiomeLoadingEvent event) {
        ResourceLocation biomeName = event.getName();

        if (biomeName == null) {
            return;
        }
        ArrayList<Ingredients> setupIngredients = new ArrayList<>();

        Biome.BiomeCategory category = event.getCategory();
        BiomeGenerationSettingsBuilder builder = event.getGeneration();

        if(category.equals(Biome.BiomeCategory.NETHER))
        {
            builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, WildHerbGeneration.INGREDIENT_FEATURES.get(Ingredients.Fire_Citrine));
        }

        if (category.equals(Biome.BiomeCategory.THEEND) || category.equals(Biome.BiomeCategory.NONE)) {
            return;
        }


        // Category-based filter
        if (category.equals(Biome.BiomeCategory.MUSHROOM)) {
            // Very rare Crystal Type, Fable Bismuth only spawns on Mooshroom Islands
            builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, WildHerbGeneration.INGREDIENT_FEATURES.get(Ingredients.Fable_Bismuth));
            return; // No other wild crops should exist here!
        }

        if (category.equals(Biome.BiomeCategory.MOUNTAIN)) {
            // Somewhat Common Crystal, Only found in mountains
            builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, WildHerbGeneration.INGREDIENT_FEATURES.get(Ingredients.Cloud_Crystal));
            return; // No other wild crops should exist here!
        }

        if(biomeName.getPath().equals(Biomes.DARK_FOREST.getRegistryName().getPath()))
        {
            builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, WildHerbGeneration.INGREDIENT_FEATURES.get(Ingredients.Boombloom));
            builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, WildHerbGeneration.INGREDIENT_FEATURES.get(Ingredients.Spellbloom));

            builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, WildHerbGeneration.INGREDIENT_FEATURES.get(Ingredients.Arcane_Crystal));
            setupIngredients.add(Ingredients.Spellbloom);
            setupIngredients.add(Ingredients.Boombloom);
        }


        Biome.ClimateSettings climate = event.getClimate();

        if(climate.temperature < 0.2f )
        {
            builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, WildHerbGeneration.INGREDIENT_FEATURES.get(Ingredients.Frost_Sapphire));
        }

        for (Ingredients ingredient : Ingredients.values())
        {
            if(setupIngredients.contains(ingredient)) continue;;

            if(WildHerbGeneration.INGREDIENT_FEATURES.get(ingredient) == null) continue;
            if(ingredient.getType() == IngredientType.Flower) {
                if (ingredient.getPrimaryAlignment() == IngredientAlignment.Fire) {
                    if (climate.temperature > 1f) {
                        builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, WildHerbGeneration.INGREDIENT_FEATURES.get(ingredient));
                    }
                }

                if (ingredient.getPrimaryAlignment() == IngredientAlignment.Water) {
                    if (climate.temperature < 0.2f || climate.downfall > 0.7f) {
                        builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, WildHerbGeneration.INGREDIENT_FEATURES.get(ingredient));
                    }
                }

                if (ingredient.getPrimaryAlignment() == IngredientAlignment.Air) {
                    if ((climate.temperature > 0.3f && climate.temperature < 0.8f)) {
                        builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, WildHerbGeneration.INGREDIENT_FEATURES.get(ingredient));
                    }
                }
            }
        }
    }
}
