package muddykat.alchemia;

import com.blamejared.crafttweaker.impl.network.PacketHandler;
import com.mojang.logging.LogUtils;
import muddykat.alchemia.client.ClientSetup;
import muddykat.alchemia.client.ClientSetupEvents;
import muddykat.alchemia.common.WorldEventHandler;
import muddykat.alchemia.common.network.NetworkHandler;
import muddykat.alchemia.common.network.packets.PacketPotionRecipe;
import muddykat.alchemia.common.potion.PotionMap;
import muddykat.alchemia.registration.AlchemiaRegistry;
import muddykat.alchemia.registration.registers.BlockEntityTypeRegistry;
import muddykat.alchemia.registration.registers.BlockRegister;
import muddykat.alchemia.registration.registers.ItemRegister;
import muddykat.alchemia.registration.registers.MenuTypeRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.jfr.event.WorldLoadFinishedEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import javax.annotation.Nonnull;

import java.util.Objects;
import java.util.Random;

import static muddykat.alchemia.Alchemia.MODID;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MODID)
public class Alchemia
{
    public static final String MODID = "alchemia";
    public static final Logger LOGGER = LogUtils.getLogger();
    public Alchemia()
    {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        LOGGER.info(MODID + ": Setting up Item and Block Registration");
        AlchemiaRegistry.initialize();

        BlockRegister.getRegistry().register(modEventBus);
        ItemRegister.getRegistry().register(modEventBus);

        BlockEntityTypeRegistry.TILES.register(modEventBus);
        MenuTypeRegistry.MENU_TYPES.register(modEventBus);

        LOGGER.info(MODID + ": Initializing Clientside Settings");
        modEventBus.addListener(ClientSetup::init);

        NetworkHandler.register();

        MinecraftForge.EVENT_BUS.register(WorldEventHandler.class);

        LOGGER.info(MODID + ": Completed");
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(PlayerEvent.PlayerLoggedInEvent event) {
        MinecraftServer server = event.getEntity().getServer();
        if(server != null) {
            if (event.getPlayer() instanceof ServerPlayer player) {
                Random rand = new Random(server.getWorldData().worldGenSettings().seed());
                long map_seed = rand.nextLong();
                LOGGER.info("Player Joined - Sending Potion Map Seed [" + map_seed + "]");
                NetworkHandler.sendToPlayer(new PacketPotionRecipe(map_seed), player);
            }
        }
    }

    public static final CreativeModeTab ITEM_GROUP = new CreativeModeTab(MODID)
    {
        @Override
        @Nonnull
        public ItemStack makeIcon()
        {
            return new ItemStack(BlockRegister.BLOCK_REGISTRY.get("alchemical_cauldron").get());
        }
    };
}
