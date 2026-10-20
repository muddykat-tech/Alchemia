package muddykat.alchemia.data.generators;

import muddykat.alchemia.common.blocks.blockentity.BlockPlacedGuide;
import muddykat.alchemia.common.blocks.blockentity.BlockResearchDesk;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.AncientOak;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

import java.util.concurrent.CompletableFuture;

public class AlchemiaBlockTagsProvider extends TagsProvider<Block> {

    public AlchemiaBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
                                     @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.BLOCK, registries, Alchemia.MODID, existingFileHelper);
    }

    private boolean ancientOak(String name, ResourceKey<Block> key) {
        if (name.equals(AncientOak.LEAVES)) {
            tag(BlockTags.LEAVES).add(key);
            tag(BlockTags.MINEABLE_WITH_HOE).add(key);
            return true;
        }
        if (name.equals(AncientOak.SAPLING)) {
            tag(BlockTags.SAPLINGS).add(key);
            return true;
        }

        TagKey<Block> family = switch (name) {
            case AncientOak.LOG, AncientOak.WOOD, AncientOak.STRIPPED_LOG, AncientOak.STRIPPED_WOOD -> BlockTags.LOGS_THAT_BURN;
            case AncientOak.PLANKS -> BlockTags.PLANKS;
            case AncientOak.STAIRS -> BlockTags.WOODEN_STAIRS;
            case AncientOak.SLAB -> BlockTags.WOODEN_SLABS;
            case AncientOak.FENCE -> BlockTags.WOODEN_FENCES;
            case AncientOak.FENCE_GATE -> BlockTags.FENCE_GATES;
            case AncientOak.DOOR -> BlockTags.WOODEN_DOORS;
            case AncientOak.TRAPDOOR -> BlockTags.WOODEN_TRAPDOORS;
            case AncientOak.BUTTON -> BlockTags.WOODEN_BUTTONS;
            case AncientOak.PRESSURE_PLATE -> BlockTags.WOODEN_PRESSURE_PLATES;
            default -> null;
        };
        if (family == null) return false;

        tag(family).add(key);
        tag(BlockTags.MINEABLE_WITH_AXE).add(key);
        if (name.equals(AncientOak.LOG)) tag(BlockTags.OVERWORLD_NATURAL_LOGS).add(key);
        return true;
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

            if (block instanceof BlockResearchDesk || block instanceof BlockPlacedGuide) {
                tag(BlockTags.MINEABLE_WITH_AXE).add(block.builtInRegistryHolder().key());
                return;
            }

            if (block instanceof BlockInfectedLog) {
                tag(BlockTags.MINEABLE_WITH_AXE).add(block.builtInRegistryHolder().key());
                tag(BlockTags.LOGS).add(block.builtInRegistryHolder().key());
                return;
            }

            if (ancientOak(name, block.builtInRegistryHolder().key())) {
                return;
            }

            pickaxe.add(block.builtInRegistryHolder().key());

            if (block instanceof BlockMineralClusterGeneric) {
                tag(BlockTags.NEEDS_IRON_TOOL).add(block.builtInRegistryHolder().key());
            }
        });
    }
}
