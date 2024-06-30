package muddykat.alchemia.data.generators;

import com.mojang.logging.LogUtils;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.*;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.registration.registers.BlockRegistry;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.function.Supplier;


public class AlchemiaBlockStateProvider extends BlockStateProvider {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int DEFAULT_ANGLE_OFFSET = 180;

    public AlchemiaBlockStateProvider(DataGenerator gen, ExistingFileHelper exHelper){
        super(gen, Alchemia.MODID, exHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        LOGGER.info("Starting Block Model Registration");
        for (RegistryObject<Block> registryObject : BlockRegistry.BLOCK_REGISTRY.values()){
            Block block = registryObject.get();
            if(block instanceof BlockIngredient ingredient) {
                generateGenericBlockIngredient(ingredient);
            }
            if(block instanceof BlockGeneric) {
                if(block instanceof BlockMineralBuddingGeneric mineralBuddingGeneric)
                {
                    generateGenericMineralBlock(registryObject, mineralBuddingGeneric.getIngredient());
                    continue;
                }
                generateGenericBlock(registryObject);
            }
            if(block instanceof BlockMineralGeneric mineralGeneric)
            {
                Ingredients ingredient = mineralGeneric.getIngredient();
                generateGenericMineralBlock(registryObject, ingredient);
            }
            if(block instanceof BlockMineralClusterGeneric clusterGeneric)
            {
                Ingredients ingredient = clusterGeneric.getIngredient();
                generateClusterBlock(clusterGeneric, ingredient);
            }
        }
    }

    private void generateGenericBlockIngredient(BlockIngredient block){
        this.stageBlock(block,  block.getAgeProperty());
    }

    private void generateGenericBlock(Supplier<Block> block){
        cubeAll(block, resourceBlock("alchemy/" + blockName(block.get())));
    }

    private void generateGenericMineralBlock(Supplier<Block> block, Ingredients ingredient)
    {
        cubeAll(block, resourceBlock("minerals/" + ingredient.name().toLowerCase() + "/" + blockName(block.get())));
    }

    public ModelFile existingModel(Block block) {
        return new ModelFile.ExistingModelFile(resourceBlock(blockName(block)), models().existingFileHelper);
    }

    public ModelFile existingModel(String path) {
        return new ModelFile.ExistingModelFile(resourceBlock(path), models().existingFileHelper);
    }

    private String blockName(Block block) {
        return block.getRegistryName().getPath();
    }

    public ResourceLocation resourceBlock(String path) {
        return new ResourceLocation(Alchemia.MODID, "block/" + path);
    }

    public void stageBlock(BlockIngredient block, IntegerProperty ageProperty, Property<?>... ignored) {
        getVariantBuilder(block)
                .forAllStatesExcept(state -> {
                    int ageSuffix = state.getValue(ageProperty);
                    String stageName = "growth/" + block.getIngredientType().name().toLowerCase() + "/growth_stage_" + ageSuffix;

                    if(ageSuffix == block.getMaxAge()){
                        return ConfiguredModel.builder()
                                .modelFile(models().cross(block.getIngredient().name().toLowerCase(), resourceBlock(block.getIngredientType().name().toLowerCase() + "s/" + block.getIngredient().name().toLowerCase()))).build();
                    }

                    return ConfiguredModel.builder()
                            .modelFile(models().cross(stageName, resourceBlock(stageName))).build();
                }, ignored);
    }

    public void generateClusterBlock(BlockMineralClusterGeneric cluster, Ingredients ingredient)
    {
        String stageName = "block/" + ingredient.getType().name().toLowerCase() + "s/" + ingredient.name().toLowerCase() + "/growth_stage_" + cluster.getSize().ordinal();

        String name = new ResourceLocation(Alchemia.MODID, ingredient.name().toLowerCase() + "_" + ((cluster.getSize() != BlockRegistry.BudSize.CLUSTER) ? "bud_" : "") + cluster.getSize().name().toLowerCase()).getPath();

        getVariantBuilder(cluster)
                .forAllStates(state -> {
                    Direction dir = state.getValue(BlockStateProperties.FACING);

                    return ConfiguredModel.builder()
                            .modelFile(models().cross(name, new ResourceLocation(Alchemia.MODID, stageName))).rotationX(dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? 90 : 0)
                            .rotationY(dir.getAxis().isVertical() ? 0 : ((int) dir.toYRot() + DEFAULT_ANGLE_OFFSET) % 360).build();
                });
    }

    public void simpleBlockAndItem(Supplier<? extends Block> b, ModelFile model)
    {
        simpleBlockAndItem(b, new ConfiguredModel(model));
    }

    protected void simpleBlockAndItem(Supplier<? extends Block> b, ConfiguredModel model)
    {
        simpleBlock(b.get(), model);
        itemModel(b, model.model);
    }

    protected void cubeSideVertical(Supplier<? extends Block> b, ResourceLocation side, ResourceLocation vertical)
    {
        simpleBlockAndItem(b, models().cubeBottomTop(name(b), side, vertical, vertical));
    }

    protected void cubeAll(Supplier<? extends Block> b, ResourceLocation texture)
    {
        simpleBlockAndItem(b, models().cubeAll(name(b), texture));
    }

    protected void itemModel(Supplier<? extends Block> block, ModelFile model)
    {
        itemModels().getBuilder(name(block)).parent(model);
    }
    protected String name(Supplier<? extends Block> b)
    {
        return name(b.get());
    }

    protected String name(Block b)
    {
        return b.getRegistryName().getPath();
    }
}
