package muddykat.alchemia.data.generators;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.BlockIngredient;
import muddykat.alchemia.common.blocks.BlockMineralClusterGeneric;
import muddykat.alchemia.registration.registers.BlockRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

public class AlchemiaBlockTagsProvider extends TagsProvider<Block> {

    public AlchemiaBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, Registries.BLOCK, registries, Alchemia.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        var pickaxe = tag(BlockTags.MINEABLE_WITH_PICKAXE);
        var swords = tag(BlockTags.SWORD_EFFICIENT);

        BlockRegistry.BLOCK_REGISTRY.forEach((name, holder) -> {
            Block block = holder.get();

            if (block instanceof BlockIngredient) {
                swords.add(block.builtInRegistryHolder().key());
                return;
            }

            pickaxe.add(block.builtInRegistryHolder().key());

            if (block instanceof BlockMineralClusterGeneric) {
                tag(BlockTags.NEEDS_IRON_TOOL).add(block.builtInRegistryHolder().key());
            }
        });
    }
}
