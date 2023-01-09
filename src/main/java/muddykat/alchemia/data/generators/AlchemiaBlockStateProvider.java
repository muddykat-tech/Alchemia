package muddykat.alchemia.data.generators;

import com.mojang.logging.LogUtils;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.BlockIngredient;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.registration.registers.BlockRegister;
import muddykat.alchemia.registration.registers.ItemRegister;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;


public class AlchemiaBlockStateProvider extends BlockStateProvider {
    private static final Logger LOGGER = LogUtils.getLogger();

    public AlchemiaBlockStateProvider(DataGenerator gen, ExistingFileHelper exHelper){
        super(gen, Alchemia.MODID, exHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        LOGGER.info("Starting Block Model Registration");
        for (RegistryObject<Block> registryObject : BlockRegister.BLOCK_REGISTRY.values()){
            Block block = registryObject.get();
            if(block instanceof BlockIngredient) {
                BlockIngredient ingredient = (BlockIngredient) block;
                generateGenericBlockIngredient(ingredient);
            }
        }
    }

    private void generateGenericBlockIngredient(BlockIngredient block){
        this.stageBlock(block,  block.getAgeProperty());
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
}
