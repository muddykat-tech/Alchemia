package muddykat.alchemia.common.blocks.helper;

import muddykat.alchemia.common.potion.PotionGlass;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public final class PotionCluster {

    public static final int MAX_POTIONS = 4;

    private static final int HALF_WIDTH = 3;

    private static final int[][][] LAYOUT = {
            {{0, 0}},
            {{-3, -3}, {3, 3}},
            {{-4, -4}, {4, -4}, {0, 4}},
            {{-4, -4}, {4, -4}, {-4, 4}, {4, 4}}
    };

    private PotionCluster() {
    }

    public static int[] offset(int count, int slot) {
        return LAYOUT[count - 1][slot];
    }

    public static VoxelShape shape(List<PotionGlass> glasses) {
        VoxelShape shape = Shapes.empty();
        int count = glasses.size();
        for (int slot = 0; slot < count; slot++) {
            int[] offset = offset(count, slot);
            double x = 8 + offset[0];
            double z = 8 + offset[1];
            shape = Shapes.or(shape, Block.box(x - HALF_WIDTH, 0, z - HALF_WIDTH,
                    x + HALF_WIDTH, glasses.get(slot).height(), z + HALF_WIDTH));
        }
        return shape;
    }
}
