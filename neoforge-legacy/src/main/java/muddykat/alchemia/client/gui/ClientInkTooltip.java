package muddykat.alchemia.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class ClientInkTooltip implements ClientTooltipComponent {
    private static final int ICON = 16;
    private static final int ICON_GAP = 4;
    private static final int LINE = 10;
    private static final int WRAP = 170;
    private static final int GAP = 3;
    private static final int BODY = 0xFF604A34;
    private static final int RULE = 0xFFB8A27A;
    private static final int RULE_LIGHT = 0xFFF0E5C9;

    private final InkTooltip tooltip;
    private final List<FormattedCharSequence> body;

    public ClientInkTooltip(InkTooltip tooltip) {
        this.tooltip = tooltip;
        this.body = tooltip.lines().stream()
                .flatMap(line -> Minecraft.getInstance().font.split(line, WRAP).stream())
                .toList();
    }

    private int headerHeight() {
        return tooltip.hasIcon() ? ICON : LINE - 1;
    }

    private int titleX() {
        return tooltip.hasIcon() ? ICON + ICON_GAP : 0;
    }

    @Override
    public int getHeight() {
        int height = headerHeight();
        if (!body.isEmpty()) {
            height += GAP + 2 + GAP + body.size() * LINE - 1;
        }
        height += 2;
        return height;
    }

    @Override
    public int getWidth(Font font) {
        int width = titleX() + font.width(tooltip.title());
        for (FormattedCharSequence sequence : body) {
            width = Math.max(width, font.width(sequence));
        }
        return width;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        int w = getWidth(font);
        if (!tooltip.icon().isEmpty()) {
            graphics.renderItem(tooltip.icon(), x, y);
        } else if (tooltip.sprite() != null) {
            GuiDraw.sprite(graphics, tooltip.sprite(), x, y, ICON, ICON);
        }

        int titleY = y + (headerHeight() - 8) / 2;
        graphics.drawString(font, tooltip.title(), x + titleX(), titleY, tooltip.titleColour(), false);

        if (body.isEmpty()) {
            return;
        }

        int ruleY = y + headerHeight() + GAP;
        graphics.fill(x, ruleY, x + w, ruleY + 1, RULE);
        graphics.fill(x, ruleY + 1, x + w, ruleY + 2, RULE_LIGHT);

        int lineY = ruleY + 2 + GAP;
        for (FormattedCharSequence sequence : body) {
            graphics.drawString(font, sequence, x, lineY, BODY, false);
            lineY += LINE;
        }
    }
}
