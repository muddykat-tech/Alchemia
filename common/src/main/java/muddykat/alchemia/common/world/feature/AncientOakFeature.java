package muddykat.alchemia.common.world.feature;

#if MC_VER < 26300
import com.mojang.serialization.Codec;
import muddykat.alchemia.common.world.configuration.AncientOakConfiguration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class AncientOakFeature extends Feature<AncientOakConfiguration> {
    public AncientOakFeature(Codec<AncientOakConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<AncientOakConfiguration> context) {
        return context.config().grow(context.level(), context.random(), context.origin());
    }
}
#endif
