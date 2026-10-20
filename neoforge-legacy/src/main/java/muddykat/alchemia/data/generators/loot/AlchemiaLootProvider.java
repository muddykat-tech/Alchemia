package muddykat.alchemia.data.generators.loot;

import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import net.minecraft.core.HolderLookup;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.Set;

public class AlchemiaLootProvider extends LootTableProvider {
    public AlchemiaLootProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, Set.of(), List.of(new SubProviderEntry(BlockLoot::new, LootContextParamSets.BLOCK)), lookup);
    }
}
