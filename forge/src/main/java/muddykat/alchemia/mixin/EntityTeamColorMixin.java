package muddykat.alchemia.mixin;

import muddykat.alchemia.client.effect.TreasureVision;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityTeamColorMixin {

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void alchemia$treasureColour(CallbackInfoReturnable<Integer> result) {
        Entity self = (Entity) (Object) this;
        if (self.level().isClientSide() && TreasureVision.outlined(self)) result.setReturnValue(TreasureVision.TREASURE_RGB);
    }
}
