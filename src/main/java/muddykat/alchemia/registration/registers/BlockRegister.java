package muddykat.alchemia.registration.registers;

import muddykat.alchemia.Alchemia;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.function.Supplier;

public class BlockRegister {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Alchemia.MODID);

    public static HashMap<String, RegistryObject<Block>> BLOCK_REGISTRY = new HashMap<>();

    public static void registerBlock(String registry_name,  Supplier<Block> blockSupplier){
        BLOCK_REGISTRY.put(registry_name, BLOCKS.register(registry_name, blockSupplier));
    }

    public static DeferredRegister<Block> getRegistry() {
        return BLOCKS;
    }
}
