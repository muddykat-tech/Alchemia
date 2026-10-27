package muddykat.alchemia.common.effect;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.registration.registers.MobEffectRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Alchemia.MODID)
public final class AttractionHandler {

    private static final double REACH = 4.0;
    private static final double PER_LEVEL = 0.5;
    private static final double MAX_SPEED = 0.45;
    private static final double MIN_SPEED = 0.08;
    private static final double SPEED_PER_BLOCK = 0.18;
    private static final double RESPONSE = 0.25;

    private AttractionHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !player.isAlive() || player.isSpectator()) return;

        MobEffectInstance instance = player.getEffect(MobEffectRegistry.ATTRACTION);
        if (instance == null) return;

        double scale = 1.0 + PER_LEVEL * (instance.getAmplifier() + 1);
        double reachDistance = REACH * scale;
        AABB reach = player.getBoundingBox().inflate(reachDistance);
        AABB playerBounds = player.getBoundingBox();
        Vec3 target = playerBounds.getCenter();
        for (ItemEntity item : player.level().getEntitiesOfClass(ItemEntity.class, reach,
                item -> item.isAlive() && !playerBounds.intersects(item.getBoundingBox()))) {
            Vec3 offset = target.subtract(item.position());
            double distanceSqr = offset.lengthSqr();
            if (distanceSqr < 1.0E-6 || distanceSqr > reachDistance * reachDistance) continue;

            double distance = Math.sqrt(distanceSqr);
            double speed = Math.min(MAX_SPEED, Math.max(MIN_SPEED, distance * SPEED_PER_BLOCK));
            Vec3 desired = offset.scale(speed / distance);
            Vec3 velocity = item.getDeltaMovement().scale(1.0 - RESPONSE).add(desired.scale(RESPONSE));
            item.setDeltaMovement(velocity);
        }
    }
}
