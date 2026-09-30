package muddykat.alchemia.common.world.feature;

#if MC_VER < 26300
import com.mojang.serialization.Codec;
import muddykat.alchemia.common.world.configuration.CrystalGeodeConfiguration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class CrystalGeodeFeature extends Feature<CrystalGeodeConfiguration> {
    public CrystalGeodeFeature(Codec<CrystalGeodeConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<CrystalGeodeConfiguration> context) {
        return context.config().placeGeode(context.level(), context.random(), context.origin());
    }
}
#endif
