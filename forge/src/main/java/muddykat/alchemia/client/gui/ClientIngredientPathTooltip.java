package muddykat.alchemia.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import muddykat.alchemia.common.items.helper.IngredientPathTooltip;
import muddykat.alchemia.common.items.helper.PathGeometry;
import muddykat.alchemia.common.items.helper.PathPoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

import java.util.List;

public class ClientIngredientPathTooltip implements ClientTooltipComponent {

    private static final int BOX_WIDTH = 80;
    private static final int BOX_HEIGHT = 72;
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

    private final IngredientPathTooltip tooltip;
    private final PathGeometry.Fit fit;
    private int frameWidth;

    public ClientIngredientPathTooltip(IngredientPathTooltip tooltip) {
        this.tooltip = tooltip;
        this.fit = PathGeometry.fit(tooltip.points(), BOX_WIDTH, BOX_HEIGHT, true);
    }

    public void frame(int width) {
        this.frameWidth = width;
    }

    private static boolean shiftDown() {
        long window = Minecraft.getInstance().getWindow().getWindow();
        return InputConstants.isKeyDown(window, InputConstants.KEY_LSHIFT)
                || InputConstants.isKeyDown(window, InputConstants.KEY_RSHIFT);
    }

    private boolean visible() {
        return shiftDown() && tooltip.points().size() > 1;
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

    @Override
    public int getWidth(Font font) {
        if (!visible()) return 0;
        return Math.max(previewWidth(), font.width(tooltip.label())) + INSET * 2;
    }

    @Override
    public int getHeight() {
        return visible() ? headerHeight() + previewHeight() + INSET * 2 : 0;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        if (!visible()) return;
        RenderSystem.enableBlend();

        int width = getWidth(font);
        int height = getHeight();
        int left = x + Math.max(0, frameWidth - width) / 2;

        AlchemiaSprites.PATH_TOOLTIP_PANEL.blit(graphics, left, y, width, height);
        graphics.drawString(font, tooltip.label(), left + (width - font.width(tooltip.label())) / 2, y + INSET, TITLE_INK, false);

        int ruleY = y + INSET + TITLE_H;
        graphics.fill(left + INSET, ruleY, left + width - INSET, ruleY + 1, RULE_DARK);
        graphics.fill(left + INSET, ruleY + 1, left + width - INSET, ruleY + 2, RULE_LIGHT);

        double originX = left + (width - Math.ceil(fit.width())) / 2.0;
        double originY = y + INSET + headerHeight() + ICON_MARGIN;

        List<PathPoint> full = PathGeometry.translate(fit.apply(tooltip.points()), originX, originY);
        List<PathPoint> reached = PathGeometry.translate(fit.apply(tooltip.reached()), originX, originY);
        double walked = PathGeometry.length(reached);
        List<PathPoint> remaining = PathGeometry.tail(full, walked);
        boolean complete = walked >= PathGeometry.length(full) - 0.5;

        PathRenderer.outline(graphics, remaining, THICKNESS, POTENTIAL_INK, tooltip.teleport(), walked);
        PathRenderer.dashes(graphics, reached, THICKNESS, TAKEN_INK, tooltip.teleport());

        PathPoint start = fit.apply(PathPoint.ORIGIN).plus(originX, originY);
        icon(graphics, AlchemiaSprites.PATH_ORIGIN, start);

        if (!complete) icon(graphics, AlchemiaSprites.PATH_ENDING, full.get(full.size() - 1));

        if (reached.size() > 1) {
            PathPoint landing = reached.get(reached.size() - 1);
            if (tooltip.teleport()) {
                PathRenderer.spiral(graphics, landing.x(), landing.y(), 4, 1, LANDING_INK);
            } else {
                PathRenderer.cross(graphics, landing.x(), landing.y(), 3, 2, LANDING_INK);
            }
        }
    }

    private static void icon(GuiGraphics graphics, AlchemiaSprites.Sprite sprite, PathPoint at) {
        sprite.blit(graphics, (int) Math.round(at.x()) - ICON / 2, (int) Math.round(at.y()) - ICON / 2);
    }
}
