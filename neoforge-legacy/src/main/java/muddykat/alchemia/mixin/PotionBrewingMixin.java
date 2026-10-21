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

    @Inject(method = "hasMix", at = @At("HEAD"), cancellable = true)
    private void alchemia$noMixForAlchemical(ItemStack source, ItemStack ingredient, CallbackInfoReturnable<Boolean> result) {
        if (PotionStacks.alchemical(source)) result.setReturnValue(false);
    }

    @Inject(method = "mix", at = @At("HEAD"), cancellable = true)
    private void alchemia$keepAlchemical(ItemStack ingredient, ItemStack source, CallbackInfoReturnable<ItemStack> result) {
        if (PotionStacks.alchemical(source)) result.setReturnValue(source);
    }
}
