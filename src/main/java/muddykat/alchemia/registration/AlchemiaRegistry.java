package muddykat.alchemia.registration;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.registration.registers.*;
import net.neoforged.bus.api.IEventBus;

import static muddykat.alchemia.Alchemia.MOD_NAME;

public class AlchemiaRegistry {
    public static void initialize() {
        Alchemia.LOGGER.info(MOD_NAME + " Initializing Registration Entries");
        ItemRegistry.initialize();
        BlockRegistry.initialize();
    }

    public static void register(IEventBus modEventBus) {
        Alchemia.LOGGER.info(MOD_NAME + " Registration Phase");
        BlockRegistry.BLOCKS.register(modEventBus);
        ItemRegistry.ITEMS.register(modEventBus);
        DataComponentRegistry.DATA_COMPONENTS.register(modEventBus);
        BiomeFeatureRegistry.FEATURES.register(modEventBus);
        PlacementModifierRegistry.PLACEMENT_MODIFIERS.register(modEventBus);
        BlockEntityTypeRegistry.TILES.register(modEventBus);
        MenuTypeRegistry.MENU_TYPES.register(modEventBus);
        CreativeTabRegistry.CREATIVE_MODE_TABS.register(modEventBus);
        IngredientTypeRegistry.INGREDIENT_TYPES.register(modEventBus);
        Alchemia.LOGGER.info(MOD_NAME + " Registration Complete");
    }
}
