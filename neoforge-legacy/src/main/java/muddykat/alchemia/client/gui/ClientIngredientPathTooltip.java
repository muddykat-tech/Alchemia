package muddykat.alchemia.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import muddykat.alchemia.common.items.helper.IngredientPathTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public class ClientIngredientPathTooltip implements ClientTooltipComponent {

    private static final int BOX_WIDTH = 80;
    private static final int BOX_HEIGHT = 72;

    private final PathPanel panel;
    private int frameWidth;

    public ClientIngredientPathTooltip(IngredientPathTooltip tooltip) {
        this.panel = new PathPanel(tooltip.label(), tooltip.points(), tooltip.taken(),
                tooltip.teleport(), BOX_WIDTH, BOX_HEIGHT);
    }

    public void frame(int width) {
        this.frameWidth = width;
    }

    static boolean shiftDown() {
        long window = Minecraft.getInstance().getWindow().getWindow();
        return InputConstants.isKeyDown(window, InputConstants.KEY_LSHIFT)
                || InputConstants.isKeyDown(window, InputConstants.KEY_RSHIFT);
    }

    private boolean visible() {
        return shiftDown() && panel.drawable();
    }

    @Override
    public int getWidth(Font font) {
        return visible() ? panel.width(font) : 0;
    }

    @Override
    public int getHeight() {
        return visible() ? panel.height() : 0;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        if (!visible()) return;
        panel.draw(graphics, font, x + Math.max(0, frameWidth - panel.width(font)) / 2, y);
    }
}
