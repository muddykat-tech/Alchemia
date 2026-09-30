package muddykat.alchemia.data.generators;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.BlockInfectedLog;
import muddykat.alchemia.common.blocks.BlockIngredient;
import muddykat.alchemia.common.blocks.BlockKelpStalk;
import muddykat.alchemia.common.blocks.BlockTangleBranch;
import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyMachinePart;
import muddykat.alchemia.common.blocks.BlockMineralClusterGeneric;
import muddykat.alchemia.registration.registers.BlockRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

import java.util.concurrent.CompletableFuture;

public class AlchemiaBlockTagsProvider extends TagsProvider<Block> {

    public AlchemiaBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
                                     @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.BLOCK, registries, Alchemia.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        var pickaxe = tag(BlockTags.MINEABLE_WITH_PICKAXE);
        var swords = tag(BlockTags.SWORD_EFFICIENT);

        BlockRegistry.BLOCK_REGISTRY.forEach((name, holder) -> {
            Block block = holder.get();

            if (block instanceof BlockAlchemyMachinePart) {
                return;
            }

            if (block instanceof BlockIngredient || block instanceof BlockTangleBranch || block instanceof BlockKelpStalk) {
                swords.add(block.builtInRegistryHolder().key());
                return;
            }

            if (block instanceof BlockInfectedLog) {
                tag(BlockTags.MINEABLE_WITH_AXE).add(block.builtInRegistryHolder().key());
                tag(BlockTags.LOGS).add(block.builtInRegistryHolder().key());
                return;
            }

            pickaxe.add(block.builtInRegistryHolder().key());

            if (block instanceof BlockMineralClusterGeneric) {
                tag(BlockTags.NEEDS_IRON_TOOL).add(block.builtInRegistryHolder().key());
            }
        });
    }
}
