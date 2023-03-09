package muddykat.alchemia.registration.registers;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityTypeRegistry {
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Alchemia.MODID);

    public static final RegistryObject<BlockEntityType<TileEntityAlchemyCauldron>> ALCHEMICAL_CAULDRON = TILES.register("alchemical_cauldron",
            () -> BlockEntityType.Builder.of(TileEntityAlchemyCauldron::new, BlockRegister.BLOCK_REGISTRY.get("alchemical_cauldron").get()).build(null));
}
