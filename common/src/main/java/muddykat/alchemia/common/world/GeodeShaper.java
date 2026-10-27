package muddykat.alchemia.common.world;

import muddykat.alchemia.common.items.helper.IngredientAlignment;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.registration.registers.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.List;

public final class GeodeShaper {

    private static final int FLAGS = 2;
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private static final double SKIN = 1.4;
    private static final double MIDDLE = 2.4;
    private static final double SHELL = 3.2;

    private static final double MINI_CAVITY = 1.2;
    private static final double MINI_INNER = 1.8;
    private static final double MINI_MIDDLE = 2.25;
    private static final double MINI_SHELL = 2.6;
    private static final int MINI_LEAKS = 1;
    private static final float MINI_BUDDING = 0.12F;

    private static final int CAVERN_SCAN = 32;
    private static final int CAVERN_FLUIDS = 8;
    private static final float CAVERN_BUDDING = 0.18F;

    private static final Cavern GIANT = new Cavern(
            6.0, 1.5, 2, 3, 4.0, 2.0, 6, 3,
            6, 7, 4, 6, 1.4, 1.4, 64);

    private static final Cavern SUPERMASSIVE = new Cavern(
            13.0, 3.0, 4, 4, 7.0, 3.0, 8, 8,
            14, 11, 8, 13, 2.2, 1.8, 192);

    private static final int SURFACE_RADIUS = 4;
    private static final int SURFACE_RADIUS_VARY = 3;
    private static final int SURFACE_SLOPE = 3;
    private static final double SURFACE_WOBBLE = 1.3;
    private static final double SURFACE_SKIRT = 2.6;
    private static final float SURFACE_BUDDING = 0.1F;
    private static final float SURFACE_MOTTLE = 0.22F;
    private static final int SURFACE_CLUSTERS = 24;

    private static final double MOUTH_RADIUS = 0.9;
    private static final double MOUTH_RADIUS_VARY = 1.1;
    private static final double MOUTH_WOBBLE = 0.9;
    private static final int MOUTH_DRIFT = 2;
    private static final float MOUTH_DRIFT_CHANCE = 0.45F;
    private static final float MOUTH_NIBBLE = 0.25F;
    private static final int MOUTH_RIM = 3;
    private static final float SURFACE_VINES = 0.6F;

    private static final int UNDERSIDE_DEPTH = 64;
    private static final int UNDERSIDE_THICKNESS = 12;
    private static final int UNDERSIDE_RADIUS = 3;
    private static final int UNDERSIDE_RADIUS_VARY = 2;
    private static final double UNDERSIDE_WOBBLE = 0.9;
    private static final float UNDERSIDE_BUDDING = 0.2F;
    private static final int UNDERSIDE_SPIKES = 2;
    private static final int UNDERSIDE_SPIKES_VARY = 3;
    private static final int UNDERSIDE_DRIPS = 2;
    private static final int UNDERSIDE_DRIPS_VARY = 3;
    private static final int UNDERSIDE_DRIP_REACH = 4;
    private static final int UNDERSIDE_DRIP_SCAN = 20;
    private static final int UNDERSIDE_CLUSTERS = 80;
    private static final int UNDERSIDE_TELLS = 1;
    private static final int UNDERSIDE_TELLS_VARY = 3;
    private static final int UNDERSIDE_TELL_REACH = 2;

    private static final Cavern UNDERSIDE_SPIKE = new Cavern(
            0.0, 0.0, 0, 1, 0.0, 0.0, 0, 0,
            0, 1, 3, 4, 1.0, 0.8, 0);

    private GeodeShaper() {
    }

    public static boolean place(WorldGenLevel level, RandomSource random, BlockPos origin,
                               Ingredients mineral, GeodeVariant variant) {
        Palette palette = paletteOf(mineral);

        return switch (variant) {
            case MINIATURE -> miniature(level, random, origin, palette);
            case GIANT -> cavern(level, random, origin, palette, GIANT);
            case SUPERMASSIVE -> cavern(level, random, origin, palette, SUPERMASSIVE);
            case SURFACE -> surface(level, random, origin, palette);
            case UNDERSIDE -> underside(level, random, origin, endPalette(palette));
            case STANDARD -> false;
        };
    }

    private record Palette(BlockState inner, BlockState budding, BlockState middle, BlockState outer,
                           List<BlockState> clusters) {

        private BlockState lining(RandomSource random, float buddingChance) {
            return random.nextFloat() < buddingChance ? budding : inner;
        }

        private BlockState cluster(RandomSource random) {
            return clusters.get(random.nextInt(clusters.size()));
        }

        private BlockState largest() {
            return clusters.get(clusters.size() - 1);
        }
    }

    private static Palette paletteOf(Ingredients mineral) {
        var budding = BlockRegistry.getGeodeBuddingBlock(mineral);
        boolean nether = mineral.getPrimaryAlignment() == IngredientAlignment.Fire;

        return new Palette(
                BlockRegistry.getGeodeBlock(mineral).defaultBlockState(),
                budding.defaultBlockState(),
                Blocks.CALCITE.defaultBlockState(),
                nether ? Blocks.BASALT.defaultBlockState() : Blocks.SMOOTH_BASALT.defaultBlockState(),
                budding.getClusterStates());
    }

    private static Palette endPalette(Palette palette) {
        return new Palette(palette.inner(), palette.budding(), Blocks.OBSIDIAN.defaultBlockState(),
                Blocks.END_STONE.defaultBlockState(), palette.clusters());
    }

    private record Wobble(double amplitude, double phaseX, double phaseZ, double phaseDiagonal, double phaseY) {

        private double at(double dx, double dy, double dz) {
            return amplitude * (Math.sin(dx * 0.7 + phaseX)
                    + Math.sin(dz * 0.7 + phaseZ)
                    + Math.sin((dx + dz) * 0.45 + phaseDiagonal)
                    + Math.sin(dy * 0.6 + phaseY)) / 4.0;
        }
    }

    private static Wobble wobbleOf(RandomSource random, double amplitude) {
        double turn = Math.PI * 2;
        return new Wobble(amplitude, random.nextDouble() * turn, random.nextDouble() * turn,
                random.nextDouble() * turn, random.nextDouble() * turn);
    }

    private static boolean replaceable(WorldGenLevel level, BlockPos pos) {
        return !level.getBlockState(pos).is(BlockTags.FEATURES_CANNOT_REPLACE);
    }

    private static boolean leaking(BlockState state) {
        return state.isAir() || !state.getFluidState().isEmpty() || state.is(BlockTags.GEODE_INVALID_BLOCKS);
    }

    private static void setCluster(WorldGenLevel level, BlockPos pos, BlockState cluster, Direction facing) {
        level.setBlock(pos, cluster.setValue(AmethystClusterBlock.FACING, facing), FLAGS);
    }

    private static boolean miniature(WorldGenLevel level, RandomSource random, BlockPos origin, Palette palette) {
        int reach = (int) Math.ceil(MINI_SHELL);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int leaks = 0;
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dy = -reach; dy <= reach; dy++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (distance <= MINI_MIDDLE || distance > MINI_SHELL + 1.0) continue;

                    pos.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    if (leaking(level.getBlockState(pos)) && ++leaks > MINI_LEAKS) return false;
                }
            }
        }

        for (int dx = -reach; dx <= reach; dx++) {
            for (int dy = -reach; dy <= reach; dy++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (distance > MINI_SHELL) continue;

                    pos.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    if (!replaceable(level, pos)) continue;

                    if (distance <= MINI_CAVITY) level.setBlock(pos, AIR, FLAGS);
                    else if (distance <= MINI_INNER) level.setBlock(pos, palette.lining(random, MINI_BUDDING), FLAGS);
                    else if (distance <= MINI_MIDDLE) level.setBlock(pos, palette.middle(), FLAGS);
                    else level.setBlock(pos, palette.outer(), FLAGS);
                }
            }
        }

        level.setBlock(origin.below(), palette.inner(), FLAGS);
        setCluster(level, origin, palette.largest(), Direction.UP);
        return true;
    }

    private record Cavern(double core, double coreVary, int lobes, int lobesVary, double lobe, double lobeVary,
                          int spread, int rise, int spikes, int spikesVary, int spikeHeight, int spikeHeightVary,
                          double spikeBase, double spikeBaseVary, int clusters) {
    }

    private record Lobe(BlockPos centre, double radius) {
    }

    private static double gapTo(List<Lobe> lobes, double x, double y, double z) {
        double gap = Double.MAX_VALUE;

        for (Lobe lobe : lobes) {
            double dx = x - lobe.centre().getX();
            double dy = y - lobe.centre().getY();
            double dz = z - lobe.centre().getZ();
            gap = Math.min(gap, Math.sqrt(dx * dx + dy * dy + dz * dz) - lobe.radius());
        }
        return gap;
    }

    private static boolean cavern(WorldGenLevel level, RandomSource random, BlockPos origin, Palette palette,
                                  Cavern spec) {
        List<Lobe> lobes = new ArrayList<>();
        lobes.add(new Lobe(origin, spec.core() + random.nextDouble() * spec.coreVary()));

        int extra = spec.lobes() + random.nextInt(spec.lobesVary());
        for (int i = 0; i < extra; i++) {
            BlockPos at = origin.offset(
                    random.nextInt(spec.spread() * 2 + 1) - spec.spread(),
                    random.nextInt(spec.rise() * 2 + 1) - spec.rise(),
                    random.nextInt(spec.spread() * 2 + 1) - spec.spread());
            lobes.add(new Lobe(at, spec.lobe() + random.nextDouble() * spec.lobeVary()));
        }

        int lowX = Integer.MAX_VALUE;
        int lowY = Integer.MAX_VALUE;
        int lowZ = Integer.MAX_VALUE;
        int highX = Integer.MIN_VALUE;
        int highY = Integer.MIN_VALUE;
        int highZ = Integer.MIN_VALUE;

        for (Lobe lobe : lobes) {
            int edge = (int) Math.ceil(lobe.radius() + SHELL);
            lowX = Math.min(lowX, lobe.centre().getX() - edge);
            lowY = Math.min(lowY, lobe.centre().getY() - edge);
            lowZ = Math.min(lowZ, lobe.centre().getZ() - edge);
            highX = Math.max(highX, lobe.centre().getX() + edge);
            highY = Math.max(highY, lobe.centre().getY() + edge);
            highZ = Math.max(highZ, lobe.centre().getZ() + edge);
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int fluids = 0;
        for (int x = lowX; x <= highX; x++) {
            for (int y = lowY; y <= highY; y++) {
                for (int z = lowZ; z <= highZ; z++) {
                    double gap = gapTo(lobes, x, y, z);
                    if (gap < SHELL - 1.0 || gap > SHELL + 1.0) continue;

                    pos.set(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if ((!state.getFluidState().isEmpty() || state.is(BlockTags.GEODE_INVALID_BLOCKS))
                            && ++fluids > CAVERN_FLUIDS) return false;
                }
            }
        }

        for (int x = lowX; x <= highX; x++) {
            for (int y = lowY; y <= highY; y++) {
                for (int z = lowZ; z <= highZ; z++) {
                    double gap = gapTo(lobes, x, y, z);
                    if (gap > SHELL) continue;

                    pos.set(x, y, z);
                    if (!replaceable(level, pos)) continue;

                    if (gap <= 0.0) level.setBlock(pos, AIR, FLAGS);
                    else if (gap <= SKIN) level.setBlock(pos, palette.lining(random, CAVERN_BUDDING), FLAGS);
                    else if (gap <= MIDDLE) level.setBlock(pos, palette.middle(), FLAGS);
                    else level.setBlock(pos, palette.outer(), FLAGS);
                }
            }
        }

        int spikes = spec.spikes() + random.nextInt(spec.spikesVary());
        for (int i = 0; i < spikes; i++) {
            Lobe lobe = lobes.get(random.nextInt(lobes.size()));
            boolean rising = random.nextBoolean();

            int span = (int) Math.floor(lobe.radius() * 0.7);
            BlockPos from = lobe.centre().offset(
                    random.nextInt(span * 2 + 1) - span, 0, random.nextInt(span * 2 + 1) - span);

            BlockPos anchor = anchorOf(level, from, rising);
            if (anchor == null) continue;

            spike(level, random, anchor, rising, palette, spec);
        }

        int reach = Math.max(highX - lowX, highY - lowY) / 2;
        scatterClusters(level, random, origin, reach, spec.clusters(), palette);
        return true;
    }

    private static BlockPos anchorOf(WorldGenLevel level, BlockPos from, boolean rising) {
        BlockPos.MutableBlockPos cursor = from.mutable();
        Direction towards = rising ? Direction.DOWN : Direction.UP;

        if (!level.getBlockState(cursor).isAir()) return null;

        for (int i = 0; i < CAVERN_SCAN; i++) {
            BlockPos next = cursor.relative(towards);
            if (!level.getBlockState(next).isAir()) return cursor.immutable();
            cursor.set(next);
        }
        return null;
    }

    private static void spike(WorldGenLevel level, RandomSource random, BlockPos anchor, boolean rising,
                              Palette palette, Cavern spec) {
        int height = spec.spikeHeight() + random.nextInt(spec.spikeHeightVary());
        double base = spec.spikeBase() + random.nextDouble() * spec.spikeBaseVary();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int step = 0; step < height; step++) {
            double radius = base * (1.0 - (double) step / height);
            int span = (int) Math.floor(radius);
            int y = anchor.getY() + (rising ? step : -step);

            for (int dx = -span; dx <= span; dx++) {
                for (int dz = -span; dz <= span; dz++) {
                    if (Math.sqrt(dx * dx + dz * dz) > radius) continue;

                    pos.set(anchor.getX() + dx, y, anchor.getZ() + dz);
                    if (!level.getBlockState(pos).isAir()) continue;
                    level.setBlock(pos, palette.lining(random, CAVERN_BUDDING), FLAGS);
                }
            }
        }

        pos.set(anchor.getX(), anchor.getY() + (rising ? height : -height), anchor.getZ());
        if (level.getBlockState(pos).isAir()) {
            setCluster(level, pos, palette.largest(), rising ? Direction.UP : Direction.DOWN);
        }
    }

    private static void scatterClusters(WorldGenLevel level, RandomSource random, BlockPos centre, int reach,
                                        int tries, Palette palette) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int i = 0; i < tries; i++) {
            pos.set(centre.getX() + random.nextInt(reach * 2 + 1) - reach,
                    centre.getY() + random.nextInt(reach * 2 + 1) - reach,
                    centre.getZ() + random.nextInt(reach * 2 + 1) - reach);
            if (!level.getBlockState(pos).isAir()) continue;

            Direction facing = Direction.values()[random.nextInt(Direction.values().length)];
            BlockState against = level.getBlockState(pos.relative(facing.getOpposite()));
            if (!against.is(palette.inner().getBlock()) && !against.is(palette.budding().getBlock())) continue;

            setCluster(level, pos.immutable(), palette.cluster(random), facing);
        }
    }

    private static boolean surface(WorldGenLevel level, RandomSource random, BlockPos origin, Palette palette) {
        int radius = SURFACE_RADIUS + random.nextInt(SURFACE_RADIUS_VARY);
        int reach = radius + (int) Math.ceil(SHELL + SURFACE_SKIRT + SURFACE_WOBBLE);
        int width = reach * 2 + 1;

        int[] ground = new int[width * width];
        BlockState[] top = new BlockState[width * width];
        BlockState[] upper = new BlockState[width * width];
        BlockState[] mid = new BlockState[width * width];
        BlockState[] deep = new BlockState[width * width];

        int lowest = Integer.MAX_VALUE;
        int highest = Integer.MIN_VALUE;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                int index = (dx + reach) * width + dz + reach;
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                int height = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);

                ground[index] = height;
                top[index] = level.getBlockState(pos.set(x, height - 1, z));
                upper[index] = level.getBlockState(pos.set(x, height - 2, z));
                mid[index] = level.getBlockState(pos.set(x, height - 4, z));
                deep[index] = level.getBlockState(pos.set(x, height - 6, z));

                if (dx * dx + dz * dz > radius * radius) continue;
                if (!level.getBlockState(pos.set(x, height, z)).getFluidState().isEmpty()) return false;
                if (leaking(top[index])) return false;

                lowest = Math.min(lowest, height);
                highest = Math.max(highest, height);
            }
        }

        if (highest - lowest > SURFACE_SLOPE) return false;

        int centreY = lowest - 1;
        Wobble wobble = wobbleOf(random, SURFACE_WOBBLE);

        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                int index = (dx + reach) * width + dz + reach;
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;

                boolean capped = false;
                int coverDepth = 0;

                for (int y = centreY + reach; y >= centreY - reach; y--) {
                    int dy = y - centreY;
                    double distance = Math.sqrt(dx * dx + (double) dy * dy + dz * dz);
                    double gap = distance - radius - wobble.at(dx, dy, dz);
                    if (gap > SHELL + SURFACE_SKIRT) continue;

                    pos.set(x, y, z);
                    if (!replaceable(level, pos)) continue;

                    if (gap <= 0.0) {
                        level.setBlock(pos, AIR, FLAGS);
                        continue;
                    }

                    if (y >= ground[index] && gap > MIDDLE) {
                        level.setBlock(pos, cover(random, capped, coverDepth, index, top, upper, mid, deep), FLAGS);
                        capped = true;
                        coverDepth++;
                        continue;
                    }

                    if (gap > SHELL) continue;

                    if (gap <= SKIN) level.setBlock(pos, palette.lining(random, SURFACE_BUDDING), FLAGS);
                    else if (gap <= MIDDLE) level.setBlock(pos, palette.middle(), FLAGS);
                    else level.setBlock(pos, palette.outer(), FLAGS);
                }
            }
        }

        scatterClusters(level, random, new BlockPos(origin.getX(), centreY, origin.getZ()), radius,
                SURFACE_CLUSTERS, palette);
        mouth(level, random, origin, centreY, radius, reach, top[reach * width + reach]);
        return true;
    }

    private static BlockState cover(RandomSource random, boolean capped, int depth, int index,
                                    BlockState[] top, BlockState[] upper, BlockState[] mid, BlockState[] deep) {
        if (!capped) return top[index];

        BlockState banded = depth <= 1 ? upper[index] : (depth <= 3 ? mid[index] : deep[index]);
        if (random.nextFloat() >= SURFACE_MOTTLE) return banded;

        return switch (random.nextInt(3)) {
            case 0 -> upper[index];
            case 1 -> mid[index];
            default -> deep[index];
        };
    }

    private static void mouth(WorldGenLevel level, RandomSource random, BlockPos origin, int centreY,
                              int radius, int reach, BlockState cover) {
        int driftX = 0;
        int driftZ = 0;
        boolean vines = cover.is(BlockTags.DIRT);
        Wobble wobble = wobbleOf(random, MOUTH_WOBBLE);

        List<BlockPos> rim = new ArrayList<>();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int y = centreY + reach; y >= centreY; y--) {
            if (random.nextFloat() < MOUTH_DRIFT_CHANCE) {
                driftX = Mth.clamp(driftX + random.nextInt(3) - 1, -MOUTH_DRIFT, MOUTH_DRIFT);
                driftZ = Mth.clamp(driftZ + random.nextInt(3) - 1, -MOUTH_DRIFT, MOUTH_DRIFT);
            }

            double mouthRadius = MOUTH_RADIUS + random.nextDouble() * MOUTH_RADIUS_VARY;
            int span = (int) Math.ceil(mouthRadius + MOUTH_WOBBLE) + 1;
            boolean opened = false;

            for (int dx = -span; dx <= span; dx++) {
                for (int dz = -span; dz <= span; dz++) {
                    boolean throat = dx == 0 && dz == 0;
                    double edge = Math.sqrt(dx * dx + dz * dz) - mouthRadius - wobble.at(dx, y, dz);

                    if (!throat) {
                        if (edge > 0.0) continue;
                        if (edge > -0.5 && random.nextFloat() < MOUTH_NIBBLE) continue;
                    }

                    pos.set(origin.getX() + driftX + dx, y, origin.getZ() + driftZ + dz);
                    if (!replaceable(level, pos)) continue;
                    if (level.getBlockState(pos).isAir()) continue;

                    level.setBlock(pos, AIR, FLAGS);
                    opened = true;
                    if (y >= centreY + radius - MOUTH_RIM) rim.add(pos.immutable());
                }
            }

            if (!opened && y < centreY + radius) break;
        }

        if (!vines) return;

        for (BlockPos at : rim) {
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                if (random.nextFloat() > SURFACE_VINES) continue;
                if (!level.getBlockState(at).isAir()) continue;

                BlockPos against = at.relative(facing);
                if (!level.getBlockState(against).isFaceSturdy(level, against, facing.getOpposite())) continue;

                level.setBlock(at, Blocks.VINE.defaultBlockState()
                        .setValue(VineBlock.PROPERTY_BY_DIRECTION.get(facing), true), FLAGS);
                break;
            }
        }
    }

    private static boolean underside(WorldGenLevel level, RandomSource random, BlockPos origin, Palette palette) {
        BlockPos top = origin.below();
        if (!level.getBlockState(top).is(Blocks.END_STONE)) return false;

        BlockPos.MutableBlockPos pos = top.mutable();
        int bottom = Integer.MIN_VALUE;
        for (int depth = 1; depth <= UNDERSIDE_DEPTH; depth++) {
            pos.move(Direction.DOWN);
            if (level.getBlockState(pos).isAir()) {
                bottom = pos.getY() + 1;
                break;
            }
        }
        if (bottom == Integer.MIN_VALUE || top.getY() - bottom + 1 < UNDERSIDE_THICKNESS) return false;

        int radius = UNDERSIDE_RADIUS + random.nextInt(UNDERSIDE_RADIUS_VARY);
        BlockPos centre = new BlockPos(top.getX(), bottom + radius - 1, top.getZ());
        int reach = radius + (int) Math.ceil(SHELL + UNDERSIDE_WOBBLE);
        Wobble wobble = wobbleOf(random, UNDERSIDE_WOBBLE);

        for (int dx = -reach; dx <= reach; dx++) {
            for (int dy = -reach; dy <= reach; dy++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    double gap = Math.sqrt(dx * dx + dy * dy + dz * dz) - radius - wobble.at(dx, dy, dz);
                    if (gap > MIDDLE) continue;

                    pos.set(centre.getX() + dx, centre.getY() + dy, centre.getZ() + dz);
                    if (!level.getBlockState(pos).is(Blocks.END_STONE)) continue;

                    if (gap <= 0.0) level.setBlock(pos, AIR, FLAGS);
                    else if (gap <= SKIN) level.setBlock(pos, palette.lining(random, UNDERSIDE_BUDDING), FLAGS);
                    else level.setBlock(pos, palette.middle(), FLAGS);
                }
            }
        }

        int spikes = UNDERSIDE_SPIKES + random.nextInt(UNDERSIDE_SPIKES_VARY);
        for (int i = 0; i < spikes; i++) {
            int span = radius - 1;
            BlockPos from = centre.offset(random.nextInt(span * 2 + 1) - span, 0, random.nextInt(span * 2 + 1) - span);
            BlockPos anchor = anchorOf(level, from, false);
            if (anchor != null) spike(level, random, anchor, false, palette, UNDERSIDE_SPIKE);
        }

        int drips = UNDERSIDE_DRIPS + random.nextInt(UNDERSIDE_DRIPS_VARY);
        for (int i = 0; i < drips; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double out = radius + 1 + random.nextInt(UNDERSIDE_DRIP_REACH);
            BlockPos anchor = dripAnchor(level, centre.offset((int) Math.round(Math.cos(angle) * out), radius,
                    (int) Math.round(Math.sin(angle) * out)));
            if (anchor == null) continue;

            level.setBlock(anchor.above(), palette.lining(random, UNDERSIDE_BUDDING), FLAGS);
            spike(level, random, anchor, false, palette, UNDERSIDE_SPIKE);
        }

        scatterClusters(level, random, centre, radius + 1, UNDERSIDE_CLUSTERS, palette);
        tells(level, random, top, palette);
        return true;
    }

    private static BlockPos dripAnchor(WorldGenLevel level, BlockPos from) {
        BlockPos.MutableBlockPos cursor = from.mutable();
        for (int i = 0; i < UNDERSIDE_DRIP_SCAN; i++) {
            BlockPos below = cursor.below();
            if (level.getBlockState(cursor).is(Blocks.END_STONE) && level.getBlockState(below).isAir()) return below;
            cursor.move(Direction.DOWN);
        }
        return null;
    }

    private static void tells(WorldGenLevel level, RandomSource random, BlockPos top, Palette palette) {
        int tells = UNDERSIDE_TELLS + random.nextInt(UNDERSIDE_TELLS_VARY);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int i = 0; i < tells; i++) {
            int x = top.getX() + random.nextInt(UNDERSIDE_TELL_REACH * 2 + 1) - UNDERSIDE_TELL_REACH;
            int z = top.getZ() + random.nextInt(UNDERSIDE_TELL_REACH * 2 + 1) - UNDERSIDE_TELL_REACH;
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;

            pos.set(x, y, z);
            if (!level.getBlockState(pos).is(Blocks.END_STONE) || !level.getBlockState(pos.above()).isAir()) continue;

            level.setBlock(pos, palette.inner(), FLAGS);
            setCluster(level, pos.above(), palette.clusters().get(random.nextInt(Math.min(2, palette.clusters().size()))), Direction.UP);
        }
    }
}
