package muddykat.alchemia.common.world.configuration;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
#if MC_VER >= 26300
import net.minecraft.world.level.levelgen.feature.Feature;
#else
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
#endif

import java.util.Optional;

public record WildHerbConfiguration(int tries, int xzSpread, int ySpread, Holder<PlacedFeature> primaryFeature,
                                    Holder<PlacedFeature> secondaryFeature, Optional<Holder<PlacedFeature>> floorFeature)
#if MC_VER >= 26300
        implements Feature {
#else
        implements FeatureConfiguration {
#endif

    public static final MapCodec<WildHerbConfiguration> CODEC = RecordCodecBuilder.mapCodec(config -> config.group(
            ExtraCodecs.POSITIVE_INT.fieldOf("tries").orElse(64).forGetter(WildHerbConfiguration::tries),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("xz_spread").orElse(4).forGetter(WildHerbConfiguration::xzSpread),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("y_spread").orElse(3).forGetter(WildHerbConfiguration::ySpread),
            PlacedFeature.CODEC.fieldOf("primary_feature").forGetter(WildHerbConfiguration::primaryFeature),
            PlacedFeature.CODEC.fieldOf("secondary_feature").forGetter(WildHerbConfiguration::secondaryFeature),
            PlacedFeature.CODEC.optionalFieldOf("floor_feature").forGetter(WildHerbConfiguration::floorFeature)
    ).apply(config, WildHerbConfiguration::new));

#if MC_VER >= 26300
    @Override
    public MapCodec<WildHerbConfiguration> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        return placeHerbs(level, chunkGenerator, random, origin);
    }
#endif

    public boolean placeHerbs(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        int placed = 0;
        int xzRange = xzSpread + 1;
        int yRange = ySpread + 1;

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        if (floorFeature.isPresent()) {
            for (int i = 0; i < tries; ++i) {
                mutablePos.setWithOffset(origin, random.nextInt(xzRange) - random.nextInt(xzRange), random.nextInt(yRange) - random.nextInt(yRange), random.nextInt(xzRange) - random.nextInt(xzRange));
                if (floorFeature.get().value().place(level, chunkGenerator, random, mutablePos)) {
                    ++placed;
                }
            }
        }

        int shorterXZ = Math.max(1, xzRange - 2);
        for (int i = 0; i < tries; ++i) {
            mutablePos.setWithOffset(origin, random.nextInt(shorterXZ) - random.nextInt(shorterXZ), random.nextInt(yRange) - random.nextInt(yRange), random.nextInt(shorterXZ) - random.nextInt(shorterXZ));
            if (primaryFeature.value().place(level, chunkGenerator, random, mutablePos)) {
                ++placed;
            }
        }

        for (int i = 0; i < tries; ++i) {
            mutablePos.setWithOffset(origin, random.nextInt(xzRange) - random.nextInt(xzRange), random.nextInt(yRange) - random.nextInt(yRange), random.nextInt(xzRange) - random.nextInt(xzRange));
            if (secondaryFeature.value().place(level, chunkGenerator, random, mutablePos)) {
                ++placed;
            }
        }

        return placed > 0;
    }
}
