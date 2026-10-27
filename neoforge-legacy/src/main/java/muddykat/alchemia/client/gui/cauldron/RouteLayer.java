package muddykat.alchemia.client.gui.cauldron;

import muddykat.alchemia.client.gui.PathAnimator;
import muddykat.alchemia.client.gui.PathRenderer;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.items.helper.PathGeometry;
import muddykat.alchemia.common.items.helper.PathPoint;
import muddykat.alchemia.common.potion.BrewCourse;
import muddykat.alchemia.common.potion.LureReach;
import muddykat.alchemia.common.potion.PotionMap;
import muddykat.alchemia.common.potion.VanillaIngredients;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import muddykat.alchemia.client.effect.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;

import java.util.List;

public final class RouteLayer {

    private final BrewContext context;
    private final LureLayer lures;
    private final PathAnimator pendingFull = new PathAnimator();
    private final PathAnimator pendingLive = new PathAnimator();

    private @Nullable Object planKey;
    private List<PathPoint> planCommitted = List.of();
    private List<PathPoint> planPotential = List.of();
    private PathPoint planStart = PathPoint.ORIGIN;
    private boolean planTeleport;

    public RouteLayer(BrewContext context, LureLayer lures) {
        this.context = context;
        this.lures = lures;
    }

    public void drawPlan(GuiGraphics graphics, int node) {
        BrewCourse course = context.course();
        MapView view = context.view();
        List<PotionMap.RouteLeg> legs = course.legs(context.map());
        int first = course.spent();
        if (first >= legs.size()) return;

        PathPoint at = context.brewAt();
        PathPoint tip = view.toScreen(at);

        boolean stalled = false;
        List<LureReach.Span> active = lures.active();
        LureReach.Span pendingLure = lures.pending();

        for (int index = first; index < legs.size(); index++) {
            PotionMap.RouteLeg leg = index == first ? remainder(course, legs.get(index)) : legs.get(index);
            PotionMap.PotionPath path = leg.path();

            if (path.stalled()) {
                stalled = true;
                if (path.teleport()) drawOffMapAttempt(graphics, view, at.x(), at.y(), leg.stack(), node);
                continue;
            }

            int rgb = MapInk.PATH_RGB;
            for (LureReach.Span span : active) {
                if (span.bends(index)) rgb = MapInk.mix(MapInk.PATH_RGB, lures.tint(span.target()), LureLayer.INK_MIX);
            }
            int alpha = pendingLure != null && pendingLure.bends(index) ? LureLayer.GHOST_ALPHA : MapInk.PATH_ALPHA;

            tip = drawLeg(graphics, view, leg, tip, rgb, alpha,
                    index == first ? course.progress() * view.zoom() : 0.0);
            at = path.end(at.x(), at.y());
        }

        marker(graphics, tip, node, stalled ? MapInk.BLOCKED_RGB : MapInk.GHOST_RGB);
    }

    public void drawPending(GuiGraphics graphics, int node) {
        ItemStack pending = context.pendingIngredient();

        if (!TileEntityAlchemyCauldron.isBrewingInput(pending) || !PotionMap.isReady()) {
            forget();
            lures.forgetBend();
            return;
        }

        long millis = Util.getMillis();
        BrewCourse course = context.course();
        PotionMap map = context.map();
        MapView view = context.view();
        List<ItemStack> queued = course.plannedStacks();

        if (VanillaIngredients.isLure(pending)) {
            forget();
            lures.drawPending(graphics, pending, node, millis);
            return;
        }
        lures.forgetBend();

        List<Object> signature = List.of(pending.getItem(), ItemIngredient.crushOf(pending),
                ItemIngredient.trimOf(pending), ItemIngredient.rotationOf(pending), queued.size());

        PathPoint from = course.end(map);
        List<Object> key = List.of(signature, from, map);

        if (!key.equals(planKey)) {
            PotionMap.PotionPath path = map.walk(from.x(), from.y(), pending);

            planKey = key;
            planStart = from;
            planTeleport = path.teleport();
            planCommitted = List.copyOf(path.teleport()
                    ? flightOrHop(map, path, from.x(), from.y(), pending)
                    : path.points());
            planPotential = List.copyOf(potentialPath(map, pending, from.x(), from.y()));
        }

        boolean teleport = planTeleport;
        List<PathPoint> full = view.toScreen(pendingFull.update(signature, planPotential, millis));
        List<PathPoint> live = view.toScreen(pendingLive.update(signature, planCommitted, millis));

        PathRenderer.Clip clip = view.clip();

        if (full.size() > 1) {
            double walked = PathGeometry.length(live);
            PathRenderer.outline(graphics, PathGeometry.tail(full, walked), MapInk.PATH_THICKNESS,
                    ARGB.color(MapInk.PATH_ALPHA, MapInk.PATH_RGB), teleport, walked, clip, view.pathScale());
            marker(graphics, full.get(full.size() - 1), node, MapInk.PENDING_RGB);
        }

        if (live.size() > 1) {
            PathRenderer.dashes(graphics, live, MapInk.PATH_THICKNESS, ARGB.color(MapInk.PATH_ALPHA, MapInk.PATH_RGB), teleport, 0.0,
                    clip, view.pathScale());
        }

        PathPoint landing = live.isEmpty() ? view.toScreen(planStart) : live.get(live.size() - 1);
        PathRenderer.ending(graphics, landing.x(), landing.y(), Math.max(3, node / 3.0), 1,
                ARGB.color(235, MapInk.PENDING_RGB), teleport);
    }

    private void forget() {
        pendingFull.forget();
        pendingLive.forget();
        planKey = null;
    }

    private static PotionMap.RouteLeg remainder(BrewCourse course, PotionMap.RouteLeg leg) {
        PotionMap.PotionPath path = leg.path();
        if (path.teleport() || path.stalled() || !course.started()) return leg;

        return new PotionMap.RouteLeg(leg.stack(), new PotionMap.PotionPath(
                PathGeometry.tail(path.points(), course.progress()), false, false, path.dead()),
                leg.curve(), leg.lure());
    }

    private static PathPoint drawLeg(GuiGraphics graphics, MapView view, PotionMap.RouteLeg leg, PathPoint from,
                                     int rgb, int alpha, double phase) {
        PotionMap.PotionPath path = leg.path();

        if (!path.teleport()) {
            if (path.points().size() < 2) return from;

            List<PathPoint> screen = view.toScreen(path.points());
            PathRenderer.dashes(graphics, screen, MapInk.PATH_THICKNESS, ARGB.color(alpha, rgb), false, phase, view.clip(), view.pathScale());
            return screen.get(screen.size() - 1);
        }

        PathPoint tip = from;

        if (leg.curve().size() > 1) {
            List<PathPoint> flight = view.toScreen(leg.curve());
            PathRenderer.dashes(graphics, flight, MapInk.PATH_THICKNESS, ARGB.color(alpha, rgb), true, 0.0, view.clip(), view.pathScale());
            tip = flight.get(flight.size() - 1);
        }

        PathPoint landing = view.toScreen(path.end(0, 0));
        PathRenderer.dashes(graphics, List.of(tip, landing), MapInk.PATH_THICKNESS, ARGB.color(alpha, rgb), true, 0.0,
                view.clip(), view.pathScale());
        return landing;
    }

    private void drawOffMapAttempt(GuiGraphics graphics, MapView view, double x, double y, ItemStack stack, int node) {
        List<PathPoint> curve = view.toScreen(context.map().teleportCurve(x, y, stack));
        if (curve.size() < 2) return;

        PathRenderer.dashes(graphics, curve, MapInk.PATH_THICKNESS, ARGB.color(MapInk.PATH_ALPHA, MapInk.BLOCKED_RGB), true, 0.0,
                view.clip(), view.pathScale());

        PathPoint tip = curve.get(curve.size() - 1);
        int half = Math.max(3, node / 3);

        PathRenderer.spiral(graphics, tip.x(), tip.y(), half, 2, ARGB.color(220, MapInk.BLOCKED_RGB));
        drawLabel(graphics, view, Component.translatable("alchemia.gui.offmap"), tip, half, ARGB.opaque(MapInk.BLOCKED_RGB));
    }

    private void drawLabel(GuiGraphics graphics, MapView view, Component label, PathPoint at, int half, int colour) {
        Font font = context.getFont();
        int width = font.width(label);
        int labelX = Mth.clamp(at.roundedX(), view.left() + width / 2 + 2, view.right() - width / 2 - 2);
        graphics.drawString(font, label, labelX - width / 2, at.roundedY() + half + 3, colour, false);
    }

    private static List<PathPoint> flightOrHop(PotionMap map, PotionMap.PotionPath path, double fromX, double fromY, ItemStack stack) {
        PathPoint landing = path.end(fromX, fromY);
        List<PathPoint> flight = map.teleportFlight(fromX, fromY, stack);

        if (path.points().size() > 1 && flight.size() > 1
                && flight.get(flight.size() - 1).distanceTo(landing) < 1.0) return flight;
        return List.of(path.start(), landing);
    }

    private static List<PathPoint> potentialPath(PotionMap map, ItemStack stack, double x, double y) {
        VanillaIngredients.Drift drift = VanillaIngredients.driftOf(stack);
        if (drift != null && !drift.lure()) {
            return map.project(x, y, map.fixedDriftOffsets(drift.target(), Ingredients.MAX_CRUSH_UNITS), true).points();
        }
        if (!(stack.getItem() instanceof ItemIngredient item)) return map.walk(x, y, stack).points();

        double maxPotency = item.getIngredient().getPotency(Ingredients.MAX_CRUSH);
        List<PathPoint> offsets = item.previewPath(maxPotency,
                ItemIngredient.trimOf(stack), ItemIngredient.rotationOf(stack));

        return map.project(x, y, offsets, true).points();
    }

    private static void marker(GuiGraphics graphics, PathPoint at, int node, int rgb) {
        PathRenderer.marker(graphics, at.x(), at.y(), Math.max(3, node / 3), rgb);
    }
}
