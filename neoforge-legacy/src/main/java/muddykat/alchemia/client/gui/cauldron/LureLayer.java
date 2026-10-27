package muddykat.alchemia.client.gui.cauldron;

import muddykat.alchemia.client.gui.PathRenderer;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.helper.PathGeometry;
import muddykat.alchemia.common.items.helper.PathPoint;
import muddykat.alchemia.common.potion.AlchemiaEffect;
import muddykat.alchemia.common.potion.BrewCourse;
import muddykat.alchemia.common.potion.LureReach;
import muddykat.alchemia.common.potion.PotionMap;
import muddykat.alchemia.common.potion.VanillaIngredients;
import net.minecraft.client.gui.GuiGraphics;
import muddykat.alchemia.client.effect.ARGB;
import net.minecraft.Util;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class LureLayer {

    public static final double INK_MIX = 0.45;
    public static final int GHOST_ALPHA = 70;

    private static final long BEND_MS = 650L;
    private static final long FIELD_MS = 1800L;
    private static final int FIELD_RINGS = 3;
    private static final int FIELD_ALPHA = 170;
    private static final int QUEUED_ALPHA = 70;
    private static final double TETHER_SPACING = PotionMap.CELL;
    private static final double TETHER_MIN_PIXELS = 1.5;
    private static final int TETHER_ALPHA = 130;
    private static final int BRACKET_ALPHA = 255;
    private static final int PREVIEW_ALPHA = 200;
    private static final double BRACKET_MIX = 0.25;
    private static final int FOCUS_ALPHA = 60;
    private static final int CELL_ALPHA = 45;
    private static final int POINTER_INSET = 10;
    private static final double POINTER_SIZE = 5.0;

    private record Field(double strength, boolean pending) {
    }

    private record Bracket(int first, int last, int argb, int tint) {
    }

    private final BrewContext context;
    private final HeatGauge.Sounds sounds;

    private @Nullable Object bendKey;
    private boolean reachSounded;
    private long bendSince;
    private @Nullable Object trialKey;
    private BrewCourse trial;
    private LureReach.Span trialSpan;

    public LureLayer(BrewContext context, HeatGauge.Sounds sounds) {
        this.context = context;
        this.sounds = sounds;
    }

    public @Nullable LureReach.Span pending() {
        ItemStack pending = context.pendingIngredient();
        if (!PotionMap.isReady() || !VanillaIngredients.isLure(pending)) return null;

        BrewCourse course = context.course();
        PotionMap map = context.map();
        List<Object> key = List.of(pending.getItem(), ItemIngredient.crushOf(pending), course.entries(),
                course.anchors(), course.spent(), course.started(), map);
        if (!key.equals(trialKey)) {
            trialKey = key;
            trial = course.copy();
            trial.add(pending);
            trialSpan = LureReach.pending(course, map, pending);
        }
        return trialSpan;
    }

    public List<LureReach.Span> active() {
        if (!PotionMap.isReady()) return List.of();

        BrewCourse course = context.course();
        List<LureReach.Span> active = new ArrayList<>();
        for (LureReach.Span span : LureReach.spans(course.entries(), course.anchors(), course.anchorFrom(),
                course.legs(context.map()))) {
            if (span.lure() >= course.spent()) active.add(span);
        }
        return active;
    }

    public int tint(AlchemiaEffect target) {
        return context.knowledge().knows(target) ? target.color() : MapInk.UNKNOWN_RGB;
    }

    public void forgetBend() {
        bendKey = null;
        reachSounded = false;
    }

    public void drawFields(GuiGraphics graphics) {
        Map<AlchemiaEffect, Field> fields = new LinkedHashMap<>();
        for (LureReach.Span span : active()) {
            if (Math.max(span.from(), context.course().spent()) >= span.lure()) continue;
            fields.merge(span.target(), new Field(span.strength(), false),
                    (a, b) -> new Field(Math.max(a.strength(), b.strength()), a.pending() || b.pending()));
        }

        LureReach.Span pending = pending();
        if (pending != null) {
            fields.merge(pending.target(), new Field(pending.strength(), true),
                    (a, b) -> new Field(Math.max(a.strength(), b.strength()), true));
        }

        fields.forEach((target, field) -> drawField(graphics, target, field.strength(),
                field.pending() ? FIELD_ALPHA : QUEUED_ALPHA, field.pending()));
    }

    public void drawPending(GuiGraphics graphics, ItemStack pending, int node, long millis) {
        LureReach.Span span = pending();
        if (span == null) return;

        BrewCourse course = context.course();
        MapView view = context.view();
        List<Object> key = List.of(pending.getItem(), course.entries().size(), course.spent(), course.started());
        if (!key.equals(bendKey)) {
            bendKey = key;
            bendSince = millis;
            reachSounded = false;
        }

        double bend = PathGeometry.ease(Math.min(1.0, (millis - bendSince) / (double) BEND_MS));
        int tint = tint(span.target());
        int ink = MapInk.mix(MapInk.PATH_RGB, tint, INK_MIX);
        int tetherAlpha = (int) Math.round(TETHER_ALPHA * Math.min(1.0, bend * 1.5) * (0.6 + 0.4 * breath(millis)));

        List<PotionMap.RouteLeg> before = course.legs(context.map());
        List<PotionMap.RouteLeg> after = trial.legs(context.map());
        PathPoint tip = null;

        for (int index = Math.max(span.from(), course.spent()); index < span.lure(); index++) {
            PotionMap.RouteLeg plain = before.get(index);
            PotionMap.RouteLeg bent = after.get(index);
            boolean teleport = bent.path().teleport();
            boolean flight = teleport && bent.curve().size() > 1 && plain.curve().size() > 1;

            List<PathPoint> from = flight ? plain.curve() : plain.path().points();
            List<PathPoint> to = flight ? bent.curve() : bent.path().points();
            if (!teleport && index == course.spent() && course.started()) {
                from = PathGeometry.tail(from, course.progress());
                to = PathGeometry.tail(to, course.progress());
            }
            if (to.isEmpty()) continue;

            List<PathPoint> moving = new ArrayList<>(to.size());
            for (int step = 0; step < to.size(); step++) {
                PathPoint origin = from.isEmpty() ? to.get(step) : from.get(Math.min(step, from.size() - 1));
                moving.add(lerp(origin, to.get(step), bend));
            }

            drawTethers(graphics, view, from, moving, ARGB.color(tetherAlpha, tint));

            List<PathPoint> screen = view.toScreen(moving);
            if (screen.size() > 1) {
                PathRenderer.dashes(graphics, screen, MapInk.PATH_THICKNESS, ARGB.color(MapInk.PATH_ALPHA, ink), teleport,
                        0.0, view.clip(), view.pathScale());
            }
            tip = screen.get(screen.size() - 1);

            if (flight) {
                PathPoint landing = view.toScreen(lerp(plain.path().end(), bent.path().end(), bend));
                if (tip.distanceTo(landing) > 0.5) {
                    PathRenderer.dashes(graphics, List.of(tip, landing), MapInk.PATH_THICKNESS, ARGB.color(MapInk.PATH_ALPHA, ink), true,
                            0.0, view.clip(), view.pathScale());
                }
                tip = landing;
            }
        }

        if (tip == null) return;

        if (!reachSounded && bend >= 1.0 && span.lure() > Math.max(span.from(), course.spent())) {
            PathPoint goal = context.map().positionOf(span.target());
            PathPoint landing = after.get(span.lure() - 1).path().end();
            if (goal != null && landing.distanceTo(goal) <= PotionMap.EFFECT_RADIUS) {
                reachSounded = true;
                sounds.play(SoundEvents.AMETHYST_BLOCK_RESONATE, 1.4F, 0.35F);
            }
        }

        PathRenderer.ending(graphics, tip.x(), tip.y(), Math.max(3, node / 3.0), 1, ARGB.color(235, MapInk.PENDING_RGB), false);
    }

    public void drawBrackets(GuiGraphics graphics, int gridX, int gridY, int queuedCount, int hoveredCell) {
        if (!PotionMap.isReady()) return;

        int spent = context.course().spent();
        int capacity = QueueTimeline.COLUMNS * QueueTimeline.ROWS;
        List<Bracket> brackets = new ArrayList<>();

        for (LureReach.Span span : active()) {
            int lure = span.lure() - spent;
            if (lure >= capacity) continue;
            brackets.add(new Bracket(Math.max(span.from(), spent) - spent, lure,
                    bracketInk(span.target(), BRACKET_ALPHA), tint(span.target())));
        }

        LureReach.Span pending = pending();
        if (pending != null) {
            int lure = pending.lure() - spent;
            if (lure >= queuedCount && lure < capacity) {
                int x = QueueTimeline.cellX(gridX, lure);
                int y = QueueTimeline.cellY(gridY, lure);
                graphics.fill(x + 1, y + 1, x + QueueTimeline.CELL - 1, y + QueueTimeline.CELL - 1,
                        ARGB.color(CELL_ALPHA, tint(pending.target())));

                int alpha = (int) Math.round(PREVIEW_ALPHA * (0.7 + 0.3 * breath(Util.getMillis())));
                brackets.add(new Bracket(Math.max(pending.from(), spent) - spent, lure,
                        bracketInk(pending.target(), alpha), tint(pending.target())));
            }
        }

        for (Bracket bracket : brackets) {
            if (bracket.last() != hoveredCell) continue;
            for (int cell = Math.max(0, bracket.first()); cell < bracket.last(); cell++) {
                int x = QueueTimeline.cellX(gridX, cell);
                int y = QueueTimeline.cellY(gridY, cell);
                graphics.fill(x + 1, y + 1, x + QueueTimeline.CELL - 1, y + QueueTimeline.CELL - 1,
                        ARGB.color(FOCUS_ALPHA, bracket.tint()));
            }
        }

        for (Bracket bracket : brackets) bracket(graphics, gridX, gridY, bracket.first(), bracket.last(), bracket.argb());
    }

    private void drawField(GuiGraphics graphics, AlchemiaEffect target, double strength, int alpha, boolean pointer) {
        PathPoint goal = context.map().positionOf(target);
        if (goal == null) return;

        MapView view = context.view();
        PathPoint centre = view.toScreen(goal);
        int tint = tint(target);
        double inner = PotionMap.EFFECT_RADIUS * view.zoom();
        double outer = (PotionMap.EFFECT_RADIUS + PotionMap.LURE_PULL * strength) * view.zoom();

        int left = view.left();
        int top = view.top();
        int right = view.right();
        int bottom = view.bottom();

        boolean onView = centre.x() >= left && centre.x() <= right && centre.y() >= top && centre.y() <= bottom;
        boolean touchesView = centre.x() + outer >= left && centre.x() - outer <= right
                && centre.y() + outer >= top && centre.y() - outer <= bottom;

        if (touchesView && outer >= 2.0) {
            long millis = Util.getMillis();
            PathRenderer.ring(graphics, centre.x(), centre.y(), outer, ARGB.color(alpha / 3, tint), true);

            for (int ring = 0; ring < FIELD_RINGS; ring++) {
                double phase = ((millis % FIELD_MS) / (double) FIELD_MS + ring / (double) FIELD_RINGS) % 1.0;
                double radius = outer - (outer - inner) * phase;
                int fade = (int) Math.round(alpha * Math.sin(Math.PI * phase));
                if (fade > 0) PathRenderer.ring(graphics, centre.x(), centre.y(), radius, ARGB.color(fade, tint), false);
            }
        }

        if (pointer && !onView) drawOffViewPointer(graphics, view, centre, tint);
    }

    private static void drawOffViewPointer(GuiGraphics graphics, MapView view, PathPoint target, int tint) {
        double cx = view.centreX();
        double cy = view.centreY();
        double dx = target.x() - cx;
        double dy = target.y() - cy;
        if (Math.abs(dx) < 1.0E-6 && Math.abs(dy) < 1.0E-6) return;

        double halfW = view.width() / 2.0 - POINTER_INSET;
        double halfH = view.height() / 2.0 - POINTER_INSET;
        double scale = Math.min(halfW / Math.max(Math.abs(dx), 1.0E-6), halfH / Math.max(Math.abs(dy), 1.0E-6));
        double angle = Math.atan2(dy, dx);
        double nudge = breath(Util.getMillis()) * 2.0;

        double x = cx + dx * scale - Math.cos(angle) * nudge;
        double y = cy + dy * scale - Math.sin(angle) * nudge;
        int colour = ARGB.color(230, MapInk.mix(MapInk.PATH_RGB, tint, 0.6));

        PathRenderer.chevron(graphics, x, y, angle, POINTER_SIZE, 1.5, colour);
        PathRenderer.chevron(graphics, x - Math.cos(angle) * 4, y - Math.sin(angle) * 4, angle, POINTER_SIZE, 1.0,
                ARGB.color(140, MapInk.mix(MapInk.PATH_RGB, tint, 0.6)));
    }

    private static void drawTethers(GuiGraphics graphics, MapView view, List<PathPoint> from, List<PathPoint> to, int argb) {
        if (from.isEmpty() || ARGB.alpha(argb) <= 0) return;

        double travelled = 0.0;
        double next = TETHER_SPACING / 2.0;

        for (int step = 0; step < to.size(); step++) {
            PathPoint origin = from.get(Math.min(step, from.size() - 1));
            if (step > 0) travelled += origin.distanceTo(from.get(Math.min(step - 1, from.size() - 1)));
            if (travelled < next) continue;
            next += TETHER_SPACING;

            PathPoint a = view.toScreen(origin);
            PathPoint b = view.toScreen(to.get(step));
            if (a.distanceTo(b) < TETHER_MIN_PIXELS) continue;

            PathRenderer.line(graphics, a.x(), a.y(), b.x(), b.y(), 1.0, argb, true);
        }
    }

    private int bracketInk(AlchemiaEffect target, int alpha) {
        return ARGB.color(alpha, MapInk.mix(MapInk.PATH_RGB, tint(target), BRACKET_MIX));
    }

    private static void bracket(GuiGraphics graphics, int gridX, int gridY, int first, int last, int argb) {
        first = Math.max(0, Math.min(first, last));

        for (int cell = first; cell <= last; cell++) {
            int x = QueueTimeline.cellX(gridX, cell);
            int y = QueueTimeline.cellY(gridY, cell);
            int x0 = cell == first ? x + 1 : x;
            int x1 = cell == last ? x + QueueTimeline.CELL - 1 : x + QueueTimeline.CELL;
            int base = y + QueueTimeline.CELL - 1;

            graphics.fill(x0, base, x1, base + 1, argb);
            if (cell == first) graphics.fill(x0, base - 3, x0 + 1, base, argb);
            if (cell == last) graphics.fill(x1 - 1, base - 3, x1, base, argb);
        }
    }

    private static PathPoint lerp(PathPoint from, PathPoint to, double amount) {
        return from.plus(to.minus(from).scale(amount));
    }

    private static double breath(long millis) {
        return (Math.sin(millis / 420.0) + 1.0) * 0.5;
    }
}
