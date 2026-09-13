package muddykat.alchemia.data;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.world.WildHerbGeneration;
import muddykat.alchemia.data.generators.AlchemiaModelProvider;
import muddykat.alchemia.data.generators.AlchemiaAdvancementProvider;
import muddykat.alchemia.data.generators.AlchemiaBlockTagsProvider;
import muddykat.alchemia.data.generators.AlchemiaItemTagsProvider;
import muddykat.alchemia.data.generators.AlchemiaRecipeProvider;
import muddykat.alchemia.data.generators.loot.AlchemiaLootProvider;
import muddykat.alchemia.data.worldgen.AlchemiaBiomeModifiers;
import muddykat.alchemia.data.worldgen.AlchemiaBiomeTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = Alchemia.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataProvider {

    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
        event.createProvider(AlchemiaModelProvider::new);
    }

    @SubscribeEvent
    public static void gatherServerData(GatherDataEvent.Server event) {
        event.createProvider(AlchemiaBlockTagsProvider::new);
        event.createProvider(AlchemiaItemTagsProvider::new);
        event.createProvider((PackOutput out, CompletableFuture<HolderLookup.Provider> reg) ->
                new net.minecraft.data.advancements.AdvancementProvider(out, reg, List.of(new AlchemiaAdvancementProvider())));
        event.createProvider(AlchemiaRecipeProvider.Runner::new);
        event.createProvider(AlchemiaLootProvider::new);
        event.createProvider(AlchemiaBiomeTagsProvider::new);

        event.createDatapackRegistryObjects(new RegistrySetBuilder()
                .add(Registries.CONFIGURED_FEATURE, WildHerbGeneration::bootstrapConfiguredFeatures)
                .add(Registries.PLACED_FEATURE, WildHerbGeneration::bootstrapPlacedFeatures)
                .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, AlchemiaBiomeModifiers::bootstrap));
    }
}
