package muddykat.alchemia.common.potion;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.config.Configuration;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.helper.IngredientAlignment;
import muddykat.alchemia.common.items.helper.IngredientPath;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.items.helper.PathGeometry;
import muddykat.alchemia.common.items.helper.PathPoint;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Holder;
import muddykat.alchemia.common.potion.noise.OctaveSimplexNoise;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;

import java.util.*;

public class PotionMap {
    private static final Map<String, PotionMap> MAPS = new LinkedHashMap<>();
    private static long currentSeed;
    private static boolean seeded;

    public static final double CELL = PathGeometry.CELL;
    public static final int HARD_MARGIN = (int) (3 * CELL);
    public static final int DEADZONE_RESOLUTION = 8;
    public static final int EFFECT_RINGS = 2;
    public static final double EFFECT_RADIUS = EFFECT_RINGS * CELL;
    public static final double EFFECT_CORE = CELL / 2.0;

    private static final double BASE_RADIUS_FRACTION = 15.0 / 68.0;
    private static final double SPACING_RELIEF = CELL;
    private static final double STALL_THRESHOLD = 1.0;

    private final BrewBase base;
    private final int size;
    private final int softSize;
    private final int middlePosition;
    private final int tiles;
    private final float[] depth;
    private final List<float[]> lod = new ArrayList<>();
    private final List<Integer> lodTiles = new ArrayList<>();
    private final List<MapEntry> entries = new ArrayList<>();

    public PotionMap(long seed, BrewBase base) {
        Random rand = new Random(seed);
        this.base = base;

        softSize = (int) (PotionEnum.values().length * 2 * CELL);
        tiles = (softSize + HARD_MARGIN * 2 + DEADZONE_RESOLUTION - 1) / DEADZONE_RESOLUTION;
        size = tiles * DEADZONE_RESOLUTION;
        middlePosition = size / 2;
        depth = new float[tiles * tiles];

        double minDistance = Configuration.effectSpacing();

        for (PotionEnum e : PotionEnum.values()) {
            if (!base.allows(e)) continue;
            Holder<MobEffect> effect = e.getEffect();
            int strength = e.maxStrength;
            boolean placed = false;
            double spacing = minDistance;
            int attempts = 0;

            while (!placed) {
                if (attempts >= 250) {
                    if (spacing <= SPACING_RELIEF) break;
                    spacing -= SPACING_RELIEF;
                    attempts = 0;
                }

                int biasX = 0;
                int biasY = 0;
                for (IngredientAlignment alignment : e.getAlignments()) {
                    biasX += alignment.getX();
                    biasY += alignment.getY();
                }

                if (biasX == 0 && biasY == 0) {
                    biasX = rand.nextInt(3) - 1;
                    biasY = rand.nextInt(3) - 1;
                }

                int rarityOrdinal = e.getRarity().ordinal();
                double baseRadius = size * BASE_RADIUS_FRACTION;
                double jitter = rand.nextInt(-2, 2) * CELL;

                double randomRadius;
                if (rarityOrdinal == 0) {
                    randomRadius = baseRadius * 0.75 + jitter;
                } else if (rarityOrdinal == 1) {
                    randomRadius = baseRadius * 1.25 + jitter;
                } else if (rarityOrdinal == 2) {
                    randomRadius = baseRadius * 2 + jitter;
                } else {
                    randomRadius = baseRadius * 3.25 + jitter;
                }

                double angle = Math.atan2(biasY, biasX) + (rand.nextDouble(-1, 1)) * Math.PI / 4.0;

                int randX = middlePosition + (int) Math.round(randomRadius * Math.cos(angle));
                int randY = middlePosition + (int) Math.round(randomRadius * Math.sin(angle));

                randX = Math.max(HARD_MARGIN, Math.min(size - 1 - HARD_MARGIN, randX));
                randY = Math.max(HARD_MARGIN, Math.min(size - 1 - HARD_MARGIN, randY));

                boolean positionValid = true;

                for (MapEntry entry : entries) {
                    double distance = Math.sqrt(Math.pow(entry.x() - randX, 2) + Math.pow(entry.y() - randY, 2));
                    if (distance < spacing) {
                        positionValid = false;
                        break;
                    }
                }

                if (positionValid) {
                    entries.add(new MapEntry(randX, randY,
                            new PotionEffectPosition(e, effect, 1200, strength, e.getPotion(), true)));
                    placed = true;
                }

                attempts++;
            }

            if (!placed) placed = placeAnywhere(e, effect, strength);

            if (!placed) {
                Alchemia.LOGGER.warn("Could not place potion effect {}", e.name());
            }
        }

        generateDeadzones(seed, rand);
        Alchemia.LOGGER.info("Potion map built for {}: {}px across, {} effects, {} dead tiles, {} effects need crystals",
                base.id(), size, entries.size(), deadTileCount(), crystalGated);
    }

    private int crystalGated;

    public boolean isDeadzone(double x, double y) {
        if (!inBounds(x, y)) return false;
        return sample(0, x, y) > 0.0;
    }

    public boolean isDeadzone(PathPoint point) {
        return isDeadzone(point.x(), point.y());
    }

    public boolean isDeadTile(int tileX, int tileY) {
        if (tileX < 0 || tileY < 0 || tileX >= tiles || tileY >= tiles) return false;
        return depth[tileIndex(tileX, tileY)] > 0.0f;
    }

    public boolean deadAt(int level, double x, double y) {
        return inBounds(x, y) && sample(level, x, y) > 0.0;
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

    private static double at(float[] field, int count, int tileX, int tileY) {
        int clampedX = Math.max(0, Math.min(count - 1, tileX));
        int clampedY = Math.max(0, Math.min(count - 1, tileY));
        return field[clampedY * count + clampedX];
    }

    private static double lerp(double from, double to, double t) {
        return from + (to - from) * t;
    }

    public boolean inBounds(double x, double y) {
        return x >= 0 && x < size && y >= 0 && y < size;
    }

    public boolean inBounds(PathPoint point) {
        return inBounds(point.x(), point.y());
    }

    public double clamp(double value) {
        return Math.max(0, Math.min(size - 1, value));
    }

    public int tileCount() {
        return tiles;
    }

    public int deadTileCount() {
        int count = 0;
        for (float tile : depth) {
            if (tile > 0.0f) count++;
        }
        return count;
    }

    public static int tileOf(double value) {
        return (int) Math.floor(value / DEADZONE_RESOLUTION);
    }

    public static double tileOrigin(int tile) {
        return (double) tile * DEADZONE_RESOLUTION;
    }

    private int tileIndex(int tileX, int tileY) {
        return tileY * tiles + tileX;
    }

    public int crystalGatedEffects() {
        return crystalGated;
    }

    private static final PathPoint[] CRYSTAL_VECTORS = crystalVectors();

    private static PathPoint[] crystalVectors() {
        List<PathPoint> vectors = new ArrayList<>();
        for (Ingredients ingredient : Ingredients.values()) {
            if (ingredient.getType() != IngredientType.Mineral) continue;
            PathPoint bearing = new PathPoint(
                    ingredient.getPrimaryAlignment().getX() + ingredient.getSecondaryAlignment().getX(),
                    ingredient.getPrimaryAlignment().getY() + ingredient.getSecondaryAlignment().getY()).normalised();
            if (!bearing.isZero()) vectors.add(bearing);
        }
        return vectors.toArray(PathPoint[]::new);
    }

    public static boolean isTeleport(ItemStack stack) {
        return stack.getItem() instanceof ItemIngredient ingredient
                && ingredient.getIngredientType() == IngredientType.Mineral;
    }

    public static double teleportReach(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemIngredient ingredient)) return IngredientPath.teleportRadius(1);
        return IngredientPath.reachFor(ingredient.getPotency(stack), ItemIngredient.trimOf(stack));
    }

    public static double teleportCeiling(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemIngredient ingredient)) return IngredientPath.teleportRadius(1);
        return IngredientPath.teleportCeiling(ingredient.getIngredient());
    }

    public static int teleportDistance(ItemStack stack) {
        double reach = teleportReach(stack);

        if (stack.getItem() instanceof ItemIngredient ingredient) {
            IngredientPath shape = IngredientPath.of(ingredient.getIngredient());
            if (shape.followsFlight()) {
                return (int) Math.round(shape.flightEnd(Integer.signum(ingredient.getDriftX(stack)),
                        Integer.signum(ingredient.getDriftY(stack)), teleportCeiling(stack), reach).length());
            }
        }
        return (int) Math.round(reach);
    }

    public static int teleportDistance(double potency) {
        return IngredientPath.teleportDistance(potency);
    }

    public double flightReach(double x, double y, IngredientPath shape, int dx, int dy,
                              double ceiling, double radius, int rotation) {
        double turn = IngredientPath.rotationRadians(rotation);

        for (double reach = radius; reach >= CELL; reach -= CELL) {
            PathPoint at = new PathPoint(x, y).plus(shape.flightEnd(dx, dy, ceiling, reach).rotate(turn));
            if (inBounds(at)) return reach;
        }
        return 0.0;
    }

    public PathPoint flightLanding(double x, double y, IngredientPath shape, int dx, int dy,
                                  double ceiling, double radius, int rotation) {
        double reach = flightReach(x, y, shape, dx, dy, ceiling, radius, rotation);
        if (reach <= 0.0) return new PathPoint(x, y);

        return new PathPoint(x, y).plus(
                shape.flightEnd(dx, dy, ceiling, reach).rotate(IngredientPath.rotationRadians(rotation)));
    }

    public PathPoint teleport(double x, double y, double dx, double dy, double distance) {
        PathPoint bearing = new PathPoint(dx, dy).normalised();
        if (bearing.isZero()) return new PathPoint(middlePosition, middlePosition);

        for (double reach = distance; reach >= CELL; reach -= CELL) {
            PathPoint at = new PathPoint(x, y).plus(bearing.scale(reach));
            if (inBounds(at)) return at;
        }
        return new PathPoint(x, y);
    }

    public record PotionPath(List<PathPoint> points, boolean teleport, boolean stalled, boolean dead) {
        public PathPoint start() {
            return points.isEmpty() ? PathPoint.ORIGIN : points.get(0);
        }

        public PathPoint end(double fromX, double fromY) {
            return points.isEmpty() ? new PathPoint(fromX, fromY) : points.get(points.size() - 1);
        }

        public PathPoint end() {
            return end(0, 0);
        }

        public double length() {
            return PathGeometry.length(points);
        }
    }

    public PathPoint positionOf(PotionEnum recipe) {
        for (MapEntry entry : entries) {
            if (entry.effect().getRecipe() == recipe) return new PathPoint(entry.x(), entry.y());
        }
        return null;
    }

    private PotionPath stalledAt(double x, double y) {
        return new PotionPath(List.of(new PathPoint(x, y)), false, true, false);
    }

    public List<PathPoint> fixedDriftOffsets(PotionEnum target) {
        PathPoint goal = positionOf(target);
        if (goal == null) return List.of();

        PathPoint bearing = goal.minus(new PathPoint(middlePosition, middlePosition)).normalised();
        if (bearing.isZero()) return List.of();

        return PathGeometry.resample(
                List.of(PathPoint.ORIGIN, bearing.scale(VanillaIngredients.DRIFT_LENGTH * CELL)),
                PathGeometry.SAMPLE_SPACING);
    }

    private PotionPath fixedPath(double x, double y, PotionEnum target) {
        return trail(x, y, fixedDriftOffsets(target));
    }

    public PotionPath project(double x, double y, List<PathPoint> offsets, boolean throughDeadzones) {
        List<PathPoint> points = new ArrayList<>(offsets.size() + 1);
        points.add(new PathPoint(x, y));

        for (PathPoint offset : offsets) {
            if (offset.isZero()) continue;

            PathPoint at = new PathPoint(x + offset.x(), y + offset.y());
            if (!inBounds(at) || (!throughDeadzones && isDeadzone(at))) break;
            points.add(at);
        }

        boolean stalled = PathGeometry.length(points) < STALL_THRESHOLD;
        return new PotionPath(stalled ? List.of(points.get(0)) : points, false, stalled, false);
    }

    private PotionPath trail(double x, double y, List<PathPoint> offsets) {
        return project(x, y, offsets, false);
    }

    public PotionPath walk(double x, double y, ItemStack stack) {
        VanillaIngredients.Drift drift = VanillaIngredients.driftOf(stack);
        if (drift != null) return drift.lure() ? stalledAt(x, y) : fixedPath(x, y, drift.target());

        if (!(stack.getItem() instanceof ItemIngredient ingredient)) return stalledAt(x, y);

        int dx = Integer.signum(ingredient.getDriftX(stack));
        int dy = Integer.signum(ingredient.getDriftY(stack));
        double potency = ingredient.getPotency(stack);
        IngredientPath shape = IngredientPath.of(ingredient.getIngredient());

        int[] aimed = IngredientPath.rotateDirection(dx, dy, ItemIngredient.rotationOf(stack));

        if (shape.teleports()) {
            PathPoint landing = shape.followsFlight()
                    ? flightLanding(x, y, shape, dx, dy, teleportCeiling(stack), teleportReach(stack),
                            ItemIngredient.rotationOf(stack))
                    : teleport(x, y, aimed[0], aimed[1], teleportReach(stack));

            List<PathPoint> hop = List.of(new PathPoint(x, y), landing);

            if (isDeadzone(landing)) return new PotionPath(hop, true, false, true);

            boolean moved = landing.distanceTo(x, y) >= STALL_THRESHOLD;
            return new PotionPath(moved ? hop : List.of(new PathPoint(x, y)), true, !moved, false);
        }

        if (shape.needsDirection() && dx == 0 && dy == 0) return stalledAt(x, y);

        return trail(x, y, IngredientPath.pathFor(ingredient.getIngredient(), dx, dy, potency,
                ItemIngredient.trimOf(stack), ItemIngredient.rotationOf(stack)));
    }


    public static final double LURE_PULL = 4 * CELL;

    public record RouteLeg(ItemStack stack, PotionPath path, List<PathPoint> curve, boolean lure) {
        public PathPoint end(double fromX, double fromY) {
            return path.end(fromX, fromY);
        }
    }

    private static final class Span {
        final ItemStack stack;
        final boolean lure;
        List<PathPoint> points;
        List<PathPoint> curve;
        boolean teleport;
        boolean stalled;
        boolean dead;

        Span(ItemStack stack, boolean lure, PotionPath path, List<PathPoint> curve) {
            this.stack = stack;
            this.lure = lure;
            this.points = new ArrayList<>(path.points());
            this.curve = curve;
            this.teleport = path.teleport();
            this.stalled = path.stalled();
            this.dead = path.dead();
        }

        RouteLeg leg() {
            return new RouteLeg(stack, new PotionPath(List.copyOf(points), teleport, stalled, dead),
                    List.copyOf(curve), lure);
        }
    }

    public List<RouteLeg> route(double startX, double startY, List<ItemStack> queue) {
        return route(startX, startY, queue, List.of());
    }

    public List<RouteLeg> route(double startX, double startY, List<ItemStack> queue, List<Integer> anchors) {
        List<Span> spans = new ArrayList<>(queue.size());
        double x = startX;
        double y = startY;

        for (ItemStack stack : queue) {
            VanillaIngredients.Drift drift = VanillaIngredients.driftOf(stack);

            if (drift != null && drift.lure()) {
                int anchor = spans.size() < anchors.size() ? anchors.get(spans.size()) : 0;
                boolean pulled = pull(spans, stack, drift.target(), anchor);
                PathPoint here = pulled ? chainEnd(spans, startX, startY) : new PathPoint(x, y);

                spans.add(new Span(stack, true,
                        new PotionPath(List.of(here), false, !pulled, false), List.of()));
                x = here.x();
                y = here.y();
                continue;
            }

            PotionPath path = walk(x, y, stack);
            spans.add(new Span(stack, false, path, path.teleport() ? teleportCurve(x, y, stack) : List.of()));

            PathPoint end = path.end(x, y);
            x = end.x();
            y = end.y();
        }

        List<RouteLeg> legs = new ArrayList<>(spans.size());
        for (Span span : spans) legs.add(span.leg());
        return legs;
    }

    private PathPoint chainEnd(List<Span> spans, double startX, double startY) {
        for (int index = spans.size() - 1; index >= 0; index--) {
            List<PathPoint> points = spans.get(index).points;
            if (!points.isEmpty()) return points.get(points.size() - 1);
        }
        return new PathPoint(startX, startY);
    }

    private boolean pull(List<Span> spans, ItemStack stack, PotionEnum target, int anchor) {
        PathPoint goal = positionOf(target);
        if (goal == null) return false;

        int from = 0;
        for (int index = spans.size() - 1; index >= 0; index--) {
            Span span = spans.get(index);
            if (span.dead || (span.lure && span.stack.getItem() == stack.getItem())) {
                from = index + 1;
                break;
            }
        }
        from = Math.max(from, Math.min(anchor, spans.size()));

        List<PathPoint> chain = new ArrayList<>();
        for (int index = from; index < spans.size(); index++) chain.addAll(spans.get(index).points);
        if (chain.size() < 2) return false;

        List<PathPoint> pulled = new ArrayList<>(chain.size());
        for (int index = 0; index < chain.size(); index++) {
            PathPoint point = chain.get(index);
            PathPoint toGoal = goal.minus(point);
            double reach = Math.min(toGoal.length(), LURE_PULL * index / (chain.size() - 1));
            PathPoint moved = point.plus(toGoal.normalised().scale(reach));

            if (index > 0 && (!inBounds(moved) || isDeadzone(moved))) break;
            pulled.add(moved);
        }

        int cursor = 0;
        for (int index = from; index < spans.size(); index++) {
            Span span = spans.get(index);
            int size = span.points.size();

            List<PathPoint> slice = new ArrayList<>(size);
            for (int step = 0; step < size && cursor + step < pulled.size(); step++) {
                slice.add(pulled.get(cursor + step));
            }
            cursor += size;

            if (slice.isEmpty()) slice.add(pulled.get(pulled.size() - 1));

            span.points = slice;
            span.curve = List.of();
            span.stalled = slice.size() < 2;
            if (span.stalled) span.teleport = false;
        }
        return true;
    }

    public List<PathPoint> teleportFlight(double x, double y, ItemStack stack) {
        if (!(stack.getItem() instanceof ItemIngredient ingredient)) return List.of();
        IngredientPath shape = IngredientPath.of(ingredient.getIngredient());
        if (!shape.followsFlight()) return List.of();

        int dx = Integer.signum(ingredient.getDriftX(stack));
        int dy = Integer.signum(ingredient.getDriftY(stack));
        int rotation = ItemIngredient.rotationOf(stack);
        double ceiling = teleportCeiling(stack);
        double reach = flightReach(x, y, shape, dx, dy, ceiling, teleportReach(stack), rotation);
        if (reach <= 0.0) return List.of();

        List<PathPoint> flight = IngredientPath.rotated(PathGeometry.resample(
                shape.flightTo(dx, dy, ceiling, reach), PathGeometry.SAMPLE_SPACING), rotation);

        List<PathPoint> curve = new ArrayList<>(flight.size());
        for (PathPoint offset : flight) {
            PathPoint at = new PathPoint(x + offset.x(), y + offset.y());
            if (!inBounds(at)) break;
            curve.add(at);
        }
        return curve;
    }

    public List<PathPoint> teleportCurve(double x, double y, ItemStack stack) {
        if (!(stack.getItem() instanceof ItemIngredient ingredient)) return List.of();

        IngredientPath shape = IngredientPath.of(ingredient.getIngredient());
        if (!shape.teleports()) return List.of();

        int dx = Integer.signum(ingredient.getDriftX(stack));
        int dy = Integer.signum(ingredient.getDriftY(stack));

        List<PathPoint> flight = IngredientPath.rotated(
                shape.teleportPreview(dx, dy, teleportCeiling(stack), teleportReach(stack)),
                ItemIngredient.rotationOf(stack));

        List<PathPoint> curve = new ArrayList<>(flight.size());
        for (PathPoint offset : flight) {
            PathPoint at = new PathPoint(x + offset.x(), y + offset.y());
            if (!inBounds(at)) break;
            curve.add(at);
        }
        return curve;
    }

    public int[] step(double x, double y, ItemStack stack) {
        PathPoint end = walk(x, y, stack).end(x, y);
        return new int[]{end.roundedX(), end.roundedY()};
    }

    public MapEntry nearest(double x, double y, double within) {
        MapEntry closest = null;
        double closestDistance = within;

        for (MapEntry entry : entries) {
            double distance = Math.sqrt(Math.pow(entry.x() - x, 2) + Math.pow(entry.y() - y, 2));
            if (distance <= closestDistance) {
                closest = entry;
                closestDistance = distance;
            }
        }
        return closest;
    }

    public PotionEffectPosition getEffectAt(double x, double y) {
        MapEntry entry = nearest(x, y, EFFECT_CORE);
        return entry == null ? null : entry.effect();
    }

    public List<MapEntry> touched(List<PathPoint> points, boolean includeLast) {
        List<MapEntry> hit = new ArrayList<>();
        int last = includeLast ? points.size() : points.size() - 1;

        for (int i = 0; i < last; i++) {
            MapEntry entry = nearest(points.get(i).x(), points.get(i).y(), EFFECT_CORE);
            if (entry != null && !hit.contains(entry)) hit.add(entry);
        }
        return hit;
    }

    private static final double[] RING_FACTORS = {1.0, 1.6, 2.6, 3.9};
    private static final double RING_WOBBLE_SCALE = 2.0;
    private static final double RING_WOBBLE_OFFSET = 977.0;

    private record Ring(double radius, double[] gates) {
    }

    private List<Ring> ringsFor(Random rand) {
        int count = Math.min(Configuration.ringCount(), RING_FACTORS.length);
        if (count <= 0 || Configuration.ringThickness() <= 0) return List.of();

        double baseRadius = size * BASE_RADIUS_FRACTION;
        int gates = Configuration.ringGates();

        List<Ring> rings = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            double radius = baseRadius * RING_FACTORS[index];
            if (radius >= middlePosition - HARD_MARGIN) break;

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

    private void paintRings(List<Ring> rings, OctaveSimplexNoise noise, double noiseScale) {
        if (rings.isEmpty()) return;

        double thickness = Configuration.ringThickness();
        double roughness = Configuration.ringRoughness();
        double gateWidth = Math.toRadians(Configuration.ringGateDegrees());

        for (int tileX = 0; tileX < tiles; tileX++) {
            for (int tileY = 0; tileY < tiles; tileY++) {
                int index = tileIndex(tileX, tileY);

                double x = tileOrigin(tileX) + DEADZONE_RESOLUTION / 2.0;
                double y = tileOrigin(tileY) + DEADZONE_RESOLUTION / 2.0;
                double dx = x - middlePosition;
                double dy = y - middlePosition;
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

    private void generateDeadzones(long seed, Random rand) {
        OctaveSimplexNoise noise = new OctaveSimplexNoise(seed, List.of(-3, -2, -1, 0));
        double noiseScale = Configuration.noiseScale();
        double maxThreshold = Configuration.maxThreshold();
        double step = Configuration.thresholdStep();
        int maxGated = Configuration.maxCrystalGated();
        double start = Configuration.minThreshold() + rand.nextDouble() * Configuration.thresholdSpread();

        boolean scattered = base.deadSpace() == BrewBase.DeadSpace.FIELD;
        List<Ring> rings = scattered ? List.of() : ringsFor(rand);

        for (double threshold = start; threshold <= maxThreshold; threshold += step) {
            paint(scattered, rings, noise, noiseScale, threshold);

            crystalGated = countUnreachable(false);
            if (crystalGated <= maxGated && countUnreachable(true) == 0) {
                buildLod();
                return;
            }
        }

        Arrays.fill(depth, -1.0f);
        if (!scattered) {
            paintRings(rings, noise, noiseScale);
            carve(noise, noiseScale);
        }
        crystalGated = countUnreachable(false);

        if (crystalGated > maxGated || countUnreachable(true) > 0) {
            Arrays.fill(depth, -1.0f);
            crystalGated = 0;
        }

        buildLod();
    }

    private void paint(boolean scattered, List<Ring> rings, OctaveSimplexNoise noise, double noiseScale, double threshold) {
        if (scattered) {
            paintField(noise, noiseScale, threshold);
        } else {
            paintBlobs(noise, noiseScale, threshold);
            paintRings(rings, noise, noiseScale);
        }
        carve(noise, noiseScale);
    }

    private void carve(OctaveSimplexNoise noise, double noiseScale) {
        for (MapEntry entry : entries) {
            shield(entry.x(), entry.y(), Configuration.effectMargin(), noise, noiseScale);
        }
        shield(middlePosition, middlePosition, Configuration.centreMargin(), noise, noiseScale);
    }

    private void paintBlobs(OctaveSimplexNoise noise, double noiseScale, double threshold) {
        for (int tileX = 0; tileX < tiles; tileX++) {
            for (int tileY = 0; tileY < tiles; tileY++) {
                double x = tileOrigin(tileX) + DEADZONE_RESOLUTION / 2.0;
                double y = tileOrigin(tileY) + DEADZONE_RESOLUTION / 2.0;
                depth[tileIndex(tileX, tileY)] =
                        (float) (noise.getValue(x * noiseScale, y * noiseScale, false) - threshold);
            }
        }
    }

    private void paintField(OctaveSimplexNoise noise, double noiseScale, double threshold) {
        double detail = Configuration.fieldPatchScale();
        double calm = Configuration.fieldCentreCalm();
        double reach = Math.max(CELL, middlePosition - HARD_MARGIN);

        for (int tileX = 0; tileX < tiles; tileX++) {
            for (int tileY = 0; tileY < tiles; tileY++) {
                double x = tileOrigin(tileX) + DEADZONE_RESOLUTION / 2.0;
                double y = tileOrigin(tileY) + DEADZONE_RESOLUTION / 2.0;
                double outward = Math.min(1.0, Math.sqrt(Math.pow(x - middlePosition, 2)
                        + Math.pow(y - middlePosition, 2)) / reach);

                double local = threshold + calm * (1.0 - outward);
                depth[tileIndex(tileX, tileY)] = (float) (noise.getValue(
                        x * noiseScale * detail, y * noiseScale * detail, false) - local);
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

    public int lodLevels() {
        return lod.size();
    }

    public int tileCount(int level) {
        return lodTiles.get(Math.max(0, Math.min(lodTiles.size() - 1, level)));
    }

    public static double tileSize(int level) {
        return DEADZONE_RESOLUTION * (1 << Math.max(0, level));
    }

    public static double tileOrigin(int level, int tile) {
        return tile * tileSize(level);
    }

    public boolean isDeadTile(int level, int tileX, int tileY) {
        int clamped = Math.max(0, Math.min(lod.size() - 1, level));
        int count = lodTiles.get(clamped);
        if (tileX < 0 || tileY < 0 || tileX >= count || tileY >= count) return false;
        return lod.get(clamped)[tileY * count + tileX] > 0.0f;
    }

    private boolean placeAnywhere(PotionEnum recipe, Holder<MobEffect> effect, int strength) {
        for (int x = HARD_MARGIN; x <= size - 1 - HARD_MARGIN; x += (int) CELL) {
            for (int y = HARD_MARGIN; y <= size - 1 - HARD_MARGIN; y += (int) CELL) {
                if (nearest(x, y, CELL) != null) continue;
                entries.add(new MapEntry(x, y,
                        new PotionEffectPosition(recipe, effect, 1200, strength, recipe.getPotion(), true)));
                return true;
            }
        }
        return false;
    }

    private static final double SHIELD_ROUGHNESS = 0.45;
    private static final double SHIELD_WOBBLE_SCALE = 3.0;
    private static final double SHIELD_WOBBLE_OFFSET = 613.0;

    private void shield(double x, double y, double margin, OctaveSimplexNoise noise, double noiseScale) {
        if (margin <= 0.0) return;

        double reachMax = margin * (1.0 + SHIELD_ROUGHNESS);
        int from = tileOf(-reachMax) - 1;
        int to = tileOf(reachMax) + 1;

        for (int tileX = tileOf(x) + from; tileX <= tileOf(x) + to; tileX++) {
            for (int tileY = tileOf(y) + from; tileY <= tileOf(y) + to; tileY++) {
                if (tileX < 0 || tileY < 0 || tileX >= tiles || tileY >= tiles) continue;

                double tileCentreX = tileOrigin(tileX) + DEADZONE_RESOLUTION / 2.0;
                double tileCentreY = tileOrigin(tileY) + DEADZONE_RESOLUTION / 2.0;
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

    private int countUnreachable(boolean withCrystals) {
        boolean[] seen = new boolean[tiles * tiles];
        ArrayDeque<int[]> queue = new ArrayDeque<>();

        int startX = tileOf(middlePosition);
        int startY = tileOf(middlePosition);
        seen[tileIndex(startX, startY)] = true;
        queue.add(new int[]{startX, startY});

        double crystalReach = IngredientPath.teleportRadius(1);

        while (!queue.isEmpty()) {
            int[] at = queue.poll();

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int tileX = at[0] + dx;
                    int tileY = at[1] + dy;
                    if (tileX < 0 || tileY < 0 || tileX >= tiles || tileY >= tiles) continue;

                    int index = tileIndex(tileX, tileY);
                    if (seen[index] || depth[index] > 0.0f) continue;

                    seen[index] = true;
                    queue.add(new int[]{tileX, tileY});
                }
            }

            if (!withCrystals) continue;

            double x = tileOrigin(at[0]) + DEADZONE_RESOLUTION / 2.0;
            double y = tileOrigin(at[1]) + DEADZONE_RESOLUTION / 2.0;

            for (PathPoint vector : CRYSTAL_VECTORS) {
                PathPoint landing = teleport(x, y, vector.x(), vector.y(), crystalReach);
                int tileX = tileOf(landing.x());
                int tileY = tileOf(landing.y());
                if (tileX < 0 || tileY < 0 || tileX >= tiles || tileY >= tiles) continue;

                int index = tileIndex(tileX, tileY);
                if (seen[index] || depth[index] > 0.0f) continue;

                seen[index] = true;
                queue.add(new int[]{tileX, tileY});
            }
        }

        int unreachable = 0;
        for (MapEntry entry : entries) {
            if (!seen[tileIndex(tileOf(entry.x()), tileOf(entry.y()))]) unreachable++;
        }
        return unreachable;
    }

    public static void scramble(long seed) {
        currentSeed = seed;
        seeded = true;
        rebuild();
    }

    public static void rebuild() {
        if (!seeded) return;
        MAPS.clear();
        for (BrewBase brewBase : BrewBases.values()) {
            MAPS.put(brewBase.id(), new PotionMap(seedFor(currentSeed, brewBase), brewBase));
        }
    }

    public static long seedFor(long seed, BrewBase base) {
        long mixed = seed ^ (base.id().hashCode() * 0x9E3779B97F4A7C15L);
        mixed ^= mixed >>> 33;
        mixed *= 0xFF51AFD7ED558CCDL;
        mixed ^= mixed >>> 33;
        return mixed;
    }

    public static PotionMap get(BrewBase base) {
        PotionMap map = MAPS.get(base.id());
        return map != null ? map : MAPS.get(BrewBases.defaultBase().id());
    }

    public static boolean isReady() {
        return seeded && !MAPS.isEmpty();
    }

    public BrewBase base() {
        return base;
    }

    public PotionEffectPosition getEffectPotion(double x, double y) {
        PotionEffectPosition defaultEffect = new PotionEffectPosition(null, null, 10, 0, Potions.AWKWARD, false);

        MapEntry closest = nearest(x, y, EFFECT_RADIUS);
        if (closest == null) return defaultEffect;

        double distance = Math.sqrt(Math.pow(closest.x() - x, 2) + Math.pow(closest.y() - y, 2));
        if (distance <= EFFECT_CORE) return closest.effect();

        int ring = Math.max(1, (int) Math.ceil(distance / CELL));
        int falloff = Math.max(0, closest.effect().getStrength() - ring);
        return new PotionEffectPosition(closest.effect().getRecipe(), closest.effect().getEffect(),
                closest.effect().getDuration(), falloff, closest.effect().getPotion(), false);
    }

    public PotionEffectPosition getEffectPotion(PathPoint point) {
        return getEffectPotion(point.x(), point.y());
    }

    public List<MapEntry> entries() {
        return entries;
    }

    public int getSize() {
        return size;
    }

    public int getSoftSize() {
        return softSize;
    }

    public int getMiddlePosition() {
        return middlePosition;
    }

    public int getMaxAlignment() {
        return size;
    }

    public record MapEntry(int x, int y, PotionEffectPosition effect) {
        public PathPoint position() {
            return new PathPoint(x, y);
        }
    }

    public static class PotionEffectPosition {
        final PotionEnum recipe;
        final Holder<MobEffect> effect;
        final int duration;
        final int strength;
        final Holder<Potion> potion;
        final boolean perfect;
        PotionEffectPosition(PotionEnum recipe, Holder<MobEffect> effect, int duration, int maxStrength, Holder<Potion> potion, boolean isPerfect) {
            this.recipe = recipe;
            this.effect = effect;
            this.duration = duration;
            this.strength = maxStrength;
            this.potion = potion;
            this.perfect = isPerfect;
        }

        public PotionEnum getRecipe() {
            return recipe;
        }

        public Holder<MobEffect> getEffect() {
            return effect;
        }

        public int getDuration() {
            return duration;
        }

        public int getStrength() {
            return strength;
        }

        public Holder<Potion> getPotion() {
            return potion;
        }

        public boolean isPerfect() {
            return perfect;
        }

        public PotionEffectPosition capped(int maxStrength) {
            if (strength <= maxStrength) return this;
            return new PotionEffectPosition(recipe, effect, duration, Math.max(0, maxStrength), potion, false);
        }
    }
}
