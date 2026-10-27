package muddykat.alchemia.common.world.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.Fluids;
#if MC_VER >= 26100
import org.jspecify.annotations.Nullable;
#else
import javax.annotation.Nullable;
#endif

public final class ShackCanvas {

    private final WorldGenLevel level;
    private final BoundingBox chunk;
    private final BlockPos pivot;
    private final Rotation rotation;
    private final long seed;

    public ShackCanvas(WorldGenLevel level, BoundingBox chunk, BlockPos pivot, Rotation rotation, long seed) {
        this.level = level;
        this.chunk = chunk;
        this.pivot = pivot;
        this.rotation = rotation;
        this.seed = seed;
    }

    public static BlockPos offset(Rotation rotation, int x, int y, int z) {
        return switch (rotation) {
            case NONE -> new BlockPos(x, y, z);
            case CLOCKWISE_90 -> new BlockPos(-z, y, x);
            case CLOCKWISE_180 -> new BlockPos(-x, y, -z);
            case COUNTERCLOCKWISE_90 -> new BlockPos(z, y, -x);
        };
    }

    public WorldGenLevel level() {
        return level;
    }

    public long seed() {
        return seed;
    }

    public BlockPos world(int x, int y, int z) {
        return pivot.offset(offset(rotation, x, y, z));
    }

    public boolean inside(int x, int y, int z) {
        return chunk.isInside(world(x, y, z));
    }

    public Direction facing(Direction local) {
        return rotation.rotate(local);
    }

    public void set(int x, int y, int z, BlockState state) {
        BlockPos pos = world(x, y, z);
        if (!chunk.isInside(pos)) return;

        BlockState placed = state.rotate(rotation);
        if (placed.hasProperty(BlockStateProperties.WATERLOGGED)) {
            placed = placed.setValue(BlockStateProperties.WATERLOGGED, level.getFluidState(pos).isSourceOfType(Fluids.WATER));
        }
        level.setBlock(pos, placed, Block.UPDATE_CLIENTS);
    }

    public void set(int x, int y, int z, Block block) {
        set(x, y, z, block.defaultBlockState());
    }

    public void air(int x, int y, int z) {
        set(x, y, z, Blocks.AIR.defaultBlockState());
    }

    public void box(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) set(x, y, z, state);
            }
        }
    }

    public BlockState get(int x, int y, int z) {
        BlockPos pos = world(x, y, z);
        return chunk.isInside(pos) ? level.getBlockState(pos) : Blocks.AIR.defaultBlockState();
    }

    public boolean column(int x, int z) {
        BlockPos pos = world(x, 0, z);
        return pos.getX() >= chunk.minX() && pos.getX() <= chunk.maxX() && pos.getZ() >= chunk.minZ() && pos.getZ() <= chunk.maxZ();
    }

    public int surface(int x, int z) {
        BlockPos pos = world(x, 0, z);
        return level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, pos.getX(), pos.getZ()) - 1 - pivot.getY();
    }

    public int waterline(int x, int z) {
        BlockPos pos = world(x, 0, z);
        return level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ()) - 1 - pivot.getY();
    }

    public void fillDown(int x, int y, int z, BlockState state) {
        if (!column(x, z)) return;
#if MC_VER >= 26100
        int bottom = level.getMinY() + 1 - pivot.getY();
#else
        int bottom = level.getMinBuildHeight() + 1 - pivot.getY();
#endif
        for (int at = y; at > bottom; at--) {
            if (!yields(get(x, at, z))) return;
            set(x, at, z, state);
        }
    }

    public static boolean yields(BlockState state) {
        return state.isAir() || state.liquid() || state.canBeReplaced();
    }

    public @Nullable BlockEntity entity(int x, int y, int z) {
        BlockPos pos = world(x, y, z);
        return chunk.isInside(pos) ? level.getBlockEntity(pos) : null;
    }

    public long hash(int x, int y, int z, int salt) {
        long h = seed ^ (x * 0x9E3779B97F4A7C15L) ^ (y * 0xC2B2AE3D27D4EB4FL) ^ (z * 0x165667B19E3779F9L) ^ (salt * 0xD6E8FEB86659FD93L);
        h = (h ^ (h >>> 33)) * 0xFF51AFD7ED558CCDL;
        h = (h ^ (h >>> 33)) * 0xC4CEB9FE1A85EC53L;
        return h ^ (h >>> 33);
    }

    public float chance(int x, int y, int z, int salt) {
        return (hash(x, y, z, salt) >>> 40) / (float) (1 << 24);
    }

    public int pick(int x, int y, int z, int salt, int bound) {
        return (int) Math.floorMod(hash(x, y, z, salt), (long) bound);
    }

    public RandomSource random(int x, int y, int z, int salt) {
        return RandomSource.create(hash(x, y, z, salt));
    }
}
