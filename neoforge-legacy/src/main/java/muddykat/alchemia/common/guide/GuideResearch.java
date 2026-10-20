package muddykat.alchemia.common.guide;

import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.potion.BrewRecipe;
import muddykat.alchemia.common.potion.BrewRecipeBook;
import muddykat.alchemia.common.potion.IngredientDiscovery;
import muddykat.alchemia.common.potion.RecipeDiscovery;
import muddykat.alchemia.common.potion.WorldEffects;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class GuideResearch {

    public static final int THIN = 0;
    public static final int NORMAL = 1;
    public static final int THICK = 2;
    public static final int BURSTING = 3;
    public static final int COMPLETE = 4;
    public static final int MAX_THICKNESS = COMPLETE;
    public static final int MAX_TABS = 4;

    private static final int[] TAB_STEPS = {1, 5, 12, 24};

    public record Shared(int discoveries, int recipes, int skipped) {
        public Shared plus(Shared other) {
            return new Shared(discoveries + other.discoveries, recipes + other.recipes, skipped + other.skipped);
        }

        public boolean any() {
            return discoveries > 0 || recipes > 0;
        }
    }

    private GuideResearch() {
    }

    public static Shared exchange(ItemStack a, ItemStack b) {
        return copy(b, a).plus(copy(a, b));
    }

    public static Shared copy(ItemStack from, ItemStack into) {
        int before = discoveries(into);
        RecipeDiscovery.learn(List.of(into), RecipeDiscovery.stored(from));
        IngredientDiscovery.learnKeys(into, IngredientDiscovery.stored(from));
        WorldEffects.learnNotes(into, WorldEffects.notesIn(from));
        GreatWork.learn(into, GreatWork.stored(from));
        int learned = discoveries(into) - before;
        java.util.Map<String, String> theirs = BrewRecipeBook.ingredientNotes(from);
        java.util.Map<String, String> ours = BrewRecipeBook.ingredientNotes(into);
        for (java.util.Map.Entry<String, String> note : theirs.entrySet()) {
            if (!ours.containsKey(note.getKey()) && BrewRecipeBook.annotateIngredient(into, note.getKey(), note.getValue())) learned++;
        }

        int written = 0;
        int skipped = 0;
        for (BrewRecipe recipe : BrewRecipeBook.recipes(from)) {
            switch (BrewRecipeBook.write(into, recipe)) {
                case WRITTEN -> written++;
                case NO_PAGES -> skipped++;
                default -> {
                }
            }
        }
        return new Shared(learned, written, skipped);
    }

    private static int discoveries(ItemStack guide) {
        return RecipeDiscovery.stored(guide).size() + IngredientDiscovery.stored(guide).size()
                + WorldEffects.notesIn(guide).size() + GreatWork.stored(guide).size();
    }

    public static int secrets(ItemStack guide) {
        return RecipeDiscovery.knownTo(guide).size() + WorldEffects.notesIn(guide).size();
    }

    public static int completion(ItemStack guide) {
        int total = RecipeDiscovery.total() + IngredientDiscovery.totalAll() + GreatWork.total();
        int found = RecipeDiscovery.knownTo(guide).size() + IngredientDiscovery.found(guide) + GreatWork.known(guide);
        return total == 0 ? 0 : found * 100 / total;
    }

    public static int thickness(ItemStack guide) {
        int percent = completion(guide);
        if (percent >= 100) return COMPLETE;
        if (percent >= 76) return BURSTING;
        if (percent >= 43) return THICK;
        if (percent >= 16) return NORMAL;
        return THIN;
    }

    public static int tabs(ItemStack guide) {
        return step(secrets(guide), TAB_STEPS);
    }

    private static int step(int value, int[] steps) {
        int reached = 0;
        for (int threshold : steps) {
            if (value >= threshold) reached++;
        }
        return reached;
    }

    public static boolean linksEffect(ItemStack guide, String name) {
        for (BrewRecipe recipe : BrewRecipeBook.recipes(guide)) {
            for (BrewRecipe.BrewEffect effect : recipe.effects()) {
                if (effect.recipe().equals(name)) return true;
            }
        }
        return false;
    }

    public static boolean linksReagent(ItemStack guide, String key) {
        net.minecraft.world.item.Item reagent = IngredientDiscovery.reagentByKey(key);
        if (reagent == null) return false;
        for (BrewRecipe recipe : BrewRecipeBook.recipes(guide)) {
            for (ItemStack used : recipe.resolve()) {
                if (used.getItem() == reagent) return true;
            }
            if (reagent == net.minecraft.world.item.Items.REDSTONE && recipe.steps().contains(String.valueOf(BrewRecipe.STEP_REDSTONE))) return true;
            if (reagent == net.minecraft.world.item.Items.GUNPOWDER && recipe.steps().contains(String.valueOf(BrewRecipe.STEP_SPLASH))) return true;
            if (reagent == net.minecraft.world.item.Items.GLOWSTONE_DUST && recipe.steps().contains(String.valueOf(BrewRecipe.STEP_GLOW))) return true;
            for (muddykat.alchemia.common.potion.Imbuements.Attuner attuner : muddykat.alchemia.common.potion.Imbuements.of(recipe)) {
                if (attuner.item() == reagent) return true;
            }
        }
        return false;
    }

    public static boolean linksIngredient(ItemStack guide, String name) {
        for (BrewRecipe recipe : BrewRecipeBook.recipes(guide)) {
            for (String entry : recipe.ingredients()) {
                ItemStack stack = BrewRecipe.stackFor(entry);
                if (stack.getItem() instanceof ItemIngredient item && item.getIngredient().name().equals(name)) return true;
            }
        }
        return false;
    }
}
