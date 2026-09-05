package muddykat.alchemia.common.items.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public enum IngredientPath {
    STRAIGHT(0),
    WAVE(1),
    ARC(2),
    SPIRAL(0),
    SCRIPTED(0),
    TELEPORT(0),
    SPIRAL_TELEPORT(0);

    public static final int BASE_LENGTH = 5;
    public static final int LENGTH_PER_POTENCY = 2;

    private static final Map<Ingredients, IngredientPath> OVERRIDES = Map.of(
            Ingredients.Flameweed, WAVE,
            Ingredients.Rainbow_Cap, SPIRAL,
            Ingredients.Fable_Bismuth, SPIRAL_TELEPORT
    );

    private final int amplitude;

    IngredientPath(int amplitude) {
        this.amplitude = amplitude;
    }

    private static final Map<Ingredients, int[][]> SCRIPTS = Map.of(
            Ingredients.Featherbloom, new int[][]{{0, -3}, {1, -5}, {1, -8}, {0, -10}, {0, -11}},
            Ingredients.Fluffbloom, new int[][]{{-2, -2}, {-4, -4}, {-5, -6},
                    {-7, -8}, {-6, -10}, {-4, -11}, {-2, -10}, {-2, -8}, {-4, -7},
                    {-3, -10}, {-2, -13}, {-1, -16}},
            Ingredients.Boombloom, new int[][]{{-1, 0}, {-1, -3}, {2, 0}, {0, 0}, {7, 7}},
            Ingredients.Bloodthorn, new int[][]{{-2, 0}, {-4, -1}, {-5, -3}, {-6, -6}},
            Ingredients.Coldleaf, new int[][]{{2, 1}, {4, 3}, {6, 3}, {7, 2}, {8, 1}}
    );

    public static IngredientPath of(Ingredients ingredient) {
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
        return this == TELEPORT || this == SPIRAL_TELEPORT;
    }

    public boolean needsDirection() {
        return this != SPIRAL && this != SPIRAL_TELEPORT && this != SCRIPTED;
    }

    private static List<int[]> scriptWaypoints(Ingredients ingredient, int dx, int dy, double potency) {
        int[][] script = SCRIPTS.get(ingredient);
        List<int[]> waypoints = new ArrayList<>();
        for (int[] step : script) waypoints.add(new int[]{step[0], step[1]});

        int extra = (int) Math.round(Math.max(0.0, potency - 1) * LENGTH_PER_POTENCY);
        if (extra <= 0 || script.length < 2) return waypoints;

        int[] last = script[script.length - 1];
        int stepX = dx;
        int stepY = dy;

        if (stepX == 0 && stepY == 0) {
            int[] prev = script[script.length - 2];
            stepX = Integer.signum(last[0] - prev[0]);
            stepY = Integer.signum(last[1] - prev[1]);
        }

        for (int i = 1; i <= extra; i++) {
            waypoints.add(new int[]{last[0] + stepX * i, last[1] + stepY * i});
        }
        return waypoints;
    }

    public static List<int[]> pathFor(Ingredients ingredient, int dx, int dy, double potency) {
        IngredientPath shape = of(ingredient);
        if (shape == SCRIPTED) return interpolate(scriptWaypoints(ingredient, dx, dy, potency));
        return shape.offsets(dx, dy, lengthFor(potency), potency);
    }

    public static int lengthFor(double potency) {
        return BASE_LENGTH + (int) Math.round(Math.max(0.0, potency - 1) * LENGTH_PER_POTENCY);
    }

    private static final int WAVE_PERIOD = 4;
    private static final int ARC_PERIOD = 8;
    private static final double SPIRAL_START_RADIUS = 1.0;
    private static final double SPIRAL_TURN_GROWTH = 4.0;
    private static final double SPIRAL_SAMPLE_STEP = Math.PI / 24;
    private static final int TELEPORT_BASE = 10;
    private static final int TELEPORT_PER_CRUSH = 4;

    private int offsetAt(int index) {
        return switch (this) {
            case WAVE -> (int) Math.round(Math.sin(2 * Math.PI * index / WAVE_PERIOD) * amplitude);
            case ARC -> (int) Math.round(Math.abs(Math.sin(2 * Math.PI * index / ARC_PERIOD)) * amplitude);
            default -> 0;
        };
    }

    public static double teleportRadius(double potency) {
        return TELEPORT_BASE + (potency - 1) * TELEPORT_PER_CRUSH;
    }

    public static int teleportDistance(double potency) {
        return Math.max(1, (int) Math.round(teleportRadius(potency)));
    }

    public static double spiralAngle(double potency) {
        return Math.PI / 2 + spiralSweep(potency);
    }

    public static int[] spiralDirection(double potency) {
        double angle = spiralAngle(potency);
        return new int[]{(int) Math.round(Math.cos(angle)), (int) Math.round(Math.sin(angle))};
    }

    private static double previewRadius(double swept) {
        if (swept <= 2 * Math.PI) return TELEPORT_BASE * swept / (2 * Math.PI);
        return TELEPORT_BASE + (swept - 2 * Math.PI) * (TELEPORT_PER_CRUSH / (Math.PI / 2));
    }

    private List<int[]> teleportSpiralWaypoints(double potency, double scale) {
        double sweep = spiralSweep(potency);
        int samples = Math.max(1, (int) Math.round(sweep / SPIRAL_SAMPLE_STEP));

        List<int[]> waypoints = new ArrayList<>();
        for (int i = 1; i <= samples; i++) {
            double swept = SPIRAL_SAMPLE_STEP * i;
            double angle = Math.PI / 2 + swept;
            double radius = previewRadius(swept) * scale;
            waypoints.add(new int[]{
                    (int) Math.round(Math.cos(angle) * radius),
                    (int) Math.round(Math.sin(angle) * radius)
            });
        }
        return waypoints;
    }

    public List<int[]> teleportPreview(int dx, int dy, double potency, double scale) {
        if (this == SPIRAL_TELEPORT) return interpolate(teleportSpiralWaypoints(potency, scale));

        List<int[]> waypoints = new ArrayList<>();
        int steps = Math.max(1, (int) Math.round(teleportDistance(potency) * scale));
        for (int i = 1; i <= steps; i++) {
            waypoints.add(new int[]{dx * i, dy * i});
        }
        return interpolate(waypoints);
    }

    private static List<int[]> interpolate(List<int[]> waypoints) {
        List<int[]> cells = new ArrayList<>();
        int currentX = 0;
        int currentY = 0;

        for (int[] target : waypoints) {
            while (currentX != target[0] || currentY != target[1]) {
                currentX += Integer.signum(target[0] - currentX);
                currentY += Integer.signum(target[1] - currentY);
                cells.add(new int[]{currentX, currentY});
            }
        }
        return cells;
    }

    public static double spiralSweep(double potency) {
        return 2 * Math.PI + (Math.PI / 2) * (potency - 1);
    }

    private List<int[]> spiralWaypoints(double potency) {
        double sweep = spiralSweep(potency);
        double start = Math.PI / 2;
        double growth = SPIRAL_TURN_GROWTH / (2 * Math.PI);
        int samples = Math.max(1, (int) Math.round(sweep / SPIRAL_SAMPLE_STEP));

        List<int[]> waypoints = new ArrayList<>();
        for (int i = 1; i <= samples; i++) {
            double swept = SPIRAL_SAMPLE_STEP * i;
            double angle = start + swept;
            double radius = SPIRAL_START_RADIUS + swept * growth;
            waypoints.add(new int[]{
                    (int) Math.round(Math.cos(angle) * radius),
                    (int) Math.round(Math.sin(angle) * radius)
            });
        }
        return waypoints;
    }

    public List<int[]> offsets(int dx, int dy, int length, double potency) {
        List<int[]> waypoints;

        if (this == SPIRAL) {
            waypoints = spiralWaypoints(potency);
        } else {
            waypoints = new ArrayList<>();
            int perpX = -dy;
            int perpY = dx;

            for (int i = 0; i < length; i++) {
                int offset = offsetAt(i);
                waypoints.add(new int[]{dx * (i + 1) + perpX * offset, dy * (i + 1) + perpY * offset});
            }
        }

        return interpolate(waypoints);
    }
}
