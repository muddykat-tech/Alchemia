package muddykat.alchemia.common.world.structure;

import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyCauldron;
import muddykat.alchemia.registration.registers.BlockRegistry;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

import java.util.List;

final class ShackRooms {

    private static final int[][] CORNER_WEBS = {{3, 3, 4}, {7, 3, 4}, {7, 3, 8}, {6, 3, 10}, {5, 7, 4}, {5, 7, 10}, {4, 6, 7}, {6, 6, 8}};

    private ShackRooms() {
    }

    static void build(ShackCanvas canvas, ShackStyle style, ShackContents contents) {
        hearth(canvas);
        sleeping(canvas, style);
        workbench(canvas, style, contents);
        storage(canvas, style, contents);
        rug(canvas, style);
        neglect(canvas, style);
    }

    private static boolean nearCauldron(int x, int y, int z) {
        return x >= 3 && x <= 5 && z >= 8 && z <= 10 && y >= 1 && y <= 3;
    }

    private static void hearth(ShackCanvas canvas) {
        for (int x = 3; x <= 5; x++) {
            for (int z = 8; z <= 10; z++) {
                canvas.set(x, 0, z, canvas.chance(x, 0, z, 44) < 0.25F ? Blocks.CRACKED_DEEPSLATE_TILES : Blocks.DEEPSLATE_TILES);
            }
        }
        canvas.set(4, 1, 9, BlockRegistry.BLOCK_REGISTRY.get("alchemical_cauldron").get().defaultBlockState()
                .setValue(BlockAlchemyCauldron.FACING, Direction.EAST));
    }

    private static void sleeping(ShackCanvas canvas, ShackStyle style) {
        if (!(style.bed() instanceof BedBlock)) return;
        BlockState bed = style.bed().defaultBlockState().setValue(BedBlock.FACING, Direction.SOUTH);
        canvas.set(7, 1, 9, bed.setValue(BedBlock.PART, BedPart.FOOT));
        canvas.set(7, 1, 10, bed.setValue(BedBlock.PART, BedPart.HEAD));
    }

    private static void workbench(ShackCanvas canvas, ShackStyle style, ShackContents contents) {
        canvas.set(3, 1, 5, ShackShell.slab(style, SlabType.TOP));
        canvas.set(3, 1, 6, ShackShell.slab(style, SlabType.TOP));
        canvas.set(3, 1, 7, ShackShell.stairs(style, Direction.SOUTH, Half.TOP));
        contents.potions(3, 2, 5, List.of(contents.treasureVision(), contents.vessel(23)));
        canvas.set(3, 2, 6, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 2).setValue(CandleBlock.LIT, false));

        Block shelf = style.shelf();
        if (shelf == null) return;
        for (int y = 2; y <= 3; y++) {
            for (int z = 8; z <= 10; z++) contents.shelf(3, y, z, shelf, Direction.EAST, contents.dried(y * 10 + z));
        }
    }

    private static void storage(ShackCanvas canvas, ShackStyle style, ShackContents contents) {
        Block chest = ShackShell.vanilla("exposed_copper_chest", Blocks.CHEST);
        contents.container(7, 1, 5, chest.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST), ShackContents.SHACK_CHEST);
        canvas.set(7, 1, 4, Blocks.COMPOSTER.defaultBlockState().setValue(ComposterBlock.LEVEL, 3));
        canvas.set(3, 1, 4, Blocks.BARREL.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP));
        canvas.set(3, 2, 4, Blocks.POTTED_DEAD_BUSH);
    }

    private static void rug(ShackCanvas canvas, ShackStyle style) {
        for (int x = 4; x <= 6; x++) {
            for (int z = 4; z <= 6; z++) {
                boolean hatch = x == 4 && z == 6;
                if (hatch || canvas.chance(x, 1, z, 40) > 0.15F) canvas.set(x, 1, z, style.carpet());
            }
        }
    }

    private static void neglect(ShackCanvas canvas, ShackStyle style) {
        for (int[] web : CORNER_WEBS) {
            if (canvas.chance(web[0], web[1], web[2], 41) < 0.6F && canvas.get(web[0], web[1], web[2]).isAir()) {
                canvas.set(web[0], web[1], web[2], Blocks.COBWEB);
            }
        }

        for (int x = 3; x <= 7; x++) {
            for (int z = 4; z <= 9; z++) {
                if (!nearCauldron(x, 1, z) && canvas.chance(x, 1, z, 42) < 0.14F && canvas.get(x, 1, z).isAir()) {
                    canvas.set(x, 1, z, style.overgrowth());
                }
            }
            if (canvas.chance(x, 3, 5, 43) < 0.6F && canvas.get(x, 3, 5).isAir()) canvas.set(x, 3, 5, hanging(style));
        }
    }

    private static BlockState hanging(ShackStyle style) {
#if MC_VER >= 26100
        if (style == ShackStyle.PALE_OAK) return Blocks.PALE_HANGING_MOSS.defaultBlockState().setValue(BlockStateProperties.TIP, true);
#endif
        return Blocks.HANGING_ROOTS.defaultBlockState();
    }
}
