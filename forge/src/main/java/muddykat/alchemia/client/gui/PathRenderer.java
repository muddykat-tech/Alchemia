package muddykat.alchemia.client.gui;

import muddykat.alchemia.common.items.helper.PathPoint;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.List;

public class PathRenderer {

    public static final double SUPERSAMPLE = 3.0;

    private static final double DASH_ON = 4.0;
    private static final double DASH_OFF = 3.0;
    private static final double DOT_ON = 1.0;
    private static final double DOT_ON_HOLLOW = 2.0;
    private static final double DOT_OFF = 3.0;

    private static final double STEP = 1.0;
    private static final int AURA_LAYERS = 3;
    private static final double RING_DASH = 3.0;
    private static final double SPIRAL_TURNS = 1.75;

    public record Clip(double left, double top, double right, double bottom) {
    }

    private static final class Runs {
        private final GuiGraphics graphics;

        private int left;
        private int top;
        private int right;
        private int bottom;
        private int colour;
        private boolean pending;

        private Runs(GuiGraphics graphics) {
            this.graphics = graphics;
        }

        private void stamp(double x, double y, int thickness, int argb) {
            int size = Math.max(1, thickness);
            int half = size / 2;
            int x0 = (int) Math.round(x) - half;
            int y0 = (int) Math.round(y) - half;

            if (pending && argb == colour) {
                if (y0 == top && y0 + size == bottom && (x0 == right || x0 + size == left)) {
                    left = Math.min(left, x0);
                    right = Math.max(right, x0 + size);
                    return;
                }
                if (x0 == left && x0 + size == right && (y0 == bottom || y0 + size == top)) {
                    top = Math.min(top, y0);
                    bottom = Math.max(bottom, y0 + size);
                    return;
                }
            }

            flush();

            left = x0;
            top = y0;
            right = x0 + size;
            bottom = y0 + size;
            colour = argb;
            pending = true;
        }

        private void flush() {
            if (!pending) return;
            graphics.fill(left, top, right, bottom, colour);
            pending = false;
        }
    }

    private static void pushScaled(GuiGraphics graphics) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.scale((float) (1.0 / SUPERSAMPLE), (float) (1.0 / SUPERSAMPLE), 1.0F);
    }

    private static void popScaled(GuiGraphics graphics) {
        graphics.pose().popPose();
    }

    public static void dashes(GuiGraphics graphics, List<PathPoint> points, int thickness, int argb, boolean teleport) {
        stroke(graphics, points, thickness, argb, teleport, false, 0.0, null);
    }

    public static void dashes(GuiGraphics graphics, List<PathPoint> points, int thickness, int argb,
                              boolean teleport, Clip clip) {
        stroke(graphics, points, thickness, argb, teleport, false, 0.0, clip);
    }

    public static void outline(GuiGraphics graphics, List<PathPoint> points, int thickness, int argb,
                               boolean teleport, double phase) {
        stroke(graphics, points, thickness, argb, teleport, true, phase, null);
    }

    public static void outline(GuiGraphics graphics, List<PathPoint> points, int thickness, int argb,
                               boolean teleport, double phase, Clip clip) {
        stroke(graphics, points, thickness, argb, teleport, true, phase, clip);
    }

    private static void stroke(GuiGraphics graphics, List<PathPoint> points, int thickness, int argb,
                               boolean teleport, boolean hollow, double phase, Clip clip) {
        if (points.size() < 2) return;

        double on = (teleport ? (hollow ? DOT_ON_HOLLOW : DOT_ON) : DASH_ON) * SUPERSAMPLE;
        double period = (teleport ? DOT_ON + DOT_OFF : DASH_ON + DASH_OFF) * SUPERSAMPLE;
        int weight = Math.max(1, (int) Math.round(thickness * SUPERSAMPLE));
        double rail = Math.max(1.0, weight / 2.0);
        double margin = weight + rail + STEP;
        double shift = Math.max(0.0, phase) * SUPERSAMPLE;

        double clipLeft = clip == null ? 0.0 : clip.left() * SUPERSAMPLE - margin;
        double clipTop = clip == null ? 0.0 : clip.top() * SUPERSAMPLE - margin;
        double clipRight = clip == null ? 0.0 : clip.right() * SUPERSAMPLE + margin;
        double clipBottom = clip == null ? 0.0 : clip.bottom() * SUPERSAMPLE + margin;

        Runs core = new Runs(graphics);
        Runs mirror = hollow ? new Runs(graphics) : core;
        Runs caps = hollow ? new Runs(graphics) : core;

        boolean inside = false;
        double tailX = 0.0;
        double tailY = 0.0;
        double tailSideX = 0.0;
        double tailSideY = 0.0;

        pushScaled(graphics);

        double walked = 0.0;
        double cursor = 0.0;

        for (int i = 1; i < points.size(); i++) {
            PathPoint origin = points.get(i - 1);
            PathPoint target = points.get(i);

            double fromX = origin.x() * SUPERSAMPLE;
            double fromY = origin.y() * SUPERSAMPLE;
            double runX = target.x() * SUPERSAMPLE - fromX;
            double runY = target.y() * SUPERSAMPLE - fromY;

            double span = Math.sqrt(runX * runX + runY * runY);
            if (span < 1.0E-6) continue;

            if (clip != null && (Math.max(fromX, fromX + runX) < clipLeft
                    || Math.min(fromX, fromX + runX) > clipRight
                    || Math.max(fromY, fromY + runY) < clipTop
                    || Math.min(fromY, fromY + runY) > clipBottom)) {
                walked += span;
                continue;
            }

            double sideX = -runY / span;
            double sideY = runX / span;

            while (cursor < walked + span) {
                double travelled = (cursor - walked) / span;
                double atX = fromX + runX * travelled;
                double atY = fromY + runY * travelled;
                boolean lit = (cursor + shift) % period < on;
                cursor += STEP;

                if (clip != null && (atX < clipLeft || atX > clipRight || atY < clipTop || atY > clipBottom)) {
                    inside = false;
                    continue;
                }

                if (!hollow) {
                    if (lit) core.stamp(atX, atY, weight, argb);
                    continue;
                }

                if (lit) {
                    if (!inside) {
                        cap(caps, atX, atY, sideX, sideY, rail, argb);
                        inside = true;
                    }

                    core.stamp(atX + sideX * rail, atY + sideY * rail, 1, argb);
                    mirror.stamp(atX - sideX * rail, atY - sideY * rail, 1, argb);
                    tailX = atX;
                    tailY = atY;
                    tailSideX = sideX;
                    tailSideY = sideY;
                    continue;
                }

                if (inside) {
                    cap(caps, tailX, tailY, tailSideX, tailSideY, rail, argb);
                    inside = false;
                }
            }
            walked += span;
        }

        if (hollow && inside) cap(caps, tailX, tailY, tailSideX, tailSideY, rail, argb);

        core.flush();
        mirror.flush();
        caps.flush();

        popScaled(graphics);
    }

    private static void cap(Runs runs, double x, double y, double sideX, double sideY, double rail, int argb) {
        int steps = Math.max(1, (int) Math.round(rail * 2));

        for (int step = 0; step <= steps; step++) {
            double offset = -rail + 2 * rail * step / steps;
            runs.stamp(x + sideX * offset, y + sideY * offset, 1, argb);
        }
    }

    public static void dot(GuiGraphics graphics, double x, double y, int radius, int argb) {
        int px = (int) Math.round(x);
        int py = (int) Math.round(y);
        graphics.fill(px - radius, py - radius, px + radius, py + radius, argb);
    }

    public static void cross(GuiGraphics graphics, double x, double y, double radius, int thickness, int argb) {
        double reach = radius * SUPERSAMPLE;
        int weight = Math.max(1, (int) Math.round(thickness * SUPERSAMPLE));
        int steps = Math.max(2, (int) Math.round(reach));

        Runs runs = new Runs(graphics);
        pushScaled(graphics);

        for (int step = -steps; step <= steps; step++) {
            double along = reach * step / steps;
            runs.stamp(x * SUPERSAMPLE + along, y * SUPERSAMPLE + along, weight, argb);
            runs.stamp(x * SUPERSAMPLE + along, y * SUPERSAMPLE - along, weight, argb);
        }

        runs.flush();
        popScaled(graphics);
    }

    public static void spiral(GuiGraphics graphics, double x, double y, double radius, int thickness, int argb) {
        double reach = radius * SUPERSAMPLE;
        int weight = Math.max(1, (int) Math.round(thickness * SUPERSAMPLE));
        double sweep = SPIRAL_TURNS * 2 * Math.PI;
        int steps = Math.max(24, (int) Math.round(reach * 6));

        Runs runs = new Runs(graphics);
        pushScaled(graphics);

        for (int step = 0; step <= steps; step++) {
            double travelled = (double) step / steps;
            double angle = -Math.PI / 2 + sweep * travelled;
            double along = reach * travelled;
            runs.stamp(x * SUPERSAMPLE + Math.cos(angle) * along,
                    y * SUPERSAMPLE + Math.sin(angle) * along, weight, argb);
        }

        runs.flush();
        popScaled(graphics);
    }

    public static void ending(GuiGraphics graphics, double x, double y, double radius, int thickness,
                              int argb, boolean teleport) {
        if (teleport) {
            spiral(graphics, x, y, radius, thickness, argb);
        } else {
            cross(graphics, x, y, radius, thickness, argb);
        }
    }

    public static void marker(GuiGraphics graphics, double x, double y, int radius, int rgb) {
        int px = (int) Math.round(x);
        int py = (int) Math.round(y);
        graphics.renderOutline(px - radius, py - radius, radius * 2, radius * 2, ARGB.color(200, rgb));
        graphics.renderOutline(px - radius - 1, py - radius - 1, radius * 2 + 2, radius * 2 + 2, ARGB.color(80, rgb));
    }

    public static void aura(GuiGraphics graphics, double cx, double cy, double radius, int rgb, int alpha) {
        for (int layer = AURA_LAYERS; layer >= 1; layer--) {
            disc(graphics, cx, cy, radius * layer / AURA_LAYERS, ARGB.color(Math.max(1, alpha / AURA_LAYERS), rgb));
        }
    }

    public static void disc(GuiGraphics graphics, double cx, double cy, double radius, int argb) {
        int reach = (int) Math.round(radius);
        if (reach < 1) return;

        int px = (int) Math.round(cx);
        int py = (int) Math.round(cy);

        for (int dy = -reach; dy <= reach; dy++) {
            int half = (int) Math.round(Math.sqrt((double) reach * reach - (double) dy * dy));
            if (half < 1) continue;
            graphics.fill(px - half, py + dy, px + half, py + dy + 1, argb);
        }
    }

    public static void ring(GuiGraphics graphics, double cx, double cy, double radius, int argb, boolean dashed) {
        double circumference = 2 * Math.PI * radius;
        int steps = Math.max(8, (int) Math.round(circumference / STEP));

        Runs runs = new Runs(graphics);

        for (int step = 0; step < steps; step++) {
            if (dashed && ((int) (circumference * step / steps / RING_DASH)) % 2 == 1) continue;

            double angle = 2 * Math.PI * step / steps;
            runs.stamp(cx + Math.cos(angle) * radius, cy + Math.sin(angle) * radius, 1, argb);
        }

        runs.flush();
    }
}
