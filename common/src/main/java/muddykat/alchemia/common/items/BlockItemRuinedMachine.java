package muddykat.alchemia.common.items;

import muddykat.alchemia.common.blocks.blockentity.BlockRuinedMachine;
import muddykat.alchemia.common.blocks.helper.MachineRepair;
import muddykat.alchemia.common.blocks.helper.UpgradeSide;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;

public class BlockItemRuinedMachine extends BlockItemGeneric {

    public BlockItemRuinedMachine(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        if (getBlock() instanceof BlockRuinedMachine ruined && ruined.side() != null) {
            UpgradeSide side = ruined.side();
            BlockPos pos = context.getClickedPos();
            Direction facing = ruined.findCoreFacing(context.getLevel(), pos);
            if (facing == null) return refuse(context, Component.translatable("alchemia.gui.machine.needs_core"));
            if (!MachineRepair.attachedFor(context.getLevel(), pos, side, facing)) {
                return refuse(context, Component.translatable("alchemia.gui.machine.needs_upgrade",
                        Component.translatable("block.alchemia.ruined_" + side.requires().registryName())));
            }
        }
        return super.place(context);
    }

    private InteractionResult refuse(BlockPlaceContext context, Component reason) {
        if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
#if MC_VER >= 26100
            serverPlayer.sendOverlayMessage(reason.copy().withStyle(ChatFormatting.RED));
#else
            serverPlayer.displayClientMessage(reason.copy().withStyle(ChatFormatting.RED), true);
#endif
        }
        return InteractionResult.FAIL;
    }
}
