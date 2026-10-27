package muddykat.alchemia.client.gui.guide;

import muddykat.alchemia.client.gui.GuiDraw;

import muddykat.alchemia.client.gui.AlchemiaSprites;
import muddykat.alchemia.client.gui.GuideInk;
import muddykat.alchemia.common.guide.GuidePrimer;
import muddykat.alchemia.common.items.ItemAlbedo;
import muddykat.alchemia.common.items.ItemBark;
import muddykat.alchemia.common.items.ItemCitrinitas;
import muddykat.alchemia.common.items.ItemMortarPestle;
import muddykat.alchemia.common.items.ItemNigredo;
import muddykat.alchemia.common.network.packets.PacketGuideAction;
import muddykat.alchemia.common.network.packets.PacketNameBrew;
import muddykat.alchemia.common.potion.AlchemiaEffect;
import muddykat.alchemia.common.potion.BrewRecipe;
import muddykat.alchemia.common.potion.BrewRecipeBook;
import muddykat.alchemia.common.potion.Imbuements;
import muddykat.alchemia.common.potion.WorldEffects;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static muddykat.alchemia.client.gui.guide.GuideLayout.*;

public final class RecipePage extends GuidePage {

    private static final int JOINT_U = 92;
    private static final int JOINT_V = 140;
    private static final int JOINT = 7;
    private static final int RING_U = 132;
    private static final int RING_V = 140;
    private static final int RING = 22;
    private static final int INGREDIENT_ROWS_SHOWN = 3;
    private static final int DISC_U = 0;
    private static final int DISC_V = 160;
    private static final int DISC = 52;
    private static final float[] TITLE_SCALES = {0.875F, 0.75F, 0.625F, 0.5F};
    private static final int PANEL_H = 56;
    private static final int PANEL_DIVIDER = 56;
    private static final int PANEL_ROW = 10;
    private static final int RECIPE_BAR_Y = 182;
    private static final int DELETE_CONFIRM_TICKS = 60;
    private static final double STIRS_PER_SECOND = 20.0;
    private static final double FINE_STIR = 0.25;

    private record Step(Component text, ItemStack item, @Nullable GuideIcon icon, List<Component> tooltip, @Nullable Runnable action) {
        Step(Component text, ItemStack item, @Nullable GuideIcon icon, List<Component> tooltip) {
            this(text, item, icon, tooltip, null);
        }
    }

    private static final int STAMP = 28;
    private static final int STAMP_CELL = 30;
    private static final int STAMP_DROP = 4;
    private static final int BAR_RULE_GAP = 3;

    private double ingredientScroll;
    private int ingredientMax;
    private int ingredientPaneX;
    private int ingredientPaneY;
    private int ingredientPaneW;
    private int ingredientPaneH;
    private @Nullable String pendingDelete;
    private int pendingDeleteTicks;
    private final RecipeIndex recipeIndex;

    public RecipePage(GuideContext context, RecipeIndex recipeIndex) {
        super(context);
        this.recipeIndex = recipeIndex;
    }

    public void beginFrame() {
        ingredientMax = 0;
    }

    public void tick() {
        if (pendingDeleteTicks > 0 && --pendingDeleteTicks == 0) pendingDelete = null;
    }

    public void disarm() {
        pendingDelete = null;
    }

    public double ingredientScroll() {
        return ingredientScroll;
    }

    public void setIngredientScroll(double offset) {
        ingredientScroll = offset;
    }

    public boolean scrollIngredients(double scrollY) {
        if (ingredientMax <= 0 || !hovered(ingredientPaneX, ingredientPaneY, ingredientPaneW, ingredientPaneH)) return false;
        ingredientScroll = Mth.clamp(ingredientScroll - Math.signum(scrollY) * INGREDIENT_ROW, 0, ingredientMax);
        return true;
    }

    public void draw(GuiGraphics graphics, BrewRecipe recipe) {
        if (!pageMode()) {
            topCorner(graphics, false, () -> {
                commitEdit();
                context.closeRecipe();
                resetScroll();
                turnPage();
            }, Component.translatable("alchemia.guide.back"));
        }

        int y = recipeTitle(graphics, recipe, TOP);

        y = effectRow(graphics, recipe, y + 4);
        y = potionPanel(graphics, recipe, y + 4);
        y = ingredientRow(graphics, recipe, y + 5);
        notesBox(graphics, recipe, y + 5, BOTTOM - 12);

        if (pageMode()) {
            centred(graphics, Component.translatable("alchemia.page.loose"), LEFT_X, BOTTOM - 7, INK_FAINT);
        } else {
            int index = book.recipes().indexOf(recipe);
            centred(graphics, Component.translatable("alchemia.guide.folio", index + 1, BrewRecipeBook.pages(book.stack())), LEFT_X, BOTTOM - 7, INK_FAINT);
            if (index > 0) corner(graphics, false, () -> recipeIndex.flipRecipe(-1), Component.translatable("alchemia.guide.previous_recipe"));
            if (index < book.recipes().size() - 1) corner(graphics, true, () -> recipeIndex.flipRecipe(1), Component.translatable("alchemia.guide.next_recipe"));
        }

        scrollPanel(graphics, TOP, pageMode() ? RECIPE_BAR_Y - 4 : RECIPE_BAR_Y - BAR_RULE_GAP - DIVIDER_H - 2, () -> recipeMethod(graphics, recipe));
        if (!pageMode()) divider(graphics, RIGHT_X, RECIPE_BAR_Y - BAR_RULE_GAP - DIVIDER_H, PAGE_W - 20);
        extractRecipeBar(graphics, recipe, RECIPE_BAR_Y);
    }

    private void effectIcon(GuiGraphics graphics, @Nullable AlchemiaEffect potion, int x, int y, int size) {
        if (potion != null && book.discovered().contains(potion)) {
            GuiDraw.sprite(graphics, potion.sprite(), x, y, size, size);
        } else if (potion != null && WorldEffects.anyVisible(book.notes(), potion)) {
            icon(graphics, GuideIcon.EYE_CLOSED, x + (size - ICON) / 2, y + (size - ICON) / 2, false);
        } else {
            icon(graphics, GuideIcon.UNKNOWN, x + (size - ICON) / 2, y + (size - ICON) / 2, true);
        }
    }

    private int effectRow(GuiGraphics graphics, BrewRecipe recipe, int y) {
        List<BrewRecipe.BrewEffect> levels = new ArrayList<>();
        for (BrewRecipe.BrewEffect effect : recipe.effects()) {
            for (int level = 0; level < Math.max(1, effect.potency()); level++) levels.add(effect);
        }
        int slots = Math.max(EFFECT_SLOTS, levels.size());
        int available = PAGE_W - 2 * (VINE_W + 3);
        boolean vines = slots * 9 <= available;
        if (!vines) available = PAGE_W;
        int pitch = Math.max(6, Math.min(EFFECT_SLOT, available / slots));
        int size = Math.min(ICON, pitch - 1);
        int rowW = slots * pitch;
        int x0 = LEFT_X + (PAGE_W - rowW) / 2;
        if (vines) {
            widget(graphics, x0 - VINE_W - 2, y + 2, VINE_U, VINE_V, VINE_W, VINE_H);
            widget(graphics, x0 + rowW + 1, y + 2, VINE_RIGHT_U, VINE_V, VINE_W, VINE_H);
        }

        for (int slot = 0; slot < slots; slot++) {
            int x = x0 + slot * pitch;
            if (slot >= levels.size()) {
                widget(graphics, x + (pitch - DIAMOND) / 2, y + (ICON - DIAMOND) / 2 + 1, DIAMOND_U, DIAMOND_V, DIAMOND, DIAMOND);
                continue;
            }
            BrewRecipe.BrewEffect effect = levels.get(slot);
            AlchemiaEffect potion = effect.asPotion();
            effectIcon(graphics, potion, x + (pitch - size) / 2, y + (ICON - size) / 2, size);
            Runnable follow = potion == null || pageMode() ? null : () -> followEffect(potion);
            hotspot(x, y, pitch, ICON, follow, () -> book.effectTooltip(effect, potion));
        }
        return y + ICON;
    }

    private int potionPanel(GuiGraphics graphics, BrewRecipe recipe, int y) {
        int bottom = y + PANEL_H;
        int dividerX = LEFT_X + PANEL_DIVIDER;
        graphics.fill(LEFT_X - 3, y, LEFT_X + PAGE_W + 3, y + 1, RULE);
        graphics.fill(LEFT_X - 3, bottom, LEFT_X + PAGE_W + 3, bottom + 1, RULE);
        graphics.fill(dividerX, y, dividerX + 1, bottom, RULE);
        widget(graphics, dividerX - JOINT / 2, y - JOINT / 2, JOINT_U, JOINT_V, JOINT, JOINT);
        widget(graphics, dividerX - JOINT / 2, bottom - JOINT / 2, JOINT_U, JOINT_V, JOINT, JOINT);

        int discX = LEFT_X + (PANEL_DIVIDER - DISC) / 2;
        int discY = y + (PANEL_H - DISC) / 2 + 1;
        widget(graphics, discX, discY, DISC_U, DISC_V, DISC, DISC);
        if (recipe.steps().contains(String.valueOf(BrewRecipe.STEP_GLOW))) {
            int glow = (int) Math.round(190 + 55 * Math.sin(net.minecraft.Util.getMillis() / 260.0));
            GuiDraw.sprite(graphics, AlchemiaSprites.RADIANT_HALO, discX, discY, DISC, DISC, muddykat.alchemia.client.effect.ARGB.color(glow, 0xFFFFFF));
        }
        graphics.pose().pushPose();
        graphics.pose().translate(discX + (DISC - 32) / 2.0F, discY + (DISC - 32) / 2.0F, 0.0F);
        graphics.pose().scale(2.0F, 2.0F, 1.0F);
        graphics.renderItem(GuideItems.potionFor(recipe), 0, 0);
        graphics.pose().popPose();

        int listX = dividerX + 5;
        int textW = LEFT_X + PAGE_W - listX - 12;
        int rowY = y + 4;
        List<BrewRecipe.BrewEffect> effects = recipe.effects();
        int rows = (PANEL_H - 6) / PANEL_ROW;
        for (int index = 0; index < rows; index++) {
            int lineY = rowY + index * PANEL_ROW + PANEL_ROW - 1;
            graphics.fill(listX, lineY, LEFT_X + PAGE_W + 1, lineY + 1, 0x40B8A27A);
            if (index >= effects.size()) continue;

            boolean overflow = index == rows - 1 && effects.size() > rows;
            int textY = rowY + index * PANEL_ROW;
            if (overflow) {
                text(graphics, Component.translatable("alchemia.guide.more_effects", effects.size() - index), listX + 11, textY, INK_FAINT);
                continue;
            }
            BrewRecipe.BrewEffect effect = effects.get(index);
            AlchemiaEffect potion = effect.asPotion();
            boolean over = potion != null && !pageMode() && hovered(listX, textY - 1, LEFT_X + PAGE_W - listX, PANEL_ROW);
            effectIcon(graphics, potion, listX, textY - 1, 9);
            text(graphics, Component.literal(clip(book.effectLabel(effect), textW)), listX + 11, textY, over ? INK_LINK : INK);
            Runnable follow = potion == null || pageMode() ? null : () -> followEffect(potion);
            hotspot(listX, textY - 1, LEFT_X + PAGE_W - listX, PANEL_ROW, follow, () -> book.effectTooltip(effect, potion));
        }
        return bottom;
    }

    private int ingredientRow(GuiGraphics graphics, BrewRecipe recipe, int y) {
        centred(graphics, Component.translatable("alchemia.guide.ingredients"), LEFT_X, y, INK_HEAD);
        y += LINE + 2;

        Item baseItem = GuideItems.baseItem(recipe);
        widget(graphics, LEFT_X, y, RING_U, RING_V, RING, RING);
        if (baseItem != null) GuideInk.detailedItem(graphics, new ItemStack(baseItem), LEFT_X + 3, y + 3, GuideInk.PLAIN);
        hotspot(LEFT_X, y, RING, RING, null, () -> List.of(Component.translatable("alchemia.guide.recipe.base", recipe.brewBase().displayName())));

        Map<ItemStack, Integer> tally = new java.util.LinkedHashMap<>(recipe.tally());
        for (Imbuements.Attuner attuner : Imbuements.of(recipe)) tally.merge(attuner.stack(), 1, Integer::sum);
        int countW = 0;
        for (int needed : tally.values()) countW = Math.max(countW, font().width(String.valueOf(needed)));
        int pitch = 16 + countW + 3;
        int rowStart = LEFT_X + RING + 4;
        int perRow = Math.max(1, (LEFT_X + PAGE_W + 2 - 16 - countW - rowStart) / pitch + 1);
        int rows = Math.max(1, (tally.size() + perRow - 1) / perRow);
        int shown = Math.min(rows, INGREDIENT_ROWS_SHOWN);
        int paneTop = y + 2;
        int paneBottom = y + 3 + shown * INGREDIENT_ROW - 2;
        int maxScroll = (rows - shown) * INGREDIENT_ROW;
        ingredientScroll = Mth.clamp(ingredientScroll, 0, maxScroll);
        ingredientMax = maxScroll;
        ingredientPaneX = rowStart - 1;
        ingredientPaneY = paneTop;
        ingredientPaneW = LEFT_X + PAGE_W + 7 - ingredientPaneX;
        ingredientPaneH = paneBottom - paneTop;
        if (maxScroll > 0) GuiDraw.scissor(graphics, rowStart - 1, paneTop, LEFT_X + PAGE_W + 3, paneBottom);
        int x = rowStart;
        int rowY = y + 3 - (int) ingredientScroll;

        for (Map.Entry<ItemStack, Integer> entry : tally.entrySet()) {
            if (x + 16 + countW > LEFT_X + PAGE_W + 2) {
                x = rowStart;
                rowY += INGREDIENT_ROW;
            }
            ItemStack item = entry.getKey();
            int needed = entry.getValue();
            int held = countHeld(item);
            boolean enough = held >= needed;
            GuideInk.detailedItem(graphics, item, x, rowY, enough ? GuideInk.PLAIN : GuideInk.SHORT);
            text(graphics, Component.literal(String.valueOf(needed)), x + 17, rowY + 8, enough ? INK : INK_RED);

            if (rowY + 8 >= paneTop && rowY + 8 <= paneBottom) {
                hotspot(x - 1, Math.max(rowY - 1, paneTop), pitch, Math.min(rowY + 17, paneBottom) - Math.max(rowY - 1, paneTop),
                        pageMode() ? null : followFromRecipe(item), () -> ingredientTooltip(item, needed, held));
            }
            x += pitch;
        }
        if (maxScroll > 0) {
            graphics.disableScissor();
            int trackX = LEFT_X + PAGE_W + 4;
            int trackH = paneBottom - paneTop;
            int thumbH = Math.max(6, trackH * shown / rows);
            int thumbY = paneTop + (int) Math.round((trackH - thumbH) * ingredientScroll / maxScroll);
            graphics.fill(trackX, paneTop, trackX + 2, paneBottom, 0x33B8A27A);
            graphics.fill(trackX, thumbY, trackX + 2, thumbY + thumbH, RULE);
        }
        return Math.max(y + RING, y + 3 + shown * INGREDIENT_ROW - 3);
    }

    private List<Component> ingredientTooltip(ItemStack item, int needed, int held) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("alchemia.guide.ingredient.count", needed, item.getHoverName()));
        Component treatment = GuideItems.treatment(item);
        if (treatment != null) lines.add(treatment);
        lines.add(Component.translatable("alchemia.guide.ingredient.carried", held));
        return lines;
    }

    private void notesBox(GuiGraphics graphics, BrewRecipe recipe, int y, int bottom) {
        if (bottom - y < 2 * LINE + 2) return;
        graphics.fill(LEFT_X - 3, y, LEFT_X + PAGE_W + 3, y + 1, RULE);
        graphics.fill(LEFT_X - 3, bottom, LEFT_X + PAGE_W + 3, bottom + 1, RULE);
        graphics.fill(LEFT_X - 3, y, LEFT_X - 2, bottom, RULE);
        graphics.fill(LEFT_X + PAGE_W + 2, y, LEFT_X + PAGE_W + 3, bottom + 1, RULE);
        widget(graphics, LEFT_X + PAGE_W / 2 - JOINT / 2, y - JOINT / 2, JOINT_U, JOINT_V, JOINT, JOINT);

        int textX = LEFT_X + 1;
        int textY = y + 4;
        int textW = PAGE_W - 2;
        int lines = (bottom - textY) / LINE;
        for (int index = 0; index < lines; index++) {
            int ruleY = textY + index * LINE + LINE - 1;
            graphics.fill(LEFT_X, ruleY, LEFT_X + PAGE_W, ruleY + 1, 0x33B8A27A);
        }

        boolean active = editor.editing(NoteEditor.Kind.RECIPE_NOTES, recipe.id());
        String text = active ? editor.value() : recipe.notes();
        if (active) editor.mark(LEFT_X - 3, y, PAGE_W + 6, bottom - y);
        if (text.isEmpty() && !active && !pageMode()) {
            editor.hint(graphics, textX, textY, textW, lines);
        }
        editor.draw(graphics, text, textX, textY, textW, lines, active);
        if (!pageMode() && !active) {
            hotspot(LEFT_X - 3, y, PAGE_W + 6, bottom - y,
                    () -> editor.start(NoteEditor.Kind.RECIPE_NOTES, recipe.id(), recipe.notes(), PacketGuideAction.MAX_NOTE, textW, lines),
                    () -> List.of(Component.translatable("alchemia.guide.notes.tip")));
        }
    }

    private int recipeTitle(GuiGraphics graphics, BrewRecipe recipe, int y) {
        boolean editing = editor.editing(NoteEditor.Kind.TITLE, recipe.id());
        String value = editing ? editor.value() : recipe.displayName().getString();
        float scale = TITLE_SCALES[TITLE_SCALES.length - 1];
        List<int[]> lines = List.of();
        for (float option : TITLE_SCALES) {
            scale = option;
            lines = titleLines(value, scale);
            if (lines.size() <= 2) break;
        }
        float pitch = TITLE_LINE * scale;
        int height = Mth.ceil(lines.size() * pitch);

        int titleX = LEFT_X + (PAGE_W - TITLE_W) / 2;
        int rosetteY = y + Math.round(TITLE_CAP * scale / 2.0F) - ROSETTE / 2;
        widget(graphics, titleX - ROSETTE - 2, rosetteY, ROSETTE_U, ROSETTE_V, ROSETTE, ROSETTE);
        widget(graphics, titleX + TITLE_W + 2, rosetteY, ROSETTE_U, ROSETTE_V, ROSETTE, ROSETTE);

        if (editing) {
            editor.mark(LEFT_X, y - 2, PAGE_W, height + 3);
            int cursor = Mth.clamp(editor.cursor(), 0, value.length());
            boolean blink = (System.currentTimeMillis() / 400L) % 2L == 0L;
            for (int index = 0; index < lines.size(); index++) {
                int[] line = lines.get(index);
                float lineY = y + index * pitch;
                int ruleY = Math.round(lineY + TITLE_RULE * scale);
                graphics.fill(titleX, ruleY, titleX + TITLE_W, ruleY + 1, INK_FAINT);
                String text = value.substring(line[0], line[1]);
                float lineX = LEFT_X + (PAGE_W - letteredWidth(text) * scale) / 2.0F;
                if (value.isEmpty()) {
                    String hint = Component.translatable("alchemia.guide.rename.hint").getString();
                    lettered(graphics, hint, LEFT_X + (PAGE_W - letteredWidth(hint) * scale) / 2.0F, lineY, scale, INK_FAINT);
                }
                inked(graphics, text, lineX, lineY, scale, INK, index == 0);
                boolean here = cursor >= line[0] && (index == lines.size() - 1 || cursor < lines.get(index + 1)[0]);
                if (blink && here) {
                    int caret = Math.round(lineX + letteredWidth(value.substring(line[0], cursor)) * scale);
                    graphics.fill(caret, Math.round(lineY - scale), caret + 1, Math.round(lineY + TITLE_RULE * scale) - 1, INK);
                }
            }
            return y + height + 2;
        }

        int titleW = 0;
        for (int[] line : lines) titleW = Math.max(titleW, Mth.ceil(letteredWidth(value.substring(line[0], line[1])) * scale));
        boolean editable = !pageMode();
        boolean over = editable && hovered(LEFT_X + (PAGE_W - titleW) / 2 - 2, y - 2, titleW + 4, height + 2);
        for (int index = 0; index < lines.size(); index++) {
            String text = value.substring(lines.get(index)[0], lines.get(index)[1]);
            inked(graphics, text, LEFT_X + (PAGE_W - letteredWidth(text) * scale) / 2.0F, y + index * pitch, scale, over ? INK_LINK : INK, index == 0);
        }
        if (editable) {
            hotspot(LEFT_X + (PAGE_W - titleW) / 2 - 2, y - 2, titleW + 4, height + 2,
                    () -> editor.start(NoteEditor.Kind.TITLE, recipe.id(), recipe.name(), PacketNameBrew.MAX_LENGTH, 0, 0),
                    () -> List.of(Component.translatable("alchemia.guide.rename.tip")));
        }
        return y + height;
    }

    private int recipeMethod(GuiGraphics graphics, BrewRecipe recipe) {
        int width = PAGE_W - TRACK_W - 4;
        int y = TOP - (int) scroll();

        y = scrolledHeading(graphics, Component.translatable("alchemia.guide.method"), y, width);
        List<Step> steps = steps(recipe);
        int gutter = Math.max(15, font().width(steps.size() + "."));
        int number = 1;
        for (Step step : steps) {
            Component numeral = Component.literal(number + ".");
            text(graphics, numeral, RIGHT_X + gutter - font().width(numeral), y + 4, INK_RED);
            int iconX = RIGHT_X + gutter + 4;
            if (!step.item().isEmpty()) {
                GuideInk.detailedItem(graphics, step.item(), iconX - 2, y, GuideInk.PLAIN);
            } else if (step.icon() != null) {
                icon(graphics, step.icon(), iconX, y + 2, false);
            }
            int textX = iconX + 16;
            List<FormattedCharSequence> lines = font().split(step.text(), width - (textX - RIGHT_X));
            int rowH = Math.max(STEP_ROW, lines.size() * LINE + 6);
            int lineY = y + (rowH - lines.size() * LINE) / 2 + 1;
            for (FormattedCharSequence line : lines) {
                graphics.drawString(font(), line, textX, lineY, INK, false);
                lineY += LINE;
            }
            boolean linked = step.action() != null;
            if (linked && hovered(RIGHT_X - 2, y, width + 4, rowH) && inView()) wash(graphics, RIGHT_X - 2, y, width + 4, rowH, false);
            if (!step.tooltip().isEmpty() || linked) clippedHotspot(RIGHT_X - 2, y, width + 4, rowH, step.action(), step::tooltip);
            y += rowH;
            number++;
        }

        if (recipe.requiresGrinding() || recipe.requiresTrimming() || recipe.requiresTurning()) {
            y = scrolledHeading(graphics, Component.translatable("alchemia.guide.tools"), y + 4, width);
            y = requirement(graphics, recipe.requiresGrinding(), "alchemia.guide.needs_mortar", "mortar_and_pestle", y, width,
                    held -> held.getItem() instanceof ItemMortarPestle);
            y = requirement(graphics, recipe.requiresTrimming(), "alchemia.guide.needs_nigredo", "nigredo", y, width,
                    held -> held.getItem() instanceof ItemNigredo);
            y = requirement(graphics, recipe.requiresTurning(), "alchemia.guide.needs_turning", "albedo", y, width,
                    held -> held.getItem() instanceof ItemAlbedo || held.getItem() instanceof ItemCitrinitas);
        }
        return y + 4;
    }

    private int requirement(GuiGraphics graphics, boolean needed, String key, String toolId, int y, int width,
                            Predicate<ItemStack> tool) {
        if (!needed) return y;
        boolean carried = minecraft() != null && minecraft().player != null
                && minecraft().player.getInventory().items.stream().anyMatch(tool::test);
        ItemStack sprite = new ItemStack(ItemRegistry.getItemFromRegistry(toolId));
        GuideInk.detailedItem(graphics, sprite, RIGHT_X, y, carried ? GuideInk.PLAIN : GuideInk.SHORT);
        List<FormattedCharSequence> lines = font().split(Component.translatable(key), width - 20);
        int textY = y + Math.max(0, (STEP_ROW - lines.size() * LINE) / 2) + 1;
        for (FormattedCharSequence line : lines) {
            graphics.drawString(font(), line, RIGHT_X + 20, textY, carried ? INK : INK_RED, false);
            textY += LINE;
        }
        clippedHotspot(RIGHT_X - 2, y, width + 4, Math.max(STEP_ROW, lines.size() * LINE + 4), null, () -> List.of(
                Component.translatable(carried ? "alchemia.guide.tool.carried" : "alchemia.guide.tool.missing")));
        return y + Math.max(STEP_ROW, lines.size() * LINE + 4);
    }

    private List<Step> steps(BrewRecipe recipe) {
        List<Step> steps = new ArrayList<>();
        for (String step : recipe.steps()) {
            if (step.isEmpty()) continue;
            char kind = step.charAt(0);
            String value = step.substring(1);
            switch (kind) {
                case BrewRecipe.STEP_ADD -> {
                    ItemStack item = BrewRecipe.stackFor(value);
                    if (item.isEmpty()) continue;
                    Component treatment = GuideItems.treatment(item);
                    Component text = treatment == null
                            ? Component.translatable("alchemia.guide.step.add", item.getHoverName())
                            : Component.translatable("alchemia.guide.step.add_treated", item.getHoverName(), treatment);
                    steps.add(new Step(text, item, null, List.of(item.getHoverName()), pageMode() ? null : followFromRecipe(item)));
                }
                case BrewRecipe.STEP_STIR -> steps.add(stirStep(stirs(value), 1.0));
                case BrewRecipe.STEP_FINE -> steps.add(stirStep(stirs(value), FINE_STIR));
                case BrewRecipe.STEP_FINISH -> steps.add(new Step(Component.translatable("alchemia.guide.step.settle"), ItemStack.EMPTY, GuideIcon.STIR, List.of()));
                case BrewRecipe.STEP_WHIRL -> steps.add(new Step(Component.translatable("alchemia.guide.step.whirl"), ItemStack.EMPTY, GuideIcon.BREW,
                        List.of(Component.translatable("alchemia.guide.step.whirl.tip"))));
                case BrewRecipe.STEP_BREW -> steps.add(new Step(Component.translatable("alchemia.guide.step.brew"), ItemStack.EMPTY, GuideIcon.BREW,
                        List.of(Component.translatable("alchemia.guide.step.brew.tip"))));
                case BrewRecipe.STEP_REDSTONE -> steps.add(new Step(Component.translatable("alchemia.guide.step.redstone"),
                        new ItemStack(Items.REDSTONE), null, List.of(Component.translatable("alchemia.guide.step.redstone.tip")),
                        pageMode() ? null : () -> followReagent(Items.REDSTONE)));
                case BrewRecipe.STEP_BARK -> {
                    Imbuements.Attuner attuner = Imbuements.parse(step);
                    if (attuner == null) continue;
                    ItemStack shown = attuner.stack();
                    steps.add(new Step(Component.translatable("alchemia.guide.step.attune", shown.getHoverName()), shown, null,
                            List.of(Component.translatable("alchemia.guide.reagent.attunes", attuner.region()),
                                    Component.translatable("alchemia.guide.step.bark.tip")),
                            pageMode() ? null : () -> followReagent(shown.getItem())));
                }
                case BrewRecipe.STEP_GLOW -> steps.add(new Step(Component.translatable("alchemia.guide.step.glow"),
                        new ItemStack(Items.GLOWSTONE_DUST), null, List.of(Component.translatable("alchemia.guide.step.glow.tip")),
                        pageMode() ? null : () -> followReagent(Items.GLOWSTONE_DUST)));
                case BrewRecipe.STEP_BOTTLE -> {
                    muddykat.alchemia.common.potion.PotionGlass glass = muddykat.alchemia.common.potion.PotionGlass.byName(value);
                    if (!glass.hasEmptyItem()) continue;
                    ItemStack vessel = new ItemStack(muddykat.alchemia.common.potion.PotionGlasses.emptyItem(glass));
                    steps.add(new Step(Component.translatable("alchemia.guide.step.bottle", vessel.getHoverName()), vessel, null,
                            List.of(vessel.getHoverName())));
                }
                case BrewRecipe.STEP_SPLASH -> steps.add(new Step(Component.translatable("alchemia.guide.step.splash"),
                        new ItemStack(Items.GUNPOWDER), null, List.of(Component.translatable("alchemia.guide.step.splash.tip")),
                        pageMode() ? null : () -> followReagent(Items.GUNPOWDER)));
                default -> {
                }
            }
        }
        return steps;
    }

    private static int stirs(String value) {
        try {
            return Math.max(0, Integer.parseInt(value));
        } catch (NumberFormatException error) {
            return 0;
        }
    }

    private Step stirStep(int count, double weight) {
        double seconds = count * weight / STIRS_PER_SECOND;
        String shown = seconds < 0.1 ? "0.1" : seconds % 1.0 < 0.05 ? String.valueOf(Math.round(seconds)) : String.format("%.1f", seconds);
        Component text = Component.translatable(shown.equals("1") ? "alchemia.guide.step.stir_one" : "alchemia.guide.step.stir", shown);
        List<Component> tooltip = List.of(
                Component.translatable("alchemia.guide.step.stir.count", count),
                Component.translatable("alchemia.guide.step.stir.tip"));
        return new Step(text, ItemStack.EMPTY, GuideIcon.STIR, tooltip);
    }

    private void extractRecipeBar(GuiGraphics graphics, BrewRecipe recipe, int y) {
        if (pageMode()) {
            wrappedCentred(graphics, Component.translatable("alchemia.page.brew_hint"), RIGHT_X, y, INK_FAINT);
            return;
        }

        boolean prepared = recipe.id().equals(book.selected());
        boolean canPrepare = canPrepare();
        Component label = Component.translatable(prepared ? "alchemia.guide.prepared" : "alchemia.guide.prepare");
        int labelW = Math.max(font().width(Component.translatable("alchemia.guide.prepared")),
                font().width(Component.translatable("alchemia.guide.prepare")));
        int rowW = STAMP + 4 + labelW + 8 + (SMALL + 3) * 2 - 3;
        int barX = RIGHT_X + (PAGE_W - rowW) / 2;
        int middle = y + STAMP_DROP + SMALL / 2;
        int stampY = middle - STAMP / 2;
        boolean overPrepare = canPrepare && hovered(barX, stampY, STAMP + 4 + labelW, STAMP);
        stampPart(graphics, barX, stampY, overPrepare && !prepared ? 1 : 0);
        if (prepared) stampPart(graphics, barX, stampY, 2);
        text(graphics, label, barX + STAMP + 4, middle - 4, canPrepare ? (overPrepare ? INK_LINK : INK_HEAD) : INK_FAINT);
        hotspot(barX, stampY, STAMP + 4 + labelW, STAMP, canPrepare ? () -> prepare(recipe) : null, () -> List.of(Component.translatable(
                !canPrepare ? "alchemia.guide.prepare.tome" : context.source().block() ? "alchemia.guide.prepare.desk" : "alchemia.guide.prepare.tip")));
        y += STAMP_DROP;
        int prepareW = STAMP + 4 + labelW + 5;

        int copyX = barX + prepareW + 3;
        boolean overCopy = hovered(copyX, y, SMALL, SMALL);
        smallButton(graphics, copyX, y, overCopy ? 1 : 0, GuideIcon.COPY);
        hotspot(copyX, y, SMALL, SMALL, () -> {
            send(PacketGuideAction.Action.COPY, recipe.id());
            turnPage();
        }, () -> List.of(Component.translatable("alchemia.guide.copy"), Component.translatable("alchemia.guide.copy.tip")));

        int deleteX = copyX + SMALL + 3;
        boolean armed = recipe.id().equals(pendingDelete);
        boolean overDelete = hovered(deleteX, y, SMALL, SMALL);
        smallButton(graphics, deleteX, y, overDelete || armed ? 1 : 0, GuideIcon.DELETE);
        if (armed) wash(graphics, deleteX + 1, y + 1, SMALL - 2, SMALL - 2, true);
        hotspot(deleteX, y, SMALL, SMALL, () -> delete(recipe), () -> List.of(Component.translatable(armed
                ? "alchemia.guide.delete.confirm" : "alchemia.guide.delete.tip")));
    }

    private void stampPart(GuiGraphics graphics, int x, int y, int frame) {
        GuiDraw.blit(graphics, GuidePrimer.photo("stamp"), x, y, frame * STAMP_CELL, 0, STAMP_CELL, STAMP_CELL, STAMP_CELL * 3, STAMP_CELL);
    }

    private void delete(BrewRecipe recipe) {
        if (!recipe.id().equals(pendingDelete)) {
            pendingDelete = recipe.id();
            pendingDeleteTicks = DELETE_CONFIRM_TICKS;
            return;
        }
        pendingDelete = null;
        BrewRecipeBook.forget(book.stack(), recipe.id());
        send(PacketGuideAction.Action.FORGET, recipe.id());
        context.closeRecipe();
        context.refresh();
        if (minecraft() != null) minecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PUT, 0.8F));
    }

    private int countHeld(ItemStack wanted) {
        if (minecraft() == null || minecraft().player == null) return 0;
        int found = 0;
        for (ItemStack held : minecraft().player.getInventory().items) {
            if (held.getItem() != wanted.getItem()) continue;
            if (wanted.getItem() instanceof ItemBark && !Imbuements.sameKind(held, wanted)) continue;
            found += held.getCount();
        }
        return found;
    }
}
