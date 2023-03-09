package muddykat.alchemia.data.generators;

import com.mojang.logging.LogUtils;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.items.BlockItemGeneric;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.ItemIngredientSeed;
import muddykat.alchemia.registration.registers.ItemRegister;
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
        for (RegistryObject<Item> registryObject : ItemRegister.ITEM_REGISTRY.values()){
            Item item = registryObject.get();
            if(item instanceof ItemIngredientSeed ingredient) {
                generateGenericIngredientSeed(ingredient);
            }

            if(item instanceof ItemIngredient ingredient) {
                generateGenericIngredient(ingredient);
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
