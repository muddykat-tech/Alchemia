package muddykat.alchemia.registration.registers;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.world.configuration.AncientOakConfiguration;
import muddykat.alchemia.common.world.configuration.CrystalGeodeConfiguration;
import muddykat.alchemia.common.world.configuration.ShelfInfectionConfiguration;
import muddykat.alchemia.common.world.configuration.WildHerbConfiguration;
import muddykat.alchemia.common.world.configuration.WildPlantConfiguration;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
#if MC_VER >= 26300
import com.mojang.serialization.MapCodec;
#else
import muddykat.alchemia.common.world.feature.AncientOakFeature;
import muddykat.alchemia.common.world.feature.CrystalGeodeFeature;
import muddykat.alchemia.common.world.feature.ShelfInfectionFeature;
import muddykat.alchemia.common.world.feature.WildHerbFeature;
import muddykat.alchemia.common.world.feature.WildPlantFeature;
#endif

public class BiomeFeatureRegistry {
#if MC_VER >= 26300
    public static final DeferredRegister<MapCodec<? extends Feature>> FEATURES =
            DeferredRegister.create(Registries.FEATURE_TYPE, Alchemia.MODID);

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<WildHerbConfiguration>> WILD_HERB =
            FEATURES.register("wild_herb", () -> WildHerbConfiguration.CODEC);

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<CrystalGeodeConfiguration>> CRYSTAL_GEODE =
            FEATURES.register("crystal_geode", () -> CrystalGeodeConfiguration.CODEC);

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<WildPlantConfiguration>> WILD_PLANT =
            FEATURES.register("wild_plant", () -> WildPlantConfiguration.CODEC);

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ShelfInfectionConfiguration>> SHELF_INFECTION =
            FEATURES.register("shelf_infection", () -> ShelfInfectionConfiguration.CODEC);

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<AncientOakConfiguration>> ANCIENT_OAK =
            FEATURES.register("ancient_oak", () -> AncientOakConfiguration.CODEC);
#else
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Alchemia.MODID);

    public static final DeferredHolder<Feature<?>, Feature<WildHerbConfiguration>> WILD_HERB =
            FEATURES.register("wild_herb", () -> new WildHerbFeature(WildHerbConfiguration.CODEC.codec()));

    public static final DeferredHolder<Feature<?>, Feature<CrystalGeodeConfiguration>> CRYSTAL_GEODE =
            FEATURES.register("crystal_geode", () -> new CrystalGeodeFeature(CrystalGeodeConfiguration.CODEC.codec()));

    public static final DeferredHolder<Feature<?>, Feature<WildPlantConfiguration>> WILD_PLANT =
            FEATURES.register("wild_plant", () -> new WildPlantFeature(WildPlantConfiguration.CODEC.codec()));

    public static final DeferredHolder<Feature<?>, Feature<ShelfInfectionConfiguration>> SHELF_INFECTION =
            FEATURES.register("shelf_infection", () -> new ShelfInfectionFeature(ShelfInfectionConfiguration.CODEC.codec()));

    public static final DeferredHolder<Feature<?>, Feature<AncientOakConfiguration>> ANCIENT_OAK =
            FEATURES.register("ancient_oak", () -> new AncientOakFeature(AncientOakConfiguration.CODEC.codec()));
#endif
}
