package muddykat.alchemia.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.InputConstants;
import muddykat.alchemia.common.items.helper.IngredientPathTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

import java.util.List;

public class ClientIngredientPathTooltip implements ClientTooltipComponent {

    private static final int CELL = 5;
    private static final int PADDING = 4;
    private static final int LABEL_H = 11;

    private static final int TAKEN = PathPalette.TAKEN;
    private static final int POTENTIAL = PathPalette.POTENTIAL;
    private static final int ORIGIN = PathPalette.ORIGIN;
    private static final int LABEL_INK = 0xFFC9A96E;

    private final IngredientPathTooltip tooltip;
    private final int minX;
    private final int minY;
    private final int columns;
    private final int rows;

    public ClientIngredientPathTooltip(IngredientPathTooltip tooltip) {
        this.tooltip = tooltip;

        int lowX = 0, highX = 0, lowY = 0, highY = 0;
        for (int[] cell : tooltip.cells()) {
            lowX = Math.min(lowX, cell[0]);
            highX = Math.max(highX, cell[0]);
            lowY = Math.min(lowY, cell[1]);
            highY = Math.max(highY, cell[1]);
        }

        this.minX = lowX;
        this.minY = lowY;
        this.columns = highX - lowX + 1;
        this.rows = highY - lowY + 1;
    }

    private static boolean shiftDown() {
        long window = Minecraft.getInstance().getWindow().getWindow();
        return InputConstants.isKeyDown(window, InputConstants.KEY_LSHIFT)
                || InputConstants.isKeyDown(window, InputConstants.KEY_RSHIFT);
    }

    private boolean visible() {
        return shiftDown() && !tooltip.cells().isEmpty();
    }

    private int gridWidth() {
        return columns * CELL;
    }

    @Override
    public int getWidth(Font font) {
        if (!visible()) return 0;
        return Math.max(gridWidth(), font.width(tooltip.label())) + PADDING * 2;
    }

    @Override
    public int getHeight() {
        return visible() ? rows * CELL + LABEL_H + PADDING * 2 : 0;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        if (!visible()) return;
        RenderSystem.enableBlend();

        int width = getWidth(font);
        int height = getHeight();

        AlchemiaSprites.PATH_TOOLTIP_PANEL.blit(graphics, x, y, width, height);
        graphics.drawString(font, tooltip.label(), x + PADDING, y + PADDING, LABEL_INK, false);

        int gridX = x + (width - gridWidth()) / 2;
        int gridY = y + PADDING + LABEL_H;

        int originX = gridX + (-minX) * CELL;
        int originY = gridY + (-minY) * CELL;

        List<int[]> cells = tooltip.cells();
        int taken = tooltip.taken();

        for (int i = 0; i < cells.size(); i++) {
            int[] cell = cells.get(i);
            int cellX = gridX + (cell[0] - minX) * CELL;
            int cellY = gridY + (cell[1] - minY) * CELL;
            graphics.fill(cellX, cellY, cellX + CELL - 1, cellY + CELL - 1, i < taken ? TAKEN : POTENTIAL);
        }

        graphics.fill(originX, originY, originX + CELL - 1, originY + CELL - 1, ORIGIN);
    }
}
