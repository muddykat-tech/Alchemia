package muddykat.alchemia.common.potion;

import muddykat.alchemia.common.items.helper.PathGeometry;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class LureReach {

    public record Span(int lure, int from, PotionEnum target, int stacked, double strength) {
        public boolean bends(int entry) {
            return entry >= from && entry < lure;
        }

        public boolean bendsAnything() {
            return from < lure;
        }
    }

    public static final double STACK_BONUS = 0.5;

    private LureReach() {
    }

    public static List<Span> spans(List<ItemStack> entries, List<Integer> anchors, List<PotionMap.RouteLeg> legs) {
        return spans(entries, anchors, List.of(), legs);
    }

    public static List<Span> spans(List<ItemStack> entries, List<Integer> anchors, List<Double> anchorFrom,
                                   List<PotionMap.RouteLeg> legs) {
        List<Span> spans = new ArrayList<>();

        for (int lure = 0; lure < entries.size(); lure++) {
            VanillaIngredients.Drift drift = VanillaIngredients.driftOf(entries.get(lure));
            if (drift == null || !drift.lure()) continue;

            int from = reachStart(entries, anchors, anchorFrom, legs, lure);
            int stacked = stackedBefore(entries, from, lure);
            spans.add(new Span(lure, from, drift.target(), stacked, strengthOf(entries.get(lure), stacked)));
        }
        return spans;
    }

    public static int reachStart(List<ItemStack> entries, List<Integer> anchors, List<PotionMap.RouteLeg> legs, int lure) {
        return reachStart(entries, anchors, List.of(), legs, lure);
    }

    public static int reachStart(List<ItemStack> entries, List<Integer> anchors, List<Double> anchorFrom,
                                 List<PotionMap.RouteLeg> legs, int lure) {
        int from = 0;

        for (int index = lure - 1; index >= 0; index--) {
            if (index < legs.size() && legs.get(index).path().dead() && !legs.get(index).path().teleport()) {
                from = index + 1;
                break;
            }
        }

        int anchor = lure < anchors.size() ? anchors.get(lure) : 0;
        int start = Math.max(from, Math.min(anchor, lure));

        double partial = lure < anchorFrom.size() ? anchorFrom.get(lure) : 0.0;
        if (partial > 0.0 && start == anchor && start < lure && start < legs.size()) {
            PotionMap.RouteLeg leg = legs.get(start);
            if (leg.lure() || leg.path().teleport() || leg.path().points().size() < 2
                    || partial >= PathGeometry.length(leg.path().points()) - 1.0E-6) {
                start++;
            }
        }
        return start;
    }

    public static Span pending(BrewCourse course, PotionMap map, ItemStack lure) {
        VanillaIngredients.Drift drift = VanillaIngredients.driftOf(lure);
        if (drift == null || !drift.lure()) return null;

        BrewCourse trial = course.copy();
        trial.add(lure);
        List<ItemStack> entries = trial.entries();
        int index = entries.size() - 1;
        int from = reachStart(entries, trial.anchors(), trial.anchorFrom(), trial.legs(map), index);
        int stacked = stackedBefore(entries, from, index);
        return new Span(index, from, drift.target(), stacked, strengthOf(lure, stacked));
    }

    public static double strengthOf(ItemStack lure, int stacked) {
        return stackScale(stacked) * VanillaIngredients.grindScale(lure);
    }

    public static double stackScale(int stacked) {
        return 1.0 + STACK_BONUS * Math.max(0, stacked);
    }

    public static int stackedBefore(List<ItemStack> entries, int from, int lure) {
        int stacked = 0;
        for (int index = Math.max(0, from); index < lure; index++) {
            if (VanillaIngredients.isLure(entries.get(index))) stacked++;
        }
        return stacked;
    }
}
