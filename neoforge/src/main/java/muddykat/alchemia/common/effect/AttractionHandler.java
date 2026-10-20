package muddykat.alchemia.common.effect;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.registration.registers.MobEffectRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Alchemia.MODID)
public final class AttractionHandler {

    private static final double REACH = 1.0;
    private static final double HEIGHT = 0.5;
    private static final double PER_LEVEL = 0.5;

    private AttractionHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !player.isAlive() || player.isSpectator()) return;

        MobEffectInstance instance = player.getEffect(MobEffectRegistry.ATTRACTION);
        if (instance == null) return;

        double scale = 1.0 + PER_LEVEL * (instance.getAmplifier() + 1);
        AABB vanilla = player.getBoundingBox().inflate(REACH, HEIGHT, REACH);
        AABB reach = player.getBoundingBox().inflate(REACH * scale, HEIGHT * scale, REACH * scale);
        for (ItemEntity item : player.level().getEntitiesOfClass(ItemEntity.class, reach,
                item -> item.isAlive() && !vanilla.intersects(item.getBoundingBox()))) {
            item.playerTouch(player);
        }
    }
}
