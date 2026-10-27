package muddykat.alchemia.client.gui.cauldron;

import muddykat.alchemia.client.gui.CauldronArt;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.potion.BrewBase;
import muddykat.alchemia.common.potion.CauldronHeat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import muddykat.alchemia.client.effect.ARGB;
import net.minecraft.util.Mth;

import java.util.concurrent.ThreadLocalRandom;

public final class HeatGauge {

    public static final int BOIL_GLOW_RGB = 0xFFE9A8;

    private static final long TRUST_MS = 300L;
    private static final long STRAIN_MS = 320L;

    public enum Stoke { IGNORED, HEATED, BOILED, STRAINED }

    @FunctionalInterface
    public interface Sounds {
        void play(SoundEvent sound, float pitch, float volume);
    }

    private double heat;
    private double shown;
    private long frame;
    private long heldUntil;
    private long boiledAt;
    private long strainAt;
    private long nextBubble;
    private long stokedAt;
    private double lastDt;

    public boolean boiling() {
        return boiledAt != 0L;
    }

    public double shown() {
        return shown;
    }

    public double lastDt() {
        return lastDt;
    }

    public double intensity(double cap) {
        return cap <= 0.0 ? 0.0 : Mth.clamp(shown / cap, 0.0, 1.0);
    }

    public Stoke stoke(long now, double cap, boolean ready, Sounds sounds) {
        if (boiledAt != 0L) return Stoke.IGNORED;

        stokedAt = now;
        heat = Math.min(cap, heat + CauldronHeat.PUMP);
        heldUntil = now + CauldronHeat.HOLD_MS;
        sounds.play(SoundEvents.FIRECHARGE_USE, 1.3F + (float) intensity(cap) * 0.4F, 0.18F);

        if (heat < cap - 1.0E-6) return Stoke.HEATED;
        if (ready) {
            boiledAt = now;
            return Stoke.BOILED;
        }
        heat = Math.max(0.0, cap - CauldronHeat.STRAIN_GAP);
        strainAt = now;
        sounds.play(SoundEvents.FIRE_EXTINGUISH, 1.6F, 0.15F);
        return Stoke.STRAINED;
    }

    public boolean tick(long now, boolean usable, double cap, double server, boolean serverBoil, Sounds sounds) {
        double dt = frame == 0L ? 0.0 : Math.min(0.1, (now - frame) / 1000.0);
        frame = now;
        lastDt = dt;

        if (boiledAt != 0L && now - boiledAt >= CauldronHeat.BOIL_MS) {
            boiledAt = 0L;
            heat = cap * CauldronHeat.BOIL_REST_SHARE;
            heldUntil = now + CauldronHeat.HOLD_MS;
        }
        heat = Math.min(heat, cap);
        if (!usable) {
            boiledAt = 0L;
            heat = Math.max(0.0, heat - CauldronHeat.COOL_PER_SECOND * 4.0 * dt);
        } else if (boiledAt == 0L && now >= heldUntil) {
            heat = Math.max(0.0, heat - CauldronHeat.COOL_PER_SECOND * dt);
        }

        if (now - stokedAt > TRUST_MS && boiledAt == 0L) heat += (server - heat) * Math.min(1.0, dt * 6.0);
        boolean boiledByServer = serverBoil && boiledAt == 0L && now - stokedAt < CauldronHeat.BOIL_MS * 2;
        if (boiledByServer) boiledAt = now;

        double rate = heat > shown ? 16.0 : 7.0;
        shown += (heat - shown) * (1.0 - Math.exp(-rate * dt));

        double hot = intensity(cap);
        if (hot > 0.3 && now >= nextBubble) {
            float pitch = 0.8F + ThreadLocalRandom.current().nextFloat() * 0.5F;
            sounds.play(SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, pitch, 0.08F + (float) hot * 0.22F);
            nextBubble = now + Math.round(900 - 650 * hot) + ThreadLocalRandom.current().nextInt(160);
        }
        return boiledByServer;
    }

    public void drawVial(GuiGraphics graphics, TileEntityAlchemyCauldron cauldron, int x, int y,
                         double cap, boolean ready, long millis) {
        CauldronArt.vial(graphics, x, y);

        int level = cauldron.getWaterLevel();
        int filled = liquidWidth(level);
        if (level > 0) {
            int rgb = cauldron.isSpoiled() ? 0x55603E : cauldron.getPotionColor() & 0xFFFFFF;
            int inner = CauldronArt.VIAL_W - 4;
            int top = y + 2;
            int rows = CauldronArt.VIAL_H - 4;
            double radius = CauldronArt.VIAL_H / 2.0 - 2.0;

            for (int row = 0; row < rows; row++) {
                double dy = row + 0.5 - rows / 2.0;
                int inset = (int) Math.ceil(radius - Math.sqrt(Math.max(0.0, radius * radius - dy * dy)));
                int from = x + 2 + inset;
                int to = x + 2 + Math.min(filled, inner - inset);
                if (to <= from) continue;
                double light = row == 0 ? 0.35 : row == 1 ? 0.15 : row >= rows - 1 ? -0.3 : 0.0;
                int shade = light >= 0 ? ARGB.srgbLerp((float) light, rgb, 0xFFFFFF) : ARGB.srgbLerp((float) -light, rgb, 0x000000);
                graphics.fill(from, top + row, to, top + row + 1, ARGB.opaque(shade));
            }

            if (!cauldron.isSpoiled()) {
                drawBubbles(graphics, x, top, rows, filled, rgb, millis, intensity(cap));
                if (boiledAt != 0L) {
                    double flash = 1.0 - Math.min(1.0, (millis - boiledAt) / (double) CauldronHeat.BOIL_MS);
                    graphics.fill(x + 2, top, x + 2 + filled, top + rows, ARGB.color((int) Math.round(150 * flash), BOIL_GLOW_RGB));
                }
            }
        }

        CauldronArt.vialGlass(graphics, x, y);
        if (level > 0 && !cauldron.isSpoiled()) drawGauge(graphics, x, y, filled, cap, ready, millis);
    }

    private static int liquidWidth(int level) {
        int inner = CauldronArt.VIAL_W - 4;
        return Math.max(4, Math.round(inner * Math.min(1.0F, (float) level / BrewBase.MAX_FILL_LEVEL)));
    }

    private static double hash(int seed, long salt) {
        long h = seed * 0x9E3779B97F4A7C15L + salt * 0xC2B2AE3D27D4EB4FL;
        h ^= h >>> 31;
        h *= 0xBF58476D1CE4E5B9L;
        h ^= h >>> 29;
        return (h >>> 11) * 0x1.0p-53;
    }

    private static void drawBubbles(GuiGraphics graphics, int x, int top, int rows, int filled, int rgb, long millis, double hot) {
        int bright = ARGB.opaque(ARGB.srgbLerp(0.6F, rgb, 0xFFFFFF));
        int pale = ARGB.opaque(ARGB.srgbLerp(0.35F, rgb, 0xFFFFFF));
        int left = x + 3;
        int right = x + 2 + filled - 2;
        int span = Math.max(1, right - left);
        int count = 6 + (int) Math.round(hot * 14);
        double cycle = 1150.0 - 720.0 * hot;

        for (int bubble = 0; bubble < count; bubble++) {
            double speed = 0.6 + 0.8 * hash(bubble, 1);
            double t = millis * speed / cycle + hash(bubble, 2) * 16.0;
            long generation = (long) Math.floor(t);
            double phase = t - generation;
            long salt = generation * 8 + 3;
            double wander = Math.sin(phase * Math.PI * (1.5 + 2.5 * hash(bubble, salt + 1)) + bubble) * (0.5 + hot * 1.8);
            int bx = Mth.clamp(left + (int) Math.round(hash(bubble, salt) * span + wander), left, right);
            int by = top + rows - 1 - (int) Math.floor(phase * rows);
            if (by < top) continue;

            boolean large = hot > 0.4 && hash(bubble, salt + 2) < (hot - 0.4) * 1.1;
            int colour = by == top ? ARGB.color(220, 0xFFFFFF) : large ? pale : bright;
            graphics.fill(bx, by, Math.min(right + 1, bx + (large ? 2 : 1)), by + 1, colour);
            if (large && by > top) graphics.fill(bx, by - 1, bx + 1, by, bright);
        }

        if (hot > 0.15) {
            long beat = millis / (long) Math.round(110 - 60 * hot);
            for (int column = left; column <= right; column++) {
                if (hash(column, beat) < hot * 0.22) graphics.fill(column, top, column + 1, top + 1, bright);
            }
        }

        int glint = x + 2 + (int) ((millis / (45 - 20 * hot)) % Math.max(1, filled + 20)) - 10;
        if (glint > x + 3 && glint < x + 2 + filled - 2) {
            graphics.fill(glint, top, glint + 2, top + 1, ARGB.color(200, 0xFFFFFF));
        }
    }

    private void drawGauge(GuiGraphics graphics, int x, int y, int filled, double cap, boolean ready, long millis) {
        boolean boiling = boiledAt != 0L;
        int last = x + 1 + filled;

        double beat = (Math.sin(millis / 220.0) + 1.0) * 0.5;
        int markAlpha = ready ? (int) Math.round(150 + beat * 105) : 110;
        graphics.fill(last, y - 3, last + 1, y + 1, ARGB.color(markAlpha, CauldronArt.INK_RED & 0xFFFFFF));
        graphics.fill(last - 1, y - 3, last + 2, y - 2, ARGB.color(markAlpha, CauldronArt.INK_RED & 0xFFFFFF));

        double hot = intensity(cap);
        if (hot > 0.55) {
            int puffs = (int) Math.round((hot - 0.55) * 14);
            for (int puff = 0; puff < puffs; puff++) {
                double phase = (millis / 700.0 + hash(puff, 9) * 4.0) % 1.0;
                int px = x + 4 + (int) Math.round(hash(puff, (long) (millis / 700.0 + hash(puff, 9) * 4.0)) * Math.max(1, filled - 6));
                int py = y - 1 - (int) Math.floor(phase * 6);
                graphics.fill(px, py, px + 1, py + 1, ARGB.color((int) Math.round(140 * (1.0 - phase)), 0xFFFFFF));
            }
        }

        double strain = strainAt == 0L ? 1.0 : Math.min(1.0, (millis - strainAt) / (double) STRAIN_MS);
        int shake = strain < 1.0 ? (int) Math.round(Math.sin(strain * Math.PI * 6) * 2 * (1.0 - strain)) : 0;
        int travel = (int) Math.round(Mth.clamp(shown, 0.0, cap) * (CauldronArt.VIAL_W - 4));
        int notchX = Mth.clamp(x + 1 + Math.max(1, travel), x + 2, last) + shake;
        graphics.fill(notchX, y + 1, notchX + 1, y + CauldronArt.VIAL_H - 1, ARGB.color(boiling ? 230 : 150, 0x2A1D13));
        CauldronArt.notch(graphics, notchX, y - CauldronArt.NOTCH_H + 1,
                boiling || (ready && shown > cap - CauldronHeat.STRAIN_GAP - 0.03));
    }
}
