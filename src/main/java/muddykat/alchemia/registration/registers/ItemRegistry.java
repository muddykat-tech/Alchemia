package muddykat.alchemia.registration.registers;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.items.ItemAlchemiaGuide;
import muddykat.alchemia.common.items.ItemMortarPestle;
import muddykat.alchemia.common.items.helper.Ingredients;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class ItemRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Alchemia.MODID);

    public static final Map<String, DeferredItem<Item>> ITEM_REGISTRY = new LinkedHashMap<>();

    public static DeferredItem<Item> registerItem(String registry_name, Function<Item.Properties, ? extends Item> factory) {
        DeferredItem<Item> item = ITEMS.registerItem(registry_name, factory);
        ITEM_REGISTRY.put(registry_name, item);
        return item;
    }

    public static DeferredRegister.Items getRegistry() {
        return ITEMS;
    }

    public static ItemLike getSeedByIngredient(Ingredients ingredient) {
        return ITEM_REGISTRY.get(ingredient.getSeedRegistryName()).get();
    }

    public static ItemLike getItemFromRegistry(String reg_name) {
        return ITEM_REGISTRY.get(reg_name).get();
    }

    public static void initialize() {
        for (Ingredients ingredient : Ingredients.values()) {
            ingredient.register();
        }

        registerItem("alchemia_guide", ItemAlchemiaGuide::new);
        registerItem("mortar_and_pestle", ItemMortarPestle::new);
    }
}
