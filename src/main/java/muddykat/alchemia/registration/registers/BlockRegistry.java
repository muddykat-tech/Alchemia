package muddykat.alchemia.registration.registers;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.BlockGeneric;
import muddykat.alchemia.common.blocks.BlockMineralBuddingGeneric;
import muddykat.alchemia.common.blocks.BlockMineralClusterGeneric;
import muddykat.alchemia.common.blocks.BlockMineralGeneric;
import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyCauldron;
import muddykat.alchemia.common.blocks.blockentity.EntityBlockGeneric;
import muddykat.alchemia.common.items.BlockItemGeneric;
import muddykat.alchemia.common.items.helper.IngredientType;
import muddykat.alchemia.common.items.helper.Ingredients;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Alchemia.MODID);
    public static HashMap<String, RegistryObject<Block>> BLOCK_REGISTRY = new HashMap<>();
    public static DeferredRegister<Block> getRegistry() {
        return BLOCKS;
    }

    public static void initialize() {
        createBasicBlock("deepmetal_tile");
        createBlockEntity(BlockAlchemyCauldron.class, "alchemical_cauldron");

        for(Ingredients ingredient : Ingredients.values())
        {
            if(ingredient.getType().equals(IngredientType.Mineral))
            {
                createMineralGeodeBlocks(ingredient);
            }
        }
    }


    public static RegistryObject<Block> getBlock(Ingredients ingredients) {
        return BLOCK_REGISTRY.get(ingredients.getSeedRegistryName());
    }

    public static void registerBlock(String registry_name,  Supplier<Block> blockSupplier){
        BLOCK_REGISTRY.put(registry_name, BLOCKS.register(registry_name, blockSupplier));
    }

    public static void createBasicBlock(String id){
        registerBlock(id, BlockGeneric::new);
        ItemRegistry.registerItem(id, () -> new BlockItemGeneric(BlockRegistry.BLOCK_REGISTRY.get(id).get()));
    }

    public static void createBlockEntity(Class<? extends EntityBlockGeneric> generic, String id) {
        registerBlock(id, () -> {
            try {
                return generic.getDeclaredConstructor().newInstance();
            } catch (RuntimeException | InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException error) {
                Logger log =  LogManager.getLogger();
                log.info(error.getMessage());
            }
            return null;
        });

        ItemRegistry.registerItem(id, () -> new BlockItemGeneric(BlockRegistry.BLOCK_REGISTRY.get(id).get()));
    }

    public static void createMineralGeodeBlocks(Ingredients ingredient){
        registerBlock(ingredient.name().toLowerCase() + "_cluster", () -> new BlockMineralClusterGeneric(ingredient, 7, 3, BudSize.CLUSTER));
        registerBlock(ingredient.name().toLowerCase() + "_bud_large", () -> new BlockMineralClusterGeneric(ingredient, 5, 3, BudSize.LARGE));
        registerBlock(ingredient.name().toLowerCase() + "_bud_medium",() -> new BlockMineralClusterGeneric(ingredient, 4, 3, BudSize.MEDIUM));
        registerBlock(ingredient.name().toLowerCase() + "_bud_small", () -> new BlockMineralClusterGeneric(ingredient, 4, 4, BudSize.SMALL));

        // Register Item for main cluster and buds
        ItemRegistry.registerItem(ingredient.name().toLowerCase() + "_cluster", () -> new BlockItemGeneric(BlockRegistry.BLOCK_REGISTRY.get(ingredient.name().toLowerCase() + "_cluster").get()));
        ItemRegistry.registerItem(ingredient.name().toLowerCase() + "_bud_large", () -> new BlockItemGeneric(BlockRegistry.BLOCK_REGISTRY.get(ingredient.name().toLowerCase() + "_bud_large").get()));
        ItemRegistry.registerItem(ingredient.name().toLowerCase() + "_bud_medium", () -> new BlockItemGeneric(BlockRegistry.BLOCK_REGISTRY.get(ingredient.name().toLowerCase() + "_bud_medium").get()));
        ItemRegistry.registerItem(ingredient.name().toLowerCase() + "_bud_small", () -> new BlockItemGeneric(BlockRegistry.BLOCK_REGISTRY.get(ingredient.name().toLowerCase() + "_bud_small").get()));

        registerBlock(ingredient.name().toLowerCase() + "_geode", ()->new BlockMineralGeneric(ingredient));

        // Register Item for Geode block itself
        ItemRegistry.registerItem(ingredient.name().toLowerCase() + "_geode", () -> new BlockItemGeneric(BlockRegistry.BLOCK_REGISTRY.get(ingredient.name().toLowerCase() + "_geode").get()));

        // Scuffed way to get the list of cluster parts...
        registerBlock(ingredient.name().toLowerCase() + "_budding_geode", ()->new BlockMineralBuddingGeneric(ingredient,
                List.of(getGeodeCluster(ingredient, BudSize.SMALL), getGeodeCluster(ingredient, BudSize.MEDIUM), getGeodeCluster(ingredient, BudSize.LARGE), getGeodeCluster(ingredient, BudSize.CLUSTER))));

        // And an Item Block for the budding geode
        ItemRegistry.registerItem(ingredient.name().toLowerCase() + "_budding_geode", () -> new BlockItemGeneric(BlockRegistry.BLOCK_REGISTRY.get(ingredient.name().toLowerCase() + "_budding_geode").get()));

    }

    public static Block getGeodeBlock(Ingredients ingredient) {
        return BLOCK_REGISTRY.get(ingredient.name().toLowerCase() + "_geode").get();
    }

    public static BlockMineralClusterGeneric getGeodeCluster(Ingredients ingredient, BudSize size)
    {
        String id = size == BudSize.CLUSTER ? "_cluster" : "_bud_" + size.name().toLowerCase();
        return (BlockMineralClusterGeneric) BLOCK_REGISTRY.get(ingredient.name().toLowerCase() + id).get();
    }

    public static BlockMineralBuddingGeneric getGeodeBuddingBlock(Ingredients ingredient)
    {
        return (BlockMineralBuddingGeneric) BLOCK_REGISTRY.get(ingredient.name().toLowerCase() + "_budding_geode").get();
    }

    public enum BudSize {
        SMALL,
        MEDIUM,
        LARGE,
        CLUSTER;
    }
}
