package muddykat.alchemia.registration.registers;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.world.configuration.WildHerbConfiguration;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
#if MC_VER >= 26300
import com.mojang.serialization.MapCodec;
#else
import muddykat.alchemia.common.world.feature.WildHerbFeature;
#endif

public class BiomeFeatureRegistry {
#if MC_VER >= 26300
    public static final DeferredRegister<MapCodec<? extends Feature>> FEATURES =
            DeferredRegister.create(Registries.FEATURE_TYPE, Alchemia.MODID);

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<WildHerbConfiguration>> WILD_HERB =
            FEATURES.register("wild_herb", () -> WildHerbConfiguration.CODEC);
#else
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Alchemia.MODID);

    public static final DeferredHolder<Feature<?>, Feature<WildHerbConfiguration>> WILD_HERB =
            FEATURES.register("wild_herb", () -> new WildHerbFeature(WildHerbConfiguration.CODEC.codec()));
#endif
}
