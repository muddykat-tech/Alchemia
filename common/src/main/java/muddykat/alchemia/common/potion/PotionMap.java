package muddykat.alchemia.common.potion;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.config.Configuration;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.helper.IngredientPath;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.items.helper.PathGeometry;
import muddykat.alchemia.common.items.helper.PathPoint;
import net.minecraft.world.item.ItemStack;
#if MC_VER >= 12100
import net.minecraft.core.Holder;
#endif
import muddykat.alchemia.common.potion.map.DeadSpace;
import muddykat.alchemia.common.potion.map.MapWall;
import net.minecraft.world.effect.MobEffect;

import java.util.*;

public class PotionMap {
    private static volatile Map<String, PotionMap> MAPS = Map.of();
    private static volatile long currentSeed;
    private static volatile boolean seeded;
    private static int builtGeneration = -1;
    private static int builtEffects = -1;
    private static long builtSeed;

    public static final double CELL = PathGeometry.CELL;
    public static final int HARD_MARGIN = (int) (3 * CELL);
    public static final int DEADZONE_RESOLUTION = DeadSpace.RESOLUTION;
    public static final int EFFECT_RINGS = 2;
    public static final double EFFECT_RADIUS = EFFECT_RINGS * CELL;
    public static final double EFFECT_CORE = CELL / 2.0;

    private static final double BASE_RADIUS_FRACTION = 15.0 / 68.0;
    private static final double SPACING_RELIEF = CELL;
    private static final double STALL_THRESHOLD = 1.0;
    private static final double WALL_CLEARANCE = EFFECT_RADIUS + 2 * CELL;
    private static final long WALL_SALT = 0x2545F4914F6CDD1DL;
    private static final long WHIRL_SALT = 0x6A09E667F3BCC909L;
    private static final long ISLAND_SALT = 0x3C6EF372FE94F82BL;
    private static final Map<String, Double> ISLANDS = Map.of("Returning", 1.0, "Recall", 0.7);

    private final BrewBase base;
    private final int size;
    private final int softSize;
    private final int middlePosition;
    private final int tiles;
    private final DeadSpace dead;
    private final MapWall wall;
    private final List<MapEntry> entries = new ArrayList<>();
    private List<Whirlpool> whirlpools = List.of();

    public PotionMap(long seed, BrewBase base) {
        Random rand = new Random(seed);
        this.base = base;

        softSize = (int) (AlchemiaEffects.count() * 2 * CELL);
        tiles = (softSize + HARD_MARGIN * 2 + DEADZONE_RESOLUTION - 1) / DEADZONE_RESOLUTION;
        size = tiles * DEADZONE_RESOLUTION;
        middlePosition = size / 2;
        dead = new DeadSpace(tiles, middlePosition, HARD_MARGIN, CELL, size * BASE_RADIUS_FRACTION);
        wall = new MapWall(middlePosition, WALL_CLEARANCE, CELL);

        double minDistance = Configuration.effectSpacing();

        for (AlchemiaEffect e : AlchemiaEffects.values()) {
            if (!base.allows(e)) continue;
#if MC_VER >= 12100
            Holder<MobEffect> effect = e.effect();
#else
            MobEffect effect = e.effect();
#endif
            int strength = e.maxAmplifier();
            boolean placed = false;
            double spacing = minDistance;
            int attempts = 0;

            while (!placed) {
                if (attempts >= 250) {
                    if (spacing <= SPACING_RELIEF) break;
                    spacing -= SPACING_RELIEF;
                    attempts = 0;
                }

                int biasX = e.alignmentX();
                int biasY = e.alignmentY();

                if (biasX == 0 && biasY == 0) {
                    biasX = rand.nextInt(3) - 1;
                    biasY = rand.nextInt(3) - 1;
                }

                int rarityOrdinal = e.rarity().ordinal();
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
                            new PotionEffectPosition(e, effect, e.durationTicks(), strength, true)));
                    placed = true;
                }

                attempts++;
            }

            if (!placed) placed = placeAnywhere(e, effect, strength);

            if (!placed) {
                Alchemia.LOGGER.warn("Could not place potion effect {}", e.id());
            }
        }

        List<PathPoint> effects = new ArrayList<>(entries.size());
        List<PathPoint> islands = new ArrayList<>();
        Random islandRoll = new Random(seed ^ ISLAND_SALT);
        for (MapEntry entry : entries) {
            effects.add(entry.position());
            double chance = ISLANDS.getOrDefault(entry.effect().getRecipe().id(), 0.0);
            if (chance > 0.0 && islandRoll.nextDouble() < chance) islands.add(entry.position());
        }

        dead.generate(seed, rand, deadSpaceSettings(), effects, islands, wall, this::teleport, CRYSTAL_VECTORS, IngredientPath.teleportRadius(1));
        int gated = dead.crystalGated();
        int stranded = unreachable(true, effects);
        wall.raise(new Random(seed ^ WALL_SALT), effects,
                () -> unreachable(false, effects) <= gated && unreachable(true, effects) <= stranded);
        whirlpools = Whirlpool.scatter(this, new Random(seed ^ WHIRL_SALT));
        Alchemia.LOGGER.info("Potion map built for {}: {}px across, {} effects, {} dead tiles, {} effects need crystals, {} whirlpools",
                base.id(), size, entries.size(), deadTileCount(), dead.crystalGated(), whirlpools.size());
    }

    private DeadSpace.Settings deadSpaceSettings() {
        return new DeadSpace.Settings(Configuration.noiseScale(), Configuration.minThreshold(), Configuration.thresholdSpread(),
                Configuration.maxThreshold(), Configuration.thresholdStep(), Configuration.maxCrystalGated(),
                base.deadSpace() == BrewBase.DeadSpace.FIELD, Configuration.ringCount(), Configuration.ringThickness(),
                Configuration.ringGates(), Configuration.ringRoughness(), Configuration.ringGateDegrees(),
                Configuration.effectMargin(), Configuration.centreMargin(), Configuration.fieldPatchScale(),
                Configuration.fieldCentreCalm());
    }

    private int unreachable(boolean withCrystals, List<PathPoint> effects) {
        return dead.unreachable(withCrystals, effects, wall, this::teleport, CRYSTAL_VECTORS, IngredientPath.teleportRadius(1));
    }

    public List<Whirlpool> whirlpools() {
        return whirlpools;
    }

    public Whirlpool whirlpoolAt(PathPoint at) {
        for (Whirlpool pool : whirlpools) {
            if (pool.touches(at)) return pool;
        }
        return null;
    }
    public boolean isDeadzone(double x, double y) {
        if (!inBounds(x, y)) return false;
        return sample(0, x, y) > 0.0;
    }

    public boolean isDeadzone(PathPoint point) {
        return isDeadzone(point.x(), point.y());
    }

    public boolean isDeadTile(int tileX, int tileY) {
        return dead.isDeadTile(tileX, tileY);
    }

    public boolean deadAt(int level, double x, double y) {
        return inBounds(x, y) && sample(level, x, y) > 0.0;
    }

    public double sample(int level, double x, double y) {
        return dead.sample(level, x, y);
    }

    public boolean inBounds(double x, double y) {
        return x >= 0 && x < size && y >= 0 && y < size && insideWall(x, y);
    }

    public boolean insideWall(double x, double y) {
        return wall.inside(x, y);
    }

    public boolean walled() {
        return wall.raised();
    }

    public double wallRadius(double angle) {
        return wall.radius(angle);
    }

    public double wallReach() {
        return wall.reach();
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
        return dead.deadTileCount();
    }

    public static int tileOf(double value) {
        return DeadSpace.tileOf(value);
    }

    public static double tileOrigin(int tile) {
        return DeadSpace.tileOrigin(tile);
    }

    public int crystalGatedEffects() {
        return dead.crystalGated();
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

    public PathPoint teleportLanding(double x, double y, ItemStack stack) {
        List<PathPoint> curve = teleportCurve(x, y, stack);
        return curve.isEmpty() ? new PathPoint(x, y) : curve.get(curve.size() - 1);
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

    public PathPoint positionOf(AlchemiaEffect recipe) {
        for (MapEntry entry : entries) {
            if (recipe != null && recipe.equals(entry.effect().getRecipe())) return new PathPoint(entry.x(), entry.y());
        }
        return null;
    }

    private PotionPath stalledAt(double x, double y) {
        return new PotionPath(List.of(new PathPoint(x, y)), false, true, false);
    }

    public List<PathPoint> fixedDriftOffsets(AlchemiaEffect target) {
        return fixedDriftOffsets(target, 0);
    }

    public List<PathPoint> fixedDriftOffsets(AlchemiaEffect target, int crushUnits) {
        PathPoint goal = positionOf(target);
        if (goal == null) return List.of();

        PathPoint bearing = goal.minus(new PathPoint(middlePosition, middlePosition)).normalised();
        if (bearing.isZero()) return List.of();

        return PathGeometry.resample(
                List.of(PathPoint.ORIGIN, bearing.scale(VanillaIngredients.DRIFT_LENGTH * CELL * VanillaIngredients.grindScale(crushUnits))),
                PathGeometry.SAMPLE_SPACING);
    }

    private PotionPath fixedPath(double x, double y, AlchemiaEffect target, int crushUnits) {
        return trail(x, y, fixedDriftOffsets(target, crushUnits));
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
        return project(x, y, offsets, true);
    }

    public PotionPath walkFrom(double x, double y, ItemStack stack, double skip) {
        List<PathPoint> offsets = walkingOffsets(stack);
        if (offsets == null) return walk(x, y, stack);

        List<PathPoint> line = new ArrayList<>(offsets.size() + 1);
        line.add(PathPoint.ORIGIN);
        line.addAll(offsets);
        List<PathPoint> rest = PathGeometry.tail(line, skip);
        if (rest.size() < 2) return stalledAt(x, y);

        PathPoint first = rest.get(0);
        List<PathPoint> shifted = new ArrayList<>(rest.size() - 1);
        for (int index = 1; index < rest.size(); index++) shifted.add(rest.get(index).minus(first));
        return trail(x, y, shifted);
    }

    private List<PathPoint> walkingOffsets(ItemStack stack) {
        VanillaIngredients.Drift drift = VanillaIngredients.driftOf(stack);
        if (drift != null) return drift.lure() ? null : fixedDriftOffsets(drift.target(), ItemIngredient.crushOf(stack));
        if (!(stack.getItem() instanceof ItemIngredient ingredient)) return null;

        int dx = Integer.signum(ingredient.getDriftX(stack));
        int dy = Integer.signum(ingredient.getDriftY(stack));
        IngredientPath shape = IngredientPath.of(ingredient.getIngredient());
        if (shape.teleports() || (shape.needsDirection() && dx == 0 && dy == 0)) return null;
        return IngredientPath.pathFor(ingredient.getIngredient(), dx, dy, ingredient.getPotency(stack),
                ItemIngredient.trimOf(stack), ItemIngredient.rotationOf(stack));
    }

    public PotionPath walk(double x, double y, ItemStack stack) {
        VanillaIngredients.Drift drift = VanillaIngredients.driftOf(stack);
        if (drift != null) return drift.lure() ? stalledAt(x, y) : fixedPath(x, y, drift.target(), ItemIngredient.crushOf(stack));

        if (!(stack.getItem() instanceof ItemIngredient ingredient)) return stalledAt(x, y);

        int dx = Integer.signum(ingredient.getDriftX(stack));
        int dy = Integer.signum(ingredient.getDriftY(stack));
        double potency = ingredient.getPotency(stack);
        IngredientPath shape = IngredientPath.of(ingredient.getIngredient());

        int[] aimed = IngredientPath.rotateDirection(dx, dy, ItemIngredient.rotationOf(stack));

        if (shape.teleports()) {
            PathPoint landing = !shape.followsFlight() && aimed[0] == 0 && aimed[1] == 0
                    ? new PathPoint(middlePosition, middlePosition)
                    : teleportLanding(x, y, stack);

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
    public static final double LURE_DRIFT = Math.toRadians(1.5) / CELL;
    public static final double LURE_GRAVITY = Math.toRadians(45.0);
    public static final double LURE_CORE = 1.5 * CELL;
    public static final double LURE_MAX_TURN = Math.PI;
    public static final double LURE_CAPTURE = EFFECT_CORE;
    private static final double SHORT_PULL_STEP = 1.0;
    private static final int FLIGHT_EASE_STEPS = 20;
    private static final int EDGE_SEARCH_STEPS = 12;

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
        return route(startX, startY, queue, anchors, List.of());
    }

    public List<RouteLeg> route(double startX, double startY, List<ItemStack> queue, List<Integer> anchors,
                                List<Double> anchorFrom) {
        return route(startX, startY, queue, anchors, anchorFrom, 0.0);
    }

    public List<RouteLeg> route(double startX, double startY, List<ItemStack> queue, List<Integer> anchors,
                                List<Double> anchorFrom, double headSkip) {
        List<Span> spans = new ArrayList<>(queue.size());
        double x = startX;
        double y = startY;

        for (ItemStack stack : queue) {
            VanillaIngredients.Drift drift = VanillaIngredients.driftOf(stack);

            if (drift != null && drift.lure()) {
                int anchor = spans.size() < anchors.size() ? anchors.get(spans.size()) : 0;
                double partial = spans.size() < anchorFrom.size() ? anchorFrom.get(spans.size()) : 0.0;
                boolean pulled = pull(spans, stack, drift.target(), anchor, partial);
                PathPoint here = pulled ? chainEnd(spans, startX, startY) : new PathPoint(x, y);

                spans.add(new Span(stack, true,
                        new PotionPath(List.of(here), false, !pulled, false), List.of()));
                x = here.x();
                y = here.y();
                continue;
            }

            PotionPath path = spans.isEmpty() && headSkip > 0.0 ? walkFrom(x, y, stack, headSkip) : walk(x, y, stack);
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

    private boolean pull(List<Span> spans, ItemStack stack, AlchemiaEffect target, int anchor, double partial) {
        PathPoint goal = positionOf(target);
        if (goal == null) return false;

        int from = 0;
        for (int index = spans.size() - 1; index >= 0; index--) {
            Span span = spans.get(index);
            if (span.dead && !span.teleport) {
                from = index + 1;
                break;
            }
        }
        from = Math.max(from, Math.min(anchor, spans.size()));

        double head = 0.0;
        if (partial > 0.0 && from == anchor && from < spans.size()) {
            Span first = spans.get(from);
            if (first.teleport || follows(first) || partial >= PathGeometry.length(first.points) - 1.0E-6) {
                from++;
            } else {
                head = partial;
            }
        }

        int stacked = 0;
        int bendable = 0;
        PathPoint at = null;
        for (int index = from; index < spans.size(); index++) {
            Span span = spans.get(index);
            if (span.lure) stacked++;
            if (follows(span)) continue;
            bendable += span.points.size();
            if (at == null) at = index == from && head > 0.0 ? PathGeometry.pointAt(span.points, head) : span.points.get(0);
        }
        if (bendable < 2) return false;

        Steer steer = new Steer(at, goal, lureTurn(stacked) * VanillaIngredients.grindScale(stack));
        boolean halted = false;

        for (int index = from; index < spans.size(); index++) {
            Span span = spans.get(index);

            if (follows(span) || halted) {
                span.points = List.of(steer.at);
                if (halted && !span.lure) {
                    span.stalled = true;
                    span.teleport = false;
                    span.curve = List.of();
                }
                continue;
            }

            if (span.teleport) {
                halted = !steerFlight(span, steer);
                continue;
            }

            boolean split = index == from && head > 0.0;
            List<PathPoint> source = split ? PathGeometry.tail(span.points, head) : span.points;
            List<PathPoint> walked = new ArrayList<>(span.points.size() + 1);
            if (split) {
                walked.addAll(PathGeometry.truncate(span.points, head));
            } else {
                walked.add(steer.at);
            }
            for (int step = 1; step < source.size() && !steer.captured; step++) {
                PathPoint next = steer.advance(source.get(step).minus(source.get(step - 1)));
                if (!landable(next)) {
                    PathPoint edge = pulledShort(walked.get(walked.size() - 1), next);
                    if (edge.distanceTo(walked.get(walked.size() - 1)) > 1.0E-6) walked.add(edge);
                    steer.at = walked.get(walked.size() - 1);
                    halted = true;
                    break;
                }
                walked.add(next);
            }

            span.points = walked;
            span.curve = List.of();
            span.stalled = walked.size() < 2;
            if (steer.captured) halted = true;
        }
        return true;
    }

    public static double lureTurn(int stacked) {
        return LureReach.stackScale(stacked);
    }

    private static final class Steer {
        PathPoint at;
        double offset;
        final PathPoint goal;
        final double turn;

        Steer(PathPoint at, PathPoint goal, double turn) {
            this.at = at;
            this.goal = goal;
            this.turn = turn;
        }

        boolean captured;

        Steer copy(double factor) {
            Steer twin = new Steer(at, goal, turn * factor);
            twin.offset = offset;
            return twin;
        }

        PathPoint advance(PathPoint segment) {
            double length = segment.length();
            if (length < 1.0E-9 || captured) return at;

            double heading = Math.atan2(segment.y(), segment.x()) + offset;
            PathPoint toGoal = goal.minus(at);
            if (toGoal.length() > 1.0E-6) {
                double wanted = Math.atan2(toGoal.y(), toGoal.x());
                double limit = turn * (LURE_DRIFT + LURE_GRAVITY / Math.max(toGoal.length(), LURE_CORE)) * length;
                double change = Math.max(-limit, Math.min(limit, Math.IEEEremainder(wanted - heading, 2 * Math.PI)));
                double turned = Math.max(-LURE_MAX_TURN, Math.min(LURE_MAX_TURN, offset + change));
                heading += turned - offset;
                offset = turned;
            }

            PathPoint from = at;
            at = new PathPoint(at.x() + Math.cos(heading) * length, at.y() + Math.sin(heading) * length);

            if (from.distanceTo(goal) > LURE_CAPTURE && passesWithin(from, at, goal, LURE_CAPTURE)) {
                at = goal;
                captured = true;
            }
            return at;
        }

        private static boolean passesWithin(PathPoint from, PathPoint to, PathPoint point, double radius) {
            PathPoint run = to.minus(from);
            double span = run.x() * run.x() + run.y() * run.y();
            if (span < 1.0E-12) return from.distanceTo(point) <= radius;

            PathPoint offset = point.minus(from);
            double t = Math.max(0.0, Math.min(1.0, (offset.x() * run.x() + offset.y() * run.y()) / span));
            return from.plus(run.scale(t)).distanceTo(point) <= radius;
        }
    }

    private boolean steerFlight(Span span, Steer steer) {
        PathPoint start = span.points.get(0);
        PathPoint landing = span.points.get(span.points.size() - 1);
        boolean curved = span.curve.size() > 1;
        List<PathPoint> flight = new ArrayList<>(curved ? span.curve : List.of(start, landing));
        if (flight.get(flight.size() - 1).distanceTo(landing) > 1.0E-3) flight.add(landing);

        for (int attempt = FLIGHT_EASE_STEPS; attempt >= -1; attempt--) {
            Steer trial = steer.copy(Math.max(0, attempt) / (double) FLIGHT_EASE_STEPS);
            if (attempt < 0) trial.offset = 0.0;

            PathPoint origin = trial.at;
            List<PathPoint> steered = new ArrayList<>(flight.size());
            steered.add(origin);
            for (int step = 1; step < flight.size() && !trial.captured; step++) {
                steered.add(trial.advance(flight.get(step).minus(flight.get(step - 1))));
            }

            PathPoint end = steered.get(steered.size() - 1);
            if (!inBounds(end)) continue;

            span.points = List.of(origin, end);
            span.curve = curved ? List.copyOf(steered) : List.of();
            span.stalled = false;
            span.dead = isDeadzone(end);
            steer.at = end;
            steer.offset = attempt < 0 ? steer.offset : trial.offset;
            steer.captured = trial.captured;
            return !trial.captured;
        }

        span.points = List.of(steer.at);
        span.curve = List.of();
        span.stalled = true;
        span.teleport = false;
        return false;
    }

    private boolean landable(PathPoint point) {
        return inBounds(point);
    }

    private PathPoint pulledShort(PathPoint from, PathPoint wanted) {
        PathPoint run = wanted.minus(from);
        double length = run.length();
        if (length <= SHORT_PULL_STEP) return from;

        PathPoint direction = run.normalised();
        for (double reach = length - SHORT_PULL_STEP; reach > 0.0; reach -= SHORT_PULL_STEP) {
            PathPoint candidate = from.plus(direction.scale(reach));
            if (landable(candidate)) return candidate;
        }
        return from;
    }

    private static boolean follows(Span span) {
        return span.lure || span.points.size() < 2;
    }

    public List<PathPoint> teleportFlight(double x, double y, ItemStack stack) {
        if (!(stack.getItem() instanceof ItemIngredient ingredient)) return List.of();
        if (!IngredientPath.of(ingredient.getIngredient()).followsFlight()) return List.of();
        return teleportCurve(x, y, stack);
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
            if (!inBounds(at)) {
                if (!curve.isEmpty()) curve.add(edgeBetween(curve.get(curve.size() - 1), at));
                break;
            }
            curve.add(at);
        }
        return curve;
    }

    private PathPoint edgeBetween(PathPoint inside, PathPoint outside) {
        PathPoint in = inside;
        PathPoint out = outside;
        for (int step = 0; step < EDGE_SEARCH_STEPS; step++) {
            PathPoint middle = new PathPoint((in.x() + out.x()) / 2.0, (in.y() + out.y()) / 2.0);
            if (inBounds(middle)) {
                in = middle;
            } else {
                out = middle;
            }
        }
        return in;
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

    public int lodLevels() {
        return dead.lodLevels();
    }

    public int tileCount(int level) {
        return dead.tileCount(level);
    }

    public static double tileSize(int level) {
        return DeadSpace.tileSize(level);
    }

    public static double tileOrigin(int level, int tile) {
        return tile * tileSize(level);
    }

    public boolean isDeadTile(int level, int tileX, int tileY) {
        return dead.isDeadTile(level, tileX, tileY);
    }

#if MC_VER >= 12100
    private boolean placeAnywhere(AlchemiaEffect recipe, Holder<MobEffect> effect, int strength) {
#else
    private boolean placeAnywhere(AlchemiaEffect recipe, MobEffect effect, int strength) {
#endif
        for (int x = HARD_MARGIN; x <= size - 1 - HARD_MARGIN; x += (int) CELL) {
            for (int y = HARD_MARGIN; y <= size - 1 - HARD_MARGIN; y += (int) CELL) {
                if (nearest(x, y, CELL) != null) continue;
                entries.add(new MapEntry(x, y,
                        new PotionEffectPosition(recipe, effect, recipe.durationTicks(), strength, true)));
                return true;
            }
        }
        return false;
    }

    public static synchronized void scramble(long seed) {
        if (seeded && seed == builtSeed && builtGeneration == BrewBases.generation()
                && builtEffects == AlchemiaEffects.generation()) return;
        currentSeed = seed;
        seeded = true;
        rebuild();
    }

    public static synchronized void rebuild() {
        if (!seeded) return;
        long seed = currentSeed;
        int generation = BrewBases.generation();
        int effects = AlchemiaEffects.generation();
        Map<String, PotionMap> built = new LinkedHashMap<>();
        for (BrewBase brewBase : BrewBases.values()) {
            built.put(brewBase.id(), new PotionMap(seedFor(seed, brewBase), brewBase));
        }
        MAPS = java.util.Collections.unmodifiableMap(built);
        builtSeed = seed;
        builtGeneration = generation;
        builtEffects = effects;
    }

    public static long seedFor(long seed, BrewBase base) {
        long mixed = seed ^ (base.id().hashCode() * 0x9E3779B97F4A7C15L);
        mixed ^= mixed >>> 33;
        mixed *= 0xFF51AFD7ED558CCDL;
        mixed ^= mixed >>> 33;
        return mixed;
    }

    public static PotionMap get(BrewBase base) {
        Map<String, PotionMap> maps = MAPS;
        PotionMap map = maps.get(base.id());
        return map != null ? map : maps.get(BrewBases.defaultBase().id());
    }

    public static boolean isReady() {
        return seeded && !MAPS.isEmpty();
    }

    public BrewBase base() {
        return base;
    }

    public PotionEffectPosition getEffectPotion(double x, double y) {
        PotionEffectPosition defaultEffect = new PotionEffectPosition(null, null, 10, 0, false);

        MapEntry closest = nearest(x, y, EFFECT_RADIUS);
        if (closest == null) return defaultEffect;

        double distance = Math.sqrt(Math.pow(closest.x() - x, 2) + Math.pow(closest.y() - y, 2));
        if (distance <= EFFECT_CORE) return closest.effect();

        int ring = Math.max(1, (int) Math.ceil(distance / CELL));
        int falloff = Math.max(0, closest.effect().getStrength() - ring);
        return new PotionEffectPosition(closest.effect().getRecipe(), closest.effect().getEffect(),
                closest.effect().getDuration(), falloff, false);
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
        final AlchemiaEffect recipe;
#if MC_VER >= 12100
        final Holder<MobEffect> effect;
#else
        final MobEffect effect;
#endif
        final int duration;
        final int strength;
        final boolean perfect;
#if MC_VER >= 12100
        PotionEffectPosition(AlchemiaEffect recipe, Holder<MobEffect> effect, int duration, int maxStrength, boolean isPerfect) {
#else
        PotionEffectPosition(AlchemiaEffect recipe, MobEffect effect, int duration, int maxStrength, boolean isPerfect) {
#endif
            this.recipe = recipe;
            this.effect = effect;
            this.duration = duration;
            this.strength = maxStrength;
            this.perfect = isPerfect;
        }

        public AlchemiaEffect getRecipe() {
            return recipe;
        }

#if MC_VER >= 12100
        public Holder<MobEffect> getEffect() {
#else
        public MobEffect getEffect() {
#endif
            return effect;
        }

        public int getDuration() {
            return duration;
        }

        public int getStrength() {
            return strength;
        }

        public boolean isPerfect() {
            return perfect;
        }

        public PotionEffectPosition capped(int maxStrength) {
            if (strength <= maxStrength) return this;
            return new PotionEffectPosition(recipe, effect, duration, Math.max(0, maxStrength), false);
        }
    }
}
