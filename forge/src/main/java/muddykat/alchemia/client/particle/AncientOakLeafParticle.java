package muddykat.alchemia.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.CherryParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public final class AncientOakLeafParticle extends CherryParticle {

    private AncientOakLeafParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites,
                                   double red, double green, double blue) {
        super(level, x, y, z, sprites);
        setColor((float) red, (float) green, (float) blue);
    }

    public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double red, double green, double blue) {
            return new AncientOakLeafParticle(level, x, y, z, sprites, red, green, blue);
        }
    }
}
