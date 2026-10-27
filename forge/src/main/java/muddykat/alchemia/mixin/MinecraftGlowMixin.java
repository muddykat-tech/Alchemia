package muddykat.alchemia.mixin;

import muddykat.alchemia.client.effect.TreasureVision;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftGlowMixin {

    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
    private void alchemia$treasureOutline(Entity entity, CallbackInfoReturnable<Boolean> result) {
        if (TreasureVision.outlined(entity)) result.setReturnValue(true);
    }
}
