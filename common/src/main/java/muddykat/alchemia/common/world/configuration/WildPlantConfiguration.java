package muddykat.alchemia.common.world.configuration;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import muddykat.alchemia.common.blocks.WildGrowth;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
#if MC_VER >= 26300
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
#else
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
#endif

public record WildPlantConfiguration(BlockState plant)
#if MC_VER >= 26300
        implements Feature {
#else
        implements FeatureConfiguration {
#endif

    public static final MapCodec<WildPlantConfiguration> CODEC = RecordCodecBuilder.mapCodec(config -> config.group(
            BlockState.CODEC.fieldOf("plant").forGetter(WildPlantConfiguration::plant)
    ).apply(config, WildPlantConfiguration::new));

#if MC_VER >= 26300
    @Override
    public MapCodec<WildPlantConfiguration> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        return placePlant(level, random, origin);
    }
#endif

    public boolean placePlant(WorldGenLevel level, RandomSource random, BlockPos origin) {
        if (plant.getBlock() instanceof WildGrowth growth) return growth.growWild(level, origin, plant, random);
        if (!plant.canSurvive(level, origin)) return false;

        level.setBlock(origin, plant, 2);
        return true;
    }
}
