package muddykat.alchemia.client.gui.guide;

import muddykat.alchemia.client.gui.GuiDraw;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.helper.MachineOwner;
import muddykat.alchemia.common.config.Configuration;
import muddykat.alchemia.common.crafting.MachineCrafting;
import muddykat.alchemia.common.guide.GuideMastery;
import muddykat.alchemia.common.guide.GuidePrimer;
import muddykat.alchemia.common.blocks.helper.UpgradeSide;
import muddykat.alchemia.common.crafting.MachineBlueprints;
import muddykat.alchemia.common.crafting.MagnumOpus;
import muddykat.alchemia.common.crafting.PotionEffectIngredient;
import muddykat.alchemia.common.guide.GreatWork;
import muddykat.alchemia.common.network.packets.PacketGuideAction;
import muddykat.alchemia.common.potion.AlchemiaEffect;
import muddykat.alchemia.common.potion.RecipeDiscovery;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import static muddykat.alchemia.client.gui.guide.GuideLayout.*;

public final class OpusPages extends GuidePage {

    private static final int STAGE_ROW = 24;
    private static final int FLASK_COLUMNS = 6;
    private static final int STAGE_INDENT = 16;
    private static final int ITEM_HOLDER = CELL + HOLDER_MARGIN * 2 + 10;

    private @Nullable String selectedStage;
    private boolean machinePage;
    private @Nullable String flaskStage;
    private int flaskFocus;

    public OpusPages(GuideContext context) {
        super(context);
    }

    public void showStages() {
        machinePage = false;
    }

    public void draw(GuiGraphics graphics) {
        if (machinePage) {
            extractMachine(graphics);
            return;
        }
        int y = chapterHeading(graphics, Component.translatable("alchemia.guide.chapter.opus"), LEFT_X, TOP + 2);
        centred(graphics, Component.translatable("alchemia.guide.opus.known", book.greatWork().size(), GreatWork.total()), LEFT_X, y, INK_FAINT);

        List<MagnumOpus.Stage> stages = MagnumOpus.stages();
        if (selectedStage == null) {
            for (MagnumOpus.Stage stage : stages) {
                if (book.greatWork().contains(stage.result())) selectedStage = stage.result();
            }
        }
        int cellX = LEFT_X + STAGE_INDENT;
        int rowY = y + 14;
        for (int index = 0; index < stages.size(); index++) {
            MagnumOpus.Stage stage = stages.get(index);
            boolean known = book.greatWork().contains(stage.result());
            boolean chosen = stage.result().equals(selectedStage);
            int top = rowY + index * STAGE_ROW;
            boolean over = hovered(LEFT_X - 2, top - 2, PAGE_W + 4, STAGE_ROW - 2);

            if (index + 1 < stages.size()) {
                boolean bound = known && book.greatWork().contains(stages.get(index + 1).result());
                int lineX = cellX + CELL / 2;
                for (int lineY = top + CELL; lineY < top + STAGE_ROW; lineY++) {
                    if (bound || (lineY - top) % 2 == 0) graphics.fill(lineX, lineY, lineX + 1, lineY + 1, bound ? INK_SOFT : INK_FAINT);
                }
            }

            if (over || chosen) wash(graphics, LEFT_X - 2, top - 2, PAGE_W + 4, STAGE_ROW - 2, chosen);
            String numeral = Component.translatable("enchantment.level." + (index + 1)).getString();
            float numeralX = cellX - 3 - letteredWidth(numeral) * SUBTITLE_SCALE;
            float numeralY = top + (CELL - TITLE_CAP * SUBTITLE_SCALE) / 2.0F;
            inked(graphics, numeral, numeralX, numeralY, SUBTITLE_SCALE, known ? INK_RED : INK_FAINT, false);
            cell(graphics, cellX, top, known ? chosen ? 2 : over ? 1 : 0 : 3);
            if (known) {
                graphics.renderItem(GreatWork.result(stage), cellX + 1, top + 1);
                text(graphics, GreatWork.result(stage).getHoverName(), cellX + CELL + 6, top, over ? INK_LINK : INK_HEAD);
                text(graphics, Component.translatable("alchemia.opus." + stage.result() + ".epithet"), cellX + CELL + 6, top + 10, INK_FAINT);
            } else {
                icon(graphics, GuideIcon.UNKNOWN, cellX + 3, top + 3, true);
                text(graphics, Component.translatable("alchemia.guide.opus.unrevealed"), cellX + CELL + 6, top, INK_FAINT);
                text(graphics, Component.translatable("alchemia.guide.opus.buried"), cellX + CELL + 6, top + 10, INK_FAINT);
            }
            hotspot(LEFT_X - 2, top - 2, PAGE_W + 4, STAGE_ROW - 2, () -> {
                selectedStage = stage.result();
                resetScroll();
            });
        }

        Component legend = Component.translatable("alchemia.guide.opus.legend");
        int lines = font().split(legend, PAGE_W - 8).size();
        wrappedCentred(graphics, legend, LEFT_X, BOTTOM - lines * LINE, INK_FAINT);

        corner(graphics, true, () -> {
            machinePage = true;
            resetScroll();
            turnPage();
        }, Component.translatable("alchemia.guide.machine.title"));
        scrollPanel(graphics, TOP, BOTTOM - 20, () -> opusDetail(graphics));
    }

    private int opusDetail(GuiGraphics graphics) {
        int width = PAGE_W - TRACK_W - 4;
        int y = TOP - (int) scroll();
        MagnumOpus.Stage stage = selectedStage == null ? null : GreatWork.byName(selectedStage);
        if (stage == null) {
            String key = book.greatWork().isEmpty() ? "alchemia.guide.opus.none" : "alchemia.guide.opus.select";
            return wrapped(graphics, Component.translatable(key), RIGHT_X, y + 30, width, INK_FAINT);
        }

        if (!book.greatWork().contains(stage.result())) {
            y = banner(graphics, Component.translatable("alchemia.guide.opus.unrevealed").getString(), y, width,
                    (x, iy) -> icon(graphics, GuideIcon.UNKNOWN, x + 3, iy + 3, true));
            return wrapped(graphics, Component.translatable("alchemia.guide.opus.locked"), RIGHT_X, y, width, INK_FAINT);
        }

        ItemStack result = GreatWork.result(stage);
        y = banner(graphics, result.getHoverName().getString(), y, width, (x, iy) -> graphics.renderItem(result, x + 1, iy + 1));
        y = wrapped(graphics, Component.translatable("alchemia.opus." + stage.result() + ".lore"), RIGHT_X, y, width, INK_SOFT) + 4;

        y = scrolledHeading(graphics, Component.translatable("alchemia.guide.opus.input"), y + 2, width);
        ItemStack input = GreatWork.input(stage);
        if (input.isEmpty()) {
            icon(graphics, GuideIcon.UNKNOWN, RIGHT_X + 2, y + 2, true);
            y = wrapped(graphics, Component.translatable("alchemia.guide.opus.input.empty"), RIGHT_X + 20, y + 4, width - 20, INK) + 6;
        } else {
            MagnumOpus.Stage before = GreatWork.byName(stage.previous());
            int rowH = ITEM_HOLDER + 2;
            boolean over = hovered(RIGHT_X - 2, y - 1, width + 4, rowH) && inView();
            if (over) wash(graphics, RIGHT_X - 2, y - 1, width + 4, rowH, false);
            itemHolder(graphics, input, RIGHT_X, y);
            text(graphics, input.getHoverName(), RIGHT_X + ITEM_HOLDER + 4, y + (ITEM_HOLDER - 8) / 2, over ? INK_LINK : INK);
            if (before != null) clippedHotspot(RIGHT_X - 2, y - 1, width + 4, rowH, () -> {
                selectedStage = before.result();
                resetScroll();
            }, () -> List.of(input.getHoverName()));
            y += rowH + 2;
        }
        y = wrapped(graphics, Component.translatable("alchemia.guide.opus.fuel"), RIGHT_X, y, width, INK_FAINT) + 4;

        y = subtitle(graphics, Component.translatable("alchemia.guide.opus.flasks"), y + 2, width) + 3;
        y = flaskGrid(graphics, stage, y, width);

        y = scrolledHeading(graphics, Component.translatable("alchemia.guide.opus.yields"), y + 4, width);
        itemHolder(graphics, result, RIGHT_X, y);
        text(graphics, result.getHoverName(), RIGHT_X + ITEM_HOLDER + 4, y + (ITEM_HOLDER - 8) / 2, INK_HEAD);
        return y + ITEM_HOLDER + 2;
    }

    private void extractMachine(GuiGraphics graphics) {
        ItemStack core = MachineBlueprints.CORE.resultStack();
        int y = chapterHeading(graphics, Component.translatable("alchemia.guide.machine.title"), LEFT_X, TOP + 2);
        y = wrappedCentred(graphics, Component.translatable("alchemia.guide.machine.lore"), LEFT_X, y, INK_SOFT) + 4;

        y = scrolledHeading(graphics, Component.translatable("alchemia.guide.machine.assembly"), LEFT_X, y, PAGE_W);
        int centre = LEFT_X + PAGE_W / 2;
        y += FRAME_PAD_H + CRAFT_MARGIN;
        int rowY = y + CELL + 2;
        int boxX = centre - CELL / 2 - CELL - 2 - CRAFT_MARGIN;
        int boxW = CELL * 3 + 4 + CRAFT_MARGIN * 2;
        int boxH = CELL * 2 + 2 + CRAFT_MARGIN * 2;
        graphics.fill(boxX, y - CRAFT_MARGIN, boxX + boxW, y - CRAFT_MARGIN + boxH, LEATHER_LIGHT);
        schematicCell(graphics, UpgradeSide.TOP, centre - CELL / 2, y);
        schematicCell(graphics, UpgradeSide.LEFT, centre - CELL / 2 - CELL - 2, rowY);
        cell(graphics, centre - CELL / 2, rowY, 2);
        graphics.renderItem(core, centre - CELL / 2 + 1, rowY + 1);
        hotspot(centre - CELL / 2, rowY, CELL, CELL, null, () -> List.of(core.getHoverName()));
        schematicCell(graphics, UpgradeSide.RIGHT, centre + CELL / 2 + 2, rowY);
        leatherFrame(graphics, boxX, y - CRAFT_MARGIN, boxW, boxH);
        int frontY = rowY + CELL + CRAFT_MARGIN + FRAME_PAD_H + 4;
        centred(graphics, Component.translatable("alchemia.guide.machine.front"), LEFT_X, frontY, INK_FAINT);
        wrappedCentred(graphics, Component.translatable("alchemia.guide.machine.attach"), LEFT_X, frontY + LINE + 6, INK_SOFT);

        corner(graphics, false, () -> {
            machinePage = false;
            resetScroll();
            turnPage();
        }, Component.translatable("alchemia.guide.chapter.opus"));
        scrollPanel(graphics, TOP, BOTTOM, () -> machineRecipes(graphics));
    }

    private int machineRecipes(GuiGraphics graphics) {
        int width = PAGE_W - TRACK_W - 4;
        int y = TOP - (int) scroll();
        y = scrolledHeading(graphics, Component.translatable("alchemia.guide.machine.core"), y + 2, width);
        y = machineBlueprint(graphics, MachineOwner.CORE, MachineBlueprints.CORE, y, width);
        y = wrapped(graphics, Component.translatable("alchemia.guide.opus.fuel"), RIGHT_X, y, width, INK_FAINT) + 4;

        y = scrolledHeading(graphics, Component.translatable("alchemia.guide.machine.upgrades"), y + 2, width);
        for (UpgradeSide side : new UpgradeSide[]{UpgradeSide.RIGHT, UpgradeSide.LEFT, UpgradeSide.TOP}) {
            y = wrapped(graphics, Component.translatable("alchemia.guide.machine.upgrade." + side.getSerializedName(), side.potionSlots()),
                    RIGHT_X, y, width, INK) + 2;
            y = machineBlueprint(graphics, MachineOwner.of(side), MachineBlueprints.upgrade(side), y, width) + 2;
        }
        return y;
    }

    private int machineBlueprint(GuiGraphics graphics, MachineOwner part, MachineBlueprints.Blueprint blueprint, int y, int width) {
        if (!MachineCrafting.visible(part, stage -> book.greatWork().contains(stage.result()))) {
            return wrapped(graphics, Component.translatable("alchemia.machine.craft.locked"), RIGHT_X, y, width, INK_FAINT) + 4;
        }
        y = blueprintGrid(graphics, blueprint, y, width);
        if (Configuration.requireMachineRepair() && !GuidePrimer.earned(new ResourceLocation(Alchemia.MODID, GuideMastery.restoredName(part)))) {
            y = wrapped(graphics, Component.translatable("alchemia.guide.machine.restore"), RIGHT_X, y - 2, width, INK_FAINT) + 4;
        }
        return y;
    }

    private void schematicCell(GuiGraphics graphics, UpgradeSide side, int x, int y) {
        ItemStack upgrade = MachineBlueprints.upgrade(side).resultStack();
        cell(graphics, x, y, 0);
        graphics.renderItem(upgrade, x + 1, y + 1);
        String slots = String.valueOf(side.potionSlots());
        graphics.drawString(font(), slots, x + CELL - font().width(slots), y + CELL - 8, 0xFFFFFFFF, true);
        hotspot(x, y, CELL, CELL, null, () -> List.of(upgrade.getHoverName(),
                Component.translatable("alchemia.guide.machine.slots", side.potionSlots())));
    }

    private int blueprintGrid(GuiGraphics graphics, MachineBlueprints.Blueprint blueprint, int y, int width) {
        int rows = blueprint.rows().size();
        int columns = blueprint.rows().get(0).length();
        int gridW = columns * CELL;
        int arrowW = 12;
        int holderW = CRAFT_MARGIN + FRAME_PAD_W;
        int holderH = CRAFT_MARGIN + FRAME_PAD_H;
        int total = gridW + holderW * 2 + 6 + arrowW + 6 + CELL;
        int x0 = RIGHT_X + (width - total) / 2 + holderW;
        y += holderH;
        graphics.fill(x0 - CRAFT_MARGIN, y - CRAFT_MARGIN, x0 + gridW + CRAFT_MARGIN, y + rows * CELL + CRAFT_MARGIN, LEATHER_LIGHT);
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                int x = x0 + column * CELL;
                int cy = y + row * CELL;
                ItemStack item = blueprint.at(row, column);
                cell(graphics, x, cy, item.isEmpty() ? 3 : 0);
                if (item.isEmpty()) continue;
                graphics.renderItem(item, x + 1, cy + 1);
                clippedHotspot(x, cy, CELL, CELL, null, () -> List.of(item.getHoverName()));
            }
        }
        leatherFrame(graphics, x0 - CRAFT_MARGIN, y - CRAFT_MARGIN, gridW + CRAFT_MARGIN * 2, rows * CELL + CRAFT_MARGIN * 2);
        int arrowX = x0 + gridW + holderW + 6;
        int arrowY = y + rows * CELL / 2;
        graphics.fill(arrowX, arrowY, arrowX + arrowW - 3, arrowY + 1, INK_SOFT);
        for (int step = 0; step < 4; step++) {
            graphics.fill(arrowX + arrowW - 4 + step, arrowY - 3 + step, arrowX + arrowW - 3 + step, arrowY + 4 - step, INK_SOFT);
        }
        ItemStack result = blueprint.resultStack();
        int resultX = arrowX + arrowW + 6;
        int resultY = arrowY - CELL / 2;
        cell(graphics, resultX, resultY, 2);
        graphics.renderItem(result, resultX + 1, resultY + 1);
        clippedHotspot(resultX, resultY, CELL, CELL, null, () -> List.of(result.getHoverName()));
        return y + rows * CELL + holderH + 4;
    }

    private int flaskGrid(GuiGraphics graphics, MagnumOpus.Stage stage, int y, int width) {
        if (!stage.result().equals(flaskStage)) {
            flaskStage = stage.result();
            flaskFocus = 0;
        }
        int count = stage.potionCount();
        if (count == 0) return y;
        flaskFocus = Mth.clamp(flaskFocus, 0, count - 1);

        int columns = Math.min(count, FLASK_COLUMNS);
        int rows = Mth.ceil(count / (float) columns);
        int x0 = RIGHT_X + (width - columns * CELL) / 2;
        int holderH = CRAFT_MARGIN + FRAME_PAD_H;
        int gridY = y + ICON + 6 + holderH;
        effectBar(graphics, stage.potions().get(flaskFocus), y, width);
        y = gridY;
        graphics.fill(x0 - CRAFT_MARGIN, y - CRAFT_MARGIN, x0 + columns * CELL + CRAFT_MARGIN, y + rows * CELL + CRAFT_MARGIN, LEATHER_LIGHT);
        boolean creative = minecraft() != null && minecraft().player != null && minecraft().player.getAbilities().instabuild;
        for (int index = 0; index < count; index++) {
            int x = x0 + (index % columns) * CELL;
            int cy = y + (index / columns) * CELL;
            boolean over = hovered(x, cy, CELL, CELL) && inView();
            cell(graphics, x, cy, index == flaskFocus ? 2 : over ? 1 : 0);
            PotionEffectIngredient flask = stage.potions().get(index);
            graphics.renderItem(GuideItems.flaskPotion(flask), x + 1, cy + 1);
            int flaskIndex = index;
            List<Component> names = book.flaskNames(flask);
            clippedHotspot(x, cy, CELL, CELL, () -> flaskFocus = flaskIndex, () -> names);
            if (creative) {
                clippedRightClick(x, cy, CELL, CELL, () -> send(PacketGuideAction.Action.SUMMON_FLASK, PacketGuideAction.flask(stage.result(), flaskIndex)));
            }
        }
        leatherFrame(graphics, x0 - CRAFT_MARGIN, y - CRAFT_MARGIN, columns * CELL + CRAFT_MARGIN * 2, rows * CELL + CRAFT_MARGIN * 2);
        return y + rows * CELL + holderH + 4;
    }

    private int effectBar(GuiGraphics graphics, PotionEffectIngredient flask, int y, int width) {
        List<Component> names = book.flaskNames(flask);
        List<PotionEffectIngredient.Requirement> levels = new ArrayList<>();
        for (PotionEffectIngredient.Requirement requirement : flask.required()) {
            for (int level = 0; level <= requirement.amplifier(); level++) levels.add(requirement);
        }
        int slots = Math.max(EFFECT_SLOTS, levels.size());
        int rowW = slots * EFFECT_SLOT;
        int x0 = RIGHT_X + (width - rowW) / 2;
        if (rowW + 2 * (VINE_W + 3) <= width) {
            widget(graphics, x0 - VINE_W - 2, y + 2, VINE_U, VINE_V, VINE_W, VINE_H);
            widget(graphics, x0 + rowW + 1, y + 2, VINE_RIGHT_U, VINE_V, VINE_W, VINE_H);
        }
        for (int slot = 0; slot < slots; slot++) {
            int x = x0 + slot * EFFECT_SLOT;
            if (slot >= levels.size()) {
                widget(graphics, x + (EFFECT_SLOT - DIAMOND) / 2, y + (ICON - DIAMOND) / 2 + 1, DIAMOND_U, DIAMOND_V, DIAMOND, DIAMOND);
                continue;
            }
            PotionEffectIngredient.Requirement requirement = levels.get(slot);
            AlchemiaEffect recipe = RecipeDiscovery.byEffect(requirement.effect());
            boolean known = recipe != null && book.discovered().contains(recipe);
            int iconX = x + (EFFECT_SLOT - ICON) / 2;
            if (known) {
                GuiDraw.sprite(graphics, recipe.sprite(), iconX, y, ICON, ICON);
            } else {
                icon(graphics, GuideIcon.UNKNOWN, iconX, y, true);
            }
            Component name = names.get(flask.required().indexOf(requirement));
            clippedHotspot(x, y, EFFECT_SLOT, ICON, known ? () -> followEffect(recipe) : null, () -> List.of(name));
        }
        return y + ICON + 6;
    }
}
