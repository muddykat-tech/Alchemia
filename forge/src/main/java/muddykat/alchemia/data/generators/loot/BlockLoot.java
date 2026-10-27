package muddykat.alchemia.data.generators.loot;

import muddykat.alchemia.common.blocks.blockentity.BlockResearchDesk;

import muddykat.alchemia.common.blocks.AncientOak;

import muddykat.alchemia.common.blocks.BlockInfectedLog;
import muddykat.alchemia.common.blocks.BlockIngredient;
import muddykat.alchemia.common.blocks.BlockIngredientBerry;
import muddykat.alchemia.common.blocks.BlockIngredientPatch;
import muddykat.alchemia.common.blocks.BlockIngredientTall;
import muddykat.alchemia.common.blocks.BlockIngredientWild;
import muddykat.alchemia.common.blocks.BlockKelpStalk;
import muddykat.alchemia.common.blocks.BlockTangleBranch;
import muddykat.alchemia.common.blocks.BlockMineralBuddingGeneric;
import muddykat.alchemia.common.blocks.BlockMineralClusterGeneric;
import muddykat.alchemia.common.blocks.BlockMineralGeneric;
import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyCauldron;
import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyMachineCore;
import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyMachineUpgrade;
import muddykat.alchemia.common.blocks.blockentity.BlockRuinedMachine;
import muddykat.alchemia.common.blocks.helper.MachineRepair;
import muddykat.alchemia.registration.registers.BlockRegistry;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyBlockState;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BlockLoot extends BlockLootSubProvider {

    public BlockLoot() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    private static final float[] ANCIENT_OAK_SAPLING_CHANCES = {0.025F, 0.027777778F, 0.03125F, 0.041666668F, 0.1F};

    private void ancientOak() {
        for (String id : List.of(AncientOak.LOG, AncientOak.WOOD, AncientOak.STRIPPED_LOG, AncientOak.STRIPPED_WOOD,
                AncientOak.PLANKS, AncientOak.STAIRS, AncientOak.FENCE, AncientOak.FENCE_GATE, AncientOak.TRAPDOOR,
                AncientOak.BUTTON, AncientOak.PRESSURE_PLATE, AncientOak.SAPLING)) {
            dropSelf(AncientOak.block(id));
        }

        Block slab = AncientOak.block(AncientOak.SLAB);
        add(slab, createSlabItemTable(slab));
        Block door = AncientOak.block(AncientOak.DOOR);
        add(door, createDoorTable(door));
        Block leaves = AncientOak.block(AncientOak.LEAVES);
        add(leaves, createLeavesDrops(leaves, AncientOak.block(AncientOak.SAPLING), ANCIENT_OAK_SAPLING_CHANCES));
    }

    @Override
    protected void generate() {
        ancientOak();

        for (Block block : getKnownBlocks()) {
            if (block instanceof BlockIngredient ingredient) {
                add(block, cropTable(block, ingredient));
            } else if (block instanceof BlockTangleBranch branch) {
                add(block, LootTable.lootTable().withPool(singleItem(ItemRegistry.getItemFromRegistry(branch.getIngredient().getRegistryName()))));
            } else if (block instanceof BlockKelpStalk stalk) {
                add(block, LootTable.lootTable().withPool(singleItem(ItemRegistry.getItemFromRegistry(stalk.getIngredient().getRegistryName()))));
            } else if (block instanceof BlockInfectedLog log) {
                dropOther(block, log.getWood().log());
            } else if (block instanceof BlockAlchemyCauldron) {
                add(block, LootTable.lootTable().withPool(singleItem(Blocks.CAULDRON)).withPool(singleItem(Blocks.CAMPFIRE)));
            } else if (block instanceof BlockAlchemyMachineCore || block instanceof BlockAlchemyMachineUpgrade) {
                add(block, LootTable.lootTable().withPool(singleItem(block)));
            } else if (block instanceof BlockRuinedMachine) {
                add(block, LootTable.lootTable().withPool(singleItem(block)
                        .apply(CopyBlockState.copyState(block).copy(MachineRepair.REPAIR))));
            } else if (block instanceof BlockResearchDesk) {
                dropSelf(block);
            } else if (block instanceof BlockMineralBuddingGeneric) {
                add(block, noDrop());
            } else if (block instanceof BlockMineralGeneric) {
                dropSelf(block);
            } else if (block instanceof BlockMineralClusterGeneric cluster) {
                add(block, createSilkTouchDispatchTable(block, applyExplosionDecay(block,
                        LootItem.lootTableItem(ItemRegistry.getItemFromRegistry(cluster.getIngredient().getRegistryName()))
                                .apply(SetItemCountFunction.setCount(mineralYield(cluster.getSize()))))));
            }
        }
    }

    private LootTable.Builder cropTable(Block block, BlockIngredient ingredient) {
        ItemLike harvest = ItemRegistry.getItemFromRegistry(ingredient.getIngredient().getRegistryName());
        ItemLike seed = ItemRegistry.getSeedByIngredient(ingredient.getIngredient());

        if (ingredient instanceof BlockIngredientBerry) {
            return LootTable.lootTable()
                    .withPool(singleItem(seed).when(lowerHalf(block)))
                    .withPool(randomAmountItem(harvest, BlockIngredientBerry.MIN_PICKED, BlockIngredientBerry.MAX_PICKED)
                            .when(lowerHalf(block)).when(fruiting(block)));
        }

        if (ingredient instanceof BlockIngredientPatch) {
            return LootTable.lootTable()
                    .withPool(singleItem(seed).when(mature(block, ingredient, null)))
                    .withPool(randomAmountItem(harvest, BlockIngredientPatch.MIN_PICKED, BlockIngredientPatch.MAX_PICKED)
                            .when(mature(block, ingredient, null)));
        }

        if (ingredient instanceof BlockIngredientTall) {
            return LootTable.lootTable()
                    .withPool(singleItem(seed).when(lowerHalf(block)).when(mature(block, ingredient, null).invert()))
                    .withPool(randomAmountItem(harvest, 2, 4).when(lowerHalf(block)).when(mature(block, ingredient, null)));
        }

        LootTable.Builder table = LootTable.lootTable()
                .withPool(singleItem(ItemRegistry.getSeedByIngredient(ingredient.getIngredient())).when(mature(block, ingredient, null).invert()));

        if (ingredient instanceof BlockIngredientWild) {
            return table
                    .withPool(randomAmountItem(harvest, 2, 4).when(mature(block, ingredient, false)))
                    .withPool(randomAmountItem(harvest, 4, 8).when(mature(block, ingredient, true)));
        }

        return table.withPool(randomAmountItem(harvest, 2, 4).when(mature(block, ingredient, null)));
    }

    private LootItemCondition.Builder mature(Block block, BlockIngredient ingredient, Boolean wild) {
        StatePropertiesPredicate.Builder state = StatePropertiesPredicate.Builder.properties()
                .hasProperty(ingredient.getAgeProperty(), ingredient.getMaxAge());
        if (wild != null) state.hasProperty(BlockIngredientWild.WILD, wild);
        return LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(state);
    }

    private LootItemCondition.Builder lowerHalf(Block block) {
        return LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(
                StatePropertiesPredicate.Builder.properties().hasProperty(BlockIngredientTall.HALF, DoubleBlockHalf.LOWER));
    }

    private LootItemCondition.Builder fruiting(Block block) {
        return LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(
                StatePropertiesPredicate.Builder.properties().hasProperty(BlockIngredientBerry.FRUITING, true));
    }

    private static NumberProvider mineralYield(BlockRegistry.BudSize size) {
        return switch (size) {
            case CLUSTER -> UniformGenerator.between(3, 4);
            case LARGE -> ConstantValue.exactly(2);
            default -> ConstantValue.exactly(1);
        };
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        List<Block> blocks = new ArrayList<>();
        for (var entry : BlockRegistry.BLOCK_REGISTRY.entrySet()) {
            Block block = entry.getValue().get();
            if (entry.getKey().contains(AncientOak.WOOD_NAME) && !(block instanceof BlockInfectedLog)) {
                blocks.add(block);
                continue;
            }
            if (block instanceof BlockIngredient || block instanceof BlockAlchemyCauldron
                    || block instanceof BlockTangleBranch || block instanceof BlockKelpStalk || block instanceof BlockInfectedLog
                    || block instanceof BlockAlchemyMachineCore || block instanceof BlockAlchemyMachineUpgrade
                    || block instanceof BlockRuinedMachine
                    || block instanceof BlockMineralGeneric || block instanceof BlockMineralBuddingGeneric
                    || block instanceof BlockMineralClusterGeneric || block instanceof BlockResearchDesk) {
                blocks.add(block);
            }
        }
        return blocks;
    }

    private LootPool.Builder singleItem(ItemLike item) {
        return createPoolBuilder()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(item));
    }

    private LootPool.Builder randomAmountItem(ItemLike item, int min, int max) {
        return createPoolBuilder()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(item))
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)));
    }

    private LootPool.Builder createPoolBuilder() {
        return LootPool.lootPool().when(ExplosionCondition.survivesExplosion());
    }
}
