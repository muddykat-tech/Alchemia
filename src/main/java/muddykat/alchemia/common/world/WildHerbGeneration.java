package muddykat.alchemia.common.world;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.BlockIngredient;
import muddykat.alchemia.common.items.helper.IngredientAlignment;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.world.configuration.WildHerbConfiguration;
import muddykat.alchemia.registration.registers.BiomeFeatureRegistry;
import muddykat.alchemia.registration.registers.BlockRegistry;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RandomizedIntStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement;
import net.minecraft.world.level.levelgen.placement.RandomOffsetPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WildHerbGeneration {
    public static final Vec3i BLOCK_BELOW = new Vec3i(0, -1, 0);
    public static final Vec3i BLOCK_ABOVE = new Vec3i(0, 1, 0);

    public static final Map<Ingredients, ResourceKey<ConfiguredFeature<?, ?>>> CONFIGURED_FEATURES = new EnumMap<>(Ingredients.class);
    public static final Map<Ingredients, ResourceKey<PlacedFeature>> PLACED_FEATURES = new EnumMap<>(Ingredients.class);

    static {
        for (Ingredients ingredient : Ingredients.values()) {
            if (!generates(ingredient)) continue;
            String name = featureName(ingredient);
            CONFIGURED_FEATURES.put(ingredient, ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(Alchemia.MODID, name)));
            PLACED_FEATURES.put(ingredient, ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(Alchemia.MODID, name)));
        }
    }

    public static boolean generates(Ingredients ingredient) {
        return true;
    }

    private static String featureName(Ingredients ingredient) {
        String name = ingredient.name().toLowerCase();
        return switch (IngredientHabitat.of(ingredient)) {
            case GEODE -> "geode_" + name;
            case CEILING -> "roots_" + name;
            case UNDERGROUND, SULFUR -> "fungi_" + name;
            case AQUATIC -> "seabed_" + name;
            case SURFACE, SAND -> "patch_wild_" + name;
        };
    }

    public static void bootstrapConfiguredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        HolderGetter<Block> blocks = context.lookup(Registries.BLOCK);

        for (Ingredients ingredient : Ingredients.values()) {
            if (!generates(ingredient)) continue;

            IngredientHabitat habitat = IngredientHabitat.of(ingredient);

            if (habitat == IngredientHabitat.GEODE) {
                context.register(CONFIGURED_FEATURES.get(ingredient),
                        new ConfiguredFeature<>(Feature.GEODE, crystalGeodeConfig(ingredient, blocks)));
                continue;
            }

            Block block = BlockRegistry.getBlock(ingredient).get();
            BlockPredicate occupies = habitat == IngredientHabitat.AQUATIC
                    ? BlockPredicate.matchesFluids(Fluids.WATER)
                    : BlockPredicate.ONLY_IN_AIR_PREDICATE;

            BlockPredicate grownOn = switch (habitat) {
                case SURFACE -> BlockPredicate.matchesTag(BLOCK_BELOW, BlockTags.SUPPORTS_VEGETATION);
                case SAND -> BlockPredicate.matchesTag(BLOCK_BELOW, BlockTags.SAND);
                case UNDERGROUND -> BlockPredicate.matchesTag(BLOCK_BELOW, BlockTags.BASE_STONE_OVERWORLD);
                case SULFUR -> BlockPredicate.matchesBlocks(BLOCK_BELOW, Blocks.SULFUR, Blocks.POTENT_SULFUR);
                case AQUATIC -> BlockPredicate.anyOf(
                        BlockPredicate.matchesTag(BLOCK_BELOW, BlockTags.SAND),
                        BlockPredicate.matchesTag(BLOCK_BELOW, BlockTags.DIRT),
                        BlockPredicate.matchesBlocks(BLOCK_BELOW, Blocks.GRAVEL, Blocks.CLAY));
                case CEILING -> BlockPredicate.allOf(
                        BlockPredicate.matchesTag(BLOCK_ABOVE, BlockTags.DIRT),
                        BlockPredicate.matchesTag(BLOCK_BELOW, BlockTags.AIR));
                case GEODE -> BlockPredicate.alwaysTrue();
            };

            BlockPredicate placedWhere = BlockPredicate.allOf(occupies, grownOn);

            WildHerbConfiguration config = switch (habitat) {
                case CEILING -> rootConfig(block, placedWhere);
                case UNDERGROUND, SULFUR -> fungiConfig(block, placedWhere);
                case AQUATIC -> seabedConfig(block, placedWhere);
                default -> colonyConfig(block, placedWhere);
            };

            context.register(CONFIGURED_FEATURES.get(ingredient),
                    new ConfiguredFeature<>(BiomeFeatureRegistry.WILD_HERB.get(), config));
        }
    }

    public static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configured = context.lookup(Registries.CONFIGURED_FEATURE);

        for (Ingredients ingredient : Ingredients.values()) {
            if (!generates(ingredient)) continue;

            IngredientHabitat habitat = IngredientHabitat.of(ingredient);
            int chance = ingredient.getPatchChance();

            List<PlacementModifier> modifiers = switch (habitat) {
                case SURFACE, SAND -> List.of(RarityFilter.onAverageOnceEvery(chance), InSquarePlacement.spread(),
                        PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
                case AQUATIC -> List.of(RarityFilter.onAverageOnceEvery(Math.max(1, chance / 2)), InSquarePlacement.spread(),
                        PlacementUtils.HEIGHTMAP_TOP_SOLID, BiomeFilter.biome());
                case CEILING -> List.of(RarityFilter.onAverageOnceEvery(Math.max(1, chance / 4)),
                        CountPlacement.of(24), InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(30), VerticalAnchor.absolute(110)),
                        EnvironmentScanPlacement.scanningFor(Direction.UP, BlockPredicate.solid(), BlockPredicate.ONLY_IN_AIR_PREDICATE, 12),
                        RandomOffsetPlacement.vertical(ConstantInt.of(-1)),
                        BiomeFilter.biome());
                case UNDERGROUND, SULFUR -> List.of(RarityFilter.onAverageOnceEvery(Math.max(1, chance / 4)),
                        CountPlacement.of(24), InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(8), VerticalAnchor.absolute(96)),
                        EnvironmentScanPlacement.scanningFor(Direction.DOWN, BlockPredicate.solid(), BlockPredicate.ONLY_IN_AIR_PREDICATE, 12),
                        RandomOffsetPlacement.vertical(ConstantInt.of(1)),
                        BiomeFilter.biome());
                case GEODE -> List.of(RarityFilter.onAverageOnceEvery(chance), InSquarePlacement.spread(),
                        getHeightRangePlacement(ingredient), BiomeFilter.biome());
            };

            context.register(PLACED_FEATURES.get(ingredient),
                    new PlacedFeature(configured.getOrThrow(CONFIGURED_FEATURES.get(ingredient)), modifiers));
        }
    }

    private static PlacementModifier getHeightRangePlacement(Ingredients ingredient) {
        if (ingredient.getPrimaryAlignment().equals(IngredientAlignment.Fire)) {
            return HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(6), VerticalAnchor.absolute(120));
        }

        if (ingredient.getPrimaryAlignment().equals(IngredientAlignment.Air)) {
            return HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(48), VerticalAnchor.absolute(128));
        }

        return HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(6), VerticalAnchor.absolute(64));
    }

    public static WildHerbConfiguration colonyConfig(Block block, BlockPredicate plantedOn) {
        return new WildHerbConfiguration(64, 6, 3, colonyBlockConfig(block, plantedOn), plantBlockConfig(block, plantedOn), Optional.empty());
    }

    public static WildHerbConfiguration seabedConfig(Block block, BlockPredicate growsOn) {
        return new WildHerbConfiguration(48, 6, 1, colonyBlockConfig(block, growsOn), plantBlockConfig(block, growsOn), Optional.empty());
    }

    public static WildHerbConfiguration fungiConfig(Block block, BlockPredicate growsOn) {
        return new WildHerbConfiguration(24, 4, 1, colonyBlockConfig(block, growsOn), plantBlockConfig(block, growsOn), Optional.empty());
    }

    public static WildHerbConfiguration rootConfig(Block block, BlockPredicate hangsFrom) {
        return new WildHerbConfiguration(24, 4, 0, colonyBlockConfig(block, hangsFrom), plantBlockConfig(block, hangsFrom), Optional.empty());
    }

    static final BlockStateProvider NETHER_OUTER_SHELL = new WeightedStateProvider(WeightedList.<BlockState>builder()
            .add(Blocks.CRYING_OBSIDIAN.defaultBlockState(), 1)
            .add(Blocks.OBSIDIAN.defaultBlockState(), 4)
            .add(Blocks.BASALT.defaultBlockState(), 5)
            .build());

    public static GeodeConfiguration crystalGeodeConfig(Ingredients ingredient, HolderGetter<Block> blocks) {
        HolderSet<Block> cannotReplace = blocks.getOrThrow(BlockTags.FEATURES_CANNOT_REPLACE);
        HolderSet<Block> invalidBlocks = blocks.getOrThrow(BlockTags.GEODE_INVALID_BLOCKS);

        return new GeodeConfiguration(
                new GeodeBlockSettings(
                        BlockStateProvider.simple(Blocks.AIR.defaultBlockState()),
                        BlockStateProvider.simple(BlockRegistry.getGeodeBlock(ingredient).defaultBlockState()),
                        BlockStateProvider.simple(BlockRegistry.getGeodeBuddingBlock(ingredient).defaultBlockState()),
                        BlockStateProvider.simple(Blocks.CALCITE.defaultBlockState()),
                        ingredient.getPrimaryAlignment().equals(IngredientAlignment.Fire) ? NETHER_OUTER_SHELL : BlockStateProvider.simple(Blocks.SMOOTH_BASALT.defaultBlockState()),
                        BlockRegistry.getGeodeBuddingBlock(ingredient).getClusterStates(),
                        cannotReplace,
                        invalidBlocks),
                new GeodeLayerSettings(1.7D, 2.2D, 3.2D, 4.2D),
                new GeodeCrackSettings(0.95D, 2.0D, 2),
                0.35D, 0.083D, true,
                UniformInt.of(4, 6),
                UniformInt.of(3, 4),
                UniformInt.of(1, 2),
                -16, 16, 0.05D, 1);
    }

    public static net.minecraft.core.Holder<PlacedFeature> plantBlockConfig(Block block, BlockPredicate placedWhere) {
        return PlacementUtils.filtered(
                Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(block)), placedWhere);
    }

    public static net.minecraft.core.Holder<PlacedFeature> colonyBlockConfig(Block block, BlockPredicate placedWhere) {
        return PlacementUtils.filtered(
                Feature.SIMPLE_BLOCK,
                new SimpleBlockConfiguration(new RandomizedIntStateProvider(BlockStateProvider.simple(block), BlockIngredient.AGE, UniformInt.of(0, 3))),
                placedWhere);
    }
}
