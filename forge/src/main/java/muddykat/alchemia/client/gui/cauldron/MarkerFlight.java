package muddykat.alchemia.client.gui.cauldron;

import muddykat.alchemia.client.gui.PathRenderer;
import muddykat.alchemia.common.items.helper.PathGeometry;
import muddykat.alchemia.common.items.helper.PathPoint;
import muddykat.alchemia.common.potion.BrewCourse;
import muddykat.alchemia.common.potion.PotionMap;
import net.minecraft.client.gui.GuiGraphics;
import muddykat.alchemia.client.effect.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.Util;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class MarkerFlight {

    private static final double HOP_DISTANCE = PotionMap.CELL;
    private static final double CHASE_MS = 45.0;
    private static final double CHARGE_SHRINK = 0.4;
    private static final long VANISH_MS = 220L;
    private static final long APPEAR_MS = 480L;
    private static final long FLIGHT_MIN_MS = 350L;
    private static final long FLIGHT_MAX_MS = 1400L;
    private static final double FLIGHT_PER_MS = 0.45;
    private static final double APPEAR_TILT = 0.75 * Math.PI;
    private static final double APPEAR_DAMPING = 5.5;
    private static final double APPEAR_SWINGS = 3.0;
    private static final double TRAIL = 3.0 * PotionMap.CELL;
    private static final int TRAIL_STEPS = 10;

    public record Pose(double x, double y, double scale, double spin) {
    }

    private final BrewContext context;

    private @Nullable PathPoint at;
    private PathPoint hopFrom = PathPoint.ORIGIN;
    private long hopStart = Long.MIN_VALUE;
    private double hopCharge;
    private double chargeShown;
    private List<PathPoint> plan = List.of();
    private List<PathPoint> flight = List.of();
    private double length;
    private long flightMs;
    private boolean camHeld;
    private double camX;
    private double camY;
    private long frame;
    private double frameMs;

    public MarkerFlight(BrewContext context) {
        this.context = context;
    }

    public @Nullable PathPoint at() {
        return at;
    }

    public double frameMs() {
        return frameMs;
    }

    public boolean inFlight() {
        return elapsed() < VANISH_MS + flightMs + APPEAR_MS;
    }

    public void track(long now) {
        PathPoint brewAt = context.brewAt();
        BrewCourse course = context.course();
        double blend = Math.min(1.0, (now - frame) / CHASE_MS);
        PotionMap.RouteLeg leg = PotionMap.isReady() ? course.current(context.map()) : null;
        boolean winding = leg != null && leg.path().teleport() && !leg.path().stalled();
        double charge = winding && course.started()
                ? Math.min(1.0, course.progress() / BrewCourse.TELEPORT_STIR)
                : 0.0;

        if (at == null) {
            at = brewAt;
        } else if (at.distanceTo(brewAt) > HOP_DISTANCE) {
            if (plan.isEmpty()) plan = PotionMap.isReady() ? WhirlpoolLayer.flight(context.map(), at, brewAt) : List.of();
            start(now, brewAt);
            at = brewAt;
        } else {
            at = at.lerp(brewAt, blend);
        }

        plan = winding ? flightOf(leg) : List.of();
        chargeShown = charge > chargeShown ? chargeShown + (charge - chargeShown) * blend : charge;
        frameMs = now - frame;
        frame = now;
    }

    public void steer(MapCamera camera, boolean stirring) {
        if (inFlight()) {
            follow(camera);
            return;
        }
        if (stirring) camera.keepNear(at);
    }

    public Pose pose() {
        long elapsed = elapsed();

        if (elapsed < VANISH_MS) {
            double k = (double) elapsed / VANISH_MS;
            return new Pose(hopFrom.x(), hopFrom.y(), (1.0 - hopCharge * CHARGE_SHRINK) * (1.0 - k),
                    (hopCharge * 2 + k * 4) * Math.PI);
        }

        if (elapsed < VANISH_MS + flightMs) return new Pose(at.x(), at.y(), 0.0, 0.0);

        if (elapsed < VANISH_MS + flightMs + APPEAR_MS) {
            double t = (double) (elapsed - VANISH_MS - flightMs) / APPEAR_MS;
            double swing = Math.exp(-APPEAR_DAMPING * t) * Math.cos(APPEAR_SWINGS * Math.PI * t) * (1.0 - t);
            return new Pose(at.x(), at.y(), 1.0 - swing, APPEAR_TILT * swing);
        }

        return new Pose(at.x(), at.y(), 1.0 - chargeShown * CHARGE_SHRINK, chargeShown * 2 * Math.PI);
    }

    public void drawTrail(GuiGraphics graphics, MapView view) {
        long elapsed = elapsed();
        if (flight.size() < 2 || elapsed >= VANISH_MS + flightMs) return;

        double walked = elapsed < VANISH_MS ? 0.0 : flown();
        double trail = Math.min(TRAIL, walked);

        for (int step = 0; step < TRAIL_STEPS && trail > 0.0; step++) {
            double from = walked - trail + trail * step / TRAIL_STEPS;
            double to = walked - trail + trail * (step + 1) / TRAIL_STEPS;
            int alpha = (int) Math.round(MapInk.PATH_ALPHA * (step + 0.5) / TRAIL_STEPS);
            drawPiece(graphics, view, PathGeometry.truncate(PathGeometry.tail(flight, from), to - from), from, alpha);
        }

        drawPiece(graphics, view, PathGeometry.tail(flight, walked), walked, MapInk.PATH_ALPHA);
    }

    private static List<PathPoint> flightOf(PotionMap.RouteLeg leg) {
        List<PathPoint> course = new ArrayList<>(leg.curve().size() > 1 ? leg.curve() : leg.path().points());
        PathPoint landing = leg.path().end();

        if (course.isEmpty() || course.get(course.size() - 1).distanceTo(landing) > 0.01) course.add(landing);
        return course;
    }

    private void start(long now, PathPoint brewAt) {
        boolean flown = plan.size() > 1 && plan.get(plan.size() - 1).distanceTo(brewAt) <= HOP_DISTANCE;

        hopFrom = at;
        hopCharge = chargeShown;
        flight = flown ? plan : List.of();
        length = PathGeometry.length(flight);
        flightMs = flown
                ? (long) Mth.clamp(Math.round(length / FLIGHT_PER_MS), FLIGHT_MIN_MS, FLIGHT_MAX_MS)
                : 0L;
        hopStart = flown ? now : now - VANISH_MS;
        camHeld = false;
    }

    private long elapsed() {
        return hopStart == Long.MIN_VALUE ? Long.MAX_VALUE : Util.getMillis() - hopStart;
    }

    private double flown() {
        if (flightMs <= 0L) return length;

        double travelled = (double) (elapsed() - VANISH_MS) / flightMs;
        return length * PathGeometry.ease(travelled);
    }

    private void follow(MapCamera camera) {
        long elapsed = elapsed();
        if (flight.size() < 2 || elapsed < VANISH_MS || elapsed >= VANISH_MS + flightMs + APPEAR_MS) return;

        if (!camHeld) {
            camX = camera.x() - flight.get(0).x();
            camY = camera.y() - flight.get(0).y();
            camHeld = true;
        }

        double walked = flown();
        double held = length <= 0.0 ? 0.0 : 1.0 - walked / length;
        PathPoint point = PathGeometry.pointAt(flight, walked);
        camera.moveTo(point.x() + camX * held, point.y() + camY * held);
    }

    private static void drawPiece(GuiGraphics graphics, MapView view, List<PathPoint> piece, double from, int alpha) {
        if (piece.size() < 2 || alpha <= 0) return;
        PathRenderer.dashes(graphics, view.toScreen(piece), MapInk.PATH_THICKNESS, ARGB.color(alpha, MapInk.PATH_RGB), true,
                from * view.zoom(), view.clip(), view.pathScale());
    }
}
