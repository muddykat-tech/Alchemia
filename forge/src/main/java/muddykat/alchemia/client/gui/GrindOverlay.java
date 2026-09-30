package muddykat.alchemia.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.ItemMortarPestle;
import muddykat.alchemia.common.items.ItemPathTool;
import muddykat.alchemia.common.items.helper.IngredientPath;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.items.helper.PathGeometry;
import muddykat.alchemia.common.items.helper.PathPoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class GrindOverlay {

    private static final int BOX_WIDTH = 96;
    private static final int BOX_HEIGHT = 72;
    private static final int THICKNESS = 2;
    private static final int PADDING = 6;
    private static final int TOP_MARGIN_PERCENT = 22;

    private static final float FADE_SECONDS = 0.22F;
    private static final float GLIDE_SECONDS = 0.15F;
    private static final double GLIDE_SNAP = 0.05;

    private static final int PATH_TAKEN = PathPalette.TAKEN;
    private static final int PATH_POTENTIAL = PathPalette.POTENTIAL;
    private static final int PATH_ORIGIN = PathPalette.ORIGIN;
    private static final int PATH_LANDING = PathPalette.LANDING;
    private static final int PATH_ENDING = PathPalette.ENDING;
    private static final int LABEL_INK_RGB = 0xE7CD9D;
    private static final int LABEL_FAINT_RGB = 0xC9A96E;

    private static float alpha = 0.0F;
    private static long lastFrame = 0L;
    private static Ingredients cached;
    private static int cachedCrush;
    private static int cachedTrim;
    private static Component cachedName;
    private static int cachedRotation;
    private static String cachedProgressKey;
    private static int cachedProgressValue;
    private static int cachedUsesLeft;

    private record Shape(Ingredients ingredient, int crush, int trim, int rotation) {
    }

    private static Shape shape;
    private static boolean shaped;
    private static int shapeWidth;
    private static int shapeHeight;
    private static List<PathPoint> shapePotential = List.of();
    private static PathPoint shapeStart = PathPoint.ORIGIN;
    private static PathPoint shapeEnding = PathPoint.ORIGIN;
    private static double shapeTarget;
    private static double shapeWalked;

    private static int shade(int rgb, float amount) {
        return ARGB.color((int) (255 * amount), rgb);
    }

    public static void render(net.minecraftforge.client.gui.overlay.ForgeGui gui, GuiGraphics graphics,
                              float partialTick, int screenWidth, int screenHeight) {
        RenderSystem.enableBlend();
        long now = Util.getMillis();
        float step = lastFrame == 0L ? 0.0F : (now - lastFrame) / 1000.0F;
        lastFrame = now;

        if (captureTarget()) {
            alpha = Math.min(1.0F, alpha + step / FADE_SECONDS);
        } else {
            alpha = Math.max(0.0F, alpha - step / FADE_SECONDS);
        }

        if (alpha <= 0.01F || cached == null) return;
        draw(graphics, alpha, step);
    }

    private static boolean captureTarget() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !player.isUsingItem()) return false;

        ItemStack using = player.getUseItem();
        ItemStack target;

        if (using.getItem() instanceof ItemMortarPestle) {
            target = ItemMortarPestle.findTarget(player, using);
            cachedProgressKey = null;
        } else if (using.getItem() instanceof ItemPathTool tool) {
            target = ItemPathTool.findTarget(player, using);
            cachedProgressKey = tool.progressKey();
            cachedProgressValue = target.isEmpty() ? 0 : tool.progressValue(target);
            cachedUsesLeft = using.getMaxDamage() - using.getDamageValue();
        } else {
            return false;
        }

        if (target.isEmpty() || !(target.getItem() instanceof ItemIngredient item)) return false;

        cached = item.getIngredient();
        cachedCrush = ItemIngredient.crushOf(target);
        cachedTrim = ItemIngredient.trimOf(target);
        cachedRotation = ItemIngredient.rotationOf(target);
        cachedName = target.getHoverName();
        return true;
    }

    private static boolean build() {
        Shape key = new Shape(cached, cachedCrush, cachedTrim, cachedRotation);
        if (key.equals(shape)) return shaped;

        boolean reground = shaped && key.ingredient() == shape.ingredient()
                && key.trim() == shape.trim() && key.rotation() == shape.rotation();

        shape = key;
        shaped = false;
        shapePotential = List.of();

        Ingredients ingredient = cached;
        IngredientPath path = IngredientPath.of(ingredient);

        int dx = Integer.signum(ingredient.getPrimaryAlignment().getX() + ingredient.getSecondaryAlignment().getX());
        int dy = Integer.signum(ingredient.getPrimaryAlignment().getY() + ingredient.getSecondaryAlignment().getY());
        if (path.needsDirection() && dx == 0 && dy == 0) return false;

        double maxPotency = ingredient.getPotency(Ingredients.MAX_CRUSH);
        double nowPotency = ingredient.getPotencyForUnits(cachedCrush);

        List<PathPoint> full = pathFor(path, ingredient, dx, dy, maxPotency, cachedTrim, cachedRotation);
        List<PathPoint> current = pathFor(path, ingredient, dx, dy, nowPotency, cachedTrim, cachedRotation);
        if (full.size() < 2) return false;

        PathGeometry.Fit fit = PathGeometry.fit(full, BOX_WIDTH, BOX_HEIGHT, true);
        shapePotential = fit.apply(full);

        shapeWidth = (int) Math.ceil(fit.width()) + THICKNESS * 2;
        shapeHeight = (int) Math.ceil(fit.height()) + THICKNESS * 2;
        shapeStart = fit.apply(PathPoint.ORIGIN);
        shapeEnding = shapePotential.get(shapePotential.size() - 1);
        shapeTarget = Math.min(PathGeometry.length(shapePotential), PathGeometry.length(current) * fit.scale());
        if (!reground) shapeWalked = shapeTarget;

        shaped = true;
        return true;
    }

    private static void draw(GuiGraphics graphics, float fade, float step) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!build()) return;

        double gap = shapeTarget - shapeWalked;
        shapeWalked = Math.abs(gap) < GLIDE_SNAP
                ? shapeTarget
                : shapeWalked + gap * Math.min(1.0, step / GLIDE_SECONDS);

        List<PathPoint> taken = PathGeometry.truncate(shapePotential, shapeWalked);
        List<PathPoint> remaining = PathGeometry.tail(shapePotential, shapeWalked);

        boolean teleport = IngredientPath.of(cached).teleports();

        Component progress = cachedProgressKey != null
                ? Component.translatable(cachedProgressKey, cachedProgressValue, cachedUsesLeft)
                : Component.translatable("alchemia.tooltip.grind", cachedCrush, Ingredients.MAX_CRUSH_UNITS);

        int textW = Math.max(minecraft.font.width(cachedName), minecraft.font.width(progress));
        int panelW = Math.max(shapeWidth, textW) + PADDING * 2;
        int panelH = shapeHeight + 22 + PADDING * 2;

        int panelX = (graphics.guiWidth() - panelW) / 2;
        int panelY = graphics.guiHeight() * TOP_MARGIN_PERCENT / 100;

        AlchemiaSprites.GRIND_PANEL.blit(graphics, panelX, panelY, panelW, panelH, shade(0xFFFFFF, fade));

        graphics.drawString(minecraft.font, cachedName, panelX + (panelW - minecraft.font.width(cachedName)) / 2,
                panelY + PADDING, shade(LABEL_INK_RGB, fade), false);
        graphics.drawString(minecraft.font, progress, panelX + (panelW - minecraft.font.width(progress)) / 2,
                panelY + PADDING + 10, shade(LABEL_FAINT_RGB, fade), false);

        double originX = panelX + (panelW - shapeWidth) / 2.0 + THICKNESS;
        double originY = panelY + PADDING + 22 + THICKNESS;

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(originX, originY, 0.0);

        PathRenderer.outline(graphics, remaining, THICKNESS, shade(PATH_POTENTIAL, fade), teleport, shapeWalked);
        PathRenderer.dashes(graphics, taken, THICKNESS, shade(PATH_TAKEN, fade), teleport);
        PathRenderer.marker(graphics, shapeEnding.x(), shapeEnding.y(), 3, PATH_ENDING);

        if (taken.size() > 1) {
            PathPoint landing = taken.get(taken.size() - 1);
            PathRenderer.ending(graphics, landing.x(), landing.y(), 3, 1, shade(PATH_LANDING, fade), teleport);
        }

        PathRenderer.dot(graphics, shapeStart.x(), shapeStart.y(), 2, shade(PATH_ORIGIN, fade));

        pose.popPose();
    }

    private static List<PathPoint> pathFor(IngredientPath path, Ingredients ingredient, int dx, int dy,
                                           double potency, int trim, int rotation) {
        if (!path.teleports()) return IngredientPath.pathFor(ingredient, dx, dy, potency, trim, rotation);

        return IngredientPath.rotated(path.teleportPreview(dx, dy,
                IngredientPath.teleportCeiling(ingredient), IngredientPath.reachFor(potency, trim)), rotation);
    }
}
