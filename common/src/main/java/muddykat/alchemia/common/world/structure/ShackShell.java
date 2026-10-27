package muddykat.alchemia.common.world.structure;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
#if MC_VER >= 26100
import net.minecraft.resources.Identifier;
#else
import net.minecraft.resources.ResourceLocation;
#endif
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
#if MC_VER >= 26100
import org.jspecify.annotations.Nullable;
#else
import javax.annotation.Nullable;
#endif

final class ShackShell {

    private static final int[][] STILTS = {{2, 1}, {8, 1}, {5, 1}, {2, 3}, {8, 3}, {2, 7}, {8, 7}, {2, 11}, {8, 11}};
    private static final int[][] POSTS = {{2, 3}, {8, 3}, {2, 11}, {8, 11}, {2, 7}, {8, 7}};
    private static final int[][] WINDOWS = {{3, 2, 3}, {7, 2, 3}, {2, 2, 5}, {8, 2, 5}, {8, 2, 9}, {3, 2, 11}, {7, 2, 11}, {5, 6, 3}};

    private ShackShell() {
    }

    static void build(ShackCanvas canvas, ShackStyle style, int cellar) {
        canvas.box(2, 1, 0, 8, 8, 11, Blocks.AIR.defaultBlockState());
        floor(canvas, style);
        walls(canvas, style);
        windows(canvas);
        plates(canvas, style);
        gables(canvas, style);
        roof(canvas, style);
        chimney(canvas, style);
        porch(canvas, style);
        if (style.raised()) {
            stilts(canvas, style);
        } else {
            footings(canvas, style, cellar);
        }
    }

    static BlockState log(Block block, Direction.Axis axis) {
        return block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    static BlockState stairs(ShackStyle style, Direction facing, Half half) {
        return style.stairs().defaultBlockState().setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, half);
    }

    static BlockState slab(ShackStyle style, SlabType type) {
        return style.slab().defaultBlockState().setValue(SlabBlock.TYPE, type);
    }

    static BlockState stone(ShackCanvas canvas, ShackStyle style, int x, int y, int z) {
        return (canvas.chance(x, y, z, 11) < 0.3F ? style.wornStone() : style.stone()).defaultBlockState();
    }

    static @Nullable Block vanilla(String id, @Nullable Block fallback) {
#if MC_VER >= 26100
        return BuiltInRegistries.BLOCK.getOptional(Identifier.withDefaultNamespace(id)).orElse(fallback);
#elif MC_VER >= 12100
        return BuiltInRegistries.BLOCK.getOptional(ResourceLocation.withDefaultNamespace(id)).orElse(fallback);
#else
        return BuiltInRegistries.BLOCK.getOptional(new ResourceLocation(id)).orElse(fallback);
#endif
    }

    private static boolean perimeter(int x, int z) {
        return x == 2 || x == 8 || z == 3 || z == 11;
    }

    private static void floor(ShackCanvas canvas, ShackStyle style) {
        for (int x = 2; x <= 8; x++) {
            for (int z = 1; z <= 11; z++) {
                if (z < 3) {
                    canvas.set(x, 0, z, style.planks());
                } else if (!perimeter(x, z)) {
                    canvas.set(x, 0, z, style.planks());
                } else if (!style.raised()) {
                    canvas.set(x, 0, z, stone(canvas, style, x, 0, z));
                } else if ((x == 2 || x == 8) && (z == 3 || z == 11)) {
                    canvas.set(x, 0, z, log(style.log(), Direction.Axis.Y));
                } else {
                    canvas.set(x, 0, z, log(style.log(), x == 2 || x == 8 ? Direction.Axis.Z : Direction.Axis.X));
                }
            }
        }
    }

    private static void walls(ShackCanvas canvas, ShackStyle style) {
        for (int y = 1; y <= 3; y++) {
            for (int x = 2; x <= 8; x++) {
                for (int z = 3; z <= 11; z++) {
                    if (perimeter(x, z)) canvas.set(x, y, z, style.planks());
                }
            }
            for (int[] post : POSTS) canvas.set(post[0], y, post[1], log(style.log(), Direction.Axis.Y));
            for (int x = 4; x <= 6; x++) canvas.set(x, y, 11, stone(canvas, style, x, y, 11));
        }

        canvas.set(5, 1, 11, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false)
                .setValue(CampfireBlock.FACING, Direction.NORTH));

        Block door = vanilla("exposed_copper_door", Blocks.SPRUCE_DOOR);
        BlockState lower = door.defaultBlockState().setValue(DoorBlock.FACING, Direction.SOUTH)
                .setValue(DoorBlock.HINGE, DoorHingeSide.LEFT).setValue(DoorBlock.OPEN, false);
        canvas.set(5, 1, 3, lower.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        canvas.set(5, 2, 3, lower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
    }

    private static void windows(ShackCanvas canvas) {
        int broken = canvas.pick(0, 0, 0, 12, WINDOWS.length - 1);
        for (int index = 0; index < WINDOWS.length; index++) {
            int[] at = WINDOWS[index];
            if (index == broken) {
                canvas.air(at[0], at[1], at[2]);
                continue;
            }
            boolean alongX = at[2] == 3 || at[2] == 11;
            canvas.set(at[0], at[1], at[2], Blocks.GLASS_PANE.defaultBlockState()
                    .setValue(BlockStateProperties.EAST, alongX).setValue(BlockStateProperties.WEST, alongX)
                    .setValue(BlockStateProperties.NORTH, !alongX).setValue(BlockStateProperties.SOUTH, !alongX));
        }
    }

    private static void plates(ShackCanvas canvas, ShackStyle style) {
        for (int z = 1; z <= 11; z++) {
            canvas.set(2, 4, z, log(style.log(), Direction.Axis.Z));
            canvas.set(8, 4, z, log(style.log(), Direction.Axis.Z));
        }
        for (int x = 3; x <= 7; x++) {
            for (int z : new int[]{1, 3, 5, 9, 11}) canvas.set(x, 4, z, log(z == 5 || z == 9 ? style.strippedLog() : style.log(), Direction.Axis.X));
        }
    }

    private static void gables(ShackCanvas canvas, ShackStyle style) {
        for (int z : new int[]{3, 11}) {
            for (int y = 5; y <= 7; y++) {
                int reach = 7 - y;
                for (int x = 5 - reach; x <= 5 + reach; x++) {
                    if (z == 3 && x == 5 && y == 6) continue;
                    canvas.set(x, y, z, style.planks());
                }
            }
        }
    }

    private static void roof(ShackCanvas canvas, ShackStyle style) {
        for (int z = 0; z <= 12; z++) {
            for (int step = 0; step < 4; step++) {
                int y = 4 + step;
                shingle(canvas, style, 1 + step, y, z, Direction.EAST);
                shingle(canvas, style, 9 - step, y, z, Direction.WEST);
            }
            if (z != 12) canvas.set(5, 8, z, slab(style, SlabType.BOTTOM));
        }
    }

    private static void shingle(ShackCanvas canvas, ShackStyle style, int x, int y, int z, Direction facing) {
        boolean inner = z >= 4 && z <= 10;
        if (inner && y >= 6 && canvas.chance(x, y, z, 14) < 0.07F) return;
        canvas.set(x, y, z, stairs(style, facing, Half.BOTTOM));
    }

    private static void chimney(ShackCanvas canvas, ShackStyle style) {
        for (int y = 0; y <= 9; y++) canvas.set(5, y, 12, stone(canvas, style, 5, y, 12));
        canvas.air(5, 10, 12);
    }

    private static void porch(ShackCanvas canvas, ShackStyle style) {
        for (int y = 1; y <= 3; y++) {
            canvas.set(2, y, 1, style.fence());
            canvas.set(8, y, 1, style.fence());
        }
        if (!style.raised()) return;

        BlockState rail = style.fence().defaultBlockState();
        canvas.set(2, 1, 1, rail.setValue(BlockStateProperties.EAST, true).setValue(BlockStateProperties.SOUTH, true));
        canvas.set(8, 1, 1, rail.setValue(BlockStateProperties.WEST, true).setValue(BlockStateProperties.SOUTH, true));
        canvas.set(3, 1, 1, rail.setValue(BlockStateProperties.WEST, true).setValue(BlockStateProperties.EAST, true));
        canvas.set(4, 1, 1, rail.setValue(BlockStateProperties.WEST, true));
        canvas.set(6, 1, 1, rail.setValue(BlockStateProperties.EAST, true));
        canvas.set(7, 1, 1, rail.setValue(BlockStateProperties.WEST, true).setValue(BlockStateProperties.EAST, true));
        for (int x : new int[]{2, 8}) {
            canvas.set(x, 1, 2, rail.setValue(BlockStateProperties.NORTH, true).setValue(BlockStateProperties.SOUTH, true));
        }
    }

    private static void stilts(ShackCanvas canvas, ShackStyle style) {
        for (int[] stilt : STILTS) canvas.fillDown(stilt[0], -1, stilt[1], log(style.log(), Direction.Axis.Y));
        canvas.fillDown(5, -1, 12, style.stone().defaultBlockState());

        BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
        if (!canvas.column(5, 0)) return;
        int ground = canvas.surface(5, 0);
        for (int y = 0; y > ground; y--) canvas.set(5, y, 0, ladder);
    }

    private static void footings(ShackCanvas canvas, ShackStyle style, int cellar) {
        int below = cellar - ShackPiece.CELLAR_DROP - 1;
        for (int x = 2; x <= 8; x++) {
            for (int z = 1; z <= 11; z++) {
                int from = z >= 3 ? below : -1;
                canvas.fillDown(x, from, z, stone(canvas, style, x, from, z));
            }
        }
        canvas.fillDown(5, -1, 12, style.stone().defaultBlockState());
    }
}
