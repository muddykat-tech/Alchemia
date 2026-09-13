package muddykat.alchemia.common.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public abstract class ItemPathTool extends Item {

    public static final int USES = 100;
    public static final int TICKS_PER_STEP = 10;

    private static final int USE_DURATION = TICKS_PER_STEP * USES;

    public ItemPathTool(Properties properties) {
        super(properties.stacksTo(1).durability(USES));
    }

    public static boolean isPristine(ItemStack stack) {
        return !stack.isDamaged();
    }

    protected abstract boolean canApply(ItemStack target);

    protected abstract void apply(ItemStack target);

    public abstract String tooltipKey();

    public abstract String progressKey();

    public abstract int progressValue(ItemStack target);

    public static ItemStack findTarget(LivingEntity user, ItemStack tool) {
        if (!(tool.getItem() instanceof ItemPathTool pathTool)) return ItemStack.EMPTY;

        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack candidate = user.getItemInHand(hand);
            if (candidate != tool && pathTool.canApply(candidate)) return candidate;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack tool = player.getItemInHand(hand);
        if (findTarget(player, tool).isEmpty()) return InteractionResult.FAIL;

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack tool, int ticksRemaining) {
        int elapsed = getUseDuration(tool, user) - ticksRemaining;
        if (elapsed <= 0 || elapsed % TICKS_PER_STEP != 0) return;

        ItemStack target = findTarget(user, tool);
        if (target.isEmpty()) {
            user.stopUsingItem();
            return;
        }

        if (level instanceof ServerLevel server) {
            apply(target);
            tool.hurtAndBreak(1, server, user, item -> user.stopUsingItem());
        }

        level.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 0.6F, 1.2F + level.getRandom().nextFloat() * 0.2F);

        if (tool.isEmpty() || !canApply(target)) {
            user.stopUsingItem();
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return USE_DURATION;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BRUSH;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltips, TooltipFlag flag) {
        tooltips.accept(Component.translatable(tooltipKey()).withStyle(ChatFormatting.DARK_GRAY));

        if (isPristine(stack)) {
            tooltips.accept(Component.translatable("alchemia.tooltip.stage.pristine").withStyle(ChatFormatting.DARK_PURPLE));
        } else {
            tooltips.accept(Component.translatable("alchemia.tooltip.stage.spent").withStyle(ChatFormatting.DARK_RED));
        }
    }
}
