package muddykat.alchemia.common.crafting;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.config.Configuration;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.util.List;

public final class GreatWorkSetup {

    private GreatWorkSetup() {
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MagnumOpus.install(resolve(event.getServer()));
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        MagnumOpus.install(MagnumOpus.CLASSIC);
    }

    public static List<MagnumOpus.Stage> resolve(MinecraftServer server) {
        if (!Configuration.greatWorkSeeded()) return MagnumOpus.CLASSIC;

        ServerLevel overworld = server.overworld();
        GreatWorkData data = overworld.getDataStorage().computeIfAbsent(GreatWorkData.TYPE);
        if (!data.holds()) {
            data.store(GreatWorkGenerator.generate(overworld.getSeed()));
            Alchemia.LOGGER.info("Wrote the Great Work for this world from its seed");
        }
        return data.stages();
    }
}
