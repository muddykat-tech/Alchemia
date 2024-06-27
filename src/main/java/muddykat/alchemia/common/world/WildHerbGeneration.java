package muddykat.alchemia.common.world;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.BlockIngredient;
import muddykat.alchemia.common.config.Configuration;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.world.configuration.WildHerbConfiguration;
import muddykat.alchemia.registration.registers.AlchemiaBiomeFeatures;
import muddykat.alchemia.registration.registers.BlockRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.BanIpCommands;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RandomizedIntStateProvider;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

public class WildHerbGeneration {
    public static Holder<ConfiguredFeature<WildHerbConfiguration, ?>> FEATURE_PATCH_WILD_WINDBLOOM;
    public static Holder<ConfiguredFeature<WildHerbConfiguration, ?>> FEATURE_PATCH_WILD_MARSHROOM;
    public static Holder<ConfiguredFeature<WildHerbConfiguration, ?>> FEATURE_PATCH_WILD_ARCANE_CRYSTALS;

    public static Holder<PlacedFeature> PATCH_WILD_WINDBLOOM;
    public static Holder<PlacedFeature> PATCH_WILD_ARCANE_CRYSTALS;
    public static Holder<PlacedFeature> PATCH_WILD_MARSHROOM;

    public static final BlockPos BLOCK_BELOW = new BlockPos(0, -1, 0);
    public static final BlockPos BLOCK_ABOVE = new BlockPos(0, 1, 0);


    public static void registerWildHerbGeneration() {

        // Mushies
        FEATURE_PATCH_WILD_MARSHROOM = register(new ResourceLocation(Alchemia.MODID, "patch_wild_marshroom"),
                AlchemiaBiomeFeatures.WILD_HERB.get(), mushroomColonyConfig(BlockRegister.getBlock(Ingredients.Marshroom).get(), BlockRegister.getBlock(Ingredients.Witch_Mushroom).get(), BlockPredicate.matchesBlock(Blocks.STONE, BLOCK_BELOW)));

        PATCH_WILD_MARSHROOM = registerPlacement(new ResourceLocation(Alchemia.MODID, "patch_wild_marshroom"),
                FEATURE_PATCH_WILD_MARSHROOM, RarityFilter.onAverageOnceEvery(Configuration.CHANCE_WILD_MARSHROOM.get()), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());


        // Herbs
        FEATURE_PATCH_WILD_WINDBLOOM = register(new ResourceLocation(Alchemia.MODID, "patch_wild_windbloom"),
                AlchemiaBiomeFeatures.WILD_HERB.get(), mushroomColonyConfig(BlockRegister.getBlock(Ingredients.Windbloom).get(), BlockRegister.getBlock(Ingredients.Fluffbloom).get(), BlockPredicate.matchesBlock(Blocks.GRASS_BLOCK, BLOCK_BELOW)));

        PATCH_WILD_WINDBLOOM = registerPlacement(new ResourceLocation(Alchemia.MODID, "patch_wild_windbloom"),
                FEATURE_PATCH_WILD_WINDBLOOM, RarityFilter.onAverageOnceEvery(Configuration.CHANCE_WILD_WINDBLOOM.get()), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());

        // Minerals
        FEATURE_PATCH_WILD_ARCANE_CRYSTALS = register(new ResourceLocation(Alchemia.MODID, "patch_wild_arcane_crystals"),
                AlchemiaBiomeFeatures.WILD_HERB.get(), mushroomColonyConfig(BlockRegister.getBlock(Ingredients.Arcane_Crystal).get(), BlockRegister.getBlock(Ingredients.Fable_Bismuth).get(), BlockPredicate.matchesBlock(Blocks.STONE, BLOCK_BELOW)));

        PATCH_WILD_ARCANE_CRYSTALS = registerPlacement(new ResourceLocation(Alchemia.MODID, "patch_wild_arcane_crystals"),
                FEATURE_PATCH_WILD_ARCANE_CRYSTALS, RarityFilter.onAverageOnceEvery(Configuration.CHANCE_WILD_WINDBLOOM.get()), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());

    }

    public static RandomPatchConfiguration randomPatchConfig(Block block, int tries, int xzSpread, BlockPredicate plantedOn) {
        return new RandomPatchConfiguration(tries, xzSpread, 3, PlacementUtils.filtered(
                Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(block)),
                BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE, plantedOn)));
    }

    public static WildHerbConfiguration wildHerbConfig(Block primaryBlock, Block secondaryBlock, BlockPredicate plantedOn) {
        return new WildHerbConfiguration(64, 6, 3, plantBlockConfig(primaryBlock, plantedOn), plantBlockConfig(secondaryBlock, plantedOn), null);
    }

    public static WildHerbConfiguration wildCropWithFloorConfig(Block primaryBlock, Block secondaryBlock, BlockPredicate plantedOn, Block floorBlock, BlockPredicate replaces) {
        return new WildHerbConfiguration(64, 6, 3, plantBlockConfig(primaryBlock, plantedOn), plantBlockConfig(secondaryBlock, plantedOn), floorBlockConfig(floorBlock, replaces));
    }

    public static WildHerbConfiguration mushroomColonyConfig(Block colonyBlock, Block secondaryBlock, BlockPredicate plantedOn) {
        return new WildHerbConfiguration(64, 6, 3, colonyBlockConfig(colonyBlock, plantedOn), plantBlockConfig(secondaryBlock, plantedOn), null);
    }


    public static Holder<PlacedFeature> plantBlockConfig(Block block, BlockPredicate plantedOn) {
        return PlacementUtils.filtered(
                Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(block)),
                BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE, plantedOn));
    }

    public static Holder<PlacedFeature> colonyBlockConfig(Block block, BlockPredicate plantedOn) {
        return PlacementUtils.filtered(
                Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(new RandomizedIntStateProvider(BlockStateProvider.simple(block), BlockIngredient.AGE, UniformInt.of(0, 3))),
                BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE, plantedOn));
    }

    public static Holder<PlacedFeature> floorBlockConfig(Block block, BlockPredicate replaces) {
        return PlacementUtils.filtered(
                Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(block)),
                BlockPredicate.allOf(BlockPredicate.replaceable(BLOCK_ABOVE), replaces));
    }

    // Registry stuff

    static Holder<PlacedFeature> registerPlacement(ResourceLocation id, Holder<? extends ConfiguredFeature<?, ?>> feature, PlacementModifier... modifiers) {
        return BuiltinRegistries.register(BuiltinRegistries.PLACED_FEATURE, id, new PlacedFeature(Holder.hackyErase(feature), List.of(modifiers)));
    }

    protected static <FC extends FeatureConfiguration, F extends Feature<FC>> Holder<ConfiguredFeature<FC, ?>> register(ResourceLocation id, F feature, FC featureConfig) {
        return register(BuiltinRegistries.CONFIGURED_FEATURE, id, new ConfiguredFeature<>(feature, featureConfig));
    }

    private static <V extends T, T> Holder<V> register(Registry<T> registry, ResourceLocation id, V value) {
        return (Holder<V>) BuiltinRegistries.<T>register(registry, id, value);
    }
}
