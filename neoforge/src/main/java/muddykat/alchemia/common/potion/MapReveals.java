package muddykat.alchemia.common.potion;

import muddykat.alchemia.common.items.helper.PathPoint;
import muddykat.alchemia.registration.registers.DataComponentRegistry;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MapReveals {

    public static final double CELL = PotionMap.CELL;
    public static final double RADIUS = 8 * PotionMap.CELL;
    public static final int PAD = (int) Math.ceil(RADIUS / CELL);
    public static final double ORIGIN = -PAD * CELL;
    public static final double START_RADIUS = 11 * PotionMap.CELL;

    private static final Map<PotionMap, Integer> CHARTABLE = new java.util.WeakHashMap<>();

    private MapReveals() {
    }

    public static int columns(PotionMap map) {
        return (int) Math.ceil(map.getMaxAlignment() / CELL) + 2 * PAD;
    }

    public static long[] empty(PotionMap map) {
        int columns = columns(map);
        return new long[(columns * columns + 63) / 64];
    }

    public static long[] bits(ItemStack guide, BrewBase base, PotionMap map) {
        List<Long> stored = guide.getOrDefault(DataComponentRegistry.MAP_REVEALS.get(), Map.<String, List<Long>>of()).get(base.id());
        long[] bits = empty(map);
        if (stored == null || stored.isEmpty() || stored.get(0) != columns(map)) return bits;
        for (int index = 0; index < bits.length && index + 1 < stored.size(); index++) bits[index] = stored.get(index + 1);
        return bits;
    }

    public static boolean revealed(long[] bits, int columns, int column, int row) {
        if (column < 0 || row < 0 || column >= columns || row >= columns) return false;
        int index = row * columns + column;
        return (bits[index >> 6] & (1L << (index & 63))) != 0L;
    }

    public static boolean mark(long[] bits, PotionMap map, PathPoint at) {
        return mark(bits, map, at, RADIUS);
    }

    public static boolean markStart(long[] bits, PotionMap map) {
        double middle = map.getMiddlePosition();
        return mark(bits, map, new PathPoint(middle, middle), START_RADIUS);
    }

    public static boolean mark(long[] bits, PotionMap map, PathPoint at, double radius) {
        int columns = columns(map);
        int reach = (int) Math.ceil(radius / CELL);
        int centreColumn = (int) Math.floor((at.x() - ORIGIN) / CELL);
        int centreRow = (int) Math.floor((at.y() - ORIGIN) / CELL);
        boolean changed = false;

        for (int row = centreRow - reach; row <= centreRow + reach; row++) {
            if (row < 0 || row >= columns) continue;
            for (int column = centreColumn - reach; column <= centreColumn + reach; column++) {
                if (column < 0 || column >= columns) continue;
                double dx = centre(column) - at.x();
                double dy = centre(row) - at.y();
                if (dx * dx + dy * dy > radius * radius) continue;

                int index = row * columns + column;
                long bit = 1L << (index & 63);
                if ((bits[index >> 6] & bit) != 0L) continue;
                bits[index >> 6] |= bit;
                changed = true;
            }
        }
        return changed;
    }

    public static boolean reveal(Iterable<ItemStack> guides, BrewBase base, PotionMap map, PathPoint at) {
        boolean changed = false;
        for (ItemStack guide : guides) {
            long[] bits = bits(guide, base, map);
            if (!mark(bits, map, at, RADIUS)) continue;
            store(guide, base, map, bits);
            changed = true;
        }
        return changed;
    }

    public static boolean revealEffect(Iterable<ItemStack> guides, AlchemiaEffect recipe) {
        if (!PotionMap.isReady()) return false;

        boolean changed = false;
        for (BrewBase base : BrewBases.values()) {
            PotionMap map = PotionMap.get(base);
            if (map == null || !map.base().id().equals(base.id())) continue;
            PathPoint at = map.positionOf(recipe);
            if (at != null && reveal(guides, base, map, at)) changed = true;
        }
        return changed;
    }

    public static boolean learn(ItemStack into, ItemStack from) {
        Map<String, List<Long>> theirs = from.getOrDefault(DataComponentRegistry.MAP_REVEALS.get(), Map.<String, List<Long>>of());
        Map<String, List<Long>> ours = new HashMap<>(into.getOrDefault(DataComponentRegistry.MAP_REVEALS.get(), Map.<String, List<Long>>of()));
        boolean changed = false;

        for (Map.Entry<String, List<Long>> entry : theirs.entrySet()) {
            List<Long> mine = ours.get(entry.getKey());
            List<Long> other = entry.getValue();
            if (other.isEmpty()) continue;
            if (mine == null || mine.isEmpty() || !mine.get(0).equals(other.get(0))) {
                if (mine == null || mine.isEmpty()) {
                    ours.put(entry.getKey(), List.copyOf(other));
                    changed = true;
                }
                continue;
            }

            List<Long> merged = new ArrayList<>(mine);
            while (merged.size() < other.size()) merged.add(0L);
            boolean grew = false;
            for (int index = 1; index < other.size(); index++) {
                long joined = merged.get(index) | other.get(index);
                if (joined == merged.get(index)) continue;
                merged.set(index, joined);
                grew = true;
            }
            if (!grew) continue;
            ours.put(entry.getKey(), List.copyOf(merged));
            changed = true;
        }
        if (changed) into.set(DataComponentRegistry.MAP_REVEALS.get(), Map.copyOf(ours));
        return changed;
    }

    public static double fraction(long[] bits, PotionMap map) {
        int columns = columns(map);
        int total = chartable(map);
        if (total <= 0) return 0.0;

        int found = 0;
        for (int row = 0; row < columns; row++) {
            for (int column = 0; column < columns; column++) {
                if (revealed(bits, columns, column, row) && map.inBounds(centre(column), centre(row))) found++;
            }
        }
        return Math.min(1.0, (double) found / total);
    }

    public static double explored(ItemStack guide) {
        if (!PotionMap.isReady()) return 0.0;
        double total = 0.0;
        int maps = 0;
        for (BrewBase base : BrewBases.values()) {
            PotionMap map = PotionMap.get(base);
            if (map == null || !map.base().id().equals(base.id())) continue;
            total += fraction(bits(guide, base, map), map);
            maps++;
        }
        return maps == 0 ? 0.0 : total / maps;
    }

    private static int chartable(PotionMap map) {
        synchronized (CHARTABLE) {
            Integer known = CHARTABLE.get(map);
            if (known != null) return known;

            int columns = columns(map);
            int total = 0;
            for (int row = 0; row < columns; row++) {
                for (int column = 0; column < columns; column++) {
                    if (map.inBounds(centre(column), centre(row))) total++;
                }
            }
            CHARTABLE.put(map, total);
            return total;
        }
    }

    private static double centre(int cell) {
        return ORIGIN + (cell + 0.5) * CELL;
    }

    private static void store(ItemStack guide, BrewBase base, PotionMap map, long[] bits) {
        Map<String, List<Long>> all = new HashMap<>(guide.getOrDefault(DataComponentRegistry.MAP_REVEALS.get(), Map.<String, List<Long>>of()));
        List<Long> packed = new ArrayList<>(bits.length + 1);
        packed.add((long) columns(map));
        for (long word : bits) packed.add(word);
        all.put(base.id(), List.copyOf(packed));
        guide.set(DataComponentRegistry.MAP_REVEALS.get(), Map.copyOf(all));
    }
}
