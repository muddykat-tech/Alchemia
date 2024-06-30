package muddykat.alchemia.common;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.client.ClientSetup;
import muddykat.alchemia.common.world.WildHerbGeneration;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import static muddykat.alchemia.common.world.WildHerbGeneration.registerWildHerbGeneration;

public class CommonSetup {

    public static void init(final FMLCommonSetupEvent event) {
        event.enqueueWork(WildHerbGeneration::registerWildHerbGeneration);
    }
}
