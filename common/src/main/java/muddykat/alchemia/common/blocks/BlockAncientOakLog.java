package muddykat.alchemia.common.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
#if MC_VER < 26102
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
#endif

import java.util.function.Supplier;

public class BlockAncientOakLog extends RotatedPillarBlock {

    private final Supplier<Block> stripped;

    public BlockAncientOakLog(Supplier<Block> stripped, Properties properties) {
        super(properties);
        this.stripped = stripped;
    }

#if MC_VER < 26102
    @Override
    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
        if (stripped == null || toolAction != ToolActions.AXE_STRIP) return super.getToolModifiedState(state, context, toolAction, simulate);
        return stripped.get().defaultBlockState().setValue(AXIS, state.getValue(AXIS));
    }
#endif
}
