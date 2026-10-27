package muddykat.alchemia.mixin;

import muddykat.alchemia.common.potion.AlchemicalPotions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TippedArrowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TippedArrowItem.class)
public abstract class TippedArrowNameMixin {

    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    private void alchemia$effectName(ItemStack stack, CallbackInfoReturnable<Component> result) {
        Component named = AlchemicalPotions.arrowName(stack);
        if (named != null) result.setReturnValue(named);
    }
}
