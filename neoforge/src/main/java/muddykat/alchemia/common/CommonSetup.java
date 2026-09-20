package muddykat.alchemia.common;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.config.BrewBaseConfig;
import muddykat.alchemia.common.potion.PotionMap;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import static muddykat.alchemia.Alchemia.MOD_NAME;

public class CommonSetup {

    public static void init(final FMLCommonSetupEvent event) {
        Alchemia.LOGGER.info(MOD_NAME + " Common Setup");
        event.enqueueWork(() -> {
            BrewBaseConfig.load();
            PotionMap.scramble(0L);
        });
    }
}
