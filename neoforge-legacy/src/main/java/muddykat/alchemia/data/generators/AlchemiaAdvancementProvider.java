package muddykat.alchemia.data.generators;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.blocks.helper.MachineOwner;
import muddykat.alchemia.common.blocks.helper.MachineRepair;
import muddykat.alchemia.common.guide.GuideMastery;
import muddykat.alchemia.common.items.ItemIngredientSeed;
import muddykat.alchemia.common.blocks.BlockInfectedLog;
import muddykat.alchemia.registration.registers.BlockRegistry;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.data.AlchemiaItemTags;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AlchemiaAdvancementProvider implements AdvancementSubProvider {

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> output) {
        net.minecraft.core.HolderGetter<Item> itemLookup = registries.lookupOrThrow(net.minecraft.core.registries.Registries.ITEM);
        Item guide = ItemRegistry.getItemFromRegistry("alchemia_guide").asItem();
        Item mortar = ItemRegistry.getItemFromRegistry("mortar_and_pestle").asItem();
        Item cauldron = ItemRegistry.getItemFromRegistry("alchemical_cauldron").asItem();

        AdvancementHolder root = Advancement.Builder.advancement()
                .display(guide, title("root"), description("root"),
                        ResourceLocation.withDefaultNamespace("textures/block/dark_oak_log.png"),
                        AdvancementType.TASK, true, true, false)
                .addCriterion("has_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(anyIngredient(itemLookup)))
                .save(output, id("root"));

        AdvancementHolder book = child(output, root, guide, "guide", AdvancementType.TASK,
                InventoryChangeTrigger.TriggerInstance.hasItems(guide));

        AdvancementHolder pestle = child(output, book, mortar, "mortar", AdvancementType.TASK,
                InventoryChangeTrigger.TriggerInstance.hasItems(mortar));

        AdvancementHolder pot = child(output, book, cauldron, "cauldron", AdvancementType.TASK,
                InventoryChangeTrigger.TriggerInstance.hasItems(cauldron));

        Item[] crystals = crystals();

        Advancement.Builder anyCrystal = Advancement.Builder.advancement()
                .parent(pot)
                .display(crystals[0], title("crystal"), description("crystal"), null, AdvancementType.TASK, true, true, false);
        for (Item item : crystals) {
            anyCrystal.addCriterion(key(item), InventoryChangeTrigger.TriggerInstance.hasItems(item));
        }
        AdvancementHolder crystal = anyCrystal.requirements(AdvancementRequirements.anyOf(names(crystals))).save(output, id("crystal"));

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
        List<List<String>> planted = new ArrayList<>();
        for (var holder : ItemRegistry.ITEM_REGISTRY.values()) {
            if (!(holder.get() instanceof ItemIngredientSeed seed)) continue;
            String name = seed.getIngredient().getRegistryName();
            List<String> ways = new ArrayList<>(List.of(name));
            herbalist.addCriterion(name, planted(seed.getBlock()));
            for (var block : BlockRegistry.BLOCK_REGISTRY.values()) {
                if (!(block.get() instanceof BlockInfectedLog log) || log.getFungus() != seed.getIngredient()) continue;
                String infected = name + "_" + net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(log).getPath();
                herbalist.addCriterion(infected, planted(log));
                ways.add(infected);
            }
            planted.add(ways);
        }
        herbalist.requirements(new AdvancementRequirements(planted)).save(output, id("herbalist"));

        Item tablet = ItemRegistry.getItemFromRegistry("alchemy_tablet").asItem();
        Item notes = ItemRegistry.getItemFromRegistry("alchemist_notes").asItem();
        Item rosemary = ItemRegistry.getItemFromRegistry(Ingredients.Druids_Rosemary.getRegistryName()).asItem();
        AdvancementHolder effects = awarded(output, book, net.minecraft.world.item.Items.GLOWSTONE_DUST, "all_effects", AdvancementType.GOAL);
        AdvancementHolder secrets = awarded(output, book, notes, "all_secrets", AdvancementType.GOAL);
        AdvancementHolder lore = awarded(output, book, rosemary, "all_ingredients", AdvancementType.GOAL);
        AdvancementHolder restored = awarded(output, book, net.minecraft.world.item.Items.WRITABLE_BOOK, "forgotten_shack", AdvancementType.GOAL);
        for (MachineOwner owner : MachineRepair.ORDER) {
            restored = awarded(output, restored, ItemRegistry.getItemFromRegistry(owner.registryName()),
                    GuideMastery.restoredName(owner), owner == MachineOwner.TOP ? AdvancementType.GOAL : AdvancementType.TASK);
        }
        AdvancementHolder work = awarded(output, book, tablet, "all_stages", AdvancementType.GOAL);
        awarded(output, work, guide, "complete_guide", AdvancementType.CHALLENGE);
    }

    private AdvancementHolder awarded(Consumer<AdvancementHolder> output, AdvancementHolder parent,
                                      ItemLike icon, String name, AdvancementType type) {
        var criterion = net.minecraft.advancements.CriteriaTriggers.IMPOSSIBLE.createCriterion(
                new net.minecraft.advancements.critereon.ImpossibleTrigger.TriggerInstance());
        return child(output, parent, icon, name, type, criterion);
    }

    private AdvancementHolder child(Consumer<AdvancementHolder> output, AdvancementHolder parent, ItemLike icon,
                                    String name, AdvancementType type,
                                    net.minecraft.advancements.Criterion<?> criterion) {
        return Advancement.Builder.advancement()
                .parent(parent)
                .display(icon, title(name), description(name), null, type, true, true, false)
                .addCriterion("unlocked", criterion)
                .save(output, id(name));
    }

    private static net.minecraft.advancements.critereon.ItemPredicate.Builder anyIngredient(net.minecraft.core.HolderGetter<Item> items) {
        return net.minecraft.advancements.critereon.ItemPredicate.Builder.item()
                .of(AlchemiaItemTags.INGREDIENTS);
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

    private static net.minecraft.advancements.Criterion<?> planted(net.minecraft.world.level.block.Block block) {
        return net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(block);
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
