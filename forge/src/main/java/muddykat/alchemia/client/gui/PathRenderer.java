package muddykat.alchemia.client.gui;

import muddykat.alchemia.common.items.helper.PathGeometry;
import muddykat.alchemia.common.items.helper.PathPoint;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.List;

public class PathRenderer {

    public static final double SUPERSAMPLE = 3.0;

    private static final double DASH_ON = 4.0;
    private static final double DASH_OFF = 3.0;
    private static final double DOT_ON = 1.0;
    private static final double DOT_OFF = 3.0;

    private static final double STEP = 1.0;
    private static final int AURA_LAYERS = 3;
    private static final double RING_DASH = 3.0;
    private static final double SPIRAL_TURNS = 1.75;

    private static void pushScaled(GuiGraphics graphics) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.scale((float) (1.0 / SUPERSAMPLE), (float) (1.0 / SUPERSAMPLE), 1.0F);
    }

    private static void popScaled(GuiGraphics graphics) {
        graphics.pose().popPose();
    }

    public static void dashes(GuiGraphics graphics, List<PathPoint> points, int thickness, int argb, boolean teleport) {
        stroke(graphics, points, thickness, argb, teleport, false);
    }

    public static void outline(GuiGraphics graphics, List<PathPoint> points, int thickness, int argb, boolean teleport) {
        stroke(graphics, points, thickness, argb, teleport, true);
    }

    private static void stroke(GuiGraphics graphics, List<PathPoint> points, int thickness, int argb,
                               boolean teleport, boolean hollow) {
        if (points.size() < 2) return;

        List<PathPoint> scaled = PathGeometry.scale(points, SUPERSAMPLE);
        double on = (teleport ? DOT_ON : DASH_ON) * SUPERSAMPLE;
        double period = on + (teleport ? DOT_OFF : DASH_OFF) * SUPERSAMPLE;
        int weight = Math.max(1, (int) Math.round(thickness * SUPERSAMPLE));
        double rail = Math.max(1.0, weight / 2.0);
        double walked = 0.0;

        pushScaled(graphics);

        for (int i = 1; i < scaled.size(); i++) {
            PathPoint from = scaled.get(i - 1);
            PathPoint to = scaled.get(i);

            double span = from.distanceTo(to);
            if (span < 1.0E-6) continue;

            PathPoint bearing = to.minus(from).normalised();
            PathPoint perpendicular = new PathPoint(-bearing.y(), bearing.x());
            int steps = Math.max(1, (int) Math.ceil(span / STEP));

            for (int step = 0; step < steps; step++) {
                if ((walked + span * step / steps) % period >= on) continue;

                PathPoint at = from.lerp(to, (double) step / steps);
                if (!hollow) {
                    stamp(graphics, at.x(), at.y(), weight, argb);
                    continue;
                }

                stamp(graphics, at.x() + perpendicular.x() * rail, at.y() + perpendicular.y() * rail, 1, argb);
                stamp(graphics, at.x() - perpendicular.x() * rail, at.y() - perpendicular.y() * rail, 1, argb);
            }
            walked += span;
        }

        popScaled(graphics);
    }

    private static void stamp(GuiGraphics graphics, double x, double y, int thickness, int argb) {
        int size = Math.max(1, thickness);
        int half = size / 2;
        int px = (int) Math.round(x);
        int py = (int) Math.round(y);
        graphics.fill(px - half, py - half, px - half + size, py - half + size, argb);
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

        pushScaled(graphics);

        for (int step = -steps; step <= steps; step++) {
            double along = reach * step / steps;
            stamp(graphics, x * SUPERSAMPLE + along, y * SUPERSAMPLE + along, weight, argb);
            stamp(graphics, x * SUPERSAMPLE + along, y * SUPERSAMPLE - along, weight, argb);
        }

        popScaled(graphics);
    }

    public static void spiral(GuiGraphics graphics, double x, double y, double radius, int thickness, int argb) {
        double reach = radius * SUPERSAMPLE;
        int weight = Math.max(1, (int) Math.round(thickness * SUPERSAMPLE));
        double sweep = SPIRAL_TURNS * 2 * Math.PI;
        int steps = Math.max(24, (int) Math.round(reach * 6));

        pushScaled(graphics);

        for (int step = 0; step <= steps; step++) {
            double travelled = (double) step / steps;
            double angle = -Math.PI / 2 + sweep * travelled;
            double along = reach * travelled;
            stamp(graphics, x * SUPERSAMPLE + Math.cos(angle) * along,
                    y * SUPERSAMPLE + Math.sin(angle) * along, weight, argb);
        }

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

        for (int step = 0; step < steps; step++) {
            if (dashed && ((int) (circumference * step / steps / RING_DASH)) % 2 == 1) continue;

            double angle = 2 * Math.PI * step / steps;
            stamp(graphics, cx + Math.cos(angle) * radius, cy + Math.sin(angle) * radius, 1, argb);
        }
    }
}
