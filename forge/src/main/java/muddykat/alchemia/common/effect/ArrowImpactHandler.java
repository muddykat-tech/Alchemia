package muddykat.alchemia.common.effect;

import muddykat.alchemia.Alchemia;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.util.Set;

@Mod.EventBusSubscriber(modid = Alchemia.MODID)
public final class ArrowImpactHandler {

    private static final String LANDED = "alchemia_arrow_landed";
    private static final String POTION_FIELD = "f_36855_";
    private static final String EFFECTS_FIELD = "f_36852_";

    private ArrowImpactHandler() {
    }

    @SubscribeEvent
    public static void onImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof Arrow arrow)) return;
        if (!(arrow.level() instanceof ServerLevel level) || arrow.getPersistentData().getBoolean(LANDED)) return;

        Potion potion = ObfuscationReflectionHelper.getPrivateValue(Arrow.class, arrow, POTION_FIELD);
        Set<MobEffectInstance> effects = ObfuscationReflectionHelper.getPrivateValue(Arrow.class, arrow, EFFECTS_FIELD);
        if (potion == null || effects == null) return;

        LivingEntity hit = event.getRayTraceResult() instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity living
                ? living : null;
        boolean landed = false;
        for (MobEffectInstance instance : PotionUtils.getAllEffects(potion, effects)) {
            if (hit != null && instance.getEffect() instanceof TriggerMobEffect trigger) {
                trigger.struck(level, hit, instance.getAmplifier());
                landed = true;
            }
            if (instance.getEffect() instanceof ShatterEffect shatter) {
                shatter.arrowLanded(level, event.getRayTraceResult().getLocation(), arrow.getOwner(), instance.getAmplifier());
                landed = true;
            }
        }
        if (landed) arrow.getPersistentData().putBoolean(LANDED, true);
    }

    @SubscribeEvent
    public static void onApplicable(MobEffectEvent.Applicable event) {
        if (event.getEffectInstance().getEffect() instanceof TriggerMobEffect) event.setResult(Event.Result.DENY);
    }
}
