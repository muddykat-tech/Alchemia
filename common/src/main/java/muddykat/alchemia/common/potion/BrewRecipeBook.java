package muddykat.alchemia.common.potion;

import muddykat.alchemia.registration.registers.DataComponentRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class BrewRecipeBook {

    public static final int STARTING_PAGES = 6;
    public static final int MAX_PAGES = 64;

    public enum Recorded {
        NONE,
        WRITTEN,
        DUPLICATE,
        NO_PAGES
    }

    public static List<BrewRecipe> recipes(ItemStack guide) {
        return guide.getOrDefault(DataComponentRegistry.BREW_RECIPES.get(), List.of());
    }

    public static int pages(ItemStack guide) {
        Integer bound = guide.get(DataComponentRegistry.GUIDE_PAGES.get());
        return Math.max(bound == null ? STARTING_PAGES : bound, recipes(guide).size());
    }

    public static int freePages(ItemStack guide) {
        return Math.max(0, pages(guide) - recipes(guide).size());
    }

    public static int bindPages(ItemStack guide, int wanted) {
        int pages = pages(guide);
        int bound = Math.max(0, Math.min(wanted, MAX_PAGES - pages));
        if (bound > 0) guide.set(DataComponentRegistry.GUIDE_PAGES.get(), pages + bound);
        return bound;
    }

    public static BrewRecipe find(ItemStack guide, String id) {
        for (BrewRecipe recipe : recipes(guide)) {
            if (recipe.id().equals(id)) return recipe;
        }
        return null;
    }

    public static boolean holds(ItemStack guide, BrewRecipe recipe) {
        for (BrewRecipe stored : recipes(guide)) {
            if (stored.id().equals(recipe.id()) || stored.matches(recipe)) return true;
        }
        return false;
    }

    public static String selected(ItemStack guide) {
        return guide.get(DataComponentRegistry.SELECTED_RECIPE.get());
    }

    public static BrewRecipe selectedRecipe(ItemStack guide) {
        String name = selected(guide);
        return name == null ? null : find(guide, name);
    }

    public static void select(ItemStack guide, String key) {
        if (key == null || find(guide, key) == null) {
            guide.remove(DataComponentRegistry.SELECTED_RECIPE.get());
        } else {
            guide.set(DataComponentRegistry.SELECTED_RECIPE.get(), key);
        }
    }

    public static void forget(ItemStack guide, String id) {
        int pages = pages(guide);
        List<BrewRecipe> stored = new ArrayList<>(recipes(guide));
        if (!stored.removeIf(recipe -> recipe.id().equals(id))) return;

        guide.set(DataComponentRegistry.GUIDE_PAGES.get(), pages);
        if (stored.isEmpty()) {
            guide.remove(DataComponentRegistry.BREW_RECIPES.get());
        } else {
            guide.set(DataComponentRegistry.BREW_RECIPES.get(), List.copyOf(stored));
        }

        if (id.equals(selected(guide))) {
            guide.remove(DataComponentRegistry.SELECTED_RECIPE.get());
        }
    }

    public static boolean rename(ItemStack guide, String id, String name) {
        List<BrewRecipe> stored = new ArrayList<>(recipes(guide));
        for (int index = 0; index < stored.size(); index++) {
            BrewRecipe recipe = stored.get(index);
            if (!recipe.id().equals(id)) continue;

            String trimmed = name.trim();
            if (trimmed.equals(recipe.name())) return false;
            stored.set(index, recipe.withName(trimmed));
            guide.set(DataComponentRegistry.BREW_RECIPES.get(), List.copyOf(stored));
            return true;
        }
        return false;
    }

    public static boolean annotate(ItemStack guide, String id, String notes) {
        List<BrewRecipe> stored = new ArrayList<>(recipes(guide));
        for (int index = 0; index < stored.size(); index++) {
            BrewRecipe recipe = stored.get(index);
            if (!recipe.id().equals(id)) continue;
            if (recipe.notes().equals(notes)) return false;
            stored.set(index, recipe.withNotes(notes));
            guide.set(DataComponentRegistry.BREW_RECIPES.get(), List.copyOf(stored));
            return true;
        }
        return false;
    }

    public static java.util.Map<String, String> ingredientNotes(ItemStack guide) {
        return guide.getOrDefault(DataComponentRegistry.INGREDIENT_NOTES.get(), java.util.Map.of());
    }

    public static boolean annotateIngredient(ItemStack guide, String ingredient, String notes) {
        java.util.Map<String, String> all = new java.util.TreeMap<>(ingredientNotes(guide));
        String trimmed = notes.strip();
        if (trimmed.equals(all.getOrDefault(ingredient, ""))) return false;
        if (trimmed.isEmpty()) {
            all.remove(ingredient);
        } else {
            all.put(ingredient, trimmed);
        }
        guide.set(DataComponentRegistry.INGREDIENT_NOTES.get(), java.util.Map.copyOf(all));
        return true;
    }

    public static Recorded write(ItemStack guide, BrewRecipe recipe) {
        if (holds(guide, recipe)) return Recorded.DUPLICATE;
        if (freePages(guide) <= 0) return Recorded.NO_PAGES;

        int pages = pages(guide);
        List<BrewRecipe> stored = new ArrayList<>(recipes(guide));
        stored.add(recipe);
        guide.set(DataComponentRegistry.GUIDE_PAGES.get(), pages);
        guide.set(DataComponentRegistry.BREW_RECIPES.get(), List.copyOf(stored));
        return Recorded.WRITTEN;
    }

    public static BrewRecipe brewed(BrewBase base, Collection<MobEffectInstance> effects,
                                    List<String> ingredients, List<String> steps, String brewName) {
        if (ingredients.isEmpty()) return null;

        List<BrewRecipe.BrewEffect> produced = new ArrayList<>();
        for (MobEffectInstance instance : effects) {
            PotionEnum recipe = RecipeDiscovery.byEffect(instance.getEffect());
            if (recipe != null) produced.add(new BrewRecipe.BrewEffect(recipe.name(), instance.getAmplifier() + 1));
        }
        if (produced.isEmpty()) return null;

        produced.sort(Comparator.comparing(BrewRecipe.BrewEffect::recipe));
        return BrewRecipe.create(brewName, base, List.copyOf(produced), List.copyOf(ingredients), List.copyOf(steps));
    }

    public static Recorded record(Iterable<ItemStack> guides, BrewRecipe brew) {
        if (brew == null) return Recorded.NONE;

        Recorded outcome = Recorded.NONE;
        for (ItemStack guide : guides) {
            Recorded result = write(guide, brew);
            if (result == Recorded.WRITTEN) {
                outcome = Recorded.WRITTEN;
            } else if (result == Recorded.NO_PAGES && outcome != Recorded.WRITTEN) {
                outcome = Recorded.NO_PAGES;
            } else if (outcome == Recorded.NONE) {
                outcome = result;
            }
        }
        return outcome;
    }
}
