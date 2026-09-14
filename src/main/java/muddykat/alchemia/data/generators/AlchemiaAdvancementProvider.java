package muddykat.alchemia.data.generators;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.data.AlchemiaItemTags;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.common.data.ForgeAdvancementProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AlchemiaAdvancementProvider implements ForgeAdvancementProvider.AdvancementGenerator {

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<Advancement> output, ExistingFileHelper existingFileHelper) {
        Item guide = ItemRegistry.getItemFromRegistry("alchemia_guide").asItem();
        Item mortar = ItemRegistry.getItemFromRegistry("mortar_and_pestle").asItem();
        Item cauldron = ItemRegistry.getItemFromRegistry("alchemical_cauldron").asItem();

        Advancement root = Advancement.Builder.advancement()
                .display(guide, title("root"), description("root"),
                        new ResourceLocation("block/dark_oak_log"),
                        FrameType.TASK, true, true, false)
                .addCriterion("has_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(anyIngredient().build()))
                .save(output, id("root"));

        Advancement book = child(output, root, guide, "guide", FrameType.TASK,
                InventoryChangeTrigger.TriggerInstance.hasItems(guide));

        Advancement pestle = child(output, book, mortar, "mortar", FrameType.TASK,
                InventoryChangeTrigger.TriggerInstance.hasItems(mortar));

        Advancement pot = child(output, book, cauldron, "cauldron", FrameType.TASK,
                InventoryChangeTrigger.TriggerInstance.hasItems(cauldron));

        Item[] crystals = crystals();

        Advancement crystal = child(output, pot, crystals[0], "crystal", FrameType.TASK,
                InventoryChangeTrigger.TriggerInstance.hasItems(crystals));

        Advancement.Builder spectrum = Advancement.Builder.advancement()
                .parent(crystal)
                .display(crystals[crystals.length - 1], title("spectrum"), description("spectrum"), null,
                        FrameType.GOAL, true, true, false);
        for (Item item : crystals) {
            spectrum.addCriterion(key(item), InventoryChangeTrigger.TriggerInstance.hasItems(item));
        }
        spectrum.requirements(RequirementsStrategy.AND).save(output, id("spectrum"));

        Advancement stage = pot;
        String[] opus = {"nigredo", "albedo", "citrinitas", "rubedo"};
        for (String name : opus) {
            Item item = ItemRegistry.getItemFromRegistry(name).asItem();
            stage = child(output, stage, item, name, FrameType.TASK,
                    InventoryChangeTrigger.TriggerInstance.hasItems(item));
        }

        Item philosopherStone = ItemRegistry.getItemFromRegistry("philosopher_stone").asItem();
        Advancement.Builder.advancement()
                .parent(stage)
                .display(philosopherStone, title("philosopher_stone"), description("philosopher_stone"), null,
                        FrameType.CHALLENGE, true, true, false)
                .addCriterion("unlocked", InventoryChangeTrigger.TriggerInstance.hasItems(philosopherStone))
                .save(output, id("philosopher_stone"));

        Item[] everything = allIngredients();
        Advancement.Builder herbalist = Advancement.Builder.advancement()
                .parent(pestle)
                .display(everything[0], title("herbalist"), description("herbalist"), null,
                        FrameType.CHALLENGE, true, true, false);
        for (Item item : everything) {
            herbalist.addCriterion(key(item), InventoryChangeTrigger.TriggerInstance.hasItems(item));
        }
        herbalist.requirements(RequirementsStrategy.AND).save(output, id("herbalist"));
    }

    private Advancement child(Consumer<Advancement> output, Advancement parent, ItemLike icon,
                              String name, FrameType type, CriterionTriggerInstance criterion) {
        return Advancement.Builder.advancement()
                .parent(parent)
                .display(icon, title(name), description(name), null, type, true, true, false)
                .addCriterion("unlocked", criterion)
                .save(output, id(name));
    }

    private static ItemPredicate.Builder anyIngredient() {
        return ItemPredicate.Builder.item().of(AlchemiaItemTags.INGREDIENTS);
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

    private static String key(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getPath();
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
