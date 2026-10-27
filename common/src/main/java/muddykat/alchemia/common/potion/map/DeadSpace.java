package muddykat.alchemia.common.potion.map;

import muddykat.alchemia.common.items.helper.PathPoint;
import muddykat.alchemia.common.potion.noise.OctaveSimplexNoise;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class DeadSpace {

    public static final int RESOLUTION = 8;

    private static final double[] RING_FACTORS = {1.0, 1.6, 2.6, 3.9};
    private static final double RING_WOBBLE_SCALE = 2.0;
    private static final double RING_WOBBLE_OFFSET = 977.0;
    private static final double SHIELD_ROUGHNESS = 0.45;
    private static final double SHIELD_WOBBLE_SCALE = 3.0;
    private static final double SHIELD_WOBBLE_OFFSET = 613.0;
    private static final double MOAT_CELLS = 1.5;
    private static final double MOAT_ROUGHNESS = 0.35;
    private static final double MOAT_WOBBLE_OFFSET = 389.0;

    public record Settings(double noiseScale, double minThreshold, double thresholdSpread, double maxThreshold,
                           double thresholdStep, int maxGated, boolean scattered, int ringCount, int ringThickness,
                           int ringGates, double ringRoughness, int ringGateDegrees, int effectMargin, int centreMargin,
                           double fieldPatchScale, double fieldCentreCalm) {
    }

    public interface Hop {
        PathPoint land(double x, double y, double dx, double dy, double reach);
    }

    private record Ring(double radius, double[] gates) {
    }

    private final int tiles;
    private final int middle;
    private final int hardMargin;
    private final double cell;
    private final double baseRadius;
    private final float[] depth;
    private final List<float[]> lod = new ArrayList<>();
    private final List<Integer> lodTiles = new ArrayList<>();
    private int crystalGated;

    public DeadSpace(int tiles, int middle, int hardMargin, double cell, double baseRadius) {
        this.tiles = tiles;
        this.middle = middle;
        this.hardMargin = hardMargin;
        this.cell = cell;
        this.baseRadius = baseRadius;
        this.depth = new float[tiles * tiles];
    }

    public static int tileOf(double value) {
        return (int) Math.floor(value / RESOLUTION);
    }

    public static double tileOrigin(int tile) {
        return (double) tile * RESOLUTION;
    }

    public static double tileSize(int level) {
        return RESOLUTION * (1 << Math.max(0, level));
    }

    public int crystalGated() {
        return crystalGated;
    }

    public int tiles() {
        return tiles;
    }

    public boolean isDeadTile(int tileX, int tileY) {
        if (tileX < 0 || tileY < 0 || tileX >= tiles || tileY >= tiles) return false;
        return depth[tileIndex(tileX, tileY)] > 0.0f;
    }

    public boolean isDeadTile(int level, int tileX, int tileY) {
        int clamped = Math.max(0, Math.min(lod.size() - 1, level));
        int count = lodTiles.get(clamped);
        if (tileX < 0 || tileY < 0 || tileX >= count || tileY >= count) return false;
        return lod.get(clamped)[tileY * count + tileX] > 0.0f;
    }

    public int deadTileCount() {
        int count = 0;
        for (float tile : depth) {
            if (tile > 0.0f) count++;
        }
        return count;
    }

    public int lodLevels() {
        return lod.size();
    }

    public int tileCount(int level) {
        return lodTiles.get(Math.max(0, Math.min(lodTiles.size() - 1, level)));
    }

    public double sample(int level, double x, double y) {
        int clamped = Math.max(0, Math.min(lod.size() - 1, level));
        float[] field = lod.isEmpty() ? depth : lod.get(clamped);
        int count = lod.isEmpty() ? tiles : lodTiles.get(clamped);
        double span = tileSize(clamped);

        double u = x / span - 0.5;
        double v = y / span - 0.5;
        int tileX = (int) Math.floor(u);
        int tileY = (int) Math.floor(v);
        double fx = u - tileX;
        double fy = v - tileY;

        double top = lerp(at(field, count, tileX, tileY), at(field, count, tileX + 1, tileY), fx);
        double bottom = lerp(at(field, count, tileX, tileY + 1), at(field, count, tileX + 1, tileY + 1), fx);
        return lerp(top, bottom, fy);
    }

    public void generate(long seed, Random rand, Settings settings, List<PathPoint> effects, List<PathPoint> islands, MapWall wall,
                         Hop hop, PathPoint[] crystals, double crystalReach) {
        OctaveSimplexNoise noise = new OctaveSimplexNoise(seed, List.of(-3, -2, -1, 0));
        double noiseScale = settings.noiseScale();
        double maxThreshold = settings.maxThreshold();
        double step = settings.thresholdStep();
        int maxGated = settings.maxGated() + islands.size();
        double start = settings.minThreshold() + rand.nextDouble() * settings.thresholdSpread();

        boolean scattered = settings.scattered();
        List<Ring> rings = scattered ? List.of() : ringsFor(rand, settings);

        for (double threshold = start; threshold <= maxThreshold; threshold += step) {
            paint(settings, effects, islands, rings, noise, noiseScale, threshold);

            crystalGated = unreachable(false, effects, wall, hop, crystals, crystalReach);
            if (crystalGated <= maxGated && unreachable(true, effects, wall, hop, crystals, crystalReach) == 0) {
                buildLod();
                return;
            }
        }

        Arrays.fill(depth, -1.0f);
        if (!scattered) {
            paintRings(settings, rings, noise, noiseScale);
            carve(settings, effects, noise, noiseScale);
        }
        moats(settings, effects, islands, noise, noiseScale);
        crystalGated = unreachable(false, effects, wall, hop, crystals, crystalReach);

        if (crystalGated > maxGated || unreachable(true, effects, wall, hop, crystals, crystalReach) > 0) {
            Arrays.fill(depth, -1.0f);
            moats(settings, effects, islands, noise, noiseScale);
            crystalGated = unreachable(false, effects, wall, hop, crystals, crystalReach);
            if (unreachable(true, effects, wall, hop, crystals, crystalReach) > 0) {
                Arrays.fill(depth, -1.0f);
                crystalGated = 0;
            }
        }

        buildLod();
    }

    public int unreachable(boolean withCrystals, List<PathPoint> effects, MapWall wall, Hop hop,
                           PathPoint[] crystals, double crystalReach) {
        boolean[] seen = new boolean[tiles * tiles];
        ArrayDeque<int[]> queue = new ArrayDeque<>();

        int startX = tileOf(middle);
        int startY = tileOf(middle);
        seen[tileIndex(startX, startY)] = true;
        queue.add(new int[]{startX, startY});

        while (!queue.isEmpty()) {
            int[] at = queue.poll();

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int tileX = at[0] + dx;
                    int tileY = at[1] + dy;
                    if (tileX < 0 || tileY < 0 || tileX >= tiles || tileY >= tiles) continue;

                    int index = tileIndex(tileX, tileY);
                    if (seen[index] || depth[index] > 0.0f || walledOff(wall, tileX, tileY)) continue;

                    seen[index] = true;
                    queue.add(new int[]{tileX, tileY});
                }
            }

            if (!withCrystals) continue;

            double x = tileOrigin(at[0]) + RESOLUTION / 2.0;
            double y = tileOrigin(at[1]) + RESOLUTION / 2.0;

            for (PathPoint vector : crystals) {
                PathPoint landing = hop.land(x, y, vector.x(), vector.y(), crystalReach);
                int tileX = tileOf(landing.x());
                int tileY = tileOf(landing.y());
                if (tileX < 0 || tileY < 0 || tileX >= tiles || tileY >= tiles) continue;

                int index = tileIndex(tileX, tileY);
                if (seen[index] || depth[index] > 0.0f || walledOff(wall, tileX, tileY)) continue;

                seen[index] = true;
                queue.add(new int[]{tileX, tileY});
            }
        }

        int unreachable = 0;
        for (PathPoint effect : effects) {
            if (!seen[tileIndex(tileOf(effect.x()), tileOf(effect.y()))]) unreachable++;
        }
        return unreachable;
    }

    private static double at(float[] field, int count, int tileX, int tileY) {
        int clampedX = Math.max(0, Math.min(count - 1, tileX));
        int clampedY = Math.max(0, Math.min(count - 1, tileY));
        return field[clampedY * count + clampedX];
    }

    private static double lerp(double from, double to, double t) {
        return from + (to - from) * t;
    }

    private int tileIndex(int tileX, int tileY) {
        return tileY * tiles + tileX;
    }

    private static boolean walledOff(MapWall wall, int tileX, int tileY) {
        return wall.raised() && !wall.inside(tileOrigin(tileX) + RESOLUTION / 2.0, tileOrigin(tileY) + RESOLUTION / 2.0);
    }

    private List<Ring> ringsFor(Random rand, Settings settings) {
        int count = Math.min(settings.ringCount(), RING_FACTORS.length);
        if (count <= 0 || settings.ringThickness() <= 0) return List.of();

        int gates = settings.ringGates();

        List<Ring> rings = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            double radius = baseRadius * RING_FACTORS[index];
            if (radius >= middle - hardMargin) break;

            double[] angles = new double[gates];
            double spin = rand.nextDouble() * 2 * Math.PI;
            for (int gate = 0; gate < gates; gate++) {
                angles[gate] = spin + gate * (2 * Math.PI / gates)
                        + rand.nextDouble(-0.5, 0.5) * (Math.PI / gates);
            }
            rings.add(new Ring(radius, angles));
        }
        return rings;
    }

    private static boolean throughGate(Ring ring, double angle, double gateWidth) {
        for (double gate : ring.gates()) {
            double apart = Math.abs(Math.IEEEremainder(angle - gate, 2 * Math.PI));
            if (apart <= gateWidth / 2.0) return true;
        }
        return false;
    }

    private void paint(Settings settings, List<PathPoint> effects, List<PathPoint> islands, List<Ring> rings,
                       OctaveSimplexNoise noise, double noiseScale, double threshold) {
        if (settings.scattered()) {
            paintField(settings, noise, noiseScale, threshold);
        } else {
            paintBlobs(noise, noiseScale, threshold);
            paintRings(settings, rings, noise, noiseScale);
        }
        carve(settings, effects, noise, noiseScale);
        moats(settings, effects, islands, noise, noiseScale);
    }

    private void moats(Settings settings, List<PathPoint> effects, List<PathPoint> islands, OctaveSimplexNoise noise,
                       double noiseScale) {
        double inner = settings.effectMargin() * (1.0 + SHIELD_ROUGHNESS);
        double thickness = cell * MOAT_CELLS;
        double outer = inner + thickness * (1.0 + MOAT_ROUGHNESS);

        for (PathPoint island : islands) {
            int from = tileOf(island.x() - outer) - 1;
            int to = tileOf(island.x() + outer) + 1;
            int top = tileOf(island.y() - outer) - 1;
            int bottom = tileOf(island.y() + outer) + 1;

            for (int tileX = from; tileX <= to; tileX++) {
                for (int tileY = top; tileY <= bottom; tileY++) {
                    if (tileX < 0 || tileY < 0 || tileX >= tiles || tileY >= tiles) continue;

                    double x = tileOrigin(tileX) + RESOLUTION / 2.0;
                    double y = tileOrigin(tileY) + RESOLUTION / 2.0;
                    double wobble = noise.getValue((x + MOAT_WOBBLE_OFFSET) * noiseScale * SHIELD_WOBBLE_SCALE,
                            (y + MOAT_WOBBLE_OFFSET) * noiseScale * SHIELD_WOBBLE_SCALE, false);
                    double width = thickness * (1.0 + MOAT_ROUGHNESS * wobble);
                    double into = Math.hypot(x - island.x(), y - island.y()) - inner;
                    if (into < 0.0 || into > width || sheltered(settings, effects, island, x, y)) continue;

                    int index = tileIndex(tileX, tileY);
                    depth[index] = (float) Math.max(depth[index], 1.0 - Math.abs(2.0 * into / width - 1.0));
                }
            }
        }
    }

    private boolean sheltered(Settings settings, List<PathPoint> effects, PathPoint island, double x, double y) {
        if (Math.hypot(x - middle, y - middle) < settings.centreMargin()) return true;
        for (PathPoint effect : effects) {
            if ((effect.x() != island.x() || effect.y() != island.y()) && Math.hypot(x - effect.x(), y - effect.y()) < settings.effectMargin()) return true;
        }
        return false;
    }

    private void paintRings(Settings settings, List<Ring> rings, OctaveSimplexNoise noise, double noiseScale) {
        if (rings.isEmpty()) return;

        double thickness = settings.ringThickness();
        double roughness = settings.ringRoughness();
        double gateWidth = Math.toRadians(settings.ringGateDegrees());

        for (int tileX = 0; tileX < tiles; tileX++) {
            for (int tileY = 0; tileY < tiles; tileY++) {
                int index = tileIndex(tileX, tileY);

                double x = tileOrigin(tileX) + RESOLUTION / 2.0;
                double y = tileOrigin(tileY) + RESOLUTION / 2.0;
                double dx = x - middle;
                double dy = y - middle;
                double distance = Math.sqrt(dx * dx + dy * dy);

                double wobble = noise.getValue((x + RING_WOBBLE_OFFSET) * noiseScale * RING_WOBBLE_SCALE,
                        (y + RING_WOBBLE_OFFSET) * noiseScale * RING_WOBBLE_SCALE, false);
                double reach = thickness * (1.0 + roughness * wobble) / 2.0;
                if (reach <= 0.0) continue;

                double angle = Math.atan2(dy, dx);

                for (Ring ring : rings) {
                    double inside = reach - Math.abs(distance - ring.radius());
                    if (inside <= 0.0 || throughGate(ring, angle, gateWidth)) continue;

                    depth[index] = (float) Math.max(depth[index], inside / reach);
                    break;
                }
            }
        }
    }

    private void carve(Settings settings, List<PathPoint> effects, OctaveSimplexNoise noise, double noiseScale) {
        for (PathPoint effect : effects) {
            shield(effect.x(), effect.y(), settings.effectMargin(), noise, noiseScale);
        }
        shield(middle, middle, settings.centreMargin(), noise, noiseScale);
    }

    private void paintBlobs(OctaveSimplexNoise noise, double noiseScale, double threshold) {
        for (int tileX = 0; tileX < tiles; tileX++) {
            for (int tileY = 0; tileY < tiles; tileY++) {
                double x = tileOrigin(tileX) + RESOLUTION / 2.0;
                double y = tileOrigin(tileY) + RESOLUTION / 2.0;
                depth[tileIndex(tileX, tileY)] =
                        (float) (noise.getValue(x * noiseScale, y * noiseScale, false) - threshold);
            }
        }
    }

    private void paintField(Settings settings, OctaveSimplexNoise noise, double noiseScale, double threshold) {
        double detail = settings.fieldPatchScale();
        double calm = settings.fieldCentreCalm();
        double reach = Math.max(cell, middle - hardMargin);

        for (int tileX = 0; tileX < tiles; tileX++) {
            for (int tileY = 0; tileY < tiles; tileY++) {
                double x = tileOrigin(tileX) + RESOLUTION / 2.0;
                double y = tileOrigin(tileY) + RESOLUTION / 2.0;
                double outward = Math.min(1.0, Math.sqrt(Math.pow(x - middle, 2)
                        + Math.pow(y - middle, 2)) / reach);

                double local = threshold + calm * (1.0 - outward);
                depth[tileIndex(tileX, tileY)] = (float) (noise.getValue(
                        x * noiseScale * detail, y * noiseScale * detail, false) - local);
            }
        }
    }

    private void shield(double x, double y, double margin, OctaveSimplexNoise noise, double noiseScale) {
        if (margin <= 0.0) return;

        double reachMax = margin * (1.0 + SHIELD_ROUGHNESS);
        int from = tileOf(-reachMax) - 1;
        int to = tileOf(reachMax) + 1;

        for (int tileX = tileOf(x) + from; tileX <= tileOf(x) + to; tileX++) {
            for (int tileY = tileOf(y) + from; tileY <= tileOf(y) + to; tileY++) {
                if (tileX < 0 || tileY < 0 || tileX >= tiles || tileY >= tiles) continue;

                double tileCentreX = tileOrigin(tileX) + RESOLUTION / 2.0;
                double tileCentreY = tileOrigin(tileY) + RESOLUTION / 2.0;
                double distance = Math.sqrt(Math.pow(tileCentreX - x, 2) + Math.pow(tileCentreY - y, 2));

                double wobble = noise.getValue((tileCentreX + SHIELD_WOBBLE_OFFSET) * noiseScale * SHIELD_WOBBLE_SCALE,
                        (tileCentreY + SHIELD_WOBBLE_OFFSET) * noiseScale * SHIELD_WOBBLE_SCALE, false);

                double clear = margin * (1.0 + SHIELD_ROUGHNESS * wobble) - distance;
                if (clear <= 0.0) continue;

                int index = tileIndex(tileX, tileY);
                depth[index] = (float) Math.min(depth[index], -clear / margin);
            }
        }
    }

    private void buildLod() {
        lod.clear();
        lodTiles.clear();
        lod.add(depth);
        lodTiles.add(tiles);

        float[] previous = depth;
        int count = tiles;

        while (count > 2) {
            int next = (count + 1) / 2;
            float[] level = new float[next * next];

            for (int tileX = 0; tileX < next; tileX++) {
                for (int tileY = 0; tileY < next; tileY++) {
                    float sum = 0.0f;
                    int total = 0;

                    for (int dx = 0; dx < 2; dx++) {
                        for (int dy = 0; dy < 2; dy++) {
                            int childX = tileX * 2 + dx;
                            int childY = tileY * 2 + dy;
                            if (childX >= count || childY >= count) continue;

                            total++;
                            sum += previous[childY * count + childX];
                        }
                    }

                    level[tileY * next + tileX] = total > 0 ? sum / total : -1.0f;
                }
            }

            lod.add(level);
            lodTiles.add(next);
            previous = level;
            count = next;
        }
    }
}
