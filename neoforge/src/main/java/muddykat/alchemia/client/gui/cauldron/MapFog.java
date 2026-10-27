package muddykat.alchemia.client.gui.cauldron;

import muddykat.alchemia.common.items.helper.PathPoint;
import muddykat.alchemia.common.potion.BrewBase;
import muddykat.alchemia.common.potion.MapReveals;
import muddykat.alchemia.common.potion.PotionMap;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MapFog {

    static final double LINE = 0.5;
    static final double SHADE = 0.16;

    private static final int BLUR_PASSES = 2;
    private static final float JITTER = 0.14F;
    private static final double PUSH_MILLIS = 420.0;

    private List<float[]> levels = List.of();
    private List<float[]> from = List.of();
    private final List<Integer> counts = new ArrayList<>();
    private long[] bits = new long[0];
    private int columns;
    private long pushedAt;
    private int version;

    private @Nullable PotionMap sessionMap;
    private long[] sessionBits = new long[0];
    private @Nullable PathPoint sessionAt;

    public boolean isEmpty() {
        return levels.isEmpty();
    }

    public int version() {
        return version;
    }

    public void track(PotionMap map, PathPoint brewAt, BrewBase base, List<ItemStack> guides, long now) {
        if (sessionMap != map) {
            sessionMap = map;
            sessionBits = MapReveals.empty(map);
            MapReveals.markStart(sessionBits, map);
            sessionAt = null;
            reset();
        }
        if (sessionAt == null || sessionAt.distanceTo(brewAt) >= MapReveals.CELL / 2.0) {
            MapReveals.mark(sessionBits, map, brewAt);
            sessionAt = brewAt;
        }

        long[] combined = sessionBits.clone();
        for (ItemStack guide : guides) {
            long[] stored = MapReveals.bits(guide, base, map);
            for (int index = 0; index < combined.length; index++) combined[index] |= stored[index];
        }
        update(combined, MapReveals.columns(map), now);
    }

    public void forget() {
        sessionMap = null;
        reset();
    }

    public boolean hides(PathPoint at, long now) {
        if (levels.isEmpty()) return false;
        return clear(0, at.x(), at.y(), push(now)) < LINE;
    }

    private void reset() {
        if (!levels.isEmpty()) version++;
        levels = List.of();
        from = List.of();
        counts.clear();
        bits = new long[0];
        columns = 0;
    }

    private void update(long[] revealed, int columns, long now) {
        if (columns == this.columns && Arrays.equals(revealed, bits)) return;

        boolean fresh = columns != this.columns || levels.isEmpty();
        List<float[]> shown = fresh ? List.of() : shown(push(now));
        bits = revealed.clone();
        this.columns = columns;
        build();
        from = shown;
        pushedAt = now;
        version++;
    }

    int level(double zoom, double minPixels) {
        int level = 0;
        while (level + 1 < levels.size() && MapReveals.CELL * (1 << level) * zoom < minPixels) level++;
        return level;
    }

    double push(long now) {
        if (from.isEmpty()) return 1.0;
        double t = Mth.clamp((now - pushedAt) / PUSH_MILLIS, 0.0, 1.0);
        return t * t * (3.0 - 2.0 * t);
    }

    double clear(int level, double x, double y, double push) {
        if (levels.isEmpty()) return 1.0;
        double current = sample(levels, level, x, y);
        if (push >= 1.0) return current;
        return Mth.lerp(push, sample(from, level, x, y), current);
    }

    private double sample(List<float[]> field, int level, double x, double y) {
        int clamped = Math.max(0, Math.min(field.size() - 1, level));
        float[] values = field.get(clamped);
        int count = counts.get(clamped);
        double span = MapReveals.CELL * (1 << clamped);

        double u = (x - MapReveals.ORIGIN) / span - 0.5;
        double v = (y - MapReveals.ORIGIN) / span - 0.5;
        int column = (int) Math.floor(u);
        int row = (int) Math.floor(v);
        double fx = u - column;
        double fy = v - row;

        double upper = Mth.lerp(fx, at(values, count, column, row), at(values, count, column + 1, row));
        double lower = Mth.lerp(fx, at(values, count, column, row + 1), at(values, count, column + 1, row + 1));
        return Mth.lerp(fy, upper, lower);
    }

    private static float at(float[] values, int count, int column, int row) {
        if (column < 0 || row < 0 || column >= count || row >= count) return 0.0F;
        return values[row * count + column];
    }

    private List<float[]> shown(double push) {
        if (push >= 1.0) return levels;
        List<float[]> mixed = new ArrayList<>(levels.size());
        for (int level = 0; level < levels.size(); level++) {
            float[] start = from.get(level);
            float[] end = levels.get(level);
            float[] out = new float[end.length];
            for (int index = 0; index < end.length; index++) out[index] = (float) Mth.lerp(push, start[index], end[index]);
            mixed.add(out);
        }
        return mixed;
    }

    private void build() {
        float[] field = new float[columns * columns];
        for (int row = 0; row < columns; row++) {
            for (int column = 0; column < columns; column++) {
                field[row * columns + column] = MapReveals.revealed(bits, columns, column, row) ? 1.0F : 0.0F;
            }
        }
        for (int pass = 0; pass < BLUR_PASSES; pass++) field = blur(field, columns);
        for (int row = 0; row < columns; row++) {
            for (int column = 0; column < columns; column++) {
                field[row * columns + column] += jitter(column, row);
            }
        }

        List<float[]> built = new ArrayList<>();
        counts.clear();
        built.add(field);
        counts.add(columns);

        float[] previous = field;
        int count = columns;
        while (count > 2) {
            int next = (count + 1) / 2;
            float[] level = new float[next * next];
            for (int row = 0; row < next; row++) {
                for (int column = 0; column < next; column++) {
                    float sum = 0.0F;
                    int total = 0;
                    for (int dy = 0; dy < 2; dy++) {
                        for (int dx = 0; dx < 2; dx++) {
                            int x = column * 2 + dx;
                            int y = row * 2 + dy;
                            if (x >= count || y >= count) continue;
                            sum += previous[y * count + x];
                            total++;
                        }
                    }
                    level[row * next + column] = total > 0 ? sum / total : 0.0F;
                }
            }
            built.add(level);
            counts.add(next);
            previous = level;
            count = next;
        }
        levels = built;
    }

    private static float[] blur(float[] field, int columns) {
        float[] out = new float[field.length];
        for (int row = 0; row < columns; row++) {
            for (int column = 0; column < columns; column++) {
                float sum = 0.0F;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int x = column + dx;
                        int y = row + dy;
                        if (x >= 0 && y >= 0 && x < columns && y < columns) sum += field[y * columns + x];
                    }
                }
                out[row * columns + column] = sum / 9.0F;
            }
        }
        return out;
    }

    private static float jitter(int column, int row) {
        int hash = column * 73856093 ^ row * 19349663;
        hash ^= hash >>> 13;
        hash *= 0x5BD1E995;
        hash ^= hash >>> 15;
        return ((hash & 0xFFFF) / 65535.0F * 2.0F - 1.0F) * JITTER;
    }
}
