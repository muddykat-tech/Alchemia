package muddykat.alchemia;

import com.mojang.logging.LogUtils;
import muddykat.alchemia.client.ClientSetup;
import muddykat.alchemia.registration.AlchemiaRegistry;
import muddykat.alchemia.registration.registers.BlockRegister;
import muddykat.alchemia.registration.registers.ItemRegister;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import static muddykat.alchemia.Alchemia.MODID;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MODID)
public class Alchemia
{
    public static final String MODID = "alchemia";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Alchemia()
    {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        LOGGER.info(MODID + ": Setting up Item and Block Registration");
        AlchemiaRegistry.initialize();

        BlockRegister.getRegistry().register(modEventBus);
        ItemRegister.getRegistry().register(modEventBus);

        LOGGER.info(MODID + ": Initializing Clientside Settings");
        modEventBus.addListener(ClientSetup::init);

        LOGGER.info(MODID + ": Completed");
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }




    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents
    {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent)
        {
            // Register a new block here

        }
    }
}
