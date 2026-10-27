package muddykat.alchemia.common.potion;

import muddykat.alchemia.common.utility.PotionStacks;
import muddykat.alchemia.registration.registers.PotionRegistry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

import java.util.List;

public final class AlchemicalPotions {

    private AlchemicalPotions() {
    }

    public static ItemStack of(Item item, MobEffect effect, int level) {
        AlchemiaEffect definition = AlchemiaEffects.byEffect(effect);
        int ticks = definition != null ? definition.durationTicks() : AlchemiaEffect.DEFAULT_DURATION * 20;
        int amplifier = Math.max(0, (definition != null ? Math.min(level, definition.maxLevel()) : level) - 1);
        ItemStack stack = new ItemStack(item);
        PotionStacks.apply(stack, PotionRegistry.ALCHEMICAL.get(), List.of(new MobEffectInstance(effect, ticks, amplifier)), null);
        return stack;
    }

    public static @Nullable Component arrowName(ItemStack stack) {
        if (PotionUtils.getPotion(stack) != PotionRegistry.ALCHEMICAL.get()) return null;
        List<MobEffectInstance> effects = PotionUtils.getCustomEffects(stack);
        if (effects.isEmpty()) return null;
        return Component.translatable("item.alchemia.tipped_arrow", effects.get(0).getEffect().getDisplayName());
    }

    public static boolean hollow(ItemStack stack) {
        if (!PotionUtils.getCustomEffects(stack).isEmpty()) return false;
        Potion potion = PotionUtils.getPotion(stack);
        return potion == PotionRegistry.ALCHEMICAL.get() || potion == PotionRegistry.RUINED.get() && stack.is(Items.TIPPED_ARROW);
    }
}
