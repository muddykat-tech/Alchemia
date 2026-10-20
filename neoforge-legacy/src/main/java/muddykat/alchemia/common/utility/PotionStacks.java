package muddykat.alchemia.common.utility;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.List;
import java.util.Optional;

public class PotionStacks {

    public static void apply(ItemStack stack, Holder<Potion> potion, List<MobEffectInstance> effects, Integer color) {
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.of(potion), Optional.ofNullable(color),
                effects == null ? List.of() : List.copyOf(effects)));
    }

    public static void glint(ItemStack stack) {
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    public static boolean isPotion(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof PotionItem;
    }

    public static List<MobEffectInstance> effectsOf(ItemStack stack) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) return List.of();
        List<MobEffectInstance> effects = new java.util.ArrayList<>();
        contents.getAllEffects().forEach(effects::add);
        return effects;
    }

    public static int colorOf(ItemStack stack) {
        return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getColor();
    }
}
