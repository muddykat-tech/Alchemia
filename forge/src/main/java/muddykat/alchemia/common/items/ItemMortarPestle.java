package muddykat.alchemia.common.items;

import muddykat.alchemia.common.items.helper.IngredientTool;
import muddykat.alchemia.common.items.helper.Ingredients;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class ItemMortarPestle extends Item implements IngredientTool {

    public static final int TICKS_PER_STEP = 4;
    private static final int SOUND_GAP = 10;
    private static final int USE_DURATION = TICKS_PER_STEP * Ingredients.MAX_CRUSH_UNITS;

    public ItemMortarPestle(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public int ticksPerStep() {
        return TICKS_PER_STEP;
    }

    @Override
    public boolean canApply(ItemStack target) {
        return ItemIngredient.canCrush(target);
    }

    @Override
    public void applyStep(ItemStack target, ItemStack tool, ServerLevel level, @Nullable LivingEntity user) {
        ItemIngredient.crush(target);
    }

    @Override
    public void playStep(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.BRUSH_GRAVEL, SoundSource.BLOCKS,
                0.55F, 0.85F + level.getRandom().nextFloat() * 0.2F);
    }

    @Override
    public int soundGap() {
        return SOUND_GAP;
    }

    @Override
    public void playDone(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.BRUSH_GRAVEL_COMPLETED, SoundSource.BLOCKS, 0.7F, 1.0F);
    }

    @Override
    public String actionKey() {
        return "alchemia.gui.tool.grind";
    }

    @Override
    public float progress(ItemStack target) {
        return (float) ItemIngredient.crushOf(target) / Ingredients.MAX_CRUSH_UNITS;
    }

    public static ItemStack findTarget(LivingEntity user, ItemStack mortar) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack candidate = user.getItemInHand(hand);
            if (candidate != mortar && ItemIngredient.canCrush(candidate)) return candidate;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack mortar = player.getItemInHand(hand);
        if (findTarget(player, mortar).isEmpty()) {
            return InteractionResultHolder.fail(mortar);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(mortar);
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack mortar, int ticksRemaining) {
        int elapsed = getUseDuration(mortar) - ticksRemaining;
        if (elapsed <= 0 || elapsed % TICKS_PER_STEP != 0) return;

        ItemStack target = findTarget(user, mortar);
        if (target.isEmpty()) {
            user.stopUsingItem();
            return;
        }

        if (!level.isClientSide()) {
            ItemIngredient.crush(target);
        }

        boolean done = !ItemIngredient.canCrush(target);
        if (done) {
            level.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.BRUSH_GRAVEL_COMPLETED, SoundSource.PLAYERS, 0.7F, 1.0F);
            user.stopUsingItem();
        } else if ((elapsed / TICKS_PER_STEP) % 2 == 1) {
            level.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.BRUSH_GRAVEL, SoundSource.PLAYERS, 0.55F, 0.85F + level.getRandom().nextFloat() * 0.2F);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BRUSH;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltips, TooltipFlag flag) {
        tooltips.add(Component.translatable("alchemia.tooltip.mortar").withStyle(ChatFormatting.DARK_GRAY));
    }
}
