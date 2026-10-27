package muddykat.alchemia.client.gui.guide;

import muddykat.alchemia.client.gui.GuiDraw;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.client.gui.Lettering;
import muddykat.alchemia.common.guide.GuidePrimer;
import muddykat.alchemia.common.network.packets.PacketGuideAction;
import muddykat.alchemia.common.potion.AlchemiaEffect;
import muddykat.alchemia.common.potion.BrewRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.Util;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static muddykat.alchemia.client.gui.guide.GuideLayout.*;

public abstract class GuidePage {

    private static final ResourceLocation WIDGETS = ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, "textures/gui/guide/widgets.png");
    private static final ResourceLocation PAGE_CORNER = ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, "textures/gui/guide/page_corner.png");
    private static final int WIDGET_SHEET = 256;
    private static final int PAGE_LEFT_EDGE = 12;
    private static final int ICON_STEP = 13;
    private static final int SMALL_U = 92;
    private static final int SMALL_V = 28;
    private static final int CELL_U = 176;
    private static final int CELL_V = 28;
    private static final int CORNER_SHEET_W = CELL * 4;
    private static final int CORNER_SHEET_H = CELL * 2;
    private static final int DIVIDER_U = 90;
    private static final int DIVIDER_V = 80;
    private static final int DIVIDER_W = 150;
    private static final float EFFECT_TITLE_SCALE = 1.0F;
    private static final int FRAME_U = 0;
    private static final int FRAME_V = 28;
    private static final int FRAME = 26;
    private static final int FRAME_INSET = 4;
    private static final int FRAME_RULE = 12;
    private static final int GLOWING_SIZE = 18;
    private static final float GLOWING_CENTRE = 9.0F;
    private static final long MARQUEE_PAUSE_MS = 900L;
    private static final double MARQUEE_MS_PER_PIXEL = 32.0;
    private static final int INK_SHADOW = 0xFFB7A27B;
    private static final int WASH = 0x223B2A1E;
    private static final int WASH_STRONG = 0x333B2A1E;
    private static final int PHOTO_DOT_STEP = 4;
    private static final int CRAFT_FRAME_OUTER = 70;
    private static final int CRAFT_FRAME_SLICE = 9;

    protected final GuideContext context;
    protected final GuideCanvas canvas;
    protected final GuideKnowledge book;
    protected final NoteEditor editor;

    protected GuidePage(GuideContext context) {
        this.context = context;
        this.canvas = context.canvas();
        this.book = context.book();
        this.editor = context.editor();
    }

    protected Font font() {
        return context.getFont();
    }

    protected @Nullable Minecraft minecraft() {
        return context.client();
    }

    protected boolean pageMode() {
        return context.pageMode();
    }

    protected double scroll() {
        return context.scroller().offset();
    }

    protected boolean hovered(int x, int y, int w, int h) {
        return canvas.hovered(x, y, w, h);
    }

    protected boolean inView() {
        return canvas.inView();
    }

    protected void hotspot(int x, int y, int w, int h, @Nullable Runnable action, Supplier<List<Component>> tooltip) {
        canvas.hotspot(x, y, w, h, action, tooltip);
    }

    protected void hotspot(int x, int y, int w, int h, @Nullable Runnable action) {
        canvas.hotspot(x, y, w, h, action, List::of);
    }

    protected void clippedHotspot(int x, int y, int w, int h, @Nullable Runnable action, Supplier<List<Component>> tooltip) {
        canvas.clippedHotspot(x, y, w, h, action, tooltip);
    }

    protected void clippedRightClick(int x, int y, int w, int h, Runnable action) {
        canvas.clippedRightClick(x, y, w, h, action);
    }

    protected void scrollPanel(GuiGraphics graphics, int from, int to, Supplier<Integer> body) {
        context.scroller().panel(this, graphics, from, to, body);
    }

    protected void send(PacketGuideAction.Action action, String argument) {
        context.send(action, argument);
    }

    protected void turnPage() {
        context.turnPage();
    }

    protected void resetScroll() {
        context.resetScroll();
    }

    protected void goTo(GuideChapter next) {
        context.goTo(next);
    }

    protected void openRecipe(BrewRecipe recipe) {
        context.openRecipe(recipe);
    }

    protected void openPrimer(int spread) {
        context.openPrimer(spread);
    }

    protected void followEffect(AlchemiaEffect potion) {
        context.followEffect(potion);
    }

    protected void followReagent(Item item) {
        context.followReagent(item);
    }

    protected @Nullable Runnable followFromRecipe(ItemStack item) {
        return context.followFromRecipe(item);
    }

    protected void commitEdit() {
        editor.commit();
    }

    protected boolean canPrepare() {
        return context.canPrepare();
    }

    protected void prepare(BrewRecipe recipe) {
        context.prepare(recipe);
    }

    protected void widget(GuiGraphics graphics, int x, int y, int u, int v, int w, int h) {
        GuiDraw.blit(graphics, WIDGETS, x, y, u, v, w, h, WIDGET_SHEET, WIDGET_SHEET);
    }

    protected void bookPart(GuiGraphics graphics, int x, int y, int u, int v, int w, int h) {
        GuiDraw.blit(graphics, BOOK, x, y, u, v, w, h, BOOK_SHEET_W, BOOK_SHEET_H);
    }

    protected void icon(GuiGraphics graphics, GuideIcon icon, int x, int y, boolean faded) {
        widget(graphics, x, y, icon.ordinal() * ICON_STEP, faded ? ICON_STEP : 0, ICON, ICON);
    }

    protected void divider(GuiGraphics graphics, int pageX, int y, int width) {
        int w = Math.min(width, DIVIDER_W);
        int u = DIVIDER_U + (DIVIDER_W - w) / 2;
        widget(graphics, pageX + (PAGE_W - w) / 2, y, u, DIVIDER_V, w, DIVIDER_H);
    }

    protected void smallButton(GuiGraphics graphics, int x, int y, int state, GuideIcon icon) {
        widget(graphics, x, y, SMALL_U + state * (SMALL + 1), SMALL_V, SMALL, SMALL);
        icon(graphics, icon, x + 2, y + 2, state == 2);
    }

    protected void cell(GuiGraphics graphics, int x, int y, int state) {
        widget(graphics, x, y, CELL_U + state * (CELL + 1), CELL_V, CELL, CELL);
    }

    protected void text(GuiGraphics graphics, Component text, int x, int y, int colour) {
        graphics.drawString(font(), text, x, y, colour, false);
    }

    protected void centred(GuiGraphics graphics, Component text, int pageX, int y, int colour) {
        text(graphics, text, pageX + (PAGE_W - font().width(text)) / 2, y, colour);
    }

    protected int wrapped(GuiGraphics graphics, Component text, int x, int y, int width, int colour) {
        for (FormattedCharSequence line : font().split(text, width)) {
            graphics.drawString(font(), line, x, y, colour, false);
            y += LINE;
        }
        return y;
    }

    protected int wrappedCentred(GuiGraphics graphics, Component text, int pageX, int y, int colour) {
        for (FormattedCharSequence line : font().split(text, PAGE_W - 8)) {
            graphics.drawString(font(), line, pageX + (PAGE_W - font().width(line)) / 2, y, colour, false);
            y += LINE;
        }
        return y;
    }

    protected String clip(Component text, int width) {
        String plain = text.getString();
        if (font().width(plain) <= width) return plain;
        return font().plainSubstrByWidth(plain, width - font().width("…")) + "…";
    }

    protected int heading(GuiGraphics graphics, Component title, int pageX, int y) {
        centred(graphics, title, pageX, y, INK_HEAD);
        divider(graphics, pageX, y + LINE + 1, PAGE_W - 10);
        return y + LINE + DIVIDER_H + 4;
    }

    protected int chapterHeading(GuiGraphics graphics, Component title, int pageX, int y) {
        initialled(graphics, title.getString(), pageX + PAGE_W / 2.0F, y, 1.0F);
        divider(graphics, pageX, y + TITLE_RULE, PAGE_W - 10);
        return y + TITLE_RULE + DIVIDER_H + 4;
    }

    protected void initialled(GuiGraphics graphics, String text, float centreX, float y, float scale) {
        inked(graphics, text, centreX - letteredWidth(text) * scale / 2.0F, y, scale, INK, true);
    }

    protected void inked(GuiGraphics graphics, String text, float x, float y, float scale, int colour, boolean capital) {
        String initial = !capital || text.isEmpty() ? "" : text.substring(0, text.offsetByCodePoints(0, 1));
        String rest = text.substring(initial.length());
        lettered(graphics, text, x + scale, y + scale, scale, INK_SHADOW);
        lettered(graphics, initial, x, y, scale, INK_RED);
        lettered(graphics, rest, x + letteredWidth(initial) * scale, y, scale, colour);
    }

    protected void wash(GuiGraphics graphics, int x, int y, int w, int h, boolean strong) {
        graphics.fill(x, y, x + w, y + h, strong ? WASH_STRONG : WASH);
    }

    protected void topCorner(GuiGraphics graphics, boolean right, Runnable action, Component tooltip) {
        int x = right ? PAGE_RIGHT_EDGE - CELL + 1 : PAGE_LEFT_EDGE;
        int y = PAGE_TOP_EDGE;
        pageCorner(graphics, x, y, right ? 2 : 3);
        hotspot(x, y, CELL, CELL, action, () -> List.of(tooltip));
    }

    protected void tailpiece(GuiGraphics graphics, int pageX, int y) {
        int centre = pageX + PAGE_W / 2;
        flippedWidget(graphics, centre - DIAMOND / 2 - 2 - VINE_W, y, VINE_U, VINE_V, VINE_W, VINE_H);
        widget(graphics, centre - DIAMOND / 2, y + (VINE_H - DIAMOND) / 2, DIAMOND_U, DIAMOND_V, DIAMOND, DIAMOND);
        flippedWidget(graphics, centre + DIAMOND / 2 + 3, y, VINE_RIGHT_U, VINE_V, VINE_W, VINE_H);
    }

    protected void flippedWidget(GuiGraphics graphics, int x, int y, int u, int v, int w, int h) {
        float sheet = WIDGET_SHEET;
        GuiDraw.blitUv(graphics, WIDGETS, x, y, x + w, y + h, u / sheet, (u + w) / sheet, (v + h) / sheet, v / sheet);
    }

    protected void paperFrame(GuiGraphics graphics, ResourceLocation source, int sourceW, int sourceH, int slice, int x, int y, int w, int h) {
        int scale = GuidePrimer.PHOTO_SCALE;
        int texW = sourceW * scale;
        int texH = sourceH * scale;
        int cut = slice * scale;
        int outW = w * scale;
        int outH = h * scale;
        int spanW = texW - cut * 2;
        int spanH = texH - cut * 2;
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(1.0F / scale, 1.0F / scale, 1.0F);
        GuiDraw.blit(graphics, source, 0, 0, 0, 0, cut, cut, texW, texH);
        GuiDraw.blit(graphics, source, outW - cut, 0, texW - cut, 0, cut, cut, texW, texH);
        GuiDraw.blit(graphics, source, 0, outH - cut, 0, texH - cut, cut, cut, texW, texH);
        GuiDraw.blit(graphics, source, outW - cut, outH - cut, texW - cut, texH - cut, cut, cut, texW, texH);
        for (int along = cut; along < outW - cut; along += spanW) {
            int piece = Math.min(spanW, outW - cut - along);
            GuiDraw.blit(graphics, source, along, 0, cut, 0, piece, cut, texW, texH);
            GuiDraw.blit(graphics, source, along, outH - cut, cut, texH - cut, piece, cut, texW, texH);
        }
        for (int along = cut; along < outH - cut; along += spanH) {
            int piece = Math.min(spanH, outH - cut - along);
            GuiDraw.blit(graphics, source, 0, along, 0, cut, cut, piece, texW, texH);
            GuiDraw.blit(graphics, source, outW - cut, along, texW - cut, cut, cut, piece, texW, texH);
        }
        graphics.pose().popPose();
    }

    protected void itemHolder(GuiGraphics graphics, ItemStack stack, int x, int y) {
        int inner = CELL + HOLDER_MARGIN * 2;
        int ix = x + ITEM_HOLDER_PAD;
        int iy = y + ITEM_HOLDER_PAD;
        graphics.fill(ix, iy, ix + inner, iy + inner, LEATHER_LIGHT);
        cell(graphics, ix + HOLDER_MARGIN, iy + HOLDER_MARGIN, 0);
        graphics.renderItem(stack, ix + HOLDER_MARGIN + 1, iy + HOLDER_MARGIN + 1);
        leatherFrame(graphics, ix, iy, inner, inner, ITEM_HOLDER_PAD, ITEM_HOLDER_PAD);
    }

    protected void leatherFrame(GuiGraphics graphics, int x, int y, int w, int h) {
        leatherFrame(graphics, x, y, w, h, FRAME_PAD_W, FRAME_PAD_H);
    }

    protected void leatherFrame(GuiGraphics graphics, int x, int y, int w, int h, int padW, int padH) {
        paperFrame(graphics, GuidePrimer.craftFrame(), CRAFT_FRAME_OUTER, CRAFT_FRAME_OUTER, CRAFT_FRAME_SLICE,
                x - padW, y - padH, w + padW * 2, h + padH * 2);
    }

    protected void dots(GuiGraphics graphics, int centreX, int y, int shown, int count) {
        int width = count * PHOTO_DOT_STEP - (PHOTO_DOT_STEP - PHOTO_DOT);
        int x = centreX - width / 2;
        for (int index = 0; index < count; index++) {
            int dx = x + index * PHOTO_DOT_STEP;
            graphics.fill(dx, y, dx + PHOTO_DOT, y + PHOTO_DOT, index == shown ? INK_SOFT : INK_FAINT);
        }
    }

    protected void corner(GuiGraphics graphics, boolean next, Runnable action, Component tooltip) {
        int x = next ? PAGE_RIGHT_EDGE - CELL + 1 : PAGE_LEFT_EDGE;
        int y = PAGE_BOTTOM_EDGE - CELL + 1;
        pageCorner(graphics, x, y, next ? 0 : 1);
        hotspot(x, y, CELL, CELL, action, () -> List.of(tooltip));
    }

    private void pageCorner(GuiGraphics graphics, int x, int y, int column) {
        int v = hovered(x, y, CELL, CELL) ? CELL : 0;
        GuiDraw.blit(graphics, PAGE_CORNER, x, y, column * CELL, v, CELL, CELL, CORNER_SHEET_W, CORNER_SHEET_H);
    }

    protected void marquee(GuiGraphics graphics, Component text, int x, int y, int width, int colour, @Nullable Object hover) {
        int full = font().width(text);
        if (full <= width) {
            text(graphics, text, x, y, colour);
            return;
        }
        if (hover == null) {
            text(graphics, Component.literal(clip(text, width)), x, y, colour);
            return;
        }
        long now = Util.getMillis();
        long start = canvas.marquee(hover, now);
        int travel = full - width;
        long glide = (long) (travel * MARQUEE_MS_PER_PIXEL);
        long cycle = 2 * (MARQUEE_PAUSE_MS + glide);
        long at = (now - start) % cycle;
        double offset;
        if (at < MARQUEE_PAUSE_MS) offset = 0;
        else if (at < MARQUEE_PAUSE_MS + glide) offset = (at - MARQUEE_PAUSE_MS) / MARQUEE_MS_PER_PIXEL;
        else if (at < 2 * MARQUEE_PAUSE_MS + glide) offset = travel;
        else offset = travel - (at - 2 * MARQUEE_PAUSE_MS - glide) / MARQUEE_MS_PER_PIXEL;

        GuiDraw.scissor(graphics, x, y - 1, x + width, y + LINE);
        graphics.pose().pushPose();
        graphics.pose().translate((float) -Math.round(offset), 0.0F, 0.0F);
        text(graphics, text, x, y, colour);
        graphics.pose().popPose();
        graphics.disableScissor();
    }

    protected List<int[]> titleLines(String text, float scale) {
        return titleLines(text, scale, TITLE_W);
    }

    protected List<int[]> titleLines(String text, float scale, int width) {
        int limit = (int) (width / scale);
        List<int[]> lines = new ArrayList<>();
        int start = 0;
        while (true) {
            int reach = start;
            while (reach < text.length() && letteredWidth(text.substring(start, reach + 1)) <= limit) reach++;
            if (reach >= text.length()) {
                lines.add(new int[]{start, text.length()});
                return lines;
            }
            int space = text.lastIndexOf(' ', reach);
            if (space > start) {
                lines.add(new int[]{start, space});
                start = space + 1;
            } else {
                int cut = Math.max(reach, start + 1);
                lines.add(new int[]{start, cut});
                start = cut;
            }
        }
    }

    protected void lettered(GuiGraphics graphics, String text, float x, float capTop, float scale, int colour) {
        if (text.isEmpty()) return;
        graphics.pose().pushPose();
        graphics.pose().translate(x, capTop, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(font(), Lettering.of(text), 0, Lettering.RISE, colour, false);
        graphics.pose().popPose();
    }

    protected int letteredWidth(String text) {
        return text.isEmpty() ? 0 : font().width(Lettering.of(text));
    }

    protected int banner(GuiGraphics graphics, String name, int y, int width, Slot slot) {
        float centreX = RIGHT_X + width / 2.0F;
        for (int[] line : titleLines(name, EFFECT_TITLE_SCALE, width - 4)) {
            initialled(graphics, name.substring(line[0], line[1]), centreX, y + 2, EFFECT_TITLE_SCALE);
            y += Math.round(TITLE_LINE * EFFECT_TITLE_SCALE);
        }

        int frameX = RIGHT_X + (width - FRAME) / 2;
        int frameY = y + 2;
        int reach = (Math.min(width - 6, DIVIDER_W) - FRAME) / 2;
        int ruleY = frameY + FRAME_RULE - DIVIDER_H / 2;
        widget(graphics, frameX - reach, ruleY, DIVIDER_U + DIVIDER_W / 2 - FRAME / 2 - reach, DIVIDER_V, reach, DIVIDER_H);
        widget(graphics, frameX + FRAME, ruleY, DIVIDER_U + DIVIDER_W / 2 + FRAME / 2, DIVIDER_V, reach, DIVIDER_H);
        widget(graphics, frameX, frameY, FRAME_U, FRAME_V, FRAME, FRAME);
        slot.draw(frameX + FRAME_INSET, frameY + FRAME_INSET);
        return frameY + FRAME + 6;
    }

    protected interface Slot {
        void draw(int x, int y);
    }

    protected int scrolledHeading(GuiGraphics graphics, Component title, int y, int width) {
        return scrolledHeading(graphics, title, RIGHT_X, y, width);
    }

    protected int scrolledHeading(GuiGraphics graphics, Component title, int x, int y, int width) {
        int ruleY = subtitle(graphics, title, x, y, width);
        int w = Math.min(width - 6, DIVIDER_W);
        widget(graphics, x + (width - w) / 2, ruleY, DIVIDER_U + (DIVIDER_W - w) / 2, DIVIDER_V, w, DIVIDER_H);
        return ruleY + DIVIDER_H + 4;
    }

    protected int subtitle(GuiGraphics graphics, Component title, int y, int width) {
        return subtitle(graphics, title, RIGHT_X, y, width);
    }

    protected int subtitle(GuiGraphics graphics, Component title, int x, int y, int width) {
        initialled(graphics, title.getString(), x + width / 2.0F, y, SUBTITLE_SCALE);
        return y + Math.round(TITLE_RULE * SUBTITLE_SCALE);
    }

    protected static void glowing(GuiGraphics graphics, float centreX, float centreY, float art) {
        float scale = art / GLOWING_ART;
        graphics.pose().pushPose();
        graphics.pose().translate(centreX - GLOWING_CENTRE * scale, centreY - GLOWING_CENTRE * scale, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        GuiDraw.sprite(graphics, glowing(), 0, 0, GLOWING_SIZE, GLOWING_SIZE);
        graphics.pose().popPose();
    }

    protected static ResourceLocation glowing() {
        return mobSprite(MobEffects.GLOWING);
    }

    protected static ResourceLocation mobSprite(Holder<MobEffect> effect) {
        return effect.unwrapKey().map(key -> AlchemiaEffect.defaultSprite(key.location()))
                .orElseGet(() -> ResourceLocation.withDefaultNamespace("missingno"));
    }

    protected int linkedRecipes(GuiGraphics graphics, List<BrewRecipe> using, int y, int width) {
        for (BrewRecipe recipe : using) {
            boolean over = hovered(RIGHT_X - 2, y - 1, width + 4, STEP_ROW) && inView();
            if (over) wash(graphics, RIGHT_X - 2, y - 1, width + 4, STEP_ROW, false);
            graphics.renderItem(GuideItems.potionFor(recipe), RIGHT_X, y);
            text(graphics, Component.literal(clip(recipe.displayName(), width - 22)), RIGHT_X + 20, y + 4, over ? INK_LINK : INK);
            clippedHotspot(RIGHT_X - 2, y - 1, width + 4, STEP_ROW, () -> openRecipe(recipe), () -> List.of(recipe.displayName()));
            y += STEP_ROW;
        }
        return y;
    }
}
