package muddykat.alchemia.common.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.registration.registers.ParticleTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WildLands extends SavedData {

    private static final int BELOW = 16;
    private static final int ABOVE = 24;
    private static final int OWN_SHARE = 4;
    private static final int SPILL_SHARE = 1;
    private static final float SURROUND_EASE = 0.75F;
    private static final int ENCLOSED = 6;
    private static final int MAX_SPREAD = 12;
    private static final int BORDERED_SIDES = 3;
    private static final int COLUMNS = 16;
    private static final int MIN_LAND = 4;
    private static final int[][] SIDES = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private static final int[][] RING = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

    public static final Codec<WildLands> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("progress", Map.of()).forGetter(data -> data.progress)
    ).apply(instance, WildLands::new));

    private static final String NAME = Alchemia.MODID + "_wild_lands";

    private final Map<String, Integer> progress;

    public WildLands() {
        this(Map.of());
    }

    private static WildLands load(CompoundTag tag) {
        return CODEC.parse(NbtOps.INSTANCE, tag).result().orElseGet(WildLands::new);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CODEC.encodeStart(NbtOps.INSTANCE, this).result().ifPresent(encoded -> {
            if (encoded instanceof CompoundTag compound) tag.merge(compound);
        });
        return tag;
    }

    private WildLands(Map<String, Integer> progress) {
        this.progress = new HashMap<>(progress);
    }

    public static void converted(ServerLevel level, BlockPos pos, int recipeTier, int strength) {
        feedAll(level, pos, WildGreening.Counts.CONVERSION, recipeTier, strength);
        feedAll(level, pos, WildGreening.Counts.GROWTH, recipeTier, strength);
    }

    public static void bloomed(ServerLevel level, BlockPos pos, int strength) {
        feedAll(level, pos, WildGreening.Counts.GROWTH, 0, strength);
    }

    private static void feedAll(ServerLevel level, BlockPos pos, WildGreening.Counts kind, int recipeTier, int strength) {
        for (WildGreening.Named named : WildGreening.all(level.registryAccess())) {
            if (named.greening().fedBy(kind, recipeTier, strength)) nurture(level, pos, named);
        }
    }

    private static void nurture(ServerLevel level, BlockPos pos, WildGreening.Named named) {
        WildGreening greening = named.greening();
        int quartX = QuartPos.fromBlock(pos.getX());
        int quartY = QuartPos.fromBlock(pos.getY());
        int quartZ = QuartPos.fromBlock(pos.getZ());
        WildLands data = level.getDataStorage().computeIfAbsent(WildLands::load, WildLands::new, NAME);
        Deque<long[]> ripe = new ArrayDeque<>();
        if (greening.matches(level.getNoiseBiome(quartX, quartY, quartZ))
                && data.feed(level, named, quartX, quartY, quartZ, OWN_SHARE) && grassy(level, pos.getY(), quartX, quartZ, greening.grass())) {
            ripe.add(new long[]{quartX, quartZ});
        }
        for (int[] side : SIDES) {
            int x = quartX + side[0];
            int z = quartZ + side[1];
            if (!greening.matches(level.getNoiseBiome(x, quartY, z))) continue;
            if (data.feed(level, named, x, quartY, z, SPILL_SHARE) && grassy(level, pos.getY(), x, z, greening.grass())) ripe.add(new long[]{x, z});
        }
        for (int[] offset : RING) {
            int x = quartX + offset[0];
            int z = quartZ + offset[1];
            if (!greening.matches(level.getNoiseBiome(x, quartY, z))) continue;
            if (surround(level, greening, x, quartY, z) >= ENCLOSED && grassy(level, pos.getY(), x, z, greening.grass())) ripe.add(new long[]{x, z});
        }
        data.setDirty();
        if (!ripe.isEmpty()) data.spread(level, pos, named, ripe);
    }

    private boolean feed(ServerLevel level, WildGreening.Named named, int quartX, int quartY, int quartZ, int amount) {
        String key = key(named, quartX, quartZ);
        int needed = needed(level, named.greening(), quartX, quartY, quartZ);
        int count = Math.min(progress.getOrDefault(key, 0) + amount, needed);
        progress.put(key, count);
        return count >= needed;
    }

    private static int needed(ServerLevel level, WildGreening greening, int quartX, int quartY, int quartZ) {
        float ease = 1.0F - SURROUND_EASE * surround(level, greening, quartX, quartY, quartZ) / (float) RING.length;
        return Math.max(OWN_SHARE, Math.round(greening.steps() * OWN_SHARE * ease));
    }

    private static int surround(ServerLevel level, WildGreening greening, int quartX, int quartY, int quartZ) {
        int count = 0;
        for (int[] offset : RING) {
            if (greening.greener(level.getNoiseBiome(quartX + offset[0], quartY, quartZ + offset[1]))) count++;
        }
        return count;
    }

    private static boolean grassy(ServerLevel level, int y, int quartX, int quartZ, int needed) {
        if (needed <= 0) return true;
        if (!level.hasChunk(QuartPos.toSection(quartX), QuartPos.toSection(quartZ))) return false;
        int baseX = QuartPos.toBlock(quartX);
        int baseZ = QuartPos.toBlock(quartZ);
        int count = 0;
        int land = 0;
        for (int dx = 0; dx < 4; dx++) {
            for (int dz = 0; dz < 4; dz++) {
                BlockPos ground = WildGrowth.surface(level, new BlockPos(baseX + dx, y, baseZ + dz), 3, -4);
                if (ground == null || !WildGrowth.greenable(level, ground)) continue;
                land++;
                if (level.getBlockState(ground).is(Blocks.GRASS_BLOCK) && bordered(level, ground)) count++;
            }
        }
        if (land < MIN_LAND) return true;
        return count >= Mth.ceil(needed * land / (float) COLUMNS);
    }

    private static boolean bordered(ServerLevel level, BlockPos ground) {
        int sides = 0;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            for (int dy = 1; dy >= -1; dy--) {
                if (earthy(level.getBlockState(ground.relative(side).above(dy)))) {
                    sides++;
                    break;
                }
            }
        }
        return sides >= BORDERED_SIDES;
    }

    private static boolean earthy(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.PODZOL) || state.is(BlockTags.DIRT);
    }

    private static String key(WildGreening.Named named, int quartX, int quartZ) {
        return (named.id().getNamespace().equals(Alchemia.MODID) ? named.id().getPath() : named.id().toString()) + ":" + quartX + ":" + quartZ;
    }

    private void spread(ServerLevel level, BlockPos pos, WildGreening.Named named, Deque<long[]> ripe) {
        WildGreening greening = named.greening();
        Holder<Biome> target = level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(greening.to());
        int quartY = QuartPos.fromBlock(pos.getY());
        Set<Long> seen = new HashSet<>();
        Set<LevelChunk> touched = new HashSet<>();
        int changed = 0;
        while (!ripe.isEmpty() && changed < MAX_SPREAD) {
            long[] cell = ripe.poll();
            int quartX = (int) cell[0];
            int quartZ = (int) cell[1];
            if (!seen.add(((long) quartX << 32) | (quartZ & 0xFFFFFFFFL))) continue;
            if (!level.hasChunk(QuartPos.toSection(quartX), QuartPos.toSection(quartZ))) continue;
            if (!greening.matches(level.getNoiseBiome(quartX, quartY, quartZ))) continue;
            touched.add(rewrite(level, pos.getY(), quartX, quartZ, greening, target));
            progress.remove(key(named, quartX, quartZ));
            celebrate(level, pos.getY(), quartX, quartZ, changed == 0);
            changed++;
            for (int[] offset : RING) {
                int x = quartX + offset[0];
                int z = quartZ + offset[1];
                if (!greening.matches(level.getNoiseBiome(x, quartY, z))) continue;
                if (surround(level, greening, x, quartY, z) >= ENCLOSED && grassy(level, pos.getY(), x, z, greening.grass())) ripe.add(new long[]{x, z});
            }
        }
        setDirty();
        if (!touched.isEmpty()) level.getChunkSource().chunkMap.resendBiomesForChunks(List.copyOf(touched));
    }

    private static LevelChunk rewrite(ServerLevel level, int y, int quartX, int quartZ, WildGreening greening, Holder<Biome> target) {
        int fromY = QuartPos.fromBlock(y - BELOW);
        int toY = QuartPos.fromBlock(y + ABOVE);
        LevelChunk chunk = level.getChunk(QuartPos.toSection(quartX), QuartPos.toSection(quartZ));
#if MC_VER >= 26300
        chunk.fillBiomesFromNoise((x, qy, z) -> {
#else
        chunk.fillBiomesFromNoise((x, qy, z, sampler) -> {
#endif
            Holder<Biome> current = chunk.getNoiseBiome(x, qy, z);
            if (x == quartX && z == quartZ && qy >= fromY && qy <= toY && greening.matches(current)) return target;
            return current;
#if MC_VER >= 26300
        });
#else
        }, level.getChunkSource().randomState().sampler());
#endif
        chunk.setUnsaved(true);
        return chunk;
    }

    private static void celebrate(ServerLevel level, int blockY, int quartX, int quartZ, boolean first) {
        double x = QuartPos.toBlock(quartX) + 2.0;
        double z = QuartPos.toBlock(quartZ) + 2.0;
        double y = blockY + 1.0;
        if (first) {
            level.playSound(null, x, y, z, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.AMBIENT, 0.6F, 1.6F);
            level.playSound(null, x, y, z, SoundEvents.AZALEA_LEAVES_BREAK, SoundSource.AMBIENT, 0.8F, 0.9F);
        }
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y + 0.3, z, first ? 24 : 10, 1.6, 0.4, 1.6, 0.0);
        for (int index = 0; index < (first ? 16 : 6); index++) {
            level.sendParticles(ParticleTypeRegistry.WILD_SWIRL.get(), x, y, z, 0, 0.6 + index * 0.1, 0.05, (index & 1) == 0 ? 0.12 : -0.12, 1.0);
        }
    }
}
