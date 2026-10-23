package muddykat.alchemia.common.potion;

import muddykat.alchemia.common.items.helper.PathPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public record Whirlpool(int index, PathPoint centre, double radius, int spin, PathPoint exit, List<PathPoint> link) {

    public static final double MIN_RADIUS = 1.3 * PotionMap.CELL;
    public static final double MAX_RADIUS = 2.9 * PotionMap.CELL;
    public static final double SPIN = 1.6;
    private static final double PULL_SHARE = 0.18;
    private static final double CORE_SHARE = 0.22;
    private static final double MIN_CORE = 0.35 * PotionMap.CELL;

    private static final int MIN_COUNT = 5;
    private static final int EXTRA_COUNT = 3;
    private static final int ATTEMPTS = 2000;
    private static final int EXIT_ATTEMPTS = 80;
    private static final int RING_SAMPLES = 20;
    private static final int RINGS = 4;
    private static final int LINK_SAMPLES = 32;
    private static final double START_CLEARANCE = 6 * PotionMap.CELL;
    private static final double EFFECT_CLEARANCE = PotionMap.EFFECT_RADIUS + PotionMap.CELL;
    private static final double SPACING = 12 * PotionMap.CELL;
    private static final double EXIT_MIN = 7 * PotionMap.CELL;
    private static final double EXIT_MAX = 14 * PotionMap.CELL;
    private static final double EXIT_EFFECT_CLEARANCE = PotionMap.EFFECT_RADIUS + PotionMap.CELL;

    public double core() {
        return Math.max(MIN_CORE, radius * CORE_SHARE);
    }

    public boolean touches(PathPoint at) {
        return at.distanceTo(centre) <= radius;
    }

    public boolean swallows(PathPoint at) {
        return at.distanceTo(centre) <= core();
    }

    public PathPoint carry(PathPoint at, double intensity, double seconds) {
        double dx = at.x() - centre.x();
        double dy = at.y() - centre.y();
        double distance = Math.hypot(dx, dy);
        double angle = Math.atan2(dy, dx) + spin * SPIN * intensity * seconds;
        double reach = Math.min(1.0, distance / radius);
        double inward = PULL_SHARE * radius * intensity * seconds * (0.6 + 0.8 * (1.0 - reach));
        double next = Math.max(0.0, distance - inward);
        return new PathPoint(centre.x() + Math.cos(angle) * next, centre.y() + Math.sin(angle) * next);
    }

    public String mark(BrewBase base) {
        return base.id() + "#" + index;
    }

    static List<Whirlpool> scatter(PotionMap map, Random rand) {
        int wanted = MIN_COUNT + rand.nextInt(EXTRA_COUNT);
        int size = map.getMaxAlignment();
        PathPoint start = new PathPoint(map.getMiddlePosition(), map.getMiddlePosition());
        List<Whirlpool> placed = new ArrayList<>();

        for (int attempt = 0; attempt < ATTEMPTS && placed.size() < wanted; attempt++) {
            PathPoint centre = new PathPoint(rand.nextDouble() * size, rand.nextDouble() * size);
            double radius = MIN_RADIUS + rand.nextDouble() * (MAX_RADIUS - MIN_RADIUS);
            if (!clear(map, centre, radius, start, placed)) continue;

            PathPoint exit = exitFor(map, centre, radius, placed, rand);
            if (exit == null) continue;

            int spin = rand.nextBoolean() ? 1 : -1;
            placed.add(new Whirlpool(placed.size(), centre, radius, spin, exit, link(centre, exit, rand)));
        }
        return List.copyOf(placed);
    }

    private static boolean clear(PotionMap map, PathPoint centre, double radius, PathPoint start, List<Whirlpool> placed) {
        if (centre.distanceTo(start) < START_CLEARANCE) return false;
        if (!map.inBounds(centre) || map.isDeadzone(centre)) return false;

        for (int ring = 1; ring <= RINGS; ring++) {
            double reach = radius * ring / RINGS + (ring == RINGS ? PotionMap.CELL / 2.0 : 0.0);
            for (int sample = 0; sample < RING_SAMPLES; sample++) {
                double angle = 2 * Math.PI * sample / RING_SAMPLES;
                PathPoint rim = new PathPoint(centre.x() + Math.cos(angle) * reach, centre.y() + Math.sin(angle) * reach);
                if (!map.inBounds(rim) || map.isDeadzone(rim)) return false;
            }
        }
        for (PotionMap.MapEntry entry : map.entries()) {
            if (entry.position().distanceTo(centre) < EFFECT_CLEARANCE + radius) return false;
        }
        for (Whirlpool other : placed) {
            if (other.centre().distanceTo(centre) < SPACING || other.exit().distanceTo(centre) < radius + PotionMap.CELL) return false;
        }
        return true;
    }

    private static PathPoint exitFor(PotionMap map, PathPoint centre, double radius, List<Whirlpool> placed, Random rand) {
        for (int attempt = 0; attempt < EXIT_ATTEMPTS; attempt++) {
            double angle = rand.nextDouble() * 2 * Math.PI;
            double distance = Math.max(EXIT_MIN, radius + 4 * PotionMap.CELL) + rand.nextDouble() * (EXIT_MAX - EXIT_MIN);
            PathPoint exit = new PathPoint(centre.x() + Math.cos(angle) * distance, centre.y() + Math.sin(angle) * distance);
            if (!map.inBounds(exit) || map.isDeadzone(exit)) continue;

            boolean open = true;
            for (PotionMap.MapEntry entry : map.entries()) {
                if (entry.position().distanceTo(exit) < EXIT_EFFECT_CLEARANCE) {
                    open = false;
                    break;
                }
            }
            for (Whirlpool other : placed) {
                if (!open) break;
                open = other.centre().distanceTo(exit) > other.radius() + PotionMap.CELL && other.exit().distanceTo(exit) > 2 * PotionMap.CELL;
            }
            if (open) return exit;
        }
        return null;
    }

    private static List<PathPoint> link(PathPoint from, PathPoint to, Random rand) {
        double dx = to.x() - from.x();
        double dy = to.y() - from.y();
        double length = Math.hypot(dx, dy);
        double nx = -dy / length;
        double ny = dx / length;

        double first = (0.18 + rand.nextDouble() * 0.2) * (rand.nextBoolean() ? 1 : -1);
        double second = rand.nextBoolean() ? -first * (0.6 + rand.nextDouble() * 0.5) : first * (0.4 + rand.nextDouble() * 0.4);
        PathPoint c1 = new PathPoint(from.x() + dx / 3 + nx * first * length, from.y() + dy / 3 + ny * first * length);
        PathPoint c2 = new PathPoint(from.x() + dx * 2 / 3 + nx * second * length, from.y() + dy * 2 / 3 + ny * second * length);

        List<PathPoint> points = new ArrayList<>(LINK_SAMPLES + 1);
        for (int step = 0; step <= LINK_SAMPLES; step++) {
            double t = (double) step / LINK_SAMPLES;
            double u = 1 - t;
            //cubic bezier... may extract to function
            double x = u * u * u * from.x() + 3 * u * u * t * c1.x() + 3 * u * t * t * c2.x() + t * t * t * to.x();
            double y = u * u * u * from.y() + 3 * u * u * t * c1.y() + 3 * u * t * t * c2.y() + t * t * t * to.y();
            points.add(new PathPoint(x, y));
        }
        return List.copyOf(points);
    }
}
