package muddykat.alchemia.registration;

import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyCauldron;
import muddykat.alchemia.common.blocks.BlockGeneric;
import muddykat.alchemia.common.blocks.blockentity.EntityBlockGeneric;
import muddykat.alchemia.common.items.BlockItemGeneric;
import muddykat.alchemia.common.items.ItemAlchemiaGuide;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.items.AlchemicalPotion;
import muddykat.alchemia.registration.registers.BlockRegister;
import muddykat.alchemia.registration.registers.ItemRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;

public class AlchemiaRegistry {
    public static void initialize() {
        for (Ingredients ingredient : Ingredients.values()) {
            ingredient.register();
        }

        createBasicBlock("deepmetal_tile");
        createBlockEntity(BlockAlchemyCauldron.class, "alchemical_cauldron");

        ItemRegister.registerItem("alchemia_guide", ItemAlchemiaGuide::new);
        ItemRegister.registerItem("alchemical_potion", AlchemicalPotion::new);
    }
    public static void createBasicBlock(String id){
        BlockRegister.registerBlock(id, BlockGeneric::new);
        ItemRegister.registerItem(id, () -> new BlockItemGeneric(BlockRegister.BLOCK_REGISTRY.get(id).get()));
    }

    public static void createBlockEntity(Class<? extends EntityBlockGeneric> generic, String id) {
        BlockRegister.registerBlock(id, () -> {
            try {
                return generic.getDeclaredConstructor().newInstance();
            } catch (RuntimeException | InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException error) {
                Logger log =  LogManager.getLogger();
                log.info(error.getMessage());
            }
            return null;
        });
        ItemRegister.registerItem(id, () -> new BlockItemGeneric(BlockRegister.BLOCK_REGISTRY.get(id).get()));
    }
}
