package muddykat.alchemia.client.gui.cauldron;

import muddykat.alchemia.client.gui.GuiDraw;

import muddykat.alchemia.client.gui.CauldronArt;
import muddykat.alchemia.client.gui.InkTooltip;
import muddykat.alchemia.client.gui.PathRenderer;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.items.helper.PathPoint;
import muddykat.alchemia.common.potion.AlchemiaEffect;
import muddykat.alchemia.common.potion.PotionMap;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import muddykat.alchemia.client.effect.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.Util;
import javax.annotation.Nullable;

import java.util.List;
import java.util.function.Predicate;

public final class EffectNodes {

    private static final int AURA_CORE_ALPHA = 96;
    private static final int LINE_RGB = 0x282828;
    private static final int DORMANT_TINT = 0xFF8E8B86;
    private static final int DORMANT_RGB = 0x5F5C58;
    private static final int DORMANT_FILL_ALPHA = 56;
    private static final int HELD_FILL_ALPHA = 110;
    private static final int DORMANT_EDGE_ALPHA = 90;
    private static final int DORMANT_LIT_RGB = 0xF4E8C6;
    private static final int DORMANT_LIT_ALPHA = 230;
    private static final double DORMANT_GLOW_MS = 180.0;
    private static final double LABEL_ZOOM = 1.2;

    private final BrewContext context;

    private @Nullable PotionMap.MapEntry glowEntry;
    private double glow;

    public EffectNodes(BrewContext context) {
        this.context = context;
    }

    public void trackGlow(@Nullable PathPoint marker, double elapsedMs) {
        PotionMap.MapEntry near = null;
        if (PotionMap.isReady() && marker != null) {
            PotionMap.MapEntry entry = context.map().nearest(marker.x(), marker.y(), PotionMap.EFFECT_RADIUS);
            if (entry != null && context.cauldron().isDormant(entry.effect())) near = entry;
        }

        double step = Math.min(1.0, elapsedMs / DORMANT_GLOW_MS);
        if (near != null && near != glowEntry) {
            glowEntry = near;
            glow = 0.0;
        }
        glow += ((near != null ? 1.0 : 0.0) - glow) * step;
    }

    public @Nullable InkTooltip draw(GuiGraphics graphics, PathPoint centre, Predicate<PathPoint> fogged, int mouseX, int mouseY) {
        TileEntityAlchemyCauldron cauldron = context.cauldron();
        CauldronKnowledge knowledge = context.knowledge();
        MapView view = context.view();
        Font font = context.getFont();
        double zoom = view.zoom();
        int node = view.effectNode();
        int half = node / 2;
        int thickness = Math.max(1, (int) Math.round(2 * Mth.clamp(zoom, 0.5, 2.0)));
        InkTooltip hover = null;

        for (PotionMap.MapEntry entry : context.map().entries()) {
            if (entry.effect().getEffect() == null) continue;

            PathPoint at = view.toScreen(entry.position());
            int nx = at.roundedX();
            int ny = at.roundedY();

            if (nx < view.left() - node || nx > view.right() + node || ny < view.top() - node || ny > view.bottom() + node) continue;

            AlchemiaEffect recipe = entry.effect().getRecipe();
            boolean discovered = knowledge.knows(recipe);
            boolean hovered = !fogged.test(entry.position())
                    && mouseX >= nx - half && mouseX <= nx + half && mouseY >= ny - half && mouseY <= ny + half;

            if (cauldron.isDormant(entry.effect())) {
                drawThread(graphics, view, centre.roundedX(), centre.roundedY(), nx, ny, thickness);
                drawDormant(graphics, entry, nx, ny, node);

                if (hovered) {
                    hover = tooltip(discovered
                            ? Component.translatable("alchemia.gui.dormant.named", entry.effect().getEffect().getDisplayName())
                            : Component.translatable("alchemia.gui.dormant"), knowledge.dormantHint(recipe), null);
                }
                continue;
            }

            drawAura(graphics, view, nx, ny, entry, discovered);
            drawHeldBack(graphics, entry, nx, ny);
            drawThread(graphics, view, centre.roundedX(), centre.roundedY(), nx, ny, thickness);

            if (discovered) {
                GuiDraw.sprite(graphics, recipe.sprite(), nx - half, ny - half, node, node, -1);
            } else if (knowledge.noted(recipe)) {
                EffectGlyphs.openEye(graphics, nx, ny, node);
            } else {
                EffectGlyphs.unknown(graphics, nx, ny, node);
            }

            if (discovered && zoom >= LABEL_ZOOM) {
                Component name = entry.effect().getEffect().getDisplayName();
                graphics.drawString(font, name, nx - font.width(name) / 2, ny + half + 2, CauldronArt.INK, false);
            }

            if (hovered) {
                int cap = cauldron.levelCap(recipe);
                Component held = cap <= entry.effect().getStrength() ? knowledge.levelHint(recipe, cap + 1) : null;

                hover = discovered
                        ? tooltip(entry.effect().getEffect().getDisplayName(), held, recipe.sprite())
                        : tooltip(Component.translatable("alchemia.gui.unknown"),
                        held != null ? held : Component.translatable("alchemia.gui.unknown.hint"), null);
            }
        }
        return hover;
    }

    private static InkTooltip tooltip(Component name, @Nullable Component detail, @Nullable ResourceLocation sprite) {
        InkTooltip tip = InkTooltip.of(name, detail == null ? List.of() : List.of(detail));
        return sprite != null ? tip.withSprite(sprite) : tip;
    }

    private void drawDormant(GuiGraphics graphics, PotionMap.MapEntry entry, int cx, int cy, int node) {
        double reach = PotionMap.EFFECT_RADIUS * context.view().zoom();
        if (reach >= 2.0) {
            PathRenderer.disc(graphics, cx, cy, reach, ARGB.color(DORMANT_FILL_ALPHA, DORMANT_RGB));

            double lit = entry == glowEntry ? glow : 0.0;
            int edgeAlpha = (int) Math.round(Mth.lerp(lit, DORMANT_EDGE_ALPHA, DORMANT_LIT_ALPHA));
            int edgeRgb = ARGB.srgbLerp((float) lit, DORMANT_RGB, DORMANT_LIT_RGB);
            PathRenderer.ring(graphics, cx, cy, reach, ARGB.color(edgeAlpha, edgeRgb), false);
            if (lit > 0.0) PathRenderer.ring(graphics, cx, cy, reach - 1.0, ARGB.color(edgeAlpha / 2, edgeRgb), false);
        }
        AlchemiaEffect recipe = entry.effect().getRecipe();
        CauldronKnowledge knowledge = context.knowledge();
        if (recipe != null && !knowledge.knows(recipe) && knowledge.noted(recipe)) {
            EffectGlyphs.closedEye(graphics, cx, cy, node);
        } else {
            EffectGlyphs.symbol(graphics, cx, cy, node, DORMANT_TINT);
        }
    }

    private void drawHeldBack(GuiGraphics graphics, PotionMap.MapEntry entry, int cx, int cy) {
        int strength = entry.effect().getStrength();
        int cap = context.cauldron().levelCap(entry.effect().getRecipe());
        if (cap > strength) return;

        double radius = (cap == strength ? PotionMap.EFFECT_CORE : PotionMap.CELL * (strength - cap)) * context.view().zoom();
        if (radius < 2.0) return;

        PathRenderer.disc(graphics, cx, cy, radius, ARGB.color(HELD_FILL_ALPHA, DORMANT_RGB));
        PathRenderer.ring(graphics, cx, cy, radius, ARGB.color(DORMANT_EDGE_ALPHA, DORMANT_RGB), false);
    }

    private static void drawAura(GuiGraphics graphics, MapView view, int cx, int cy,
                                 PotionMap.MapEntry entry, boolean discovered) {
        double reach = PotionMap.EFFECT_RADIUS * view.zoom();
        if (reach < 2.0) return;

        if (cx + reach < view.left() || cx - reach > view.right()
                || cy + reach < view.top() || cy - reach > view.bottom()) return;

        int rgb = discovered ? entry.effect().getEffect().getColor() : MapInk.UNKNOWN_RGB;
        double pulse = (Math.sin(Util.getMillis() / 520.0 + cx * 0.35) + 1.0) * 0.5;
        int alpha = (int) Math.round(AURA_CORE_ALPHA * (0.72 + pulse * 0.28));

        PathRenderer.aura(graphics, cx, cy, reach, rgb, alpha);
        PathRenderer.ring(graphics, cx, cy, reach, ARGB.color(Math.min(255, alpha * 2), rgb), true);

        double core = PotionMap.EFFECT_CORE * view.zoom();
        if (core >= 2.0) PathRenderer.ring(graphics, cx, cy, core, ARGB.color(Math.min(255, alpha * 3), rgb), false);
    }

    private static void drawThread(GuiGraphics graphics, MapView view, int x0, int y0, int x1, int y1, int thickness) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length < 1) return;

        int steps = (int) Math.min(72, Math.ceil(length / 3.0));
        if (steps <= 0) return;

        for (int i = 1; i <= steps; i++) {
            double t = (double) i / steps;
            int px = (int) Math.round(x0 + dx * t);
            int py = (int) Math.round(y0 + dy * t);
            if (px < view.left() - thickness || px > view.right() + thickness
                    || py < view.top() - thickness || py > view.bottom() + thickness) continue;
            int alpha = (int) Math.round(12 + t * 120);
            graphics.fill(px - thickness / 2, py - thickness / 2,
                    px - thickness / 2 + thickness, py - thickness / 2 + thickness,
                    ARGB.color(alpha, LINE_RGB));
        }
    }
}
