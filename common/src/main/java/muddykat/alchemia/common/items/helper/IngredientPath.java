package muddykat.alchemia.common.items.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleFunction;

public enum IngredientPath {
    STRAIGHT(0.0),
    WAVE(PathGeometry.CELL),
    ARC(2.0 * PathGeometry.CELL),
    SPIRAL(0.0),
    SCRIPTED(0.0),
    CURVED(0.0),
    TELEPORT(0.0),
    SPIRAL_TELEPORT(0.0),
    SERPENT_TELEPORT(0.0),
    SAWTOOTH_TELEPORT(0.0),
    STEPPED_TELEPORT(0.0),
    RIPPLE_TELEPORT(0.0);

    public static final double BASE_LENGTH = 5.0 * PathGeometry.CELL;
    public static final double LENGTH_PER_POTENCY = 2.0 * PathGeometry.CELL;
    public static final double TRIM_STEP = PathGeometry.CELL;

    private static final Map<Ingredients, IngredientPath> OVERRIDES = Map.of(
            Ingredients.Rainbow_Cap, SPIRAL,
            Ingredients.Fable_Bismuth, SPIRAL_TELEPORT,
            Ingredients.Cloud_Crystal, SERPENT_TELEPORT,
            Ingredients.Frost_Sapphire, SAWTOOTH_TELEPORT,
            Ingredients.Earth_Pyrite, STEPPED_TELEPORT,
            Ingredients.Fire_Citrine, RIPPLE_TELEPORT
    );

    private final double amplitude;

    IngredientPath(double amplitude) {
        this.amplitude = amplitude;
    }

    private static final Map<Ingredients, int[][]> SCRIPTS = Map.of(
            Ingredients.Featherbloom, new int[][]{{0, -3}, {1, -5}, {1, -8}, {0, -10}, {0, -11}}
    );

    private record Curve(List<PathPoint> points, double baseLength) {
    }

    private static List<PathPoint> arc(DoubleFunction<PathPoint> curve, double from, double to, int samples) {
        return PathGeometry.scale(PathGeometry.sample(curve, from, to, samples), PathGeometry.CELL);
    }

    private static DoubleFunction<PathPoint> ellipse(double centreX, double centreY, double width, double height) {
        return t -> new PathPoint(centreX + width * Math.cos(t), centreY + height * Math.sin(t));
    }

    private static List<PathPoint> steps(double fromX, double fromY, double dx, double dy, int count) {
        List<PathPoint> waypoints = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            waypoints.add(new PathPoint((fromX + dx * i) * PathGeometry.CELL, (fromY + dy * i) * PathGeometry.CELL));
        }
        return waypoints;
    }

    private static List<PathPoint> corners(double[]... waypoints) {
        List<PathPoint> points = new ArrayList<>();
        for (double[] waypoint : waypoints) {
            points.add(new PathPoint(waypoint[0] * PathGeometry.CELL, waypoint[1] * PathGeometry.CELL));
        }
        return points;
    }

    @SafeVarargs
    private static Curve curve(List<PathPoint> base, List<PathPoint>... growth) {
        List<PathPoint> waypoints = new ArrayList<>();
        waypoints.add(PathPoint.ORIGIN);
        waypoints.addAll(base);

        double baseLength = PathGeometry.length(PathGeometry.spline(waypoints));

        for (List<PathPoint> segment : growth) waypoints.addAll(segment);
        return new Curve(List.copyOf(PathGeometry.spline(waypoints)), baseLength);
    }

    private static void append(List<PathPoint> points, List<PathPoint> leg) {
        for (PathPoint point : PathGeometry.resample(leg, PathGeometry.SAMPLE_SPACING)) {
            if (points.isEmpty() || points.get(points.size() - 1).distanceTo(point) > 1.0E-6) points.add(point);
        }
    }

    @SafeVarargs
    private static List<PathPoint> joined(List<PathPoint>... legs) {
        List<PathPoint> points = new ArrayList<>();
        for (List<PathPoint> leg : legs) append(points, leg);
        return points;
    }

    private static List<PathPoint> straights(List<PathPoint> waypoints) {
        List<PathPoint> points = new ArrayList<>();
        for (int i = 1; i < waypoints.size(); i++) append(points, List.of(waypoints.get(i - 1), waypoints.get(i)));
        return points;
    }

    private static Curve marked(List<PathPoint> points, PathPoint mark) {
        return new Curve(List.copyOf(points), PathGeometry.distanceAlong(points, mark));
    }

    private static Curve routed(double[][] route, int mark) {
        List<PathPoint> waypoints = corners(route);
        return marked(PathGeometry.spline(waypoints), waypoints.get(mark));
    }

    private static Curve angled(double[][] route, int mark) {
        List<PathPoint> waypoints = corners(route);
        return marked(straights(waypoints), waypoints.get(mark));
    }

    private static final int CURVE_SAMPLES_PER_TURN = 120;

    private static final double GOLDTHORN_DROP = 5.0;

    private static DoubleFunction<PathPoint> goldthornCoil() {
        return t -> {
            double radius = GOLDTHORN_DROP * (1 - t / (4 * Math.PI));
            return new PathPoint(-radius * Math.sin(t), GOLDTHORN_DROP - radius * Math.cos(t));
        };
    }

    private static Curve goldthornSpiral() {
        return curve(
                arc(goldthornCoil(), 0, Math.PI / 2, CURVE_SAMPLES_PER_TURN / 4),
                arc(goldthornCoil(), Math.PI / 2, 4 * Math.PI, CURVE_SAMPLES_PER_TURN * 7 / 4));
    }

    private static final int BERRY_LEAD = 3;
    private static final int BERRY_TAIL = 5;
    private static final double BERRY_LOOP_RADIUS = 3.0;

    private static Curve goodberryDiagonal() {
        double centreX = BERRY_LEAD + BERRY_LOOP_RADIUS * Math.cos(-Math.PI / 4);
        double centreY = BERRY_LEAD + BERRY_LOOP_RADIUS * Math.sin(-Math.PI / 4);
        double entry = Math.atan2(BERRY_LEAD - centreY, BERRY_LEAD - centreX);

        return curve(
                steps(0, 0, 1, 1, BERRY_LEAD),
                arc(ellipse(centreX, centreY, BERRY_LOOP_RADIUS, BERRY_LOOP_RADIUS),
                        entry, entry - 2 * Math.PI, CURVE_SAMPLES_PER_TURN),
                steps(BERRY_LEAD, BERRY_LEAD, 1, 1, BERRY_TAIL));
    }

    private static final double[][] ICEFRUIT_ROUTE = {
            {0, 0}, {4.0 / 3, 3}, {8.0 / 3, 0}, {4, -3}, {20.0 / 3, 3}, {8, 0}
    };
    private static final int ICEFRUIT_MARK = 2;

    private static Curve icefruitSpike() {
        List<PathPoint> waypoints = corners(ICEFRUIT_ROUTE);
        return marked(straights(waypoints), waypoints.get(ICEFRUIT_MARK));
    }

    private static final double PEPPER_WIDTH = 6.0;
    private static final double PEPPER_HEIGHT = 3.0;

    private static Curve dragonPepperOval() {
        DoubleFunction<PathPoint> oval = ellipse(-PEPPER_WIDTH, 0, PEPPER_WIDTH, PEPPER_HEIGHT);
        return curve(
                arc(oval, 0, Math.PI / 2, CURVE_SAMPLES_PER_TURN / 4),
                arc(oval, Math.PI / 2, 1.5 * Math.PI, CURVE_SAMPLES_PER_TURN / 2));
    }

    private static final double BLOODTHORN_RADIUS = 10.0;
    private static final double BLOODTHORN_BASE_SWEEP = 1.0;
    private static final double BLOODTHORN_RECOIL_RADIUS = 6.5;
    private static final double BLOODTHORN_RECOIL_SWEEP = Math.toRadians(75);

    private static Curve bloodthornSweep() {
        DoubleFunction<PathPoint> quarter = ellipse(-BLOODTHORN_RADIUS, 0, BLOODTHORN_RADIUS, BLOODTHORN_RADIUS);
        DoubleFunction<PathPoint> recoil = ellipse(-BLOODTHORN_RADIUS, BLOODTHORN_RECOIL_RADIUS - BLOODTHORN_RADIUS,
                BLOODTHORN_RECOIL_RADIUS, BLOODTHORN_RECOIL_RADIUS);

        List<PathPoint> base = arc(quarter, 0, -BLOODTHORN_BASE_SWEEP, CURVE_SAMPLES_PER_TURN / 6);
        return marked(joined(base,
                arc(quarter, -BLOODTHORN_BASE_SWEEP, -Math.PI / 2, CURVE_SAMPLES_PER_TURN / 6),
                arc(recoil, -Math.PI / 2, BLOODTHORN_RECOIL_SWEEP - Math.PI / 2, CURVE_SAMPLES_PER_TURN / 6)),
                base.get(base.size() - 1));
    }

    private static final double[][] WINDBLOOM_ROUTE = {
            {0, 0}, {1, -1.5}, {1.5, -3}, {1.6, -4.3}, {1, -5.2}, {0, -5.5},
            {-1, -5.8}, {-1.6, -6.7}, {-1.5, -8}, {-1, -9.5}, {0, -11}
    };
    private static final int WINDBLOOM_MARK = 5;

    private static final double[][] TERRARIA_ROUTE = {
            {0, 0}, {0, 1}, {0, 2}, {1, 2.6}, {1.9, 3.5}, {1.9, 4.9}, {1.1, 5.5}, {0, 5.5},
            {-1.1, 5.5}, {-1.9, 6.1}, {-2, 7.3}, {-1.5, 8.4}, {-0.3, 8.8}, {0.1, 9.8}, {0, 11}
    };
    private static final int TERRARIA_MARK = 7;

    private static final double[][] LAVA_ROOT_ROUTE = {
            {0, 0}, {-2.2, 2}, {-4.4, 0}, {-6.6, -2}, {-8.8, 0}, {-11, 2}, {-13.2, 0},
            {-14.4, -2}, {-15, -3.2}, {-14.2, -3.3}, {-12.6, -2.4}, {-11, -1.7}
    };
    private static final int LAVA_ROOT_MARK = 2;

    private static final double MAGEBERRY_REACH = 6.25;
    private static final double MAGEBERRY_GIRTH = 2.0;
    private static final double MAGEBERRY_TILT = Math.atan2(-3, 4);

    private static DoubleFunction<PathPoint> mageberryOrbit() {
        return t -> new PathPoint(MAGEBERRY_REACH * (1 - Math.cos(t)), MAGEBERRY_GIRTH * Math.sin(t))
                .rotate(MAGEBERRY_TILT);
    }

    private static Curve mageberryLoop() {
        List<PathPoint> base = arc(mageberryOrbit(), 0, Math.PI / 2, CURVE_SAMPLES_PER_TURN / 4);
        return marked(joined(base,
                arc(mageberryOrbit(), Math.PI / 2, 2 * Math.PI, CURVE_SAMPLES_PER_TURN * 3 / 4)),
                base.get(base.size() - 1));
    }

    private static final double[][] WATERBLOOM_ROUTE = {
            {0, 0}, {0.9, 0.4}, {2.1, -0.3}, {3.2, -1.3}, {5.5, -0.6}, {7.4, 0.5}, {8.6, -0.5}, {9.7, -1.5},
            {10.9, -1.5}, {11.7, -0.7}
    };
    private static final int WATERBLOOM_MARK = 4;

    private static final double[][] TANGLEWEED_ROUTE = {
            {0, 0}, {1.4, 0.5}, {2.4, 0.5}, {3.2, 0.4}, {3.7, 0.1}, {4.3, -0.6}, {4.3, -1.7}, {3.6, -2.3},
            {2.6, -2.1}, {2.1, -1.3}, {2.2, -0.2}, {2.6, 0.5}, {3.1, 0.9}, {3.9, 1}, {5, 0.7}, {5.8, 0.1}, {6.5, -1},
            {7.2, -2}, {8, -2.6}, {9.2, -2.8}, {10.3, -2.5}, {10.9, -1.4}, {10.8, -0.2}, {9.9, 0.5}, {8.9, 0.3},
            {8.4, -0.8}, {8.8, -1.8}, {9.9, -2.3}, {11.4, -2.4}, {12.4, -2.1}, {13.1, -1.6}
    };
    private static final int TANGLEWEED_MARK = 16;

    private static final double[][] COLDLEAF_ROUTE = {
            {0, 0}, {0.6, 0.9}, {1.4, 1.6}, {3.1, 2.5}, {5.5, 3.2}, {7.8, 3.3}, {10.1, 3.2}, {12.3, 2.6},
            {13.5, 1.6}, {14.2, 0.9}
    };
    private static final int COLDLEAF_MARK = 3;

    private static final double[][] SPELLBLOOM_ROUTE = {
            {0, 0}, {1.4, -0.6}, {3, -2.3}, {4.2, -4.2}, {5, -6}, {5.9, -7.9}, {7, -9.4}, {8.2, -10.5}, {9.7, -11.1},
            {10.4, -11.3}
    };
    private static final int SPELLBLOOM_MARK = 2;

    private static final double[][] LIFELEAF_ROUTE = {
            {0, 0}, {1.3, 0.1}, {2.5, 0.5}, {3.4, 1.4}, {3.7, 2.7}, {3.2, 4.1}, {2.8, 5.4}, {2.8, 6.6}, {3.4, 7.6},
            {4.5, 8.2}, {5.8, 8.3}, {6.6, 8.1}
    };
    private static final int LIFELEAF_MARK = 5;

    private static final double[][] HEALERS_HEATHER_ROUTE = {
            {0, 0}, {1.3, 0.5}, {2.7, 1.3}, {4.5, 2.8}, {6.5, 4.7}, {7.8, 7}, {8.8, 9.4}, {9.2, 11.3}, {9.3, 12}
    };
    private static final int HEALERS_HEATHER_MARK = 2;

    private static final double[][] EVERGREEN_FERN_ROUTE = {
            {0, 0}, {0.9, -0.2}, {2.1, -0.1}, {3.2, 0.2}, {3.8, 0.4}, {4.9, 1.1}, {5.8, 1.8}, {6.5, 2.9}, {6.8, 3.9},
            {6.7, 5}, {6.3, 5.9}, {5.6, 6.7}, {4.7, 7.3}, {3.5, 7.4}, {2.4, 7.2}, {1.5, 6.6}, {1, 5.8}, {0.9, 4.7},
            {1.1, 3.7}, {1.7, 3}, {2.5, 2.6}, {3.5, 2.6}, {4.4, 3}, {4.7, 3.8}, {4.6, 4.7}, {4, 5.2}, {3.3, 5.2},
            {2.9, 4.8}, {3, 4.3}, {3.2, 4}
    };
    private static final int EVERGREEN_FERN_MARK = 3;

    private static final double[][] THUNDER_THISTLE_ROUTE = {
            {0, 0}, {4.8, -3.9}, {-0.3, -3.9}, {4.7, -8.2}
    };
    private static final int THUNDER_THISTLE_MARK = 1;

    private static final double[][] FLUFFBLOOM_ROUTE = {
            {0, 0}, {-1.2, -0.3}, {-2.2, -1.1}, {-2.9, -2.3}, {-3.1, -3.8}, {-2.9, -4.9}, {-2, -6}, {-1.5, -6.5},
            {-0.6, -7.1}, {0.3, -7.3}, {1.6, -7.2}, {2.9, -6.7}, {3.8, -5.7}, {4.2, -4.3}, {3.9, -3}, {3.2, -2.1},
            {2, -1.6}, {0.9, -1.4}, {-0.3, -1.8}, {-1.1, -2.5}, {-1.6, -3.5}, {-1.5, -4.7}, {-1.3, -5.9},
            {-0.8, -7.1}, {-0.6, -8.5}, {-0.5, -9.8}, {-0.4, -10.7}
    };
    private static final int FLUFFBLOOM_MARK = 6;

    private static final double[][] TERRORBUD_ROUTE = {
            {0, 0}, {-0.3, 1}, {-1, 1.8}, {-2.2, 2.5}, {-3.6, 3}, {-4.6, 2.5}, {-5.4, 1.6}, {-6, 0.3}, {-6.2, -1.4},
            {-6.1, -3}, {-5.8, -4.6}, {-5.5, -5.7}, {-5.1, -6}, {-4.7, -5.6}, {-4.2, -4.6}, {-3.8, -3.8}
    };
    private static final int TERRORBUD_MARK = 4;

    private static final double[][] FIREBELL_ROUTE = {
            {0, 0}, {-2.6, -2.1}, {-6.3, 0.7}, {-9.8, -2.1}, {-12.6, 0.2}
    };
    private static final int FIREBELL_MARK = 2;

    private static final double[][] FLAMEWEED_ROUTE = {
            {0, 0}, {-0.8, 0.3}, {-1.8, 0.5}, {-3.3, -0.1}, {-4.2, -0.8}, {-5.3, -0.2}, {-6.4, 0.5}, {-7.3, -0.1},
            {-8.2, -0.8}, {-9.4, -0.1}, {-10.5, 0.7}, {-11.4, 0}, {-12.3, -0.8}, {-13.2, -0.3}, {-14.2, 0.5},
            {-15.1, 0.1}
    };
    private static final int FLAMEWEED_MARK = 3;

    private static final double[][] THORNSTICK_ROUTE = {
            {0, 0}, {-3, 2.2}, {-9.8, 7.4}, {-7.7, 4.2}
    };
    private static final int THORNSTICK_MARK = 1;

    private static final double[][] SULPHUR_SHELF_ROUTE = {
            {0, 0}, {0.4, 0.5}, {-0.5, 1.1}, {-1.5, 1.2}, {-2.4, 1.2}, {-3.1, 0.7}, {-2.9, -0.1}, {-2.2, -0.7},
            {-1.8, -1.5}, {-2.7, -2}, {-3.6, -2.1}, {-4.5, -2}, {-5.4, -1.6}, {-5.3, -0.8}, {-4.5, -0.1},
            {-4.1, 0.5}, {-4.8, 1.1}, {-6, 1.2}, {-6.7, 1.1}, {-7.6, 0.8}, {-7.6, -0.1}, {-6.9, -0.7}, {-6.3, -1.5},
            {-7, -2}, {-7.9, -2.1}, {-9, -2.1}, {-9.8, -1.7}, {-9.9, -0.9}, {-9.1, -0.2}, {-8.6, 0.5}, {-9.2, 1},
            {-10.2, 1.2}, {-11.1, 1.2}, {-12, 0.9}, {-12.1, 0.1}, {-11.8, -0.3}
    };
    private static final int SULPHUR_SHELF_MARK = 17;

    private static final double[][] WITCH_MUSHROOM_ROUTE = {
            {0, 0}, {0.2, -1.2}, {0.8, -2.5}, {1.8, -3.5}, {3.1, -4.1}, {4.3, -4.5}, {5.3, -4.7}, {7.1, -5.1},
            {8.3, -5.5}, {9.4, -6.3}, {10.2, -7.6}, {10.6, -8.9}, {10.7, -9.7}
    };
    private static final int WITCH_MUSHROOM_MARK = 6;

    private static final double[][] MAGMA_MOREL_ROUTE = {
            {0, 0}, {-0.8, -0.6}, {-2.8, -1.2}, {-4.4, -1.4}, {-6.1, -1.4}, {-8.2, -1.1}, {-10.4, -0.5},
            {-12.5, 0.3}, {-14.1, 1}, {-15.1, 1.7}
    };
    private static final int MAGMA_MOREL_MARK = 2;

    private static final double[][] DREAMBEET_ROUTE = {
            {0, 0}, {0.2, 0.8}, {0.8, 1.6}, {1.8, 2.1}, {3, 2.3}, {4.3, 2}, {5.1, 1.2}, {5.7, 0.2}, {6, -0.9},
            {5.8, -2.1}, {5.3, -3.1}, {4.3, -3.7}, {3.2, -3.8}, {3.2, -4.7}, {3.4, -5.6}, {3.9, -6.4}, {4.6, -7.1},
            {5.4, -7.6}, {6.4, -7.8}, {7.1, -8}
    };
    private static final int DREAMBEET_MARK = 12;

    private static final double[][] GRASPING_ROOT_ROUTE = {
            {0, 0}, {-0.3, -0.9}, {-1.2, -1.9}, {-2.5, -2.5}, {-3.6, -2.6}, {-4.6, -2.7}, {-6.0, -2.7}, {-6.8, -2.9},
            {-7.7, -3.3}, {-8.3, -3.9}, {-8.8, -4.8}, {-8.9, -5.8}, {-8.7, -6.8}, {-8.2, -7.6}, {-7.3, -8.2},
            {-6.3, -8.4}, {-5.3, -8.2}, {-4.5, -7.5}, {-4.2, -6.6}, {-4.4, -5.7}
    };
    private static final int GRASPING_ROOT_MARK = 6;

    private static final double[][] WHIRLWEED_ROUTE = {
            {0, 0}, {0.4, -0.2}, {0.8, -0.6}, {1, -1.1}, {0.7, -1.9}, {0, -2.4}, {-1, -2.7}, {-1.8, -3.5},
            {-1.9, -4.5}, {-1.7, -5.4}, {-1.2, -6}, {-0.7, -6.2}, {-0.2, -6.2}, {0.3, -6}, {0.7, -5.6}, {1, -5.1},
            {1, -4.6}, {0.6, -4.1}, {0.2, -3.8}, {-0.3, -3.7}, {-0.8, -3.8}, {-1.3, -4.2}, {-1.5, -4.8},
            {-1.3, -5.6}, {-1.1, -6.2}, {-0.9, -6.7}, {-0.6, -7.2}, {-0.3, -7.7}, {0.1, -8.1}, {0.3, -8.6},
            {0.2, -9.1}, {-0.1, -9.3}
    };
    private static final int WHIRLWEED_MARK = 4;

    private static final double[][] MARSHROOM_ROUTE = {
            {0, 0}, {0.7, 0.2}, {1.2, 1.0}, {1.1, 2.0}, {0.7, 2.9}, {0.1, 3.8}, {-0.6, 4.5}, {-1.4, 5.3}, {-2.6, 6.5},
            {-4.3, 7.7}, {-6.1, 8.6}, {-7.1, 8.7}, {-8.0, 8.3}, {-8.4, 7.7}, {-8.5, 7.1}
    };
    private static final int MARSHROOM_MARK = 6;

    private static final double[][] BOOMBLOOM_RISE = {
            {0, 0}, {-1.1, -4.4}
    };

    private static final double[][] BOOMBLOOM_CURL = {
            {-1.1, -4.4}, {-1.1, -3.3}, {-1.2, -2.4}, {-1.6, -1.6}, {-2.2, -1.2}, {-3, -1}, {-4.3, -1}
    };

    private static final double[][] BOOMBLOOM_LAUNCH = {
            {-4.3, -1}, {0, 0}, {-9.5, -9.3}
    };

    private static Curve boombloomFuse() {
        List<PathPoint> rise = corners(BOOMBLOOM_RISE);
        return marked(joined(straights(rise), PathGeometry.spline(corners(BOOMBLOOM_CURL)),
                straights(corners(BOOMBLOOM_LAUNCH))), rise.get(rise.size() - 1));
    }

    private static final Map<Ingredients, Curve> CURVES = Map.ofEntries(
            Map.entry(Ingredients.Waterbloom, routed(WATERBLOOM_ROUTE, WATERBLOOM_MARK)),
            Map.entry(Ingredients.Tangleweed, routed(TANGLEWEED_ROUTE, TANGLEWEED_MARK)),
            Map.entry(Ingredients.Coldleaf, routed(COLDLEAF_ROUTE, COLDLEAF_MARK)),
            Map.entry(Ingredients.Spellbloom, routed(SPELLBLOOM_ROUTE, SPELLBLOOM_MARK)),
            Map.entry(Ingredients.Lifeleaf, routed(LIFELEAF_ROUTE, LIFELEAF_MARK)),
            Map.entry(Ingredients.Healers_Heather, routed(HEALERS_HEATHER_ROUTE, HEALERS_HEATHER_MARK)),
            Map.entry(Ingredients.Evergreen_Fern, routed(EVERGREEN_FERN_ROUTE, EVERGREEN_FERN_MARK)),
            Map.entry(Ingredients.Thunder_Thistle, angled(THUNDER_THISTLE_ROUTE, THUNDER_THISTLE_MARK)),
            Map.entry(Ingredients.Fluffbloom, routed(FLUFFBLOOM_ROUTE, FLUFFBLOOM_MARK)),
            Map.entry(Ingredients.Terrorbud, routed(TERRORBUD_ROUTE, TERRORBUD_MARK)),
            Map.entry(Ingredients.Boombloom, boombloomFuse()),
            Map.entry(Ingredients.Sulphur_Shelf, routed(SULPHUR_SHELF_ROUTE, SULPHUR_SHELF_MARK)),
            Map.entry(Ingredients.Witch_Mushroom, routed(WITCH_MUSHROOM_ROUTE, WITCH_MUSHROOM_MARK)),
            Map.entry(Ingredients.Magma_Morel, routed(MAGMA_MOREL_ROUTE, MAGMA_MOREL_MARK)),
            Map.entry(Ingredients.Dreambeet, routed(DREAMBEET_ROUTE, DREAMBEET_MARK)),
            Map.entry(Ingredients.Grasping_Root, routed(GRASPING_ROOT_ROUTE, GRASPING_ROOT_MARK)),
            Map.entry(Ingredients.Marshroom, routed(MARSHROOM_ROUTE, MARSHROOM_MARK)),
            Map.entry(Ingredients.Whirlweed, routed(WHIRLWEED_ROUTE, WHIRLWEED_MARK)),
            Map.entry(Ingredients.Goldthorn, goldthornSpiral()),
            Map.entry(Ingredients.Goodberry, goodberryDiagonal()),
            Map.entry(Ingredients.Icefruit, icefruitSpike()),
            Map.entry(Ingredients.Thornstick, angled(THORNSTICK_ROUTE, THORNSTICK_MARK)),
            Map.entry(Ingredients.Firebell, angled(FIREBELL_ROUTE, FIREBELL_MARK)),
            Map.entry(Ingredients.Flameweed, routed(FLAMEWEED_ROUTE, FLAMEWEED_MARK)),
            Map.entry(Ingredients.Dragon_Pepper, dragonPepperOval()),
            Map.entry(Ingredients.Bloodthorn, bloodthornSweep()),
            Map.entry(Ingredients.Windbloom, routed(WINDBLOOM_ROUTE, WINDBLOOM_MARK)),
            Map.entry(Ingredients.Terraria, routed(TERRARIA_ROUTE, TERRARIA_MARK)),
            Map.entry(Ingredients.Lava_Root, routed(LAVA_ROOT_ROUTE, LAVA_ROOT_MARK)),
            Map.entry(Ingredients.Mageberry, mageberryLoop())
    );

    private static List<PathPoint> curvePath(Ingredients ingredient, double potency) {
        Curve curve = CURVES.get(ingredient);
        List<PathPoint> points = curve.points();
        if (points.size() < 2) return points;

        double progress = Math.min(1.0, Math.max(0.0, (potency - 1) / Ingredients.MAX_CRUSH));
        double total = PathGeometry.length(points);
        double base = Math.min(total, Math.max(PathGeometry.SAMPLE_SPACING, curve.baseLength()));
        return PathGeometry.truncate(points, base + (total - base) * progress);
    }

    public static IngredientPath of(Ingredients ingredient) {
        if (CURVES.containsKey(ingredient)) return CURVED;
        if (SCRIPTS.containsKey(ingredient)) return SCRIPTED;

        IngredientPath override = OVERRIDES.get(ingredient);
        if (override != null) return override;

        return switch (ingredient.getType()) {
            case Mineral -> TELEPORT;
            case Herb, Mushroom -> WAVE;
            case Flower -> ARC;
            case Root -> STRAIGHT;
        };
    }

    public boolean teleports() {
        return this == TELEPORT || followsFlight();
    }

    public boolean followsFlight() {
        return this == SPIRAL_TELEPORT || this == SERPENT_TELEPORT || markedFlight() != null;
    }

    public boolean needsDirection() {
        return this != SPIRAL && this != SPIRAL_TELEPORT && this != SCRIPTED && this != CURVED;
    }

    private static List<PathPoint> scriptPath(Ingredients ingredient, int dx, int dy, double potency) {
        int[][] script = SCRIPTS.get(ingredient);

        List<PathPoint> waypoints = new ArrayList<>();
        waypoints.add(PathPoint.ORIGIN);
        for (int[] step : script) {
            waypoints.add(new PathPoint(step[0] * PathGeometry.CELL, step[1] * PathGeometry.CELL));
        }

        List<PathPoint> path = PathGeometry.spline(waypoints);
        double extra = Math.max(0.0, potency - 1) * LENGTH_PER_POTENCY;
        if (extra <= 0.0) return path;

        PathPoint bearing = new PathPoint(dx, dy);
        return extend(path, bearing.isZero() ? tangent(path) : bearing.normalised(), extra);
    }

    private static PathPoint tangent(List<PathPoint> path) {
        if (path.size() < 2) return new PathPoint(0.0, -1.0);

        PathPoint last = path.get(path.size() - 1);
        for (int i = path.size() - 2; i >= 0; i--) {
            PathPoint bearing = last.minus(path.get(i));
            if (!bearing.isZero()) return bearing.normalised();
        }
        return new PathPoint(0.0, -1.0);
    }

    private static List<PathPoint> extend(List<PathPoint> path, PathPoint bearing, double distance) {
        if (path.isEmpty() || distance <= 0.0) return path;

        PathPoint last = path.get(path.size() - 1);
        List<PathPoint> grown = new ArrayList<>(path);

        int steps = Math.max(1, (int) Math.ceil(distance / PathGeometry.SAMPLE_SPACING));
        for (int step = 1; step <= steps; step++) {
            grown.add(last.plus(bearing.scale(distance * step / steps)));
        }
        return grown;
    }

    public static List<PathPoint> pathFor(Ingredients ingredient, int dx, int dy, double potency) {
        IngredientPath shape = of(ingredient);
        if (shape == CURVED) return curvePath(ingredient, potency);
        if (shape == SCRIPTED) return scriptPath(ingredient, dx, dy, potency);
        return shape.offsets(dx, dy, lengthFor(potency), potency);
    }

    public static List<PathPoint> pathFor(Ingredients ingredient, int dx, int dy, double potency, int trim) {
        return trimmed(pathFor(ingredient, dx, dy, potency), trim);
    }

    public static List<PathPoint> pathFor(Ingredients ingredient, int dx, int dy, double potency, int trim, int rotation) {
        return rotated(pathFor(ingredient, dx, dy, potency, trim), rotation);
    }

    public static final int ROTATION_STEPS = 8;

    public static double rotationRadians(int steps) {
        return Math.floorMod(steps, ROTATION_STEPS) * (2 * Math.PI / ROTATION_STEPS);
    }

    public static List<PathPoint> rotated(List<PathPoint> path, int steps) {
        if (Math.floorMod(steps, ROTATION_STEPS) == 0 || path.isEmpty()) return path;
        return PathGeometry.rotate(path, rotationRadians(steps));
    }

    public static int[] rotateDirection(int dx, int dy, int steps) {
        if (Math.floorMod(steps, ROTATION_STEPS) == 0) return new int[]{dx, dy};

        PathPoint turned = new PathPoint(dx, dy).rotate(rotationRadians(steps));
        return new int[]{turned.roundedX(), turned.roundedY()};
    }

    public static List<PathPoint> trimmed(List<PathPoint> path, int trim) {
        if (trim <= 0 || path.size() < 2) return path;
        double kept = PathGeometry.length(path) - trim * TRIM_STEP;
        return PathGeometry.truncate(path, Math.max(PathGeometry.SAMPLE_SPACING, kept));
    }

    public static double lengthFor(double potency) {
        return BASE_LENGTH + Math.max(0.0, potency - 1) * LENGTH_PER_POTENCY;
    }

    private static final double WAVE_PERIOD = 4.0 * PathGeometry.CELL;
    private static final double ARC_PERIOD = 8.0 * PathGeometry.CELL;
    private static final double SPIRAL_START_RADIUS = PathGeometry.CELL;
    private static final double SPIRAL_TURN_GROWTH = 4.0 * PathGeometry.CELL;
    private static final double SPIRAL_SAMPLE_STEP = Math.PI / 96;
    private static final double TELEPORT_BASE = 10.0 * PathGeometry.CELL;
    private static final double TELEPORT_PER_CRUSH = 4.0 * PathGeometry.CELL;
    private static final int SQUARE_SPIRAL_TURNS = 3;
    private static final int SQUARE_SPIRAL_LEGS = 4 * SQUARE_SPIRAL_TURNS;
    private static final double SQUARE_SPIRAL_SCALE = 0.6;

    private double sway(double travelled) {
        return switch (this) {
            case WAVE -> Math.sin(2 * Math.PI * travelled / WAVE_PERIOD) * amplitude;
            case ARC -> Math.abs(Math.sin(2 * Math.PI * travelled / ARC_PERIOD)) * amplitude;
            default -> 0.0;
        };
    }

    public static double teleportRadius(double potency) {
        return TELEPORT_BASE + (potency - 1) * TELEPORT_PER_CRUSH;
    }

    public static int teleportDistance(double potency) {
        return Math.max(1, (int) Math.round(teleportRadius(potency)));
    }

    public static double teleportCeiling(Ingredients ingredient) {
        return teleportRadius(ingredient.getPotency(Ingredients.MAX_CRUSH));
    }

    public static double reachFor(double potency, int trim) {
        return Math.max(PathGeometry.CELL, teleportRadius(potency) - trim * TRIM_STEP);
    }

    public static int[] spiralDirection(double ceiling, double reach) {
        PathPoint end = SPIRAL_TELEPORT.flightEnd(0, 0, ceiling, reach);
        return new int[]{(int) Math.signum(end.x()), (int) Math.signum(end.y())};
    }

    private static final int[][] SQUARE_SPIRAL_HEADINGS = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};

    public static List<PathPoint> squareSpiral(double radius) {
        double step = Math.max(PathGeometry.SAMPLE_SPACING, radius * SQUARE_SPIRAL_SCALE / SQUARE_SPIRAL_TURNS);

        List<PathPoint> corners = new ArrayList<>();
        corners.add(PathPoint.ORIGIN);

        double x = 0.0;
        double y = 0.0;

        for (int leg = 0; leg < SQUARE_SPIRAL_LEGS; leg++) {
            int[] heading = SQUARE_SPIRAL_HEADINGS[leg % SQUARE_SPIRAL_HEADINGS.length];
            double length = step * (leg / 2 + 1);

            x += heading[0] * length;
            y += heading[1] * length;
            corners.add(new PathPoint(x, y));
        }
        return corners;
    }

    private static final double[][] SERPENT_WAYPOINTS = {
            {0.0, 0.0}, {0.0, 0.18}, {0.10, 0.27}, {0.145, 0.38}, {0.10, 0.46}, {0.0, 0.50},
            {-0.10, 0.54}, {-0.125, 0.61}, {-0.09, 0.70}, {0.0, 0.77}, {0.05, 0.85}, {0.03, 0.93}, {0.0, 1.0}
    };

    public static List<PathPoint> serpent(int dx, int dy, double ceiling) {
        PathPoint bearing = new PathPoint(dx, dy).normalised();
        if (bearing.isZero()) return List.of(PathPoint.ORIGIN);

        PathPoint side = new PathPoint(-bearing.y(), bearing.x());
        List<PathPoint> waypoints = new ArrayList<>(SERPENT_WAYPOINTS.length);
        for (double[] waypoint : SERPENT_WAYPOINTS) {
            waypoints.add(bearing.scale(waypoint[1] * ceiling).plus(side.scale(waypoint[0] * ceiling)));
        }
        return PathGeometry.spline(waypoints);
    }

    private static final double[][] SAWTOOTH_LEAD = {
            {0.000, 0.000}, {-0.018, 0.030}, {0.023, 0.064}, {0.076, 0.095}, {0.116, 0.129}, {0.138, 0.158},
            {0.105, 0.187}, {0.022, 0.201}
    };
    private static final double[][] SAWTOOTH_TEETH = {
            {0.022, 0.201}, {-0.135, 0.264}, {0.131, 0.446}, {-0.155, 0.576}, {0.091, 0.744}, {-0.186, 0.863},
            {0.000, 1.000}
    };

    private static List<PathPoint> bearingPoints(double[][] waypoints, PathPoint bearing, double ceiling) {
        PathPoint side = new PathPoint(-bearing.y(), bearing.x());
        List<PathPoint> points = new ArrayList<>(waypoints.length);
        for (double[] waypoint : waypoints) {
            points.add(bearing.scale(waypoint[1] * ceiling).plus(side.scale(waypoint[0] * ceiling)));
        }
        return points;
    }

    private static final double[][] STEPPED_LEAD = {{0.000, 0.000}, {0.000, 0.132}};
    private static final double[][] STEPPED_BODY = {
            {0.000, 0.132}, {0.142, 0.179}, {0.147, 0.505}, {-0.153, 0.505}, {-0.153, 0.837}, {0.000, 0.837},
            {0.000, 1.000}
    };

    private static final double[][] RIPPLE_LEAD = {
            {0.000, 0.000}, {-0.041, 0.030}, {-0.063, 0.080}, {-0.044, 0.132}, {0.007, 0.163}
    };
    private static final double[][] RIPPLE_BODY = {
            {0.007, 0.163}, {0.057, 0.200}, {0.102, 0.232}, {0.137, 0.273}, {0.136, 0.329}, {0.095, 0.359},
            {0.038, 0.389}, {-0.009, 0.418}, {-0.055, 0.448}, {-0.072, 0.504}, {-0.027, 0.550}, {0.019, 0.577},
            {0.064, 0.608}, {0.114, 0.640}, {0.134, 0.692}, {0.102, 0.732}, {0.056, 0.766}, {0.004, 0.791},
            {-0.042, 0.826}, {-0.074, 0.871}, {-0.070, 0.927}, {-0.035, 0.974}, {0.000, 1.000}
    };

    private record MarkedFlight(double[][] lead, double[][] body, boolean smooth) {
    }

    private MarkedFlight markedFlight() {
        return switch (this) {
            case SAWTOOTH_TELEPORT -> new MarkedFlight(SAWTOOTH_LEAD, SAWTOOTH_TEETH, false);
            case STEPPED_TELEPORT -> new MarkedFlight(STEPPED_LEAD, STEPPED_BODY, false);
            case RIPPLE_TELEPORT -> new MarkedFlight(RIPPLE_LEAD, RIPPLE_BODY, true);
            default -> null;
        };
    }

    private static List<PathPoint> markedLead(MarkedFlight marked, int dx, int dy, double ceiling) {
        PathPoint bearing = new PathPoint(dx, dy).normalised();
        if (bearing.isZero()) return List.of(PathPoint.ORIGIN);
        return PathGeometry.spline(bearingPoints(marked.lead(), bearing, ceiling));
    }

    private static List<PathPoint> markedShape(MarkedFlight marked, int dx, int dy, double ceiling) {
        PathPoint bearing = new PathPoint(dx, dy).normalised();
        if (bearing.isZero()) return List.of(PathPoint.ORIGIN);
        List<PathPoint> body = bearingPoints(marked.body(), bearing, ceiling);
        return joined(markedLead(marked, dx, dy, ceiling), marked.smooth() ? PathGeometry.spline(body) : straights(body));
    }

    public List<PathPoint> flight(int dx, int dy, double ceiling) {
        return switch (this) {
            case SPIRAL_TELEPORT -> squareSpiral(ceiling);
            case SERPENT_TELEPORT -> serpent(dx, dy, ceiling);
            case SAWTOOTH_TELEPORT, STEPPED_TELEPORT, RIPPLE_TELEPORT -> markedShape(markedFlight(), dx, dy, ceiling);
            default -> List.of(PathPoint.ORIGIN, new PathPoint(dx, dy).normalised().scale(ceiling));
        };
    }

    public List<PathPoint> flightTo(int dx, int dy, double ceiling, double reach) {
        List<PathPoint> shape = flight(dx, dy, ceiling);
        if (ceiling <= 0.0) return shape;

        double total = PathGeometry.length(shape);
        MarkedFlight marked = markedFlight();
        if (marked != null) {
            double lead = PathGeometry.length(markedLead(marked, dx, dy, ceiling));
            return PathGeometry.truncate(shape, markedWalk(lead, total, ceiling, reach));
        }

        double walked = Math.min(1.0, Math.max(0.0, reach / ceiling));
        return PathGeometry.truncate(shape, total * walked);
    }

    private static double markedWalk(double lead, double total, double ceiling, double reach) {
        if (reach <= TELEPORT_BASE) return lead * Math.max(0.0, reach) / TELEPORT_BASE;
        double past = Math.min(1.0, (reach - TELEPORT_BASE) / Math.max(1.0E-6, ceiling - TELEPORT_BASE));
        return lead + (total - lead) * past;
    }

    public PathPoint flightEnd(int dx, int dy, double ceiling, double reach) {
        List<PathPoint> walked = flightTo(dx, dy, ceiling, reach);
        return walked.get(walked.size() - 1);
    }

    public static double spiralSweep(double potency) {
        return 2 * Math.PI + (Math.PI / 2) * (potency - 1);
    }

    public List<PathPoint> teleportPreview(int dx, int dy, double ceiling, double reach) {
        PathPoint bearing = new PathPoint(dx, dy).normalised();
        if (bearing.isZero() && this != SPIRAL_TELEPORT) return List.of(PathPoint.ORIGIN);

        if (followsFlight()) {
            return PathGeometry.resample(flightTo(dx, dy, ceiling, reach), PathGeometry.SAMPLE_SPACING);
        }

        List<PathPoint> waypoints = new ArrayList<>();
        waypoints.add(PathPoint.ORIGIN);
        waypoints.add(bearing.scale(reach));
        return PathGeometry.resample(waypoints, PathGeometry.SAMPLE_SPACING);
    }

    private List<PathPoint> spiralWaypoints(double potency) {
        double sweep = spiralSweep(potency);
        double growth = SPIRAL_TURN_GROWTH / (2 * Math.PI);
        int samples = Math.max(1, (int) Math.round(sweep / SPIRAL_SAMPLE_STEP));

        List<PathPoint> waypoints = new ArrayList<>();
        waypoints.add(PathPoint.ORIGIN);

        for (int i = 1; i <= samples; i++) {
            double swept = SPIRAL_SAMPLE_STEP * i;
            waypoints.add(PathPoint.polar(Math.PI / 2 + swept, SPIRAL_START_RADIUS + swept * growth));
        }
        return waypoints;
    }

    public List<PathPoint> offsets(int dx, int dy, double length, double potency) {
        if (this == SPIRAL) {
            return PathGeometry.resample(spiralWaypoints(potency), PathGeometry.SAMPLE_SPACING);
        }

        PathPoint bearing = new PathPoint(dx, dy).normalised();
        if (bearing.isZero()) return List.of(PathPoint.ORIGIN);

        PathPoint perpendicular = new PathPoint(-bearing.y(), bearing.x());
        int samples = Math.max(1, (int) Math.ceil(length / PathGeometry.SAMPLE_SPACING));

        List<PathPoint> waypoints = new ArrayList<>(samples + 1);
        for (int i = 0; i <= samples; i++) {
            double travelled = length * i / samples;
            waypoints.add(bearing.scale(travelled).plus(perpendicular.scale(sway(travelled))));
        }
        return PathGeometry.resample(waypoints, PathGeometry.SAMPLE_SPACING);
    }
}
