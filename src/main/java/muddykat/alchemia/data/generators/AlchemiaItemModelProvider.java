package muddykat.alchemia.data.generators;

import muddykat.alchemia.Alchemia;
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
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.registration.registers.BlockRegistry;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Map;
import java.util.Set;

public class AlchemiaItemModelProvider extends ItemModelProvider {

    private static final Set<String> MAGNUM_OPUS =
            Set.of("nigredo", "albedo", "citrinitas", "rubedo", "philosopher_stone");

    private static final ResourceLocation DAMAGE = new ResourceLocation("damage");
    private static final ResourceLocation GENERATED = new ResourceLocation("item/generated");

    public AlchemiaItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Alchemia.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
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
            } else if (item instanceof ItemPathTool) {
                wearModel(itemName(item));
            } else if (item instanceof BlockItemGeneric blockItem) {
                generateBlockItem(item, blockItem);
            } else if (MAGNUM_OPUS.contains(itemName(item))) {
                flatItem(itemName(item), itemTexture(itemName(item)));
            }
        }
    }

    private void generateBlockItem(Item item, BlockItemGeneric blockItem) {
        var block = blockItem.getBlock();

        if (block instanceof BlockAlchemyCauldron
                || block instanceof BlockAlchemyMachineCore
                || block instanceof BlockAlchemyMachineUpgrade) {
            return;
        }

        if (block instanceof BlockMineralClusterGeneric cluster) {
            flatItem(itemName(item), clusterTexture(cluster));
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
        return new ResourceLocation(Alchemia.MODID, "block/"
                + ingredient.getType().name().toLowerCase() + "s/" + ingredient.name().toLowerCase()
                + "/growth_stage_" + cluster.getSize().ordinal());
    }

    private static ResourceLocation itemTexture(String name) {
        return new ResourceLocation(Alchemia.MODID, "item/" + name);
    }

    private static String itemName(Item item) {
        return ItemRegistry.ITEM_REGISTRY.entrySet().stream()
                .filter(entry -> entry.getValue().get() == item)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unregistered item " + item));
    }
}
