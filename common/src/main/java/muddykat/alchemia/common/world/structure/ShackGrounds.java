package muddykat.alchemia.common.world.structure;

import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.registration.registers.BlockRegistry;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;

final class ShackGrounds {

    private static final Ingredients[] GARDEN = {Ingredients.Lifeleaf, Ingredients.Druids_Rosemary, Ingredients.Healers_Heather,
            Ingredients.Goodberry, Ingredients.Coldleaf};

    private ShackGrounds() {
    }

    static void build(ShackCanvas canvas, ShackStyle style, ShackContents contents) {
        creepers(canvas, style);
        if (style.raised()) {
            lilies(canvas);
            return;
        }
        path(canvas, style, contents);
        apron(canvas, style);
        woodpile(canvas, style);
        garden(canvas, style);
        litter(canvas, style);
    }

    private static boolean natural(BlockState state) {
#if MC_VER >= 26100
        return state.is(BlockTags.SUPPORTS_VEGETATION);
#else
        return state.is(BlockTags.DIRT);
#endif
    }

    private static boolean house(int x, int z) {
        return x >= 2 && x <= 8 && z >= 0 && z <= 11 || x == 5 && z == 12;
    }

    private static Block earth(ShackStyle style) {
        return style.soil() == Blocks.ROOTED_DIRT ? Blocks.ROOTED_DIRT : Blocks.DIRT;
    }

    private static void path(ShackCanvas canvas, ShackStyle style, ShackContents contents) {
        int buried = 4 + canvas.pick(0, 0, 0, 50, 3);
        for (int x = 4; x <= 6; x++) {
            canvas.fillDown(x, -1, 0, Blocks.DIRT.defaultBlockState());
            if (x == buried) {
                contents.brushable(x, 0, 0, Blocks.SUSPICIOUS_GRAVEL, ShackContents.SPOT_PATH);
            } else {
                canvas.set(x, 0, 0, canvas.chance(x, 0, 0, 51) < 0.5F ? Blocks.GRAVEL : earth(style));
            }
        }
    }

    private static void apron(ShackCanvas canvas, ShackStyle style) {
        for (int x = 1; x <= 9; x++) {
            for (int z = 0; z <= 12; z++) {
                if (house(x, z) || !canvas.column(x, z) || canvas.chance(x, 0, z, 52) > 0.5F) continue;
                int top = canvas.surface(x, z);
                if (Math.abs(top) > 1 || !natural(canvas.get(x, top, z))) continue;
                float roll = canvas.chance(x, top, z, 53);
                canvas.set(x, top, z, roll < 0.4F ? Blocks.GRAVEL : roll < 0.7F ? Blocks.COARSE_DIRT : earth(style));
            }
        }
    }

    private static void woodpile(ShackCanvas canvas, ShackStyle style) {
        for (int z = 4; z <= 6; z++) {
            if (!canvas.column(9, z) || canvas.surface(9, z) != 0) continue;
            int height = z == 6 ? 1 : 2;
            for (int y = 1; y <= height; y++) {
                if (ShackCanvas.yields(canvas.get(9, y, z))) canvas.set(9, y, z, ShackShell.log(style.log(), Direction.Axis.Z));
            }
        }
    }

    private static void garden(ShackCanvas canvas, ShackStyle style) {
        for (int x = 0; x <= 1; x++) {
            for (int z = 5; z <= 10; z++) {
                if (!canvas.column(x, z)) continue;
                int top = canvas.surface(x, z);
                if (Math.abs(top) > 1 || !natural(canvas.get(x, top, z))) continue;
                canvas.set(x, top, z, style.soil());
                if (canvas.chance(x, top, z, 54) < 0.6F && ShackCanvas.yields(canvas.get(x, top + 1, z))) {
                    herb(canvas, GARDEN[canvas.pick(x, top, z, 55, GARDEN.length)], x, top + 1, z);
                }
            }
        }
    }

    private static void herb(ShackCanvas canvas, Ingredients ingredient, int x, int y, int z) {
        var block = BlockRegistry.getBlock(ingredient);
        if (block == null || !(block.get() instanceof CropBlock crop)) return;
        int age = Math.min(crop.getMaxAge(), 2 + canvas.pick(x, y, z, 56, Math.max(1, crop.getMaxAge() - 1)));
        canvas.set(x, y, z, crop.getStateForAge(age));
    }

    private static void litter(ShackCanvas canvas, ShackStyle style) {
        for (int x = 0; x <= 10; x++) {
            for (int z = 0; z <= 12; z++) {
                if (house(x, z) || !canvas.column(x, z) || canvas.chance(x, 0, z, 57) > 0.1F) continue;
                int top = canvas.surface(x, z);
                if (Math.abs(top) > 1 || !natural(canvas.get(x, top, z)) || !canvas.get(x, top + 1, z).isAir()) continue;
                Block cover = style.litter();
                if (cover == null) cover = canvas.chance(x, top, z, 62) < 0.5F ? Blocks.FERN : grass();
                canvas.set(x, top + 1, z, cover);
            }
        }
    }

    private static Block grass() {
        Block grass = ShackShell.vanilla("short_grass", ShackShell.vanilla("grass", null));
        return grass == null ? Blocks.FERN : grass;
    }

    private static void creepers(ShackCanvas canvas, ShackStyle style) {
#if MC_VER >= 26100
        if (style == ShackStyle.PALE_OAK) {
            for (int z = 2; z <= 12; z++) {
                for (int x : new int[]{1, 9}) {
                    if (canvas.chance(x, 3, z, 58) < 0.3F && canvas.get(x, 3, z).isAir()) {
                        canvas.set(x, 3, z, Blocks.PALE_HANGING_MOSS.defaultBlockState().setValue(BlockStateProperties.TIP, true));
                    }
                }
            }
            return;
        }
#endif
        if (style.creeper() == null) return;

        for (int z = 3; z <= 11; z++) {
            vine(canvas, style.creeper(), 1, z, Direction.EAST);
            vine(canvas, style.creeper(), 9, z, Direction.WEST);
        }
        for (int x = 2; x <= 8; x++) {
            if (x != 5) vine(canvas, style.creeper(), x, 12, Direction.NORTH);
        }
    }

    private static void vine(ShackCanvas canvas, Block creeper, int x, int z, Direction face) {
        if (canvas.chance(x, 0, z, 59) > 0.22F) return;
        int length = 1 + canvas.pick(x, 0, z, 60, 3);
        BlockState vine = creeper.defaultBlockState().setValue(VineBlock.getPropertyForFace(face), true);
        for (int y = 3; y > 3 - length; y--) {
            int bx = x + face.getStepX();
            int bz = z + face.getStepZ();
#if MC_VER >= 26100
            boolean backed = canvas.get(bx, y, bz).isSolidRender();
#else
            boolean backed = canvas.get(bx, y, bz).isSolidRender(canvas.level(), canvas.world(bx, y, bz));
#endif
            if (!canvas.get(x, y, z).isAir() || !backed) return;
            canvas.set(x, y, z, vine);
        }
    }

    private static void lilies(ShackCanvas canvas) {
        for (int x = 0; x <= 10; x++) {
            for (int z = 0; z <= 12; z++) {
                if (!canvas.column(x, z) || canvas.chance(x, 0, z, 61) > 0.06F) continue;
                int water = canvas.waterline(x, z);
                if (water <= canvas.surface(x, z)) continue;
                if (canvas.get(x, water, z).getFluidState().isSourceOfType(Fluids.WATER) && canvas.get(x, water + 1, z).isAir()) {
                    canvas.set(x, water + 1, z, Blocks.LILY_PAD);
                }
            }
        }
    }
}
