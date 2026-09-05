package muddykat.alchemia.common.items;

import muddykat.alchemia.common.items.helper.Ingredients;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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

public class ItemMortarPestle extends Item {

    public static final int TICKS_PER_STEP = 8;
    private static final int USE_DURATION = TICKS_PER_STEP * Ingredients.MAX_CRUSH_UNITS;

    public ItemMortarPestle(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static ItemStack findTarget(LivingEntity user, ItemStack mortar) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack candidate = user.getItemInHand(hand);
            if (candidate != mortar && ItemIngredient.canCrush(candidate)) return candidate;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack mortar = player.getItemInHand(hand);
        if (findTarget(player, mortar).isEmpty()) {
            return InteractionResult.FAIL;
        }

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack mortar, int ticksRemaining) {
        int elapsed = getUseDuration(mortar, user) - ticksRemaining;
        if (elapsed <= 0 || elapsed % TICKS_PER_STEP != 0) return;

        ItemStack target = findTarget(user, mortar);
        if (target.isEmpty()) {
            user.stopUsingItem();
            return;
        }

        if (!level.isClientSide()) {
            ItemIngredient.crush(target);
        }

        level.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.GRINDSTONE_USE, SoundSource.PLAYERS, 0.6F, 0.9F + level.getRandom().nextFloat() * 0.2F);

        if (!ItemIngredient.canCrush(target)) {
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
        tooltips.accept(Component.translatable("alchemia.tooltip.mortar").withStyle(ChatFormatting.DARK_GRAY));
    }
}
