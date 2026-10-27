package muddykat.alchemia.common.world.structure;

#if MC_VER >= 12100
import com.mojang.serialization.MapCodec;
#else
import com.mojang.serialization.Codec;
#endif
import muddykat.alchemia.registration.registers.StructureRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Arrays;
import java.util.Optional;

public class ForgottenShackStructure extends Structure {

#if MC_VER >= 12100
    public static final MapCodec<ForgottenShackStructure> CODEC = simpleCodec(ForgottenShackStructure::new);
#else
    public static final Codec<ForgottenShackStructure> CODEC = simpleCodec(ForgottenShackStructure::new);
#endif

    private static final int[] SAMPLE_X = {2, 5, 8};
    private static final int[] SAMPLE_Z = {1, 6, 11};
    private static final int LAND_SLOPE = 5;
    private static final int MARSH_SLOPE = 3;
    private static final int MARSH_DEPTH = 3;
    private static final int JITTER = 4;

    public ForgottenShackStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        Rotation rotation = Rotation.getRandom(context.random());
        int centreX = context.chunkPos().getMinBlockX() + 8 + context.random().nextInt(JITTER * 2 + 1) - JITTER;
        int centreZ = context.chunkPos().getMinBlockZ() + 8 + context.random().nextInt(JITTER * 2 + 1) - JITTER;
        long seed = context.random().nextLong();
        BlockPos middle = ShackCanvas.offset(rotation, ShackPiece.WIDTH / 2, 0, ShackPiece.DEPTH / 2);
        BlockPos origin = new BlockPos(centreX - middle.getX(), 0, centreZ - middle.getZ());

        int[] ground = new int[SAMPLE_X.length * SAMPLE_Z.length];
        int[] surface = new int[ground.length];
        int index = 0;
        for (int x : SAMPLE_X) {
            for (int z : SAMPLE_Z) {
                BlockPos at = origin.offset(ShackCanvas.offset(rotation, x, 0, z));
                ground[index] = height(context, at, Heightmap.Types.OCEAN_FLOOR_WG);
                surface[index] = height(context, at, Heightmap.Types.WORLD_SURFACE_WG);
                index++;
            }
        }

        int[] sorted = ground.clone();
        Arrays.sort(sorted);
        int low = sorted[0];
        int high = sorted[sorted.length - 1];
        int deepest = 0;
        int waterline = Integer.MIN_VALUE;
        for (int sample = 0; sample < ground.length; sample++) {
            deepest = Math.max(deepest, surface[sample] - ground[sample]);
            waterline = Math.max(waterline, surface[sample]);
        }

        ShackStyle style = ShackStyle.of(biome(context, centreX, sorted[sorted.length / 2], centreZ));

        int floor;
        int cellar;
        if (style.raised()) {
            if (high - low > MARSH_SLOPE || deepest > MARSH_DEPTH) return Optional.empty();
            floor = waterline + 1;
            cellar = low - 2 - floor;
        } else {
            if (high - low > LAND_SLOPE || deepest > 0) return Optional.empty();
            floor = sorted[sorted.length / 2] - 1;
            cellar = 0;
        }
#if MC_VER >= 26100
        int bottom = context.heightAccessor().getMinY();
        int ceiling = context.heightAccessor().getMaxY();
#else
        int bottom = context.heightAccessor().getMinBuildHeight();
        int ceiling = context.heightAccessor().getMaxBuildHeight();
#endif
        if (floor + cellar - ShackPiece.CELLAR_DROP <= bottom + 4 || floor + ShackPiece.TOP >= ceiling) {
            return Optional.empty();
        }

        BlockPos pivot = origin.atY(floor);
        int depth = cellar;
        return Optional.of(new GenerationStub(new BlockPos(centreX, floor, centreZ),
                builder -> builder.addPiece(new ShackPiece(style, rotation, pivot, depth, seed))));
    }

    private static Holder<Biome> biome(GenerationContext context, int x, int y, int z) {
#if MC_VER >= 26300
        return context.biomeResolver().getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z));
#else
        return context.biomeSource().getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z), context.randomState().sampler());
#endif
    }

    private static int height(GenerationContext context, BlockPos at, Heightmap.Types type) {
        return context.chunkGenerator().getBaseHeight(at.getX(), at.getZ(), type, context.heightAccessor(), context.randomState());
    }

    @Override
    public StructureType<?> type() {
        return StructureRegistry.FORGOTTEN_SHACK.get();
    }
}
