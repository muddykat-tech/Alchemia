package muddykat.alchemia.mixin;

import muddykat.alchemia.common.utility.PotionStacks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionBrewing.class)
public abstract class PotionBrewingMixin {

    @Inject(method = "mix", at = @At("HEAD"), cancellable = true)
    private static void alchemia$keepAlchemical(ItemStack ingredient, ItemStack potion, CallbackInfoReturnable<ItemStack> result) {
        if (PotionStacks.alchemical(potion)) result.setReturnValue(potion);
    }
}
