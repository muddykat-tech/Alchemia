package muddykat.alchemia.client.gui;

import muddykat.alchemia.common.items.helper.PathGeometry;
import muddykat.alchemia.common.items.helper.PathPoint;

import java.util.List;
import java.util.Objects;

public class PathAnimator {

    public static final long MORPH_MS = 260L;

    private final long duration;

    private Object signature;
    private List<PathPoint> from = List.of();
    private List<PathPoint> to = List.of();
    private long startedAt;

    public PathAnimator() {
        this(MORPH_MS);
    }

    public PathAnimator(long duration) {
        this.duration = Math.max(1L, duration);
    }

    public List<PathPoint> update(Object signature, List<PathPoint> target, long millis) {
        if (this.signature == null) {
            this.signature = signature;
            this.from = List.copyOf(target);
            this.to = List.copyOf(target);
            this.startedAt = millis - duration;
            return this.to;
        }

        if (!Objects.equals(this.signature, signature)) {
            this.from = current(millis);
            this.to = List.copyOf(target);
            this.signature = signature;
            this.startedAt = millis;
        } else if (settled(millis)) {
            this.to = List.copyOf(target);
        }

        return current(millis);
    }

    public List<PathPoint> current(long millis) {
        if (from.isEmpty()) return to;
        if (settled(millis)) return to;

        double progress = (double) (millis - startedAt) / duration;
        return PathGeometry.morph(from, to, PathGeometry.ease(progress));
    }

    public PathPoint endOf(long millis) {
        List<PathPoint> points = current(millis);
        return points.isEmpty() ? PathPoint.ORIGIN : points.get(points.size() - 1);
    }

    public boolean settled(long millis) {
        return millis - startedAt >= duration;
    }

    public void forget() {
        signature = null;
        from = List.of();
        to = List.of();
    }
}
