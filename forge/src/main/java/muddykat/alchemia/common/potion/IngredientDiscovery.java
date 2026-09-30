package muddykat.alchemia.common.potion;

import muddykat.alchemia.common.items.ItemAlchemiaGuide;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.utility.StackData;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class IngredientDiscovery {

    public static List<String> stored(ItemStack guide) {
        return StackData.DISCOVERED_INGREDIENTS.getOrDefault(guide, List.of());
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

        Set<Ingredients> handled = EnumSet.of(item.getIngredient());
        Inventory inventory = player.getInventory();
        int discovered = 0;

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.getItem() instanceof ItemAlchemiaGuide) discovered = Math.max(discovered, learn(stack, handled));
        }
        return discovered;
    }

    public static int survey(Player player, ItemStack guide) {
        Set<Ingredients> handled = EnumSet.noneOf(Ingredients.class);
        Inventory inventory = player.getInventory();

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).getItem() instanceof ItemIngredient item) handled.add(item.getIngredient());
        }

        if (player instanceof ServerPlayer server) {
            Set<Ingredients> known = knownTo(guide);
            for (Ingredients ingredient : Ingredients.values()) {
                if (known.contains(ingredient) || handled.contains(ingredient)) continue;

                Item item = ItemRegistry.getItemFromRegistry(ingredient.getRegistryName()).asItem();
                if (server.getStats().getValue(Stats.ITEM_PICKED_UP.get(item)) > 0) handled.add(ingredient);
            }
        }
        return learn(guide, handled);
    }

    public static int learn(ItemStack guide, Collection<Ingredients> handled) {
        List<String> names = new ArrayList<>(stored(guide));
        int before = names.size();

        for (Ingredients ingredient : handled) {
            if (!names.contains(ingredient.name())) names.add(ingredient.name());
        }
        if (names.size() == before) return 0;

        names.sort(null);
        StackData.DISCOVERED_INGREDIENTS.set(guide, List.copyOf(names));
        return names.size() - before;
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
