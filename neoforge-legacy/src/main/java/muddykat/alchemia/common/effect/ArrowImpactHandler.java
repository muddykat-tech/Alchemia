package muddykat.alchemia.common.effect;

import muddykat.alchemia.Alchemia;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

@EventBusSubscriber(modid = Alchemia.MODID)
public final class ArrowImpactHandler {

    private static final String LANDED = "alchemia_arrow_landed";

    private ArrowImpactHandler() {
    }

    @SubscribeEvent
    public static void onImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof AbstractArrow arrow)) return;
        if (!(arrow.level() instanceof ServerLevel level) || arrow.getPersistentData().getBoolean(LANDED)) return;

        PotionContents contents = arrow.getPickupItemStackOrigin().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        LivingEntity hit = event.getRayTraceResult() instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity living
                ? living : null;
        boolean landed = false;
        for (MobEffectInstance instance : contents.getAllEffects()) {
            if (hit != null && instance.getEffect().value() instanceof TriggerMobEffect trigger) {
                trigger.struck(level, hit, instance.getAmplifier());
                landed = true;
            }
            if (instance.getEffect().value() instanceof ShatterEffect shatter) {
                shatter.arrowLanded(level, event.getRayTraceResult().getLocation(), arrow.getOwner(), instance.getAmplifier());
                landed = true;
            }
        }
        if (landed) arrow.getPersistentData().putBoolean(LANDED, true);
    }

    @SubscribeEvent
    public static void onApplicable(MobEffectEvent.Applicable event) {
        if (event.getEffectInstance().getEffect().value() instanceof TriggerMobEffect) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }
}
