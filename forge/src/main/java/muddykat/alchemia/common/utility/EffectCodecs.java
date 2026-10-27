package muddykat.alchemia.common.utility;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public final class EffectCodecs {

    public static final Codec<MobEffectInstance> INSTANCE = CompoundTag.CODEC.flatXmap(EffectCodecs::load,
            instance -> DataResult.success(instance.save(new CompoundTag())));

    private EffectCodecs() {
    }

    public static String name(MobEffect effect) {
        return String.valueOf(BuiltInRegistries.MOB_EFFECT.getKey(effect));
    }

    private static DataResult<MobEffectInstance> load(CompoundTag tag) {
        MobEffectInstance instance = MobEffectInstance.load(tag);
        return instance == null ? DataResult.error(() -> "Unknown mob effect in " + tag) : DataResult.success(instance);
    }
}
