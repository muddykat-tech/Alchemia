package muddykat.alchemia.client.gui.cauldron;

import muddykat.alchemia.client.gui.GuiDraw;

import muddykat.alchemia.client.gui.CauldronArt;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import javax.annotation.Nullable;

public final class ActionButtons {

    public static final int HEIGHT = 18;

    private static final long PRESS_MS = 140L;

    private int pressed = -1;
    private long pressedAt = Long.MIN_VALUE;

    public void press(int button) {
        pressed = button;
        pressedAt = Util.getMillis();
    }

    public boolean down(int button, boolean held) {
        return held || (pressed == button && Util.getMillis() - pressedAt < PRESS_MS);
    }

    public static void face(GuiGraphics graphics, int x, int y, int w, boolean ready, boolean hovered, boolean down, boolean call) {
        ResourceLocation sprite;
        if (!ready) {
            sprite = CauldronArt.BUTTON_DISABLED;
        } else if (call) {
            sprite = down ? CauldronArt.BUTTON_READY_PRESSED : hovered ? CauldronArt.BUTTON_READY_HIGHLIGHTED : CauldronArt.BUTTON_READY;
        } else {
            sprite = down ? CauldronArt.BUTTON_PRESSED : hovered ? CauldronArt.BUTTON_HIGHLIGHTED : CauldronArt.BUTTON;
        }
        GuiDraw.sprite(graphics, sprite, x, y, w, HEIGHT);
    }

    public static void label(GuiGraphics graphics, Font font, int x, int y, int w, boolean ready, boolean down, boolean call,
                             @Nullable CauldronArt.Icon icon, Component label) {
        int sink = down && ready ? 1 : 0;
        int colour = !ready ? CauldronArt.INK_FAINT : call ? CauldronArt.PAGE_SHINE : CauldronArt.INK;
        int iconW = icon == null ? 0 : CauldronArt.ICON + 3;
        String shown = CauldronArt.clip(font, label, w - 8 - iconW);
        int start = x + (w - font.width(shown) - iconW) / 2;
        if (icon != null) CauldronArt.icon(graphics, icon, start, y + 3 + sink, !ready);
        graphics.drawString(font, shown, start + iconW, y + 5 + sink, colour, false);
    }
}
