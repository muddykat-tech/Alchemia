package muddykat.alchemia.client.gui.guide;

import muddykat.alchemia.common.network.packets.PacketGuideAction;
import muddykat.alchemia.common.potion.BrewRecipeBook;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static muddykat.alchemia.client.gui.guide.GuideLayout.*;

public final class NoteEditor {

    public enum Kind {
        TITLE, RECIPE_NOTES, INGREDIENT_NOTES
    }

    private final GuideContext context;
    private @Nullable EditBox field;
    private @Nullable Kind kind;
    private @Nullable String key;
    private int x;
    private int y;
    private int w;
    private int h;
    private Predicate<String> fits = text -> true;
    private String valid = "";

    public NoteEditor(GuideContext context) {
        this.context = context;
    }

    public void attach(EditBox field) {
        this.field = field;
        field.setResponder(value -> {
            if (fits.test(value)) {
                valid = value;
            } else if (!value.equals(valid)) {
                field.setValue(valid);
            }
        });
    }

    public boolean open() {
        return kind != null;
    }

    public boolean active() {
        return kind != null && field != null;
    }

    public boolean covers(double bookX, double bookY) {
        return bookX >= x && bookX < x + w && bookY >= y && bookY < y + h;
    }

    public boolean editing(Kind kind, String key) {
        return this.kind == kind && key.equals(this.key) && field != null;
    }

    public String value() {
        return field == null ? "" : field.getValue();
    }

    public int cursor() {
        return field == null ? 0 : field.getCursorPosition();
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (field != null) field.keyPressed(keyCode, scanCode, modifiers);
    }

    public void start(Kind kind, String key, String value, int maxLength, int width, int lines) {
        if (field == null) return;
        commit();
        this.kind = kind;
        this.key = key;
        fits = text -> true;
        field.setMaxLength(maxLength);
        field.setValue(value);
        valid = value;
        if (lines > 0) fits = text -> wrapLines(text, width).size() <= lines;
        field.setCursorPosition(value.length());
        field.setHighlightPos(value.length());
        context.setFocused(field);
        field.setFocused(true);
    }

    public void mark(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public void commit() {
        if (kind == null || key == null || field == null) return;
        Kind committed = kind;
        String target = key;
        String value = field.getValue().strip();
        stop();
        switch (committed) {
            case TITLE -> {
                if (BrewRecipeBook.rename(context.book().stack(), target, value)) {
                    context.send(PacketGuideAction.Action.RENAME, PacketGuideAction.renaming(target, value));
                }
            }
            case RECIPE_NOTES -> {
                if (BrewRecipeBook.annotate(context.book().stack(), target, value)) {
                    context.send(PacketGuideAction.Action.NOTE, PacketGuideAction.renaming(target, value));
                }
            }
            case INGREDIENT_NOTES -> {
                if (BrewRecipeBook.annotateIngredient(context.book().stack(), target, value)) {
                    context.send(PacketGuideAction.Action.NOTE_INGREDIENT, PacketGuideAction.renaming(target, value));
                }
            }
        }
        context.refresh();
    }

    public void stop() {
        kind = null;
        key = null;
        if (field != null) field.setFocused(false);
        context.setFocused(null);
    }

    void hint(GuiGraphics graphics, int x, int y, int width, int lines) {
        Font font = context.getFont();
        List<FormattedCharSequence> parts = font.split(Component.translatable("alchemia.guide.notes.hint"), width);
        for (int index = 0; index < Math.min(lines, parts.size()); index++) {
            graphics.drawString(font, parts.get(index), x, y + index * LINE, INK_FAINT, false);
        }
    }

    void draw(GuiGraphics graphics, String text, int x, int y, int width, int lines, boolean active) {
        Font font = context.getFont();
        List<int[]> wrapped = wrapLines(text, width);
        for (int index = 0; index < Math.min(lines, wrapped.size()); index++) {
            int[] line = wrapped.get(index);
            graphics.drawString(font, Component.literal(text.substring(line[0], line[1]).stripTrailing()), x, y + index * LINE, INK_SOFT, false);
        }
        if (!active || field == null || (System.currentTimeMillis() / 400L) % 2L != 0L) return;

        int cursor = Mth.clamp(field.getCursorPosition(), 0, text.length());
        int row = 0;
        int caretX = x;
        for (int index = 0; index < wrapped.size(); index++) {
            int[] line = wrapped.get(index);
            if (cursor >= line[0] && (cursor < line[1] || index == wrapped.size() - 1)) {
                row = index;
                caretX = x + font.width(text.substring(line[0], Math.min(cursor, line[1])));
                break;
            }
        }
        graphics.fill(caretX, y + row * LINE - 1, caretX + 1, y + row * LINE + LINE - 2, INK);
    }

    private List<int[]> wrapLines(String text, int width) {
        Font font = context.getFont();
        List<int[]> lines = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = start;
            int lastBreak = -1;
            while (end < text.length() && font.width(text.substring(start, end + 1)) <= width) {
                if (text.charAt(end) == ' ') lastBreak = end;
                end++;
            }
            if (end < text.length() && lastBreak >= start) end = lastBreak + 1;
            if (end == start) end = start + 1;
            lines.add(new int[]{start, end});
            start = end;
        }
        return lines;
    }
}
