package muddykat.alchemia.registration.registers;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.BlockGeneric;
import muddykat.alchemia.common.blocks.BlockMineralBuddingGeneric;
import muddykat.alchemia.common.blocks.BlockMineralClusterGeneric;
import muddykat.alchemia.common.blocks.BlockMineralGeneric;
import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyCauldron;
import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyMachineCore;
import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyMachineUpgrade;
import muddykat.alchemia.common.blocks.helper.UpgradeSide;
import muddykat.alchemia.common.items.BlockItemGeneric;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class BlockRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Alchemia.MODID);
    public static final Map<String, DeferredBlock<Block>> BLOCK_REGISTRY = new LinkedHashMap<>();

    public static DeferredRegister.Blocks getRegistry() {
        return BLOCKS;
    }

    public static void initialize() {
        createBasicBlock("deepmetal_tile");
        createCauldronBlock("alchemical_cauldron");
        createMachineBlocks();

        for (Ingredients ingredient : Ingredients.values()) {
            if (ingredient.getType().equals(IngredientType.Mineral)) {
                createMineralGeodeBlocks(ingredient);
            }
        }
    }

    public static DeferredBlock<Block> getBlock(Ingredients ingredients) {
        return BLOCK_REGISTRY.get(ingredients.getSeedRegistryName());
    }

    public static DeferredBlock<Block> registerBlock(String registry_name, Function<BlockBehaviour.Properties, ? extends Block> factory, Supplier<BlockBehaviour.Properties> properties) {
        DeferredBlock<Block> block = BLOCKS.registerBlock(registry_name, factory, properties);
        BLOCK_REGISTRY.put(registry_name, block);
        return block;
    }

    public static void createBasicBlock(String id) {
        DeferredBlock<Block> block = registerBlock(id, BlockGeneric::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON));
        ItemRegistry.registerItem(id, properties -> new BlockItemGeneric(block.get(), properties));
    }

    public static void createCauldronBlock(String id) {
        DeferredBlock<Block> block = registerBlock(id, BlockAlchemyCauldron::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON).noOcclusion());
        ItemRegistry.registerItem(id, properties -> new BlockItemGeneric(block.get(), properties));
    }

    public static void createMachineBlocks() {
        DeferredBlock<Block> core = registerBlock("alchemy_machine_core", BlockAlchemyMachineCore::new,
                () -> BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON).noOcclusion());
        ItemRegistry.registerItem("alchemy_machine_core", properties -> new BlockItemGeneric(core.get(), properties));

        for (UpgradeSide side : UpgradeSide.values()) {
            String id = side.registryName();
            DeferredBlock<Block> upgrade = registerBlock(id, properties -> new BlockAlchemyMachineUpgrade(side, properties),
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON).noOcclusion());
            ItemRegistry.registerItem(id, properties -> new BlockItemGeneric(upgrade.get(), properties));
        }
    }

    public static void createMineralGeodeBlocks(Ingredients ingredient) {
        String base = ingredient.name().toLowerCase();

        registerClusterBlock(ingredient, base + "_cluster", 7, 3, BudSize.CLUSTER);
        registerClusterBlock(ingredient, base + "_bud_large", 5, 3, BudSize.LARGE);
        registerClusterBlock(ingredient, base + "_bud_medium", 4, 3, BudSize.MEDIUM);
        registerClusterBlock(ingredient, base + "_bud_small", 4, 4, BudSize.SMALL);

        DeferredBlock<Block> geode = registerBlock(base + "_geode", properties -> new BlockMineralGeneric(ingredient, properties),
                () -> BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK));
        ItemRegistry.registerItem(base + "_geode", properties -> new BlockItemGeneric(geode.get(), properties));

        List<Supplier<? extends AmethystClusterBlock>> clusters = List.of(
                () -> getGeodeCluster(ingredient, BudSize.SMALL),
                () -> getGeodeCluster(ingredient, BudSize.MEDIUM),
                () -> getGeodeCluster(ingredient, BudSize.LARGE),
                () -> getGeodeCluster(ingredient, BudSize.CLUSTER));

        DeferredBlock<Block> budding = registerBlock(base + "_budding_geode", properties -> new BlockMineralBuddingGeneric(ingredient, clusters, properties),
                () -> BlockBehaviour.Properties.ofFullCopy(Blocks.BUDDING_AMETHYST));
        ItemRegistry.registerItem(base + "_budding_geode", properties -> new BlockItemGeneric(budding.get(), properties));
    }

    private static void registerClusterBlock(Ingredients ingredient, String id, int height, int aabbOffset, BudSize size) {
        DeferredBlock<Block> cluster = registerBlock(id, properties -> new BlockMineralClusterGeneric(ingredient, height, aabbOffset, size, properties),
                () -> BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_CLUSTER));
        ItemRegistry.registerItem(id, properties -> new BlockItemGeneric(cluster.get(), properties));
    }

    public static Block getGeodeBlock(Ingredients ingredient) {
        return BLOCK_REGISTRY.get(ingredient.name().toLowerCase() + "_geode").get();
    }

    public static BlockMineralClusterGeneric getGeodeCluster(Ingredients ingredient, BudSize size) {
        String id = size == BudSize.CLUSTER ? "_cluster" : "_bud_" + size.name().toLowerCase();
        return (BlockMineralClusterGeneric) BLOCK_REGISTRY.get(ingredient.name().toLowerCase() + id).get();
    }

    public static BlockMineralBuddingGeneric getGeodeBuddingBlock(Ingredients ingredient) {
        return (BlockMineralBuddingGeneric) BLOCK_REGISTRY.get(ingredient.name().toLowerCase() + "_budding_geode").get();
    }

    public enum BudSize {
        SMALL,
        MEDIUM,
        LARGE,
        CLUSTER
    }
}
