package muddykat.alchemia.client.gui;

import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.ItemMortarPestle;
import muddykat.alchemia.common.items.helper.IngredientPath;
import muddykat.alchemia.common.items.helper.Ingredients;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class GrindOverlay {

    private static final int CELL = 6;
    private static final double PREVIEW_SCALE = 0.36;
    private static final int PADDING = 6;
    private static final int TOP_MARGIN_PERCENT = 22;

    private static final float FADE_SECONDS = 0.22F;

    private static final int PANEL_FILL_RGB = 0x241C12;
    private static final int PANEL_FILL_ALPHA = 192;
    private static final int PANEL_EDGE_RGB = 0x6B5335;
    private static final int PATH_TAKEN = PathPalette.TAKEN;
    private static final int PATH_POTENTIAL = PathPalette.POTENTIAL;
    private static final int PATH_ORIGIN = PathPalette.ORIGIN;
    private static final int LABEL_INK_RGB = 0xE7CD9D;
    private static final int LABEL_FAINT_RGB = 0xC9A96E;

    private static float alpha = 0.0F;
    private static long lastFrame = 0L;
    private static Ingredients cached;
    private static int cachedCrush;
    private static Component cachedName;

    private static int shade(int rgb, float amount) {
        return ARGB.color((int) (255 * amount), rgb);
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        long now = Util.getMillis();
        float step = lastFrame == 0L ? 0.0F : (now - lastFrame) / 1000.0F;
        lastFrame = now;

        if (captureTarget()) {
            alpha = Math.min(1.0F, alpha + step / FADE_SECONDS);
        } else {
            alpha = Math.max(0.0F, alpha - step / FADE_SECONDS);
        }

        if (alpha <= 0.01F || cached == null) return;
        draw(graphics, alpha);
    }

    private static boolean captureTarget() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !player.isUsingItem()) return false;

        ItemStack mortar = player.getUseItem();
        if (!(mortar.getItem() instanceof ItemMortarPestle)) return false;

        ItemStack target = ItemMortarPestle.findTarget(player, mortar);
        if (target.isEmpty() || !(target.getItem() instanceof ItemIngredient item)) return false;

        cached = item.getIngredient();
        cachedCrush = ItemIngredient.crushOf(target);
        cachedName = target.getHoverName();
        return true;
    }

    private static void draw(GuiGraphicsExtractor graphics, float fade) {
        Minecraft minecraft = Minecraft.getInstance();
        Ingredients ingredient = cached;
        IngredientPath shape = IngredientPath.of(ingredient);

        int dx = Integer.signum(ingredient.getPrimaryAlignment().getX() + ingredient.getSecondaryAlignment().getX());
        int dy = Integer.signum(ingredient.getPrimaryAlignment().getY() + ingredient.getSecondaryAlignment().getY());
        if (shape.needsDirection() && dx == 0 && dy == 0) return;

        double maxPotency = ingredient.getPotency(Ingredients.MAX_CRUSH);
        double nowPotency = ingredient.getPotencyForUnits(cachedCrush);

        List<int[]> full = cellsFor(shape, ingredient, dx, dy, maxPotency);
        List<int[]> current = cellsFor(shape, ingredient, dx, dy, nowPotency);
        if (full.isEmpty()) return;

        int lowX = 0, highX = 0, lowY = 0, highY = 0;
        for (int[] cell : full) {
            lowX = Math.min(lowX, cell[0]);
            highX = Math.max(highX, cell[0]);
            lowY = Math.min(lowY, cell[1]);
            highY = Math.max(highY, cell[1]);
        }

        int columns = highX - lowX + 1;
        int rows = highY - lowY + 1;

        Component progress = Component.translatable("alchemia.tooltip.grind", cachedCrush, Ingredients.MAX_CRUSH_UNITS);

        int gridW = columns * CELL;
        int textW = Math.max(minecraft.font.width(cachedName), minecraft.font.width(progress));
        int panelW = Math.max(gridW, textW) + PADDING * 2;
        int panelH = rows * CELL + 22 + PADDING * 2;

        int panelX = (graphics.guiWidth() - panelW) / 2;
        int panelY = graphics.guiHeight() * TOP_MARGIN_PERCENT / 100;

        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH,
                ARGB.color((int) (PANEL_FILL_ALPHA * fade), PANEL_FILL_RGB));
        graphics.outline(panelX, panelY, panelW, panelH, shade(PANEL_EDGE_RGB, fade));

        graphics.text(minecraft.font, cachedName, panelX + (panelW - minecraft.font.width(cachedName)) / 2,
                panelY + PADDING, shade(LABEL_INK_RGB, fade), false);
        graphics.text(minecraft.font, progress, panelX + (panelW - minecraft.font.width(progress)) / 2,
                panelY + PADDING + 10, shade(LABEL_FAINT_RGB, fade), false);

        int gridX = panelX + (panelW - gridW) / 2;
        int gridY = panelY + PADDING + 22;
        int taken = current.size();

        for (int i = 0; i < full.size(); i++) {
            int[] cell = full.get(i);
            int px = gridX + (cell[0] - lowX) * CELL;
            int py = gridY + (cell[1] - lowY) * CELL;
            graphics.fill(px, py, px + CELL - 1, py + CELL - 1,
                    ARGB.color((int) (255 * fade), i < taken ? PATH_TAKEN : PATH_POTENTIAL));
        }

        int originX = gridX + (-lowX) * CELL;
        int originY = gridY + (-lowY) * CELL;
        graphics.fill(originX, originY, originX + CELL - 1, originY + CELL - 1, shade(PATH_ORIGIN, fade));
    }

    private static List<int[]> cellsFor(IngredientPath shape, Ingredients ingredient, int dx, int dy, double potency) {
        return shape.teleports()
                ? shape.teleportPreview(dx, dy, potency, PREVIEW_SCALE)
                : IngredientPath.pathFor(ingredient, dx, dy, potency);
    }
}
