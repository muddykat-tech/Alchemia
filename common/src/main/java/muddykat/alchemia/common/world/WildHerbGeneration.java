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
import net.minecraft.tags.TagKey;
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
import net.minecraft.world.level.levelgen.feature.Feature;
#if MC_VER >= 26300
import net.minecraft.world.level.levelgen.feature.GeodeFeature;
import net.minecraft.world.level.levelgen.feature.SimpleBlockFeature;
import net.minecraft.world.level.levelgen.placement.OffsetPlacement;
#else
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
#endif
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RandomizedIntStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement;
#if MC_VER < 26300
import net.minecraft.world.level.levelgen.placement.RandomOffsetPlacement;
#endif
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

    public static final int ROOT_PATCH_TRIES = 24;
    public static final int ROOT_PATCHES_PER_CHUNK = 4;
    public static final int ROOT_CHANCE_BASELINE = 60;

#if MC_VER >= 26300
    public static final Map<Ingredients, ResourceKey<Feature>> CONFIGURED_FEATURES = new EnumMap<>(Ingredients.class);
#else
    public static final Map<Ingredients, ResourceKey<ConfiguredFeature<?, ?>>> CONFIGURED_FEATURES = new EnumMap<>(Ingredients.class);
#endif
    public static final Map<Ingredients, ResourceKey<PlacedFeature>> PLACED_FEATURES = new EnumMap<>(Ingredients.class);

    public static final String PEAK_CLUSTER_NAME = "peak_cloud_crystal";
#if MC_VER >= 26300
    public static final ResourceKey<Feature> PEAK_CLUSTER_CONFIGURED =
            ResourceKey.create(Registries.FEATURE, Identifier.fromNamespaceAndPath(Alchemia.MODID, PEAK_CLUSTER_NAME));
#else
    public static final ResourceKey<ConfiguredFeature<?, ?>> PEAK_CLUSTER_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(Alchemia.MODID, PEAK_CLUSTER_NAME));
#endif
    public static final ResourceKey<PlacedFeature> PEAK_CLUSTER =
            ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(Alchemia.MODID, PEAK_CLUSTER_NAME));

    static {
        for (Ingredients ingredient : Ingredients.values()) {
            if (!generates(ingredient)) continue;
            String name = featureName(ingredient);
#if MC_VER >= 26300
            CONFIGURED_FEATURES.put(ingredient, ResourceKey.create(Registries.FEATURE, Identifier.fromNamespaceAndPath(Alchemia.MODID, name)));
#else
            CONFIGURED_FEATURES.put(ingredient, ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(Alchemia.MODID, name)));
#endif
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
            case UNDERGROUND, MAGMA, SULFUR -> "fungi_" + name;
            case AQUATIC -> "seabed_" + name;
            case SURFACE, NYLIUM, SAND -> "patch_wild_" + name;
        };
    }

#if MC_VER >= 26300
    public static void bootstrapConfiguredFeatures(BootstrapContext<Feature> context) {
#else
    public static void bootstrapConfiguredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
#endif
        HolderGetter<Block> blocks = context.lookup(Registries.BLOCK);

        for (Ingredients ingredient : Ingredients.values()) {
            if (!generates(ingredient)) continue;

            IngredientHabitat habitat = IngredientHabitat.of(ingredient);

            if (habitat == IngredientHabitat.GEODE) {
#if MC_VER >= 26300
                context.register(CONFIGURED_FEATURES.get(ingredient), crystalGeodeConfig(ingredient, blocks));
#else
                context.register(CONFIGURED_FEATURES.get(ingredient),
                        new ConfiguredFeature<>(Feature.GEODE, crystalGeodeConfig(ingredient, blocks)));
#endif
                continue;
            }

            Block block = BlockRegistry.getBlock(ingredient).get();
            BlockPredicate occupies = habitat == IngredientHabitat.AQUATIC
                    ? BlockPredicate.matchesFluids(Fluids.WATER)
                    : BlockPredicate.ONLY_IN_AIR_PREDICATE;

            BlockPredicate grownOn = switch (habitat) {
                case SURFACE -> BlockPredicate.matchesTag(BLOCK_BELOW, BlockTags.SUPPORTS_VEGETATION);
                case NYLIUM -> BlockPredicate.anyOf(
                        BlockPredicate.matchesTag(BLOCK_BELOW, BlockTags.SUPPORTS_VEGETATION),
                        BlockPredicate.matchesTag(BLOCK_BELOW, BlockTags.NYLIUM));
                case SAND -> BlockPredicate.matchesTag(BLOCK_BELOW, BlockTags.SAND);
                case UNDERGROUND -> BlockPredicate.matchesTag(BLOCK_BELOW, BlockTags.BASE_STONE_OVERWORLD);
#if MC_VER >= 26300
                case MAGMA -> BlockPredicate.wouldSurvive(block);
#else
                case MAGMA -> BlockPredicate.wouldSurvive(block.defaultBlockState(), Vec3i.ZERO);
#endif
#if MC_VER >= 26300
                case SULFUR -> BlockPredicate.matchesBlocks(BLOCK_BELOW, List.of(Blocks.NETHERRACK, Blocks.CRIMSON_NYLIUM, Blocks.SULFUR, Blocks.POTENT_SULFUR));
#elif MC_VER >= 26200
                case SULFUR -> BlockPredicate.matchesBlocks(BLOCK_BELOW, Blocks.NETHERRACK, Blocks.CRIMSON_NYLIUM, Blocks.SULFUR, Blocks.POTENT_SULFUR);
#else
                case SULFUR -> BlockPredicate.matchesBlocks(BLOCK_BELOW, Blocks.NETHERRACK, Blocks.CRIMSON_NYLIUM);
#endif
                case AQUATIC -> BlockPredicate.anyOf(
                        BlockPredicate.matchesTag(BLOCK_BELOW, BlockTags.SAND),
                        BlockPredicate.matchesTag(BLOCK_BELOW, BlockTags.DIRT),
#if MC_VER >= 26300
                        BlockPredicate.matchesBlocks(BLOCK_BELOW, List.of(Blocks.GRAVEL, Blocks.CLAY)));
#else
                        BlockPredicate.matchesBlocks(BLOCK_BELOW, Blocks.GRAVEL, Blocks.CLAY));
#endif
#if MC_VER >= 26300
                case CEILING -> BlockPredicate.wouldSurvive(block);
#else
                case CEILING -> BlockPredicate.wouldSurvive(block.defaultBlockState(), Vec3i.ZERO);
#endif
                case GEODE -> BlockPredicate.alwaysTrue();
            };

            BlockPredicate placedWhere = BlockPredicate.allOf(occupies, grownOn);

            WildHerbConfiguration config = switch (habitat) {
                case CEILING -> rootConfig(block, placedWhere);
                case MAGMA -> magmaConfig(block, placedWhere);
                case UNDERGROUND, SULFUR -> fungiConfig(block, placedWhere);
                case AQUATIC -> seabedConfig(block, placedWhere);
                default -> colonyConfig(block, placedWhere);
            };

#if MC_VER >= 26300
            context.register(CONFIGURED_FEATURES.get(ingredient), config);
        }

        context.register(PEAK_CLUSTER_CONFIGURED, new SimpleBlockFeature(BlockStateProvider.of(
                BlockRegistry.getGeodeCluster(Ingredients.Cloud_Crystal, BlockRegistry.BudSize.CLUSTER))));
#else
            context.register(CONFIGURED_FEATURES.get(ingredient),
                    new ConfiguredFeature<>(BiomeFeatureRegistry.WILD_HERB.get(), config));
        }

        context.register(PEAK_CLUSTER_CONFIGURED, new ConfiguredFeature<>(Feature.SIMPLE_BLOCK,
                new SimpleBlockConfiguration(BlockStateProvider.simple(
                        BlockRegistry.getGeodeCluster(Ingredients.Cloud_Crystal, BlockRegistry.BudSize.CLUSTER)))));
#endif
    }

    public static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> context) {
#if MC_VER >= 26300
        HolderGetter<Feature> configured = context.lookup(Registries.FEATURE);
#else
        HolderGetter<ConfiguredFeature<?, ?>> configured = context.lookup(Registries.CONFIGURED_FEATURE);
#endif

        for (Ingredients ingredient : Ingredients.values()) {
            if (!generates(ingredient)) continue;

            IngredientHabitat habitat = IngredientHabitat.of(ingredient);
            int chance = ingredient.getPatchChance();

            List<PlacementModifier> modifiers = switch (habitat) {
                case SURFACE, NYLIUM, SAND -> List.of(RarityFilter.onAverageOnceEvery(chance), InSquarePlacement.spread(),
                        PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
                case AQUATIC -> List.of(RarityFilter.onAverageOnceEvery(Math.max(1, chance / 2)), InSquarePlacement.spread(),
                        PlacementUtils.HEIGHTMAP_TOP_SOLID, BiomeFilter.biome());
                case CEILING -> List.of(CountPlacement.of(rootPatchCount(chance)), InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(8), VerticalAnchor.absolute(110)),
                        EnvironmentScanPlacement.scanningFor(Direction.UP, BlockPredicate.hasSturdyFace(Direction.DOWN), BlockPredicate.ONLY_IN_AIR_PREDICATE, 12),
#if MC_VER >= 26300
                        OffsetPlacement.vertical(ConstantInt.of(-1)),
#else
                        RandomOffsetPlacement.vertical(ConstantInt.of(-1)),
#endif
                        BiomeFilter.biome());
                case UNDERGROUND, MAGMA, SULFUR -> List.of(RarityFilter.onAverageOnceEvery(Math.max(1, chance / 4)),
                        CountPlacement.of(24), InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(8), VerticalAnchor.absolute(96)),
                        EnvironmentScanPlacement.scanningFor(Direction.DOWN, BlockPredicate.solid(), BlockPredicate.ONLY_IN_AIR_PREDICATE, 12),
#if MC_VER >= 26300
                        OffsetPlacement.vertical(ConstantInt.of(1)),
#else
                        RandomOffsetPlacement.vertical(ConstantInt.of(1)),
#endif
                        BiomeFilter.biome());
                case GEODE -> List.of(RarityFilter.onAverageOnceEvery(chance), InSquarePlacement.spread(),
                        getHeightRangePlacement(ingredient), BiomeFilter.biome());
            };

            context.register(PLACED_FEATURES.get(ingredient),
                    new PlacedFeature(configured.getOrThrow(CONFIGURED_FEATURES.get(ingredient)), modifiers));
        }

        context.register(PEAK_CLUSTER, new PlacedFeature(configured.getOrThrow(PEAK_CLUSTER_CONFIGURED), List.of(
                RarityFilter.onAverageOnceEvery(3),
                CountPlacement.of(UniformInt.of(2, 5)),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP,
                BlockPredicateFilter.forPredicate(BlockPredicate.allOf(
                        BlockPredicate.ONLY_IN_AIR_PREDICATE,
#if MC_VER >= 26300
                        BlockPredicate.solid(Direction.DOWN))),
#else
                        BlockPredicate.solid(BLOCK_BELOW))),
#endif
                BiomeFilter.biome())));
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

    public static WildHerbConfiguration magmaConfig(Block block, BlockPredicate growsOn) {
        return new WildHerbConfiguration(4, 4, 2, colonyBlockConfig(block, growsOn), plantBlockConfig(block, growsOn), Optional.empty());
    }

    public static WildHerbConfiguration fungiConfig(Block block, BlockPredicate growsOn) {
        return new WildHerbConfiguration(24, 4, 1, colonyBlockConfig(block, growsOn), plantBlockConfig(block, growsOn), Optional.empty());
    }

    public static WildHerbConfiguration rootConfig(Block block, BlockPredicate hangsFrom) {
        return new WildHerbConfiguration(ROOT_PATCH_TRIES, 4, 0, colonyBlockConfig(block, hangsFrom), plantBlockConfig(block, hangsFrom), Optional.empty());
    }

    private static int rootPatchCount(int chance) {
        return Math.max(1, ROOT_PATCHES_PER_CHUNK * ROOT_CHANCE_BASELINE / Math.max(1, chance));
    }

    static final BlockStateProvider NETHER_OUTER_SHELL = new WeightedStateProvider(WeightedList.<BlockState>builder()
            .add(Blocks.CRYING_OBSIDIAN.defaultBlockState(), 1)
            .add(Blocks.OBSIDIAN.defaultBlockState(), 4)
            .add(Blocks.BASALT.defaultBlockState(), 5)
            .build());

#if MC_VER >= 26300
    public static GeodeFeature crystalGeodeConfig(Ingredients ingredient, HolderGetter<Block> blocks) {
#else
    public static GeodeConfiguration crystalGeodeConfig(Ingredients ingredient, HolderGetter<Block> blocks) {
#endif
#if MC_VER >= 26200
        HolderSet<Block> cannotReplace = blocks.getOrThrow(BlockTags.FEATURES_CANNOT_REPLACE);
        HolderSet<Block> invalidBlocks = blocks.getOrThrow(BlockTags.GEODE_INVALID_BLOCKS);
#else
        TagKey<Block> cannotReplace = BlockTags.FEATURES_CANNOT_REPLACE;
        TagKey<Block> invalidBlocks = BlockTags.GEODE_INVALID_BLOCKS;
#endif

#if MC_VER >= 26300
        return new GeodeFeature(
                new GeodeBlockSettings(
                        BlockStateProvider.holderOf(Blocks.AIR.defaultBlockState()),
                        BlockStateProvider.holderOf(BlockRegistry.getGeodeBlock(ingredient).defaultBlockState()),
                        BlockStateProvider.holderOf(BlockRegistry.getGeodeBuddingBlock(ingredient).defaultBlockState()),
                        BlockStateProvider.holderOf(Blocks.CALCITE.defaultBlockState()),
                        ingredient.getPrimaryAlignment().equals(IngredientAlignment.Fire)
                                ? net.minecraft.core.Holder.direct(NETHER_OUTER_SHELL)
                                : BlockStateProvider.holderOf(Blocks.SMOOTH_BASALT.defaultBlockState()),
#else
        return new GeodeConfiguration(
                new GeodeBlockSettings(
                        BlockStateProvider.simple(Blocks.AIR.defaultBlockState()),
                        BlockStateProvider.simple(BlockRegistry.getGeodeBlock(ingredient).defaultBlockState()),
                        BlockStateProvider.simple(BlockRegistry.getGeodeBuddingBlock(ingredient).defaultBlockState()),
                        BlockStateProvider.simple(Blocks.CALCITE.defaultBlockState()),
                        ingredient.getPrimaryAlignment().equals(IngredientAlignment.Fire) ? NETHER_OUTER_SHELL : BlockStateProvider.simple(Blocks.SMOOTH_BASALT.defaultBlockState()),
#endif
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

#if MC_VER >= 26300
    public static net.minecraft.core.Holder<PlacedFeature> plantBlockConfig(Block block, BlockPredicate placedWhere) {
        return PlacementUtils.inlinePlaced(
                net.minecraft.core.Holder.direct(new SimpleBlockFeature(BlockStateProvider.of(block))),
                List.of(BlockPredicateFilter.forPredicate(placedWhere)));
    }

    public static net.minecraft.core.Holder<PlacedFeature> colonyBlockConfig(Block block, BlockPredicate placedWhere) {
        return PlacementUtils.inlinePlaced(
                net.minecraft.core.Holder.direct(new SimpleBlockFeature(net.minecraft.core.Holder.direct(
                        new RandomizedIntStateProvider(BlockStateProvider.of(block), BlockIngredient.AGE, UniformInt.of(0, 3))))),
                List.of(BlockPredicateFilter.forPredicate(placedWhere)));
    }
#else
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
#endif
}
