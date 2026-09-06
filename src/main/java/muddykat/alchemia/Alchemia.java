package muddykat.alchemia;

import com.mojang.logging.LogUtils;
import muddykat.alchemia.common.CommonSetup;
import muddykat.alchemia.common.WorldEventHandler;
import muddykat.alchemia.common.config.Configuration;
import muddykat.alchemia.registration.registers.IngredientTypeRegistry;
import muddykat.alchemia.common.network.NetworkHandler;
import muddykat.alchemia.common.network.packets.PacketPotionRecipe;
import muddykat.alchemia.common.potion.PotionMap;
import muddykat.alchemia.registration.AlchemiaRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.slf4j.Logger;

import java.util.Random;

@Mod(Alchemia.MODID)
public class Alchemia {
    public static final String MODID = "alchemia";
    public static final String MOD_NAME = "Alchemia";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Alchemia(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("|-=-=-=-=-=-=-=-=-=-=-=-=-=-|");
        LOGGER.info(MOD_NAME + " Setup Phase");

        modEventBus.addListener(CommonSetup::init);

        LOGGER.info(MOD_NAME + " Initializing Configuration");
        modContainer.registerConfig(ModConfig.Type.COMMON, Configuration.COMMON_CONFIG);
        modContainer.registerConfig(ModConfig.Type.SERVER, Configuration.SERVER_CONFIG);

        AlchemiaRegistry.initialize();
        AlchemiaRegistry.register(modEventBus);

        LOGGER.info(MOD_NAME + " Setting up Networking");
        modEventBus.addListener(NetworkHandler::register);

        LOGGER.info(MOD_NAME + " Registering Event Handlers");
        NeoForge.EVENT_BUS.register(WorldEventHandler.class);
        NeoForge.EVENT_BUS.register(muddykat.alchemia.common.PhilosopherStoneHandler.class);
        NeoForge.EVENT_BUS.register(this);

        LOGGER.info(MOD_NAME + " Setup Complete");
        LOGGER.info("|-=-=-=-=-=-=-=-=-=-=-=-=-=-|");
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Random rand = new Random(player.level().getSeed());
            long map_seed = rand.nextLong();
            LOGGER.info("Player Joined - Sending Potion Map Seed");
            NetworkHandler.sendToPlayer(new PacketPotionRecipe(map_seed), player);
            PotionMap.scramble(map_seed);
        }
    }
}
