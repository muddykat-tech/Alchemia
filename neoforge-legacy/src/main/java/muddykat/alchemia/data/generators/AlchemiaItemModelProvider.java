package muddykat.alchemia.data.generators;

import muddykat.alchemia.common.blocks.blockentity.BlockResearchDesk;
import muddykat.alchemia.common.items.ItemAlchemistNotes;
import muddykat.alchemia.common.items.ItemAlchemyTablet;
import muddykat.alchemia.common.items.ItemBark;
import muddykat.alchemia.common.items.ItemRecipePage;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.AncientOak;
import muddykat.alchemia.client.render.CrushProperty;
import muddykat.alchemia.common.blocks.BlockMineralClusterGeneric;
import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyCauldron;
import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyMachineCore;
import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyMachineUpgrade;
import muddykat.alchemia.common.items.BlockItemGeneric;
import muddykat.alchemia.common.items.ItemAlchemiaGuide;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.ItemIngredientSeed;
import muddykat.alchemia.common.items.ItemMortarPestle;
import muddykat.alchemia.common.items.ItemPathTool;
import muddykat.alchemia.common.items.ItemPotionGlass;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.registration.registers.BlockRegistry;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.Map;
import java.util.Set;

public class AlchemiaItemModelProvider extends ItemModelProvider {

    private static final Set<String> MAGNUM_OPUS =
            Set.of("nigredo", "albedo", "citrinitas", "rubedo", "philosopher_stone");

    private static final Set<String> SHIMMERING = Set.of("philosopher_stone");

    private static final ResourceLocation DAMAGE = ResourceLocation.parse("damage");
    private static final ResourceLocation GENERATED = ResourceLocation.parse("item/generated");

    public AlchemiaItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Alchemia.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        flatItem(AncientOak.DOOR, mcLoc("item/oak_door"));

        for (var holder : ItemRegistry.ITEM_REGISTRY.values()) {
            Item item = holder.get();

            if (item instanceof ItemIngredientSeed seed) {
                flatItem(itemName(item), seed.getTextureLocation());
            } else if (item instanceof ItemIngredient ingredient) {
                crushableItem(item, ingredient);
            } else if (item instanceof ItemMortarPestle) {
                flatItem(itemName(item), itemTexture("mortar_and_pestle"));
            } else if (item instanceof ItemAlchemiaGuide) {
                flatItem(itemName(item), itemTexture("book/guide"));
            } else if (item instanceof ItemBark) {
                flatItem(itemName(item), itemTexture("bark"));
            } else if (item instanceof ItemAlchemistNotes) {
                flatItem(itemName(item), itemTexture(ItemAlchemistNotes.ID));
            } else if (item instanceof ItemAlchemyTablet) {
                flatItem(itemName(item), itemTexture(ItemAlchemyTablet.ID));
            } else if (item instanceof ItemRecipePage) {
                flatItem(itemName(item), itemTexture("book/recipe_page"));
            } else if (item instanceof ItemPathTool) {
                wearModel(itemName(item));
            } else if (item instanceof BlockItemGeneric blockItem) {
                generateBlockItem(item, blockItem);
            } else if (item instanceof ItemPotionGlass) {
                String name = itemName(item);
                flatItem(name, itemTexture("potion_glass/" + name));
                flatItem(name + "_potion", itemTexture("potion_glass/" + name + "_overlay"))
                        .texture("layer1", itemTexture("potion_glass/" + name));
            } else if (SHIMMERING.contains(itemName(item))) {
                flatItem(itemName(item), itemTexture(itemName(item)))
                        .texture("layer1", itemTexture(itemName(item) + "_shimmer"));
            } else if (MAGNUM_OPUS.contains(itemName(item))) {
                flatItem(itemName(item), itemTexture(itemName(item)));
            }
        }
    }

    private void generateBlockItem(Item item, BlockItemGeneric blockItem) {
        var block = blockItem.getBlock();

        if (block instanceof BlockAlchemyCauldron
                || block instanceof BlockResearchDesk
                || block instanceof BlockAlchemyMachineCore
                || block instanceof BlockAlchemyMachineUpgrade) {
            return;
        }

        String name = itemName(item);
        if (block instanceof BlockMineralClusterGeneric cluster) {
            flatItem(name, clusterTexture(cluster));
        } else if (name.equals(AncientOak.SAPLING)) {
            flatItem(name, ResourceLocation.parse(AncientOak.TEXTURE_ROOT + "sapling"));
        } else if (name.equals(AncientOak.FENCE)) {
            fenceInventory(name, ResourceLocation.parse(AncientOak.TEXTURE_ROOT + "planks"));
        } else if (name.equals(AncientOak.BUTTON)) {
            buttonInventory(name, ResourceLocation.parse(AncientOak.TEXTURE_ROOT + "planks"));
        } else if (name.equals(AncientOak.TRAPDOOR)) {
            withExistingParent(name, modLoc("block/" + name + "_bottom"));
        } else {
            withExistingParent(itemName(item), modLoc("block/" + itemName(item)));
        }
    }

    private ItemModelBuilder flatItem(String name, ResourceLocation texture) {
        return getBuilder(name)
                .parent(new ModelFile.UncheckedModelFile(GENERATED))
                .texture("layer0", texture);
    }

    private void crushableItem(Item item, ItemIngredient ingredient) {
        String name = itemName(item);
        ItemModelBuilder ground = flatItem(name + "_crushed", ingredient.getCrushedTextureLocation());

        flatItem(name, ingredient.getTextureLocation())
                .override()
                .predicate(CrushProperty.ID, 1.0F)
                .model(ground)
                .end();
    }

    private void wearModel(String name) {
        ItemModelBuilder worn1 = flatItem(name + "_worn_1", itemTexture(name + "_worn_1"));
        ItemModelBuilder worn2 = flatItem(name + "_worn_2", itemTexture(name + "_worn_2"));
        ItemModelBuilder worn3 = flatItem(name + "_worn_3", itemTexture(name + "_worn_3"));

        flatItem(name, itemTexture(name))
                .override().predicate(DAMAGE, 0.25F).model(worn1).end()
                .override().predicate(DAMAGE, 0.5F).model(worn2).end()
                .override().predicate(DAMAGE, 0.75F).model(worn3).end();
    }

    private static ResourceLocation clusterTexture(BlockMineralClusterGeneric cluster) {
        Ingredients ingredient = cluster.getIngredient();
        return ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, "block/"
                + ingredient.getType().name().toLowerCase() + "s/" + ingredient.name().toLowerCase()
                + "/growth_stage_" + cluster.getSize().ordinal());
    }

    private static ResourceLocation itemTexture(String name) {
        return ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, "item/" + name);
    }

    private static String itemName(Item item) {
        return ItemRegistry.ITEM_REGISTRY.entrySet().stream()
                .filter(entry -> entry.getValue().get() == item)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unregistered item " + item));
    }
}
