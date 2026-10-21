package muddykat.alchemia.mixin;

#if MC_VER < 26300
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(PotionBrewing.class)
public abstract class PotionBrewingMixin {

    private static final Set<String> ALCHEMIA_POTIONS = Set.of("alchemical", "ruined");

    @Inject(method = "hasMix", at = @At("HEAD"), cancellable = true)
    private void alchemia$noMixForAlchemical(ItemStack source, ItemStack ingredient, CallbackInfoReturnable<Boolean> result) {
        PotionContents contents = source.get(DataComponents.POTION_CONTENTS);
        if (contents != null && contents.potion().isEmpty() && contents.customName().filter(ALCHEMIA_POTIONS::contains).isPresent()) {
            result.setReturnValue(false);
        }
    }
}
#endif
