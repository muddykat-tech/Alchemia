package muddykat.alchemia.mixin;

import muddykat.alchemia.client.effect.Dangersense;
import muddykat.alchemia.client.effect.LightCoordsUtil;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    @Inject(method = "getPackedLightCoords", at = @At("HEAD"), cancellable = true)
    private void alchemia$dangerLight(Entity entity, float partialTicks, CallbackInfoReturnable<Integer> result) {
        if (entity instanceof LivingEntity living && Dangersense.threatens(living)) result.setReturnValue(LightCoordsUtil.FULL_BRIGHT);
    }
}
