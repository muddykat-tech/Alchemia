package muddykat.alchemia.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class BrewBubbleParticle extends SingleQuadParticle {

    private static final int MAX_LIFETIME = 60;
    private static final int POP_FRAMES = 5;

    private final SpriteSet sprites;

    private BrewBubbleParticle(ClientLevel level, double x, double y, double z, double red, double green, double blue, SpriteSet sprites) {
        super(level, x, y, z, sprites.first());
        this.sprites = sprites;
        this.lifetime = MAX_LIFETIME - 10 + this.random.nextInt(11);
        this.quadSize = 0.035F + this.random.nextFloat() * 0.025F;
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.xd = (this.random.nextDouble() - 0.5) * 0.002;
        this.yd = 0.0;
        this.zd = (this.random.nextDouble() - 0.5) * 0.002;
        setColor((float) (0.55 + red * 0.45), (float) (0.55 + green * 0.45), (float) (0.55 + blue * 0.45));
        setAlpha(0.85F);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            remove();
            return;
        }

        move(this.xd, 0.0, this.zd);
        int left = this.lifetime - this.age;
        setSprite(this.sprites.get(left < POP_FRAMES ? POP_FRAMES - left : 0, POP_FRAMES));
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double red, double green, double blue, RandomSource random) {
            return new BrewBubbleParticle(level, x, y, z, red, green, blue, sprites);
        }
    }
}
