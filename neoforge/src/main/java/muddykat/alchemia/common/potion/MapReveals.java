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

    private static final Map<PotionMap, Integer> CHARTABLE = new java.util.WeakHashMap<>();

    private MapReveals() {
    }

    public static int columns(PotionMap map) {
        return (int) Math.ceil(map.getMaxAlignment() / CELL);
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
        int columns = columns(map);
        int reach = (int) Math.ceil(RADIUS / CELL);
        int centreColumn = (int) Math.floor(at.x() / CELL);
        int centreRow = (int) Math.floor(at.y() / CELL);
        boolean changed = false;

        for (int row = centreRow - reach; row <= centreRow + reach; row++) {
            if (row < 0 || row >= columns) continue;
            for (int column = centreColumn - reach; column <= centreColumn + reach; column++) {
                if (column < 0 || column >= columns) continue;
                double dx = (column + 0.5) * CELL - at.x();
                double dy = (row + 0.5) * CELL - at.y();
                if (dx * dx + dy * dy > RADIUS * RADIUS) continue;

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
            if (!mark(bits, map, at)) continue;
            store(guide, base, map, bits);
            changed = true;
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
                if (revealed(bits, columns, column, row) && map.inBounds((column + 0.5) * CELL, (row + 0.5) * CELL)) found++;
            }
        }
        return Math.min(1.0, (double) found / total);
    }

    private static int chartable(PotionMap map) {
        synchronized (CHARTABLE) {
            Integer known = CHARTABLE.get(map);
            if (known != null) return known;

            int columns = columns(map);
            int total = 0;
            for (int row = 0; row < columns; row++) {
                for (int column = 0; column < columns; column++) {
                    if (map.inBounds((column + 0.5) * CELL, (row + 0.5) * CELL)) total++;
                }
            }
            CHARTABLE.put(map, total);
            return total;
        }
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
