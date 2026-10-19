package muddykat.alchemia.common.potion;

import org.jspecify.annotations.Nullable;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.HashSet;
import muddykat.alchemia.common.items.ItemAlchemiaGuide;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.registration.registers.DataComponentRegistry;
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
        return record(RecipeDiscovery.guidesOf(player), used);
    }

    public static int record(Iterable<ItemStack> guides, ItemStack used) {
        String key = keyOf(used);
        if (key == null) return 0;

        int discovered = 0;
        for (ItemStack guide : guides) discovered = Math.max(discovered, learnKeys(guide, List.of(key)));
        return discovered;
    }

    public static int survey(Player player, ItemStack guide) {
        Set<String> handled = new HashSet<>();
        Inventory inventory = player.getInventory();

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            String key = keyOf(inventory.getItem(slot));
            if (key != null) handled.add(key);
        }

        if (player instanceof ServerPlayer server) {
            List<String> known = stored(guide);
            for (Ingredients ingredient : Ingredients.values()) {
                if (known.contains(ingredient.name()) || handled.contains(ingredient.name())) continue;

                Item item = ItemRegistry.getItemFromRegistry(ingredient.getRegistryName()).asItem();
                if (server.getStats().getValue(Stats.ITEM_PICKED_UP.get(item)) > 0) handled.add(ingredient.name());
            }
            List<Item> everything = new ArrayList<>(VanillaIngredients.reagents());
            everything.addAll(Imbuements.items());
            for (Item reagent : everything) {
                String key = reagentKey(reagent);
                if (known.contains(key) || handled.contains(key)) continue;
                if (server.getStats().getValue(Stats.ITEM_PICKED_UP.get(reagent)) > 0) handled.add(key);
            }
        }
        return learnKeys(guide, handled);
    }

    public static int learn(ItemStack guide, Collection<Ingredients> handled) {
        List<String> keys = new ArrayList<>();
        for (Ingredients ingredient : handled) keys.add(ingredient.name());
        return learnKeys(guide, keys);
    }

    public static int learnKeys(ItemStack guide, Collection<String> keys) {
        List<String> names = new ArrayList<>(stored(guide));
        int before = names.size();

        for (String key : keys) {
            if (validKey(key) && !names.contains(key)) names.add(key);
        }
        if (names.size() == before) return 0;

        names.sort(null);
        guide.set(DataComponentRegistry.DISCOVERED_INGREDIENTS.get(), List.copyOf(names));
        return names.size() - before;
    }

    public static @Nullable String keyOf(ItemStack stack) {
        if (stack.getItem() instanceof ItemIngredient item) return item.getIngredient().name();
        if (isReagent(stack)) return reagentKey(stack.getItem());
        return null;
    }

    public static String reagentKey(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    public static @Nullable Item reagentByKey(String key) {
        Identifier id = Identifier.tryParse(key);
        if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) return null;
        Item item = BuiltInRegistries.ITEM.getValue(id);
        return isReagent(new ItemStack(item)) ? item : null;
    }

    public static boolean isReagent(ItemStack stack) {
        return VanillaIngredients.isReagent(stack) || Imbuements.isAttuner(stack);
    }

    public static List<Item> modifiers() {
        List<Item> all = new ArrayList<>(VanillaIngredients.MODIFIERS);
        all.addAll(Imbuements.items());
        return all;
    }

    public static boolean validKey(String key) {
        return byName(key) != null || reagentByKey(key) != null;
    }

    public static boolean knows(ItemStack guide, String key) {
        return stored(guide).contains(key);
    }

    public static Set<Item> knownReagents(ItemStack guide) {
        Set<Item> known = new HashSet<>();
        for (String key : stored(guide)) {
            Item item = reagentByKey(key);
            if (item != null) known.add(item);
        }
        return known;
    }

    public static int found(ItemStack guide) {
        return knownTo(guide).size() + knownReagents(guide).size();
    }

    public static int totalAll() {
        return total() + VanillaIngredients.reagents().size() + Imbuements.items().size();
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
