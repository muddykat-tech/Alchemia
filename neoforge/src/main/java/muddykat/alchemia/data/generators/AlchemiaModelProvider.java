package muddykat.alchemia.data.generators;

import muddykat.alchemia.common.items.ItemRecipePage;
import muddykat.alchemia.common.items.ItemAlchemyTablet;
import muddykat.alchemia.common.items.ItemAlchemistNotes;
import muddykat.alchemia.common.items.ItemBark;
import muddykat.alchemia.common.blocks.blockentity.BlockPlacedGuide;
import muddykat.alchemia.common.blocks.blockentity.BlockResearchDesk;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.AncientOak;
import muddykat.alchemia.common.blocks.BlockGeneric;
import muddykat.alchemia.common.blocks.helper.PlantForm;
import muddykat.alchemia.common.blocks.BlockInfectedLog;
import muddykat.alchemia.common.blocks.BlockIngredient;
import muddykat.alchemia.common.blocks.BlockIngredientBerry;
import muddykat.alchemia.common.blocks.BlockIngredientPatch;
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
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.util.random.WeightedList;
import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
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
import net.minecraft.world.level.FoliageColor;

import java.util.List;

public class AlchemiaModelProvider extends ModelProvider {

    private final java.util.Set<Identifier> emittedModels = new java.util.HashSet<>();

    public AlchemiaModelProvider(PackOutput output) {
        super(output, Alchemia.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        generateAncientOak(blockModels, itemModels);

        for (var holder : BlockRegistry.BLOCK_REGISTRY.values()) {
            Block block = holder.get();

            if (block instanceof BlockIngredientShelf shelf) {
                generateShelf(blockModels, shelf);
            } else if (block instanceof BlockIngredientPatch patch) {
                generatePatch(blockModels, patch);
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
            } else if (block instanceof BlockAlchemyMachineCore || block instanceof BlockAlchemyMachineUpgrade
                    || block instanceof BlockResearchDesk) {
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
            } else if (item instanceof ItemBark) {
                flatItem(itemModels, item, Identifier.fromNamespaceAndPath(Alchemia.MODID, "item/bark"));
            } else if (item instanceof ItemAlchemistNotes) {
                flatItem(itemModels, item, Identifier.fromNamespaceAndPath(Alchemia.MODID, "item/" + ItemAlchemistNotes.ID));
            } else if (item instanceof ItemAlchemyTablet) {
                flatItem(itemModels, item, Identifier.fromNamespaceAndPath(Alchemia.MODID, "item/" + ItemAlchemyTablet.ID));
            } else if (item instanceof ItemRecipePage) {
                flatItem(itemModels, item, Identifier.fromNamespaceAndPath(Alchemia.MODID, "item/book/recipe_page"));
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
                && !(holder.value() instanceof BlockAlchemyMachinePart)
                && !(holder.value() instanceof BlockResearchDesk)
                && !(holder.value() instanceof BlockPlacedGuide));
    }

    private void generateIngredientCrop(BlockModelGenerators blockModels, BlockIngredient block) {
        if (PlantForm.of(block.getIngredient()) == PlantForm.BEET) {
            blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                    .with(PropertyDispatch.initial(block.getAgeProperty())
                            .generate(age -> BlockModelGenerators.plainVariant(beetModel(blockModels, block, age)))));
            return;
        }

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

    private Identifier beetModel(BlockModelGenerators blockModels, BlockIngredient block, int age) {
        Ingredients ingredient = block.getIngredient();
        Identifier texture = PlantGrowthTextures.hasOwnStage(ingredient, age)
                ? blockTexture(PlantGrowthTextures.ownStage(ingredient, age))
                : Identifier.withDefaultNamespace("block/beetroots_stage" + age);
        Identifier model = blockModel(ingredient.name().toLowerCase() + "_beet_stage" + age);
        if (emittedModels.add(model)) {
            ModelTemplates.CROP.create(model, TextureMapping.crop(new Material(texture)), blockModels.modelOutput);
        }
        return model;
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

    private static final TextureSlot PATCH_BASE = TextureSlot.create("base");
    private static final TextureSlot PATCH_TOP = TextureSlot.create("top");
    private static final TextureSlot PATCH_DETAIL = TextureSlot.create("detail");
    private static final TextureSlot PATCH_FLOWER = TextureSlot.create("flower");
    private static final TextureSlot PATCH_CENTRE = TextureSlot.create("centre");
    private static final TextureSlot PATCH_PETAL = TextureSlot.create("petal");
    private static final String[] PATCH_STAGES = {"_sprouts", "_young", "", "_bloom"};
    private static final ModelTemplate[] PATCH_TEMPLATES = {
            patchTemplate(PATCH_STAGES[0], PATCH_BASE),
            patchTemplate(PATCH_STAGES[1], PATCH_BASE, PATCH_TOP),
            patchTemplate(PATCH_STAGES[2], PATCH_BASE, PATCH_TOP, PATCH_DETAIL),
            patchTemplate(PATCH_STAGES[3], PATCH_BASE, PATCH_TOP, PATCH_FLOWER, PATCH_CENTRE, PATCH_PETAL)};

    private static ModelTemplate patchTemplate(String stage, TextureSlot... slots) {
        return new ModelTemplate(java.util.Optional.of(Identifier.fromNamespaceAndPath(Alchemia.MODID, "block/template_patch" + stage)),
                java.util.Optional.empty(), slots);
    }

    private void generatePatch(BlockModelGenerators blockModels, BlockIngredientPatch block) {
        Ingredients ingredient = block.getIngredient();
        TextureMapping textures = new TextureMapping()
                .put(PATCH_BASE, patchTexture(ingredient, "base"))
                .put(PATCH_TOP, patchTexture(ingredient, "top"))
                .put(PATCH_DETAIL, patchTexture(ingredient, "detail"))
                .put(PATCH_FLOWER, patchTexture(ingredient, "flower_overlay"))
                .put(PATCH_CENTRE, patchTexture(ingredient, "flower_centre"))
                .put(PATCH_PETAL, patchTexture(ingredient, "petal"));

        Identifier[] models = new Identifier[PATCH_STAGES.length];
        for (int age = 0; age < models.length; age++) {
            models[age] = blockModel(ingredient.name().toLowerCase() + "_patch" + PATCH_STAGES[age]);
            if (emittedModels.add(models[age])) PATCH_TEMPLATES[age].create(models[age], textures, blockModels.modelOutput);
        }

        int bloomAge = block.getMaxAge();
        Direction[] faces = BlockIngredientPatch.FACES;
        MultiPartGenerator parts = MultiPartGenerator.multiPart(block);

        for (int index = 0; index < faces.length; index++) {
            Direction face = faces[index];
            BooleanProperty present = BlockIngredientPatch.faceProperty(face);

            for (int age = 0; age < bloomAge; age++) {
                parts.with(BlockModelGenerators.condition().term(present, true).term(block.getAgeProperty(), age),
                        patchOnFace(models[age], face));
            }

            ConditionBuilder blooms = BlockModelGenerators.condition().term(present, true).term(block.getAgeProperty(), bloomAge);
            ConditionBuilder[] outranked = new ConditionBuilder[index];
            for (int earlier = 0; earlier < index; earlier++) {
                BooleanProperty preferred = BlockIngredientPatch.faceProperty(faces[earlier]);
                blooms.term(preferred, false);
                outranked[earlier] = BlockModelGenerators.condition().term(present, true)
                        .term(block.getAgeProperty(), bloomAge).term(preferred, true);
            }

            parts.with(blooms, patchOnFace(models[bloomAge], face));
            if (index > 0) parts.with(BlockModelGenerators.or(outranked), patchOnFace(models[GREENERY_STAGE], face));
        }

        blockModels.blockStateOutput.accept(parts);
    }

    private static final int GREENERY_STAGE = 2;

    private static MultiVariant patchOnFace(Identifier model, Direction face) {
        Variant flat = BlockModelGenerators.plainModel(model);
        if (face == Direction.DOWN) {
            return BlockModelGenerators.variants(flat, flat.with(BlockModelGenerators.Y_ROT_90),
                    flat.with(BlockModelGenerators.Y_ROT_180), flat.with(BlockModelGenerators.Y_ROT_270));
        }

        VariantMutator turn = switch (face) {
            case EAST -> BlockModelGenerators.Y_ROT_90;
            case SOUTH -> BlockModelGenerators.Y_ROT_180;
            case WEST -> BlockModelGenerators.Y_ROT_270;
            default -> BlockModelGenerators.NOP;
        };
        VariantMutator flipped = switch (face) {
            case EAST -> BlockModelGenerators.Y_ROT_270;
            case SOUTH -> BlockModelGenerators.NOP;
            case WEST -> BlockModelGenerators.Y_ROT_90;
            default -> BlockModelGenerators.Y_ROT_180;
        };
        return BlockModelGenerators.variants(flat.with(BlockModelGenerators.X_ROT_270).with(turn),
                flat.with(BlockModelGenerators.X_ROT_90).with(flipped));
    }

    private static Material patchTexture(Ingredients ingredient, String part) {
        return new Material(blockTexture(PlantGrowthTextures.part(ingredient, part).texture()));
    }

    private void generatePart(BlockModelGenerators blockModels, Block block, Ingredients ingredient, String part) {
        PlantGrowthTextures.Sprite sprite = PlantGrowthTextures.part(ingredient, part);
        Identifier model = crossModelOnce(blockModels, blockModel(sprite.model()), blockTexture(sprite.texture()));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model)));
    }

    private static Material elderOak(String texture) {
        return new Material(Identifier.parse(AncientOak.TEXTURE_ROOT + texture));
    }

    private static String altSuffix(int index) {
        return index == 0 ? "" : "_alt" + index;
    }

    private static int variantWeight(List<String> sides, int index) {
        return index == 0 ? sides.size() - 1 : 1;
    }

    private void variedLog(BlockModelGenerators blockModels, Block block, List<String> sides, String top) {
        WeightedList.Builder<Variant> vertical = WeightedList.builder();
        WeightedList.Builder<Variant> horizontal = WeightedList.builder();
        Identifier item = null;

        for (int index = 0; index < sides.size(); index++) {
            TextureMapping mapping = TextureMapping.column(elderOak(sides.get(index)), elderOak(top));
            String suffix = altSuffix(index);
            Identifier upright = ModelTemplates.CUBE_COLUMN.createWithSuffix(block, suffix, mapping, blockModels.modelOutput);
            Identifier lying = ModelTemplates.CUBE_COLUMN_HORIZONTAL.createWithSuffix(block, suffix, mapping, blockModels.modelOutput);
            vertical.add(BlockModelGenerators.plainModel(upright), variantWeight(sides, index));
            horizontal.add(BlockModelGenerators.plainModel(lying), variantWeight(sides, index));
            if (index == 0) item = upright;
        }

        blockModels.blockStateOutput.accept(BlockModelGenerators.createRotatedPillarWithHorizontalVariant(block,
                new MultiVariant(vertical.build()), new MultiVariant(horizontal.build())));
        blockModels.registerSimpleItemModel(block, item);
    }

    private void variedWood(BlockModelGenerators blockModels, Block block, List<String> sides) {
        WeightedList.Builder<Variant> models = WeightedList.builder();
        Identifier item = null;

        for (int index = 0; index < sides.size(); index++) {
            Material side = elderOak(sides.get(index));
            Identifier model = ModelTemplates.CUBE_COLUMN.createWithSuffix(block, altSuffix(index),
                    TextureMapping.column(side, side), blockModels.modelOutput);
            models.add(BlockModelGenerators.plainModel(model), variantWeight(sides, index));
            if (index == 0) item = model;
        }

        blockModels.blockStateOutput.accept(BlockModelGenerators.createAxisAlignedPillarBlock(block, new MultiVariant(models.build())));
        blockModels.registerSimpleItemModel(block, item);
    }

    private void generateAncientOak(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Material planks = elderOak("planks");

        List<String> bark = AncientOak.sideVariants(AncientOak.LOG_SIDE, AncientOak.LOG_SIDE_ALTS);
        variedLog(blockModels, AncientOak.block(AncientOak.LOG), bark, AncientOak.LOG_TOP);
        variedWood(blockModels, AncientOak.block(AncientOak.WOOD), bark);

        List<String> stripped = AncientOak.sideVariants(AncientOak.STRIPPED_LOG_SIDE, AncientOak.STRIPPED_LOG_SIDE_ALTS);
        variedLog(blockModels, AncientOak.block(AncientOak.STRIPPED_LOG), stripped, AncientOak.STRIPPED_LOG_TOP);
        variedWood(blockModels, AncientOak.block(AncientOak.STRIPPED_WOOD), stripped);

        blockModels.new BlockFamilyProvider(TextureMapping.cube(planks))
                .fullBlock(AncientOak.block(AncientOak.PLANKS), ModelTemplates.CUBE_ALL)
                .stairs(AncientOak.block(AncientOak.STAIRS))
                .slab(AncientOak.block(AncientOak.SLAB))
                .fence(AncientOak.block(AncientOak.FENCE))
                .fenceGate(AncientOak.block(AncientOak.FENCE_GATE))
                .button(AncientOak.block(AncientOak.BUTTON))
                .pressurePlate(AncientOak.block(AncientOak.PRESSURE_PLATE));

        Block door = AncientOak.block(AncientOak.DOOR);
        TextureMapping doorMapping = TextureMapping.door(planks, planks);
        blockModels.blockStateOutput.accept(BlockModelGenerators.createDoor(door,
                BlockModelGenerators.plainVariant(ModelTemplates.DOOR_BOTTOM_LEFT.create(door, doorMapping, blockModels.modelOutput)),
                BlockModelGenerators.plainVariant(ModelTemplates.DOOR_BOTTOM_LEFT_OPEN.create(door, doorMapping, blockModels.modelOutput)),
                BlockModelGenerators.plainVariant(ModelTemplates.DOOR_BOTTOM_RIGHT.create(door, doorMapping, blockModels.modelOutput)),
                BlockModelGenerators.plainVariant(ModelTemplates.DOOR_BOTTOM_RIGHT_OPEN.create(door, doorMapping, blockModels.modelOutput)),
                BlockModelGenerators.plainVariant(ModelTemplates.DOOR_TOP_LEFT.create(door, doorMapping, blockModels.modelOutput)),
                BlockModelGenerators.plainVariant(ModelTemplates.DOOR_TOP_LEFT_OPEN.create(door, doorMapping, blockModels.modelOutput)),
                BlockModelGenerators.plainVariant(ModelTemplates.DOOR_TOP_RIGHT.create(door, doorMapping, blockModels.modelOutput)),
                BlockModelGenerators.plainVariant(ModelTemplates.DOOR_TOP_RIGHT_OPEN.create(door, doorMapping, blockModels.modelOutput))));
        flatItem(itemModels, door.asItem(), Identifier.withDefaultNamespace("item/oak_door"));

        Block trapdoor = AncientOak.block(AncientOak.TRAPDOOR);
        TextureMapping trapdoorMapping = TextureMapping.defaultTexture(planks);
        Identifier trapdoorBottom = ModelTemplates.ORIENTABLE_TRAPDOOR_BOTTOM.create(trapdoor, trapdoorMapping, blockModels.modelOutput);
        blockModels.blockStateOutput.accept(BlockModelGenerators.createOrientableTrapdoor(trapdoor,
                BlockModelGenerators.plainVariant(ModelTemplates.ORIENTABLE_TRAPDOOR_TOP.create(trapdoor, trapdoorMapping, blockModels.modelOutput)),
                BlockModelGenerators.plainVariant(trapdoorBottom),
                BlockModelGenerators.plainVariant(ModelTemplates.ORIENTABLE_TRAPDOOR_OPEN.create(trapdoor, trapdoorMapping, blockModels.modelOutput))));
        blockModels.registerSimpleItemModel(trapdoor, trapdoorBottom);

        Block leaves = AncientOak.block(AncientOak.LEAVES);
        Identifier leavesModel = ModelTemplates.LEAVES.create(leaves, TextureMapping.cube(elderOak("leaves")), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(leaves, BlockModelGenerators.plainVariant(leavesModel)));
        blockModels.registerSimpleTintedItemModel(leaves, leavesModel, ItemModelUtils.constantTint(FoliageColor.FOLIAGE_DEFAULT));

        Block sapling = AncientOak.block(AncientOak.SAPLING);
        blockModels.createCrossBlock(sapling, BlockModelGenerators.PlantType.NOT_TINTED, TextureMapping.cross(elderOak("sapling")));
        flatItem(itemModels, sapling.asItem(), Identifier.parse(AncientOak.TEXTURE_ROOT + "sapling"));
    }

    private void generateInfectedLog(BlockModelGenerators blockModels, BlockInfectedLog block) {
        String wood = block.getWood().name();
        String own = PlantGrowthTextures.ownTexture(block.getFungus(), "infected_" + wood + "_log");
        String ownTop = PlantGrowthTextures.ownTexture(block.getFungus(), "infected_" + wood + "_log_top");

        Identifier side = own != null ? blockTexture(own) : Identifier.parse(block.getWood().sideTexture());
        Identifier end = ownTop != null ? blockTexture(ownTop) : Identifier.parse(block.getWood().endTexture());

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
