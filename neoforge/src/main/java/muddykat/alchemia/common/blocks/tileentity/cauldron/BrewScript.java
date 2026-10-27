package muddykat.alchemia.common.blocks.tileentity.cauldron;

import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.potion.BrewRecipe;
import muddykat.alchemia.common.potion.VanillaIngredients;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class BrewScript {

    private final List<String> added = new ArrayList<>();
    private final List<String> steps = new ArrayList<>();

    public List<String> added() {
        return added;
    }

    public List<String> steps() {
        return steps;
    }

    public boolean onlyAdds() {
        for (String step : steps) {
            if (step.isEmpty() || step.charAt(0) != BrewRecipe.STEP_ADD) return false;
        }
        return true;
    }

    public void clear() {
        added.clear();
        steps.clear();
    }

    public void step(char kind) {
        steps.add(String.valueOf(kind));
    }

    public void step(String step) {
        steps.add(step);
    }

    public void ingredient(String entry) {
        steps.add(BrewRecipe.STEP_ADD + entry);
        added.add(entry);
    }

    public void modifier(Item item, char kind) {
        added.add(BrewRecipe.encode(BuiltInRegistries.ITEM.getKey(item).toString(), 0));
        steps.add(String.valueOf(kind));
    }

    public void forget(String entry, int occurrence) {
        dropFromEnd(steps, BrewRecipe.STEP_ADD + entry, occurrence);
        dropFromEnd(added, entry, occurrence);
    }

    public void stir(boolean fine) {
        char kind = fine ? BrewRecipe.STEP_FINE : BrewRecipe.STEP_STIR;

        if (!steps.isEmpty()) {
            String last = steps.get(steps.size() - 1);
            if (!last.isEmpty() && last.charAt(0) == kind) {
                steps.set(steps.size() - 1, kind + String.valueOf(countOf(last) + 1));
                return;
            }
        }
        steps.add(kind + "1");
    }

    public void drift(double dx, double dy) {
        if (!steps.isEmpty()) {
            String last = steps.get(steps.size() - 1);
            double[] previous = last.isEmpty() || last.charAt(0) != BrewRecipe.STEP_WHIRL ? null : driftOf(last.substring(1));
            if (previous != null) {
                steps.set(steps.size() - 1, driftStep(previous[0] + dx, previous[1] + dy));
                return;
            }
        }
        steps.add(driftStep(dx, dy));
    }

    public List<String> recordedAdded(List<ItemStack> idle) {
        return recorded(added, "", idle);
    }

    public List<String> recordedSteps(List<ItemStack> idle) {
        return recorded(steps, String.valueOf(BrewRecipe.STEP_ADD), idle);
    }

    public static @Nullable String encodeOf(ItemStack stack) {
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) return null;

        String name = stack.getItem() instanceof ItemIngredient ? id.getPath() : id.toString();
        return BrewRecipe.encode(name, ItemIngredient.crushOf(stack), ItemIngredient.trimOf(stack),
                ItemIngredient.rotationOf(stack));
    }

    public static int countOf(String step) {
        try {
            return Math.max(0, Integer.parseInt(step.substring(1)));
        } catch (NumberFormatException error) {
            return 0;
        }
    }

    public static double @Nullable [] driftOf(String value) {
        int split = value.indexOf(',');
        if (split < 0) return null;
        try {
            return new double[]{Double.parseDouble(value.substring(0, split)), Double.parseDouble(value.substring(split + 1))};
        } catch (NumberFormatException error) {
            return null;
        }
    }

    private static String driftStep(double dx, double dy) {
        return BrewRecipe.STEP_WHIRL + Double.toString(dx) + "," + Double.toString(dy);
    }

    private static List<String> recorded(List<String> recorded, String prefix, List<ItemStack> idle) {
        for (ItemStack stack : idle) {
            if (VanillaIngredients.isLure(stack)) return List.copyOf(recorded);
        }

        List<String> trimmed = new ArrayList<>(recorded);
        for (int index = idle.size() - 1; index >= 0; index--) {
            String entry = encodeOf(idle.get(index));
            if (entry != null) dropFromEnd(trimmed, prefix + entry, 1);
        }
        return List.copyOf(trimmed);
    }

    private static void dropFromEnd(List<String> list, String value, int occurrence) {
        int seen = 0;
        for (int index = list.size() - 1; index >= 0; index--) {
            if (!list.get(index).equals(value)) continue;
            if (++seen < occurrence) continue;

            list.remove(index);
            return;
        }
    }
}
