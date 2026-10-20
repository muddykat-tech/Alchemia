package muddykat.alchemia.data;

import muddykat.alchemia.data.generators.PlacedPotionModelProvider;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.world.WildHerbGeneration;
import muddykat.alchemia.data.generators.AlchemiaAdvancementProvider;
import muddykat.alchemia.data.generators.AlchemiaBlockStateProvider;
import muddykat.alchemia.data.generators.AlchemiaBlockTagsProvider;
import muddykat.alchemia.data.generators.AlchemiaItemModelProvider;
import muddykat.alchemia.data.generators.AlchemiaItemTagsProvider;
import muddykat.alchemia.data.generators.AlchemiaRecipeProvider;
import muddykat.alchemia.data.generators.loot.AlchemiaLootProvider;
import muddykat.alchemia.data.worldgen.AlchemiaBiomeModifiers;
import muddykat.alchemia.data.worldgen.AlchemiaBiomeTagsProvider;
import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.minecraft.core.registries.Registries;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = Alchemia.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DataProvider {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();
        ExistingFileHelper helper = event.getExistingFileHelper();

        boolean client = event.includeClient();
        boolean server = event.includeServer();

        generator.addProvider(client, new AlchemiaBlockStateProvider(output, helper));
        generator.addProvider(client, new AlchemiaItemModelProvider(output, helper));
        generator.addProvider(client, new PlacedPotionModelProvider(output, "minecraft:translucent"));

        AlchemiaBlockTagsProvider blockTags = new AlchemiaBlockTagsProvider(output, lookup, helper);
        generator.addProvider(server, blockTags);
        generator.addProvider(server, new AlchemiaItemTagsProvider(output, lookup, helper));
        generator.addProvider(server, new AlchemiaBiomeTagsProvider(output, lookup, helper));
        generator.addProvider(server, new AlchemiaRecipeProvider(output, lookup));
        generator.addProvider(server, new AlchemiaLootProvider(output, lookup));
        generator.addProvider(server, new net.minecraft.data.advancements.AdvancementProvider(output, lookup,
                List.of(new AlchemiaAdvancementProvider())));

        generator.addProvider(server, new DatapackBuiltinEntriesProvider(output, lookup,
                new RegistrySetBuilder()
                        .add(Registries.CONFIGURED_FEATURE, WildHerbGeneration::bootstrapConfiguredFeatures)
                        .add(Registries.PLACED_FEATURE, WildHerbGeneration::bootstrapPlacedFeatures)
                        .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, AlchemiaBiomeModifiers::bootstrap),
                Set.of(Alchemia.MODID)));
    }
}
