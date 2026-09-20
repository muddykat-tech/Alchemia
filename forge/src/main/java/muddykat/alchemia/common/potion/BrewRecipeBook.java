package muddykat.alchemia.common.potion;

import muddykat.alchemia.common.items.ItemAlchemiaGuide;
import muddykat.alchemia.common.utility.StackData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BrewRecipeBook {

    public static List<BrewRecipe> recipes(ItemStack guide) {
        return StackData.BREW_RECIPES.getOrDefault(guide, List.of());
    }

    public static BrewRecipe find(ItemStack guide, String id) {
        for (BrewRecipe recipe : recipes(guide)) {
            if (recipe.id().equals(id)) return recipe;
        }
        return null;
    }

    public static String selected(ItemStack guide) {
        return StackData.SELECTED_RECIPE.get(guide);
    }

    public static BrewRecipe selectedRecipe(ItemStack guide) {
        String name = selected(guide);
        return name == null ? null : find(guide, name);
    }

    public static void select(ItemStack guide, String key) {
        if (key == null || find(guide, key) == null) {
            StackData.SELECTED_RECIPE.remove(guide);
        } else {
            StackData.SELECTED_RECIPE.set(guide, key);
        }
    }

    public static void forget(ItemStack guide, String id) {
        List<BrewRecipe> stored = new ArrayList<>(recipes(guide));
        if (!stored.removeIf(recipe -> recipe.id().equals(id))) return;

        if (stored.isEmpty()) {
            StackData.BREW_RECIPES.remove(guide);
        } else {
            StackData.BREW_RECIPES.set(guide, List.copyOf(stored));
        }

        if (id.equals(selected(guide))) {
            StackData.SELECTED_RECIPE.remove(guide);
        }
    }

    public static int record(Player player, BrewBase base, Collection<MobEffectInstance> effects, List<String> ingredients, String brewName) {
        if (ingredients.isEmpty()) return 0;

        List<BrewRecipe.BrewEffect> produced = new ArrayList<>();
        for (MobEffectInstance instance : effects) {
            PotionEnum recipe = RecipeDiscovery.byEffect(instance.getEffect());
            if (recipe != null) produced.add(new BrewRecipe.BrewEffect(recipe.name(), instance.getAmplifier() + 1));
        }
        if (produced.isEmpty()) return 0;

        produced.sort(java.util.Comparator.comparing(BrewRecipe.BrewEffect::recipe));

        BrewRecipe brew = BrewRecipe.create(brewName, base, List.copyOf(produced), List.copyOf(ingredients));

        int recorded = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (!(stack.getItem() instanceof ItemAlchemiaGuide)) continue;

            List<BrewRecipe> stored = new ArrayList<>(recipes(stack));
            if (stored.stream().anyMatch(brew::matches)) continue;

            stored.add(brew);
            StackData.BREW_RECIPES.set(stack, List.copyOf(stored));
            recorded++;
        }
        return recorded;
    }
}
