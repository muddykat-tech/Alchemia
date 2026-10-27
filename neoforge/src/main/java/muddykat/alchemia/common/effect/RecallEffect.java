package muddykat.alchemia.common.effect;

import muddykat.alchemia.registration.registers.EntityTypeRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RecallEffect extends TriggerMobEffect {

    private static final int FIELD_TICKS = 240;

    public RecallEffect() {
        super(MobEffectCategory.NEUTRAL, 0x6FA8E8);
    }

    @Override
    public boolean lingers() {
        return true;
    }

    @Override
    protected void drunk(ServerLevel level, LivingEntity drinker, int amplifier) {
        TeleportSearch.recall(drinker, amplifier + 1);
    }

    @Override
    protected void splashed(ServerLevel level, @Nullable Entity owner, LivingEntity mob, int amplifier, double scale) {
        TeleportSearch.recall(mob, amplifier + 1);
    }

    @Override
    public void shattered(ServerLevel level, Vec3 at, @Nullable Entity owner, int amplifier, boolean lingering) {
        Vec3 ground = FlameBurst.ground(level, at);
        TeleportFieldEntity field = new TeleportFieldEntity(EntityTypeRegistry.TELEPORT_FIELD.get(), level);
        field.setPos(ground.x, ground.y, ground.z);
        field.configure(lingering ? TeleportFieldEntity.FIELD : TeleportFieldEntity.SCAR, amplifier + 1,
                lingering ? FIELD_TICKS : TeleportFieldEntity.SCAR_TICKS);
        field.recall();
        level.addFreshEntity(field);
    }
}
