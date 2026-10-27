package muddykat.alchemia.client.gui.cauldron;

import muddykat.alchemia.client.gui.GuiDraw;

import muddykat.alchemia.client.gui.CauldronArt;
import muddykat.alchemia.client.gui.InkTooltip;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.items.helper.PathGeometry;
import muddykat.alchemia.common.potion.BrewCourse;
import muddykat.alchemia.common.potion.LureReach;
import muddykat.alchemia.common.potion.PotionMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class QueueTimeline {

    public static final int COLUMNS = TileEntityAlchemyCauldron.MAX_QUEUE;
    public static final int ROWS = 1;
    public static final int CELL = 18;

    private static final long SLIDE_MS = 260L;
    private static final long ARRIVE_MS = 300L;

    private final BrewContext context;
    private final LureLayer lures;
    private final TooltipQueue tooltips;

    private int gridX;
    private int gridY;
    private int sourceX;
    private int sourceY;
    private List<ItemStack> shown;
    private int[] from = new int[0];
    private long moved;
    private int arrival = -1;
    private long arrivalAt;
    private ItemStack flying = ItemStack.EMPTY;
    private int flyingX;
    private int flyingY;

    public QueueTimeline(BrewContext context, LureLayer lures, TooltipQueue tooltips) {
        this.context = context;
        this.lures = lures;
        this.tooltips = tooltips;
    }

    public static int cellX(int gridX, int index) {
        return gridX + (index % COLUMNS) * CELL;
    }

    public static int cellY(int gridY, int index) {
        return gridY + (index / COLUMNS) * CELL;
    }

    public void place(int gridX, int gridY, int sourceX, int sourceY) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.sourceX = sourceX;
        this.sourceY = sourceY;
    }

    public int cellAt(double mouseX, double mouseY) {
        int column = (int) Math.floor((mouseX - gridX) / CELL);
        int row = (int) Math.floor((mouseY - gridY) / CELL);
        if (column < 0 || column >= COLUMNS || row < 0 || row >= ROWS) return -1;

        int index = row * COLUMNS + column;
        return index < context.course().plannedStacks().size() ? index : -1;
    }

    public void draw(GuiGraphics graphics, int mouseX, int mouseY) {
        BrewCourse course = context.course();
        List<ItemStack> queued = course.plannedStacks();
        watch(queued);
        flying = ItemStack.EMPTY;

        int hoveredCell = cellAt(mouseX, mouseY);
        long now = Util.getMillis();
        double slide = PathGeometry.ease(Math.min(1.0, (now - moved) / (double) SLIDE_MS));
        double arrive = Math.min(1.0, (now - arrivalAt) / (double) ARRIVE_MS);
        List<Component> hovered = null;
        ItemStack hoveredStack = ItemStack.EMPTY;

        for (int index = 0; index < COLUMNS * ROWS; index++) {
            boolean current = index == 0 && course.started() && index < queued.size();
            CauldronArt.Cell state = index >= queued.size() ? CauldronArt.Cell.PLAIN
                    : current ? CauldronArt.Cell.GILT : CauldronArt.Cell.SELECTED;
            CauldronArt.cell(graphics, cellX(gridX, index), cellY(gridY, index), state);
        }

        for (int index = 0; index < queued.size() && index < COLUMNS * ROWS; index++) {
            int x = cellX(gridX, index);
            int y = cellY(gridY, index);
            ItemStack stack = queued.get(index);
            int source = index < from.length ? from[index] : index;

            if (source < 0 && index == arrival && arrive < 1.0) {
                double t = PathGeometry.ease(arrive);
                flying = stack;
                flyingX = (int) Math.round(Mth.lerp(t, (double) sourceX, x)) + 1;
                flyingY = (int) Math.round(Mth.lerp(t, (double) sourceY, y) - Math.sin(Math.PI * t) * 6.0) + 1;
            } else {
                int offset = source >= 0 && slide < 1.0 ? (int) Math.round((source - index) * CELL * (1.0 - slide)) : 0;
                graphics.renderItem(stack, x + 1 + offset, y + 1);
            }

            if (index == 0 && course.started() && PotionMap.isReady()) {
                PotionMap.RouteLeg leg = course.current(context.map());
                double length = leg == null ? 0.0 : BrewCourse.lengthOf(leg);
                int filled = length <= 0.0 ? 0 : Mth.clamp((int) Math.round(14 * course.progress() / length), 0, 14);
                if (filled >= 2) GuiDraw.sprite(graphics, CauldronArt.LEG_PROGRESS, x + 2, y + 14, filled, 3);
            }

            if (index == hoveredCell) {
                hovered = describe(index, stack);
                hoveredStack = stack;
            }
        }

        lures.drawBrackets(graphics, gridX, gridY, queued.size(), hoveredCell);

        if (hovered != null) {
            List<Component> body = new ArrayList<>();
            for (int line = 1; line < hovered.size(); line++) body.add(Component.literal(hovered.get(line).getString()));
            tooltips.ink(InkTooltip.of(hovered.get(0), body).withIcon(hoveredStack), mouseX, mouseY);
        }
    }

    public void drawFlying(GuiGraphics graphics) {
        if (flying.isEmpty()) return;
        GuiDraw.nextStratum(graphics);
        graphics.renderItem(flying, flyingX, flyingY);
    }

    private List<Component> describe(int index, ItemStack stack) {
        BrewCourse course = context.course();
        String key = course.canRemove(index) ? "alchemia.gui.queue.entry"
                : course.bentByLure(index) ? "alchemia.gui.queue.bent" : "alchemia.gui.queue.stirring";
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable(key, index + 1, stack.getHoverName()));

        int entry = course.spent() + index;
        for (LureReach.Span span : lures.active()) {
            Component scent = span.target().displayName();
            if (span.lure() == entry) {
                int bends = Math.max(0, span.lure() - Math.max(span.from(), course.spent()));
                lines.add(Component.translatable(bends > 0 ? "alchemia.gui.queue.lure" : "alchemia.gui.queue.lure.idle",
                        scent, bends).withStyle(ChatFormatting.GRAY));
                if (span.stacked() > 0 && bends > 0) {
                    lines.add(Component.translatable("alchemia.gui.queue.lure.stacked", span.stacked())
                            .withStyle(ChatFormatting.GRAY));
                }
                if (span.strength() > 1.0 + 1.0E-6 && bends > 0) {
                    lines.add(Component.translatable("alchemia.gui.queue.lure.strength",
                            String.format(Locale.ROOT, "%.1f", span.strength())).withStyle(ChatFormatting.GRAY));
                }
            } else if (span.bends(entry)) {
                lines.add(Component.translatable("alchemia.gui.queue.leaning", scent,
                        course.entries().get(span.lure()).getHoverName()).withStyle(ChatFormatting.GRAY));
            }
        }
        return lines;
    }

    private void watch(List<ItemStack> queued) {
        if (shown == null) {
            shown = copies(queued);
            from = identity(queued.size());
            return;
        }
        if (sameStacks(queued, shown)) return;

        int[] next = new int[queued.size()];
        Arrays.fill(next, -1);
        int previous = shown.size();

        int shift = 0;
        while (shift < previous && !matchesFrom(queued, shown, shift)) shift++;
        if (shift < previous || previous == 0) {
            for (int index = 0; index < queued.size(); index++) next[index] = index + shift < previous ? index + shift : -1;
        } else {
            int removed = 0;
            while (removed < previous && removed < queued.size() && sameStack(queued.get(removed), shown.get(removed))) removed++;
            for (int index = 0; index < queued.size(); index++) {
                int source = index < removed ? index : index + 1;
                next[index] = source < previous && sameStack(queued.get(index), shown.get(source)) ? source : -1;
            }
        }

        arrival = -1;
        for (int index = 0; index < next.length; index++) {
            if (next[index] < 0) arrival = index;
        }
        from = next;
        moved = Util.getMillis();
        if (arrival >= 0) arrivalAt = moved;
        shown = copies(queued);
    }

    private static boolean matchesFrom(List<ItemStack> now, List<ItemStack> before, int shift) {
        int overlap = Math.min(now.size(), before.size() - shift);
        if (overlap <= 0 || before.size() - shift > now.size()) return false;
        for (int index = 0; index < overlap; index++) {
            if (!sameStack(now.get(index), before.get(index + shift))) return false;
        }
        return true;
    }

    private static boolean sameStacks(List<ItemStack> a, List<ItemStack> b) {
        if (a.size() != b.size()) return false;
        for (int index = 0; index < a.size(); index++) {
            if (!sameStack(a.get(index), b.get(index))) return false;
        }
        return true;
    }

    private static boolean sameStack(ItemStack a, ItemStack b) {
        return ItemStack.isSameItemSameComponents(a, b);
    }

    private static List<ItemStack> copies(List<ItemStack> stacks) {
        List<ItemStack> copied = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) copied.add(stack.copy());
        return copied;
    }

    private static int[] identity(int size) {
        int[] identity = new int[size];
        for (int index = 0; index < size; index++) identity[index] = index;
        return identity;
    }
}
