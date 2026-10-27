package muddykat.alchemia.client.gui.cauldron;

import muddykat.alchemia.client.gui.GuiDraw;

import muddykat.alchemia.client.gui.CauldronArt;
import muddykat.alchemia.client.gui.InkTooltip;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.blocks.tileentity.cauldron.BrewPotency;
import muddykat.alchemia.common.blocks.tileentity.cauldron.PotencyPlan;
import muddykat.alchemia.common.potion.AlchemiaEffect;
import muddykat.alchemia.common.potion.AlchemiaEffects;
import muddykat.alchemia.common.potion.RecipeDiscovery;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import muddykat.alchemia.client.effect.ARGB;
import net.minecraft.Util;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PotencyGrid {

    public static final int CELL = 18;

    private static final int PER_ROW = 5;
    private static final long POP_MS = 320L;
    private static final long GHOST_MS = 420L;

    private record Slot(String token, Component name, MobEffect effect, boolean redstone, boolean incoming, boolean unknown) {
    }

    private record Ghost(int index, Slot slot, long since) {
    }

    private List<String> tokens;
    private List<Slot> committed = List.of();
    private final Map<Integer, Long> born = new HashMap<>();
    private final List<Ghost> ghosts = new ArrayList<>();

    public static int rows() {
        return Math.max(1, (TileEntityAlchemyCauldron.maxPotency() + PER_ROW - 1) / PER_ROW);
    }

    public @Nullable InkTooltip draw(GuiGraphics graphics, BrewContext context, int centre, int gridY, int mouseX, int mouseY) {
        TileEntityAlchemyCauldron cauldron = context.cauldron();
        watch(slotsOf(context, cauldron.currentPotency(), null));

        int budget = TileEntityAlchemyCauldron.maxPotency();
        int columns = Math.min(budget, PER_ROW);
        int gridX = centre - columns * CELL / 2;
        long now = Util.getMillis();
        List<Slot> filled = filled(context);
        InkTooltip hovered = null;

        for (int index = 0; index < budget; index++) {
            int x = gridX + (index % PER_ROW) * CELL;
            int y = gridY + (index / PER_ROW) * CELL;
            Long bornAt = born.get(index);
            double pop = bornAt == null ? 1.0 : Math.min(1.0, (now - bornAt) / (double) POP_MS);
            boolean taken = index < filled.size();
            boolean incoming = taken && filled.get(index).incoming();

            CauldronArt.cell(graphics, x, y, pop < 1.0 || incoming ? CauldronArt.Cell.GILT : CauldronArt.Cell.PLAIN);
            if (!taken) continue;

            Slot slot = filled.get(index);
            double grow = 1.0 + 0.45 * (1.0 - pop) * (1.0 - pop);
            double beat = (Math.sin(now / 300.0) + 1.0) * 0.5;
            int alpha = incoming ? (int) Math.round(80 + beat * 150) : 255;
            drawIcon(graphics, slot, x + 9, y + 9, grow, alpha);

            if (mouseX >= x && mouseX < x + CELL && mouseY >= y && mouseY < y + CELL) {
                hovered = slot.redstone() || slot.unknown()
                        ? InkTooltip.of(slot.name(), List.of())
                        : InkTooltip.of(slot.name(), List.of()).withSprite(AlchemiaEffects.sprite(slot.effect()));
            }
        }

        for (Ghost ghost : ghosts) {
            if (ghost.index() >= budget) continue;
            double t = Math.min(1.0, (now - ghost.since()) / (double) GHOST_MS);
            int x = gridX + (ghost.index() % PER_ROW) * CELL;
            int y = gridY + (ghost.index() / PER_ROW) * CELL;
            drawIcon(graphics, ghost.slot(), x + 9, y + 9 - (int) Math.round(t * 7), 1.0 - 0.3 * t,
                    (int) Math.round(255 * (1.0 - t)));
        }
        return hovered;
    }

    private void watch(List<Slot> now) {
        long millis = Util.getMillis();
        born.values().removeIf(since -> millis - since > POP_MS);
        ghosts.removeIf(ghost -> millis - ghost.since() > GHOST_MS);

        if (tokens == null) {
            tokens = now.stream().map(Slot::token).toList();
            committed = now;
            return;
        }

        List<String> next = now.stream().map(Slot::token).toList();
        if (next.equals(tokens)) return;

        Set<String> before = new HashSet<>(tokens);
        Set<String> after = new HashSet<>(next);
        for (int index = 0; index < next.size(); index++) {
            if (!before.contains(next.get(index))) born.put(index, millis);
        }
        for (int index = 0; index < committed.size(); index++) {
            Slot gone = committed.get(index);
            if (!after.contains(gone.token())) ghosts.add(new Ghost(index, gone, millis));
        }
        tokens = next;
        committed = now;
    }

    private static List<Slot> filled(BrewContext context) {
        PotencyPlan preview = BrewHints.preview(context);
        PotencyPlan plan = preview == null ? context.cauldron().currentPotency() : preview;
        MobEffect brewing = preview == null ? null : preview.effects().get(preview.effects().size() - 1).getEffect();
        return slotsOf(context, plan, brewing);
    }

    private static List<Slot> slotsOf(BrewContext context, PotencyPlan plan,
                                      @Nullable MobEffect brewing) {
        TileEntityAlchemyCauldron cauldron = context.cauldron();
        List<Slot> slots = new ArrayList<>();
        int held = brewing == null ? 0 : cauldron.heldPotency(brewing);
        int bonus = plan.redstone() * BrewPotency.REDSTONE_DURATION_BONUS;
        int charges = 0;

        for (String token : plan.order()) {
            if (token.equals(PotencyPlan.REDSTONE_TOKEN)) {
                slots.add(new Slot(token + "#" + charges++, Component.translatable("alchemia.gui.redstone", bonus / 20),
                        null, true, false, false));
                continue;
            }

            for (MobEffectInstance instance : plan.effects()) {
                if (!muddykat.alchemia.common.utility.EffectCodecs.name(instance.getEffect()).equals(token)) continue;

                AlchemiaEffect recipe = RecipeDiscovery.byEffect(instance.getEffect());
                boolean unknown = !context.knowledge().knows(recipe);
                Component name = Component.translatable("alchemia.gui.effect.entry",
                        unknown ? Component.translatable("alchemia.gui.unknown") : instance.getEffect().getDisplayName(), instance.getAmplifier() + 1,
                        (instance.getDuration() + bonus) / 20);
                boolean fresh = instance.getEffect().equals(brewing);
                for (int i = 0; i <= instance.getAmplifier(); i++) {
                    slots.add(new Slot(token + "#" + i, name, instance.getEffect(), false, fresh && i >= held, unknown));
                }
            }
        }
        return slots;
    }

    private static void drawIcon(GuiGraphics graphics, Slot slot, int cx, int cy, double scale, int alpha) {
        if (alpha < 6) return;
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(cx, cy, 0.0F);
        pose.scale((float) scale, (float) scale, 1.0F);
        if (slot.redstone()) {
            CauldronArt.icon(graphics, CauldronArt.Icon.REDSTONE, -6, -6, false, alpha);
        } else if (slot.unknown()) {
            EffectGlyphs.symbol(graphics, 0, 0, 16, EffectGlyphs.fadedUnknown(alpha));
        } else {
            GuiDraw.sprite(graphics, AlchemiaEffects.sprite(slot.effect()), -8, -8, 16, 16, ARGB.color(alpha, 0xFFFFFF));
        }
        pose.popPose();
    }
}
