package muddykat.alchemia.common.world.feature;

#if MC_VER < 26300
import com.mojang.serialization.Codec;
import muddykat.alchemia.common.world.configuration.WildHerbConfiguration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class WildHerbFeature extends Feature<WildHerbConfiguration> {
    public WildHerbFeature(Codec<WildHerbConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<WildHerbConfiguration> context) {
        return context.config().placeHerbs(context.level(), context.chunkGenerator(), context.random(), context.origin());
    }
}
#endif
