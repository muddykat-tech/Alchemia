package muddykat.alchemia.client.gui.cauldron;

import muddykat.alchemia.client.gui.CauldronArt;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.items.helper.PathPoint;
import muddykat.alchemia.common.potion.AlchemiaEffect;
import muddykat.alchemia.common.potion.PotionMap;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class BrewReadout {

    public static final int HEIGHT = 50;

    private static final long FADE_MS = 180L;
    private static final int GLOW_RGB = HeatGauge.BOIL_GLOW_RGB;

    private enum Symbol { NONE, UNKNOWN, DORMANT, NOTED }

    private record Reading(@Nullable Identifier sprite, Symbol symbol, Component title, int titleColour, List<Component> lines) {
    }

    private final LandingPulse landing;
    private @Nullable Object key;
    private long at = Long.MIN_VALUE;
    private @Nullable PathPoint pos;

    public BrewReadout(LandingPulse landing) {
        this.landing = landing;
    }

    public void draw(GuiGraphicsExtractor graphics, BrewContext context, int left, int y, int width) {
        Reading reading = read(context);
        if (reading == null) return;

        Font font = context.getFont();
        PathPoint brewAt = context.brewAt();
        long now = Util.getMillis();
        List<Object> next = List.of(reading.title().getString(), reading.lines().stream().map(Component::getString).toList());
        if (!next.equals(key)) {
            boolean moved = pos != null && pos.distanceTo(brewAt) > 1.0E-6;
            if (key != null && moved && reading.titleColour() == CauldronArt.INK) landing.trigger(context);
            key = next;
            at = now;
        }
        pos = brewAt;
        double fade = Math.min(1.0, (now - at) / (double) FADE_MS);
        int alpha = (int) Math.round(255 * fade);
        if (alpha < 8) return;

        double flash = landing.flash(now);
        if (flash < 1.0) {
            int glow = (int) Math.round(90 * (1.0 - flash));
            graphics.fill(left - 2, y - 2, left + width + 2, y + 18, ARGB.color(glow, GLOW_RGB));
        }

        boolean iconed = reading.sprite() != null || reading.symbol() != Symbol.NONE;
        if (reading.sprite() != null) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, reading.sprite(), left, y, 16, 16, ARGB.color(alpha, 0xFFFFFF));
        } else if (reading.symbol() == Symbol.DORMANT) {
            EffectGlyphs.closedEye(graphics, left + 8, y + 8, 16);
        } else if (reading.symbol() == Symbol.NOTED) {
            EffectGlyphs.openEye(graphics, left + 8, y + 8, 16);
        } else if (reading.symbol() == Symbol.UNKNOWN) {
            EffectGlyphs.symbol(graphics, left + 8, y + 8, 16, EffectGlyphs.fadedUnknown(alpha));
        }

        int textX = iconed ? left + 20 : left;
        int titleW = width - (textX - left);
        List<FormattedCharSequence> title = font.split(reading.title(), titleW);
        int titleY = title.size() > 1 ? y - 1 : y + 4;
        for (int line = 0; line < Math.min(2, title.size()); line++) {
            graphics.text(font, title.get(line), textX, titleY + line * 9, ARGB.color(alpha, reading.titleColour() & 0xFFFFFF), false);
        }

        int lineY = y + 20;
        int bottom = y + HEIGHT;
        for (Component line : reading.lines()) {
            for (FormattedCharSequence wrapped : font.split(line, width)) {
                if (lineY + 9 > bottom) return;
                graphics.text(font, wrapped, left, lineY, ARGB.color(alpha, CauldronArt.INK_SOFT & 0xFFFFFF), false);
                lineY += 9;
            }
        }
    }

    private static @Nullable Reading read(BrewContext context) {
        TileEntityAlchemyCauldron cauldron = context.cauldron();
        if (cauldron.getWaterLevel() <= 0) {
            return new Reading(null, Symbol.NONE, Component.translatable("alchemia.gui.effects.dry"), CauldronArt.INK_FAINT,
                    List.of(Component.translatable("alchemia.gui.readout.fill")));
        }
        if (!PotionMap.isReady()) return null;

        PathPoint brewAt = context.brewAt();
        CauldronKnowledge knowledge = context.knowledge();
        TileEntityAlchemyCauldron.BrewCheck check = BrewHints.check(context);
        PotionMap.PotionEffectPosition found = cauldron.effectAt(brewAt);
        if (check == TileEntityAlchemyCauldron.BrewCheck.UNAVAILABLE) return null;
        boolean caught = context.map().whirlpoolAt(brewAt) != null;
        if (check == TileEntityAlchemyCauldron.BrewCheck.NOTHING || found.getEffect() == null) {
            if (caught) {
                return new Reading(null, Symbol.NONE, Component.translatable("alchemia.gui.readout.whirl"), CauldronArt.INK_SOFT,
                        List.of(Component.translatable("alchemia.gui.readout.whirl.hint")));
            }
            return new Reading(null, Symbol.NONE, Component.translatable("alchemia.gui.readout.nothing"), CauldronArt.INK_FAINT,
                    List.of(Component.translatable("alchemia.gui.readout.nothing.hint")));
        }

        AlchemiaEffect recipe = found.getRecipe();
        boolean discovered = knowledge.knows(recipe);
        Component name = discovered ? found.getEffect().value().getDisplayName() : Component.translatable("alchemia.gui.unknown");
        Identifier sprite = discovered ? recipe.sprite() : null;
        Symbol symbol = discovered ? Symbol.NONE : knowledge.noted(recipe) ? Symbol.NOTED : Symbol.UNKNOWN;
        List<Component> lines = new ArrayList<>();

        switch (check) {
            case DORMANT -> {
                Component hint = knowledge.dormantHint(recipe);
                lines.add(hint != null ? hint : Component.translatable("alchemia.brew.dormant"));
                return new Reading(null, Symbol.DORMANT, discovered
                        ? Component.translatable("alchemia.gui.dormant.named", name) : Component.translatable("alchemia.gui.dormant"),
                        CauldronArt.INK_SOFT, lines);
            }
            case WEAKER -> lines.add(Component.translatable("alchemia.brew.weaker"));
            case TOO_POTENT -> lines.add(Component.translatable("alchemia.brew.too_potent"));
            default -> {
                lines.add(Component.translatable(found.isPerfect() ? "alchemia.gui.readout.perfect" : "alchemia.gui.readout.level",
                        roman(found.getStrength() + 1)));
                Component rotation = BrewHints.rotationNote(context);
                if (rotation != null) lines.add(rotation);
            }
        }
        Component held = check == TileEntityAlchemyCauldron.BrewCheck.READY || check == TileEntityAlchemyCauldron.BrewCheck.WEAKER
                ? BrewHints.heldBack(context) : null;
        if (held != null) lines.add(held);

        if (caught) lines.add(Component.translatable("alchemia.gui.readout.whirl"));
        int colour = check == TileEntityAlchemyCauldron.BrewCheck.READY ? CauldronArt.INK : CauldronArt.INK_SOFT;
        return new Reading(sprite, symbol, name, colour, lines);
    }

    private static String roman(int value) {
        String[] numerals = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return value >= 1 && value <= numerals.length ? numerals[value - 1] : String.valueOf(value);
    }
}
