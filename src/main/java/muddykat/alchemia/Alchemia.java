package muddykat.alchemia;

import com.mojang.logging.LogUtils;
import muddykat.alchemia.client.ClientSetup;
import muddykat.alchemia.common.CommonSetup;
import muddykat.alchemia.common.WorldEventHandler;
import muddykat.alchemia.common.config.Configuration;
import muddykat.alchemia.common.network.NetworkHandler;
import muddykat.alchemia.common.network.packets.PacketPotionRecipe;
import muddykat.alchemia.common.potion.PotionMap;
import muddykat.alchemia.proxy.ISidedProxy;
import muddykat.alchemia.registration.AlchemiaRegistry;
import muddykat.alchemia.registration.registers.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import javax.annotation.Nonnull;

import java.util.Random;

import static muddykat.alchemia.Alchemia.MODID;
import muddykat.alchemia.proxy.*;
// The value here should match an entry in the META-INF/mods.toml file
@Mod(MODID)
public class Alchemia
{
    public static final String MODID = "alchemia";
    public static final String MOD_NAME = "Alchemia";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ISidedProxy proxy = DistExecutor.unsafeRunForDist(() -> ClientProxy::new, ()-> ServerProxy::new);

    public Alchemia()
    {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        LOGGER.info("|-=-=-=-=-=-=-=-=-=-=-=-=-=-|");
        LOGGER.info(MOD_NAME + " Setup Phase");
        modEventBus.addListener(CommonSetup::init);
        modEventBus.addListener(ClientSetup::init);

        proxy.init();

        LOGGER.info(MOD_NAME + " Initializing Configuration");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configuration.COMMON_CONFIG);

        AlchemiaRegistry.initialize();
        AlchemiaRegistry.register();

        LOGGER.info(MOD_NAME + " Setting up Networking");

        NetworkHandler.register();

        LOGGER.info(MOD_NAME + " Registering Event Handlers");

        MinecraftForge.EVENT_BUS.register(WorldEventHandler.class);
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info(MOD_NAME + " Setup Complete");
        LOGGER.info("|-=-=-=-=-=-=-=-=-=-=-=-=-=-|");
    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(PlayerEvent.PlayerLoggedInEvent event) {
        MinecraftServer server = event.getEntity().getServer();
        if(server != null) {
            if (event.getPlayer() instanceof ServerPlayer player) {
                Random rand = new Random(server.getWorldData().worldGenSettings().seed());
                long map_seed = rand.nextLong();
                LOGGER.info("Player Joined - Sending Potion Map Seed");
                NetworkHandler.sendToPlayer(new PacketPotionRecipe(map_seed), player);
                PotionMap.scramble(map_seed);
            }
        }
    }

    public static final CreativeModeTab ITEM_GROUP = new CreativeModeTab(MODID)
    {
        @Override
        @Nonnull
        public ItemStack makeIcon()
        {
            return new ItemStack(BlockRegistry.BLOCK_REGISTRY.get("alchemical_cauldron").get());
        }
    };
}
