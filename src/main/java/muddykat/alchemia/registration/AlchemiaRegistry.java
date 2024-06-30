package muddykat.alchemia.registration;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.registration.registers.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static muddykat.alchemia.Alchemia.MOD_NAME;

public class AlchemiaRegistry {
    public static void initialize() {
        Alchemia.LOGGER.info(MOD_NAME + "Initializing Registration Entries");
        ItemRegistry.initialize();
        BlockRegistry.initialize();
    }

    public static void register() {
        Alchemia.LOGGER.info(MOD_NAME + "Registration Phase");
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        BlockRegistry.BLOCKS.register(modEventBus);
        ItemRegistry.ITEMS.register(modEventBus);
        BiomeFeatureRegistry.FEATURES.register(modEventBus);
        PlacementModifierRegistry.PLACEMENT_MODIFIERS.register(modEventBus);
        BlockEntityTypeRegistry.TILES.register(modEventBus);
        MenuTypeRegistry.MENU_TYPES.register(modEventBus);
        Alchemia.LOGGER.info(MOD_NAME + "Registration Complete");
    }
}
