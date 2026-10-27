package muddykat.alchemia.common.potion;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import org.jspecify.annotations.Nullable;

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
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.empty(),
                List.of(new MobEffectInstance(effect, ticks, amplifier)), Optional.of("alchemical")));
        return stack;
    }

    public static @Nullable Component arrowName(ItemStack stack) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null || !contents.customName().filter("alchemical"::equals).isPresent() || contents.customEffects().isEmpty()) return null;
        return Component.translatable("item.alchemia.tipped_arrow", contents.customEffects().get(0).getEffect().value().getDisplayName());
    }
}
