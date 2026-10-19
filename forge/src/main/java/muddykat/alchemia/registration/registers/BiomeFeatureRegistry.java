package muddykat.alchemia.registration.registers;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.world.configuration.CrystalGeodeConfiguration;
import muddykat.alchemia.common.world.configuration.ShelfInfectionConfiguration;
import muddykat.alchemia.common.world.configuration.WildHerbConfiguration;
import muddykat.alchemia.common.world.configuration.WildPlantConfiguration;
import muddykat.alchemia.common.world.feature.CrystalGeodeFeature;
import muddykat.alchemia.common.world.feature.ShelfInfectionFeature;
import muddykat.alchemia.common.world.feature.WildHerbFeature;
import muddykat.alchemia.common.world.feature.WildPlantFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import muddykat.alchemia.common.world.configuration.AncientOakConfiguration;
import muddykat.alchemia.common.world.feature.AncientOakFeature;

public class BiomeFeatureRegistry {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Alchemia.MODID);

    public static final RegistryObject<Feature<WildHerbConfiguration>> WILD_HERB =
            FEATURES.register("wild_herb", () -> new WildHerbFeature(WildHerbConfiguration.CODEC.codec()));

    public static final RegistryObject<Feature<CrystalGeodeConfiguration>> CRYSTAL_GEODE =
            FEATURES.register("crystal_geode", () -> new CrystalGeodeFeature(CrystalGeodeConfiguration.CODEC.codec()));

    public static final RegistryObject<Feature<WildPlantConfiguration>> WILD_PLANT =
            FEATURES.register("wild_plant", () -> new WildPlantFeature(WildPlantConfiguration.CODEC.codec()));

    public static final RegistryObject<Feature<AncientOakConfiguration>> ANCIENT_OAK =
            FEATURES.register("ancient_oak", () -> new AncientOakFeature(AncientOakConfiguration.CODEC.codec()));

    public static final RegistryObject<Feature<ShelfInfectionConfiguration>> SHELF_INFECTION =
            FEATURES.register("shelf_infection", () -> new ShelfInfectionFeature(ShelfInfectionConfiguration.CODEC.codec()));
}
