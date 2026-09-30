package muddykat.alchemia.common.items.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleFunction;

public final class PathGeometry {

    public static final double CELL = 16.0;
    public static final double SAMPLE_SPACING = 2.0;
    public static final int MORPH_SAMPLES = 96;
    public static final int LEAD_PROBES = 32;

    private static final double EPSILON = 1.0E-6;
    private static final int MAX_SAMPLES = 4096;

    private PathGeometry() {
    }

    public static List<PathPoint> spline(List<PathPoint> waypoints) {
        return spline(waypoints, SAMPLE_SPACING);
    }

    public static List<PathPoint> spline(List<PathPoint> waypoints, double spacing) {
        List<PathPoint> unique = withoutRepeats(waypoints);
        if (unique.size() < 3) return resample(unique, spacing);

        List<PathPoint> dense = new ArrayList<>();
        dense.add(unique.get(0));

        for (int i = 0; i < unique.size() - 1; i++) {
            PathPoint before = unique.get(Math.max(0, i - 1));
            PathPoint from = unique.get(i);
            PathPoint to = unique.get(i + 1);
            PathPoint after = unique.get(Math.min(unique.size() - 1, i + 2));

            int steps = Math.max(2, (int) Math.ceil(from.distanceTo(to) / spacing) * 2);
            for (int step = 1; step <= steps; step++) {
                dense.add(catmullRom(before, from, to, after, (double) step / steps));
            }
        }
        return resample(dense, spacing);
    }

    public static List<PathPoint> sample(DoubleFunction<PathPoint> curve, double from, double to, int steps) {
        List<PathPoint> points = new ArrayList<>(Math.max(1, steps + 1));
        for (int step = 0; step <= steps; step++) {
            points.add(curve.apply(from + (to - from) * step / steps));
        }
        return points;
    }

    public static List<PathPoint> resample(List<PathPoint> polyline, double spacing) {
        if (polyline.size() < 2 || spacing <= EPSILON) return List.copyOf(polyline);

        double total = length(polyline);
        if (total < spacing) return List.of(polyline.get(0), polyline.get(polyline.size() - 1));

        int count = Math.min(MAX_SAMPLES, (int) Math.ceil(total / spacing));
        List<PathPoint> spaced = new ArrayList<>(count + 1);

        for (int step = 0; step <= count; step++) {
            spaced.add(pointAt(polyline, total * step / count));
        }
        return spaced;
    }

    public static List<PathPoint> resampleTo(List<PathPoint> polyline, int count) {
        if (polyline.isEmpty() || count < 2) return List.copyOf(polyline);

        double total = length(polyline);
        List<PathPoint> spaced = new ArrayList<>(count);

        for (int step = 0; step < count; step++) {
            spaced.add(pointAt(polyline, total * step / (count - 1)));
        }
        return spaced;
    }

    public static double length(List<PathPoint> polyline) {
        double total = 0.0;
        for (int i = 1; i < polyline.size(); i++) total += polyline.get(i).distanceTo(polyline.get(i - 1));
        return total;
    }

    public static PathPoint pointAt(List<PathPoint> polyline, double distance) {
        if (polyline.isEmpty()) return PathPoint.ORIGIN;
        if (distance <= 0.0) return polyline.get(0);

        double walked = 0.0;
        for (int i = 1; i < polyline.size(); i++) {
            PathPoint from = polyline.get(i - 1);
            PathPoint to = polyline.get(i);
            double span = from.distanceTo(to);
            if (span < EPSILON) continue;

            if (walked + span >= distance) return from.lerp(to, (distance - walked) / span);
            walked += span;
        }
        return polyline.get(polyline.size() - 1);
    }

    public static double distanceAlong(List<PathPoint> polyline, PathPoint target) {
        double nearest = Double.MAX_VALUE;
        double along = 0.0;
        double walked = 0.0;

        for (int i = 1; i < polyline.size(); i++) {
            PathPoint from = polyline.get(i - 1);
            PathPoint to = polyline.get(i);
            double span = from.distanceTo(to);
            if (span < EPSILON) continue;

            PathPoint run = to.minus(from);
            PathPoint reach = target.minus(from);
            double t = Math.max(0.0, Math.min(1.0, (reach.x() * run.x() + reach.y() * run.y()) / (span * span)));
            double gap = from.lerp(to, t).distanceTo(target);

            if (gap < nearest - EPSILON) {
                nearest = gap;
                along = walked + span * t;
            }
            walked += span;
        }
        return along;
    }

    public static boolean leads(List<PathPoint> lead, List<PathPoint> whole, double tolerance) {
        if (lead.isEmpty() || whole.isEmpty()) return false;

        double span = length(lead);
        if (span > length(whole) + tolerance) return false;

        for (int probe = 0; probe <= LEAD_PROBES; probe++) {
            double at = span * probe / LEAD_PROBES;
            if (pointAt(lead, at).distanceTo(pointAt(whole, at)) > tolerance) return false;
        }
        return true;
    }

    public static List<PathPoint> truncate(List<PathPoint> polyline, double maxLength) {
        if (polyline.size() < 2) return List.copyOf(polyline);
        if (maxLength <= 0.0) return List.of(polyline.get(0));

        List<PathPoint> cut = new ArrayList<>();
        cut.add(polyline.get(0));

        double walked = 0.0;
        for (int i = 1; i < polyline.size(); i++) {
            PathPoint from = polyline.get(i - 1);
            PathPoint to = polyline.get(i);
            double span = from.distanceTo(to);
            if (span < EPSILON) continue;

            if (walked + span >= maxLength) {
                cut.add(from.lerp(to, (maxLength - walked) / span));
                return cut;
            }

            cut.add(to);
            walked += span;
        }
        return cut;
    }

    public static List<PathPoint> tail(List<PathPoint> polyline, double fromLength) {
        if (polyline.size() < 2) return List.of();
        if (fromLength <= 0.0) return List.copyOf(polyline);

        double total = length(polyline);
        if (fromLength >= total) return List.of();

        List<PathPoint> rest = new ArrayList<>();
        rest.add(pointAt(polyline, fromLength));

        double walked = 0.0;
        for (int i = 1; i < polyline.size(); i++) {
            PathPoint from = polyline.get(i - 1);
            PathPoint to = polyline.get(i);
            double span = from.distanceTo(to);
            if (span < EPSILON) continue;

            walked += span;
            if (walked > fromLength) rest.add(to);
        }
        return rest;
    }

    public static List<PathPoint> rotate(List<PathPoint> polyline, double radians) {
        if (Math.abs(radians) < EPSILON) return List.copyOf(polyline);

        List<PathPoint> turned = new ArrayList<>(polyline.size());
        for (PathPoint point : polyline) turned.add(point.rotate(radians));
        return turned;
    }

    public static List<PathPoint> translate(List<PathPoint> polyline, double dx, double dy) {
        List<PathPoint> moved = new ArrayList<>(polyline.size());
        for (PathPoint point : polyline) moved.add(point.plus(dx, dy));
        return moved;
    }

    public static List<PathPoint> scale(List<PathPoint> polyline, double factor) {
        List<PathPoint> scaled = new ArrayList<>(polyline.size());
        for (PathPoint point : polyline) scaled.add(point.scale(factor));
        return scaled;
    }

    public static List<PathPoint> morph(List<PathPoint> from, List<PathPoint> to, double t) {
        if (from.isEmpty()) return List.copyOf(to);
        if (to.isEmpty()) return List.copyOf(from);
        if (t <= 0.0) return List.copyOf(from);
        if (t >= 1.0) return List.copyOf(to);

        List<PathPoint> start = resampleTo(from, MORPH_SAMPLES);
        List<PathPoint> finish = resampleTo(to, MORPH_SAMPLES);
        int count = Math.min(start.size(), finish.size());

        List<PathPoint> blended = new ArrayList<>(count);
        for (int i = 0; i < count; i++) blended.add(start.get(i).lerp(finish.get(i), t));
        return blended;
    }

    public record Fit(double scale, double offsetX, double offsetY, double width, double height) {
        public PathPoint apply(PathPoint point) {
            return new PathPoint(offsetX + point.x() * scale, offsetY + point.y() * scale);
        }

        public List<PathPoint> apply(List<PathPoint> polyline) {
            List<PathPoint> placed = new ArrayList<>(polyline.size());
            for (PathPoint point : polyline) placed.add(apply(point));
            return placed;
        }
    }

    public static Fit fit(List<PathPoint> polyline, double boxWidth, double boxHeight, boolean withOrigin) {
        double lowX = withOrigin ? 0.0 : Double.MAX_VALUE;
        double highX = withOrigin ? 0.0 : -Double.MAX_VALUE;
        double lowY = lowX;
        double highY = highX;

        for (PathPoint point : polyline) {
            lowX = Math.min(lowX, point.x());
            highX = Math.max(highX, point.x());
            lowY = Math.min(lowY, point.y());
            highY = Math.max(highY, point.y());
        }

        if (lowX > highX || lowY > highY) return new Fit(1.0, 0.0, 0.0, 0.0, 0.0);

        double spanX = Math.max(EPSILON, highX - lowX);
        double spanY = Math.max(EPSILON, highY - lowY);
        double scale = Math.min(boxWidth / spanX, boxHeight / spanY);

        return new Fit(scale, -lowX * scale, -lowY * scale, spanX * scale, spanY * scale);
    }

    public static double ease(double t) {
        double clamped = Math.max(0.0, Math.min(1.0, t));
        return clamped * clamped * (3.0 - 2.0 * clamped);
    }

    private static List<PathPoint> withoutRepeats(List<PathPoint> waypoints) {
        List<PathPoint> unique = new ArrayList<>(waypoints.size());
        for (PathPoint point : waypoints) {
            if (unique.isEmpty() || unique.get(unique.size() - 1).distanceTo(point) > EPSILON) unique.add(point);
        }
        return unique;
    }

    private static PathPoint catmullRom(PathPoint before, PathPoint from, PathPoint to, PathPoint after, double t) {
        double t2 = t * t;
        double t3 = t2 * t;
        return new PathPoint(
                cubic(before.x(), from.x(), to.x(), after.x(), t, t2, t3),
                cubic(before.y(), from.y(), to.y(), after.y(), t, t2, t3));
    }

    private static double cubic(double before, double from, double to, double after, double t, double t2, double t3) {
        return 0.5 * ((2.0 * from)
                + (-before + to) * t
                + (2.0 * before - 5.0 * from + 4.0 * to - after) * t2
                + (-before + 3.0 * from - 3.0 * to + after) * t3);
    }
}
