package muddykat.alchemia.data.generators.loot;

import muddykat.alchemia.common.blocks.BlockIngredient;
import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyCauldron;
import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyMachineCore;
import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyMachineUpgrade;
import muddykat.alchemia.registration.registers.BlockRegistry;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BlockLoot extends BlockLootSubProvider {

    public BlockLoot(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        for (Block block : getKnownBlocks()) {
            if (block instanceof BlockIngredient ingredient) {
                add(block, LootTable.lootTable().withPool(randomAmountItem(ItemRegistry.getSeedByIngredient(ingredient.getIngredient()), 1, 4)));
            } else if (block instanceof BlockAlchemyCauldron) {
                add(block, LootTable.lootTable().withPool(singleItem(Blocks.CAULDRON)).withPool(singleItem(Blocks.CAMPFIRE)));
            } else if (block instanceof BlockAlchemyMachineCore || block instanceof BlockAlchemyMachineUpgrade) {
                add(block, LootTable.lootTable().withPool(singleItem(block)));
            }
        }
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        List<Block> blocks = new ArrayList<>();
        for (var holder : BlockRegistry.BLOCK_REGISTRY.values()) {
            Block block = holder.get();
            if (block instanceof BlockIngredient || block instanceof BlockAlchemyCauldron
                    || block instanceof BlockAlchemyMachineCore || block instanceof BlockAlchemyMachineUpgrade) {
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
