package muddykat.alchemia.common.world.configuration;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import muddykat.alchemia.common.blocks.BlockInfectedLog;
import muddykat.alchemia.common.blocks.BlockIngredientShelf;
import muddykat.alchemia.common.blocks.helper.InfectableWood;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.registration.registers.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
#if MC_VER >= 26300
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
#else
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
#endif

import java.util.ArrayList;
import java.util.List;

public record ShelfInfectionConfiguration(Ingredients fungus, int attempts, int spread)
#if MC_VER >= 26300
        implements Feature {
#else
        implements FeatureConfiguration {
#endif

    public static final int MAX_TRUNK = 32;

    public static final MapCodec<ShelfInfectionConfiguration> CODEC = RecordCodecBuilder.mapCodec(config -> config.group(
            CrystalGeodeConfiguration.MINERAL.fieldOf("fungus").forGetter(ShelfInfectionConfiguration::fungus),
            ExtraCodecs.POSITIVE_INT.fieldOf("attempts").orElse(48).forGetter(ShelfInfectionConfiguration::attempts),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("spread").orElse(6).forGetter(ShelfInfectionConfiguration::spread)
    ).apply(config, ShelfInfectionConfiguration::new));

#if MC_VER >= 26300
    @Override
    public MapCodec<ShelfInfectionConfiguration> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        return placeInfection(level, random, origin);
    }
#endif

    public boolean placeInfection(WorldGenLevel level, RandomSource random, BlockPos origin) {
        for (int attempt = 0; attempt < attempts; attempt++) {
            int x = origin.getX() + random.nextInt(spread * 2 + 1) - spread;
            int z = origin.getZ() + random.nextInt(spread * 2 + 1) - spread;
            if (infectTrunk(level, random, trunkAt(level, x, z))) return true;
        }
        return false;
    }

    private static List<BlockPos> trunkAt(WorldGenLevel level, int x, int z) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z), z);
#if MC_VER >= 26102
        int floor = level.getMinY();
#else
        int floor = level.getMinBuildHeight();
#endif

        while (cursor.getY() > floor && isCanopy(level.getBlockState(cursor.below()))) cursor.move(Direction.DOWN);

        List<BlockPos> trunk = new ArrayList<>();
        InfectableWood.Wood wood = InfectableWood.of(level.getBlockState(cursor).getBlock());
        if (wood == null) return trunk;

        while (trunk.size() < MAX_TRUNK && level.getBlockState(cursor).is(wood.log())) {
            trunk.add(cursor.immutable());
            cursor.move(Direction.UP);
        }
        return trunk;
    }

    private static boolean isCanopy(BlockState state) {
        return state.isAir() || state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS);
    }

    private boolean infectTrunk(WorldGenLevel level, RandomSource random, List<BlockPos> trunk) {
        if (trunk.isEmpty()) return false;

        InfectableWood.Wood wood = InfectableWood.of(level.getBlockState(trunk.get(0)).getBlock());
        BlockInfectedLog infected = wood == null ? null : wood.infected(fungus);
        if (infected == null) return false;

        List<BlockPos> hosts = new ArrayList<>();
        for (BlockPos log : trunk) {
            if (hasOpenSide(level, log) && infected.isLivingTree(level, log)) hosts.add(log);
        }
        if (hosts.isEmpty()) return false;

        int first = random.nextInt(hosts.size());
        int count = 1 + random.nextInt(Math.min(2, hosts.size() - first));
        BlockIngredientShelf shelf = (BlockIngredientShelf) BlockRegistry.getBlock(fungus).get();

        for (int index = first; index < first + count; index++) {
            BlockPos host = hosts.get(index);
            level.setBlock(host, infected.infect(level.getBlockState(host)), 2);

            for (Direction side : Direction.Plane.HORIZONTAL) {
                BlockPos target = host.relative(side);
                if (!level.isEmptyBlock(target) || random.nextBoolean()) continue;
                level.setBlock(target, shelf.facing(side).setValue(shelf.getAgeProperty(), random.nextInt(shelf.getMaxAge() + 1)), 2);
            }
        }
        return true;
    }

    private static boolean hasOpenSide(WorldGenLevel level, BlockPos log) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (level.isEmptyBlock(log.relative(side))) return true;
        }
        return false;
    }
}
