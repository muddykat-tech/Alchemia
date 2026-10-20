package muddykat.alchemia.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;

public class NapalmFlameParticle extends SingleQuadParticle {

    private static final int FRAMES = 8;
    private static final float GROW = 0.2F;
    private static final float FADE = 0.65F;

    private final SpriteSet sprites;
    private final int offset;
    private final float size;

    private NapalmFlameParticle(ClientLevel level, double x, double y, double z, double scale, double rise, SpriteSet sprites) {
        super(level, x, y, z, sprites.first());
        this.sprites = sprites;
        this.offset = this.random.nextInt(FRAMES);
        this.size = (float) ((0.16 + this.random.nextFloat() * 0.12) * (scale <= 0.0 ? 1.0 : scale));
        this.quadSize = this.size;
        this.lifetime = 14 + this.random.nextInt(12);
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.friction = 0.92F;
        this.xd = (this.random.nextDouble() - 0.5) * 0.01;
        this.yd = rise > 0.0 ? rise : 0.015 + this.random.nextDouble() * 0.02;
        this.zd = (this.random.nextDouble() - 0.5) * 0.01;
        setSprite(sprites.get(offset, FRAMES - 1));
    }

    @Override
    public void tick() {
        super.tick();
        if (!isAlive()) return;
        float life = (float) this.age / this.lifetime;
        setSprite(this.sprites.get((this.age / 2 + offset) % FRAMES, FRAMES - 1));
        setColor(1.0F, 1.0F - 0.2F * life, 1.0F - 0.4F * life);
        setAlpha(life < FADE ? 1.0F : 1.0F - (life - FADE) / (1.0F - FADE));
        this.xd += (this.random.nextDouble() - 0.5) * 0.004;
        this.zd += (this.random.nextDouble() - 0.5) * 0.004;
    }

    @Override
    public float getQuadSize(float partialTicks) {
        float life = (this.age + partialTicks) / this.lifetime;
        float grow = Math.min(1.0F, life / GROW);
        return this.size * (0.55F + 0.45F * grow) * (1.0F - 0.45F * life * life);
    }

    @Override
    protected int getLightCoords(float partialTicks) {
        return LightCoordsUtil.FULL_BRIGHT;
    }

    @Override
    public FacingCameraMode getFacingCameraMode() {
        return FacingCameraMode.LOOKAT_Y;
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double scale, double rise, double unused, RandomSource random) {
            return new NapalmFlameParticle(level, x, y, z, scale, rise, sprites);
        }
    }
}
