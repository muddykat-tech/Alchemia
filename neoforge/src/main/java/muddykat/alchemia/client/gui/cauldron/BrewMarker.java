package muddykat.alchemia.client.gui.cauldron;

import muddykat.alchemia.client.gui.AlchemiaSprites;
import muddykat.alchemia.client.gui.PathRenderer;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import org.joml.Matrix3x2fStack;

import java.util.List;
import java.util.Optional;

public final class BrewMarker {

    private static final int STRAIN_ALPHA = 230;
    private static final double HEALTH_EASE_PER_SECOND = 4.0;
    private static final long STRAIN_PEAK_MS = 450L;
    private static final long PUFF_MS = 1200L;
    private static final long PUFF_STAGGER_MS = 110L;
    private static final int PUFF_COUNT = 5;
    private static final double SHAKE_HEAT = 0.7;
    private static final float SHAKE_PIVOT_Y = 4.0F;

    private double healthShown = 1.0;
    private long healthShownAt;
    private int healthInstability;
    private long strainPeakUntil;
    private long puffAt;
    private int puffRgb;

    public void puff(int rgb, long now) {
        puffRgb = rgb;
        puffAt = now;
    }

    public void draw(GuiGraphicsExtractor graphics, int x, int y, TileEntityAlchemyCauldron cauldron, int frame,
                     double scale, double spin, double mapScale, double hot, boolean boiling, boolean splash, long millis) {
        if (scale <= 0.01) return;

        double pulse = (Math.sin(millis / 380.0) + 1.0) * 0.5;
        int radius = (int) Math.round(frame * 0.55 + pulse * 3.0);
        int alpha = (int) Math.round(70 + pulse * 90);

        double tremble = boiling ? 1.4 : hot >= SHAKE_HEAT ? hot : 0.0;
        double amount = tremble < 0.05 ? 0.0 : tremble * 0.4;
        float shakeX = (float) ((Math.sin(millis * 0.11) + Math.sin(millis * 0.173 + 1.3)) * 0.5 * amount);
        float shakeY = (float) ((Math.sin(millis * 0.131 + 0.7) + Math.sin(millis * 0.191 + 2.1)) * 0.5 * amount);
        float wobble = (float) ((Math.sin(millis * 0.09) + Math.sin(millis * 0.141 + 0.9) * 0.5) / 1.5 * 0.11 * tremble);

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x + shakeX, y + shakeY);
        pose.rotate((float) spin);
        pose.scale((float) scale, (float) scale);
        pose.scale((float) mapScale, (float) mapScale);
        pose.translate(0.0F, SHAKE_PIVOT_Y);
        pose.rotate(wobble);
        pose.translate(0.0F, -SHAKE_PIVOT_Y);

        graphics.outline(-radius, -radius, radius * 2, radius * 2, ARGB.color(alpha, 0x2A1F14));
        graphics.outline(-radius - 1, -radius - 1, radius * 2 + 2, radius * 2 + 2, ARGB.color(alpha / 3, 0x2A1F14));
        double strain = 1.0 - shownHealth(cauldron, millis);
        drawStrainMeter(graphics, radius, strain, ARGB.color(STRAIN_ALPHA, MapSurface.DEAD_RGB));
        drawStrainMeter(graphics, radius + 1, strain, ARGB.color(STRAIN_ALPHA / 3, MapSurface.DEAD_RGB));
        ItemStack marker = new ItemStack(splash ? Items.SPLASH_POTION : Items.POTION);
        marker.set(DataComponents.POTION_CONTENTS,
                new PotionContents(Optional.empty(), Optional.of(cauldron.getPotionColor()), List.of(), Optional.empty()));
        if (cauldron.isPrimed()) {
            marker.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            int glow = (int) Math.round(170 + 55 * Math.sin(millis / 260.0));
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, AlchemiaSprites.RADIANT_HALO, -16, -16, 32, 32, ARGB.color(glow, 0xFFFFFF));
        }
        graphics.item(marker, -8, -8);

        pose.popMatrix();
    }

    public void drawPuff(GuiGraphicsExtractor graphics, int x, int y, double mapScale, long millis) {
        if (puffAt == 0L) return;

        long elapsed = millis - puffAt;
        if (elapsed >= PUFF_MS + PUFF_COUNT * PUFF_STAGGER_MS) {
            puffAt = 0L;
            return;
        }

        for (int index = 0; index < PUFF_COUNT; index++) {
            double t = (elapsed - index * PUFF_STAGGER_MS) / (double) PUFF_MS;
            if (t <= 0.0 || t >= 1.0) continue;

            double side = index % 2 == 0 ? 1.0 : -1.0;
            double drift = side * (1.0 + index * 0.7) * t * 5.0 + Math.sin(t * 5.0 + index) * 1.5;
            double rise = 4.0 + t * (14.0 + index * 2.0);
            double radius = 2.0 + t * (4.0 + index * 0.6);
            double fade = Math.pow(1.0 - t, 1.3) * Math.min(1.0, t * 8.0);
            int alpha = (int) Math.round(140 * fade);
            if (alpha < 3) continue;

            double cx = x + drift * mapScale;
            double cy = y - (8 + rise) * mapScale;
            radius *= mapScale;
            PathRenderer.disc(graphics, cx, cy, radius, ARGB.color(alpha, puffRgb));
            PathRenderer.disc(graphics, cx - radius * 0.2, cy - radius * 0.25, radius * 0.55, ARGB.color(alpha / 2, ARGB.opaque(lighten(puffRgb))));
        }
    }

    private static void drawStrainMeter(GuiGraphicsExtractor graphics, int radius, double strain, int colour) {
        if (strain <= 0.01) return;
        int size = radius * 2;
        int risen = (int) Math.round(Mth.clamp(strain, 0.0, 1.0) * size);
        if (risen <= 0) return;

        graphics.fill(-radius, radius - 1, radius, radius, colour);
        graphics.fill(-radius, radius - risen, -radius + 1, radius - 1, colour);
        graphics.fill(radius - 1, radius - risen, radius, radius - 1, colour);
        if (risen >= size) graphics.fill(-radius + 1, -radius, radius - 1, -radius + 1, colour);
    }

    private double shownHealth(TileEntityAlchemyCauldron cauldron, long now) {
        double target = cauldron.getBrewHealth();
        int instability = cauldron.getInstability();
        if (healthShownAt != 0L && instability > healthInstability) strainPeakUntil = now + STRAIN_PEAK_MS;
        healthInstability = instability;

        if (now < strainPeakUntil) {
            healthShown = 0.0;
        } else if (healthShownAt == 0L || strainPeakUntil != 0L) {
            healthShown = target;
            strainPeakUntil = 0L;
        } else {
            double seconds = Math.min(0.25, (now - healthShownAt) / 1000.0);
            double step = HEALTH_EASE_PER_SECOND * seconds;
            healthShown += Mth.clamp(target - healthShown, -step, step);
        }
        healthShownAt = now;
        return healthShown;
    }

    private static int lighten(int rgb) {
        int r = (rgb >> 16 & 0xFF) + (255 - (rgb >> 16 & 0xFF)) / 2;
        int g = (rgb >> 8 & 0xFF) + (255 - (rgb >> 8 & 0xFF)) / 2;
        int b = (rgb & 0xFF) + (255 - (rgb & 0xFF)) / 2;
        return r << 16 | g << 8 | b;
    }
}
