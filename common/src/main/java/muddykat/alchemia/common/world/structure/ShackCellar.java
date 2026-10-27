package muddykat.alchemia.common.world.structure;

import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyMachinePart;
import muddykat.alchemia.common.blocks.helper.AlchemyMachineParts;
import muddykat.alchemia.common.blocks.helper.MachineOwner;
import muddykat.alchemia.common.blocks.helper.UpgradeSide;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.registration.registers.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

import java.util.List;

final class ShackCellar {

    private static final Direction MACHINE_FACING = Direction.WEST;
    private static final int[][] BENCH = {{3, 10}, {3, 11}, {4, 11}};

    private ShackCellar() {
    }

    static void build(ShackCanvas canvas, ShackStyle style, int ceiling, ShackContents contents) {
        int floor = ceiling - ShackPiece.CELLAR_DROP;
        shell(canvas, style, ceiling, floor);
        access(canvas, style, ceiling, floor);
        caveIn(canvas, style, ceiling, floor, contents);
        kiln(canvas, floor, contents);
        stores(canvas, style, ceiling, floor, contents);
        machine(canvas, floor);
        growth(canvas, ceiling, floor);
    }

    private static void shell(ShackCanvas canvas, ShackStyle style, int ceiling, int floor) {
        for (int x = 2; x <= 10; x++) {
            for (int z = 3; z <= 12; z++) {
                boolean wall = x == 2 || x == 10 || z == 3 || z == 12;
                for (int y = floor; y < ceiling; y++) {
                    if (wall || y == floor) {
                        canvas.set(x, y, z, ShackShell.stone(canvas, style, x, y, z));
                    } else {
                        canvas.air(x, y, z);
                    }
                }
                if (style.raised()) {
                    canvas.set(x, ceiling, z, ShackShell.stone(canvas, style, x, ceiling, z));
                } else if (x > 8 && ShackCanvas.yields(canvas.get(x, ceiling, z))) {
                    canvas.set(x, ceiling, z, canvas.get(x, ceiling + 1, z).isAir() ? Blocks.GRASS_BLOCK : Blocks.DIRT);
                }
                canvas.fillDown(x, floor - 1, z, style.stone().defaultBlockState());
            }
        }
        for (int x = 3; x <= 9; x++) canvas.set(x, ceiling - 1, 5, ShackShell.log(style.log(), Direction.Axis.X));
    }

    private static void access(ShackCanvas canvas, ShackStyle style, int ceiling, int floor) {
        canvas.set(4, 0, 6, style.trapdoor().defaultBlockState().setValue(TrapDoorBlock.FACING, Direction.NORTH)
                .setValue(TrapDoorBlock.HALF, Half.TOP).setValue(TrapDoorBlock.OPEN, false));

        if (style.raised()) {
            for (int y = ceiling; y < 0; y++) {
                for (int x = 3; x <= 5; x++) {
                    for (int z = 5; z <= 7; z++) {
                        if (x != 4 || z != 6) canvas.set(x, y, z, ShackShell.stone(canvas, style, x, y, z));
                    }
                }
            }
        }

        BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
        for (int y = floor + 1; y < 0; y++) {
            if (y < ceiling) canvas.set(4, y, 7, ShackShell.log(style.log(), Direction.Axis.Y));
            canvas.set(4, y, 6, ladder);
        }
    }

    private static void caveIn(ShackCanvas canvas, ShackStyle style, int ceiling, int floor, ShackContents contents) {
        for (int y = floor + 1; y < ceiling; y++) {
            for (int z = 8; z <= 9; z++) {
                float roll = canvas.chance(2, y, z, 30);
                canvas.set(2, y, z, roll < 0.45F ? Blocks.GRAVEL : roll < 0.75F || style.raised() ? style.soil() : Blocks.ROOTED_DIRT);
            }
        }
        canvas.set(3, floor + 1, 9, Blocks.GRAVEL);
        canvas.set(3, floor + 2, 9, Blocks.GRAVEL);
        canvas.set(3, floor + 3, 9, Blocks.GRAVEL);
        canvas.set(3, floor + 1, 8, Blocks.GRAVEL);
        canvas.set(4, floor + 1, 9, Blocks.GRAVEL);
        canvas.set(3, floor + 1, 7, Blocks.GRAVEL);
        contents.brushable(3, floor + 2, 8, Blocks.SUSPICIOUS_GRAVEL, ShackContents.SPOT_CAVE_HIGH);
        contents.brushable(4, floor + 1, 8, Blocks.SUSPICIOUS_GRAVEL, ShackContents.SPOT_CAVE_LOW);
    }

    private static void kiln(ShackCanvas canvas, int floor, ShackContents contents) {
        canvas.set(3, floor + 1, 4, Blocks.FURNACE.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
        canvas.set(3, floor + 1, 5, Blocks.SAND);
        canvas.set(3, floor + 2, 5, Blocks.SAND);
        canvas.set(4, floor + 1, 4, Blocks.SAND);
        contents.brushable(4, floor + 1, 5, Blocks.SUSPICIOUS_SAND, ShackContents.SPOT_SAND);
    }

    private static void stores(ShackCanvas canvas, ShackStyle style, int ceiling, int floor, ShackContents contents) {
        int stand = floor + 1;
        Block chest = ShackShell.vanilla("weathered_copper_chest", Blocks.CHEST);
        contents.container(9, stand, 4, chest.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST), ShackContents.CELLAR_CHEST);
        canvas.set(9, stand, 5, Blocks.BARREL.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP));
        canvas.set(9, stand + 1, 5, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 3).setValue(CandleBlock.LIT, false));
        canvas.set(8, stand, 4, Blocks.DECORATED_POT.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST));
        contents.potions(7, stand, 4, List.of(contents.special(6), contents.vessel(6)));

        for (int[] top : BENCH) canvas.set(top[0], stand, top[1], ShackShell.slab(style, SlabType.TOP));
        contents.potions(3, stand + 1, 10, List.of(contents.special(0), contents.special(1), contents.special(2)));
        contents.potions(3, stand + 1, 11, List.of(contents.special(3), contents.vessel(3)));
        contents.potions(4, stand + 1, 11, List.of(contents.special(4), contents.special(5)));

        Block shelf = style.shelf();
        if (shelf != null) {
            contents.shelf(3, stand + 2, 10, shelf, Direction.EAST, contents.stored(0));
            contents.shelf(3, stand + 2, 11, shelf, Direction.EAST, contents.stored(1));
            contents.shelf(4, stand + 2, 11, shelf, Direction.NORTH, contents.stored(2));
            contents.shelf(5, stand + 2, 11, shelf, Direction.NORTH, contents.stored(3));
        }

        for (int[] web : new int[][]{{3, 4}, {9, 4}, {3, 7}, {5, 11}, {4, 9}, {6, 8}}) {
            if (canvas.chance(web[0], ceiling, web[1], 31) < 0.7F && canvas.get(web[0], ceiling - 1, web[1]).isAir()) {
                canvas.set(web[0], ceiling - 1, web[1], Blocks.COBWEB);
            }
        }
    }

    private static void machine(ShackCanvas canvas, int floor) {
        for (int x = 7; x <= 9; x++) {
            for (int z = 6; z <= 11; z++) {
                canvas.set(x, floor, z, canvas.chance(x, floor, z, 33) < 0.35F ? Blocks.CRACKED_DEEPSLATE_TILES : Blocks.DEEPSLATE_TILES);
            }
        }

        BlockPos core = new BlockPos(8, floor + 1, 9);
        Direction placed = canvas.facing(MACHINE_FACING);
        ruin(canvas, core, MachineOwner.CORE);
        for (UpgradeSide side : UpgradeSide.values()) {
            ruin(canvas, core.relative(side.directionFrom(MACHINE_FACING)), MachineOwner.of(side));
        }

        parts(canvas, core, MachineOwner.CORE, placed);
        for (UpgradeSide side : UpgradeSide.values()) parts(canvas, core.relative(side.directionFrom(MACHINE_FACING)), MachineOwner.of(side), placed);
    }

    private static void ruin(ShackCanvas canvas, BlockPos at, MachineOwner owner) {
        Block block = BlockRegistry.BLOCK_REGISTRY.get(owner.ruinedName()).get();
        canvas.set(at.getX(), at.getY(), at.getZ(), block.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, MACHINE_FACING));
    }

    private static void parts(ShackCanvas canvas, BlockPos at, MachineOwner owner, Direction placed) {
        BlockState part = BlockRegistry.BLOCK_REGISTRY.get(BlockAlchemyMachinePart.ID).get().defaultBlockState()
                .setValue(BlockAlchemyMachinePart.FACING, placed).setValue(BlockAlchemyMachinePart.OWNER, owner);
        List<AlchemyMachineParts.Placed> cells = AlchemyMachineParts.placed(owner, MACHINE_FACING);
        for (int index = 0; index < cells.size(); index++) {
            BlockPos cell = at.offset(cells.get(index).offset());
            if (cell.equals(at) || !canvas.get(cell.getX(), cell.getY(), cell.getZ()).isAir()) continue;
            canvas.set(cell.getX(), cell.getY(), cell.getZ(), part.setValue(BlockAlchemyMachinePart.INDEX, index));
        }
    }

    private static void growth(ShackCanvas canvas, int ceiling, int floor) {
        hang(canvas, Ingredients.Terraria, 5, ceiling - 1, 7);
        hang(canvas, Ingredients.Grasping_Root, 6, ceiling - 2, 5);
        hang(canvas, Ingredients.Terraria, 4, ceiling - 1, 10);

        canvas.set(3, floor, 6, Blocks.STONE);
        canvas.set(5, floor, 5, Blocks.STONE);
        hang(canvas, Ingredients.Witch_Mushroom, 3, floor + 1, 6);
        hang(canvas, Ingredients.Rainbow_Cap, 5, floor + 1, 5);
    }

    private static void hang(ShackCanvas canvas, Ingredients ingredient, int x, int y, int z) {
        var block = BlockRegistry.getBlock(ingredient);
        if (block == null || !canvas.get(x, y, z).isAir()) return;
        BlockState state = block.get().defaultBlockState();
        if (block.get() instanceof CropBlock crop) state = crop.getStateForAge(crop.getMaxAge());
        canvas.set(x, y, z, state);
    }

}
