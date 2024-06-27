package muddykat.alchemia.registration.registers;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.world.configuration.WildHerbConfiguration;
import muddykat.alchemia.common.world.feature.WildHerbFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AlchemiaBiomeFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, Alchemia.MODID);
    public static final RegistryObject<Feature<WildHerbConfiguration>> WILD_HERB = FEATURES.register("wild_herb", () -> new WildHerbFeature(WildHerbConfiguration.CODEC));
}
