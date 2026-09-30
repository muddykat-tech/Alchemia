package muddykat.alchemia.data.generators;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.BlockGeneric;
import muddykat.alchemia.common.blocks.BlockInfectedLog;
import muddykat.alchemia.common.blocks.BlockIngredient;
import muddykat.alchemia.common.blocks.BlockIngredientBerry;
import muddykat.alchemia.common.blocks.BlockIngredientShelf;
import muddykat.alchemia.common.blocks.BlockIngredientTall;
import muddykat.alchemia.common.blocks.BlockIngredientWild;
import muddykat.alchemia.common.blocks.BlockKelpStalk;
import muddykat.alchemia.common.blocks.BlockTangleBranch;
import muddykat.alchemia.common.blocks.BlockMineralBuddingGeneric;
import muddykat.alchemia.common.blocks.BlockMineralClusterGeneric;
import muddykat.alchemia.common.blocks.BlockMineralGeneric;
import muddykat.alchemia.common.blocks.blockentity.BlockPlacedPotions;
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
import muddykat.alchemia.common.items.ItemPotionGlass;
import muddykat.alchemia.client.render.CrushProperty;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.registration.registers.BlockRegistry;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.item.RangeSelectItemModel;
import net.minecraft.client.renderer.item.properties.numeric.Damage;
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

            if (block instanceof BlockIngredientShelf shelf) {
                generateShelf(blockModels, shelf);
            } else if (block instanceof BlockIngredient ingredient) {
                generateIngredientCrop(blockModels, ingredient);
            } else if (block instanceof BlockTangleBranch branch) {
                generatePart(blockModels, block, branch.getIngredient(), "branch");
            } else if (block instanceof BlockKelpStalk stalk) {
                generatePart(blockModels, block, stalk.getIngredient(), "stalk");
            } else if (block instanceof BlockInfectedLog log) {
                generateInfectedLog(blockModels, log);
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
                flatItem(itemModels, item, Identifier.fromNamespaceAndPath(Alchemia.MODID, "item/mortar_and_pestle"));
            } else if (item instanceof ItemAlchemiaGuide) {
                flatItem(itemModels, item, Identifier.fromNamespaceAndPath(Alchemia.MODID, "item/book/guide"));
            } else if (item instanceof BlockItemGeneric blockItem && blockItem.getBlock() instanceof BlockMineralClusterGeneric cluster) {
                flatItem(itemModels, item, clusterTexture(cluster));
            } else if (item instanceof ItemPathTool) {
                wearModel(itemModels, item, itemName(item));
            } else if (item instanceof ItemPotionGlass) {
                potionGlassItem(itemModels, item);
            } else if (SHIMMERING.contains(itemName(item))) {
                shimmeringItem(itemModels, item);
            } else if (MAGNUM_OPUS.contains(itemName(item))) {
                flatItem(itemModels, item, Identifier.fromNamespaceAndPath(Alchemia.MODID, "item/" + itemName(item)));
            }
        }

        PlantGrowthTextures.logPlaceholders();
    }

    @Override
    protected java.util.stream.Stream<? extends net.minecraft.core.Holder<Block>> getKnownBlocks() {
        return super.getKnownBlocks().filter(holder -> !(holder.value() instanceof BlockAlchemyCauldron)
                && !(holder.value() instanceof BlockPlacedPotions)
                && !(holder.value() instanceof BlockAlchemyMachineCore)
                && !(holder.value() instanceof BlockAlchemyMachineUpgrade)
                && !(holder.value() instanceof BlockAlchemyMachinePart));
    }

    private void generateIngredientCrop(BlockModelGenerators blockModels, BlockIngredient block) {
        if (block instanceof BlockIngredientBerry) {
            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                    .with(PropertyDispatch.initial(block.getAgeProperty(), BlockIngredientTall.HALF, BlockIngredientBerry.FRUITING)
                            .generate((age, half, fruiting) -> cropVariant(blockModels, block,
                                    age == block.getMaxAge() && !fruiting ? age - 1 : age, false))));
            return;
        }

        if (block instanceof BlockIngredientTall) {
            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                    .with(PropertyDispatch.initial(block.getAgeProperty(), BlockIngredientTall.HALF)
                            .generate((age, half) -> cropVariant(blockModels, block, age, false))));
            return;
        }

        if (block instanceof BlockIngredientWild) {
            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                    .with(PropertyDispatch.initial(block.getAgeProperty(), BlockIngredientWild.WILD)
                            .generate((age, wild) -> cropVariant(blockModels, block, age, wild))));
            return;
        }

        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(block.getAgeProperty())
                        .generate(age -> cropVariant(blockModels, block, age, false))));
    }

    private MultiVariant cropVariant(BlockModelGenerators blockModels, BlockIngredient block, int age, boolean wild) {
        PlantGrowthTextures.Sprite sprite = PlantGrowthTextures.sprite(block.getIngredient(), age, block.getMaxAge(), wild);
        return BlockModelGenerators.plainVariant(
                crossModelOnce(blockModels, blockModel(sprite.model()), blockTexture(sprite.texture())));
    }

    private static final TextureSlot SHELF = TextureSlot.create("shelf");
    private static final ModelTemplate SHELF_TEMPLATE = new ModelTemplate(
            java.util.Optional.of(Identifier.fromNamespaceAndPath(Alchemia.MODID, "block/template_shelf")), java.util.Optional.empty(), SHELF);

    private void generateShelf(BlockModelGenerators blockModels, BlockIngredientShelf block) {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(block.getAgeProperty())
                        .generate(age -> {
                            PlantGrowthTextures.Sprite sprite = PlantGrowthTextures.sprite(block.getIngredient(), age, block.getMaxAge(), false);
                            Identifier model = blockModel(sprite.model() + "_wall");
                            if (emittedModels.add(model)) {
                                Material texture = new Material(blockTexture(sprite.texture()));
                                SHELF_TEMPLATE.create(model, new TextureMapping().put(SHELF, texture).put(TextureSlot.PARTICLE, texture), blockModels.modelOutput);
                            }
                            return BlockModelGenerators.plainVariant(model);
                        }))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
    }

    private void generatePart(BlockModelGenerators blockModels, Block block, Ingredients ingredient, String part) {
        PlantGrowthTextures.Sprite sprite = PlantGrowthTextures.part(ingredient, part);
        Identifier model = crossModelOnce(blockModels, blockModel(sprite.model()), blockTexture(sprite.texture()));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model)));
    }

    private void generateInfectedLog(BlockModelGenerators blockModels, BlockInfectedLog block) {
        String wood = block.getWood().name();
        String own = PlantGrowthTextures.ownTexture(block.getFungus(), "infected_" + wood + "_log");
        String ownTop = PlantGrowthTextures.ownTexture(block.getFungus(), "infected_" + wood + "_log_top");

        Identifier side = own != null ? blockTexture(own) : Identifier.withDefaultNamespace("block/" + wood + "_log");
        Identifier end = ownTop != null ? blockTexture(ownTop) : Identifier.withDefaultNamespace("block/" + wood + "_log_top");

        Identifier model = ModelTemplates.CUBE_COLUMN.create(blockModel(blockName(block)),
                TextureMapping.column(new Material(side), new Material(end)), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(BlockModelGenerators.createAxisAlignedPillarBlock(block, BlockModelGenerators.plainVariant(model)));
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

    private void wearModel(ItemModelGenerators itemModels, Item item, String name) {
        Identifier fresh = flatModel(itemModels, name, itemTexture(name));
        Identifier worn1 = flatModel(itemModels, name + "_worn_1", itemTexture(name + "_worn_1"));
        Identifier worn2 = flatModel(itemModels, name + "_worn_2", itemTexture(name + "_worn_2"));
        Identifier worn3 = flatModel(itemModels, name + "_worn_3", itemTexture(name + "_worn_3"));

        itemModels.itemModelOutput.accept(item, ItemModelUtils.rangeSelect(
                new Damage(true),
                ItemModelUtils.plainModel(fresh),
                new RangeSelectItemModel.Entry(0.25F, ItemModelUtils.plainModel(worn1)),
                new RangeSelectItemModel.Entry(0.5F, ItemModelUtils.plainModel(worn2)),
                new RangeSelectItemModel.Entry(0.75F, ItemModelUtils.plainModel(worn3))));
    }

    private static Identifier itemTexture(String name) {
        return Identifier.fromNamespaceAndPath(Alchemia.MODID, "item/" + name);
    }

    private Identifier flatModel(ItemModelGenerators itemModels, String name, Identifier texture) {
        return ModelTemplates.FLAT_ITEM.create(itemModel(name), TextureMapping.layer0(new Material(texture)), itemModels.modelOutput);
    }

    private static final java.util.Set<String> MAGNUM_OPUS =
            java.util.Set.of("nigredo", "albedo", "citrinitas", "rubedo", "philosopher_stone");

    private static final java.util.Set<String> SHIMMERING = java.util.Set.of("philosopher_stone");

    private void shimmeringItem(ItemModelGenerators itemModels, Item item) {
        String name = itemName(item);
        Identifier modelLocation = ModelTemplates.TWO_LAYERED_ITEM.create(itemModel(name),
                TextureMapping.layered(
                        new Material(Identifier.fromNamespaceAndPath(Alchemia.MODID, "item/" + name)),
                        new Material(Identifier.fromNamespaceAndPath(Alchemia.MODID, "item/" + name + "_shimmer"))),
                itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(modelLocation));
    }

    private void potionGlassItem(ItemModelGenerators itemModels, Item item) {
        String name = itemName(item);
        Identifier glass = itemTexture("potion_glass/" + name);
        flatItem(itemModels, item, glass);
        ModelTemplates.TWO_LAYERED_ITEM.create(itemModel(name + "_potion"),
                TextureMapping.layered(new Material(itemTexture("potion_glass/" + name + "_overlay")), new Material(glass)),
                itemModels.modelOutput);
    }

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
