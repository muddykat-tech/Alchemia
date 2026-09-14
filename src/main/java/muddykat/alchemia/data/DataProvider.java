package muddykat.alchemia.data;

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
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeAdvancementProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = Alchemia.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
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

        AlchemiaBlockTagsProvider blockTags = new AlchemiaBlockTagsProvider(output, lookup, helper);
        generator.addProvider(server, blockTags);
        generator.addProvider(server, new AlchemiaItemTagsProvider(output, lookup, helper));
        generator.addProvider(server, new AlchemiaBiomeTagsProvider(output, lookup, helper));
        generator.addProvider(server, new AlchemiaRecipeProvider(output));
        generator.addProvider(server, new AlchemiaLootProvider(output));
        generator.addProvider(server, new ForgeAdvancementProvider(output, lookup, helper,
                List.of(new AlchemiaAdvancementProvider())));

        generator.addProvider(server, new DatapackBuiltinEntriesProvider(output, lookup,
                new RegistrySetBuilder()
                        .add(Registries.CONFIGURED_FEATURE, WildHerbGeneration::bootstrapConfiguredFeatures)
                        .add(Registries.PLACED_FEATURE, WildHerbGeneration::bootstrapPlacedFeatures)
                        .add(ForgeRegistries.Keys.BIOME_MODIFIERS, AlchemiaBiomeModifiers::bootstrap),
                Set.of(Alchemia.MODID)));
    }
}
