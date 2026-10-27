package muddykat.alchemia.client.gui.cauldron;

import muddykat.alchemia.client.gui.CauldronArt;
import muddykat.alchemia.client.gui.InkTooltip;
import muddykat.alchemia.client.gui.ScrollWidgetRenderer;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class LedgerPanel {

    public static final int SHEET_W = 110;
    public static final int WIDTH = ScrollWidgetRenderer.outerWidth(SHEET_W);

    private static final int INNER_X = ScrollWidgetRenderer.ROLL_END + 6;
    private static final int INNER_W = SHEET_W - 12;
    private static final int PAD = 8;
    private static final int LINE_HEIGHT = 10;
    private static final int SKULL_PITCH = 13;
    private static final int SKULL_ICON = 12;

    private final BrewContext context;
    private final HeatGauge heat;
    private final TooltipQueue tooltips;
    private final PotencyGrid potency = new PotencyGrid();
    private final BrewReadout readout;

    public LedgerPanel(BrewContext context, HeatGauge heat, LandingPulse landing, TooltipQueue tooltips) {
        this.context = context;
        this.heat = heat;
        this.tooltips = tooltips;
        this.readout = new BrewReadout(landing);
    }

    public static int height() {
        return ScrollWidgetRenderer.outerHeight(sheetHeight());
    }

    private static int sheetHeight() {
        return 5 + 13 + 10 + 13 + 12 + 10 + PotencyGrid.rows() * PotencyGrid.CELL
                + 4 + 11 + 14 + 9 + 12 + BrewReadout.HEIGHT + PAD;
    }

    public void draw(GuiGraphics graphics, int x, int y, double heatCap, int mouseX, int mouseY) {
        ScrollWidgetRenderer.openPlain(graphics, x, y, SHEET_W, sheetHeight(),
                () -> drawSheet(graphics, x, y, heatCap, mouseX, mouseY));
    }

    private void drawSheet(GuiGraphics graphics, int x, int top, double heatCap, int mouseX, int mouseY) {
        TileEntityAlchemyCauldron cauldron = context.cauldron();
        Font font = context.getFont();
        int left = x + INNER_X;
        int centre = left + INNER_W / 2;
        int y = top + ScrollWidgetRenderer.ROLL_H - 1 + 5;

        CauldronArt.initialled(graphics, font, Component.translatable("alchemia.gui.effects.title").getString(), centre, y, 0.875F);
        y += 13;
        CauldronArt.divider(graphics, centre, y, INNER_W);
        y += 10;

        heat.drawVial(graphics, cauldron, centre - CauldronArt.VIAL_W / 2, y, heatCap, BrewHints.ready(context), Util.getMillis());
        y += 13;

        Component caption = cauldron.getWaterLevel() > 0
                ? Component.translatable("alchemia.gui.base.label", cauldron.getBase().displayName())
                : Component.translatable("alchemia.gui.effects.dry");
        centredClipped(graphics, font, caption, centre, y, CauldronArt.INK_FAINT);
        y += 12;

        if (cauldron.isSpoiled()) {
            drawRuined(graphics, font, centre, y, top + height() - ScrollWidgetRenderer.ROLL_H - 2);
            return;
        }

        int budget = TileEntityAlchemyCauldron.maxPotency();
        int used = cauldron.totalPotency();
        row(graphics, font, Component.translatable("alchemia.gui.ledger.potency"), centre, y,
                used >= budget ? CauldronArt.INK_RED : CauldronArt.INK_SOFT);
        y += 10;
        InkTooltip hovered = potency.draw(graphics, context, centre, y, mouseX, mouseY);
        y += PotencyGrid.rows() * PotencyGrid.CELL + 4;

        int cap = cauldron.getInstabilityCap();
        int spent = Math.min(cauldron.getInstability(), cap);
        row(graphics, font, Component.translatable("alchemia.gui.ledger.instability"), centre, y,
                spent > 0 && spent + 1 >= cap ? CauldronArt.INK_RED : CauldronArt.INK_SOFT);
        y += 11;
        drawInstability(graphics, cap, spent, centre, y);
        y += 14;

        CauldronArt.divider(graphics, centre, y, INNER_W);
        y += 9;
        String heading = Component.translatable("alchemia.gui.readout.title").getString();
        CauldronArt.initialled(graphics, font, heading, centre, y, 0.75F);
        y += 12;

        readout.draw(graphics, context, left, y, INNER_W);

        if (hovered != null) tooltips.ink(hovered, mouseX, mouseY);
    }

    private static void row(GuiGraphics graphics, Font font, Component label, int centre, int y, int colour) {
        graphics.drawString(font, label, centre - font.width(label) / 2, y, colour, false);
    }

    private static void centredClipped(GuiGraphics graphics, Font font, Component text, int centre, int y, int colour) {
        String shown = CauldronArt.clip(font, text, INNER_W);
        graphics.drawString(font, shown, centre - font.width(shown) / 2, y, colour, false);
    }

    private static void drawRuined(GuiGraphics graphics, Font font, int centre, int y, int bottom) {
        graphics.renderItem(new ItemStack(Items.SKELETON_SKULL), centre - 8, y + 4);
        int lineY = y + 26;
        for (FormattedCharSequence line : font.split(Component.translatable("alchemia.gui.effects.ruined"), INNER_W)) {
            if (lineY + LINE_HEIGHT > bottom) break;
            graphics.drawString(font, line, centre - font.width(line) / 2, lineY, CauldronArt.INK_RED, false);
            lineY += LINE_HEIGHT;
        }
    }

    private static void drawInstability(GuiGraphics graphics, int cap, int spent, int centre, int y) {
        int width = cap * SKULL_PITCH - (SKULL_PITCH - SKULL_ICON);
        int x = centre - width / 2;
        float scale = SKULL_ICON / 16.0F;
        ItemStack intact = new ItemStack(Items.SKELETON_SKULL);
        ItemStack lost = new ItemStack(Items.WITHER_SKELETON_SKULL);

        for (int index = 0; index < cap; index++) {
            graphics.pose().pushPose();
            graphics.pose().translate(x + index * SKULL_PITCH, y, 0.0F);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.renderItem(index < spent ? lost : intact, 0, 0);
            graphics.pose().popPose();
        }
    }
}
