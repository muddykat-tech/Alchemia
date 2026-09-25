package muddykat.alchemia.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import muddykat.alchemia.common.items.helper.IngredientPathTooltip;
import muddykat.alchemia.common.items.helper.PathGeometry;
import muddykat.alchemia.common.items.helper.PathPoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;

import java.util.List;

public class ClientIngredientPathTooltip implements ClientTooltipComponent {

    private static final int BOX_WIDTH = 78;
    private static final int BOX_HEIGHT = 58;
    private static final int PADDING = 4;
    private static final int LABEL_H = 11;
    private static final int THICKNESS = 2;

    private static final int TAKEN = PathPalette.TAKEN;
    private static final int POTENTIAL = PathPalette.POTENTIAL;
    private static final int ORIGIN = PathPalette.ORIGIN;
    private static final int LANDING = PathPalette.LANDING;
    private static final int ENDING = PathPalette.ENDING;
    private static final int LABEL_INK = 0xFFC9A96E;

    private final IngredientPathTooltip tooltip;
    private final PathGeometry.Fit fit;

    public ClientIngredientPathTooltip(IngredientPathTooltip tooltip) {
        this.tooltip = tooltip;
        this.fit = PathGeometry.fit(tooltip.points(), BOX_WIDTH, BOX_HEIGHT, true);
    }

    private static boolean shiftDown() {
#if MC_VER >= 26300
        return InputConstants.isKeyDown(InputConstants.KEY_LSHIFT)
                || InputConstants.isKeyDown(InputConstants.KEY_RSHIFT);
#else
        var window = Minecraft.getInstance().getWindow();
        return InputConstants.isKeyDown(window, InputConstants.KEY_LSHIFT)
                || InputConstants.isKeyDown(window, InputConstants.KEY_RSHIFT);
#endif
    }

    private boolean visible() {
        return shiftDown() && tooltip.points().size() > 1;
    }

    private int previewWidth() {
        return (int) Math.ceil(fit.width()) + THICKNESS * 2;
    }

    private int previewHeight() {
        return (int) Math.ceil(fit.height()) + THICKNESS * 2;
    }

    @Override
    public int getWidth(Font font) {
        if (!visible()) return 0;
        return Math.max(previewWidth(), font.width(tooltip.label())) + PADDING * 2;
    }

    @Override
    public int getHeight(Font font) {
        return visible() ? previewHeight() + LABEL_H + PADDING * 2 : 0;
    }

    @Override
    public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
        if (!visible()) return;

        int width = getWidth(font);
        int height = getHeight(font);

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, AlchemiaSprites.PATH_TOOLTIP_PANEL, x, y, width, height);
        graphics.text(font, tooltip.label(), x + PADDING, y + PADDING, LABEL_INK, false);

        double originX = x + (width - previewWidth()) / 2.0 + THICKNESS;
        double originY = y + PADDING + LABEL_H + THICKNESS;

        List<PathPoint> full = PathGeometry.translate(fit.apply(tooltip.points()), originX, originY);
        List<PathPoint> reached = PathGeometry.translate(fit.apply(tooltip.reached()), originX, originY);
        List<PathPoint> remaining = PathGeometry.tail(full, PathGeometry.length(reached));

        PathRenderer.outline(graphics, remaining, THICKNESS, POTENTIAL, tooltip.teleport());
        PathRenderer.dashes(graphics, reached, THICKNESS, TAKEN, tooltip.teleport());

        PathPoint start = fit.apply(PathPoint.ORIGIN).plus(originX, originY);
        PathRenderer.dot(graphics, start.x(), start.y(), 2, ORIGIN);

        PathPoint ending = full.get(full.size() - 1);
        PathRenderer.marker(graphics, ending.x(), ending.y(), 3, ENDING);

        if (!reached.isEmpty()) {
            PathPoint landing = reached.get(reached.size() - 1);
            PathRenderer.ending(graphics, landing.x(), landing.y(), 3, 1, LANDING, tooltip.teleport());
        }
    }
}
