package muddykat.alchemia.common.world.feature;

#if MC_VER < 26300
import com.mojang.serialization.Codec;
import muddykat.alchemia.common.world.configuration.WildPlantConfiguration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class WildPlantFeature extends Feature<WildPlantConfiguration> {
    public WildPlantFeature(Codec<WildPlantConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<WildPlantConfiguration> context) {
        return context.config().placePlant(context.level(), context.random(), context.origin());
    }
}
#endif
