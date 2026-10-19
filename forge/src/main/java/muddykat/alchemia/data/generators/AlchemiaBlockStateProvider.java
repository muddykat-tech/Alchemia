package muddykat.alchemia.data.generators;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.AncientOak;
import muddykat.alchemia.common.blocks.BlockGeneric;
import muddykat.alchemia.common.blocks.BlockInfectedLog;
import muddykat.alchemia.common.blocks.BlockIngredient;
import muddykat.alchemia.common.blocks.BlockIngredientBerry;
import muddykat.alchemia.common.blocks.BlockIngredientPatch;
import muddykat.alchemia.common.blocks.BlockIngredientShelf;
import muddykat.alchemia.common.blocks.BlockIngredientWild;
import muddykat.alchemia.common.blocks.BlockKelpStalk;
import muddykat.alchemia.common.blocks.BlockTangleBranch;
import muddykat.alchemia.common.blocks.BlockMineralBuddingGeneric;
import muddykat.alchemia.common.blocks.BlockMineralClusterGeneric;
import muddykat.alchemia.common.blocks.BlockMineralGeneric;
import muddykat.alchemia.common.blocks.helper.PlantForm;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.registration.registers.BlockRegistry;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlchemiaBlockStateProvider extends BlockStateProvider {

    private final Map<ResourceLocation, ModelFile> crossModels = new HashMap<>();

    public AlchemiaBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Alchemia.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        for (var holder : BlockRegistry.BLOCK_REGISTRY.values()) {
            Block block = holder.get();

            if (block instanceof BlockIngredientShelf shelf) {
                generateShelf(shelf);
            } else if (block instanceof BlockIngredientPatch patch) {
                generatePatch(patch);
            } else if (block instanceof BlockIngredient ingredient) {
                generateIngredientCrop(ingredient);
            } else if (block instanceof BlockTangleBranch branch) {
                generatePart(block, branch.getIngredient(), "branch");
            } else if (block instanceof BlockKelpStalk stalk) {
                generatePart(block, stalk.getIngredient(), "stalk");
            } else if (block instanceof BlockInfectedLog log) {
                generateInfectedLog(log);
            } else if (block instanceof BlockMineralClusterGeneric cluster) {
                generateCluster(cluster);
            } else if (block instanceof BlockMineralBuddingGeneric budding) {
                generateMineralCube(block, budding.getIngredient());
            } else if (block instanceof BlockMineralGeneric mineral) {
                generateMineralCube(block, mineral.getIngredient());
            } else if (block instanceof BlockGeneric) {
                generateAlchemyCube(block);
            }
        }

        generateAncientOak();
        PlantGrowthTextures.logPlaceholders();
    }

    private static ResourceLocation elderOak(String texture) {
        return new ResourceLocation(AncientOak.TEXTURE_ROOT + texture);
    }

    private void variedPillar(Block block, List<String> sides, String top) {
        String name = BlockRegistry.BLOCK_REGISTRY.entrySet().stream()
                .filter(entry -> entry.getValue().get() == block)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow();
        List<ModelFile> upright = new ArrayList<>();
        List<ModelFile> lying = new ArrayList<>();

        for (int index = 0; index < sides.size(); index++) {
            String model = index == 0 ? name : name + "_alt" + index;
            ResourceLocation side = elderOak(sides.get(index));
            ResourceLocation end = top == null ? side : elderOak(top);
            ModelFile column = models().cubeColumn(model, side, end);
            upright.add(column);
            lying.add(top == null ? column : models().cubeColumnHorizontal(model + "_horizontal", side, end));
        }

        getVariantBuilder(block).forAllStates(state -> {
            Direction.Axis axis = state.getValue(RotatedPillarBlock.AXIS);
            List<ModelFile> models = axis == Direction.Axis.Y ? upright : lying;
            ConfiguredModel.Builder<?> builder = ConfiguredModel.builder();
            for (int index = 0; index < models.size(); index++) {
                if (index > 0) builder = builder.nextModel();
                builder.modelFile(models.get(index)).weight(index == 0 ? models.size() - 1 : 1);
                if (axis != Direction.Axis.Y) builder.rotationX(90);
                if (axis == Direction.Axis.X) builder.rotationY(90);
            }
            return builder.build();
        });
    }

    private void generateAncientOak() {
        ResourceLocation planks = elderOak("planks");
        ResourceLocation leaves = elderOak("leaves");
        ResourceLocation sapling = elderOak("sapling");

        List<String> bark = AncientOak.sideVariants(AncientOak.LOG_SIDE, AncientOak.LOG_SIDE_ALTS);
        variedPillar(AncientOak.block(AncientOak.LOG), bark, AncientOak.LOG_TOP);
        variedPillar(AncientOak.block(AncientOak.WOOD), bark, null);

        List<String> stripped = AncientOak.sideVariants(AncientOak.STRIPPED_LOG_SIDE, AncientOak.STRIPPED_LOG_SIDE_ALTS);
        variedPillar(AncientOak.block(AncientOak.STRIPPED_LOG), stripped, AncientOak.STRIPPED_LOG_TOP);
        variedPillar(AncientOak.block(AncientOak.STRIPPED_WOOD), stripped, null);

        simpleBlock(AncientOak.block(AncientOak.PLANKS), models().cubeAll(AncientOak.PLANKS, planks));
        stairsBlock((StairBlock) AncientOak.block(AncientOak.STAIRS), planks);
        slabBlock((SlabBlock) AncientOak.block(AncientOak.SLAB), modLoc("block/" + AncientOak.PLANKS), planks);
        fenceBlock((FenceBlock) AncientOak.block(AncientOak.FENCE), planks);
        fenceGateBlock((FenceGateBlock) AncientOak.block(AncientOak.FENCE_GATE), planks);
        buttonBlock((ButtonBlock) AncientOak.block(AncientOak.BUTTON), planks);
        pressurePlateBlock((PressurePlateBlock) AncientOak.block(AncientOak.PRESSURE_PLATE), planks);
        doorBlockWithRenderType((DoorBlock) AncientOak.block(AncientOak.DOOR), planks, planks, "cutout");
        trapdoorBlockWithRenderType((TrapDoorBlock) AncientOak.block(AncientOak.TRAPDOOR), planks, true, "cutout");

        simpleBlock(AncientOak.block(AncientOak.LEAVES), models()
                .withExistingParent(AncientOak.LEAVES, mcLoc("block/leaves"))
                .texture("all", leaves)
                .renderType("cutout_mipped"));
        simpleBlock(AncientOak.block(AncientOak.SAPLING), models()
                .cross(AncientOak.SAPLING, sapling)
                .renderType("cutout"));
    }

    private void generateIngredientCrop(BlockIngredient block) {
        Ingredients ingredient = block.getIngredient();

        if (PlantForm.of(ingredient) == PlantForm.BEET) {
            getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder()
                    .modelFile(beetModel(ingredient, state.getValue(block.getAgeProperty())))
                    .build());
            return;
        }

        getVariantBuilder(block).forAllStates(state -> {
            int age = state.getValue(block.getAgeProperty());
            if (block instanceof BlockIngredientBerry && age == block.getMaxAge() && !state.getValue(BlockIngredientBerry.FRUITING)) {
                age--;
            }
            boolean wild = state.hasProperty(BlockIngredientWild.WILD) && state.getValue(BlockIngredientWild.WILD);
            PlantGrowthTextures.Sprite sprite = PlantGrowthTextures.sprite(ingredient, age, block.getMaxAge(), wild);

            return ConfiguredModel.builder()
                    .modelFile(crossModel(blockPath(sprite.model()), texturePath(sprite.texture())))
                    .build();
        });
    }

    private ModelFile beetModel(Ingredients ingredient, int age) {
        ResourceLocation texture = PlantGrowthTextures.hasOwnStage(ingredient, age)
                ? texturePath(PlantGrowthTextures.ownStage(ingredient, age))
                : mcLoc("block/beetroots_stage" + age);
        String name = ingredient.name().toLowerCase() + "_beet_stage" + age;
        return crossModels.computeIfAbsent(new ResourceLocation(Alchemia.MODID, name),
                id -> models().crop(name, texture).renderType("cutout"));
    }

    private static final String[] PATCH_STAGES = {"_sprouts", "_young", "", "_bloom"};
    private static final String[][] PATCH_SLOTS = {
            {"base"},
            {"base", "top"},
            {"base", "top", "detail"},
            {"base", "top", "flower", "centre", "petal"}};
    private static final int GREENERY_STAGE = 2;

    private static String patchPart(String slot) {
        return switch (slot) {
            case "flower" -> "flower_overlay";
            case "centre" -> "flower_centre";
            default -> slot;
        };
    }

    private void generatePatch(BlockIngredientPatch block) {
        Ingredients ingredient = block.getIngredient();

        ModelFile[] models = new ModelFile[PATCH_STAGES.length];
        for (int age = 0; age < models.length; age++) {
            var builder = models().withExistingParent(ingredient.name().toLowerCase() + "_patch" + PATCH_STAGES[age],
                    modLoc("block/template_patch" + PATCH_STAGES[age])).renderType("cutout");
            for (String slot : PATCH_SLOTS[age]) {
                builder.texture(slot, texturePath(PlantGrowthTextures.part(ingredient, patchPart(slot)).texture()));
            }
            models[age] = builder;
        }

        int bloomAge = block.getMaxAge();
        Direction[] faces = BlockIngredientPatch.FACES;
        MultiPartBlockStateBuilder parts = getMultipartBuilder(block);

        for (int index = 0; index < faces.length; index++) {
            Direction face = faces[index];
            BooleanProperty present = BlockIngredientPatch.faceProperty(face);

            for (int age = 0; age < bloomAge; age++) {
                patchOnFace(parts, models[age], face)
                        .condition(present, true)
                        .condition(block.getAgeProperty(), age)
                        .end();
            }

            MultiPartBlockStateBuilder.PartBuilder blooms = patchOnFace(parts, models[bloomAge], face)
                    .condition(present, true)
                    .condition(block.getAgeProperty(), bloomAge);
            for (int earlier = 0; earlier < index; earlier++) {
                blooms.condition(BlockIngredientPatch.faceProperty(faces[earlier]), false);
            }
            blooms.end();

            if (index == 0) continue;
            MultiPartBlockStateBuilder.PartBuilder outranked = patchOnFace(parts, models[GREENERY_STAGE], face).useOr();
            for (int earlier = 0; earlier < index; earlier++) {
                outranked.nestedGroup()
                        .condition(present, true)
                        .condition(block.getAgeProperty(), bloomAge)
                        .condition(BlockIngredientPatch.faceProperty(faces[earlier]), true)
                        .end();
            }
            outranked.end();
        }
    }

    private static MultiPartBlockStateBuilder.PartBuilder patchOnFace(MultiPartBlockStateBuilder parts, ModelFile model, Direction face) {
        if (face == Direction.DOWN) {
            return parts.part()
                    .modelFile(model).nextModel()
                    .modelFile(model).rotationY(90).nextModel()
                    .modelFile(model).rotationY(180).nextModel()
                    .modelFile(model).rotationY(270).addModel();
        }

        int turn = switch (face) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
        int flipped = switch (face) {
            case EAST -> 270;
            case SOUTH -> 0;
            case WEST -> 90;
            default -> 180;
        };
        return parts.part()
                .modelFile(model).rotationX(270).rotationY(turn).nextModel()
                .modelFile(model).rotationX(90).rotationY(flipped).addModel();
    }

    private void generateShelf(BlockIngredientShelf block) {
        Ingredients ingredient = block.getIngredient();

        getVariantBuilder(block).forAllStates(state -> {
            PlantGrowthTextures.Sprite sprite = PlantGrowthTextures.sprite(ingredient, state.getValue(block.getAgeProperty()), block.getMaxAge(), false);
            ResourceLocation texture = texturePath(sprite.texture());
            ModelFile model = crossModels.computeIfAbsent(new ResourceLocation(Alchemia.MODID, sprite.model() + "_wall"),
                    id -> models().getBuilder(id.getPath())
                            .parent(new ModelFile.UncheckedModelFile(new ResourceLocation(Alchemia.MODID, "block/template_shelf")))
                            .texture("shelf", texture)
                            .texture("particle", texture)
                            .renderType("cutout"));

            return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationY(((int) BlockIngredientShelf.getFacing(state).toYRot() + 180) % 360)
                    .build();
        });
    }

    private void generatePart(Block block, Ingredients ingredient, String part) {
        PlantGrowthTextures.Sprite sprite = PlantGrowthTextures.part(ingredient, part);
        simpleBlock(block, crossModel(blockPath(sprite.model()), texturePath(sprite.texture())));
    }

    private void generateInfectedLog(BlockInfectedLog block) {
        String wood = block.getWood().name();
        String own = PlantGrowthTextures.ownTexture(block.getFungus(), "infected_" + wood + "_log");
        String ownTop = PlantGrowthTextures.ownTexture(block.getFungus(), "infected_" + wood + "_log_top");

        ResourceLocation side = own != null ? texturePath(own) : new ResourceLocation(block.getWood().sideTexture());
        ResourceLocation end = ownTop != null ? texturePath(ownTop) : new ResourceLocation(block.getWood().endTexture());

        axisBlock(block, side, end);
    }

    private void generateCluster(BlockMineralClusterGeneric cluster) {
        ModelFile model = crossModel(blockPath(clusterName(cluster)), clusterTexture(cluster));
        directionalBlock(cluster, model);
    }

    private ModelFile crossModel(String name, ResourceLocation texture) {
        ResourceLocation key = new ResourceLocation(Alchemia.MODID, name);
        return crossModels.computeIfAbsent(key, id -> models().cross(name, texture).renderType("cutout"));
    }

    private void generateMineralCube(Block block, Ingredients ingredient) {
        cubeAll(block, texturePath("minerals/" + ingredient.name().toLowerCase() + "/" + blockName(block)));
    }

    private void generateAlchemyCube(Block block) {
        cubeAll(block, texturePath("alchemy/" + blockName(block)));
    }

    private void cubeAll(Block block, ResourceLocation texture) {
        ModelFile model = models().cubeAll(blockName(block), texture);
        simpleBlock(block, model);
    }

    private static String clusterName(BlockMineralClusterGeneric cluster) {
        String suffix = cluster.getSize() == BlockRegistry.BudSize.CLUSTER
                ? "_cluster"
                : "_bud_" + cluster.getSize().name().toLowerCase();
        return cluster.getIngredient().name().toLowerCase() + suffix;
    }

    private static ResourceLocation clusterTexture(BlockMineralClusterGeneric cluster) {
        Ingredients ingredient = cluster.getIngredient();
        return texturePath(ingredient.getType().name().toLowerCase() + "s/" + ingredient.name().toLowerCase()
                + "/growth_stage_" + cluster.getSize().ordinal());
    }

    private static String blockName(Block block) {
        return BlockRegistry.BLOCK_REGISTRY.entrySet().stream()
                .filter(entry -> entry.getValue().get() == block)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unregistered block " + block));
    }

    private static String blockPath(String path) {
        return path;
    }

    private static ResourceLocation texturePath(String path) {
        return new ResourceLocation(Alchemia.MODID, "block/" + path);
    }
}
