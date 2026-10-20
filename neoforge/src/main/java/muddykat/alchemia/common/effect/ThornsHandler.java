package muddykat.alchemia.common.effect;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.registration.registers.MobEffectRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = Alchemia.MODID)
public final class ThornsHandler {

    private static final float PER_LEVEL = 0.25F;
    private static final float MAX_RATIO = 1.25F;

    private ThornsHandler() {
    }

    @SubscribeEvent
    public static void onDamaged(LivingDamageEvent.Post event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel level)) return;

        MobEffectInstance instance = victim.getEffect(MobEffectRegistry.THORNS);
        if (instance == null) return;

        DamageSource source = event.getSource();
        if (source.is(DamageTypes.THORNS)) return;
        if (!(source.getEntity() instanceof LivingEntity attacker) || attacker == victim) return;
        if (source.getDirectEntity() != attacker) return;

        float ratio = Math.min(MAX_RATIO, PER_LEVEL * (instance.getAmplifier() + 1));
        float reflected = event.getInflictedDamage() * ratio;
        if (reflected > 0.0F) attacker.hurtServer(level, level.damageSources().thorns(victim), reflected);
    }
}
