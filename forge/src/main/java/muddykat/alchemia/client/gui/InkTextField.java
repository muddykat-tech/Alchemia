package muddykat.alchemia.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class InkTextField {

    private final int width;
    private final int ink;
    private final int faint;
    private final int selection;
    private int scroll;

    public InkTextField(int width, int ink, int faint, int selection) {
        this.width = width;
        this.ink = ink;
        this.faint = faint;
        this.selection = selection;
    }

    public void draw(GuiGraphics graphics, Font font, EditBox field, Component hint, int textX, int textY) {
        int top = textY - 1;
        int bottom = textY + 9;

        String value = field.getValue();
        boolean focused = field.isFocused();

        if (value.isEmpty() && !focused) {
            graphics.drawString(font, hint, textX, textY, faint, false);
            return;
        }

        int cursor = Mth.clamp(field.getCursorPosition(), 0, value.length());
        int selected = cursor;

        String highlighted = field.getHighlighted();
        if (!highlighted.isEmpty()) {
            int before = cursor - highlighted.length();
            selected = before >= 0 && value.startsWith(highlighted, before)
                    ? before
                    : Math.min(value.length(), cursor + highlighted.length());
        }

        int start = focused ? scrolled(font, value, cursor) : 0;
        int end = fitting(font, value, start, width);
        String shown = value.substring(start, end);
        if (!focused && end < value.length()) {
            String ellipsis = "...";
            shown = value.substring(0, fitting(font, value, 0, width - font.width(ellipsis))) + ellipsis;
        }

        if (focused && selected != cursor) {
            int from = Mth.clamp(Math.min(cursor, selected), start, end);
            int to = Mth.clamp(Math.max(cursor, selected), start, end);
            int selStart = textX + font.width(value.substring(start, from));
            int selEnd = textX + font.width(value.substring(start, to));
            graphics.fill(selStart, top, selEnd, bottom, selection);
        }

        graphics.drawString(font, shown, textX, textY, ink, false);

        if (focused && cursor >= start && cursor <= end && (System.currentTimeMillis() / 400L) % 2L == 0L) {
            int caret = textX + font.width(value.substring(start, cursor));
            graphics.fill(caret, top, caret + 1, bottom, ink);
        }
    }

    public int indexAt(Font font, EditBox field, double x, int textX) {
        String value = field.getValue();
        int start = Mth.clamp(scroll, 0, value.length());
        double offset = x - textX;
        int best = start;
        double nearest = Double.MAX_VALUE;
        for (int index = start; index <= value.length(); index++) {
            double distance = Math.abs(font.width(value.substring(start, index)) - offset);
            if (distance < nearest) {
                nearest = distance;
                best = index;
            }
        }
        return best;
    }

    private static int fitting(Font font, String value, int start, int width) {
        int end = start;
        while (end < value.length() && font.width(value.substring(start, end + 1)) <= width) end++;
        return end;
    }

    private int scrolled(Font font, String value, int cursor) {
        scroll = Mth.clamp(scroll, 0, value.length());
        if (cursor < scroll) scroll = cursor;
        while (scroll < cursor && font.width(value.substring(scroll, cursor)) > width - 1) scroll++;
        while (scroll > 0 && font.width(value.substring(scroll - 1)) <= width - 1) scroll--;
        return scroll;
    }
}
