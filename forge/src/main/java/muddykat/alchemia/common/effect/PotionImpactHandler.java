package muddykat.alchemia.common.effect;

import muddykat.alchemia.Alchemia;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import java.util.List;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.ProjectileImpactEvent;

@Mod.EventBusSubscriber(modid = Alchemia.MODID)
public final class PotionImpactHandler {

    private PotionImpactHandler() {
    }

    @SubscribeEvent
    public static void onImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof ThrownPotion potion)) return;
        if (!(potion.level() instanceof ServerLevel level)) return;

        List<MobEffectInstance> effects = PotionUtils.getMobEffects(potion.getItem());
        boolean lingering = potion.getItem().is(Items.LINGERING_POTION);
        boolean triggersOnly = !effects.isEmpty();
        for (MobEffectInstance instance : effects) {
            if (instance.getEffect() instanceof ShatterEffect shatter) {
                shatter.shattered(level, event.getRayTraceResult().getLocation(), potion.getOwner(), instance.getAmplifier(), lingering);
            }
            if (!(instance.getEffect() instanceof TriggerMobEffect)) triggersOnly = false;
        }

        if (lingering && triggersOnly) {
            level.levelEvent(LevelEvent.PARTICLES_SPELL_POTION_SPLASH, potion.blockPosition(), PotionUtils.getColor(potion.getItem()));
#if MC_VER >= 26300
            if (!potion.isSilent()) level.levelEvent(LevelEvent.SOUND_SPELL_POTION_SPLASH, potion.blockPosition(), 0);
#endif
            potion.discard();
            event.setCanceled(true);
        }
    }
}
