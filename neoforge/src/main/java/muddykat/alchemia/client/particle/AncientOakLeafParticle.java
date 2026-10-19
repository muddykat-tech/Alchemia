package muddykat.alchemia.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FallingLeavesParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public final class AncientOakLeafParticle {

    private AncientOakLeafParticle() {
    }

    public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double red, double green, double blue, RandomSource random) {
            FallingLeavesParticle particle = new FallingLeavesParticle(level, x, y, z, sprites.get(random),
                    0.07F, 10.0F, true, false, 2.0F, 0.021F);
            particle.setColor((float) red, (float) green, (float) blue);
            return particle;
        }
    }
}
