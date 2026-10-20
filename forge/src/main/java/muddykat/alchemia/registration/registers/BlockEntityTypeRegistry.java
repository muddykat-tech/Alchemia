package muddykat.alchemia.registration.registers;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.helper.UpgradeSide;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyMachineCore;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyMachineUpgrade;
import muddykat.alchemia.common.blocks.tileentity.TileEntityPlacedPotions;
import muddykat.alchemia.common.blocks.blockentity.BlockPlacedGuide;
import muddykat.alchemia.common.blocks.blockentity.BlockPlacedPotions;
import muddykat.alchemia.common.blocks.blockentity.BlockResearchDesk;
import muddykat.alchemia.common.blocks.tileentity.TileEntityPlacedGuide;
import muddykat.alchemia.common.blocks.tileentity.TileEntityResearchDesk;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

import java.util.Arrays;

public class BlockEntityTypeRegistry {
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Alchemia.MODID);

    public static final RegistryObject<BlockEntityType<TileEntityAlchemyCauldron>> ALCHEMICAL_CAULDRON = TILES.register("alchemical_cauldron",
            () -> BlockEntityType.Builder.of(TileEntityAlchemyCauldron::new,
                    BlockRegistry.BLOCK_REGISTRY.get("alchemical_cauldron").get()).build(null));

    public static final RegistryObject<BlockEntityType<TileEntityAlchemyMachineCore>> ALCHEMY_MACHINE_CORE = TILES.register("alchemy_machine_core",
            () -> BlockEntityType.Builder.of(TileEntityAlchemyMachineCore::new,
                    BlockRegistry.BLOCK_REGISTRY.get("alchemy_machine_core").get()).build(null));

    public static final RegistryObject<BlockEntityType<TileEntityAlchemyMachineUpgrade>> ALCHEMY_MACHINE_UPGRADE = TILES.register("alchemy_machine_upgrade",
            () -> BlockEntityType.Builder.of(TileEntityAlchemyMachineUpgrade::new, upgradeBlocks()).build(null));

    public static final RegistryObject<BlockEntityType<TileEntityPlacedPotions>> PLACED_POTIONS = TILES.register(BlockPlacedPotions.ID,
            () -> BlockEntityType.Builder.of(TileEntityPlacedPotions::new,
                    BlockRegistry.BLOCK_REGISTRY.get(BlockPlacedPotions.ID).get()).build(null));

    public static final RegistryObject<BlockEntityType<TileEntityResearchDesk>> RESEARCH_DESK = TILES.register(BlockResearchDesk.ID,
            () -> BlockEntityType.Builder.of(TileEntityResearchDesk::new,
                    BlockRegistry.BLOCK_REGISTRY.get(BlockResearchDesk.ID).get()).build(null));

    public static final RegistryObject<BlockEntityType<TileEntityPlacedGuide>> PLACED_GUIDE = TILES.register(BlockPlacedGuide.ID,
            () -> BlockEntityType.Builder.of(TileEntityPlacedGuide::new,
                    BlockRegistry.BLOCK_REGISTRY.get(BlockPlacedGuide.ID).get()).build(null));

    private static Block[] upgradeBlocks() {
        return Arrays.stream(UpgradeSide.values())
                .map(side -> BlockRegistry.BLOCK_REGISTRY.get(side.registryName()).get())
                .toArray(Block[]::new);
    }
}
