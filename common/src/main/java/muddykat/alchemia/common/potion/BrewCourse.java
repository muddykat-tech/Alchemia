package muddykat.alchemia.common.potion;

import muddykat.alchemia.common.items.helper.PathGeometry;
import muddykat.alchemia.common.items.helper.PathPoint;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class BrewCourse {

    public static final double TELEPORT_STIR = PotionMap.CELL;

    private static final double EPSILON = 1.0E-6;

    public record Arrival(ItemStack stack, PotionMap.PotionPath path, boolean lure) {
    }

    private final List<ItemStack> entries = new ArrayList<>();
    private final List<Integer> anchors = new ArrayList<>();
    private final List<Double> anchorFrom = new ArrayList<>();
    private double originX;
    private double originY;
    private int spent;
    private double progress;
    private double headSkip;

    private PotionMap routedOn;
    private List<PotionMap.RouteLeg> routed = List.of();

    public BrewCourse(double x, double y) {
        this.originX = x;
        this.originY = y;
    }

    public BrewCourse copy() {
        BrewCourse twin = new BrewCourse(originX, originY);
        twin.entries.addAll(entries);
        twin.anchors.addAll(anchors);
        twin.anchorFrom.addAll(anchorFrom);
        twin.spent = spent;
        twin.progress = progress;
        twin.headSkip = headSkip;
        twin.routedOn = routedOn;
        twin.routed = routed;
        return twin;
    }

    public void reset(double x, double y) {
        entries.clear();
        anchors.clear();
        anchorFrom.clear();
        originX = x;
        originY = y;
        spent = 0;
        progress = 0.0;
        headSkip = 0.0;
        routedOn = null;
    }

    public void restore(double x, double y, List<ItemStack> stacks, List<Integer> pulls, int walked, double stirred) {
        restore(x, y, stacks, pulls, List.of(), walked, stirred);
    }

    public void restore(double x, double y, List<ItemStack> stacks, List<Integer> pulls, List<Double> pullFrom,
                        int walked, double stirred) {
        restore(x, y, stacks, pulls, pullFrom, walked, stirred, 0.0);
    }

    public void restore(double x, double y, List<ItemStack> stacks, List<Integer> pulls, List<Double> pullFrom,
                        int walked, double stirred, double skip) {
        List<Double> partial = new ArrayList<>(stacks.size());
        for (int index = 0; index < stacks.size(); index++) partial.add(index < pullFrom.size() ? pullFrom.get(index) : 0.0);

        boolean unchanged = x == originX && y == originY && skip == headSkip && pulls.equals(anchors) && partial.equals(anchorFrom)
                && stacks.size() == entries.size();
        for (int index = 0; unchanged && index < stacks.size(); index++) {
            unchanged = ItemStack.matches(stacks.get(index), entries.get(index));
        }

        if (!unchanged) {
            entries.clear();
            anchors.clear();
            anchorFrom.clear();
            for (int index = 0; index < stacks.size(); index++) {
                entries.add(stacks.get(index));
                anchors.add(index < pulls.size() ? pulls.get(index) : 0);
                anchorFrom.add(partial.get(index));
            }
            originX = x;
            originY = y;
            headSkip = Math.max(0.0, skip);
            routedOn = null;
        }

        spent = Math.max(0, Math.min(entries.size(), walked));
        progress = Math.max(0.0, stirred);
    }

    public void rebase(PotionMap map) {
        PotionMap.RouteLeg leg = current(map);
        boolean partway = leg != null && progress > EPSILON && !leg.path().teleport() && !leg.path().stalled();
        if (spent == 0 && !partway) return;

        PathPoint start = partway ? position(map) : legStart(map);
        double skip = partway ? (spent == 0 ? headSkip : 0.0) + progress : 0.0;
        double walked = partway ? progress : 0.0;

        entries.subList(0, spent).clear();
        for (int index = 0; index < anchors.size(); index++) {
            int anchor = anchors.get(index);
            if (anchor < spent) {
                anchors.set(index, 0);
                anchorFrom.set(index, 0.0);
            } else if (anchor == spent) {
                anchors.set(index, 0);
                anchorFrom.set(index, Math.max(0.0, anchorFrom.get(index) - walked));
            } else {
                anchors.set(index, anchor - spent);
            }
        }
        anchors.subList(0, spent).clear();
        anchorFrom.subList(0, spent).clear();

        originX = start.x();
        originY = start.y();
        spent = 0;
        progress = 0.0;
        headSkip = skip;
        routedOn = null;
    }

    public PathPoint carry(PotionMap map, PathPoint target) {
        rebase(map);
        PathPoint now = position(map);
        double dx = target.x() - now.x();
        double dy = target.y() - now.y();
        shift(dx, dy);
        return new PathPoint(dx, dy);
    }

    public void shift(double dx, double dy) {
        originX += dx;
        originY += dy;
        routedOn = null;
    }

    public double originX() {
        return originX;
    }

    public double originY() {
        return originY;
    }

    public int spent() {
        return spent;
    }

    public double progress() {
        return progress;
    }

    public List<ItemStack> entries() {
        return List.copyOf(entries);
    }

    public List<Integer> anchors() {
        return List.copyOf(anchors);
    }

    public List<Double> anchorFrom() {
        return List.copyOf(anchorFrom);
    }

    public int planned() {
        return entries.size() - spent;
    }

    public boolean started() {
        return progress > EPSILON || (spent == 0 && headSkip > EPSILON);
    }

    public double headSkip() {
        return headSkip;
    }

    public boolean canStir() {
        return spent < entries.size();
    }

    public List<ItemStack> plannedStacks() {
        List<ItemStack> stacks = new ArrayList<>(planned());
        for (int index = spent; index < entries.size(); index++) stacks.add(entries.get(index).copy());
        return stacks;
    }

    public List<ItemStack> unstarted() {
        List<ItemStack> stacks = new ArrayList<>();
        for (int index = spent + (started() ? 1 : 0); index < entries.size(); index++) stacks.add(entries.get(index).copy());
        return stacks;
    }

    public void add(ItemStack stack) {
        entries.add(stack.copyWithCount(1));
        boolean midLeg = started() && VanillaIngredients.isLure(stack);
        anchors.add(spent + (started() && !midLeg ? 1 : 0));
        anchorFrom.add(midLeg ? progress : 0.0);
        routedOn = null;
    }

    public boolean canRemove(int plannedIndex) {
        if (plannedIndex < 0 || plannedIndex >= planned()) return false;
        if (plannedIndex == 0 && started()) return false;
        return !bentByLure(plannedIndex);
    }

    public boolean bentByLure(int plannedIndex) {
        int index = spent + plannedIndex;
        if (index < spent || index >= entries.size()) return false;

        for (int lure = index; lure < entries.size(); lure++) {
            if (!VanillaIngredients.isLure(entries.get(lure))) continue;

            int anchor = anchors.get(lure);
            double partial = lure < anchorFrom.size() ? anchorFrom.get(lure) : 0.0;
            boolean reached = anchor < spent || (anchor == spent && progress > partial + EPSILON);
            if (anchor <= index && reached) return true;
        }
        return false;
    }

    public ItemStack remove(int plannedIndex) {
        if (!canRemove(plannedIndex)) return ItemStack.EMPTY;

        anchors.remove(spent + plannedIndex);
        anchorFrom.remove(spent + plannedIndex);
        routedOn = null;
        return entries.remove(spent + plannedIndex);
    }

    public List<PotionMap.RouteLeg> legs(PotionMap map) {
        if (routedOn != map) {
#if MC_VER >= 26100
            routed = map.route(originX, originY, entries, anchors, anchorFrom, headSkip);
#else
            routed = map.route(originX, originY, entries, anchors, anchorFrom);
#endif
            routedOn = map;
        }
        return routed;
    }

    public static double lengthOf(PotionMap.RouteLeg leg) {
        PotionMap.PotionPath path = leg.path();
        if (path.stalled() || path.points().size() < 2) return 0.0;
        return path.teleport() ? TELEPORT_STIR : path.length();
    }

    public PotionMap.RouteLeg current(PotionMap map) {
        List<PotionMap.RouteLeg> legs = legs(map);
        return spent < legs.size() ? legs.get(spent) : null;
    }

    public boolean teleporting(PotionMap map) {
        if (!started()) return false;

        PotionMap.RouteLeg leg = current(map);
        return leg != null && leg.path().teleport();
    }

    public PathPoint end(PotionMap map) {
        List<PotionMap.RouteLeg> legs = legs(map);
        for (int index = legs.size() - 1; index >= 0; index--) {
            List<PathPoint> points = legs.get(index).path().points();
            if (!points.isEmpty()) return points.get(points.size() - 1);
        }
        return new PathPoint(originX, originY);
    }

    private PathPoint legStart(PotionMap map) {
        PotionMap.RouteLeg leg = current(map);
        if (leg != null && !leg.path().points().isEmpty()) return leg.path().points().get(0);
        return end(map);
    }

    public PathPoint position(PotionMap map) {
        PotionMap.RouteLeg leg = current(map);
        if (leg == null || leg.path().points().isEmpty()) return legStart(map);

        PotionMap.PotionPath path = leg.path();
        if (path.teleport() || path.points().size() < 2) return path.points().get(0);
        return PathGeometry.pointAt(path.points(), progress);
    }

    public boolean stir(PotionMap map, double distance, Predicate<Arrival> onArrival) {
        boolean moved = false;
        boolean changed = false;

        while (true) {
            PotionMap.RouteLeg leg = current(map);
            if (leg == null) break;

            double length = lengthOf(leg);
            if (length - progress > EPSILON) {
                if (moved) break;

                progress = Math.min(length, progress + distance);
                moved = true;
                changed = true;
                if (length - progress > EPSILON) break;
            }

            spent++;
            progress = 0.0;
            changed = true;
            if (!onArrival.test(new Arrival(leg.stack(), leg.path(), leg.lure()))) break;
        }

        compact(map);
        return changed;
    }

    public void compact(PotionMap map) {
        if (spent == 0) return;
        for (int index = spent; index < entries.size(); index++) {
            if (VanillaIngredients.isLure(entries.get(index))) return;
        }

        PathPoint start = legStart(map);
        entries.subList(0, spent).clear();
        anchors.subList(0, spent).clear();
        anchorFrom.subList(0, spent).clear();
        for (int index = 0; index < anchors.size(); index++) anchors.set(index, Math.max(0, anchors.get(index) - spent));

        originX = start.x();
        originY = start.y();
        spent = 0;
        headSkip = 0.0;
        routedOn = null;
    }
}
