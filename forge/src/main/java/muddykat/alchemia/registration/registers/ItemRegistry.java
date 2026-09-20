package muddykat.alchemia.registration.registers;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.items.ItemAlchemiaGuide;
import muddykat.alchemia.common.items.ItemGeneric;
import muddykat.alchemia.common.items.ItemMortarPestle;
import muddykat.alchemia.common.items.ItemAlbedo;
import muddykat.alchemia.common.items.ItemCitrinitas;
import muddykat.alchemia.common.items.ItemNigredo;
import muddykat.alchemia.common.items.ItemRubedo;
import muddykat.alchemia.common.items.ItemPhilosopherStone;
import muddykat.alchemia.common.items.helper.Ingredients;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Alchemia.MODID);

    public static final Map<String, RegistryObject<Item>> ITEM_REGISTRY = new LinkedHashMap<>();

    public static RegistryObject<Item> registerItem(String registry_name, Function<Item.Properties, ? extends Item> factory) {
        RegistryObject<Item> item = ITEMS.register(registry_name, () -> factory.apply(new Item.Properties()));
        ITEM_REGISTRY.put(registry_name, item);
        return item;
    }

    public static DeferredRegister<Item> getRegistry() {
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

        registerItem("nigredo", ItemNigredo::new);
        registerItem("albedo", ItemAlbedo::new);
        registerItem("citrinitas", ItemCitrinitas::new);
        registerItem("rubedo", ItemRubedo::new);
        registerItem("philosopher_stone", ItemPhilosopherStone::new);
    }
}
