package muddykat.alchemia.common.world;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.world.configuration.AncientOakConfiguration;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.placement.SurfaceWaterDepthFilter;
#if MC_VER >= 26102
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
#elif MC_VER >= 12100
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceLocation;
#else
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceLocation;
#endif
#if MC_VER < 26300
import muddykat.alchemia.registration.registers.BiomeFeatureRegistry;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
#endif

import java.util.List;

public final class AncientOakGeneration {

    public static final String NAME = "ancient_oak";
    public static final int CHUNKS_PER_TREE = 120;

#if MC_VER >= 26300
    public static final ResourceKey<Feature> CONFIGURED = ResourceKey.create(Registries.FEATURE, id());
#else
    public static final ResourceKey<ConfiguredFeature<?, ?>> CONFIGURED = ResourceKey.create(Registries.CONFIGURED_FEATURE, id());
#endif
    public static final ResourceKey<PlacedFeature> PLACED = ResourceKey.create(Registries.PLACED_FEATURE, id());

    private AncientOakGeneration() {
    }

#if MC_VER >= 26102
    private static Identifier id() {
        return Identifier.fromNamespaceAndPath(Alchemia.MODID, NAME);
    }
#elif MC_VER >= 12100
    private static ResourceLocation id() {
        return ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, NAME);
    }
#else
    private static ResourceLocation id() {
        return new ResourceLocation(Alchemia.MODID, NAME);
    }
#endif

#if MC_VER >= 26300
    public static void bootstrapConfigured(BootstrapContext<Feature> context) {
        context.register(CONFIGURED, new AncientOakConfiguration(AncientOakShape.MIN_HEIGHT, AncientOakShape.HEIGHT_SPREAD));
    }

    public static void bootstrapPlaced(BootstrapContext<PlacedFeature> context, HolderGetter<Feature> configured) {
#elif MC_VER >= 12100
    public static void bootstrapConfigured(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        context.register(CONFIGURED, new ConfiguredFeature<>(BiomeFeatureRegistry.ANCIENT_OAK.get(),
                new AncientOakConfiguration(AncientOakShape.MIN_HEIGHT, AncientOakShape.HEIGHT_SPREAD)));
    }

    public static void bootstrapPlaced(BootstrapContext<PlacedFeature> context, HolderGetter<ConfiguredFeature<?, ?>> configured) {
#else
    public static void bootstrapConfigured(BootstapContext<ConfiguredFeature<?, ?>> context) {
        context.register(CONFIGURED, new ConfiguredFeature<>(BiomeFeatureRegistry.ANCIENT_OAK.get(),
                new AncientOakConfiguration(AncientOakShape.MIN_HEIGHT, AncientOakShape.HEIGHT_SPREAD)));
    }

    public static void bootstrapPlaced(BootstapContext<PlacedFeature> context, HolderGetter<ConfiguredFeature<?, ?>> configured) {
#endif
        context.register(PLACED, new PlacedFeature(configured.getOrThrow(CONFIGURED), List.of(
                RarityFilter.onAverageOnceEvery(CHUNKS_PER_TREE),
                InSquarePlacement.spread(),
                SurfaceWaterDepthFilter.forMaxDepth(0),
                PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
                BiomeFilter.biome())));
    }
}
