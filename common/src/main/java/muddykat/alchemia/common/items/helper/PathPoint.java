package muddykat.alchemia.common.items.helper;

public record PathPoint(double x, double y) {

    public static final PathPoint ORIGIN = new PathPoint(0.0, 0.0);

    public static PathPoint of(double x, double y) {
        return new PathPoint(x, y);
    }

    public static PathPoint polar(double angle, double radius) {
        return new PathPoint(Math.cos(angle) * radius, Math.sin(angle) * radius);
    }

    public PathPoint plus(PathPoint other) {
        return new PathPoint(x + other.x, y + other.y);
    }

    public PathPoint plus(double dx, double dy) {
        return new PathPoint(x + dx, y + dy);
    }

    public PathPoint minus(PathPoint other) {
        return new PathPoint(x - other.x, y - other.y);
    }

    public PathPoint scale(double factor) {
        return new PathPoint(x * factor, y * factor);
    }

    public PathPoint rotate(double radians) {
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new PathPoint(x * cos - y * sin, x * sin + y * cos);
    }

    public PathPoint lerp(PathPoint target, double t) {
        return new PathPoint(x + (target.x - x) * t, y + (target.y - y) * t);
    }

    public PathPoint normalised() {
        double length = length();
        return length < 1.0E-6 ? ORIGIN : scale(1.0 / length);
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    public double distanceTo(PathPoint other) {
        return minus(other).length();
    }

    public double distanceTo(double otherX, double otherY) {
        double dx = x - otherX;
        double dy = y - otherY;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public double angle() {
        return Math.atan2(y, x);
    }

    public boolean isZero() {
        return length() < 1.0E-6;
    }

    public int roundedX() {
        return (int) Math.round(x);
    }

    public int roundedY() {
        return (int) Math.round(y);
    }
}
