package muddykat.alchemia.common.potion;

import muddykat.alchemia.common.utility.PotionStacks;
import muddykat.alchemia.registration.registers.PotionRegistry;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

import java.util.List;
import java.util.Optional;

public final class AlchemicalPotions {

    private AlchemicalPotions() {
    }

    public static ItemStack of(Item item, Holder<MobEffect> effect, int level) {
        AlchemiaEffect definition = AlchemiaEffects.byEffect(effect);
        int ticks = definition != null ? definition.durationTicks() : AlchemiaEffect.DEFAULT_DURATION * 20;
        int amplifier = Math.max(0, (definition != null ? Math.min(level, definition.maxLevel()) : level) - 1);
        ItemStack stack = new ItemStack(item);
        PotionStacks.apply(stack, PotionRegistry.ALCHEMICAL, List.of(new MobEffectInstance(effect, ticks, amplifier)), null);
        return stack;
    }

    public static @Nullable Component arrowName(ItemStack stack) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null || contents.customEffects().isEmpty()) return null;
        if (contents.potion().isEmpty() || !contents.potion().get().is(PotionRegistry.ALCHEMICAL.getKey())) return null;
        return Component.translatable("item.alchemia.tipped_arrow", contents.customEffects().get(0).getEffect().value().getDisplayName());
    }

    public static boolean hollow(ItemStack stack) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null || !contents.customEffects().isEmpty() || contents.potion().isEmpty()) return false;
        Holder<Potion> potion = contents.potion().get();
        return potion.is(PotionRegistry.ALCHEMICAL.getKey()) || potion.is(PotionRegistry.RUINED.getKey()) && stack.is(Items.TIPPED_ARROW);
    }
}
