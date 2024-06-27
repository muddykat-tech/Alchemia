package muddykat.alchemia.common;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.potion.PotionMap;
import muddykat.alchemia.common.world.WildHerbGeneration;
import muddykat.alchemia.registration.registers.BlockRegister;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

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
                    event.getWorld().setBlock(pos.below(), BlockRegister.BLOCK_REGISTRY.get("alchemical_cauldron").get().defaultBlockState(), 1);
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

        Biome.BiomeCategory category = event.getCategory();
        if (category.equals(Biome.BiomeCategory.NETHER) || category.equals(Biome.BiomeCategory.THEEND) || category.equals(Biome.BiomeCategory.NONE)) {
            return;
        }

        BiomeGenerationSettingsBuilder builder = event.getGeneration();

        // Category-based filter
        if (category.equals(Biome.BiomeCategory.MUSHROOM)) {
            builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, WildHerbGeneration.PATCH_WILD_MARSHROOM);
            return; // No other wild crops should exist here!
        }

        if (biomeName.getPath().equals("beach")) {

        }

        if (biomeName.getPath().equals("plains")) {
            builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, WildHerbGeneration.PATCH_WILD_WINDBLOOM);
        }


        if (category.equals(Biome.BiomeCategory.SWAMP) || category.equals(Biome.BiomeCategory.JUNGLE)) {

        }

        Biome.ClimateSettings climate = event.getClimate();
        if (climate.temperature >= 1.0F) {

        }


        if (climate.temperature > 0.3F && climate.temperature < 1.0F) {
            builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, WildHerbGeneration.PATCH_WILD_ARCANE_CRYSTALS);
        }


        if (climate.temperature > 0.0F && climate.temperature <= 0.3F) {

        }
    }

}
