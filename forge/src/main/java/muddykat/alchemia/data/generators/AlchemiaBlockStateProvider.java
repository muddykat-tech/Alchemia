package muddykat.alchemia.data.generators;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.BlockGeneric;
import muddykat.alchemia.common.blocks.BlockIngredient;
import muddykat.alchemia.common.blocks.BlockMineralBuddingGeneric;
import muddykat.alchemia.common.blocks.BlockMineralClusterGeneric;
import muddykat.alchemia.common.blocks.BlockMineralGeneric;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.registration.registers.BlockRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.HashMap;
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

            if (block instanceof BlockIngredient ingredient) {
                generateIngredientCrop(ingredient);
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
    }

    private void generateIngredientCrop(BlockIngredient block) {
        String type = block.getIngredientType().name().toLowerCase();
        String name = block.getIngredient().name().toLowerCase();

        getVariantBuilder(block).forAllStates(state -> {
            int age = state.getValue(block.getAgeProperty());

            ModelFile model;
            if (age == block.getMaxAge()) {
                model = crossModel(blockPath(name), texturePath(type + "s/" + name));
            } else {
                String stage = "growth/" + type + "/growth_stage_" + age;
                model = crossModel(blockPath(stage), texturePath(stage));
            }
            return ConfiguredModel.builder().modelFile(model).build();
        });
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
