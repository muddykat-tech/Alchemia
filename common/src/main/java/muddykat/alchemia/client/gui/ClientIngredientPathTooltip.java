package muddykat.alchemia.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import muddykat.alchemia.common.items.helper.IngredientPathTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public class ClientIngredientPathTooltip implements ClientTooltipComponent {

    private static final int BOX_WIDTH = 80;
    private static final int BOX_HEIGHT = 72;

    private final IngredientPathTooltip tooltip;
    private final PathPanel panel;

    public ClientIngredientPathTooltip(IngredientPathTooltip tooltip) {
        this.tooltip = tooltip;
        this.panel = new PathPanel(tooltip.label(), tooltip.points(), tooltip.taken(), tooltip.teleport(),
                BOX_WIDTH, BOX_HEIGHT);
    }

    static boolean shiftDown() {
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
        return shiftDown() && panel.drawable();
    }

    @Override
    public int getWidth(Font font) {
        return visible() ? panel.width(font) : 0;
    }

    @Override
    public int getHeight(Font font) {
        return visible() ? panel.height() : 0;
    }

    @Override
    public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
        if (!visible()) return;
        panel.draw(graphics, font, x + Math.max(0, w - panel.width(font)) / 2, y);
    }
}
