package muddykat.alchemia.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import muddykat.alchemia.common.items.helper.IngredientPathTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import muddykat.alchemia.common.items.helper.Ingredients;
import net.minecraft.network.chat.Component;

public class ClientIngredientPathTooltip implements ClientTooltipComponent {

    private static final int BOX_WIDTH = 80;
    private static final int BOX_HEIGHT = 72;
    private static final int LINE = 11;
    private static final int INK = 0xFF4A2E1C;

    private final IngredientPathTooltip tooltip;
    private final PathPanel panel;
    private final Component ground;

    public ClientIngredientPathTooltip(IngredientPathTooltip tooltip) {
        this.tooltip = tooltip;
        this.panel = new PathPanel(tooltip.label(), tooltip.points(), tooltip.taken(), tooltip.teleport(),
                BOX_WIDTH, BOX_HEIGHT);
        this.ground = Component.translatable("alchemia.tooltip.ground", Ingredients.crushPercent(tooltip.crush()));
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

    private int sheetWidth(Font font) {
        return Math.max(panel.plainWidth(), Math.max(font.width(tooltip.label()), font.width(ground))) + ScrollWidgetRenderer.PAD_W * 2;
    }

    private int sheetHeight() {
        return ScrollWidgetRenderer.PAD_H * 2 + LINE + panel.plainHeight() + LINE;
    }

    @Override
    public int getWidth(Font font) {
        return visible() ? ScrollWidgetRenderer.outerWidth(sheetWidth(font)) : 0;
    }

    @Override
    public int getHeight(Font font) {
        return visible() ? ScrollWidgetRenderer.outerHeight(sheetHeight()) + 2 : 0;
    }

    @Override
    public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
        if (!visible()) return;
        int sheetW = sheetWidth(font);
        int left = x + Math.max(0, w - ScrollWidgetRenderer.outerWidth(sheetW)) / 2;
        int sheetX = left + ScrollWidgetRenderer.ROLL_END;
        int top = y + ScrollWidgetRenderer.ROLL_H - 1;
        ScrollWidgetRenderer.open(graphics, left, y, sheetW, sheetHeight(), () -> {
            int textY = top + ScrollWidgetRenderer.PAD_H;
            graphics.text(font, tooltip.label(), sheetX + (sheetW - font.width(tooltip.label())) / 2, textY, INK, false);
            int pathY = textY + LINE;
            panel.drawPlain(graphics, sheetX + (sheetW - panel.plainWidth()) / 2, pathY);
            graphics.text(font, ground, sheetX + (sheetW - font.width(ground)) / 2, pathY + panel.plainHeight() + 2, INK, false);
        });
    }
}
