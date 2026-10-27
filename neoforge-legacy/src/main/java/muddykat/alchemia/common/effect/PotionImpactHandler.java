package muddykat.alchemia.common.effect;

import muddykat.alchemia.Alchemia;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.LevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;

@EventBusSubscriber(modid = Alchemia.MODID)
public final class PotionImpactHandler {

    private PotionImpactHandler() {
    }

    @SubscribeEvent
    public static void onImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof ThrownPotion potion)) return;
        if (!(potion.level() instanceof ServerLevel level)) return;

        PotionContents contents = potion.getItem().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        boolean lingering = potion.getItem().is(Items.LINGERING_POTION);
        boolean triggersOnly = contents.hasEffects();
        for (MobEffectInstance instance : contents.getAllEffects()) {
            if (instance.getEffect().value() instanceof ShatterEffect shatter) {
                shatter.shattered(level, event.getRayTraceResult().getLocation(), potion.getOwner(), instance.getAmplifier(), lingering);
            }
            if (!(instance.getEffect().value() instanceof TriggerMobEffect)) triggersOnly = false;
        }

        if (lingering && triggersOnly) {
            level.levelEvent(LevelEvent.PARTICLES_SPELL_POTION_SPLASH, potion.blockPosition(), contents.getColor());
#if MC_VER >= 26300
            if (!potion.isSilent()) level.levelEvent(LevelEvent.SOUND_SPELL_POTION_SPLASH, potion.blockPosition(), 0);
#endif
            potion.discard();
            event.setCanceled(true);
        }
    }
}
