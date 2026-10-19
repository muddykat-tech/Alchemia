package muddykat.alchemia.client.gui;

import muddykat.alchemia.common.items.helper.PathGeometry;
import muddykat.alchemia.common.items.helper.PathPoint;
import net.minecraft.client.gui.Font;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class PathPanel {

    private static final int MIN_WIDTH = 64;
    private static final int INSET = 7;
    private static final int TITLE_H = 10;
    private static final int RULE_GAP = 3;
    private static final int ICON = 9;
    private static final int ICON_MARGIN = ICON / 2 + 1;
    private static final int THICKNESS = 2;

    private static final int TITLE_INK = 0xFF4A2E1C;
    private static final int RULE_DARK = 0xFF9A7448;
    private static final int RULE_LIGHT = 0xFFE6CFA4;
    private static final int TAKEN_INK = 0xFF4A2E1C;
    private static final int POTENTIAL_INK = 0xFF8A6846;
    private static final int LANDING_INK = 0xFF8C2A18;

    private final Component label;
    private final List<PathPoint> points;
    private final List<PathPoint> reached;
    private final boolean teleport;
    private final PathGeometry.Fit fit;

    public PathPanel(Component label, List<PathPoint> points, double taken, boolean teleport, int boxWidth, int boxHeight) {
        this.label = label;
        this.points = points;
        this.reached = PathGeometry.truncate(points, taken);
        this.teleport = teleport;
        this.fit = PathGeometry.fit(points, boxWidth, boxHeight, true);
    }

    public boolean drawable() {
        return points.size() > 1;
    }

    private int previewWidth() {
        return Math.max(MIN_WIDTH, (int) Math.ceil(fit.width())) + ICON_MARGIN * 2;
    }

    private int previewHeight() {
        return (int) Math.ceil(fit.height()) + ICON_MARGIN * 2;
    }

    private static int headerHeight() {
        return TITLE_H + 2 + RULE_GAP;
    }

    public int width(Font font) {
        return Math.max(previewWidth(), font.width(label)) + INSET * 2;
    }

    public int height() {
        return headerHeight() + previewHeight() + INSET * 2;
    }

    public void draw(GuiGraphics graphics, Font font, int left, int y) {
        int width = width(font);

        RenderSystem.enableBlend();
        AlchemiaSprites.PATH_TOOLTIP_PANEL.blit(graphics, left, y, width, height());
        graphics.drawString(font, label, left + (width - font.width(label)) / 2, y + INSET, TITLE_INK, false);

        int ruleY = y + INSET + TITLE_H;
        graphics.fill(left + INSET, ruleY, left + width - INSET, ruleY + 1, RULE_DARK);
        graphics.fill(left + INSET, ruleY + 1, left + width - INSET, ruleY + 2, RULE_LIGHT);

        double originX = left + (width - Math.ceil(fit.width())) / 2.0;
        double originY = y + INSET + headerHeight() + ICON_MARGIN;

        List<PathPoint> full = PathGeometry.translate(fit.apply(points), originX, originY);
        List<PathPoint> taken = PathGeometry.translate(fit.apply(reached), originX, originY);
        double walked = PathGeometry.length(taken);
        boolean complete = walked >= PathGeometry.length(full) - 0.5;

        PathRenderer.outline(graphics, PathGeometry.tail(full, walked), THICKNESS, POTENTIAL_INK, teleport, walked);
        PathRenderer.dashes(graphics, taken, THICKNESS, TAKEN_INK, teleport);

        icon(graphics, AlchemiaSprites.PATH_ORIGIN, fit.apply(PathPoint.ORIGIN).plus(originX, originY));
        if (!complete) icon(graphics, AlchemiaSprites.PATH_ENDING, full.get(full.size() - 1));

        if (taken.size() > 1) {
            PathPoint landing = taken.get(taken.size() - 1);
            if (teleport) {
                PathRenderer.spiral(graphics, landing.x(), landing.y(), 4, 1, LANDING_INK);
            } else {
                PathRenderer.cross(graphics, landing.x(), landing.y(), 3, 2, LANDING_INK);
            }
        }
    }

    private static void icon(GuiGraphics graphics, AlchemiaSprites.Sprite sprite, PathPoint at) {
        sprite.blit(graphics, (int) Math.round(at.x()) - ICON / 2, (int) Math.round(at.y()) - ICON / 2, ICON, ICON);
    }
}
