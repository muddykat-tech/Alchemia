package muddykat.alchemia.common.world.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.Optional;

public record WildHerbConfiguration(int tries, int xzSpread, int ySpread, Holder<PlacedFeature> primaryFeature,
                                    Holder<PlacedFeature> secondaryFeature, Optional<Holder<PlacedFeature>> floorFeature)
        implements FeatureConfiguration {

    public static final Codec<WildHerbConfiguration> CODEC = RecordCodecBuilder.create(config -> config.group(
            ExtraCodecs.POSITIVE_INT.fieldOf("tries").orElse(64).forGetter(WildHerbConfiguration::tries),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("xz_spread").orElse(4).forGetter(WildHerbConfiguration::xzSpread),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("y_spread").orElse(3).forGetter(WildHerbConfiguration::ySpread),
            PlacedFeature.CODEC.fieldOf("primary_feature").forGetter(WildHerbConfiguration::primaryFeature),
            PlacedFeature.CODEC.fieldOf("secondary_feature").forGetter(WildHerbConfiguration::secondaryFeature),
            PlacedFeature.CODEC.optionalFieldOf("floor_feature").forGetter(WildHerbConfiguration::floorFeature)
    ).apply(config, WildHerbConfiguration::new));
}
