package muddykat.alchemia.client.gui.guide;

import muddykat.alchemia.common.potion.BrewRecipe;
import muddykat.alchemia.common.potion.BrewRecipeBook;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

import static muddykat.alchemia.client.gui.guide.GuideLayout.*;

public final class RecipeIndex extends GuidePage {

    private static final int PREPARED_U = 54;
    private static final int MARKER_COLUMN = SEAL + 3;
    private static final int INDEX_ROW_H = 22;

    private int indexSpread;

    public RecipeIndex(GuideContext context) {
        super(context);
    }

    public int spread() {
        return indexSpread;
    }

    public void showSpread(int spread) {
        indexSpread = spread;
    }

    void recipeRow(GuiGraphics graphics, BrewRecipe recipe, int x, int y, int width, int rowH) {
        boolean over = hovered(x - 2, y - 1, width + 4, rowH);
        boolean prepared = recipe.id().equals(book.selected());
        if (over) wash(graphics, x - 2, y - 1, width + 4, rowH, false);

        graphics.renderItem(GuideItems.potionFor(recipe), x, y + 1);
        int textW = width - 20 - MARKER_COLUMN;
        text(graphics, Component.literal(clip(recipe.displayName(), textW)), x + 20, y + 1, over ? INK_LINK : INK_HEAD);
        Component detail = Component.translatable("alchemia.guide.recipe.entry", recipe.brewBase().displayName(), book.effectSummary(recipe));
        text(graphics, Component.literal(clip(detail, textW)), x + 20, y + 11, INK_FAINT);
        int sealX = x + width - SEAL;
        int sealY = y + (rowH - 2 - SEAL) / 2 - 1;
        boolean overSeal = hovered(sealX + 1, sealY + 1, SEAL - 2, SEAL - 2);
        if (prepared) {
            widget(graphics, sealX, sealY, PREPARED_U, PREPARED_V, SEAL, SEAL);
        } else if (canPrepare()) {
            widget(graphics, sealX, sealY, EMPTY_SEAL_U + (overSeal ? SEAL + 1 : 0), PREPARED_V, SEAL, SEAL);
        }

        hotspot(x - 2, y - 1, width + 4, rowH, () -> openRecipe(recipe), () -> {
            List<Component> lines = new ArrayList<>();
            lines.add(recipe.displayName());
            lines.add(Component.translatable("alchemia.guide.recipe.base", recipe.brewBase().displayName()));
            for (BrewRecipe.BrewEffect effect : recipe.effects()) lines.add(book.effectLabel(effect));
            if (prepared) lines.add(Component.translatable("alchemia.guide.prepared"));
            return lines;
        });
        if (canPrepare()) {
            hotspot(sealX + 1, sealY + 1, SEAL - 2, SEAL - 2, () -> prepare(recipe), () -> List.of(Component.translatable(
                    prepared ? "alchemia.guide.prepared.clear" : "alchemia.guide.prepare.quick")));
        }
    }

    private void blankRow(GuiGraphics graphics, int x, int y, int width) {
        icon(graphics, GuideIcon.PAGE, x + 2, y + 3, true);
        text(graphics, Component.translatable("alchemia.guide.blank_page"), x + 20, y + 5, INK_FAINT);
        graphics.fill(x + 20, y + 15, x + width - 4, y + 16, 0x18604A34);
    }

    public int spreads() {
        int entries = Math.max(BrewRecipeBook.pages(book.stack()), book.recipes().size());
        return Math.max(1, Mth.ceil(entries / (float) (INDEX_ROWS * 2)));
    }

    public void draw(GuiGraphics graphics) {
        int spreads = spreads();
        indexSpread = Mth.clamp(indexSpread, 0, spreads - 1);
        int pages = Math.max(BrewRecipeBook.pages(book.stack()), book.recipes().size());

        chapterHeading(graphics, Component.translatable("alchemia.guide.recipes.title"), LEFT_X, TOP + 2);
        heading(graphics, Component.translatable("alchemia.guide.recipes.used", book.recipes().size(), pages), RIGHT_X, TOP + 5);

        int first = indexSpread * INDEX_ROWS * 2;
        for (int slot = 0; slot < INDEX_ROWS * 2; slot++) {
            int entry = first + slot;
            if (entry >= pages) break;
            int pageX = slot < INDEX_ROWS ? LEFT_X : RIGHT_X;
            int y = TOP + 26 + (slot % INDEX_ROWS) * INDEX_ROW_H;
            if (entry < book.recipes().size()) {
                recipeRow(graphics, book.recipes().get(entry), pageX, y, PAGE_W, INDEX_ROW_H);
            } else {
                blankRow(graphics, pageX, y, PAGE_W);
            }
        }

        if (book.recipes().isEmpty() && pages <= 6) {
            wrappedCentred(graphics, Component.translatable("alchemia.guide.recipes.none"), LEFT_X, TOP + 30 + INDEX_ROWS * INDEX_ROW_H - 30, INK_FAINT);
        }

        Component folio = Component.translatable("alchemia.guide.page", indexSpread + 1, spreads);
        centred(graphics, folio, LEFT_X, BOTTOM - 7, INK_FAINT);
        centred(graphics, Component.translatable("alchemia.guide.bind_more"), RIGHT_X, BOTTOM - 7, INK_FAINT);

        if (indexSpread > 0) corner(graphics, false, () -> flip(-1), Component.translatable("alchemia.guide.previous"));
        if (indexSpread < spreads - 1) corner(graphics, true, () -> flip(1), Component.translatable("alchemia.guide.next"));
    }

    public void flip(int delta) {
        int next = Mth.clamp(indexSpread + delta, 0, spreads() - 1);
        if (next == indexSpread) return;
        indexSpread = next;
        turnPage();
    }

    public void flipRecipe(int delta) {
        BrewRecipe current = context.currentRecipe();
        if (current == null) return;
        int index = book.recipes().indexOf(current);
        int next = index + delta;
        if (next < 0 || next >= book.recipes().size()) return;
        openRecipe(book.recipes().get(next));
        indexSpread = next / (INDEX_ROWS * 2);
    }
}
