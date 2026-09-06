package muddykat.alchemia.data.generators;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.BlockGeneric;
import muddykat.alchemia.common.blocks.BlockIngredient;
import muddykat.alchemia.common.blocks.BlockMineralBuddingGeneric;
import muddykat.alchemia.common.blocks.BlockMineralClusterGeneric;
import muddykat.alchemia.common.blocks.BlockMineralGeneric;
import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyCauldron;
import muddykat.alchemia.common.items.BlockItemGeneric;
import muddykat.alchemia.common.items.ItemAlchemiaGuide;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.ItemIngredientSeed;
import muddykat.alchemia.common.items.ItemMortarPestle;
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
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class AlchemiaModelProvider extends ModelProvider {

    private final java.util.Set<Identifier> emittedModels = new java.util.HashSet<>();

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
                flatItem(itemModels, item, Identifier.fromNamespaceAndPath(Alchemia.MODID, "item/mortar_and_pestle"));
            } else if (item instanceof ItemAlchemiaGuide) {
                flatItem(itemModels, item, Identifier.fromNamespaceAndPath(Alchemia.MODID, "item/book/guide"));
            } else if (item instanceof BlockItemGeneric blockItem && blockItem.getBlock() instanceof BlockMineralClusterGeneric cluster) {
                flatItem(itemModels, item, clusterTexture(cluster));
            } else if (MAGNUM_OPUS.contains(itemName(item))) {
                flatItem(itemModels, item, Identifier.fromNamespaceAndPath(Alchemia.MODID, "item/" + itemName(item)));
            }
        }
    }

    @Override
    protected java.util.stream.Stream<? extends net.minecraft.core.Holder<Block>> getKnownBlocks() {
        return super.getKnownBlocks().filter(holder -> !(holder.value() instanceof BlockAlchemyCauldron));
    }

    private void generateIngredientCrop(BlockModelGenerators blockModels, BlockIngredient block) {
        String type = block.getIngredientType().name().toLowerCase();
        String name = block.getIngredient().name().toLowerCase();

        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(block.getAgeProperty()).generate(age -> {
                    Identifier modelLocation;
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
        Identifier modelLocation = crossModelOnce(blockModels, blockModel(clusterName(cluster)), clusterTexture(cluster));

        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(cluster, BlockModelGenerators.plainVariant(modelLocation))
                .with(BlockModelGenerators.ROTATIONS_COLUMN_WITH_FACING));
    }

    private Identifier crossModelOnce(BlockModelGenerators blockModels, Identifier modelLocation, Identifier texture) {
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

    private void cubeAll(BlockModelGenerators blockModels, Block block, Identifier texture) {
        Identifier modelLocation = ModelTemplates.CUBE_ALL.create(blockModel(blockName(block)),
                TextureMapping.cube(new Material(texture)), blockModels.modelOutput);

        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(modelLocation)));
        blockModels.registerSimpleItemModel(block, modelLocation);
    }

    private void crushableItem(ItemModelGenerators itemModels, Item item, ItemIngredient ingredient) {
        Identifier whole = flatModel(itemModels, itemName(item), ingredient.getTextureLocation());
        Identifier ground = flatModel(itemModels, itemName(item) + "_crushed", ingredient.getCrushedTextureLocation());

        itemModels.itemModelOutput.accept(item, ItemModelUtils.rangeSelect(
                CrushProperty.INSTANCE,
                ItemModelUtils.plainModel(whole),
                new RangeSelectItemModel.Entry(1.0F, ItemModelUtils.plainModel(ground))));
    }

    private Identifier flatModel(ItemModelGenerators itemModels, String name, Identifier texture) {
        return ModelTemplates.FLAT_ITEM.create(itemModel(name), TextureMapping.layer0(new Material(texture)), itemModels.modelOutput);
    }

    private static final java.util.Set<String> MAGNUM_OPUS =
            java.util.Set.of("nigredo", "albedo", "citrinitas", "rubedo", "philosopher_stone");

    private void flatItem(ItemModelGenerators itemModels, Item item, Identifier texture) {
        Identifier modelLocation = ModelTemplates.FLAT_ITEM.create(itemModel(itemName(item)),
                TextureMapping.layer0(new Material(texture)), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(modelLocation));
    }

    private static String clusterName(BlockMineralClusterGeneric cluster) {
        String suffix = cluster.getSize() == BlockRegistry.BudSize.CLUSTER ? "_cluster" : "_bud_" + cluster.getSize().name().toLowerCase();
        return cluster.getIngredient().name().toLowerCase() + suffix;
    }

    private static Identifier clusterTexture(BlockMineralClusterGeneric cluster) {
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

    private static Identifier blockTexture(String path) {
        return Identifier.fromNamespaceAndPath(Alchemia.MODID, "block/" + path);
    }

    private static Identifier blockModel(String path) {
        return Identifier.fromNamespaceAndPath(Alchemia.MODID, "block/" + path);
    }

    private static Identifier itemModel(String path) {
        return Identifier.fromNamespaceAndPath(Alchemia.MODID, "item/" + path);
    }
}
