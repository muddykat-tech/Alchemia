package muddykat.alchemia.common.world.feature;

import com.mojang.serialization.Codec;
import muddykat.alchemia.common.world.configuration.WildHerbConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.Optional;

public class WildHerbFeature extends Feature<WildHerbConfiguration> {
    public WildHerbFeature(Codec<WildHerbConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<WildHerbConfiguration> context) {
        WildHerbConfiguration config = context.config();
        BlockPos origin = context.origin();
        WorldGenLevel level = context.level();
        RandomSource random = context.random();

        int placed = 0;
        int tries = config.tries();
        int xzSpread = config.xzSpread() + 1;
        int ySpread = config.ySpread() + 1;

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        Optional<Holder<PlacedFeature>> floorFeature = config.floorFeature();
        if (floorFeature.isPresent()) {
            for (int i = 0; i < tries; ++i) {
                mutablePos.setWithOffset(origin, random.nextInt(xzSpread) - random.nextInt(xzSpread), random.nextInt(ySpread) - random.nextInt(ySpread), random.nextInt(xzSpread) - random.nextInt(xzSpread));
                if (floorFeature.get().value().place(level, context.chunkGenerator(), random, mutablePos)) {
                    ++placed;
                }
            }
        }

        int shorterXZ = Math.max(1, xzSpread - 2);
        for (int i = 0; i < tries; ++i) {
            mutablePos.setWithOffset(origin, random.nextInt(shorterXZ) - random.nextInt(shorterXZ), random.nextInt(ySpread) - random.nextInt(ySpread), random.nextInt(shorterXZ) - random.nextInt(shorterXZ));
            if (config.primaryFeature().value().place(level, context.chunkGenerator(), random, mutablePos)) {
                ++placed;
            }
        }

        for (int i = 0; i < tries; ++i) {
            mutablePos.setWithOffset(origin, random.nextInt(xzSpread) - random.nextInt(xzSpread), random.nextInt(ySpread) - random.nextInt(ySpread), random.nextInt(xzSpread) - random.nextInt(xzSpread));
            if (config.secondaryFeature().value().place(level, context.chunkGenerator(), random, mutablePos)) {
                ++placed;
            }
        }

        return placed > 0;
    }
}
