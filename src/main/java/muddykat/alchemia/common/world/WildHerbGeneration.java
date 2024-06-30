package muddykat.alchemia.common.world;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.BlockIngredient;
import muddykat.alchemia.common.config.Configuration;
import muddykat.alchemia.common.items.helper.IngredientAlignment;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.world.configuration.WildHerbConfiguration;
import muddykat.alchemia.registration.registers.BiomeFeatureRegistry;
import muddykat.alchemia.registration.registers.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.GeodeFeature;
import net.minecraft.world.level.levelgen.feature.SimpleBlockFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RandomizedIntStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.placement.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

import static muddykat.alchemia.Alchemia.MOD_NAME;

public class WildHerbGeneration {
    public static final BlockPos BLOCK_BELOW = new BlockPos(0, -1, 0);
    public static final BlockPos BLOCK_ABOVE = new BlockPos(0, 1, 0);

    public static HashMap<Ingredients, Holder<PlacedFeature>> INGREDIENT_FEATURES = new HashMap<>();

    public static void registerWildHerbGeneration() {
        Alchemia.LOGGER.info(MOD_NAME + " World Generation Initialization");

        for(Ingredients ingredient : Ingredients.values())
        {
            switch(ingredient.getType())
            {
                case Herb:
                    break;
                case Root:
                    break;
                case Flower:
                    Holder<ConfiguredFeature<WildHerbConfiguration, ?>> ingredient_feature = register(new ResourceLocation(Alchemia.MODID, "patch_wild_" + ingredient.name().toLowerCase()),
                            BiomeFeatureRegistry.WILD_HERB.get(), mushroomColonyConfig(BlockRegistry.getBlock(ingredient).get(), BlockRegistry.getBlock(ingredient).get(), BlockPredicate.matchesTag(ingredient.getPrimaryAlignment() == IngredientAlignment.Fire ? BlockTags.SAND : BlockTags.DIRT, BLOCK_BELOW)));
                    Holder<PlacedFeature> placed_feature = registerPlacement(new ResourceLocation(Alchemia.MODID, "patch_wild_" + ingredient.name().toLowerCase()),
                            ingredient_feature, RarityFilter.onAverageOnceEvery(ingredient.getPatchChance()), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
                    INGREDIENT_FEATURES.put(ingredient, placed_feature);
                    break;
                case Mineral:
                    PlacementModifier mineral_placement_range = getHeightRangePlacement(ingredient);

                    Holder<ConfiguredFeature<GeodeConfiguration,?>> mineral_geode = register(new ResourceLocation(Alchemia.MODID, "geode_" + ingredient.name().toLowerCase()), Feature.GEODE, crystalGeodeConfig(ingredient));
                    Holder<PlacedFeature> placed_geode = registerPlacement(new ResourceLocation(Alchemia.MODID, "geode_" + ingredient.name().toLowerCase()), mineral_geode,
                            RarityFilter.onAverageOnceEvery(ingredient.getPatchChance()),
                                    InSquarePlacement.spread(), mineral_placement_range, BiomeFilter.biome());

                    INGREDIENT_FEATURES.put(ingredient, placed_geode);
                    break;
                case Mushroom:
                    break;
            }
        }
    }

    private static @NotNull PlacementModifier getHeightRangePlacement(Ingredients ingredient) {
        PlacementModifier mineral_placement_range = HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(6), VerticalAnchor.absolute(64));

        if(ingredient.getPrimaryAlignment().equals(IngredientAlignment.Fire))
        {
            mineral_placement_range = HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(6), VerticalAnchor.absolute(120));
        }

        if(ingredient.getPrimaryAlignment().equals(IngredientAlignment.Air))
        {
            mineral_placement_range = HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(48), VerticalAnchor.absolute(128));
        }

        return mineral_placement_range;
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

    static BlockStateProvider nether_outer_shell = new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder()
            .add(Blocks.CRYING_OBSIDIAN.defaultBlockState(), 1)
            .add(Blocks.OBSIDIAN.defaultBlockState(), 4)
            .add(Blocks.BASALT.defaultBlockState(), 5)
            .build());

    public static GeodeConfiguration crystalGeodeConfig(Ingredients ingredient)
    {
        return new GeodeConfiguration(
                new GeodeBlockSettings
                        (
                                BlockStateProvider.simple(Blocks.AIR.defaultBlockState()),
                                BlockStateProvider.simple(BlockRegistry.getGeodeBlock(ingredient).defaultBlockState()),         // Inside Crystal Mixed with block below
                                BlockStateProvider.simple(BlockRegistry.getGeodeBuddingBlock(ingredient).defaultBlockState()),  // which is less common than the above.
                                BlockStateProvider.simple(Blocks.CALCITE.defaultBlockState()), // Middle Layer
                                // Check if Fire is primary ingredient alignment (means it'll spawn in the nether
                                ingredient.getPrimaryAlignment().equals(IngredientAlignment.Fire) ? nether_outer_shell : BlockStateProvider.simple(Blocks.SMOOTH_BASALT.defaultBlockState()), // Outer Layer
                                BlockRegistry.getGeodeBuddingBlock(ingredient).getClusterStates(),
                                BlockTags.FEATURES_CANNOT_REPLACE,
                                BlockTags.GEODE_INVALID_BLOCKS
                        ),
                new GeodeLayerSettings(1.7D, 2.2D, 3.2D, 4.2D),
                new GeodeCrackSettings(0.95D, 2.0D, 2),
                0.35D, 0.083D, true,
                UniformInt.of(4, 6),
                UniformInt.of(3, 4),
                UniformInt.of(1, 2),
                -16, 16, 0.05D, 1
        );
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
