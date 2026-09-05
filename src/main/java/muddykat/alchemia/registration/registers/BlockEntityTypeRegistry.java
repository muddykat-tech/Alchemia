package muddykat.alchemia.registration.registers;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockEntityTypeRegistry {
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Alchemia.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityAlchemyCauldron>> ALCHEMICAL_CAULDRON = TILES.register("alchemical_cauldron",
            () -> new BlockEntityType<>(TileEntityAlchemyCauldron::new, BlockRegistry.BLOCK_REGISTRY.get("alchemical_cauldron").get()));
}
