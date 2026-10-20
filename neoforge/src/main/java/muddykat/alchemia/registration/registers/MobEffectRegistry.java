package muddykat.alchemia.registration.registers;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.effect.AlchemiaMobEffect;
import muddykat.alchemia.common.effect.FlamesEffect;
import muddykat.alchemia.common.effect.ReturningEffect;
import muddykat.alchemia.common.effect.TeleportationEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MobEffectRegistry {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Alchemia.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> RETURNING = MOB_EFFECTS.register("returning", ReturningEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> TELEPORTATION = MOB_EFFECTS.register("teleportation", TeleportationEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> ATTRACTION = MOB_EFFECTS.register("attraction",
            () -> new AlchemiaMobEffect(MobEffectCategory.BENEFICIAL, 0xD8A23A));
    public static final DeferredHolder<MobEffect, MobEffect> TREASURE_VISION = MOB_EFFECTS.register("treasure_vision",
            () -> new AlchemiaMobEffect(MobEffectCategory.BENEFICIAL, 0xF3D35B));
    public static final DeferredHolder<MobEffect, MobEffect> DANGERSENSE = MOB_EFFECTS.register("dangersense",
            () -> new AlchemiaMobEffect(MobEffectCategory.BENEFICIAL, 0xC8332B));
    public static final DeferredHolder<MobEffect, MobEffect> FLAMES = MOB_EFFECTS.register("flames", FlamesEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> THORNS = MOB_EFFECTS.register("thorns",
            () -> new AlchemiaMobEffect(MobEffectCategory.BENEFICIAL, 0x5E8C3A));
}
