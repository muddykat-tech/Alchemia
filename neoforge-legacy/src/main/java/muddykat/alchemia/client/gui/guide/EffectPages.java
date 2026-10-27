package muddykat.alchemia.client.gui.guide;

import muddykat.alchemia.client.gui.GuiDraw;

import muddykat.alchemia.common.potion.AlchemiaEffect;
import muddykat.alchemia.common.potion.AlchemiaEffects;
import muddykat.alchemia.common.potion.BrewRecipe;
import muddykat.alchemia.common.potion.RecipeDiscovery;
import muddykat.alchemia.common.potion.WorldEffects;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import static muddykat.alchemia.client.gui.guide.GuideLayout.*;

public final class EffectPages extends GuidePage {

    private @Nullable AlchemiaEffect selectedEffect;
    private int effectPage;

    public EffectPages(GuideContext context) {
        super(context);
    }

    public int page() {
        return effectPage;
    }

    public void showPage(int page) {
        effectPage = page;
    }

    public void select(AlchemiaEffect potion) {
        selectedEffect = potion;
        int index = book.effectOrder().indexOf(potion);
        if (index >= 0) effectPage = index / EFFECTS_PER_PAGE;
    }

    public void refresh() {
        if (selectedEffect != null) selectedEffect = AlchemiaEffects.byId(selectedEffect.id());
    }

    public void draw(GuiGraphics graphics) {
        int y = chapterHeading(graphics, Component.translatable("alchemia.guide.chapter.effects"), LEFT_X, TOP + 2);
        centred(graphics, Component.translatable("alchemia.guide.effects.known", book.discovered().size(), RecipeDiscovery.total()), LEFT_X, y, INK_FAINT);

        List<AlchemiaEffect> shown = book.effectOrder();
        int total = RecipeDiscovery.total();
        int pages = Math.max(1, Mth.ceil(total / (float) EFFECTS_PER_PAGE));
        effectPage = Math.max(0, Math.min(effectPage, pages - 1));
        int first = effectPage * EFFECTS_PER_PAGE;

        int gridX = LEFT_X + (PAGE_W - GRID_COLUMNS * CELL) / 2;
        int gridY = y + 14;
        for (int place = 0; place < Math.min(EFFECTS_PER_PAGE, total - first); place++) {
            int slot = first + place;
            int x = gridX + (place % GRID_COLUMNS) * CELL;
            int cy = gridY + (place / GRID_COLUMNS) * CELL;
            AlchemiaEffect potion = slot < shown.size() ? shown.get(slot) : null;
            boolean isKnown = potion != null && book.discovered().contains(potion);
            boolean over = potion != null && hovered(x, cy, CELL, CELL);
            cell(graphics, x, cy, potion == null ? 3 : potion.equals(selectedEffect) ? 2 : over ? 1 : 0);

            if (potion == null) {
                icon(graphics, GuideIcon.UNKNOWN, x + 3, cy + 3, true);
                hotspot(x, cy, CELL, CELL, null, () -> List.of(Component.translatable("alchemia.guide.effects.undiscovered")));
                continue;
            }
            if (isKnown) {
                GuiDraw.sprite(graphics, potion.sprite(), x + 1, cy + 1, 16, 16);
            } else {
                icon(graphics, GuideIcon.EYE_CLOSED, x + 3, cy + 3, false);
            }
            AlchemiaEffect chosen = potion;
            hotspot(x, cy, CELL, CELL, () -> {
                selectedEffect = chosen;
                resetScroll();
            }, () -> List.of(book.effectName(chosen)));
        }

        int legendY = gridY + EFFECTS_ROWS * CELL + 8;
        wrappedCentred(graphics, Component.translatable("alchemia.guide.effects.legend"), LEFT_X, legendY, INK_FAINT);

        if (pages > 1) {
            Component page = Component.translatable("alchemia.guide.effects.page", effectPage + 1, pages);
            if (effectPage > 0) corner(graphics, false, () -> flipEffects(-1), page);
            if (effectPage < pages - 1) corner(graphics, true, () -> flipEffects(1), page);
        }
        scrollPanel(graphics, TOP, pages > 1 ? BOTTOM - 20 : BOTTOM, () -> effectDetail(graphics));
    }

    private void flipEffects(int by) {
        effectPage += by;
        turnPage();
    }

    private int effectDetail(GuiGraphics graphics) {
        int width = PAGE_W - TRACK_W - 4;
        int y = TOP - (int) scroll();
        if (selectedEffect == null) {
            return wrapped(graphics, Component.translatable("alchemia.guide.effects.select"), RIGHT_X, y + 30, width, INK_FAINT);
        }

        boolean known = book.discovered().contains(selectedEffect);
        AlchemiaEffect shown = selectedEffect;
        y = banner(graphics, book.effectName(shown).getString(), y, width, (x, iy) -> {
            if (known) {
                GuiDraw.sprite(graphics, shown.sprite(), x, iy, 18, 18);
            } else {
                icon(graphics, GuideIcon.EYE_CLOSED, x + 3, iy + 3, false);
            }
        });

        if (known) {
            y = wrapped(graphics, Component.translatable("alchemia.guide.effect.rarity",
                    Component.translatable("alchemia.guide.rarity." + selectedEffect.rarity().getSerializedName())), RIGHT_X, y, width, INK_SOFT);
            y = wrapped(graphics, Component.translatable("alchemia.guide.effect.alignment",
                    GuideItems.alignmentName(selectedEffect.primary()), GuideItems.alignmentName(selectedEffect.secondary())),
                    RIGHT_X, y + 2, width, INK_SOFT) + 4;
            Component description = selectedEffect.descriptionText();
            if (description != null) y = wrapped(graphics, description, RIGHT_X, y, width, INK) + 4;
        } else {
            y = wrapped(graphics, Component.translatable("alchemia.guide.effect.unbrewed"), RIGHT_X, y, width, INK_FAINT) + 4;
        }

        List<Component> lines = WorldEffects.guideLines(book.notes(), selectedEffect);
        if (!lines.isEmpty()) {
            y = scrolledHeading(graphics, Component.translatable("alchemia.guide.effect.conditions"), y + 2, width);
            for (Component line : lines) {
                List<FormattedCharSequence> parts = font().split(line, width - SEAL - 4);
                int rowH = Math.max(SEAL + 2, parts.size() * LINE + 4);
                widget(graphics, RIGHT_X, y + (rowH - SEAL) / 2, EMPTY_SEAL_U, PREPARED_V, SEAL, SEAL);
                icon(graphics, GuideIcon.EYE, RIGHT_X + 3, y + (rowH - SEAL) / 2 + 3, false);
                int textY = y + (rowH - parts.size() * LINE) / 2 + 1;
                for (FormattedCharSequence part : parts) {
                    graphics.drawString(font(), part, RIGHT_X + SEAL + 4, textY, INK, false);
                    textY += LINE;
                }
                y += rowH;
            }
        }

        List<BrewRecipe> using = new ArrayList<>();
        for (BrewRecipe recipe : book.recipes()) {
            for (BrewRecipe.BrewEffect effect : recipe.effects()) {
                if (effect.recipe().equalsIgnoreCase(selectedEffect.id())) {
                    using.add(recipe);
                    break;
                }
            }
        }
        y = scrolledHeading(graphics, Component.translatable("alchemia.guide.effect.recipes"), y + 4, width);
        if (using.isEmpty()) return wrapped(graphics, Component.translatable("alchemia.guide.effect.no_recipes"), RIGHT_X, y, width, INK_FAINT);
        return linkedRecipes(graphics, using, y, width);
    }
}
