package muddykat.alchemia.data;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.world.WildHerbGeneration;
import muddykat.alchemia.data.generators.AlchemiaModelProvider;
import muddykat.alchemia.data.generators.AlchemiaRecipeProvider;
import muddykat.alchemia.data.generators.loot.AlchemiaLootProvider;
import muddykat.alchemia.data.worldgen.AlchemiaBiomeModifiers;
import muddykat.alchemia.data.worldgen.AlchemiaBiomeTagsProvider;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@EventBusSubscriber(modid = Alchemia.MODID)
public class DataProvider {

    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
        event.createProvider(AlchemiaModelProvider::new);
    }

    @SubscribeEvent
    public static void gatherServerData(GatherDataEvent.Server event) {
        event.createProvider(AlchemiaRecipeProvider.Runner::new);
        event.createProvider(AlchemiaLootProvider::new);
        event.createProvider(AlchemiaBiomeTagsProvider::new);

        event.createDatapackRegistryObjects(new RegistrySetBuilder()
                .add(Registries.CONFIGURED_FEATURE, WildHerbGeneration::bootstrapConfiguredFeatures)
                .add(Registries.PLACED_FEATURE, WildHerbGeneration::bootstrapPlacedFeatures)
                .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, AlchemiaBiomeModifiers::bootstrap));
    }
}
