package muddykat.alchemia.client.gui.guide;

import muddykat.alchemia.common.config.Configuration;
import muddykat.alchemia.common.guide.GreatWork;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.potion.BrewRecipe;
import muddykat.alchemia.common.potion.BrewRecipeBook;
import muddykat.alchemia.common.potion.IngredientDiscovery;
import muddykat.alchemia.common.potion.MapReveals;
import muddykat.alchemia.common.potion.RecipeDiscovery;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static muddykat.alchemia.client.gui.guide.GuideLayout.*;

public final class ContentsPage extends GuidePage {

    private static final int TITLE_ART_U = 54;
    private static final int TITLE_ART_V = 214;
    private static final int TITLE_ART_W = 66;
    private static final int TITLE_ART_H = 12;
    private static final int RECENT_ROW_H = 20;
    private static final int CHAPTER_ROW_H = 21;
    private static final int RECENT = 3;
    private static final int CHAPTER_MARGIN = 1;
    private static final int CHAPTER_CELL_SHIFT = 4;

    private final RecipeIndex recipeIndex;

    public ContentsPage(GuideContext context, RecipeIndex index) {
        super(context);
        this.recipeIndex = index;
    }

    public void draw(GuiGraphics graphics) {
        topCorner(graphics, true, () -> openPrimer(0), Component.translatable("alchemia.guide.primer.open"));
        graphics.pose().pushPose();
        graphics.pose().translate(LEFT_X + (PAGE_W - TITLE_ART_W * 2) / 2.0F, TOP, 0.0F);
        graphics.pose().scale(2.0F, 2.0F, 1.0F);
        widget(graphics, 0, 0, TITLE_ART_U, TITLE_ART_V, TITLE_ART_W, TITLE_ART_H);
        graphics.pose().popPose();

        int y = wrappedCentred(graphics, Component.translatable("alchemia.guide.cover_subtitle"), LEFT_X, TOP + 27, INK_FAINT);
        divider(graphics, LEFT_X, y + 1, PAGE_W - 20);
        y += DIVIDER_H + 7;

        int used = book.recipes().size();
        int pages = BrewRecipeBook.pages(book.stack());
        y = stat(graphics, y, (gx, gy) -> graphics.renderItem(new ItemStack(Items.WRITABLE_BOOK), gx, gy), "alchemia.guide.stat.pages",
                Component.translatable("alchemia.guide.stat.of", used, pages));
        y = stat(graphics, y, (gx, gy) -> glowing(graphics, gx + 8.5F, gy + 8.5F, GLOWING_ART), "alchemia.guide.stat.effects",
                Component.translatable("alchemia.guide.stat.of", book.discovered().size(), RecipeDiscovery.total()));
        y = stat(graphics, y, (gx, gy) -> graphics.renderItem(new ItemStack(Items.BRUSH), gx, gy), "alchemia.guide.stat.secrets",
                Component.literal(String.valueOf(book.notes().size())));
        y = stat(graphics, y, (gx, gy) -> graphics.renderItem(GuideItems.ingredientStack(Ingredients.Druids_Rosemary), gx, gy), "alchemia.guide.stat.ingredients",
                Component.translatable("alchemia.guide.stat.of", IngredientDiscovery.found(book.stack()), IngredientDiscovery.totalAll()));

        y = stat(graphics, y, (gx, gy) -> graphics.renderItem(GuideItems.tabletIcon(), gx, gy), "alchemia.guide.stat.opus",
                Component.translatable("alchemia.guide.stat.of", book.greatWork().size(), GreatWork.total()));
        Component explored = Configuration.fogOfWar()
                ? Component.translatable("alchemia.guide.stat.percent", Math.round(MapReveals.explored(book.stack()) * 100.0))
                : Component.translatable("alchemia.guide.stat.none");
        y = stat(graphics, y, (gx, gy) -> graphics.renderItem(new ItemStack(Items.MAP), gx, gy), "alchemia.guide.stat.map", explored);

        BrewRecipe prepared = book.selected() == null ? null : book.find(book.selected());
        Component preparedName = prepared == null ? Component.translatable("alchemia.guide.stat.none") : prepared.displayName();
        int preparedY = y;
        y = stat(graphics, y, (gx, gy) -> icon(graphics, GuideIcon.SEAL, gx + 2, gy + 2, false), "alchemia.guide.stat.prepared", Component.literal(clip(preparedName, 70)));
        if (prepared != null) hotspot(LEFT_X, preparedY - 2, PAGE_W, 14, () -> openRecipe(prepared));

        int ry = scrolledHeading(graphics, Component.translatable("alchemia.guide.chapters"), RIGHT_X, TOP + 1, PAGE_W);
        GuideChapter[] listed = java.util.stream.Stream.of(GuideChapter.RECIPES, GuideChapter.EFFECTS, GuideChapter.INGREDIENTS, GuideChapter.OPUS)
                .filter(GuideChapter::shown).toArray(GuideChapter[]::new);
        int panel = CELL + CHAPTER_MARGIN * 2;
        int panelX = RIGHT_X + CHAPTER_CELL_SHIFT - CHAPTER_MARGIN;
        int cellX = panelX + CHAPTER_MARGIN;
        int textX = panelX + panel + FRAME_PAD_W + 2;
        for (int row = 0; row < listed.length; row++) {
            GuideChapter entry = listed[row];
            int rowY = ry + 1 + row * CHAPTER_ROW_H;
            int cellY = rowY + CHAPTER_MARGIN;
            boolean over = hovered(RIGHT_X - 2, rowY - 1, PAGE_W + 4, CHAPTER_ROW_H);
            if (over) wash(graphics, RIGHT_X - 2, rowY - 1, PAGE_W + 4, CHAPTER_ROW_H, false);
            graphics.fill(panelX, rowY, panelX + panel, rowY + panel, LEATHER_LIGHT);
            cell(graphics, cellX, cellY, over ? 1 : 0);
            if (entry == GuideChapter.RECIPES) {
                graphics.renderItem(GuideItems.healingPotion(), cellX, cellY + 1);
            } else if (entry == GuideChapter.OPUS) {
                graphics.renderItem(GuideItems.tabletIcon(), cellX + 1, cellY + 1);
            } else if (entry == GuideChapter.EFFECTS) {
                glowing(graphics, cellX + 9.0F, cellY + 9.0F, ICON);
            } else {
                icon(graphics, entry.icon, cellX + 3, cellY + 3, false);
            }
            text(graphics, entry.title(), textX, rowY + 1, over ? INK_LINK : INK_HEAD);
            marquee(graphics, entry.blurb(), textX, rowY + 11, RIGHT_X + PAGE_W - textX, INK_FAINT, over ? entry : null);
            hotspot(RIGHT_X - 2, rowY - 1, PAGE_W + 4, CHAPTER_ROW_H, () -> goTo(entry));
        }
        ry = ry + 1 + listed.length * CHAPTER_ROW_H + 3;

        ry = scrolledHeading(graphics, Component.translatable("alchemia.guide.recent"), RIGHT_X, ry + 1, PAGE_W);
        if (book.recipes().isEmpty()) {
            wrappedCentred(graphics, Component.translatable("alchemia.guide.recipes.none"), RIGHT_X, ry, INK_FAINT);
            return;
        }
        for (int index = book.recipes().size() - 1; index >= Math.max(0, book.recipes().size() - RECENT); index--) {
            BrewRecipe recipe = book.recipes().get(index);
            recipeIndex.recipeRow(graphics, recipe, RIGHT_X, ry, PAGE_W, RECENT_ROW_H);
            ry += RECENT_ROW_H;
        }
    }

    private interface Glyph {
        void draw(int x, int y);
    }

    private int stat(GuiGraphics graphics, int y, Glyph glyph, String key, Component value) {
        glyph.draw(LEFT_X, y - 4);
        text(graphics, Component.translatable(key), LEFT_X + 20, y, INK);
        text(graphics, value, LEFT_X + PAGE_W - 2 - font().width(value), y, INK_HEAD);
        graphics.fill(LEFT_X + 20, y + LINE, LEFT_X + PAGE_W - 2, y + LINE + 1, 0x22604A34);
        return y + 17;
    }
}
