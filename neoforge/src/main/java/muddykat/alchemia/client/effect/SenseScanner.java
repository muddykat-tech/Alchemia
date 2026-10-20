package muddykat.alchemia.client.effect;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class SenseScanner {

    private static final int SECTIONS_PER_TICK = 24;

    public record Hit(BlockPos pos, BlockState state) {
    }

    private final Predicate<BlockState> wanted;
    private final List<Long> queue = new ArrayList<>();
    private List<Hit> building = new ArrayList<>();
    private List<Hit> found = List.of();
    private int cursor;
    private BlockPos centre = BlockPos.ZERO;
    private int radius;

    public SenseScanner(Predicate<BlockState> wanted) {
        this.wanted = wanted;
    }

    public List<Hit> found() {
        return found;
    }

    public void clear() {
        queue.clear();
        cursor = 0;
        building = new ArrayList<>();
        found = List.of();
    }

    public void tick(ClientLevel level, BlockPos around, int reach) {
        if (cursor >= queue.size()) restart(level, around, reach);
        for (int budget = SECTIONS_PER_TICK; budget > 0 && cursor < queue.size(); budget--) scan(level, queue.get(cursor++));
        if (cursor >= queue.size()) found = List.copyOf(building);
    }

    private void restart(ClientLevel level, BlockPos around, int reach) {
        centre = around.immutable();
        radius = reach;
        building = new ArrayList<>();
        queue.clear();
        cursor = 0;
        int minY = Math.max(level.getMinSectionY(), SectionPos.blockToSectionCoord(around.getY() - reach));
        int maxY = Math.min(level.getMaxSectionY(), SectionPos.blockToSectionCoord(around.getY() + reach));
        for (int x = SectionPos.blockToSectionCoord(around.getX() - reach); x <= SectionPos.blockToSectionCoord(around.getX() + reach); x++) {
            for (int z = SectionPos.blockToSectionCoord(around.getZ() - reach); z <= SectionPos.blockToSectionCoord(around.getZ() + reach); z++) {
                for (int y = minY; y <= maxY; y++) queue.add(SectionPos.asLong(x, y, z));
            }
        }
    }

    private void scan(ClientLevel level, long packed) {
        int sectionX = SectionPos.x(packed);
        int sectionY = SectionPos.y(packed);
        int sectionZ = SectionPos.z(packed);
        LevelChunk chunk = level.getChunkSource().getChunk(sectionX, sectionZ, ChunkStatus.FULL, false);
        if (chunk == null) return;

        LevelChunkSection section = chunk.getSections()[sectionY - level.getMinSectionY()];
        if (section.hasOnlyAir() || !section.maybeHas(wanted)) return;

        long reachSqr = (long) radius * radius;
        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    BlockState state = section.getBlockState(x, y, z);
                    if (!wanted.test(state)) continue;
                    BlockPos pos = new BlockPos(SectionPos.sectionToBlockCoord(sectionX) + x, SectionPos.sectionToBlockCoord(sectionY) + y,
                            SectionPos.sectionToBlockCoord(sectionZ) + z);
                    if (pos.distSqr(centre) <= reachSqr) building.add(new Hit(pos, state));
                }
            }
        }
    }
}
