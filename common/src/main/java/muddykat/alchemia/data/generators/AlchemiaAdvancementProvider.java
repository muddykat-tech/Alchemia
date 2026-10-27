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
#if MC_VER >= 26200
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
#else
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
#endif
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

#if MC_VER >= 26300
public class AlchemiaAdvancementProvider extends AdvancementSubProvider {

    public AlchemiaAdvancementProvider(net.minecraft.data.worldgen.BootstrapContext<Advancement> output) {
        super(output);
    }

    @Override
    public void generate() {
        generate(this.output, this.output.lookup(net.minecraft.core.registries.Registries.ITEM));
    }

    private void generate(net.minecraft.data.worldgen.BootstrapContext<Advancement> output,
                          net.minecraft.core.HolderGetter<Item> itemLookup) {
#else
public class AlchemiaAdvancementProvider implements AdvancementSubProvider {

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> output) {
        net.minecraft.core.HolderGetter<Item> itemLookup = registries.lookupOrThrow(net.minecraft.core.registries.Registries.ITEM);
#endif
        Item guide = ItemRegistry.getItemFromRegistry("alchemia_guide").asItem();
        Item mortar = ItemRegistry.getItemFromRegistry("mortar_and_pestle").asItem();
        Item cauldron = ItemRegistry.getItemFromRegistry("alchemical_cauldron").asItem();

        AdvancementHolder root = Advancement.Builder.advancement()
#if MC_VER >= 26300
                .rootDisplay(guide, title("root"), description("root"),
                        Identifier.withDefaultNamespace("block/dark_oak_log"),
                        AdvancementType.TASK, true, true, false)
#else
                .display(guide, title("root"), description("root"),
                        Identifier.withDefaultNamespace("block/dark_oak_log"),
                        AdvancementType.TASK, true, true, false)
#endif
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
#if MC_VER >= 26300
                .display(crystals[0], title("crystal"), description("crystal"), AdvancementType.TASK, true, true, false);
#else
                .display(crystals[0], title("crystal"), description("crystal"), null, AdvancementType.TASK, true, true, false);
#endif
        for (Item item : crystals) {
            anyCrystal.addCriterion(key(item), InventoryChangeTrigger.TriggerInstance.hasItems(item));
        }
        AdvancementHolder crystal = anyCrystal.requirements(AdvancementRequirements.anyOf(names(crystals))).save(output, id("crystal"));

        Advancement.Builder spectrum = Advancement.Builder.advancement()
                .parent(crystal)
#if MC_VER >= 26300
                .display(crystals[crystals.length - 1], title("spectrum"), description("spectrum"),
                        AdvancementType.GOAL, true, true, false);
#else
                .display(crystals[crystals.length - 1], title("spectrum"), description("spectrum"), null,
                        AdvancementType.GOAL, true, true, false);
#endif
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
#if MC_VER >= 26300
                .display(philosopherStone, title("philosopher_stone"), description("philosopher_stone"),
                        AdvancementType.CHALLENGE, true, true, false)
#else
                .display(philosopherStone, title("philosopher_stone"), description("philosopher_stone"), null,
                        AdvancementType.CHALLENGE, true, true, false)
#endif
                .addCriterion("unlocked", InventoryChangeTrigger.TriggerInstance.hasItems(philosopherStone))
                .save(output, id("philosopher_stone"));

        Item[] everything = allIngredients();
        Advancement.Builder herbalist = Advancement.Builder.advancement()
                .parent(pestle)
#if MC_VER >= 26300
                .display(everything[0], title("herbalist"), description("herbalist"),
                        AdvancementType.CHALLENGE, true, true, false);
#else
                .display(everything[0], title("herbalist"), description("herbalist"), null,
                        AdvancementType.CHALLENGE, true, true, false);
#endif
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

#if MC_VER >= 26300
    private AdvancementHolder awarded(net.minecraft.data.worldgen.BootstrapContext<Advancement> output, AdvancementHolder parent,
                                      ItemLike icon, String name, AdvancementType type) {
#else
    private AdvancementHolder awarded(Consumer<AdvancementHolder> output, AdvancementHolder parent,
                                      ItemLike icon, String name, AdvancementType type) {
#endif
#if MC_VER >= 26200
        var criterion = net.minecraft.advancements.triggers.CriteriaTriggers.IMPOSSIBLE.createCriterion(
                new net.minecraft.advancements.triggers.ImpossibleTrigger.TriggerInstance());
#else
        var criterion = net.minecraft.advancements.CriteriaTriggers.IMPOSSIBLE.createCriterion(
                new net.minecraft.advancements.criterion.ImpossibleTrigger.TriggerInstance());
#endif
        return child(output, parent, icon, name, type, criterion);
    }

#if MC_VER >= 26300
    private AdvancementHolder child(net.minecraft.data.worldgen.BootstrapContext<Advancement> output, AdvancementHolder parent, ItemLike icon,
                                    String name, AdvancementType type,
#else
    private AdvancementHolder child(Consumer<AdvancementHolder> output, AdvancementHolder parent, ItemLike icon,
                                    String name, AdvancementType type,
#endif
#if MC_VER >= 26200
                                    net.minecraft.advancements.triggers.Criterion<?> criterion) {
#else
                                    net.minecraft.advancements.Criterion<?> criterion) {
#endif
        return Advancement.Builder.advancement()
                .parent(parent)
#if MC_VER >= 26300
                .display(icon.asItem(), title(name), description(name), type, true, true, false)
#else
                .display(icon, title(name), description(name), null, type, true, true, false)
#endif
                .addCriterion("unlocked", criterion)
                .save(output, id(name));
    }

#if MC_VER >= 26200
    private static net.minecraft.advancements.predicates.ItemPredicate.Builder anyIngredient(net.minecraft.core.HolderGetter<Item> items) {
        return net.minecraft.advancements.predicates.ItemPredicate.Builder.item()
#else
    private static net.minecraft.advancements.criterion.ItemPredicate.Builder anyIngredient(net.minecraft.core.HolderGetter<Item> items) {
        return net.minecraft.advancements.criterion.ItemPredicate.Builder.item()
#endif
                .of(items, AlchemiaItemTags.INGREDIENTS);
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

#if MC_VER >= 26300
    private net.minecraft.advancements.triggers.Criterion<?> planted(net.minecraft.world.level.block.Block block) {
        return net.minecraft.advancements.triggers.ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(
                this.output.lookup(net.minecraft.core.registries.Registries.BLOCK), block);
    }
#elif MC_VER >= 26200
    private static net.minecraft.advancements.triggers.Criterion<?> planted(net.minecraft.world.level.block.Block block) {
        return net.minecraft.advancements.triggers.ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(block);
    }
#else
    private static net.minecraft.advancements.Criterion<?> planted(net.minecraft.world.level.block.Block block) {
        return net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(block);
    }
#endif

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
