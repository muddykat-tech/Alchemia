package muddykat.alchemia.common.utility;

import muddykat.alchemia.registration.registers.PotionRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;

import java.util.List;

public class PotionStacks {
    public static final String COLOR_TAG = "CustomPotionColor";

    public static void apply(ItemStack stack, Potion potion, List<MobEffectInstance> effects, Integer color) {
        PotionUtils.setPotion(stack, potion);
        if (effects != null && !effects.isEmpty()) PotionUtils.setCustomEffects(stack, effects);
        if (color != null) stack.getOrCreateTag().putInt(COLOR_TAG, color);
    }

    public static void glint(ItemStack stack) {
        ListTag marks = new ListTag();
        marks.add(new CompoundTag());
        stack.getOrCreateTag().put(ItemStack.TAG_ENCH, marks);
    }

    public static boolean isPotion(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof PotionItem;
    }

    public static boolean alchemical(ItemStack stack) {
        if (!isPotion(stack)) return false;
        Potion potion = PotionUtils.getPotion(stack);
        return potion == PotionRegistry.ALCHEMICAL.get() || potion == PotionRegistry.RUINED.get();
    }

    public static List<MobEffectInstance> effectsOf(ItemStack stack) {
        return PotionUtils.getMobEffects(stack);
    }

    public static int colorOf(ItemStack stack) {
        return PotionUtils.getColor(stack);
    }
}
