package muddykat.alchemia.common.items;

import muddykat.alchemia.common.blocks.blockentity.BlockAlchemyMachineUpgrade;
import muddykat.alchemia.common.blocks.helper.AlchemyMachineParts;
import muddykat.alchemia.common.blocks.helper.UpgradeSide;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;

public class BlockItemAlchemyMachineUpgrade extends BlockItemGeneric {

    public BlockItemAlchemyMachineUpgrade(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        if (getBlock() instanceof BlockAlchemyMachineUpgrade upgrade) {
            BlockPos pos = context.getClickedPos();
            Direction facing = upgrade.findCoreFacing(context.getLevel(), pos);
            if (facing == null) {
                return refuse(context, Component.translatable("alchemia.gui.machine.needs_core"));
            }

            UpgradeSide required = upgrade.getSide().requires();
            if (required != null && !AlchemyMachineParts.hasUpgrade(context.getLevel(), upgrade.corePos(pos, facing), facing, required)) {
                return refuse(context, Component.translatable("alchemia.gui.machine.needs_upgrade",
                        Component.translatable("block.alchemia." + required.registryName())));
            }
        }
        return super.place(context);
    }

    private InteractionResult refuse(BlockPlaceContext context, Component reason) {
        if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(reason.copy().withStyle(ChatFormatting.RED), true);
        }
        return InteractionResult.FAIL;
    }
}
