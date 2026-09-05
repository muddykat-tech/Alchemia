package muddykat.alchemia.registration.registers;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.world.configuration.WildHerbConfiguration;
import muddykat.alchemia.common.world.feature.WildHerbFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BiomeFeatureRegistry {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Alchemia.MODID);

    public static final DeferredHolder<Feature<?>, Feature<WildHerbConfiguration>> WILD_HERB =
            FEATURES.register("wild_herb", () -> new WildHerbFeature(WildHerbConfiguration.CODEC));
}
