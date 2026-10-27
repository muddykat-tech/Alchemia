package muddykat.alchemia.mixin;

import muddykat.alchemia.client.effect.Dangersense;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Inject(method = "getOverlayCoords", at = @At("HEAD"), cancellable = true)
    private static void alchemia$dangerOverlay(LivingEntity entity, float whiteAlpha, CallbackInfoReturnable<Integer> result) {
        if (Dangersense.threatens(entity)) result.setReturnValue(OverlayTexture.pack(OverlayTexture.u(whiteAlpha), OverlayTexture.v(true)));
    }
}
