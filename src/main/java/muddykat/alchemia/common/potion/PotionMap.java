package muddykat.alchemia.common.potion;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.helper.IngredientAlignment;
import muddykat.alchemia.common.items.helper.IngredientPath;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;

import java.util.*;

public class PotionMap {
    public static PotionMap INSTANCE;
    public static final int HARD_MARGIN = 3;

    private final int size;
    private final int softSize;
    private final int middlePosition;
    public final HashMap<String, PotionEffectPosition> effectHashMap = new HashMap<>();
    private final List<MapEntry> entries = new ArrayList<>();
    private final Set<Long> deadzones = new HashSet<>();

    public PotionMap(long seed) {
        Random rand = new Random(seed);
        softSize = PotionEnum.values().length * 2;
        size = softSize + HARD_MARGIN * 2;
        middlePosition = size / 2;

        int minDistance = 6;

        for (PotionEnum e : PotionEnum.values()) {
            Holder<MobEffect> effect = e.getEffect();
            int strength = e.maxStrength;
            boolean placed = false;
            int attempts = 0;

            while (!placed && attempts < 250) {
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
                double baseRadius = 15.0;

                double randomRadius;
                if (rarityOrdinal == 0) {
                    randomRadius = baseRadius * 0.75 + rand.nextInt(-2,2);
                } else if (rarityOrdinal == 1) {
                    randomRadius = baseRadius * 1.25 + rand.nextInt(-2,2);
                } else if (rarityOrdinal == 2) {
                    randomRadius = baseRadius * 2  + rand.nextInt(-2,2);
                } else {
                    randomRadius = baseRadius * 3.25  + rand.nextInt(-2,2);
                }

                double angle = Math.atan2(biasY, biasX) + (rand.nextDouble(-1,1)) * Math.PI / 4.0;

                int randX = middlePosition + (int) (randomRadius * Math.cos(angle));
                int randY = middlePosition + (int) (randomRadius * Math.sin(angle));

                randX = Math.max(HARD_MARGIN, Math.min(size - 1 - HARD_MARGIN, randX));
                randY = Math.max(HARD_MARGIN, Math.min(size - 1 - HARD_MARGIN, randY));

                boolean positionValid = true;

                for (Map.Entry<String, PotionEffectPosition> entry : effectHashMap.entrySet()) {
                    String position = entry.getKey();
                    String[] parts = position.split(",");
                    int x = Integer.parseInt(parts[0]);
                    int y = Integer.parseInt(parts[1]);

                    double distance = Math.sqrt(Math.pow(x - randX, 2) + Math.pow(y - randY, 2));

                    if (distance < minDistance) {
                        positionValid = false;
                        break;
                    }
                }

                if (positionValid) {
                    String position = randX + "," + randY;
                    effectHashMap.put(position, new PotionEffectPosition(e, effect, 1200, strength, e.getPotion(), true));
                    placed = true;
                }

                attempts++;
            }

            if (!placed) {
                Alchemia.LOGGER.warn("Could not place potion effect {}", e.name());
            }
        }

        generateDeadzones(seed, rand);
        Alchemia.LOGGER.info("Potion map built: {} effects, {} deadzone cells, {} effects need crystals",
                effectHashMap.size(), deadzones.size(), crystalGated);
    }

    public static final int MAX_CRYSTAL_GATED = 3;
    public static final int EFFECT_RADIUS = 2;

    private static final int DEADZONE_MARGIN = 2;
    private static final int CENTRE_MARGIN = 6;
    private static final double NOISE_SCALE = 0.11;
    private static final double MIN_THRESHOLD = 0.09;
    private static final double THRESHOLD_SPREAD = 0.22;
    private static final double MAX_THRESHOLD = 1.80;
    private static final double THRESHOLD_STEP = 0.05;

    private int crystalGated;

    private static long pack(int x, int y) {
        return ((long) x << 32) | (y & 0xFFFFFFFFL);
    }

    public boolean isDeadzone(int x, int y) {
        return deadzones.contains(pack(x, y));
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && x < size && y >= 0 && y < size;
    }

    public int clamp(int value) {
        return Math.max(0, Math.min(size - 1, value));
    }

    public Set<Long> deadzoneCells() {
        return deadzones;
    }

    public int crystalGatedEffects() {
        return crystalGated;
    }

    private static final int[][] CRYSTAL_VECTORS = crystalVectors();

    private static int[][] crystalVectors() {
        List<int[]> vectors = new ArrayList<>();
        for (Ingredients ingredient : Ingredients.values()) {
            if (ingredient.getType() != IngredientType.Mineral) continue;
            vectors.add(new int[]{
                    ingredient.getPrimaryAlignment().getX() + ingredient.getSecondaryAlignment().getX(),
                    ingredient.getPrimaryAlignment().getY() + ingredient.getSecondaryAlignment().getY()
            });
        }
        return vectors.toArray(int[][]::new);
    }

    public static boolean isTeleport(ItemStack stack) {
        return stack.getItem() instanceof ItemIngredient ingredient
                && ingredient.getIngredientType() == IngredientType.Mineral;
    }

    public static int teleportDistance(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemIngredient ingredient)) return IngredientPath.teleportDistance(1);
        return teleportDistance(ingredient.getPotency(stack));
    }

    public static int teleportDistance(double potency) {
        return IngredientPath.teleportDistance(potency);
    }

    public int[] teleportPolar(int x, int y, double angle, double radius) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        for (double reach = radius; reach >= 1.0; reach -= 1.0) {
            int nx = x + (int) Math.round(cos * reach);
            int ny = y + (int) Math.round(sin * reach);
            if (inBounds(nx, ny)) return new int[]{nx, ny};
        }
        return new int[]{x, y};
    }

    public int[] teleport(int x, int y, int dx, int dy, int distance) {
        if (dx == 0 && dy == 0) return new int[]{middlePosition, middlePosition};

        for (int step = distance; step >= 1; step--) {
            int nx = x + dx * step;
            int ny = y + dy * step;
            if (inBounds(nx, ny)) return new int[]{nx, ny};
        }
        return new int[]{x, y};
    }

    public record PotionPath(List<int[]> cells, boolean teleport, boolean stalled, boolean dead) {
        public int[] end(int fromX, int fromY) {
            return cells.isEmpty() ? new int[]{fromX, fromY} : cells.get(cells.size() - 1);
        }
    }

    public int[] positionOf(PotionEnum recipe) {
        for (MapEntry entry : entries()) {
            if (entry.effect().getRecipe() == recipe) return new int[]{entry.x(), entry.y()};
        }
        return null;
    }

    private PotionPath homingPath(int x, int y, PotionEnum target) {
        int[] goal = positionOf(target);
        if (goal == null) return new PotionPath(List.of(), false, true, false);

        List<int[]> cells = new ArrayList<>();
        int cx = x;
        int cy = y;

        for (int step = 0; step < VanillaIngredients.DRIFT_LENGTH; step++) {
            if (cx == goal[0] && cy == goal[1]) break;

            int nx = cx + Integer.signum(goal[0] - cx);
            int ny = cy + Integer.signum(goal[1] - cy);
            if (!inBounds(nx, ny) || isDeadzone(nx, ny)) break;

            cx = nx;
            cy = ny;
            cells.add(new int[]{cx, cy});
        }
        return new PotionPath(cells, false, cells.isEmpty(), false);
    }

    public List<int[]> fixedDriftOffsets(PotionEnum target) {
        int[] goal = positionOf(target);
        if (goal == null) return List.of();

        int reachX = goal[0] - middlePosition;
        int reachY = goal[1] - middlePosition;
        if (reachX == 0 && reachY == 0) return List.of();

        double angle = Math.atan2(reachY, reachX);
        List<int[]> offsets = new ArrayList<>();
        int cx = 0;
        int cy = 0;

        for (int step = 1; step <= VanillaIngredients.DRIFT_LENGTH; step++) {
            int tx = (int) Math.round(Math.cos(angle) * step);
            int ty = (int) Math.round(Math.sin(angle) * step);

            while (cx != tx || cy != ty) {
                cx += Integer.signum(tx - cx);
                cy += Integer.signum(ty - cy);
                offsets.add(new int[]{cx, cy});
            }
        }
        return offsets;
    }

    private PotionPath fixedPath(int x, int y, PotionEnum target) {
        List<int[]> cells = new ArrayList<>();

        for (int[] offset : fixedDriftOffsets(target)) {
            int nx = x + offset[0];
            int ny = y + offset[1];
            if (!inBounds(nx, ny) || isDeadzone(nx, ny)) break;
            cells.add(new int[]{nx, ny});
        }
        return new PotionPath(cells, false, cells.isEmpty(), false);
    }

    public PotionPath walk(int x, int y, ItemStack stack) {
        VanillaIngredients.Drift drift = VanillaIngredients.driftOf(stack);
        if (drift != null) {
            return drift.homing() ? homingPath(x, y, drift.target()) : fixedPath(x, y, drift.target());
        }

        if (!(stack.getItem() instanceof ItemIngredient ingredient)) {
            return new PotionPath(List.of(), false, true, false);
        }

        int dx = Integer.signum(ingredient.getDriftX(stack));
        int dy = Integer.signum(ingredient.getDriftY(stack));
        double potency = ingredient.getPotency(stack);
        IngredientPath shape = IngredientPath.of(ingredient.getIngredient());

        if (shape.teleports()) {
            int[] landing = shape == IngredientPath.SPIRAL_TELEPORT
                    ? teleportPolar(x, y, IngredientPath.spiralAngle(potency), IngredientPath.teleportRadius(potency))
                    : teleport(x, y, dx, dy, teleportDistance(stack));

            if (isDeadzone(landing[0], landing[1])) {
                return new PotionPath(List.of(landing), true, false, true);
            }

            boolean moved = landing[0] != x || landing[1] != y;
            return new PotionPath(moved ? List.of(landing) : List.of(), true, !moved, false);
        }

        if (shape.needsDirection() && dx == 0 && dy == 0) return new PotionPath(List.of(), false, true, false);

        List<int[]> cells = new ArrayList<>();

        for (int[] offset : IngredientPath.pathFor(ingredient.getIngredient(), dx, dy, potency)) {
            int nx = x + offset[0];
            int ny = y + offset[1];
            if (!inBounds(nx, ny) || isDeadzone(nx, ny)) break;
            cells.add(new int[]{nx, ny});
        }

        return new PotionPath(cells, false, cells.isEmpty(), false);
    }

    public List<int[]> teleportCurve(int x, int y, ItemStack stack) {
        if (!(stack.getItem() instanceof ItemIngredient ingredient)) return List.of();

        IngredientPath shape = IngredientPath.of(ingredient.getIngredient());
        if (!shape.teleports()) return List.of();

        int dx = Integer.signum(ingredient.getDriftX(stack));
        int dy = Integer.signum(ingredient.getDriftY(stack));

        List<int[]> curve = new ArrayList<>();
        for (int[] offset : shape.teleportPreview(dx, dy, ingredient.getPotency(stack), 1.0)) {
            int nx = x + offset[0];
            int ny = y + offset[1];
            if (!inBounds(nx, ny)) break;
            curve.add(new int[]{nx, ny});
        }
        return curve;
    }

    public int[] step(int x, int y, ItemStack stack) {
        return walk(x, y, stack).end(x, y);
    }

    public PotionEffectPosition getEffectAt(int x, int y) {
        return effectHashMap.get(x + "," + y);
    }

    private void generateDeadzones(long seed, Random rand) {
        PerlinSimplexNoise noise = new PerlinSimplexNoise(RandomSource.create(seed), List.of(-3, -2, -1, 0));
        double start = MIN_THRESHOLD + rand.nextDouble() * THRESHOLD_SPREAD;

        Set<Long> protectedCells = new HashSet<>();
        for (MapEntry entry : entries()) {
            markProtected(protectedCells, entry.x(), entry.y(), DEADZONE_MARGIN);
        }
        markProtected(protectedCells, middlePosition, middlePosition, CENTRE_MARGIN);

        for (double threshold = start; threshold <= MAX_THRESHOLD; threshold += THRESHOLD_STEP) {
            deadzones.clear();

            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    if (protectedCells.contains(pack(x, y))) continue;
                    if (noise.getValue(x * NOISE_SCALE, y * NOISE_SCALE, false) > threshold) {
                        deadzones.add(pack(x, y));
                    }
                }
            }

            crystalGated = countUnreachable(false);
            if (crystalGated <= MAX_CRYSTAL_GATED && countUnreachable(true) == 0) return;
        }

        deadzones.clear();
        crystalGated = 0;
    }

    private void markProtected(Set<Long> protectedCells, int x, int y, int margin) {
        for (int dx = -margin; dx <= margin; dx++) {
            for (int dy = -margin; dy <= margin; dy++) {
                protectedCells.add(pack(x + dx, y + dy));
            }
        }
    }

    private int countUnreachable(boolean withCrystals) {
        boolean[][] seen = new boolean[size][size];
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        seen[middlePosition][middlePosition] = true;
        queue.add(new int[]{middlePosition, middlePosition});

        while (!queue.isEmpty()) {
            int[] at = queue.poll();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int nx = at[0] + dx;
                    int ny = at[1] + dy;
                    if (!inBounds(nx, ny) || seen[nx][ny] || isDeadzone(nx, ny)) continue;
                    seen[nx][ny] = true;
                    queue.add(new int[]{nx, ny});
                }
            }

            if (!withCrystals) continue;
            for (int[] vector : CRYSTAL_VECTORS) {
                int[] landing = teleport(at[0], at[1], vector[0], vector[1], IngredientPath.teleportDistance(1));
                if (isDeadzone(landing[0], landing[1]) || seen[landing[0]][landing[1]]) continue;
                seen[landing[0]][landing[1]] = true;
                queue.add(landing);
            }
        }

        int unreachable = 0;
        for (MapEntry entry : entries()) {
            if (!seen[entry.x()][entry.y()]) unreachable++;
        }
        return unreachable;
    }

    public static void scramble(long seed) {
        INSTANCE = new PotionMap(seed);
    }

    public PotionEffectPosition getEffectPotion(int[] alignment){
        String key = alignment[0] + "," + alignment[1];
        PotionEffectPosition defaultEffect = new PotionEffectPosition(null, null, 10, 0, Potions.AWKWARD, false);

        PotionEffectPosition closestEffect = null;
        double closestDistance = Double.MAX_VALUE;
        int closestRing = Integer.MAX_VALUE;

        for (Map.Entry<String, PotionEffectPosition> entry : effectHashMap.entrySet()) {
            String positionKey = entry.getKey();
            PotionEffectPosition effectPosition = entry.getValue();

            String[] parts = positionKey.split(",");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);

            int dx = x - alignment[0];
            int dy = y - alignment[1];
            int ring = Math.max(Math.abs(dx), Math.abs(dy));
            if (ring > EFFECT_RADIUS) continue;

            double distance = Math.sqrt(dx * dx + dy * dy);
            if (closestEffect == null || ring < closestRing
                    || (ring == closestRing && distance < closestDistance)) {
                closestEffect = effectPosition;
                closestDistance = distance;
                closestRing = ring;
            }
        }

        if (closestEffect == null) return defaultEffect;
        if (closestRing == 0) return closestEffect;

        int falloff = Math.max(0, closestEffect.getStrength() - closestRing);
        return new PotionEffectPosition(closestEffect.getRecipe(), closestEffect.getEffect(),
                closestEffect.getDuration(), falloff, closestEffect.getPotion(), false);
    }

    public List<MapEntry> entries() {
        if (entries.isEmpty() && !effectHashMap.isEmpty()) {
            for (Map.Entry<String, PotionEffectPosition> entry : effectHashMap.entrySet()) {
                String[] parts = entry.getKey().split(",");
                entries.add(new MapEntry(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), entry.getValue()));
            }
        }
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
    }
}
