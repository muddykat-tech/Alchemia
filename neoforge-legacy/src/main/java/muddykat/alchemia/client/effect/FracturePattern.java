package muddykat.alchemia.client.effect;

import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FracturePattern {

    private static final float SPLIT_CHANCE = 0.35F;
    private static final float HIDDEN = 0.16F;
    private static final float WEB_CHANCE = 0.5F;

    public record Cell(int[] vertices, float cx, float cy, float reach, float wobble) {
    }

    public record Edge(int from, int to, float reach, float fade, int first, int second) {
    }

    private final float extent;
    private final int spokes;
    private final float[] xs;
    private final float[] ys;
    private final List<Cell> cells;
    private final List<Edge> edges;

    public FracturePattern(long seed, int spokes, int rings, float extent, float core) {
        this.extent = extent;
        this.spokes = spokes;
        RandomSource random = RandomSource.create(seed);
        float step = (float) (2.0 * Math.PI / spokes);
        float[] base = new float[spokes];
        for (int spoke = 0; spoke < spokes; spoke++) base[spoke] = spoke * step + (random.nextFloat() - 0.5F) * 0.6F * step * 0.5F;

        xs = new float[1 + rings * spokes];
        ys = new float[1 + rings * spokes];
        for (int ring = 1; ring <= rings; ring++) {
            for (int spoke = 0; spoke < spokes; spoke++) {
                int id = id(ring, spoke, spokes);
                float angle = base[spoke] + (random.nextFloat() - 0.5F) * 0.22F;
                float radius = extent * (float) Math.pow((float) ring / rings, 0.85) * (0.82F + 0.36F * random.nextFloat());
                xs[id] = (float) Math.cos(angle) * radius;
                ys[id] = (float) Math.sin(angle) * radius;
            }
        }

        List<Cell> built = new ArrayList<>();
        for (int spoke = 0; spoke < spokes; spoke++) {
            built.add(cell(new int[]{0, id(1, spoke, spokes), id(1, (spoke + 1) % spokes, spokes)}, random));
        }
        for (int ring = 1; ring < rings; ring++) {
            for (int spoke = 0; spoke < spokes; spoke++) {
                int a = id(ring, spoke, spokes);
                int b = id(ring + 1, spoke, spokes);
                int c = id(ring + 1, (spoke + 1) % spokes, spokes);
                int d = id(ring, (spoke + 1) % spokes, spokes);
                if (random.nextFloat() < SPLIT_CHANCE) {
                    if (random.nextFloat() < 0.5F) {
                        built.add(cell(new int[]{a, b, c}, random));
                        built.add(cell(new int[]{a, c, d}, random));
                    } else {
                        built.add(cell(new int[]{a, b, d}, random));
                        built.add(cell(new int[]{b, c, d}, random));
                    }
                } else {
                    built.add(cell(new int[]{a, b, c, d}, random));
                }
            }
        }
        cells = Collections.unmodifiableList(built);

        float[] lengths = new float[spokes];
        for (int spoke = 0; spoke < spokes; spoke++) {
            lengths[spoke] = core + 0.1F + (extent * 1.02F - core - 0.1F) * (float) Math.pow(random.nextFloat(), 0.6);
        }

        Map<Long, Edge> found = new LinkedHashMap<>();
        for (int index = 0; index < built.size(); index++) {
            int[] corners = built.get(index).vertices();
            for (int corner = 0; corner < corners.length; corner++) {
                int from = corners[corner];
                int to = corners[(corner + 1) % corners.length];
                long key = ((long) Math.min(from, to) << 32) | Math.max(from, to);
                Edge existing = found.get(key);
                if (existing == null) {
                    found.put(key, new Edge(from, to, Math.min(radius(from), radius(to)), fade(from, to, core, lengths, random), index, -1));
                } else {
                    found.put(key, new Edge(existing.from(), existing.to(), existing.reach(), existing.fade(), existing.first(), index));
                }
            }
        }
        edges = Collections.unmodifiableList(new ArrayList<>(found.values()));
    }

    private float fade(int from, int to, float core, float[] lengths, RandomSource random) {
        if (from == 0 || to == 0) return lengths[(Math.max(from, to) - 1) % spokes];
        int fromRing = (from - 1) / spokes;
        int toRing = (to - 1) / spokes;
        int fromSpoke = (from - 1) % spokes;
        int toSpoke = (to - 1) % spokes;
        if (fromSpoke == toSpoke) return lengths[fromSpoke];
        float reach = Math.min(radius(from), radius(to));
        float roll = random.nextFloat();
        if (fromRing == toRing) {
            if (reach < core) return roll < 0.6F ? core : HIDDEN;
            float reachable = Math.min(lengths[fromSpoke], lengths[toSpoke]);
            return roll < WEB_CHANCE && reach < reachable ? reachable : HIDDEN;
        }
        return reach < core && roll < 0.4F ? core : HIDDEN;
    }

    private static int id(int ring, int spoke, int spokes) {
        return 1 + (ring - 1) * spokes + spoke;
    }

    private Cell cell(int[] corners, RandomSource random) {
        float cx = 0.0F;
        float cy = 0.0F;
        for (int corner : corners) {
            cx += xs[corner];
            cy += ys[corner];
        }
        cx /= corners.length;
        cy /= corners.length;
        return new Cell(corners, cx, cy, (float) Math.sqrt(cx * cx + cy * cy), random.nextFloat() - 0.5F);
    }

    public float radius(int vertex) {
        return (float) Math.sqrt(xs[vertex] * xs[vertex] + ys[vertex] * ys[vertex]);
    }

    public int vertexCount() {
        return xs.length;
    }

    public float x(int vertex) {
        return xs[vertex];
    }

    public float y(int vertex) {
        return ys[vertex];
    }

    public List<Cell> cells() {
        return cells;
    }

    public List<Edge> edges() {
        return edges;
    }

    public float extent() {
        return extent;
    }
}
