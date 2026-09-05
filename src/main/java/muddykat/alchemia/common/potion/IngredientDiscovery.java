package muddykat.alchemia.common.potion;

import muddykat.alchemia.common.items.ItemAlchemiaGuide;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.registration.registers.DataComponentRegistry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class IngredientDiscovery {

    public static List<String> stored(ItemStack guide) {
        return guide.getOrDefault(DataComponentRegistry.DISCOVERED_INGREDIENTS.get(), List.of());
    }

    public static Set<Ingredients> knownTo(ItemStack guide) {
        Set<Ingredients> known = EnumSet.noneOf(Ingredients.class);
        for (String name : stored(guide)) {
            Ingredients ingredient = byName(name);
            if (ingredient != null) known.add(ingredient);
        }
        return known;
    }

    public static int record(Player player, ItemStack used) {
        if (!(used.getItem() instanceof ItemIngredient item)) return 0;

        Ingredients ingredient = item.getIngredient();
        int discovered = 0;

        for (ItemStack stack : player.getInventory()) {
            if (!(stack.getItem() instanceof ItemAlchemiaGuide)) continue;

            List<String> names = new ArrayList<>(stored(stack));
            if (names.contains(ingredient.name())) continue;

            names.add(ingredient.name());
            names.sort(null);
            stack.set(DataComponentRegistry.DISCOVERED_INGREDIENTS.get(), List.copyOf(names));
            discovered = 1;
        }
        return discovered;
    }

    public static Ingredients byName(String name) {
        for (Ingredients ingredient : Ingredients.values()) {
            if (ingredient.name().equals(name)) return ingredient;
        }
        return null;
    }

    public static int total() {
        return Ingredients.values().length;
    }
}
