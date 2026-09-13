package muddykat.alchemia.data.generators;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.BlockGeneric;
import muddykat.alchemia.common.blocks.BlockIngredient;
import muddykat.alchemia.common.blocks.BlockMineralBuddingGeneric;
import muddykat.alchemia.common.blocks.BlockMineralClusterGeneric;
import muddykat.alchemia.common.blocks.BlockMineralGeneric;
import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyCauldron;
import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyMachineCore;
import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyMachinePart;
import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyMachineUpgrade;
import muddykat.alchemia.common.items.BlockItemGeneric;
import muddykat.alchemia.common.items.ItemAlchemiaGuide;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.ItemIngredientSeed;
import muddykat.alchemia.common.items.ItemMortarPestle;
import muddykat.alchemia.common.items.ItemPathTool;
import muddykat.alchemia.client.render.CrushProperty;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.registration.registers.BlockRegistry;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.renderer.item.RangeSelectItemModel;
import net.minecraft.client.renderer.item.properties.numeric.Damage;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class AlchemiaModelProvider extends ModelProvider {

    private final java.util.Set<ResourceLocation> emittedModels = new java.util.HashSet<>();

    public AlchemiaModelProvider(PackOutput output) {
        super(output, Alchemia.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        for (var holder : BlockRegistry.BLOCK_REGISTRY.values()) {
            Block block = holder.get();

            if (block instanceof BlockIngredient ingredient) {
                generateIngredientCrop(blockModels, ingredient);
            } else if (block instanceof BlockMineralClusterGeneric cluster) {
                generateCluster(blockModels, cluster);
            } else if (block instanceof BlockMineralBuddingGeneric budding) {
                generateMineralCube(blockModels, block, budding.getIngredient());
            } else if (block instanceof BlockMineralGeneric mineral) {
                generateMineralCube(blockModels, block, mineral.getIngredient());
            } else if (block instanceof BlockAlchemyCauldron) {
                blockModels.registerSimpleItemModel(block, itemModel("alchemical_cauldron"));
            } else if (block instanceof BlockAlchemyMachineCore || block instanceof BlockAlchemyMachineUpgrade) {
                blockModels.registerSimpleItemModel(block, itemModel(blockName(block)));
            } else if (block instanceof BlockGeneric) {
                generateAlchemyCube(blockModels, block);
            }
        }

        for (var holder : ItemRegistry.ITEM_REGISTRY.values()) {
            Item item = holder.get();

            if (item instanceof ItemIngredientSeed seed) {
                flatItem(itemModels, item, seed.getTextureLocation());
            } else if (item instanceof ItemIngredient ingredient) {
                crushableItem(itemModels, item, ingredient);
            } else if (item instanceof ItemMortarPestle) {
                flatItem(itemModels, item, new ResourceLocation(Alchemia.MODID, "item/mortar_and_pestle"));
            } else if (item instanceof ItemAlchemiaGuide) {
                flatItem(itemModels, item, new ResourceLocation(Alchemia.MODID, "item/book/guide"));
            } else if (item instanceof BlockItemGeneric blockItem && blockItem.getBlock() instanceof BlockMineralClusterGeneric cluster) {
                flatItem(itemModels, item, clusterTexture(cluster));
            } else if (item instanceof ItemPathTool) {
                wearModel(itemModels, item, itemName(item));
            } else if (MAGNUM_OPUS.contains(itemName(item))) {
                flatItem(itemModels, item, new ResourceLocation(Alchemia.MODID, "item/" + itemName(item)));
            }
        }
    }

    @Override
    protected java.util.stream.Stream<? extends net.minecraft.core.Holder<Block>> getKnownBlocks() {
        return super.getKnownBlocks().filter(holder -> !(holder.value() instanceof BlockAlchemyCauldron)
                && !(holder.value() instanceof BlockAlchemyMachineCore)
                && !(holder.value() instanceof BlockAlchemyMachineUpgrade)
                && !(holder.value() instanceof BlockAlchemyMachinePart));
    }

    private void generateIngredientCrop(BlockModelGenerators blockModels, BlockIngredient block) {
        String type = block.getIngredientType().name().toLowerCase();
        String name = block.getIngredient().name().toLowerCase();

        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(block.getAgeProperty()).generate(age -> {
                    ResourceLocation modelLocation;
                    if (age == block.getMaxAge()) {
                        modelLocation = crossModelOnce(blockModels, blockModel(name), blockTexture(type + "s/" + name));
                    } else {
                        String stage = "growth/" + type + "/growth_stage_" + age;
                        modelLocation = crossModelOnce(blockModels, blockModel(stage), blockTexture(stage));
                    }
                    return BlockModelGenerators.plainVariant(modelLocation);
                })));
    }

    private void generateCluster(BlockModelGenerators blockModels, BlockMineralClusterGeneric cluster) {
        ResourceLocation modelLocation = crossModelOnce(blockModels, blockModel(clusterName(cluster)), clusterTexture(cluster));

        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(cluster, BlockModelGenerators.plainVariant(modelLocation))
                .with(BlockModelGenerators.ROTATIONS_COLUMN_WITH_FACING));
    }

    private ResourceLocation crossModelOnce(BlockModelGenerators blockModels, ResourceLocation modelLocation, ResourceLocation texture) {
        if (emittedModels.add(modelLocation)) {
            ModelTemplates.CROSS.create(modelLocation, TextureMapping.cross(new Material(texture)), blockModels.modelOutput);
        }
        return modelLocation;
    }

    private void generateMineralCube(BlockModelGenerators blockModels, Block block, Ingredients ingredient) {
        cubeAll(blockModels, block, blockTexture("minerals/" + ingredient.name().toLowerCase() + "/" + blockName(block)));
    }

    private void generateAlchemyCube(BlockModelGenerators blockModels, Block block) {
        cubeAll(blockModels, block, blockTexture("alchemy/" + blockName(block)));
    }

    private void cubeAll(BlockModelGenerators blockModels, Block block, ResourceLocation texture) {
        ResourceLocation modelLocation = ModelTemplates.CUBE_ALL.create(blockModel(blockName(block)),
                TextureMapping.cube(new Material(texture)), blockModels.modelOutput);

        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(modelLocation)));
        blockModels.registerSimpleItemModel(block, modelLocation);
    }

    private void crushableItem(ItemModelGenerators itemModels, Item item, ItemIngredient ingredient) {
        ResourceLocation whole = flatModel(itemModels, itemName(item), ingredient.getTextureLocation());
        ResourceLocation ground = flatModel(itemModels, itemName(item) + "_crushed", ingredient.getCrushedTextureLocation());

        itemModels.itemModelOutput.accept(item, ItemModelUtils.rangeSelect(
                CrushProperty.INSTANCE,
                ItemModelUtils.plainModel(whole),
                new RangeSelectItemModel.Entry(1.0F, ItemModelUtils.plainModel(ground))));
    }

    private void wearModel(ItemModelGenerators itemModels, Item item, String name) {
        ResourceLocation fresh = flatModel(itemModels, name, itemTexture(name));
        ResourceLocation worn1 = flatModel(itemModels, name + "_worn_1", itemTexture(name + "_worn_1"));
        ResourceLocation worn2 = flatModel(itemModels, name + "_worn_2", itemTexture(name + "_worn_2"));
        ResourceLocation worn3 = flatModel(itemModels, name + "_worn_3", itemTexture(name + "_worn_3"));

        itemModels.itemModelOutput.accept(item, ItemModelUtils.rangeSelect(
                new Damage(true),
                ItemModelUtils.plainModel(fresh),
                new RangeSelectItemModel.Entry(0.25F, ItemModelUtils.plainModel(worn1)),
                new RangeSelectItemModel.Entry(0.5F, ItemModelUtils.plainModel(worn2)),
                new RangeSelectItemModel.Entry(0.75F, ItemModelUtils.plainModel(worn3))));
    }

    private static ResourceLocation itemTexture(String name) {
        return new ResourceLocation(Alchemia.MODID, "item/" + name);
    }

    private ResourceLocation flatModel(ItemModelGenerators itemModels, String name, ResourceLocation texture) {
        return ModelTemplates.FLAT_ITEM.create(itemModel(name), TextureMapping.layer0(new Material(texture)), itemModels.modelOutput);
    }

    private static final java.util.Set<String> MAGNUM_OPUS =
            java.util.Set.of("nigredo", "albedo", "citrinitas", "rubedo", "philosopher_stone");

    private void flatItem(ItemModelGenerators itemModels, Item item, ResourceLocation texture) {
        ResourceLocation modelLocation = ModelTemplates.FLAT_ITEM.create(itemModel(itemName(item)),
                TextureMapping.layer0(new Material(texture)), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(modelLocation));
    }

    private static String clusterName(BlockMineralClusterGeneric cluster) {
        String suffix = cluster.getSize() == BlockRegistry.BudSize.CLUSTER ? "_cluster" : "_bud_" + cluster.getSize().name().toLowerCase();
        return cluster.getIngredient().name().toLowerCase() + suffix;
    }

    private static ResourceLocation clusterTexture(BlockMineralClusterGeneric cluster) {
        Ingredients ingredient = cluster.getIngredient();
        return blockTexture(ingredient.getType().name().toLowerCase() + "s/" + ingredient.name().toLowerCase() + "/growth_stage_" + cluster.getSize().ordinal());
    }

    private static String blockName(Block block) {
        return BlockRegistry.BLOCK_REGISTRY.entrySet().stream()
                .filter(entry -> entry.getValue().get() == block)
                .map(java.util.Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unregistered block " + block));
    }

    private static String itemName(Item item) {
        return ItemRegistry.ITEM_REGISTRY.entrySet().stream()
                .filter(entry -> entry.getValue().get() == item)
                .map(java.util.Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unregistered item " + item));
    }

    private static ResourceLocation blockTexture(String path) {
        return new ResourceLocation(Alchemia.MODID, "block/" + path);
    }

    private static ResourceLocation blockModel(String path) {
        return new ResourceLocation(Alchemia.MODID, "block/" + path);
    }

    private static ResourceLocation itemModel(String path) {
        return new ResourceLocation(Alchemia.MODID, "item/" + path);
    }
}
