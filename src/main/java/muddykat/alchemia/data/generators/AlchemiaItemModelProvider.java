package muddykat.alchemia.data.generators;

import com.mojang.logging.LogUtils;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.BlockMineralClusterGeneric;
import muddykat.alchemia.common.items.*;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.registration.registers.BlockRegistry;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;


public class AlchemiaItemModelProvider extends ItemModelProvider {
    private static final Logger LOGGER = LogUtils.getLogger();

    public AlchemiaItemModelProvider(DataGenerator gen, ExistingFileHelper exHelper){
        super(gen, Alchemia.MODID, exHelper);
    }

    @Override
    protected void registerModels() {
        LOGGER.info("Starting Item Model Registration");
        for (RegistryObject<Item> registryObject : ItemRegistry.ITEM_REGISTRY.values()){
            Item item = registryObject.get();

            if(item instanceof ItemIngredientSeed seed) {
                generateGenericIngredientSeed(seed);
            }

            if(item instanceof ItemIngredient ingredient) {
                if(item instanceof ItemIngredientCrushed crushed)
                {
                    generateGenericIngredientCrushed(crushed);
                    continue;
                }
                generateGenericIngredient(ingredient);
            }

            if(item instanceof BlockItemGeneric blockItem)
            {
                if(blockItem.getBlock() instanceof BlockMineralClusterGeneric cluster){
                    Ingredients ingredient = cluster.getIngredient();
                    String stageName = Alchemia.MODID + ":block/" + ingredient.getType().name().toLowerCase() + "s/" + ingredient.name().toLowerCase() + "/growth_stage_" + cluster.getSize().ordinal();

                    withExistingParent(new ResourceLocation(Alchemia.MODID, ingredient.name().toLowerCase() + "_" + ((cluster.getSize() != BlockRegistry.BudSize.CLUSTER) ? "bud_" : "") + cluster.getSize().name().toLowerCase()).getPath(),
                            new ResourceLocation("item/generated"))
                            .texture("layer0", stageName);
                }
            }
        }
    }

    private void generateGenericIngredientSeed(ItemIngredientSeed item){
        String item_loc = new ResourceLocation(Alchemia.MODID, "item/" + item.getIngredientType().name().toLowerCase() + "_" + item.getIngredient().name().toLowerCase() + "_seed").getPath();
        try {
            withExistingParent(item_loc,
                    new ResourceLocation("item/generated"))
                    .texture("layer0", item.getTextureLocation());
        } catch (Exception ex){
            LOGGER.error("Attempted generation of textures for Item: " + item.getRegistryName().getPath() + " received error: " + ex.getMessage());
        }
    }

    private void generateGenericIngredientCrushed(ItemIngredientCrushed item){
        String item_loc = new ResourceLocation(Alchemia.MODID, "item/" + item.getIngredientType().name().toLowerCase() + "_" + item.getIngredient().name().toLowerCase() + "_crushed").getPath();
        try {
            withExistingParent(item_loc,
                    new ResourceLocation("item/generated"))
                    .texture("layer0", item.getTextureLocation());
        } catch (Exception ex){
            LOGGER.error("Attempted generation of textures for Item: " + item.getRegistryName().getPath() + " received error: " + ex.getMessage());
        }
    }

    private void generateGenericIngredient(ItemIngredient item){
        String item_loc = new ResourceLocation(Alchemia.MODID, "item/" + item.getIngredientType().name().toLowerCase() + "_" + item.getIngredient().name().toLowerCase()).getPath();
        try {
            withExistingParent(item_loc,
                    new ResourceLocation("item/generated"))
                    .texture("layer0", item.getTextureLocation());
        } catch (Exception ex){
            LOGGER.error("Attempted generation of textures for Item: " + item.getRegistryName().getPath() + " received error: " + ex.getMessage());
        }
    }
}
