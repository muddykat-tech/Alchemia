package muddykat.alchemia.common.world.configuration;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import muddykat.alchemia.common.world.AncientOakShape;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
#if MC_VER >= 26300
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
#else
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
#endif

public record AncientOakConfiguration(int minHeight, int heightSpread)
#if MC_VER >= 26300
        implements Feature {
#else
        implements FeatureConfiguration {
#endif

    public static final MapCodec<AncientOakConfiguration> CODEC = RecordCodecBuilder.mapCodec(config -> config.group(
            ExtraCodecs.POSITIVE_INT.fieldOf("min_height").orElse(AncientOakShape.MIN_HEIGHT).forGetter(AncientOakConfiguration::minHeight),
            ExtraCodecs.POSITIVE_INT.fieldOf("height_spread").orElse(AncientOakShape.HEIGHT_SPREAD).forGetter(AncientOakConfiguration::heightSpread)
    ).apply(config, AncientOakConfiguration::new));

#if MC_VER >= 26300
    @Override
    public MapCodec<AncientOakConfiguration> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        return grow(level, random, origin);
    }
#endif

    public boolean grow(WorldGenLevel level, RandomSource random, BlockPos origin) {
        return AncientOakShape.place(level, random, origin, minHeight, heightSpread);
    }
}
