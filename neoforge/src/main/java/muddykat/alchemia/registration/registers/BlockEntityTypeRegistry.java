package muddykat.alchemia.registration.registers;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.helper.UpgradeSide;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyMachineCore;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyMachineUpgrade;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Arrays;

public class BlockEntityTypeRegistry {
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Alchemia.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityAlchemyCauldron>> ALCHEMICAL_CAULDRON = TILES.register("alchemical_cauldron",
            () -> new BlockEntityType<>(TileEntityAlchemyCauldron::new, BlockRegistry.BLOCK_REGISTRY.get("alchemical_cauldron").get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityAlchemyMachineCore>> ALCHEMY_MACHINE_CORE = TILES.register("alchemy_machine_core",
            () -> new BlockEntityType<>(TileEntityAlchemyMachineCore::new, BlockRegistry.BLOCK_REGISTRY.get("alchemy_machine_core").get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityAlchemyMachineUpgrade>> ALCHEMY_MACHINE_UPGRADE = TILES.register("alchemy_machine_upgrade",
            () -> new BlockEntityType<>(TileEntityAlchemyMachineUpgrade::new, upgradeBlocks()));

    private static Block[] upgradeBlocks() {
        return Arrays.stream(UpgradeSide.values())
                .map(side -> BlockRegistry.BLOCK_REGISTRY.get(side.registryName()).get())
                .toArray(Block[]::new);
    }
}
