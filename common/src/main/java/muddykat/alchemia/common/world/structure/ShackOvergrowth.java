package muddykat.alchemia.common.world.structure;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
#if MC_VER >= 12100
import net.minecraft.world.level.chunk.status.ChunkStatus;
#else
import net.minecraft.world.level.chunk.ChunkStatus;
#endif
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
#if MC_VER >= 12100
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
#else
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
#endif

import java.util.Map;

public final class ShackOvergrowth {

    private ShackOvergrowth() {
    }

    @SubscribeEvent
    public static void loaded(ChunkEvent.Load event) {
        if (!event.isNewChunk() || !(event.getLevel() instanceof ServerLevel level) || !(event.getChunk() instanceof LevelChunk chunk)) return;
        for (Map.Entry<Structure, LongSet> entry : chunk.getAllReferences().entrySet()) {
            if (!(entry.getKey() instanceof ForgottenShackStructure)) continue;
            for (long origin : entry.getValue()) {
                ChunkAccess source = level.getChunkSource().getChunk(ChunkPos.getX(origin), ChunkPos.getZ(origin), ChunkStatus.STRUCTURE_STARTS, false);
                StructureStart start = source == null ? null : source.getStartForStructure(entry.getKey());
                if (start == null || !start.isValid()) continue;
                for (StructurePiece piece : start.getPieces()) {
                    if (piece instanceof ShackPiece shack) shack.clearOvergrowth(chunk);
                }
            }
        }
    }
}
