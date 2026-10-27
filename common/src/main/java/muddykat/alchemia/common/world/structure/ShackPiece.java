package muddykat.alchemia.common.world.structure;

import muddykat.alchemia.registration.registers.StructureRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
#if MC_VER >= 12100
import net.neoforged.neoforge.common.world.PieceBeardifierModifier;
#else
import net.minecraftforge.common.world.PieceBeardifierModifier;
#endif

public class ShackPiece extends StructurePiece implements PieceBeardifierModifier {

    public static final int WIDTH = 11;
    public static final int DEPTH = 13;
    public static final int TOP = 10;
    public static final int CELLAR_DROP = 5;

    private final ShackStyle style;
    private final Rotation rotation;
    private final BlockPos pivot;
    private final int cellar;
    private final long seed;

    public ShackPiece(ShackStyle style, Rotation rotation, BlockPos pivot, int cellar, long seed) {
        super(StructureRegistry.SHACK_PIECE.get(), 0, box(rotation, pivot, 0, cellar - CELLAR_DROP, 0, WIDTH - 1, TOP, DEPTH - 1));
        this.style = style;
        this.rotation = rotation;
        this.pivot = pivot;
        this.cellar = cellar;
        this.seed = seed;
    }

    public ShackPiece(CompoundTag tag) {
        super(StructureRegistry.SHACK_PIECE.get(), tag);
#if MC_VER >= 26100
        this.style = ShackStyle.byName(tag.getStringOr("Style", ShackStyle.OAK.id()));
        this.rotation = rotation(tag.getStringOr("Rot", Rotation.NONE.name()));
        this.pivot = new BlockPos(tag.getIntOr("PX", 0), tag.getIntOr("PY", 0), tag.getIntOr("PZ", 0));
        this.cellar = tag.getIntOr("Cellar", 0);
        this.seed = tag.getLongOr("Seed", 0L);
#else
        this.style = ShackStyle.byName(tag.getString("Style"));
        this.rotation = rotation(tag.getString("Rot"));
        this.pivot = new BlockPos(tag.getInt("PX"), tag.getInt("PY"), tag.getInt("PZ"));
        this.cellar = tag.getInt("Cellar");
        this.seed = tag.getLong("Seed");
#endif
    }

    public static BoundingBox box(Rotation rotation, BlockPos pivot, int x0, int y0, int z0, int x1, int y1, int z1) {
        return BoundingBox.fromCorners(pivot.offset(ShackCanvas.offset(rotation, x0, y0, z0)), pivot.offset(ShackCanvas.offset(rotation, x1, y1, z1)));
    }

    public ShackStyle style() {
        return style;
    }

    private static Rotation rotation(String name) {
        for (Rotation rotation : Rotation.values()) {
            if (rotation.name().equals(name)) return rotation;
        }
        return Rotation.NONE;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("Style", style.id());
        tag.putString("Rot", rotation.name());
        tag.putInt("PX", pivot.getX());
        tag.putInt("PY", pivot.getY());
        tag.putInt("PZ", pivot.getZ());
        tag.putInt("Cellar", cellar);
        tag.putLong("Seed", seed);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
                            BoundingBox chunkBB, ChunkPos chunkPos, BlockPos referencePos) {
        ShackCanvas canvas = new ShackCanvas(level, chunkBB, pivot, rotation, seed);
        ShackContents contents = new ShackContents(canvas, style);
        ShackShell.build(canvas, style, cellar);
        ShackCellar.build(canvas, style, cellar, contents);
        ShackRooms.build(canvas, style, contents);
        ShackGrounds.build(canvas, style, contents);
    }

    public void clearOvergrowth(LevelChunk chunk) {
        clear(chunk, 3, 1, 4, 7, 7, 10);
        clear(chunk, 2, 1, 0, 8, 3, 2);
    }

    private void clear(LevelChunk chunk, int x0, int y0, int z0, int x1, int y1, int z1) {
        ChunkPos at = chunk.getPos();
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    BlockPos pos = pivot.offset(ShackCanvas.offset(rotation, x, y, z));
                    if (pos.getX() < at.getMinBlockX() || pos.getX() > at.getMaxBlockX() || pos.getZ() < at.getMinBlockZ()
                            || pos.getZ() > at.getMaxBlockZ()) continue;
#if MC_VER >= 26100
                    if (overgrowth(chunk.getBlockState(pos), y)) chunk.setBlockState(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
#else
                    if (overgrowth(chunk.getBlockState(pos), y)) chunk.setBlockState(pos, Blocks.AIR.defaultBlockState(), false);
#endif
                }
            }
        }
    }

    private static boolean overgrowth(BlockState state, int y) {
        return state.is(BlockTags.LEAVES) || state.is(Blocks.VINE) || state.is(Blocks.BROWN_MUSHROOM_BLOCK)
                || state.is(Blocks.RED_MUSHROOM_BLOCK) || state.is(Blocks.MUSHROOM_STEM)
#if MC_VER >= 26100
                || state.is(BlockTags.LOGS) && y != 4 || state.is(Blocks.PALE_HANGING_MOSS) && y != 3;
#else
                || state.is(BlockTags.LOGS) && y != 4;
#endif
    }

    @Override
    public BoundingBox getBeardifierBox() {
        return box(rotation, pivot, 2, 0, 1, 10, 8, 12);
    }

    @Override
    public TerrainAdjustment getTerrainAdjustment() {
        return style.raised() ? TerrainAdjustment.NONE : TerrainAdjustment.BEARD_THIN;
    }

    @Override
    public int getGroundLevelDelta() {
        return 1;
    }
}
