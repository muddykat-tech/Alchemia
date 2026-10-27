package muddykat.alchemia.client.gui.guide;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.client.gui.CauldronArt;
import muddykat.alchemia.client.gui.PathPanel;
import muddykat.alchemia.common.guide.GuidePrimer;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.client.gui.Font;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static muddykat.alchemia.client.gui.guide.GuideLayout.*;

public final class PrimerLayout {

    record Spread(List<Part> left, List<Part> right, @Nullable GuidePrimer.Chapter chapter, boolean index) {
    }

    sealed interface Part permits HeadingPart, RulePart, PhotoPart, ItemsPart, PathPart, CraftPart, LinePart, TailPart {
    }

    record HeadingPart(int y, Component text, boolean chapter) implements Part {
    }

    record RulePart(int y) implements Part {
    }

    record PhotoPart(int y, int x, String key, PrimerBook.Photo photo) implements Part {
    }

    record PhotoBox(int frameW, int frameH, int boundsW, int boundsH) {
        static PhotoBox of(PrimerBook.Photo photo) {
            int frameW = photo.shape().width + PHOTO_MAT * 2;
            int frameH = photo.shape().height + PHOTO_MAT * 2;
            double radians = Math.toRadians(Math.abs(photo.angle()));
            double rotatedW = frameW * Math.cos(radians) + frameH * Math.sin(radians);
            double rotatedH = frameW * Math.sin(radians) + frameH * Math.cos(radians);
            int boundsW = (int) Math.ceil(frameW + (rotatedW - frameW) * PHOTO_TILT_FIT);
            int boundsH = (int) Math.ceil(frameH + (rotatedH - frameH) * PHOTO_TILT_FIT);
            return new PhotoBox(frameW, frameH, Math.min(PAGE_W, boundsW), boundsH);
        }
    }

    record ItemsPart(int y, int x, List<List<ResourceLocation>> slots) implements Part {
    }

    record PathPart(int y, int x, String key, ItemStack item, PathPanel panel, Component label, boolean compact,
                            @Nullable double[] grind) implements Part {
        String name() {
            return item.getHoverName().getString();
        }
    }

    record CraftPart(int y, String key, List<GuidePrimer.CraftSource> crafts, int size) implements Part {
    }

    record LinePart(int y, int x, FormattedCharSequence line) implements Part {
    }

    record TailPart(int y) implements Part {
    }

    private static final int PHOTO_MARGIN_TOP = 3;
    private static final int PHOTO_MARGIN_BOTTOM = 4;
    private static final int PHOTO_MARGIN_EDGE = 1;
    private static final int PHOTO_TEXT_GAP = 1;
    private static final float PHOTO_TILT_FIT = 0.2F;
    private static final int ITEM_MARGIN_BOTTOM = 3;
    private static final int ITEM_TEXT_GAP = 4;
    private static final int PRIMER_TEXT_INSET = 4;
    private static final int PANEL_ROW_MAX = 4;
    private static final int PRIMER_BOTTOM = PAGE_BOTTOM_EDGE - FOLIO_LIFT - 3;
    private static final int PARAGRAPH_GAP = 4;
    private static final int PATH_MARGIN_TOP = 1;
    private static final int PATH_MARGIN_BOTTOM = 4;
    private static final int PATH_LABEL = Math.round(TITLE_RULE * SUBTITLE_SCALE);
    private static final int NAMEPLATE_TEXT_PAD = 3;
    private static final int PLATE_CORNER_CLEAR = 8;
    private static final int SCROLL_TUCK = 2;
    private static final Pattern PRIMER_LINK = Pattern.compile("\\[\\[([a-z_:]+)\\|([^\\]]+)]]");

    private @Nullable List<Spread> primerLayout;
    private @Nullable String primerLayoutKey;
    private final GuideContext context;

    public PrimerLayout(GuideContext context) {
        this.context = context;
    }

    public void invalidate() {
        PrimerBook.clear();
        primerLayout = null;
    }

    private Font font() {
        return context.getFont();
    }

    private int letteredWidth(String text) {
        return CauldronArt.letteredWidth(font(), text);
    }

    List<Spread> spreads() {
        String layoutSettings = PHOTO_MAT + ":" + PHOTO_MARGIN_TOP + ":" + PHOTO_MARGIN_BOTTOM + ":"
                + PHOTO_MARGIN_EDGE + ":" + PHOTO_TEXT_GAP + ":" + PHOTO_TILT_FIT + ":"
                + FRAME_PAD_W + ":" + FRAME_PAD_H + ":" + ITEM_MARGIN_BOTTOM + ":" + ITEM_TEXT_GAP + ":" + PRIMER_TEXT_INSET;
        String key = PrimerBook.language() + "#" + GuidePrimer.generation() + "#" + layoutSettings;
        if (primerLayout != null && key.equals(primerLayoutKey)) return primerLayout;
        List<List<Part>> pages = new ArrayList<>();
        List<GuidePrimer.Chapter> opens = new ArrayList<>();
        pages.add(List.of());
        opens.add(null);
        PrimerBook.Section intro = PrimerBook.section(GuidePrimer.INTRO);
        for (List<Part> page : flow(intro, Component.literal(intro.title()), false, true)) {
            pages.add(page);
            opens.add(null);
        }
        for (int number = 0; number < GuidePrimer.CHAPTERS.size(); number++) {
            GuidePrimer.Chapter entry = GuidePrimer.CHAPTERS.get(number);
            if (!PrimerUnlocks.unlocked(entry)) continue;
            if (pages.size() % 2 == 1) {
                pages.add(List.of());
                opens.add(null);
            }
            for (int part = 0; part < entry.sections().size(); part++) {
                PrimerBook.Section section = PrimerBook.section(entry.sections().get(part));
                Component heading = part == 0
                        ? Component.translatable("alchemia.guide.primer.chapter_line", Component.translatable("enchantment.level." + (number + 1)), section.title())
                        : Component.literal(section.title());
                List<List<Part>> flowed = flow(section, heading, part == 0, false);
                for (int index = 0; index < flowed.size(); index++) {
                    pages.add(flowed.get(index));
                    opens.add(part == 0 && index == 0 ? entry : null);
                }
            }
        }
        if (pages.size() % 2 == 1) {
            pages.add(List.of());
            opens.add(null);
        }
        List<Spread> spreads = new ArrayList<>();
        for (int index = 0; index < pages.size(); index += 2) {
            spreads.add(new Spread(pages.get(index), pages.get(index + 1), opens.get(index), index == 0));
        }
        primerLayout = spreads;
        primerLayoutKey = key;
        return spreads;
    }

    private List<List<Part>> flow(PrimerBook.Section section, Component heading, boolean chapterTitle, boolean tail) {
        Flow flow = new Flow();
        flow.parts.add(new HeadingPart(TOP, heading, chapterTitle));
        flow.y = TOP + (chapterTitle ? TITLE_RULE : Math.round(TITLE_RULE * SUBTITLE_SCALE)) + DIVIDER_H + 4;
        int blockIndex = 0;
        for (PrimerBook.Block block : section.blocks()) {
            String id = section.key() + ":" + blockIndex++;
            dispatch: {
                if (block instanceof PrimerBook.Text text) {
                    if (text.line().isEmpty()) {
                        if (!flow.atTop()) flow.y += PARAGRAPH_GAP;
                    } else {
                        flowText(flow, linked(text.line()));
                    }
                } else if (block instanceof PrimerBook.Photo photo) {
                    PhotoBox box = PhotoBox.of(photo);
                    int edge = Math.min(PHOTO_MARGIN_EDGE, Math.max(0, PAGE_W - box.boundsW()));
                    int height = PHOTO_MARGIN_TOP + box.boundsH() + PHOTO_MARGIN_BOTTOM;
                    flow.place(height);
                    int x = switch (photo.align()) {
                        case LEFT -> edge;
                        case RIGHT -> PAGE_W - edge - box.boundsW();
                        case CENTER -> (PAGE_W - box.boundsW()) / 2;
                    };
                    flow.parts.add(new PhotoPart(flow.y + PHOTO_MARGIN_TOP, x, id, photo));
                    flow.floatOrAdvance(photo.align(), edge + box.boundsW() + PHOTO_TEXT_GAP, height);
                } else if (block instanceof PrimerBook.Items items) {
                    List<List<ResourceLocation>> slots = slots(items.slots());
                    if (slots.isEmpty()) break dispatch;
                    int count = Math.min(slots.size(), PANEL_ROW_MAX);
                    int outer = count * CELL + CRAFT_MARGIN * 2 + FRAME_PAD_W * 2;
                    int h = CELL + CRAFT_MARGIN * 2 + FRAME_PAD_H * 2;
                    flow.place(h);
                    int x = switch (items.align()) {
                        case LEFT -> 0;
                        case RIGHT -> PAGE_W - outer;
                        case CENTER -> (PAGE_W - outer) / 2;
                    };
                    flow.parts.add(new ItemsPart(flow.y, x, slots.subList(0, count)));
                    flow.floatOrAdvance(items.align(), outer + ITEM_TEXT_GAP, h + ITEM_MARGIN_BOTTOM);
                } else if (block instanceof PrimerBook.Path path) {
                    Ingredients ingredient = GuidePaths.primerPathIngredient(path.itemName());
                    if (ingredient == null) {
                        Alchemia.LOGGER.warn("Guide section {} references an unknown ingredient path: {}", section.key(), path.itemName());
                        break dispatch;
                    }
                    GuidePaths.PathVisual visual = GuidePaths.pathVisual(ingredient);
                    if (visual == null || !visual.panel().drawable()) break dispatch;
                    ItemStack item = new ItemStack(ItemRegistry.getItemFromRegistry(ingredient.getRegistryName()));
                    double[] grind = path.grind() && !path.compact() ? GuidePaths.grindTable(ingredient) : null;
                    PathPart probe = new PathPart(0, 0, id, item, visual.panel(), visual.label(), path.compact(), grind);
                    int width = path.compact() ? SCROLL_REST_W : plateWidth(probe);
                    int height = PATH_MARGIN_TOP + (path.compact() ? scrollRestHeight() : plateHeight(probe));
                    int reach = path.compact() ? PATH_MARGIN_TOP + scrollOpenExtent(probe) - (PAGE_BOTTOM_EDGE - 3 - PRIMER_BOTTOM) : 0;
                    flow.place(Math.max(height, reach));
                    PrimerBook.Align align = path.compact() ? path.align() : PrimerBook.Align.CENTER;
                    int x = switch (align) {
                        case LEFT -> 0;
                        case RIGHT -> PAGE_W - width;
                        case CENTER -> (PAGE_W - width) / 2;
                    };
                    flow.parts.add(new PathPart(flow.y + PATH_MARGIN_TOP, x, id, item, visual.panel(), visual.label(), path.compact(), grind));
                    flow.floatOrAdvance(align, width + ITEM_TEXT_GAP, height + PATH_MARGIN_BOTTOM);
                } else if (block instanceof PrimerBook.Crafts crafts) {
                    int size = crafts.compact() && crafts.crafts().stream().allMatch(source -> PrimerRecipes.view(source).fitsSmall()) ? PrimerRecipes.SMALL : 3;
                    int h = CRAFT_GRID_PAD * 2 + CELL * size + CRAFT_MARGIN * 2 + (crafts.crafts().size() > 1 ? PHOTO_ARROW + 2 : 0);
                    flow.place(h);
                    flow.parts.add(new CraftPart(flow.y, id, crafts.crafts(), size));
                    flow.y += h + 4;
                } else if (block instanceof PrimerBook.Rule rule) {
                    flow.place(DIVIDER_H);
                    flow.parts.add(new RulePart(flow.y));
                    flow.y += DIVIDER_H + 4;
                } else if (block instanceof PrimerBook.Break pageBreak) {
                    flow.newPage();
                }
            }
        }
        if (tail) {
            flow.clearFloat();
            if (flow.y + VINE_H <= PRIMER_BOTTOM) flow.parts.add(new TailPart(flow.y + 2));
        }
        flow.pages.add(flow.parts);
        return flow.pages;
    }

    private void flowText(Flow flow, FormattedText text) {
        FormattedText remaining = text;
        while (remaining != null) {
            if (flow.y + LINE > PRIMER_BOTTOM) flow.newPage();
            int x = PRIMER_TEXT_INSET;
            int right = PAGE_W - PRIMER_TEXT_INSET;
            if (flow.y < flow.floatBottom) {
                if (flow.floatLeft) x = Math.max(x, flow.floatReach);
                else right = Math.min(right, PAGE_W - flow.floatReach);
            }
            int width = right - x;
            List<FormattedText> lines = font().getSplitter().splitLines(remaining, width, Style.EMPTY);
            if (lines.isEmpty()) return;
            flow.parts.add(new LinePart(flow.y, x, Language.getInstance().getVisualOrder(lines.get(0))));
            flow.y += LINE;
            if (lines.size() == 1) {
                remaining = null;
            } else {
                List<FormattedText> rest = new ArrayList<>();
                for (int index = 1; index < lines.size(); index++) {
                    if (index > 1) rest.add(FormattedText.of(" "));
                    rest.add(lines.get(index));
                }
                remaining = FormattedText.composite(rest);
            }
        }
    }

    private final class Flow {
        final List<List<Part>> pages = new ArrayList<>();
        List<Part> parts = new ArrayList<>();
        int y;
        int floatBottom;
        int floatReach;
        boolean floatLeft;

        boolean atTop() {
            return parts.size() <= 1;
        }

        void clearFloat() {
            if (floatBottom > y) y = floatBottom;
            floatBottom = 0;
        }

        void place(int height) {
            clearFloat();
            if (y + height > PRIMER_BOTTOM && !atTop()) newPage();
        }

        void floatOrAdvance(PrimerBook.Align align, int reach, int height) {
            if (align == PrimerBook.Align.CENTER) {
                y += height;
                return;
            }
            floatBottom = y + height;
            floatReach = reach;
            floatLeft = align == PrimerBook.Align.LEFT;
        }

        void newPage() {
            pages.add(parts);
            parts = new ArrayList<>();
            parts.add(new RulePart(TOP + 1));
            y = TOP + 1 + DIVIDER_H + 4;
            floatBottom = 0;
        }
    }

    private static List<List<ResourceLocation>> slots(List<String> tokens) {
        List<List<ResourceLocation>> slots = new ArrayList<>();
        for (String token : tokens) {
            if (token.startsWith("#")) {
                slots.addAll(GuidePrimer.live(token.substring(1)));
                continue;
            }
            List<ResourceLocation> options = new ArrayList<>();
            for (String option : token.split("\\|")) {
                ResourceLocation id = ResourceLocation.tryParse(option);
                if (id != null && BuiltInRegistries.ITEM.containsKey(id)) options.add(id);
            }
            if (!options.isEmpty()) slots.add(options);
        }
        return slots;
    }

    float nameplateScale(PathPart part) {
        int widest = PAGE_W - PLATE_CORNER_CLEAR * 2 - NAMEPLATE_TEXT_X - NAMEPLATE_RIGHT - NAMEPLATE_TEXT_PAD;
        return Math.min(NAMEPLATE_SCALE, widest / (float) Math.max(1, letteredWidth(part.name())));
    }

    int nameplateWidth(PathPart part) {
        return NAMEPLATE_TEXT_X + Mth.ceil(letteredWidth(part.name()) * nameplateScale(part)) + NAMEPLATE_TEXT_PAD + NAMEPLATE_RIGHT;
    }

    int plateInnerWidth(PathPart part) {
        int widest = PAGE_W - PHOTO_MAT * 2;
        return Math.min(widest, Math.max(part.panel().plainWidth(), nameplateWidth(part) + PLATE_CORNER_CLEAR * 2 - PHOTO_MAT * 2));
    }

    private int plateWidth(PathPart part) {
        return plateInnerWidth(part) + PHOTO_MAT * 2;
    }

    static int plateInnerHeight(PathPart part) {
        return PLATE_HEAD + part.panel().plainHeight() + (part.grind() != null ? GRIND_H : 0);
    }

    private static int plateHeight(PathPart part) {
        return PLATE_RISE + PHOTO_MAT * 2 + plateInnerHeight(part);
    }

    static int holderOuter() {
        return CELL + HOLDER_MARGIN * 2 + ITEM_HOLDER_PAD * 2;
    }

    static int scrollRollY() {
        return holderOuter() - SCROLL_TUCK;
    }

    static int scrollRestHeight() {
        return scrollRollY() + ROLL_H - PULL_TUCK + PULL_H + 1;
    }

    static int scrollSheetW(PathPart part) {
        return part.panel().plainWidth() + SCROLL_PAD_W * 2;
    }

    static int scrollSheetH(PathPart part) {
        return PATH_LABEL + 2 + part.panel().plainHeight() + SCROLL_PAD_H * 2;
    }

    private static int scrollOpenExtent(PathPart part) {
        return scrollRollY() + ROLL_H - 1 + scrollSheetH(part) + ROLL_H - PULL_TUCK + PULL_H + 1;
    }

    private static Component linked(String raw) {
        MutableComponent text = Component.empty();
        Matcher match = PRIMER_LINK.matcher(raw);
        int last = 0;
        while (match.find()) {
            text.append(raw.substring(last, match.start()));
            text.append(Component.literal(match.group(2)).withStyle(Style.EMPTY.withColor(INK_LINK & 0xFFFFFF).withUnderlined(true)
                    .withInsertion(PRIMER_LINK_TAG + match.group(1))));
            last = match.end();
        }
        text.append(raw.substring(last));
        return text;
    }
}
