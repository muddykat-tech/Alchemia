package muddykat.alchemia.common.potion;

import muddykat.alchemia.common.items.ItemAlchemiaGuide;
import muddykat.alchemia.registration.registers.DataComponentRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BrewRecipeBook {

    public static List<BrewRecipe> recipes(ItemStack guide) {
        return guide.getOrDefault(DataComponentRegistry.BREW_RECIPES.get(), List.of());
    }

    public static BrewRecipe find(ItemStack guide, String id) {
        for (BrewRecipe recipe : recipes(guide)) {
            if (recipe.id().equals(id)) return recipe;
        }
        return null;
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
        List<BrewRecipe> stored = new ArrayList<>(recipes(guide));
        if (!stored.removeIf(recipe -> recipe.id().equals(id))) return;

        if (stored.isEmpty()) {
            guide.remove(DataComponentRegistry.BREW_RECIPES.get());
        } else {
            guide.set(DataComponentRegistry.BREW_RECIPES.get(), List.copyOf(stored));
        }

        if (id.equals(selected(guide))) {
            guide.remove(DataComponentRegistry.SELECTED_RECIPE.get());
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

        int recorded = 0;
        for (ItemStack stack : player.getInventory()) {
            if (!(stack.getItem() instanceof ItemAlchemiaGuide)) continue;

            List<BrewRecipe> stored = new ArrayList<>(recipes(stack));
            stored.add(BrewRecipe.create(brewName, base, List.copyOf(produced), List.copyOf(ingredients)));
            stack.set(DataComponentRegistry.BREW_RECIPES.get(), List.copyOf(stored));
            recorded++;
        }
        return recorded;
    }
}
