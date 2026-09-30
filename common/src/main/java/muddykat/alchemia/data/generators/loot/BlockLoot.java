package muddykat.alchemia.data.generators.loot;

import muddykat.alchemia.common.blocks.BlockInfectedLog;
import muddykat.alchemia.common.blocks.BlockIngredient;
import muddykat.alchemia.common.blocks.BlockIngredientBerry;
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
import muddykat.alchemia.registration.registers.BlockRegistry;
import muddykat.alchemia.registration.registers.ItemRegistry;
#if MC_VER >= 26200
import net.minecraft.advancements.predicates.StatePropertiesPredicate;
#else
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
#endif
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
#if MC_VER >= 26300
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.predicates.MatchBlock;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
#else
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
#endif

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BlockLoot extends BlockLootSubProvider {

#if MC_VER >= 26300
    private final net.minecraft.data.loot.LootTableSubProvider.Context context;

    public BlockLoot(net.minecraft.data.loot.LootTableSubProvider.Context context) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), context);
        this.context = context;
    }
#else
    public BlockLoot(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }
#endif

    @Override
    protected void generate() {
        for (Block block : getKnownBlocks()) {
            if (block instanceof BlockIngredient ingredient) {
                add(block, ingredientDrops(block, ingredient));
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

#if MC_VER >= 26300
    private static Holder<ContextIntProvider> mineralYield(BlockRegistry.BudSize size) {
        return switch (size) {
            case CLUSTER -> ContextIntProviders.between(3, 4);
            case LARGE -> ContextIntProviders.exactly(2);
            default -> ContextIntProviders.exactly(1);
        };
    }
#else
    private static NumberProvider mineralYield(BlockRegistry.BudSize size) {
        return switch (size) {
            case CLUSTER -> UniformGenerator.between(3, 4);
            case LARGE -> ConstantValue.exactly(2);
            default -> ConstantValue.exactly(1);
        };
    }
#endif

    @Override
    protected Iterable<Block> getKnownBlocks() {
        List<Block> blocks = new ArrayList<>();
        for (var holder : BlockRegistry.BLOCK_REGISTRY.values()) {
            Block block = holder.get();
            if (block instanceof BlockIngredient || block instanceof BlockAlchemyCauldron
                    || block instanceof BlockTangleBranch || block instanceof BlockKelpStalk || block instanceof BlockInfectedLog
                    || block instanceof BlockAlchemyMachineCore || block instanceof BlockAlchemyMachineUpgrade
                    || block instanceof BlockMineralGeneric || block instanceof BlockMineralBuddingGeneric
                    || block instanceof BlockMineralClusterGeneric) {
                blocks.add(block);
            }
        }
        return blocks;
    }

    private LootTable.Builder ingredientDrops(Block block, BlockIngredient ingredient) {
        ItemLike harvest = ItemRegistry.getItemFromRegistry(ingredient.getIngredient().getRegistryName());
        ItemLike seed = ItemRegistry.getSeedByIngredient(ingredient.getIngredient());

        if (ingredient instanceof BlockIngredientBerry) {
            return LootTable.lootTable()
                    .withPool(singleItem(seed).when(lowerHalf(block)))
                    .withPool(harvestPool(harvest, BlockIngredientBerry.MIN_PICKED, BlockIngredientBerry.MAX_PICKED)
                            .when(lowerHalf(block)).when(fruiting(block)));
        }

        if (ingredient instanceof BlockIngredientTall) {
            return LootTable.lootTable()
                    .withPool(singleItem(seed).when(lowerHalf(block)).when(mature(block, ingredient, null).invert()))
                    .withPool(harvestPool(harvest, 2, 4).when(lowerHalf(block)).when(mature(block, ingredient, null)));
        }

        LootTable.Builder table = LootTable.lootTable()
                .withPool(singleItem(ItemRegistry.getSeedByIngredient(ingredient.getIngredient())).when(mature(block, ingredient, null).invert()));

        if (ingredient instanceof BlockIngredientWild) {
            return table
                    .withPool(harvestPool(harvest, 2, 4).when(mature(block, ingredient, false)))
                    .withPool(harvestPool(harvest, 4, 8).when(mature(block, ingredient, true)));
        }

        return table.withPool(harvestPool(harvest, 2, 4).when(mature(block, ingredient, null)));
    }

    private LootItemCondition.Builder mature(Block block, BlockIngredient ingredient, Boolean wild) {
        StatePropertiesPredicate.Builder state = StatePropertiesPredicate.Builder.properties()
                .hasProperty(ingredient.getAgeProperty(), ingredient.getMaxAge());
        if (wild != null) state.hasProperty(BlockIngredientWild.WILD, wild);
        return stateMatches(block, state);
    }

    private LootItemCondition.Builder lowerHalf(Block block) {
        return stateMatches(block, StatePropertiesPredicate.Builder.properties()
                .hasProperty(BlockIngredientTall.HALF, DoubleBlockHalf.LOWER));
    }

    private LootItemCondition.Builder fruiting(Block block) {
        return stateMatches(block, StatePropertiesPredicate.Builder.properties()
                .hasProperty(BlockIngredientBerry.FRUITING, true));
    }

    private LootItemCondition.Builder stateMatches(Block block, StatePropertiesPredicate.Builder state) {
#if MC_VER >= 26300
        return MatchBlock.blockMatches(context.lookup(net.minecraft.core.registries.Registries.BLOCK), block, state);
#else
        return LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(state);
#endif
    }

    private LootPool.Builder harvestPool(ItemLike item, int min, int max) {
        return createPoolBuilder()
#if MC_VER >= 26300
                .setRolls(ContextIntProviders.exactly(1))
                .add(LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(ContextIntProviders.between(min, max))));
#else
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max))));
#endif
    }

    private LootPool.Builder singleItem(ItemLike item) {
        return createPoolBuilder()
#if MC_VER >= 26300
                .setRolls(ContextIntProviders.exactly(1))
#else
                .setRolls(ConstantValue.exactly(1))
#endif
                .add(LootItem.lootTableItem(item));
    }

    private LootPool.Builder createPoolBuilder() {
        return LootPool.lootPool().when(ExplosionCondition.survivesExplosion());
    }
}
