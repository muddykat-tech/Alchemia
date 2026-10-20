package muddykat.alchemia.common.blocks;

import muddykat.alchemia.common.blocks.helper.InfectableWood;
import muddykat.alchemia.common.items.BlockItemGeneric;
import muddykat.alchemia.registration.registers.BlockRegistry;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.data.BlockFamily;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class AncientOak {

    public static final String WOOD_NAME = "ancient_oak";
    public static final String TEXTURE_ROOT = "alchemia:block/elder_oak/";

    public static final String LOG = "ancient_oak_log";
    public static final String WOOD = "ancient_oak_wood";
    public static final String STRIPPED_LOG = "stripped_ancient_oak_log";
    public static final String STRIPPED_WOOD = "stripped_ancient_oak_wood";
    public static final String PLANKS = "ancient_oak_planks";
    public static final String STAIRS = "ancient_oak_stairs";
    public static final String SLAB = "ancient_oak_slab";
    public static final String FENCE = "ancient_oak_fence";
    public static final String FENCE_GATE = "ancient_oak_fence_gate";
    public static final String DOOR = "ancient_oak_door";
    public static final String TRAPDOOR = "ancient_oak_trapdoor";
    public static final String BUTTON = "ancient_oak_button";
    public static final String PRESSURE_PLATE = "ancient_oak_pressure_plate";
    public static final String LEAVES = "ancient_oak_leaves";
    public static final String SAPLING = "ancient_oak_sapling";

    public static final List<String> LOGS = List.of(LOG, WOOD, STRIPPED_LOG, STRIPPED_WOOD);

    public static final String LOG_SIDE = "log_side";
    public static final String LOG_TOP = "log_top";
    public static final String STRIPPED_LOG_SIDE = "stripped_log_side";
    public static final String STRIPPED_LOG_TOP = "stripped_log_top";
    public static final int LOG_SIDE_ALTS = 4;
    public static final int STRIPPED_LOG_SIDE_ALTS = 3;

    public static final int NURTURE_REACH = 7;
    public static final int NURTURE_BELOW = 2;
    public static final int NURTURE_ABOVE = 7;

    private AncientOak() {
    }

    public static void register() {
        block(LOG, properties -> new BlockAncientOakLog(() -> block(STRIPPED_LOG), properties), () -> copy(Blocks.OAK_LOG));
        block(WOOD, properties -> new BlockAncientOakLog(() -> block(STRIPPED_WOOD), properties), () -> copy(Blocks.OAK_WOOD));
        block(STRIPPED_LOG, RotatedPillarBlock::new, () -> copy(Blocks.STRIPPED_OAK_LOG));
        block(STRIPPED_WOOD, RotatedPillarBlock::new, () -> copy(Blocks.STRIPPED_OAK_WOOD));
        block(PLANKS, Block::new, () -> copy(Blocks.OAK_PLANKS));
        block(STAIRS, properties -> new StairBlock(block(PLANKS).defaultBlockState(), properties), () -> copy(Blocks.OAK_STAIRS));
        block(SLAB, SlabBlock::new, () -> copy(Blocks.OAK_SLAB));
        block(FENCE, FenceBlock::new, () -> copy(Blocks.OAK_FENCE));
#if MC_VER >= 26102
        block(FENCE_GATE, properties -> new FenceGateBlock(WoodType.OAK, properties), () -> copy(Blocks.OAK_FENCE_GATE));
        block(BUTTON, properties -> new ButtonBlock(BlockSetType.OAK, 30, properties), () -> copy(Blocks.OAK_BUTTON));
        block(PRESSURE_PLATE, properties -> new PressurePlateBlock(BlockSetType.OAK, properties), () -> copy(Blocks.OAK_PRESSURE_PLATE));
        block(TRAPDOOR, properties -> new TrapDoorBlock(BlockSetType.OAK, properties), () -> copy(Blocks.OAK_TRAPDOOR));
        var door = BlockRegistry.registerBlock(DOOR, properties -> new DoorBlock(BlockSetType.OAK, properties), () -> copy(Blocks.OAK_DOOR));
        ItemRegistry.registerItem(DOOR, properties -> new DoubleHighBlockItem(door.get(), properties.useBlockDescriptionPrefix()));
#elif MC_VER >= 12100
        block(FENCE_GATE, properties -> new FenceGateBlock(WoodType.OAK, properties), () -> copy(Blocks.OAK_FENCE_GATE));
        block(BUTTON, properties -> new ButtonBlock(BlockSetType.OAK, 30, properties), () -> copy(Blocks.OAK_BUTTON));
        block(PRESSURE_PLATE, properties -> new PressurePlateBlock(BlockSetType.OAK, properties), () -> copy(Blocks.OAK_PRESSURE_PLATE));
        block(TRAPDOOR, properties -> new TrapDoorBlock(BlockSetType.OAK, properties), () -> copy(Blocks.OAK_TRAPDOOR));
        var door = BlockRegistry.registerBlock(DOOR, properties -> new DoorBlock(BlockSetType.OAK, properties), () -> copy(Blocks.OAK_DOOR));
        ItemRegistry.registerItem(DOOR, properties -> new DoubleHighBlockItem(door.get(), properties));
#else
        block(FENCE_GATE, properties -> new FenceGateBlock(properties, WoodType.OAK), () -> copy(Blocks.OAK_FENCE_GATE));
        block(BUTTON, properties -> new ButtonBlock(properties, BlockSetType.OAK, 30, true), () -> copy(Blocks.OAK_BUTTON));
        block(PRESSURE_PLATE, properties -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, properties, BlockSetType.OAK), () -> copy(Blocks.OAK_PRESSURE_PLATE));
        block(TRAPDOOR, properties -> new TrapDoorBlock(properties, BlockSetType.OAK), () -> copy(Blocks.OAK_TRAPDOOR));
        var door = BlockRegistry.registerBlock(DOOR, properties -> new DoorBlock(properties, BlockSetType.OAK), () -> copy(Blocks.OAK_DOOR));
        ItemRegistry.registerItem(DOOR, properties -> new DoubleHighBlockItem(door.get(), properties));
#endif
        block(LEAVES, BlockAncientOakLeaves::new, () -> copy(Blocks.OAK_LEAVES));
        block(SAPLING, BlockAncientOakSapling::new, () -> copy(Blocks.OAK_SAPLING));
    }

    private static void block(String id, Function<BlockBehaviour.Properties, ? extends Block> factory,
                              Supplier<BlockBehaviour.Properties> properties) {
        var block = BlockRegistry.registerBlock(id, factory, properties);
        ItemRegistry.registerItem(id, itemProperties -> new BlockItemGeneric(block.get(), itemProperties));
    }

    private static BlockBehaviour.Properties copy(Block block) {
#if MC_VER >= 12100
        return BlockBehaviour.Properties.ofFullCopy(block);
#else
        return BlockBehaviour.Properties.copy(block);
#endif
    }

    public static List<String> sideVariants(String side, int alts) {
        List<String> variants = new ArrayList<>();
        variants.add(side);
        for (int alt = 1; alt <= alts; alt++) variants.add(side + "_alt" + alt);
        return variants;
    }

    public static Block block(String id) {
        return BlockRegistry.BLOCK_REGISTRY.get(id).get();
    }

    public static BlockFamily family() {
        return new BlockFamily.Builder(block(PLANKS))
                .stairs(block(STAIRS))
                .slab(block(SLAB))
                .fence(block(FENCE))
                .fenceGate(block(FENCE_GATE))
                .door(block(DOOR))
                .trapdoor(block(TRAPDOOR))
                .button(block(BUTTON))
                .pressurePlate(block(PRESSURE_PLATE))
                .recipeGroupPrefix("wooden")
                .recipeUnlockedBy("has_planks")
                .getFamily();
    }

    public static boolean isTrunk(BlockState state) {
        Block block = state.getBlock();
        if (block == block(LOG) || block == block(WOOD)) return true;
        return block instanceof BlockInfectedLog infected && infected.getWood().name().equals(WOOD_NAME);
    }

    public static boolean nurtures(BlockGetter level, BlockPos pos) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int ring = 0; ring <= NURTURE_REACH; ring++) {
            for (int dx = -ring; dx <= ring; dx++) {
                for (int dz = -ring; dz <= ring; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) continue;
                    for (int dy = -NURTURE_BELOW; dy <= NURTURE_ABOVE; dy++) {
                        cursor.setWithOffset(pos, dx, dy, dz);
                        if (isTrunk(level.getBlockState(cursor))) return true;
                    }
                }
            }
        }
        return false;
    }

    public static InfectableWood.Wood wood() {
        for (InfectableWood.Wood wood : InfectableWood.WOODS) {
            if (wood.name().equals(WOOD_NAME)) return wood;
        }
        return null;
    }
}
