package muddykat.alchemia.client.gui.guide;

import muddykat.alchemia.client.gui.GuiDraw;

import muddykat.alchemia.client.gui.GuideInk;
import muddykat.alchemia.client.gui.PathPanel;
import muddykat.alchemia.client.gui.ScrollWidgetRenderer;
import muddykat.alchemia.common.guide.GuidePrimer;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static muddykat.alchemia.client.gui.guide.GuideLayout.*;

public final class GrindSliders extends GuidePage {

    private record GrindDrag(String key, int x, int w) {
    }

    private static final int GRIND_ICON = 10;
    private static final int GRIND_INSET = 9;
    private static final int GRIND_KNOB_W = 7;
    private static final int GRIND_KNOB_H = 9;
    private static final int GRIND_TICK = 3;

    private final Map<String, Integer> grinds = new HashMap<>();
    private @Nullable GrindDrag grindDrag;

    public GrindSliders(GuideContext context) {
        super(context);
    }

    public boolean dragging() {
        return grindDrag != null;
    }

    public void drag() {
        if (grindDrag != null) dragGrind(grindDrag);
    }

    public void release() {
        grindDrag = null;
    }

    int units(String key) {
        return grinds.getOrDefault(key, 0);
    }

    void slider(GuiGraphics graphics, String key, ItemStack item, int x, int y, int w, boolean unclipped) {
        int units = grinds.getOrDefault(key, 0);
        int max = Ingredients.MAX_CRUSH_UNITS;
        int iconY = y + (GRIND_H - GRIND_ICON) / 2;
        float iconScale = GRIND_ICON / 16.0F;
        graphics.pose().pushPose();
        graphics.pose().translate(x + GRIND_INSET, iconY, 0.0F);
        graphics.pose().scale(iconScale, iconScale, 1.0F);
        GuideInk.detailedItem(graphics, item, 0, 0, GuideInk.PLAIN);
        graphics.pose().popPose();
        graphics.pose().pushPose();
        graphics.pose().translate(x + w - GRIND_INSET - GRIND_ICON, iconY, 0.0F);
        graphics.pose().scale(iconScale, iconScale, 1.0F);
        GuideInk.detailedItem(graphics, new ItemStack(ItemRegistry.getItemFromRegistry("mortar_and_pestle")), 0, 0, GuideInk.PLAIN);
        graphics.pose().popPose();

        int trackX = x + GRIND_INSET + GRIND_ICON + 5;
        int trackW = w - (GRIND_INSET + GRIND_ICON + 5) * 2;
        int trackY = y + GRIND_H / 2;
        int knobX = trackX + Math.round(trackW * units / (float) max);
        graphics.fill(trackX, trackY, knobX, trackY + 1, INK);
        graphics.fill(knobX, trackY, trackX + trackW + 1, trackY + 1, INK_FAINT);
        graphics.fill(trackX, trackY + 1, trackX + trackW + 1, trackY + 2, 0x60F6EED6);
        for (int level = 0; level <= Ingredients.MAX_CRUSH; level++) {
            int tickX = trackX + Math.round(trackW * level / (float) Ingredients.MAX_CRUSH);
            graphics.fill(tickX, trackY - GRIND_TICK + 1, tickX + 1, trackY + GRIND_TICK, level * Ingredients.CRUSH_UNITS_PER_LEVEL <= units ? INK_SOFT : INK_FAINT);
        }
        boolean over = grindDrag != null && grindDrag.key().equals(key) || hovered(trackX - 3, y, trackW + 7, GRIND_H);
        GuiDraw.blit(graphics, GuidePrimer.grindKnob(), knobX - GRIND_KNOB_W / 2, trackY - GRIND_KNOB_H / 2, over ? GRIND_KNOB_W : 0, 0, GRIND_KNOB_W, GRIND_KNOB_H + 1, GRIND_KNOB_W * 2, GRIND_KNOB_H + 1);
        GrindDrag drag = new GrindDrag(key, trackX, trackW);
        Runnable press = () -> {
            grindDrag = drag;
            dragGrind(drag);
        };
        Supplier<List<Component>> tip = () -> List.of(Component.translatable("alchemia.guide.primer.grind", Ingredients.crushPercent(grinds.getOrDefault(key, 0))),
                Component.translatable("alchemia.guide.primer.grind_hint").withStyle(ChatFormatting.GRAY));
        if (unclipped) {
            hotspot(trackX - 3, y, trackW + 7, GRIND_H, press, tip);
        } else {
            clippedHotspot(trackX - 3, y, trackW + 7, GRIND_H, press, tip);
        }
    }

    private void dragGrind(GrindDrag drag) {
        int max = Ingredients.MAX_CRUSH_UNITS;
        int units = Mth.clamp(Math.round((float) ((canvas.bookX() - drag.x()) / Math.max(1, drag.w()) * max)), 0, max);
        Integer before = grinds.put(drag.key(), units);
        boolean detent = units % Ingredients.CRUSH_UNITS_PER_LEVEL == 0;
        if (detent && (before == null || before != units) && minecraft() != null) {
            minecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.GRINDSTONE_USE, 1.4F + units / (float) max * 0.4F, 0.2F));
        }
    }

    int pathScroll(GuiGraphics graphics, PathPanel panel, @Nullable double[] grind, String key, ItemStack item, int y, int width) {
        int pathW = panel.plainWidth();
        int pathH = panel.plainHeight();
        int sliderH = grind != null ? GRIND_H : 0;
        int sheetW = pathW + SCROLL_PAD_W * 2;
        int sheetH = pathH + sliderH + SCROLL_PAD_H * 2;
        int sheetX = RIGHT_X + (width - sheetW) / 2;
        return ScrollWidgetRenderer.open(graphics, RIGHT_X + (width - ScrollWidgetRenderer.outerWidth(sheetW)) / 2, y, sheetW, sheetH, () -> {
            int pathX = sheetX + SCROLL_PAD_W;
            int pathY = y + ROLL_H - 1 + SCROLL_PAD_H;
            if (grind != null) {
                panel.drawPlain(graphics, pathX, pathY, grind[grinds.getOrDefault(key, 0)]);
                slider(graphics, key, item, sheetX, pathY + pathH, sheetW, false);
            } else {
                panel.drawPlain(graphics, pathX, pathY);
            }
        });
    }
}
