package muddykat.alchemia.common;

import muddykat.alchemia.client.ClientSetup;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static muddykat.alchemia.common.world.WildHerbGeneration.registerWildHerbGeneration;

public class CommonProxy extends ClientSetup {

    public static void init(final FMLClientSetupEvent event) {
        registerWildHerbGeneration();
    }
}
