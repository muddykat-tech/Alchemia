package muddykat.alchemia.common.world.feature;

#if MC_VER < 26300
import com.mojang.serialization.Codec;
import muddykat.alchemia.common.world.configuration.ShelfInfectionConfiguration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class ShelfInfectionFeature extends Feature<ShelfInfectionConfiguration> {
    public ShelfInfectionFeature(Codec<ShelfInfectionConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<ShelfInfectionConfiguration> context) {
        return context.config().placeInfection(context.level(), context.random(), context.origin());
    }
}
#endif
