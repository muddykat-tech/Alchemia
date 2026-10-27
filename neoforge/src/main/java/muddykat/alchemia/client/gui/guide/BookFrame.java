package muddykat.alchemia.client.gui.guide;

import muddykat.alchemia.common.network.packets.PacketGuideAction;
import muddykat.alchemia.common.potion.BrewRecipe;
import muddykat.alchemia.registration.registers.DataComponentRegistry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static muddykat.alchemia.client.gui.guide.GuideLayout.*;

public final class BookFrame extends GuidePage {

    private static final int RIBBON_U = 380;
    private static final int RIBBON_W = 9;
    private static final int RIBBON_BODY_H = 24;
    private static final int RIBBON_TAIL_V = 26;
    private static final int RIBBON_TAIL_H = 5;
    private static final int RIBBON_X = (BOOK_W - 1) / 2 - RIBBON_W / 2;
    private static final int PAGE_STACK = 3;
    private static final int RIBBON_OFFSET = 20;
    private static final int TAB_STRAIGHT_U = 204;
    private static final int TAB_FOLDED_U = 154;
    private static final int TAB_ROW_V = 92;
    private static final int TAB_ROW_STEP = 26;
    private static final int TAB_DROP = 3;
    private static final int TAB_PATCH_X = 23;
    private static final int TAB_PATCH_Y = 2;
    private static final int TAB_REACH = 4;
    private static final int TAB_H = 22;
    private static final int TAB_TOP = 20;
    private static final int TAB_SPACING = 26;
    private static final int SEAL_U = 54;
    private static final int SEAL_V = 160;
    private static final int RIBBON_ANCHOR = 6;
    private static final String RECIPE_AT = "recipe:";
    private static final String RECIPES_AT = "recipes:";
    private static final String PRIMER_AT = "primer:";
    private static final String EFFECTS_AT = "effects:";

    private final PrimerPages primer;
    private final RecipeIndex index;
    private final EffectPages effects;
    private final IngredientPages ingredients;
    private final OpusPages opus;

    public BookFrame(GuideContext context, PrimerPages primer, RecipeIndex index, EffectPages effects,
                     IngredientPages ingredients, OpusPages opus) {
        super(context);
        this.primer = primer;
        this.index = index;
        this.effects = effects;
        this.ingredients = ingredients;
        this.opus = opus;
    }

    public void draw(GuiGraphicsExtractor graphics) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, BOOK, 0, 0, 0, 0, BOOK_W, BOOK_H, BOOK_SHEET_W, BOOK_SHEET_H);
        if (!pageMode()) extractTabs(graphics);
        ribbon(graphics);
        seal(graphics, BOOK_W - 13, -SEAL_RISE, false, context::onClose, Component.translatable("alchemia.guide.close"));
    }

    private void seal(GuiGraphicsExtractor graphics, int x, int y, boolean back, Runnable action, Component tooltip) {
        boolean over = hovered(x + 1, y + 1, SEAL - 2, SEAL - 2);
        int index = (back ? 2 : 0) + (over ? 1 : 0);
        widget(graphics, x, y, SEAL_U + index * (SEAL + 1), SEAL_V, SEAL, SEAL);
        hotspot(x + 1, y + 1, SEAL - 2, SEAL - 2, action, () -> List.of(tooltip));
    }

    private void ribbon(GuiGraphicsExtractor graphics) {
        if (pageMode()) return;
        String marked = markedLocation();
        String here = location();
        boolean anchorOver = hovered(RIBBON_X, 0, RIBBON_W, PAGE_TOP_EDGE + RIBBON_ANCHOR);
        if (anchorOver) graphics.fill(RIBBON_X, 0, RIBBON_X + RIBBON_W, PAGE_TOP_EDGE + RIBBON_ANCHOR, 0x30FFFFFF);
        boolean placedHere = here.equals(book.bookmark());
        hotspot(RIBBON_X, 0, RIBBON_W, PAGE_TOP_EDGE + RIBBON_ANCHOR, () -> setBookmark(placedHere ? null : here),
                () -> List.of(Component.translatable(placedHere ? "alchemia.guide.bookmark.clear" : "alchemia.guide.bookmark.set")));
        if (marked == null) return;

        if (marked.equals(here)) {
            hangRibbon(graphics, RIBBON_X, PAGE_TOP_EDGE + 1);
            graphics.fill(RIBBON_X + RIBBON_W, PAGE_TOP_EDGE, RIBBON_X + RIBBON_W + 1, PAGE_BOTTOM_EDGE + 1, 0x30000000);
            return;
        }

        boolean ahead = position(here) < position(marked);
        int x = RIBBON_X + (ahead ? RIBBON_OFFSET : -RIBBON_OFFSET);
        int top = PAGE_BOTTOM_EDGE + 1;
        hangRibbon(graphics, x, top);
        bookPart(graphics, x, top, x, top, RIBBON_W, PAGE_STACK);
        graphics.fill(x, top + PAGE_STACK, x + RIBBON_W, top + PAGE_STACK + 1, 0x60000000);
        graphics.fill(x, top + PAGE_STACK + 1, x + RIBBON_W, top + PAGE_STACK + 2, 0x28000000);
        hotspot(x, top, RIBBON_W, BOOK_H + RIBBON_SHOWN - top, () -> visit(marked),
                () -> List.of(Component.translatable(book.bookmark() == null ? "alchemia.guide.bookmark.prepared" : "alchemia.guide.bookmark.open")));
    }

    private @Nullable String markedLocation() {
        String bookmark = book.bookmark();
        if (bookmark != null && valid(bookmark)) return bookmark;
        if (book.selected() != null && book.find(book.selected()) != null) return RECIPE_AT + book.selected();
        return null;
    }

    private String location() {
        String openRecipe = context.openRecipeId();
        return switch (context.chapter()) {
            case CONTENTS -> primer.spread() >= 0 ? PRIMER_AT + primer.spread() : "contents";
            case RECIPES -> openRecipe != null && context.currentRecipe() != null ? RECIPE_AT + openRecipe : RECIPES_AT + index.spread();
            case EFFECTS -> EFFECTS_AT + effects.page();
            case INGREDIENTS -> ingredients.reagentPage() ? "reagents:" + ingredients.reagentSpread() : "ingredients";
            case OPUS -> "opus";
        };
    }

    private boolean valid(String place) {
        if (place.startsWith(RECIPE_AT)) return book.find(place.substring(RECIPE_AT.length())) != null;
        return true;
    }

    private double position(String place) {
        if (place.startsWith(PRIMER_AT)) return 0.1 + number(place, PRIMER_AT) * 1.0E-4;
        if (place.startsWith(RECIPES_AT)) return 1.0 + number(place, RECIPES_AT) * 1.0E-4;
        if (place.startsWith(RECIPE_AT)) {
            BrewRecipe recipe = book.find(place.substring(RECIPE_AT.length()));
            return 2.0 + (recipe == null ? 0 : book.recipes().indexOf(recipe));
        }
        if (place.startsWith(EFFECTS_AT)) return 1.0E9 + number(place, EFFECTS_AT) * 1.0E-4;
        return switch (place) {
            case "ingredients" -> 2.0E9;
            case "opus" -> 3.0E9;
            default -> place.startsWith("reagents:") ? 2.0E9 + (number(place, "reagents:") + 1) * 0.25
                    : place.startsWith("ingredients:") ? 2.0E9
                    : place.equals("reagents") ? 2.0E9 + 0.25 : 0.0;
        };
    }

    private static int number(String place, String prefix) {
        try {
            return Integer.parseInt(place.substring(prefix.length()));
        } catch (NumberFormatException error) {
            return 0;
        }
    }

    private void visit(String place) {
        if (place.startsWith(RECIPE_AT)) {
            BrewRecipe recipe = book.find(place.substring(RECIPE_AT.length()));
            if (recipe != null) openRecipe(recipe);
            return;
        }
        if (place.startsWith(PRIMER_AT)) {
            goTo(GuideChapter.CONTENTS);
            openPrimer(Math.min(number(place, PRIMER_AT), primer.spreadCount() - 1));
            return;
        }
        if (place.startsWith(RECIPES_AT)) {
            goTo(GuideChapter.RECIPES);
            index.showSpread(Mth.clamp(number(place, RECIPES_AT), 0, index.spreads() - 1));
            return;
        }
        if (place.startsWith(EFFECTS_AT)) {
            goTo(GuideChapter.EFFECTS);
            effects.showPage(Math.max(0, number(place, EFFECTS_AT)));
            return;
        }
        if (place.equals("ingredients") || place.startsWith("ingredients:")) {
            goTo(GuideChapter.INGREDIENTS);
            ingredients.showIngredients();
            return;
        }
        if (place.equals("reagents") || place.startsWith("reagents:")) {
            goTo(GuideChapter.INGREDIENTS);
            ingredients.showReagents(Mth.clamp(place.startsWith("reagents:") ? number(place, "reagents:") : 0, 0, 1));
            return;
        }
        for (GuideChapter entry : GuideChapter.values()) {
            if (entry.name().equalsIgnoreCase(place)) goTo(entry);
        }
    }

    private void setBookmark(@Nullable String place) {
        book.mark(place);
        if (place == null) {
            book.stack().remove(DataComponentRegistry.GUIDE_BOOKMARK.get());
        } else {
            book.stack().set(DataComponentRegistry.GUIDE_BOOKMARK.get(), place);
        }
        send(PacketGuideAction.Action.BOOKMARK, place == null ? "" : place);
        if (minecraft() != null) {
            minecraft().getSoundManager().play(SimpleSoundInstance.forUI(place == null ? SoundEvents.BOOK_PAGE_TURN : SoundEvents.BOOK_PUT, 1.0F));
        }
    }

    private void hangRibbon(GuiGraphicsExtractor graphics, int x, int from) {
        int tailY = BOOK_H + RIBBON_SHOWN - RIBBON_TAIL_H;
        for (int y = from; y < tailY; y += RIBBON_BODY_H) {
            bookPart(graphics, x, y, RIBBON_U, 0, RIBBON_W, Math.min(RIBBON_BODY_H, tailY - y));
        }
        bookPart(graphics, x, tailY, RIBBON_U, RIBBON_TAIL_V, RIBBON_W, RIBBON_TAIL_H);
        graphics.fill(x, from, x + RIBBON_W, from + 1, 0x66000000);
        graphics.fill(x, from + 1, x + RIBBON_W, from + 2, 0x2A000000);
    }

    private void extractTabs(GuiGraphicsExtractor graphics) {
        for (GuideChapter tab : GuideChapter.values()) {
            if (!tab.shown()) continue;
            int y = TAB_TOP + tab.ordinal() * TAB_SPACING;
            boolean active = tab == context.chapter();
            int drop = active ? 0 : TAB_DROP;
            boolean over = hovered(PAGE_RIGHT_EDGE + 1, y, TAB_W - TAB_REACH, TAB_H + drop);
            int reach = active ? TAB_REACH : over ? TAB_REACH / 2 : 0;
            int x = PAGE_RIGHT_EDGE + 1 - TAB_REACH + reach;
            int root = PAGE_RIGHT_EDGE + 1 + PAGE_STACK;

            int v = TAB_ROW_V + tab.ordinal() * TAB_ROW_STEP;
            if (active) {
                widget(graphics, x, y, TAB_STRAIGHT_U, v, TAB_W, TAB_H);
            } else {
                widget(graphics, x, y, TAB_FOLDED_U, v, TAB_W, TAB_H + TAB_DROP);
            }
            int patchX = x + TAB_PATCH_X;
            int patchY = y + TAB_PATCH_Y + drop;
            if (tab == GuideChapter.RECIPES) {
                graphics.item(GuideItems.healingPotion(), patchX, patchY);
            } else if (tab == GuideChapter.OPUS) {
                graphics.item(GuideItems.tabletIcon(), patchX + 1, patchY);
            } else if (tab == GuideChapter.EFFECTS) {
                glowing(graphics, patchX + 8.5F, patchY + 8.5F, ICON);
            } else if (tab.icon == GuideIcon.SEAL) {
                icon(graphics, tab.icon, patchX + 2, patchY + 3, false);
            } else {
                icon(graphics, tab.icon, patchX + 3, patchY + 3, false);
            }
            bookPart(graphics, x, y, x, y, root - x, TAB_H + drop);
            graphics.fill(root, y + 1, root + 1, y + TAB_H + drop - 1, 0x55000000);
            hotspot(PAGE_RIGHT_EDGE + 1, y, TAB_W - TAB_REACH, TAB_H + drop, () -> {
                if (tab != context.chapter()) {
                    ingredients.showIngredients();
                    opus.showStages();
                }
                goTo(tab);
            }, () -> List.of(tab.title()));
        }
    }
}
