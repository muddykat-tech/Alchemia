package muddykat.alchemia;

import com.mojang.logging.LogUtils;
import muddykat.alchemia.common.CommonSetup;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import muddykat.alchemia.common.WorldEventHandler;
import muddykat.alchemia.common.config.Configuration;
import muddykat.alchemia.registration.registers.IngredientTypeRegistry;
import muddykat.alchemia.common.network.NetworkHandler;
import muddykat.alchemia.common.network.packets.PacketBrewBases;
import muddykat.alchemia.common.network.packets.PacketPotionRecipe;
import muddykat.alchemia.common.potion.BrewBases;
import muddykat.alchemia.common.potion.PotionMap;
import muddykat.alchemia.registration.AlchemiaRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.slf4j.Logger;

import java.util.Random;

@Mod(Alchemia.MODID)
public class Alchemia {
    public static final String MODID = "alchemia";
    public static final String MOD_NAME = "Alchemia";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Alchemia() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        LOGGER.info("|-=-=-=-=-=-=-=-=-=-=-=-=-=-|");
        LOGGER.info(MOD_NAME + " Setup Phase");

        modEventBus.addListener(CommonSetup::init);
        modEventBus.addListener(Alchemia::onConfigChanged);

        LOGGER.info(MOD_NAME + " Initializing Configuration");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configuration.COMMON_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Configuration.SERVER_CONFIG);

        AlchemiaRegistry.initialize();
        AlchemiaRegistry.register(modEventBus);

        LOGGER.info(MOD_NAME + " Setting up Networking");
        modEventBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(NetworkHandler::register));

        LOGGER.info(MOD_NAME + " Registering Event Handlers");
        MinecraftForge.EVENT_BUS.register(WorldEventHandler.class);
        MinecraftForge.EVENT_BUS.register(muddykat.alchemia.common.PhilosopherStoneHandler.class);
        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info(MOD_NAME + " Setup Complete");
        LOGGER.info("|-=-=-=-=-=-=-=-=-=-=-=-=-=-|");
    }

    private static void onConfigChanged(ModConfigEvent event) {
        if (event.getConfig().getSpec() != Configuration.SERVER_CONFIG) return;
        PotionMap.rebuild();
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Random rand = new Random(player.level().getSeed());
            long map_seed = rand.nextLong();
            LOGGER.info("Player Joined - Sending Brew Bases and Potion Map Seed");
            NetworkHandler.sendToPlayer(new PacketBrewBases(BrewBases.definitions()), player);
            NetworkHandler.sendToPlayer(new PacketPotionRecipe(map_seed), player);
            PotionMap.scramble(map_seed);
        }
    }
}
