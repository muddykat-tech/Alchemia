package muddykat.alchemia.common.potion.map;

import muddykat.alchemia.common.items.helper.PathPoint;

import java.util.List;
import java.util.Random;
import java.util.function.BooleanSupplier;

public final class MapWall {

    private static final int SAMPLES = 720;
    private static final double FILL = 0.9;
    private static final int[] HARMONICS = {2, 3, 5, 7, 11};
    private static final double[] WOBBLE = {0.035, 0.03, 0.022, 0.014, 0.009};
    private static final int SMOOTHING = 3;
    private static final int WINDOW = 6;
    private static final int GROWTH_STEPS = 16;

    private final int middle;
    private final double clearance;
    private final double cell;
    private double[] radii;
    private double min;
    private double max;

    public MapWall(int middle, double clearance, double cell) {
        this.middle = middle;
        this.clearance = clearance;
        this.cell = cell;
    }

    public boolean raised() {
        return radii != null;
    }

    public boolean inside(double x, double y) {
        if (radii == null) return true;
        double dx = x - middle;
        double dy = y - middle;
        double distance = dx * dx + dy * dy;
        if (distance <= min * min) return true;
        if (distance > max * max) return false;
        return Math.sqrt(distance) <= radius(Math.atan2(dy, dx));
    }

    public double radius(double angle) {
        if (radii == null) return squareLimit(angle);
        double t = (angle + Math.PI) / (2 * Math.PI) * SAMPLES;
        int index = (int) Math.floor(t);
        double fraction = t - index;
        double from = radii[Math.floorMod(index, SAMPLES)];
        double to = radii[Math.floorMod(index + 1, SAMPLES)];
        return from + (to - from) * fraction;
    }

    public double reach() {
        return radii == null ? middle * Math.sqrt(2) : max;
    }

    public void raise(Random rand, List<PathPoint> effects, BooleanSupplier settled) {
        double[] shape = shape(rand, effects);

        for (int growth = 0; growth <= GROWTH_STEPS; growth++) {
            double[] trial = new double[SAMPLES];
            for (int index = 0; index < SAMPLES; index++) {
                trial[index] = Math.min(squareLimit(angle(index)), shape[index] + growth * cell);
            }
            build(trial);
            if (settled.getAsBoolean()) return;
        }
        radii = null;
    }

    private static double angle(int index) {
        return -Math.PI + index * 2 * Math.PI / SAMPLES;
    }

    private double squareLimit(double angle) {
        return (middle - 1) / Math.max(Math.abs(Math.cos(angle)), Math.abs(Math.sin(angle)));
    }

    private void build(double[] trial) {
        radii = trial;
        min = Double.MAX_VALUE;
        max = 0.0;
        for (double radius : trial) {
            min = Math.min(min, radius);
            max = Math.max(max, radius);
        }
    }

    private double[] shape(Random rand, List<PathPoint> effects) {
        double[] phases = new double[HARMONICS.length];
        for (int index = 0; index < phases.length; index++) phases[index] = rand.nextDouble() * 2 * Math.PI;

        double[] need = new double[SAMPLES];
        for (PathPoint effect : effects) {
            double dx = effect.x() - middle;
            double dy = effect.y() - middle;
            double distance = Math.sqrt(dx * dx + dy * dy);
            double bearing = Math.atan2(dy, dx);
            for (int index = 0; index < SAMPLES; index++) {
                double turn = Math.IEEEremainder(angle(index) - bearing, 2 * Math.PI);
                double side = distance * Math.sin(turn);
                if (Math.cos(turn) <= 0.0 || Math.abs(side) >= clearance) continue;
                need[index] = Math.max(need[index], distance * Math.cos(turn) + Math.sqrt(clearance * clearance - side * side));
            }
        }

        double[] raw = new double[SAMPLES];
        for (int index = 0; index < SAMPLES; index++) {
            double angle = angle(index);
            double wobble = 1.0;
            for (int harmonic = 0; harmonic < HARMONICS.length; harmonic++) {
                wobble += WOBBLE[harmonic] * Math.sin(HARMONICS[harmonic] * angle + phases[harmonic]);
            }
            raw[index] = Math.max(middle * FILL * wobble, need[index]);
        }

        for (int pass = 0; pass < SMOOTHING; pass++) {
            double[] smoothed = new double[SAMPLES];
            for (int index = 0; index < SAMPLES; index++) {
                double total = 0.0;
                for (int offset = -WINDOW; offset <= WINDOW; offset++) total += raw[Math.floorMod(index + offset, SAMPLES)];
                smoothed[index] = Math.max(total / (2 * WINDOW + 1), need[index]);
            }
            raw = smoothed;
        }
        return raw;
    }
}
