package muddykat.alchemia.common.blocks;

import muddykat.alchemia.common.blocks.helper.InfectableWood;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.registration.registers.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BlockInfectedLog extends RotatedPillarBlock {

    public static final int LEAF_REACH = 4;
    public static final int SHELF_CHANCE = 4;
    public static final int TRUNK_CLIMB = 32;

    private final Ingredients fungus;
    private final InfectableWood.Wood wood;

    public BlockInfectedLog(Ingredients fungus, InfectableWood.Wood wood, Properties properties) {
        super(properties);
        this.fungus = fungus;
        this.wood = wood;
    }

    public Ingredients getFungus() {
        return fungus;
    }

    public InfectableWood.Wood getWood() {
        return wood;
    }

    public boolean isLivingTree(BlockGetter level, BlockPos pos) {
        if (wood.hasLeavesNear(level, pos, LEAF_REACH)) return true;

        BlockPos.MutableBlockPos cursor = pos.mutable();
        for (int step = 0; step < TRUNK_CLIMB; step++) {
            cursor.move(Direction.UP);
            if (!wood.isTrunk(level.getBlockState(cursor))) break;
        }
        cursor.move(Direction.DOWN);
        return cursor.getY() != pos.getY() && wood.hasLeavesNear(level, cursor, LEAF_REACH);
    }

    public BlockState infect(BlockState log) {
        return defaultBlockState().setValue(AXIS, log.getValue(AXIS));
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(SHELF_CHANCE) != 0) return;
        if (!level.isAreaLoaded(pos, LEAF_REACH)) return;

        Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        BlockPos target = pos.relative(side);
        if (!level.isEmptyBlock(target) || !isLivingTree(level, pos)) return;

        BlockIngredientShelf shelf = (BlockIngredientShelf) BlockRegistry.getBlock(fungus).get();
        level.setBlock(target, shelf.facing(side), 3);
    }
}
