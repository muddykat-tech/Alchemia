package muddykat.alchemia.client.gui.guide;

import muddykat.alchemia.client.gui.GuideInk;
import muddykat.alchemia.client.gui.PathPanel;
import muddykat.alchemia.common.items.ItemBark;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.helper.IngredientPath;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.network.packets.PacketGuideAction;
import muddykat.alchemia.common.potion.BrewRecipe;
import muddykat.alchemia.common.potion.BrewRecipeBook;
import muddykat.alchemia.common.potion.Imbuements;
import muddykat.alchemia.common.potion.PotionMap;
import muddykat.alchemia.common.potion.VanillaIngredients;
import muddykat.alchemia.common.world.IngredientRange;
import muddykat.alchemia.data.worldgen.AlchemiaBiomeModifiers;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import static muddykat.alchemia.client.gui.guide.GuideLayout.*;

public final class IngredientPages extends GuidePage {

    private static final int INGREDIENT_NOTE_LINES = 4;

    private @Nullable Ingredients selectedIngredient;
    private boolean reagentPage;
    private int reagentSpread;
    private @Nullable Item selectedReagent;
    private final GrindSliders grinds;

    public IngredientPages(GuideContext context, GrindSliders grinds) {
        super(context);
        this.grinds = grinds;
    }

    public boolean reagentPage() {
        return reagentPage;
    }

    public int reagentSpread() {
        return reagentSpread;
    }

    public void showIngredients() {
        reagentPage = false;
        reagentSpread = 0;
    }

    public void showReagents(int spread) {
        reagentSpread = spread;
        reagentPage = true;
    }

    public void select(Ingredients ingredient) {
        selectedIngredient = ingredient;
        selectedReagent = null;
    }

    public void select(Item reagent) {
        selectedReagent = reagent;
        selectedIngredient = null;
    }

    public void draw(GuiGraphics graphics) {
        if (reagentPage) {
            extractReagents(graphics);
            returnCorner(graphics);
            return;
        }
        int y = chapterHeading(graphics, Component.translatable("alchemia.guide.chapter.ingredients"), LEFT_X, TOP + 2);
        int gridX = LEFT_X + (PAGE_W - GRID_COLUMNS * CELL) / 2;
        y = ingredientGroup(graphics, IngredientType.Flower, "alchemia.guide.ingredients.flower", gridX, y);
        y = ingredientGroup(graphics, IngredientType.Herb, "alchemia.guide.ingredients.herb", gridX, y);
        y = ingredientGroup(graphics, IngredientType.Mushroom, "alchemia.guide.ingredients.mushroom", gridX, y);
        ingredientGroup(graphics, IngredientType.Root, "alchemia.guide.ingredients.root", gridX, y);

        corner(graphics, true, () -> {
            reagentPage = true;
            reagentSpread = 0;
            resetScroll();
            turnPage();
        }, Component.translatable("alchemia.guide.reagents.title"));
        scrollPanel(graphics, TOP, BOTTOM - 20, () -> selectedReagent != null ? reagentDetail(graphics) : ingredientDetail(graphics));
        returnCorner(graphics);
    }

    private void returnCorner(GuiGraphics graphics) {
        if (!context.returning()) return;
        topCorner(graphics, false, context::backToRecipe, Component.translatable("alchemia.guide.back_recipe"));
    }

    private void ingredientItem(GuiGraphics graphics, ItemStack item, int x, int y, boolean discovered) {
        if (discovered) {
            graphics.renderItem(item, x, y);
        } else if (item.is(Items.WITHER_SKELETON_SKULL)) {
            icon(graphics, GuideIcon.UNKNOWN, x + 2, y + 2, true);
        } else {
            GuideInk.detailedItem(graphics, item, x, y, GuideInk.PLAIN);
        }
    }

    private int undiscoveredDetail(GuiGraphics graphics, int y, int width) {
        return wrapped(graphics, Component.translatable("alchemia.guide.ingredients.undiscovered_detail"), RIGHT_X, y + 30, width, INK_FAINT);
    }

    private int ingredientGroup(GuiGraphics graphics, IngredientType type, String title, int gridX, int y) {
        List<Ingredients> items = GuideItems.ingredientsOf(type);
        centred(graphics, Component.translatable(title, book.knownIngredients(items), items.size()), LEFT_X, y, INK_HEAD);
        return ingredientGrid(graphics, items, gridX, y + 10) + 1;
    }

    private int ingredientGrid(GuiGraphics graphics, List<Ingredients> items, int gridX, int gridY) {
        for (int slot = 0; slot < items.size(); slot++) {
            int x = gridX + (slot % GRID_COLUMNS) * CELL;
            int cy = gridY + (slot / GRID_COLUMNS) * CELL;
            Ingredients ingredient = items.get(slot);
            boolean discovered = book.knownIngredients().contains(ingredient);
            boolean over = hovered(x, cy, CELL, CELL);
            cell(graphics, x, cy, ingredient == selectedIngredient ? 2 : over ? 1 : discovered ? 0 : 3);
            ItemStack stackOf = GuideItems.ingredientStack(ingredient);
            ingredientItem(graphics, stackOf, x + 1, cy + 1, discovered);
            hotspot(x, cy, CELL, CELL, () -> {
                selectedIngredient = ingredient;
                selectedReagent = null;
                resetScroll();
            }, () -> List.of(discovered ? stackOf.getHoverName() : Component.translatable("alchemia.guide.ingredients.undiscovered")));
        }
        return gridY + Mth.ceil(items.size() / (float) GRID_COLUMNS) * CELL;
    }

    private void extractReagents(GuiGraphics graphics) {
        int y = chapterHeading(graphics, Component.translatable("alchemia.guide.reagents.title"), LEFT_X, TOP + 2);
        int gridX = LEFT_X + (PAGE_W - GRID_COLUMNS * CELL) / 2;
        if (reagentSpread == 0) {
            List<Item> reagents = GuideItems.generalReagents();
            centred(graphics, Component.translatable("alchemia.guide.reagents.general", known(reagents), reagents.size()), LEFT_X, y, INK_HEAD);
            int gridEnd = reagentGrid(graphics, reagents, gridX, y + 12);
            List<Item> monsters = GuideItems.lureReagents();
            centred(graphics, Component.translatable("alchemia.guide.reagents.monster_drops", known(monsters), monsters.size()), LEFT_X, gridEnd + 6, INK_HEAD);
            gridEnd = reagentGrid(graphics, monsters, gridX, gridEnd + 18);
            List<Ingredients> minerals = GuideItems.ingredientsOf(IngredientType.Mineral);
            centred(graphics, Component.translatable("alchemia.guide.ingredients.mineral", book.knownIngredients(minerals), minerals.size()), LEFT_X, gridEnd + 6, INK_HEAD);
            ingredientGrid(graphics, minerals, gridX, gridEnd + 18);
        } else {
            List<Item> modifiers = VanillaIngredients.MODIFIERS;
            centred(graphics, Component.translatable("alchemia.guide.reagents.modifiers", known(modifiers), modifiers.size()), LEFT_X, y, INK_HEAD);
            int gridEnd = reagentGrid(graphics, modifiers, gridX, y + 12);
            List<Item> imbuements = Imbuements.items();
            centred(graphics, Component.translatable("alchemia.guide.reagents.imbuements", known(imbuements), imbuements.size()), LEFT_X, gridEnd + 6, INK_HEAD);
            reagentGrid(graphics, imbuements, gridX, gridEnd + 18);
        }

        if (reagentSpread == 0) corner(graphics, false, () -> {
            reagentPage = false;
            reagentSpread = 0;
            resetScroll();
            turnPage();
        }, Component.translatable("alchemia.guide.chapter.ingredients"));
        else corner(graphics, false, () -> {
            reagentSpread = 0;
            resetScroll();
            turnPage();
        }, Component.translatable("alchemia.guide.previous"));
        if (reagentSpread == 0) corner(graphics, true, () -> {
            reagentSpread = 1;
            resetScroll();
            turnPage();
        }, Component.translatable("alchemia.guide.next"));
        scrollPanel(graphics, TOP, BOTTOM - 20, () -> selectedReagent != null ? reagentDetail(graphics) : ingredientDetail(graphics));
    }

    private int imbuementDetail(GuiGraphics graphics, Item item, int y, int width) {
        Imbuements.Attuner attuner = Imbuements.of(new ItemStack(item));
        Component region = item instanceof ItemBark ? Component.translatable("alchemia.guide.reagent.attunes.bark")
                : attuner == null ? Component.empty() : attuner.region();
        y = wrapped(graphics, Component.translatable("alchemia.guide.reagent.imbuement"), RIGHT_X, y, width, INK_HEAD) + 2;
        y = wrapped(graphics, Component.translatable("alchemia.guide.reagent.attunes", region), RIGHT_X, y, width, INK_HEAD) + 2;
        boolean altitude = attuner != null && attuner.placesAltitude();
        y = wrapped(graphics, Component.translatable(altitude ? "alchemia.guide.reagent.imbuement.altitude" : "alchemia.guide.reagent.imbuement.land"),
                RIGHT_X, y, width, INK_SOFT) + 2;
        String gather = "alchemia.guide.reagent." + BuiltInRegistries.ITEM.getKey(item).getPath() + ".gather";
        Component gathered = Component.translatableWithFallback(gather, "");
        if (!gathered.getString().isEmpty()) y = wrapped(graphics, gathered, RIGHT_X, y, width, INK_SOFT) + 2;
        return y + 2;
    }

    private int known(List<Item> items) {
        int count = 0;
        for (Item item : items) {
            if (book.knownReagents().contains(item)) count++;
        }
        return count;
    }

    private int reagentGrid(GuiGraphics graphics, List<Item> items, int gridX, int gridY) {
        for (int slot = 0; slot < items.size(); slot++) {
            int x = gridX + (slot % GRID_COLUMNS) * CELL;
            int cy = gridY + (slot / GRID_COLUMNS) * CELL;
            Item reagent = items.get(slot);
            boolean discovered = book.knownReagents().contains(reagent);
            boolean over = hovered(x, cy, CELL, CELL);
            cell(graphics, x, cy, reagent == selectedReagent ? 2 : over ? 1 : discovered ? 0 : 3);
            ItemStack stackOf = new ItemStack(reagent);
            ingredientItem(graphics, stackOf, x + 1, cy + 1, discovered);
            hotspot(x, cy, CELL, CELL, () -> {
                selectedReagent = reagent;
                selectedIngredient = null;
                resetScroll();
            }, () -> List.of(discovered ? stackOf.getHoverName() : Component.translatable("alchemia.guide.ingredients.undiscovered")));
        }
        return gridY + Mth.ceil(items.size() / (float) GRID_COLUMNS) * CELL;
    }

    private int reagentDetail(GuiGraphics graphics) {
        int width = PAGE_W - TRACK_W - 4;
        int y = TOP - (int) scroll();
        if (selectedReagent == null) {
            return wrapped(graphics, Component.translatable("alchemia.guide.reagents.select"), RIGHT_X, y + 30, width, INK_FAINT);
        }
        if (!book.knownReagents().contains(selectedReagent)) return undiscoveredDetail(graphics, y, width);

        ItemStack item = new ItemStack(selectedReagent);
        boolean attuning = Imbuements.isAttuner(item);
        y = banner(graphics, item.getHoverName().getString(), y, width, (x, iy) -> graphics.renderItem(item, x + 1, iy + 1));

        VanillaIngredients.Drift drift = VanillaIngredients.driftOf(item);
        if (drift != null && drift.target() != null) {
            Component target = drift.target().displayName();
            boolean over = hovered(RIGHT_X - 2, y - 1, width + 4, LINE + 1) && inView();
            y = wrapped(graphics, Component.translatable(drift.lure() ? "alchemia.guide.reagent.lure" : "alchemia.guide.reagent.drift", target),
                    RIGHT_X, y, width, over ? INK_LINK : INK_HEAD) + 2;
            clippedHotspot(RIGHT_X - 2, y - LINE - 3, width + 4, LINE + 1, () -> followEffect(drift.target()), () -> List.of(target));
            y = wrapped(graphics, Component.translatable(drift.lure() ? "alchemia.guide.reagent.lure.detail" : "alchemia.guide.reagent.drift.detail"),
                    RIGHT_X, y, width, INK_SOFT) + 2;
            y = wrapped(graphics, Component.translatable(drift.lure() ? "alchemia.guide.reagent.lure.grind" : "alchemia.guide.reagent.drift.grind",
                    String.format(java.util.Locale.ROOT, "%.1f", VanillaIngredients.grindScale(Ingredients.MAX_CRUSH_UNITS))),
                    RIGHT_X, y, width, INK_SOFT) + 4;

            PathPanel panel = drift.lure() || !PotionMap.isReady() ? null : GuidePaths.driftPanel(drift);
            if (panel != null && panel.drawable()) {
                y = subtitle(graphics, Component.translatable("alchemia.tooltip.label.path"), y + 2, width) + 6;
                y = grinds.pathScroll(graphics, panel, GuidePaths.driftGrindTable(drift), "reagent:" + GuideItems.reagentKey(selectedReagent), item, y, width) + 6;
            }
        } else if (attuning) {
            y = imbuementDetail(graphics, selectedReagent, y, width);
        } else {
            String use = selectedReagent == Items.REDSTONE ? "alchemia.tooltip.use.redstone"
                    : selectedReagent == Items.GUNPOWDER ? "alchemia.tooltip.use.gunpowder"
                    : selectedReagent == Items.GLOWSTONE_DUST ? "alchemia.tooltip.use.glowstone" : "alchemia.tooltip.use.dragon_breath";
            y = wrapped(graphics, Component.translatable("alchemia.guide.reagent.modifier"), RIGHT_X, y, width, INK_HEAD) + 2;
            y = wrapped(graphics, Component.translatable(use), RIGHT_X, y, width, INK_SOFT) + 4;
        }

        y = scrolledHeading(graphics, Component.translatable("alchemia.guide.your_notes"), y, width);
        y = ingredientNotes(graphics, GuideItems.reagentKey(selectedReagent), y, width) + 6;

        List<BrewRecipe> using = new ArrayList<>();
        for (BrewRecipe recipe : book.recipes()) {
            boolean uses = false;
            for (ItemStack used : recipe.resolve()) uses |= used.getItem() == selectedReagent && !attuning;
            if (selectedReagent == Items.REDSTONE) uses |= recipe.steps().contains(String.valueOf(BrewRecipe.STEP_REDSTONE));
            if (selectedReagent == Items.GUNPOWDER) uses |= GuideItems.splash(recipe);
            if (selectedReagent == Items.GLOWSTONE_DUST) uses |= recipe.steps().contains(String.valueOf(BrewRecipe.STEP_GLOW));
            if (attuning) uses |= Imbuements.of(recipe).stream().anyMatch(used -> used.item() == selectedReagent);
            if (uses) using.add(recipe);
        }
        y = scrolledHeading(graphics, Component.translatable("alchemia.guide.ingredient.recipes"), y, width);
        if (using.isEmpty()) return wrapped(graphics, Component.translatable("alchemia.guide.effect.no_recipes"), RIGHT_X, y, width, INK_FAINT);
        return linkedRecipes(graphics, using, y, width);
    }

    private int ingredientDetail(GuiGraphics graphics) {
        int width = PAGE_W - TRACK_W - 4;
        int y = TOP - (int) scroll();
        if (selectedIngredient == null) {
            return wrapped(graphics, Component.translatable("alchemia.guide.ingredients.select"), RIGHT_X, y + 30, width, INK_FAINT);
        }
        Component habitat = IngredientRange.describe(selectedIngredient, AlchemiaBiomeModifiers.biomeFor(selectedIngredient));
        if (!book.knownIngredients().contains(selectedIngredient)) {
            y = undiscoveredDetail(graphics, y, width) + 4;
            return habitat == null ? y : wrapped(graphics, habitat, RIGHT_X, y, width, INK_SOFT);
        }

        ItemStack icon = GuideItems.ingredientStack(selectedIngredient);
        y = banner(graphics, icon.getHoverName().getString(), y, width, (x, iy) -> graphics.renderItem(icon, x + 1, iy + 1));
        if (habitat != null) y = wrapped(graphics, habitat, RIGHT_X, y, width, INK_SOFT) + 4;

        PathPanel panel = GuidePaths.pathPanel(selectedIngredient);
        if (panel != null && panel.drawable()) {
            boolean teleports = IngredientPath.of(selectedIngredient).teleports();
            y = subtitle(graphics, Component.translatable(teleports ? "alchemia.tooltip.label.teleport" : "alchemia.tooltip.label.path"), y + 2, width) + 6;
            y = grinds.pathScroll(graphics, panel, GuidePaths.grindTable(selectedIngredient), "ingredient:" + selectedIngredient.name(),
                    GuideItems.ingredientStack(selectedIngredient), y, width) + 6;
        }

        y = scrolledHeading(graphics, Component.translatable("alchemia.guide.your_notes"), y, width);
        y = ingredientNotes(graphics, selectedIngredient.name(), y, width) + 6;

        List<BrewRecipe> using = new ArrayList<>();
        for (BrewRecipe recipe : book.recipes()) {
            for (ItemStack item : recipe.resolve()) {
                if (item.getItem() instanceof ItemIngredient ingredient && ingredient.getIngredient() == selectedIngredient) {
                    using.add(recipe);
                    break;
                }
            }
        }
        y = scrolledHeading(graphics, Component.translatable("alchemia.guide.ingredient.recipes"), y, width);
        if (using.isEmpty()) return wrapped(graphics, Component.translatable("alchemia.guide.effect.no_recipes"), RIGHT_X, y, width, INK_FAINT);
        return linkedRecipes(graphics, using, y, width);
    }

    private int ingredientNotes(GuiGraphics graphics, String key, int y, int width) {
        int height = INGREDIENT_NOTE_LINES * LINE + 5;
        int boxX = RIGHT_X - 2;
        int boxW = width + 4;
        graphics.fill(boxX, y, boxX + boxW, y + 1, RULE);
        graphics.fill(boxX, y + height, boxX + boxW, y + height + 1, RULE);
        graphics.fill(boxX, y, boxX + 1, y + height, RULE);
        graphics.fill(boxX + boxW - 1, y, boxX + boxW, y + height, RULE);
        int textY = y + 4;
        for (int index = 0; index < INGREDIENT_NOTE_LINES; index++) {
            int ruleY = textY + index * LINE + LINE - 1;
            graphics.fill(RIGHT_X, ruleY, RIGHT_X + width, ruleY + 1, 0x33B8A27A);
        }

        boolean active = editor.editing(NoteEditor.Kind.INGREDIENT_NOTES, key);
        String saved = BrewRecipeBook.ingredientNotes(book.stack()).getOrDefault(key, "");
        String text = active ? editor.value() : saved;
        if (active) editor.mark(boxX, y, boxW, height);
        if (text.isEmpty() && !active) editor.hint(graphics, RIGHT_X + 1, textY, width - 2, INGREDIENT_NOTE_LINES);
        editor.draw(graphics, text, RIGHT_X + 1, textY, width - 2, INGREDIENT_NOTE_LINES, active);
        if (!active) {
            clippedHotspot(boxX, y, boxW, height,
                    () -> editor.start(NoteEditor.Kind.INGREDIENT_NOTES, key, saved, PacketGuideAction.MAX_NOTE, width - 2, INGREDIENT_NOTE_LINES),
                    () -> List.of(Component.translatable("alchemia.guide.notes.tip")));
        }
        return y + height;
    }
}
