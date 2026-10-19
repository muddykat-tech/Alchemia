package muddykat.alchemia.common.world;

import muddykat.alchemia.common.blocks.AncientOak;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class AncientOakShape {

    public static final int MIN_HEIGHT = 13;
    public static final int HEIGHT_SPREAD = 7;

    private static final int CROWN_RADIUS = 4;
    private static final int BRANCH_RADIUS = 3;
    private static final int MIN_BRANCHES = 3;
    private static final int EXTRA_BRANCHES = 3;
    private static final int MIN_BRANCH_LENGTH = 4;
    private static final int EXTRA_BRANCH_LENGTH = 4;
    private static final int ROOT_DEPTH = 4;
    private static final float ROOT_CHANCE = 0.55F;
    private static final int MAX_LEAF_DISTANCE = 6;
    private static final int PLACE_FLAGS = 19;

    private AncientOakShape() {
    }

    public static void growFromSapling(ServerLevel level, BlockPos pos, BlockState sapling, RandomSource random) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 4);
        if (!place(level, random, pos, MIN_HEIGHT, HEIGHT_SPREAD)) level.setBlock(pos, sapling, 4);
    }

    public static boolean place(WorldGenLevel level, RandomSource random, BlockPos origin, int minHeight, int heightSpread) {
        int height = minHeight + random.nextInt(Math.max(1, heightSpread));
        if (!fits(level, origin, height)) return false;

        Map<BlockPos, Direction.Axis> logs = new LinkedHashMap<>();
        Set<BlockPos> leaves = new LinkedHashSet<>();

        for (int y = 0; y < height; y++) {
            for (int dx = 0; dx <= 1; dx++) {
                for (int dz = 0; dz <= 1; dz++) logs.put(origin.offset(dx, y, dz), Direction.Axis.Y);
            }
        }

        roots(level, random, origin, logs);

        double centreX = origin.getX() + 1.0;
        double centreZ = origin.getZ() + 1.0;
        int branches = MIN_BRANCHES + random.nextInt(EXTRA_BRANCHES);
        double turn = random.nextDouble() * Math.PI * 2.0;

        for (int index = 0; index < branches; index++) {
            double angle = turn + index * (Math.PI * 2.0 / branches) + (random.nextDouble() - 0.5) * 0.6;
            int startY = origin.getY() + height / 2 + random.nextInt(Math.max(1, height / 3));
            int length = MIN_BRANCH_LENGTH + random.nextInt(EXTRA_BRANCH_LENGTH);
            BlockPos tip = branch(logs, centreX, startY, centreZ, angle, length);
            blob(leaves, tip.getX() + 0.5, tip.getY() + 0.5, tip.getZ() + 0.5, BRANCH_RADIUS, random);
        }

        blob(leaves, centreX, origin.getY() + height + 0.5, centreZ, CROWN_RADIUS, random);

        Set<BlockPos> placedLogs = new LinkedHashSet<>();
        BlockState log = AncientOak.block(AncientOak.LOG).defaultBlockState();
        for (Map.Entry<BlockPos, Direction.Axis> entry : logs.entrySet()) {
            BlockPos pos = entry.getKey();
            if (!canHoldLog(level, pos, pos.getY() < origin.getY())) continue;
            level.setBlock(pos, log.setValue(RotatedPillarBlock.AXIS, entry.getValue()), PLACE_FLAGS);
            placedLogs.add(pos);
        }

        Set<BlockPos> placedLeaves = new LinkedHashSet<>();
        for (BlockPos pos : leaves) {
            if (!logs.containsKey(pos) && isOpen(level.getBlockState(pos))) placedLeaves.add(pos);
        }

        Map<BlockPos, Integer> distance = leafDistances(placedLogs, placedLeaves);
        BlockState leaf = AncientOak.block(AncientOak.LEAVES).defaultBlockState().setValue(LeavesBlock.PERSISTENT, false);
        for (BlockPos pos : placedLeaves) {
            int steps = distance.getOrDefault(pos, LeavesBlock.DECAY_DISTANCE);
            level.setBlock(pos, leaf.setValue(LeavesBlock.DISTANCE, steps), PLACE_FLAGS);
        }
        return true;
    }

    private static boolean fits(WorldGenLevel level, BlockPos origin, int height) {
#if MC_VER >= 26102
        if (origin.getY() + height + CROWN_RADIUS + 2 >= level.getMaxY()) return false;
#else
        if (origin.getY() + height + CROWN_RADIUS + 2 >= level.getMaxBuildHeight()) return false;
#endif
        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                if (!isSoil(level.getBlockState(origin.offset(dx, -1, dz)))) return false;
                for (int y = 0; y < height; y++) {
                    if (!isOpen(level.getBlockState(origin.offset(dx, y, dz)))) return false;
                }
            }
        }
        return true;
    }

    public static boolean isSoil(BlockState state) {
#if MC_VER >= 26102
        return state.is(BlockTags.DIRT) || state.is(BlockTags.SUPPORTS_VEGETATION);
#else
        return state.is(BlockTags.DIRT);
#endif
    }

    private static boolean isOpen(BlockState state) {
        return state.isAir() || state.canBeReplaced() || state.is(BlockTags.LEAVES) || state.getBlock() instanceof SaplingBlock;
    }

    private static boolean canHoldLog(WorldGenLevel level, BlockPos pos, boolean underground) {
        BlockState state = level.getBlockState(pos);
        return isOpen(state) || (underground && isSoil(state));
    }

    private static void roots(WorldGenLevel level, RandomSource random, BlockPos origin, Map<BlockPos, Direction.Axis> logs) {
        int[][] sides = {{-1, 0}, {-1, 1}, {2, 0}, {2, 1}, {0, -1}, {1, -1}, {0, 2}, {1, 2}};
        for (int[] side : sides) {
            if (random.nextFloat() >= ROOT_CHANCE) continue;

            int top = random.nextInt(3);
            for (int y = top; y >= -ROOT_DEPTH; y--) {
                BlockPos pos = origin.offset(side[0], y, side[1]);
                BlockState state = level.getBlockState(pos);
                boolean underground = y < 0;
                if (!isOpen(state) && !(underground && isSoil(state))) break;
                logs.put(pos, Direction.Axis.Y);
            }
        }
    }

    private static BlockPos branch(Map<BlockPos, Direction.Axis> logs, double centreX, int startY, double centreZ,
                                   double angle, int length) {
        double stepX = Math.cos(angle);
        double stepZ = Math.sin(angle);
        Direction.Axis axis = Math.abs(stepX) >= Math.abs(stepZ) ? Direction.Axis.X : Direction.Axis.Z;

        BlockPos previous = null;
        for (int step = 0; step <= length; step++) {
            double reach = 1.0 + step;
            BlockPos pos = BlockPos.containing(centreX + stepX * reach, startY + step / 2, centreZ + stepZ * reach);

            if (previous != null) {
                if (pos.getX() != previous.getX() && pos.getZ() != previous.getZ()) {
                    logs.putIfAbsent(new BlockPos(pos.getX(), previous.getY(), previous.getZ()), axis);
                }
                if (pos.getY() != previous.getY()) {
                    logs.putIfAbsent(new BlockPos(pos.getX(), previous.getY(), pos.getZ()), axis);
                }
            }

            logs.putIfAbsent(pos, axis);
            previous = pos;
        }
        return previous;
    }

    private static void blob(Set<BlockPos> leaves, double centreX, double centreY, double centreZ, int radius, RandomSource random) {
        double squash = radius * 0.65;
        int minX = (int) Math.floor(centreX - radius - 1);
        int maxX = (int) Math.ceil(centreX + radius + 1);
        int minY = (int) Math.floor(centreY - squash - 1);
        int maxY = (int) Math.ceil(centreY + squash + 1);
        int minZ = (int) Math.floor(centreZ - radius - 1);
        int maxZ = (int) Math.ceil(centreZ + radius + 1);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    double dx = (x + 0.5 - centreX) / radius;
                    double dy = (y + 0.5 - centreY) / squash;
                    double dz = (z + 0.5 - centreZ) / radius;
                    double reach = dx * dx + dy * dy + dz * dz;
                    if (reach <= 1.0 - 0.15 + random.nextDouble() * 0.3) leaves.add(new BlockPos(x, y, z));
                }
            }
        }
    }

    private static Map<BlockPos, Integer> leafDistances(Set<BlockPos> logs, Set<BlockPos> leaves) {
        Map<BlockPos, Integer> distance = new HashMap<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        for (BlockPos log : logs) {
            distance.put(log, 0);
            queue.add(log);
        }

        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            int next = distance.get(pos) + 1;
            if (next > MAX_LEAF_DISTANCE) continue;

            for (Direction direction : Direction.values()) {
                BlockPos neighbour = pos.relative(direction);
                if (!leaves.contains(neighbour) || distance.containsKey(neighbour)) continue;
                distance.put(neighbour, next);
                queue.add(neighbour);
            }
        }
        return distance;
    }
}
