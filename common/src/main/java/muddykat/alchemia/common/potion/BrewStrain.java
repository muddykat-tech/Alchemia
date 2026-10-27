package muddykat.alchemia.common.potion;

import muddykat.alchemia.common.items.helper.PathGeometry;

public final class BrewStrain {

    public static final double DRAIN_DISTANCE = PathGeometry.CELL;
    public static final double RECOVER_DELAY = PathGeometry.CELL;
    public static final double RECOVER_DISTANCE = 4 * PathGeometry.CELL;
    private static final double EPSILON = 1.0E-6;

    private double health = 1.0;
    private double calm = RECOVER_DELAY;

    public double health() {
        return health;
    }

    public double calm() {
        return calm;
    }

    public void restore(double health, double calm) {
        this.health = Math.max(0.0, Math.min(1.0, health));
        this.calm = Math.max(0.0, Math.min(RECOVER_DELAY, calm));
    }

    public void reset() {
        health = 1.0;
        calm = RECOVER_DELAY;
    }

    public boolean travel(double distance, boolean dead) {
        if (distance <= 0.0) return false;

        if (dead) {
            calm = 0.0;
            health -= distance / DRAIN_DISTANCE;
            if (health > EPSILON) return false;
            health = 1.0;
            return true;
        }

        double rest = distance;
        if (calm < RECOVER_DELAY) {
            double wait = Math.min(rest, RECOVER_DELAY - calm);
            calm += wait;
            rest -= wait;
        }
        if (rest > 0.0) health = Math.min(1.0, health + rest / RECOVER_DISTANCE);
        return false;
    }
}
