package muddykat.alchemia.data.generators;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.data.AlchemiaItemTags;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AlchemiaAdvancementProvider implements AdvancementSubProvider {

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> output) {
        Item guide = ItemRegistry.getItemFromRegistry("alchemia_guide").asItem();
        Item mortar = ItemRegistry.getItemFromRegistry("mortar_and_pestle").asItem();
        Item cauldron = ItemRegistry.getItemFromRegistry("alchemical_cauldron").asItem();

        AdvancementHolder root = Advancement.Builder.advancement()
                .display(guide, title("root"), description("root"),
                        Identifier.withDefaultNamespace("block/dark_oak_log"),
                        AdvancementType.TASK, true, true, false)
                .addCriterion("has_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(anyIngredient(registries)))
                .save(output, id("root"));

        AdvancementHolder book = child(output, root, guide, "guide", AdvancementType.TASK,
                InventoryChangeTrigger.TriggerInstance.hasItems(guide));

        AdvancementHolder pestle = child(output, book, mortar, "mortar", AdvancementType.TASK,
                InventoryChangeTrigger.TriggerInstance.hasItems(mortar));

        AdvancementHolder pot = child(output, book, cauldron, "cauldron", AdvancementType.TASK,
                InventoryChangeTrigger.TriggerInstance.hasItems(cauldron));

        Item[] crystals = crystals();

        AdvancementHolder crystal = child(output, pot, crystals[0], "crystal", AdvancementType.TASK,
                InventoryChangeTrigger.TriggerInstance.hasItems(crystals));

        Advancement.Builder spectrum = Advancement.Builder.advancement()
                .parent(crystal)
                .display(crystals[crystals.length - 1], title("spectrum"), description("spectrum"), null,
                        AdvancementType.GOAL, true, true, false);
        for (Item item : crystals) {
            spectrum.addCriterion(key(item), InventoryChangeTrigger.TriggerInstance.hasItems(item));
        }
        spectrum.requirements(AdvancementRequirements.allOf(names(crystals))).save(output, id("spectrum"));

        AdvancementHolder stage = pot;
        String[] opus = {"nigredo", "albedo", "citrinitas", "rubedo"};
        for (String name : opus) {
            Item item = ItemRegistry.getItemFromRegistry(name).asItem();
            stage = child(output, stage, item, name, AdvancementType.TASK,
                    InventoryChangeTrigger.TriggerInstance.hasItems(item));
        }

        Item philosopherStone = ItemRegistry.getItemFromRegistry("philosopher_stone").asItem();
        Advancement.Builder.advancement()
                .parent(stage)
                .display(philosopherStone, title("philosopher_stone"), description("philosopher_stone"), null,
                        AdvancementType.CHALLENGE, true, true, false)
                .addCriterion("unlocked", InventoryChangeTrigger.TriggerInstance.hasItems(philosopherStone))
                .save(output, id("philosopher_stone"));

        Item[] everything = allIngredients();
        Advancement.Builder herbalist = Advancement.Builder.advancement()
                .parent(pestle)
                .display(everything[0], title("herbalist"), description("herbalist"), null,
                        AdvancementType.CHALLENGE, true, true, false);
        for (Item item : everything) {
            herbalist.addCriterion(key(item), InventoryChangeTrigger.TriggerInstance.hasItems(item));
        }
        herbalist.requirements(AdvancementRequirements.allOf(names(everything))).save(output, id("herbalist"));
    }

    private AdvancementHolder child(Consumer<AdvancementHolder> output, AdvancementHolder parent, ItemLike icon,
                                    String name, AdvancementType type,
                                    net.minecraft.advancements.triggers.Criterion<?> criterion) {
        return Advancement.Builder.advancement()
                .parent(parent)
                .display(icon, title(name), description(name), null, type, true, true, false)
                .addCriterion("unlocked", criterion)
                .save(output, id(name));
    }

    private static net.minecraft.advancements.predicates.ItemPredicate.Builder anyIngredient(HolderLookup.Provider registries) {
        return net.minecraft.advancements.predicates.ItemPredicate.Builder.item()
                .of(registries.lookupOrThrow(net.minecraft.core.registries.Registries.ITEM), AlchemiaItemTags.INGREDIENTS);
    }

    private static Item[] crystals() {
        List<Item> items = new ArrayList<>();
        for (Ingredients ingredient : Ingredients.values()) {
            if (ingredient.getType() == IngredientType.Mineral) {
                items.add(ItemRegistry.getItemFromRegistry(ingredient.getRegistryName()).asItem());
            }
        }
        return items.toArray(Item[]::new);
    }

    private static Item[] allIngredients() {
        List<Item> items = new ArrayList<>();
        for (Ingredients ingredient : Ingredients.values()) {
            items.add(ItemRegistry.getItemFromRegistry(ingredient.getRegistryName()).asItem());
        }
        return items.toArray(Item[]::new);
    }

    private static List<String> names(Item[] items) {
        List<String> keys = new ArrayList<>();
        for (Item item : items) keys.add(key(item));
        return keys;
    }

    private static String key(Item item) {
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getPath();
    }

    private static Component title(String name) {
        return Component.translatable("advancements.alchemia." + name + ".title");
    }

    private static Component description(String name) {
        return Component.translatable("advancements.alchemia." + name + ".description");
    }

    private static String id(String name) {
        return Alchemia.MODID + ":" + name;
    }
}
