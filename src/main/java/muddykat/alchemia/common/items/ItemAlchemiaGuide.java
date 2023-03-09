package muddykat.alchemia.common.items;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.client.gui.AlchemicalScreen;
import muddykat.alchemia.client.helper.ScreenHelper;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.blocks.tileentity.container.AlchemicalCauldronMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemAlchemiaGuide extends Item {

    public ItemAlchemiaGuide() {
        super(new Properties().tab(Alchemia.ITEM_GROUP).stacksTo(1));
    }


    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        BlockPos clickPos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockPos existingPosition = null;
        CompoundTag tag;
        if (stack.hasTag())
        {
            tag = stack.getTag();
            existingPosition = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
        }
        else
        {
            tag = new CompoundTag();
        }

        if(existingPosition == null || !existingPosition.equals(clickPos)) {
            BlockEntity blockEntity = context.getLevel().getBlockEntity(clickPos);
            if (blockEntity instanceof TileEntityAlchemyCauldron cauldron) {
                if (context.getLevel().isClientSide) {
                    return InteractionResult.CONSUME;
                } else {
                    tag.putInt("x", clickPos.getX());
                    tag.putInt("y", clickPos.getY());
                    tag.putInt("z", clickPos.getZ());
                    player.displayClientMessage(new TranslatableComponent("alchemia.guide.bound.message"), true);
                }
            }
        }

        stack.setTag(tag);
        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level pLevel, List<Component> tooltips, TooltipFlag pIsAdvanced) {

        if (stack.hasTag())
        {
            CompoundTag tag = stack.getTag();
            BlockPos existingPosition = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
            tooltips.add(new TextComponent("Bound: " + existingPosition.toString()));
        }

        super.appendHoverText(stack, pLevel, tooltips, pIsAdvanced);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag();
    }
}
