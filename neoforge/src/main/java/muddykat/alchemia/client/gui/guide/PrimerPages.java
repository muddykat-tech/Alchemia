package muddykat.alchemia.client.gui.guide;

import muddykat.alchemia.client.gui.GuideInk;
import muddykat.alchemia.client.gui.Lettering;
import muddykat.alchemia.client.gui.PathPanel;
import muddykat.alchemia.client.gui.ScrollWidgetRenderer;
import muddykat.alchemia.client.gui.guide.PrimerLayout.CraftPart;
import muddykat.alchemia.client.gui.guide.PrimerLayout.HeadingPart;
import muddykat.alchemia.client.gui.guide.PrimerLayout.ItemsPart;
import muddykat.alchemia.client.gui.guide.PrimerLayout.LinePart;
import muddykat.alchemia.client.gui.guide.PrimerLayout.Part;
import muddykat.alchemia.client.gui.guide.PrimerLayout.PathPart;
import muddykat.alchemia.client.gui.guide.PrimerLayout.PhotoBox;
import muddykat.alchemia.client.gui.guide.PrimerLayout.PhotoPart;
import muddykat.alchemia.client.gui.guide.PrimerLayout.RulePart;
import muddykat.alchemia.client.gui.guide.PrimerLayout.Spread;
import muddykat.alchemia.client.gui.guide.PrimerLayout.TailPart;
import muddykat.alchemia.common.guide.GuidePrimer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static muddykat.alchemia.client.gui.guide.GuideLayout.*;

public final class PrimerPages extends GuidePage {

    private record Unroll(boolean open, long since) {
        float progress(long now) {
            float t = Mth.clamp((now - since) / (float) SCROLL_MS, 0.0F, 1.0F);
            return open ? t : 1.0F - t;
        }
    }

    private static final int LABEL_H = 13;
    private static final int LABEL_CAP = 6;
    private static final int LABEL_MID = 16;
    private static final int LABEL_SHEET_W = LABEL_CAP * 2 + LABEL_MID;
    private static final int LABEL_SHEET_H = LABEL_H + 1;
    private static final int LABEL_PAD = 6;
    private static final float LABEL_TEXT_TOP = 2.5F;
    private static final float LABEL_SCALE = 0.75F;
    private static final int PHOTO_LABEL_RISE = 1;
    private static final int PHOTO_ARROW_INSET = 11;
    private static final int PRIMER_ROW_H = 15;
    private static final float NUMERAL_SCALE = 0.75F;
    private static final int NUMERAL_COLUMN = 22;
    private static final String CRAFT_KEY = "craft:";
    private static final int LEADER_GAP = 4;
    private static final int FOLIO_DASH = 5;
    private static final int LEADER_STEP = 3;
    private static final int LEADER_RISE = 6;
    private static final int NAMEPLATE_MID = 16;
    private static final int NAMEPLATE_SHEET_W = NAMEPLATE_LEFT + NAMEPLATE_MID + NAMEPLATE_RIGHT;
    private static final int NAMEPLATE_SHEET_H = NAMEPLATE_H + 1;
    private static final int NAMEPLATE_ITEM_X = 7;
    private static final int NAMEPLATE_ITEM_Y = 2;
    private static final float NAMEPLATE_TEXT_TOP = 3.0F;
    private static final int ROLL_END = ScrollWidgetRenderer.ROLL_END;
    private static final int SCROLL_SHEET_W = ScrollWidgetRenderer.SHEET_W;
    private static final int SCROLL_SHEET_H = ScrollWidgetRenderer.SHEET_H;
    private static final int PULL_U = ScrollWidgetRenderer.PULL_U;
    private static final int PULL_W = ScrollWidgetRenderer.PULL_W;
    private static final int SCROLL_MS = 260;
    private static final int PHOTO_FRAME_OUTER_W = 138;
    private static final int PHOTO_FRAME_OUTER_H = 82;
    private static final int PHOTO_FRAME_SLICE = 13;
    private static final int CRAFT_GAP = 5;
    private static final int CRAFT_ARROW = 14;

    private int primerSpread = -1;
    private final Map<String, Integer> photoIndex = new HashMap<>();
    private final Map<String, Unroll> unrolls = new HashMap<>();
    private final List<Runnable> overlays = new ArrayList<>();
    private final PrimerLayout layout;
    private final GrindSliders grinds;

    public PrimerPages(GuideContext context, PrimerLayout layout, GrindSliders grinds) {
        super(context);
        this.layout = layout;
        this.grinds = grinds;
    }

    public int spread() {
        return primerSpread;
    }

    public void close() {
        primerSpread = -1;
    }

    public int spreadCount() {
        return layout.spreads().size();
    }

    public void open(int spread) {
        commitEdit();
        unrolls.clear();
        primerSpread = spread;
        resetScroll();
        turnPage();
    }

    public void flip(int delta) {
        int next = primerSpread + delta;
        if (next >= layout.spreads().size()) return;
        open(next);
    }

    public void draw(GuiGraphicsExtractor graphics) {
        List<Spread> spreads = layout.spreads();
        primerSpread = Math.min(primerSpread, spreads.size() - 1);
        Spread spread = spreads.get(primerSpread);
        if (spread.index()) {
            extractPrimerIndex(graphics, spreads);
        } else {
            primerParts(graphics, spread.left(), LEFT_X);
        }
        primerParts(graphics, spread.right(), RIGHT_X);

        folio(graphics, LEFT_X, primerSpread * 2 + 1);
        folio(graphics, RIGHT_X, primerSpread * 2 + 2);
        Component back = primerSpread == 0 ? Component.translatable("alchemia.guide.chapter.contents") : Component.translatable("alchemia.guide.previous");
        corner(graphics, false, () -> flip(-1), back);
        if (primerSpread == 0) {
            topCorner(graphics, false, () -> flip(-1), Component.translatable("alchemia.guide.chapter.contents"));
        } else {
            topCorner(graphics, false, () -> open(0), Component.translatable("alchemia.guide.primer.to_index"));
        }
        if (primerSpread < spreads.size() - 1) corner(graphics, true, () -> flip(1), Component.translatable("alchemia.guide.next"));
    }

    private void primerParts(GuiGraphicsExtractor graphics, List<Part> parts, int pageX) {
        overlays.clear();
        primerPartsInner(graphics, parts, pageX);
        for (Runnable overlay : overlays) overlay.run();
        overlays.clear();
    }

    private void primerPartsInner(GuiGraphicsExtractor graphics, List<Part> parts, int pageX) {
        for (Part part : parts) {
            switch (part) {
                case HeadingPart heading -> {
                    if (heading.chapter()) {
                        chapterHeading(graphics, heading.text(), pageX, heading.y());
                    } else {
                        scrolledHeading(graphics, heading.text(), pageX, heading.y(), PAGE_W);
                    }
                }
                case RulePart rule -> divider(graphics, pageX, rule.y(), PAGE_W - 6);
                case PhotoPart photo -> photo(graphics, photo, pageX);
                case ItemsPart items -> itemPanel(graphics, items.slots(), pageX + items.x() + FRAME_PAD_W, items.y() + FRAME_PAD_H);
                case PathPart path -> pathBlock(graphics, path, pageX);
                case CraftPart craft -> craft(graphics, craft.key(), craft.crafts(), craft.size(), pageX, craft.y());
                case LinePart line -> primerLine(graphics, line.line(), pageX + line.x(), line.y());
                case TailPart tail -> tailpiece(graphics, pageX, tail.y());
            }
        }
    }

    private void extractPrimerIndex(GuiGraphicsExtractor graphics, List<Spread> spreads) {
        String indexTitle = Component.translatable("alchemia.guide.primer.index").getString();
        int y = chapterHeading(graphics, Component.literal(indexTitle), LEFT_X, TOP);
        int titleHalf = letteredWidth(indexTitle) / 2 + 5;
        int rosetteY = TOP + (TITLE_CAP - ROSETTE) / 2;
        int centre = LEFT_X + PAGE_W / 2;
        widget(graphics, centre - titleHalf - ROSETTE, rosetteY, ROSETTE_U, ROSETTE_V, ROSETTE, ROSETTE);
        widget(graphics, centre + titleHalf, rosetteY, ROSETTE_U, ROSETTE_V, ROSETTE, ROSETTE);
        for (int number = 0; number < GuidePrimer.CHAPTERS.size(); number++) {
            GuidePrimer.Chapter entry = GuidePrimer.CHAPTERS.get(number);
            String numeral = Component.translatable("enchantment.level." + (number + 1)).getString();
            int rowY = y;
            float numeralX = LEFT_X + NUMERAL_COLUMN / 2.0F - letteredWidth(numeral) * NUMERAL_SCALE / 2.0F;
            if (!PrimerUnlocks.unlocked(entry)) {
                lettered(graphics, numeral, numeralX, rowY, NUMERAL_SCALE, INK_FAINT);
                text(graphics, Component.translatable("alchemia.guide.primer.locked"), LEFT_X + 24, rowY, INK_FAINT);
                hotspot(LEFT_X, rowY - 3, PAGE_W, PRIMER_ROW_H, null, () -> lockedTooltip(entry));
            } else {
                int target = firstSpread(spreads, entry);
                boolean over = hovered(LEFT_X, rowY - 3, PAGE_W, PRIMER_ROW_H);
                if (over) wash(graphics, LEFT_X, rowY - 3, PAGE_W, PRIMER_ROW_H, false);
                lettered(graphics, numeral, numeralX, rowY, NUMERAL_SCALE, over ? INK_LINK : INK_SOFT);
                Component title = Component.literal(PrimerBook.section(entry.sections().getFirst()).label());
                text(graphics, title, LEFT_X + 24, rowY, over ? INK_LINK : INK);
                String folio = String.valueOf(target * 2 + 1);
                int folioX = LEFT_X + PAGE_W - 4 - font().width(folio);
                text(graphics, Component.literal(folio), folioX, rowY, over ? INK_LINK : INK_SOFT);
                for (int dot = LEFT_X + 24 + font().width(title) + LEADER_GAP; dot < folioX - LEADER_GAP; dot += LEADER_STEP) {
                    graphics.fill(dot, rowY + LEADER_RISE, dot + 1, rowY + LEADER_RISE + 1, INK_FAINT);
                }
                hotspot(LEFT_X, rowY - 3, PAGE_W, PRIMER_ROW_H, () -> open(target));
            }
            y += PRIMER_ROW_H;
        }
        tailpiece(graphics, LEFT_X, y + 2);
    }

    private void folio(GuiGraphicsExtractor graphics, int pageX, int number) {
        String text = String.valueOf(number);
        int w = font().width(text);
        int x = pageX + (PAGE_W - w) / 2;
        int y = PAGE_BOTTOM_EDGE - FOLIO_LIFT;
        text(graphics, Component.literal(text), x, y, INK_SOFT);
        graphics.fill(x - 3 - FOLIO_DASH, y + 4, x - 3, y + 5, INK_FAINT);
        graphics.fill(x + w + 2, y + 4, x + w + 2 + FOLIO_DASH, y + 5, INK_FAINT);
    }

    private List<Component> lockedTooltip(GuidePrimer.Chapter entry) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("alchemia.guide.primer.unlock"));
        for (PrimerBook.Unlock unlock : PrimerUnlocks.requirements(entry)) {
            boolean met = minecraft() != null && minecraft().player != null && PrimerUnlocks.met(unlock, minecraft().player);
            lines.add(PrimerUnlocks.describe(unlock).copy().withStyle(met ? ChatFormatting.GREEN : ChatFormatting.GRAY));
        }
        return lines;
    }

    private int firstSpread(List<Spread> spreads, GuidePrimer.Chapter entry) {
        for (int index = 0; index < spreads.size(); index++) {
            if (spreads.get(index).chapter() == entry) return index;
        }
        return 0;
    }

    private void pathBlock(GuiGraphicsExtractor graphics, PathPart part, int pageX) {
        if (part.compact()) {
            pathScrollRest(graphics, part, pageX);
        } else {
            pathPlate(graphics, part, pageX);
        }
    }

    private void pathPlate(GuiGraphicsExtractor graphics, PathPart part, int pageX) {
        PathPanel panel = part.panel();
        int x = pageX + part.x();
        int frameY = part.y() + PLATE_RISE;
        int innerX = x + PHOTO_MAT;
        int innerY = frameY + PHOTO_MAT;
        int innerW = layout.plateInnerWidth(part);
        int innerH = PrimerLayout.plateInnerHeight(part);
        int frameW = innerW + PHOTO_MAT * 2;
        int frameH = innerH + PHOTO_MAT * 2;
        ScrollWidgetRenderer.chartPaper(graphics, innerX, innerY, innerW, innerH);
        int pathX = innerX + (innerW - panel.plainWidth()) / 2;
        int pathY = innerY + PLATE_HEAD;
        if (part.grind() != null) {
            int units = grinds.units(part.key());
            panel.drawPlain(graphics, pathX, pathY, part.grind()[units]);
        } else {
            panel.drawPlain(graphics, pathX, pathY);
        }
        paperFrame(graphics, GuidePrimer.frame(), PHOTO_FRAME_OUTER_W, PHOTO_FRAME_OUTER_H, PHOTO_FRAME_SLICE, x, frameY, frameW, frameH);
        hotspot(x, frameY, frameW, frameH, null, () -> List.of(part.label()));
        if (part.grind() != null) grinds.slider(graphics, part.key(), part.item(), innerX, innerY + innerH - GRIND_H, innerW, true);
        nameplate(graphics, part, x + (frameW - layout.nameplateWidth(part)) / 2, part.y());
    }

    private void nameplate(GuiGraphicsExtractor graphics, PathPart part, int x, int y) {
        int w = layout.nameplateWidth(part);
        Identifier sheet = GuidePrimer.nameplate();
        graphics.blit(RenderPipelines.GUI_TEXTURED, sheet, x, y, 0, 0, NAMEPLATE_LEFT, NAMEPLATE_SHEET_H, NAMEPLATE_SHEET_W, NAMEPLATE_SHEET_H);
        for (int at = NAMEPLATE_LEFT; at < w - NAMEPLATE_RIGHT; at += NAMEPLATE_MID) {
            int piece = Math.min(NAMEPLATE_MID, w - NAMEPLATE_RIGHT - at);
            graphics.blit(RenderPipelines.GUI_TEXTURED, sheet, x + at, y, NAMEPLATE_LEFT, 0, piece, NAMEPLATE_SHEET_H, NAMEPLATE_SHEET_W, NAMEPLATE_SHEET_H);
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, sheet, x + w - NAMEPLATE_RIGHT, y, NAMEPLATE_LEFT + NAMEPLATE_MID, 0,
                NAMEPLATE_RIGHT, NAMEPLATE_SHEET_H, NAMEPLATE_SHEET_W, NAMEPLATE_SHEET_H);
        float scale = layout.nameplateScale(part);
        inked(graphics, part.name(), x + NAMEPLATE_TEXT_X, y + NAMEPLATE_TEXT_TOP + (NAMEPLATE_SCALE - scale) * Lettering.HEIGHT / 2.0F, scale, INK, true);
        graphics.item(part.item(), x + NAMEPLATE_ITEM_X, y + NAMEPLATE_ITEM_Y);
        hotspot(x + NAMEPLATE_ITEM_X, y + NAMEPLATE_ITEM_Y, 16, 16, null, () -> List.of(part.item().getHoverName()));
    }

    private float unrolled(String key) {
        Unroll state = unrolls.get(key);
        return state == null ? 0.0F : state.progress(net.minecraft.util.Util.getMillis());
    }

    private void toggleUnroll(String key) {
        long now = net.minecraft.util.Util.getMillis();
        float at = unrolled(key);
        boolean open = at <= 0.0F || !unrolls.get(key).open();
        long since = now - (long) ((open ? at : 1.0F - at) * SCROLL_MS);
        unrolls.put(key, new Unroll(open, since));
        turnPage();
    }

    private static float easeOut(float t) {
        float inverse = 1.0F - t;
        return 1.0F - inverse * inverse * inverse;
    }

    private void pathScrollRest(GuiGraphicsExtractor graphics, PathPart part, int pageX) {
        int x = pageX + part.x();
        int y = part.y();
        int holder = PrimerLayout.holderOuter();
        int holderX = x + (SCROLL_REST_W - holder) / 2;
        float progress = unrolled(part.key());
        boolean over = hovered(x, y, SCROLL_REST_W, PrimerLayout.scrollRestHeight());
        itemHolder(graphics, part.item(), holderX, y);
        if (progress <= 0.0F) {
            int rollY = y + PrimerLayout.scrollRollY();
            pull(graphics, x + (SCROLL_REST_W - PULL_W) / 2, rollY + ROLL_H - PULL_TUCK + (over ? 1 : 0), over);
            ScrollWidgetRenderer.roll(graphics, x, rollY, SCROLL_REST_W);
        } else {
            overlays.add(() -> pathScrollOpen(graphics, part, pageX, progress));
        }
        Component hint = Component.translatable(progress > 0.0F ? "alchemia.guide.primer.path_close" : "alchemia.guide.primer.path_open")
                .withStyle(ChatFormatting.GRAY);
        hotspot(x, y, SCROLL_REST_W, PrimerLayout.scrollRestHeight(), () -> toggleUnroll(part.key()), () -> List.of(part.item().getHoverName(), hint));
    }

    private void pathScrollOpen(GuiGraphicsExtractor graphics, PathPart part, int pageX, float progress) {
        PathPanel panel = part.panel();
        float across = easeOut(Mth.clamp(progress / 0.35F, 0.0F, 1.0F));
        float down = easeOut(Mth.clamp((progress - 0.15F) / 0.85F, 0.0F, 1.0F));
        int sheetW = PrimerLayout.scrollSheetW(part);
        int sheetH = PrimerLayout.scrollSheetH(part);
        int fullW = sheetW + ROLL_END * 2;
        float restCentre = pageX + part.x() + SCROLL_REST_W / 2.0F;
        float openCentre = Mth.clamp(restCentre, pageX + fullW / 2.0F, pageX + PAGE_W - fullW / 2.0F);
        int rollW = Math.round(Mth.lerp(across, SCROLL_REST_W, fullW));
        int rollX = Math.round(Mth.lerp(across, restCentre, openCentre) - rollW / 2.0F);
        int topY = part.y() + PrimerLayout.scrollRollY();
        int sheetTop = topY + ROLL_H - 1;
        int shown = Math.round(down * sheetH);
        int bottomY = sheetTop + shown - ROLL_H + 1 + Math.round(down * (ROLL_H - 1));
        int sheetX = rollX + ROLL_END;
        int w = rollW - ROLL_END * 2;
        int sheetBottom = Math.max(sheetTop, bottomY + 1);

        ScrollWidgetRenderer.paper(graphics, sheetX, sheetTop, w, sheetBottom, () -> {
            int contentX = sheetX + (w - sheetW) / 2 + SCROLL_PAD_W;
            int pathTop = subtitle(graphics, part.label(), contentX, sheetTop + SCROLL_PAD_H, panel.plainWidth()) + 2;
            panel.drawPlain(graphics, contentX, pathTop);
        });
        ScrollWidgetRenderer.roll(graphics, rollX, topY, rollW);
        int pullY = bottomY + ROLL_H - PULL_TUCK;
        boolean over = hovered(rollX, topY, rollW, pullY + PULL_H - topY);
        pull(graphics, rollX + (rollW - PULL_W) / 2, pullY + (over ? 1 : 0), over);
        ScrollWidgetRenderer.roll(graphics, rollX, bottomY, rollW);
        hotspot(rollX, topY, rollW, pullY + PULL_H - topY, () -> toggleUnroll(part.key()),
                () -> List.of(Component.translatable("alchemia.guide.primer.path_close")));
    }

    private void pull(GuiGraphicsExtractor graphics, int x, int y, boolean over) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, GuidePrimer.pathScroll(), x, y, PULL_U + (over ? PULL_W : 0), 0, PULL_W, PULL_H, SCROLL_SHEET_W, SCROLL_SHEET_H);
    }

    private void itemPanel(GuiGraphicsExtractor graphics, List<List<Identifier>> slots, int gx, int gy) {
        int count = slots.size();
        int w = count * CELL + CRAFT_MARGIN * 2;
        int h = CELL + CRAFT_MARGIN * 2;
        graphics.fill(gx, gy, gx + w, gy + h, LEATHER_LIGHT);
        for (int index = 0; index < count; index++) {
            List<Identifier> options = slots.get(index);
            ItemStack shown = new ItemStack(BuiltInRegistries.ITEM.getValue(options.get(GuideItems.cycle(options.size()))));
            int x = gx + CRAFT_MARGIN + index * CELL;
            int cy = gy + CRAFT_MARGIN;
            cell(graphics, x, cy, 0);
            graphics.item(shown, x + 1, cy + 1);
            hotspot(x, cy, CELL, CELL, null, () -> List.of(shown.getHoverName()));
        }
        leatherFrame(graphics, gx, gy, w, h);
    }

    private void craft(GuiGraphicsExtractor graphics, String section, List<GuidePrimer.CraftSource> crafts, int size, int pageX, int y) {
        String key = CRAFT_KEY + section;
        int shown = Mth.clamp(photoIndex.getOrDefault(key, 0), 0, crafts.size() - 1);
        PrimerRecipes.View view = PrimerRecipes.view(crafts.get(shown));
        int grid = CELL * size + CRAFT_MARGIN * 2;
        int panel = grid + CRAFT_GRID_PAD * 2 + CRAFT_GAP * 2 + CRAFT_ARROW + CELL;
        int px = pageX + (PAGE_W - panel) / 2;
        int gx = px + CRAFT_GRID_PAD;
        int gy = y + CRAFT_GRID_PAD;
        graphics.fill(gx, gy, gx + grid, gy + grid, LEATHER_LIGHT);
        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                int cx = gx + CRAFT_MARGIN + column * CELL;
                int cy = gy + CRAFT_MARGIN + row * CELL;
                cell(graphics, cx, cy, 0);
                List<ItemStack> options = view.cells(size).get(row * size + column);
                if (options.isEmpty()) continue;
                ItemStack ingredient = options.get(GuideItems.cycle(options.size()));
                graphics.item(ingredient, cx + 1, cy + 1);
                hotspot(cx, cy, CELL, CELL, null, () -> List.of(ingredient.getHoverName()));
            }
        }
        leatherFrame(graphics, gx, gy, grid, grid, CRAFT_GRID_PAD, CRAFT_GRID_PAD);

        int ax = gx + grid + CRAFT_GRID_PAD + CRAFT_GAP;
        int ay = gy + grid / 2;
        graphics.fill(ax, ay - 1, ax + CRAFT_ARROW - 4, ay + 1, INK_SOFT);
        for (int step = 0; step < 4; step++) {
            graphics.fill(ax + CRAFT_ARROW - 4 + step, ay - 3 + step, ax + CRAFT_ARROW - 3 + step, ay + 3 - step, INK_SOFT);
        }
        int rx = ax + CRAFT_ARROW + CRAFT_GAP;
        int ry = gy + (grid - CELL) / 2;
        ItemStack result = view.result();
        cell(graphics, rx, ry, 1);
        if (view.missing() != null) {
            icon(graphics, GuideIcon.UNKNOWN, rx + 3, ry + 3, true);
            Identifier missing = view.missing();
            hotspot(rx, ry, CELL, CELL, null, () -> List.of(Component.translatable("alchemia.guide.primer.no_recipe", missing.toString())));
        } else {
            graphics.item(result, rx + 1, ry + 1);
            graphics.itemDecorations(font(), result, rx + 1, ry + 1);
            hotspot(rx, ry, CELL, CELL, null, () -> List.of(result.getHoverName()));
        }
        int bottom = gy + grid + CRAFT_GRID_PAD;
        if (crafts.size() <= 1) return;
        int rowY = bottom + 2;
        if (shown > 0) flipButton(graphics, px, rowY, false, key, shown - 1, crafts.size());
        if (shown < crafts.size() - 1) flipButton(graphics, px + panel - PHOTO_ARROW, rowY, true, key, shown + 1, crafts.size());
        dots(graphics, px + panel / 2, rowY + (PHOTO_ARROW - PHOTO_DOT) / 2, shown, crafts.size());
    }

    private void primerLine(GuiGraphicsExtractor graphics, FormattedCharSequence line, int x, int y) {
        {
            List<String> targets = new ArrayList<>();
            List<int[]> spans = new ArrayList<>();
            int[] cursor = {0};
            line.accept((index, style, codePoint) -> {
                int w = font().width(FormattedCharSequence.forward(Character.toString(codePoint), style));
                String insertion = style.getInsertion();
                if (insertion != null && insertion.startsWith(PRIMER_LINK_TAG)) {
                    String target = insertion.substring(PRIMER_LINK_TAG.length());
                    int last = targets.size() - 1;
                    if (last >= 0 && targets.get(last).equals(target) && spans.get(last)[1] == cursor[0]) {
                        spans.get(last)[1] += w;
                    } else {
                        targets.add(target);
                        spans.add(new int[]{cursor[0], cursor[0] + w});
                    }
                }
                cursor[0] += w;
                return true;
            });
            for (int index = 0; index < spans.size(); index++) {
                int[] span = spans.get(index);
                String target = targets.get(index);
                int sx = x + span[0] - 1;
                int sw = span[1] - span[0] + 2;
                if (hovered(sx, y - 1, sw, LINE)) wash(graphics, sx, y - 1, sw, LINE, true);
                hotspot(sx, y - 1, sw, LINE, () -> follow(target), () -> List.of(Component.translatable("alchemia.guide.primer.goto")));
            }
            graphics.text(font(), line, x, y, INK, false);
        }
    }

    private void follow(String target) {
        if (target.startsWith("primer:")) {
            GuidePrimer.Chapter entry = GuidePrimer.chapter(target.substring("primer:".length()));
            if (entry == null || !PrimerUnlocks.unlocked(entry)) return;
            open(firstSpread(layout.spreads(), entry));
            return;
        }
        for (GuideChapter entry : GuideChapter.values()) {
            if (entry.name().equalsIgnoreCase(target)) {
                goTo(entry);
                return;
            }
        }
    }

    private void photo(GuiGraphicsExtractor graphics, PhotoPart part, int pageX) {
        PrimerBook.Photo photo = part.photo();
        List<String> photos = photo.names();
        int shown = Mth.clamp(photoIndex.getOrDefault(part.key(), 0), 0, photos.size() - 1);
        int w = photo.shape().width;
        int h = photo.shape().height;
        PhotoBox box = PhotoBox.of(photo);
        int ow = box.frameW();
        int oh = box.frameH();
        int scale = GuidePrimer.PHOTO_SCALE;
        float centreX = pageX + part.x() + box.boundsW() / 2.0F;
        float centreY = part.y() + box.boundsH() / 2.0F;
        float radians = (float) Math.toRadians(photo.angle());

        graphics.pose().pushMatrix();
        graphics.pose().translate(centreX, centreY);
        graphics.pose().rotate(radians);
        graphics.pose().translate(-ow / 2.0F, -oh / 2.0F);

        graphics.pose().pushMatrix();
        graphics.pose().translate(PHOTO_MAT, PHOTO_MAT);
        graphics.pose().scale(1.0F / scale, 1.0F / scale);
        Identifier image = GuidePrimer.photo(photos.get(shown));
        var pipeline = switch (photo.filter()) {
            case INKED -> GuideInk.PIPELINE;
            case INKED_DETAILED -> GuideInk.DETAILED_PIPELINE;
            case NONE -> RenderPipelines.GUI_TEXTURED;
        };
        graphics.blit(pipeline, image, 0, 0, 0, 0, w * scale, h * scale, w * scale, h * scale);
        graphics.pose().popMatrix();
        paperFrame(graphics, GuidePrimer.frame(), PHOTO_FRAME_OUTER_W, PHOTO_FRAME_OUTER_H, PHOTO_FRAME_SLICE, 0, 0, ow, oh);
        String title = photo.title(shown);
        if (!title.isEmpty()) label(graphics, title, widestTitle(photo), ow / 2.0F, -PHOTO_LABEL_RISE, ow - PHOTO_MAT * 4);

        if (photos.size() > 1) {
            int arrowY = PHOTO_MAT + h - PHOTO_ARROW - 2;
            int inset = Math.min(PHOTO_ARROW_INSET, (w - PHOTO_ARROW * 2) / 4);
            if (shown > 0) {
                flipButton(graphics, PHOTO_MAT + inset, arrowY, rotated(PHOTO_MAT + inset, arrowY, ow, oh, centreX, centreY, radians),
                        false, part.key(), shown - 1, photos.size());
            }
            if (shown < photos.size() - 1) {
                int ax = PHOTO_MAT + w - inset - PHOTO_ARROW;
                flipButton(graphics, ax, arrowY, rotated(ax, arrowY, ow, oh, centreX, centreY, radians), true, part.key(), shown + 1, photos.size());
            }
            dots(graphics, PHOTO_MAT + w / 2, PHOTO_MAT + h + (PHOTO_MAT - PHOTO_DOT) / 2, shown, photos.size());
        }
        graphics.pose().popMatrix();
    }

    private int widestTitle(PrimerBook.Photo photo) {
        int widest = 0;
        for (String title : photo.titles()) widest = Math.max(widest, letteredWidth(title));
        return widest;
    }

    private void label(GuiGraphicsExtractor graphics, String text, int widest, float centreX, int top, int maxW) {
        int natural = letteredWidth(text);
        int w = Math.max(LABEL_CAP * 2, Math.min(maxW, Mth.ceil(widest * LABEL_SCALE) + LABEL_PAD * 2));
        float scale = Math.min(LABEL_SCALE, (w - LABEL_PAD * 2) / (float) Math.max(1, natural));
        float textW = natural * scale;
        int x = Math.round(centreX - w / 2.0F);
        labelStrip(graphics, x, top, w);
        inked(graphics, text, x + (w - textW) / 2.0F, top + LABEL_TEXT_TOP + (LABEL_SCALE - scale) * LABEL_H / 2.0F, scale, INK, true);
    }

    private void labelStrip(GuiGraphicsExtractor graphics, int x, int y, int w) {
        Identifier sheet = GuidePrimer.label();
        graphics.blit(RenderPipelines.GUI_TEXTURED, sheet, x, y, 0, 0, LABEL_CAP, LABEL_SHEET_H, LABEL_SHEET_W, LABEL_SHEET_H);
        for (int at = LABEL_CAP; at < w - LABEL_CAP; at += LABEL_MID) {
            int piece = Math.min(LABEL_MID, w - LABEL_CAP - at);
            graphics.blit(RenderPipelines.GUI_TEXTURED, sheet, x + at, y, LABEL_CAP, 0, piece, LABEL_SHEET_H, LABEL_SHEET_W, LABEL_SHEET_H);
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, sheet, x + w - LABEL_CAP, y, LABEL_CAP + LABEL_MID, 0, LABEL_CAP, LABEL_SHEET_H, LABEL_SHEET_W, LABEL_SHEET_H);
    }

    private static int[] rotated(int localX, int localY, int ow, int oh, float centreX, float centreY, float radians) {
        float dx = localX + PHOTO_ARROW / 2.0F - ow / 2.0F;
        float dy = localY + PHOTO_ARROW / 2.0F - oh / 2.0F;
        float cos = Mth.cos(radians);
        float sin = Mth.sin(radians);
        int hitX = Math.round(centreX + dx * cos - dy * sin - PHOTO_ARROW / 2.0F);
        int hitY = Math.round(centreY + dx * sin + dy * cos - PHOTO_ARROW / 2.0F);
        return new int[]{hitX, hitY};
    }

    private void flipButton(GuiGraphicsExtractor graphics, int x, int y, boolean next, String key, int target, int count) {
        flipButton(graphics, x, y, new int[]{x, y}, next, key, target, count);
    }

    private void flipButton(GuiGraphicsExtractor graphics, int x, int y, int[] hit, boolean next, String key, int target, int count) {
        boolean over = hovered(hit[0], hit[1], PHOTO_ARROW, PHOTO_ARROW);
        int scale = GuidePrimer.PHOTO_SCALE;
        int size = PHOTO_ARROW * scale;
        int index = (next ? 2 : 0) + (over ? 1 : 0);
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(1.0F / scale, 1.0F / scale);
        graphics.blit(RenderPipelines.GUI_TEXTURED, GuidePrimer.arrows(), 0, 0, index * size, 0, size, size, size * 4, size);
        graphics.pose().popMatrix();
        hotspot(hit[0], hit[1], PHOTO_ARROW, PHOTO_ARROW, () -> {
            photoIndex.put(key, target);
            turnPage();
        }, () -> List.of(Component.translatable("alchemia.guide.primer.photo", target + 1, count)));
    }
}
