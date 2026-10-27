package muddykat.alchemia.client.gui.cauldron;

import muddykat.alchemia.client.gui.CauldronArt;
import muddykat.alchemia.client.gui.ScrollWidgetRenderer;
import muddykat.alchemia.common.items.helper.PathGeometry;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.Util;

import java.util.ArrayList;
import java.util.List;

public final class NoticeScroll {

    public static final int GAP = 2;

    private static final double ROLL_MS = 420.0;
    private static final long READ_BASE_MS = 2200L;
    private static final long READ_PER_CHAR_MS = 55L;
    private static final long HOVER_GRACE_MS = 800L;
    private static final int PAD = 7;
    private static final int MAX_H = 84;
    private static final int LINE_HEIGHT = 10;

    private final List<Component> pending = new ArrayList<>();
    private List<Component> shown = List.of();
    private boolean open;
    private double roll;
    private long clock;
    private long closeAt;
    private boolean manual;
    private int hitX;
    private int hitY;
    private int hitW;
    private int hitH;

    public void queue(Component text) {
        pending.add(text);
    }

    public boolean held(double mouseX, double mouseY) {
        return hitW > 0 && mouseX >= hitX && mouseX < hitX + hitW && mouseY >= hitY && mouseY < hitY + hitH;
    }

    public void toggle() {
        if (shown.isEmpty()) return;
        open = !open;
        manual = open;
        closeAt = 0L;
    }

    public void draw(GuiGraphics graphics, Font font, int left, int topY, int sheetW, int mouseX, int mouseY) {
        long now = Util.getMillis();
        double elapsed = clock == 0L ? 0.0 : Math.min(100L, now - clock);
        clock = now;

        if (!pending.isEmpty()) {
            if (roll > 0.0) {
                open = false;
                manual = false;
                closeAt = 0L;
            } else {
                shown = List.copyOf(pending);
                pending.clear();
                open = true;
                manual = false;
                closeAt = 0L;
            }
        }
        if (shown.isEmpty()) return;

        roll = Mth.clamp(roll + (open ? 1.0 : -1.0) * elapsed / ROLL_MS, 0.0, 1.0);

        int innerW = sheetW - PAD * 2;
        List<FormattedCharSequence> lines = new ArrayList<>();
        int characters = 0;
        for (Component notice : shown) {
            lines.addAll(font.split(notice, innerW));
            characters += notice.getString().length();
        }
        int sheetH = Math.min(MAX_H, lines.size() * LINE_HEIGHT + PAD * 2 - 2);

        if (open && roll >= 1.0 && closeAt == 0L && !manual) {
            closeAt = now + READ_BASE_MS + characters * READ_PER_CHAR_MS;
        }
        if (closeAt != 0L && now >= closeAt && !held(mouseX, mouseY)) {
            open = false;
            closeAt = 0L;
        } else if (closeAt != 0L && now >= closeAt) {
            closeAt = now + HOVER_GRACE_MS;
        }

        double opened = PathGeometry.ease(roll);
        int sheetTop = topY + ScrollWidgetRenderer.ROLL_H - 1;
        int bottomY = topY + (int) Math.round(opened * (ScrollWidgetRenderer.ROLL_H - 1 + sheetH));
        int sheetX = left + ScrollWidgetRenderer.ROLL_END;
        int rollW = ScrollWidgetRenderer.outerWidth(sheetW);

        ScrollWidgetRenderer.paperPlain(graphics, sheetX, sheetTop, sheetW, bottomY + 1, () -> {
            int lineY = sheetTop + PAD - 1;
            for (FormattedCharSequence line : lines) {
                graphics.drawString(font, line, sheetX + (sheetW - font.width(line)) / 2, lineY, CauldronArt.INK, false);
                lineY += LINE_HEIGHT;
            }
        });

        int pullY = bottomY + ScrollWidgetRenderer.ROLL_H - ScrollWidgetRenderer.PULL_TUCK;
        hitX = left;
        hitY = topY;
        hitW = rollW;
        hitH = pullY + ScrollWidgetRenderer.PULL_H - topY;
        boolean over = held(mouseX, mouseY);
        ScrollWidgetRenderer.pull(graphics, left + (rollW - ScrollWidgetRenderer.PULL_W) / 2, pullY + (over ? 1 : 0), over);
        ScrollWidgetRenderer.roll(graphics, left, topY, rollW);
        ScrollWidgetRenderer.roll(graphics, left, bottomY, rollW);
    }
}
