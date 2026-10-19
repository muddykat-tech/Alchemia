package muddykat.alchemia.registration.registers;

import muddykat.alchemia.Alchemia;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ParticleTypeRegistry {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, Alchemia.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BREW_BUBBLE = PARTICLE_TYPES
            .register("brew_bubble", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ANCIENT_OAK_LEAF = PARTICLE_TYPES
            .register("ancient_oak_leaf", () -> new SimpleParticleType(false));
}
