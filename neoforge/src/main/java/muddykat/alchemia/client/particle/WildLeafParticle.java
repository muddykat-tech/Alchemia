package muddykat.alchemia.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public class WildLeafParticle extends SingleQuadParticle {

    private static final float FADE_IN = 0.12F;
    private static final float FADE_OUT = 0.7F;
    private static final float DRAG = 0.95F;
    private static final float FALL = 0.0025F;
    private static final double SPREAD = 1.006;

    private final float phase;
    private final float spin;
    private final float size;
    private boolean orbiting;
    private @Nullable LivingEntity anchor;
    private double centreX;
    private double centreY;
    private double centreZ;
    private double radius;
    private double angle;
    private double omega;
    private double rise;

    private WildLeafParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z, sprites.get(level.getRandom()));
        this.phase = this.random.nextFloat() * Mth.TWO_PI;
        this.spin = (this.random.nextFloat() - 0.5F) * 0.3F;
        this.size = 0.06F + this.random.nextFloat() * 0.05F;
        this.quadSize = this.size;
        this.roll = this.random.nextFloat() * Mth.TWO_PI;
        this.oRoll = this.roll;
        this.gravity = 0.0F;
        this.friction = DRAG;
        this.hasPhysics = true;
        this.alpha = 0.0F;
    }

    private void orbit(double x, double y, double z, double radius, double rise, double omega) {
        this.orbiting = true;
        this.hasPhysics = false;
        this.centreX = x;
        this.centreY = y;
        this.centreZ = z;
        this.radius = radius;
        this.rise = rise;
        this.omega = omega;
        this.angle = this.random.nextFloat() * Mth.TWO_PI;
        place();
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
    }

    private void place() {
        double ox = centreX;
        double oz = centreZ;
        if (anchor != null) {
            ox += anchor.getX();
            oz += anchor.getZ();
        }
        double baseY = anchor == null ? centreY : anchor.getY() + centreY;
        setPos(ox + Math.cos(angle) * radius, baseY, oz + Math.sin(angle) * radius);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.oRoll = this.roll;
        if (this.age++ >= this.lifetime) {
            remove();
            return;
        }
        float life = (float) this.age / this.lifetime;
        this.alpha = life < FADE_IN ? life / FADE_IN : life > FADE_OUT ? 1.0F - (life - FADE_OUT) / (1.0F - FADE_OUT) : 1.0F;
        this.roll += this.spin * (1.0F + 0.6F * Mth.sin(this.age * 0.2F + this.phase));

        if (orbiting && anchor != null && !anchor.isAlive()) {
            centreX += anchor.getX();
            centreY += anchor.getY();
            centreZ += anchor.getZ();
            anchor = null;
        }
        if (orbiting) {
            angle += omega;
            radius *= SPREAD;
            centreY += rise;
            double fromX = this.x;
            double fromY = this.y;
            double fromZ = this.z;
            place();
            this.xd = this.x - fromX;
            this.yd = this.y - fromY;
            this.zd = this.z - fromZ;
            return;
        }

        this.xd += Mth.sin(this.age * 0.23F + this.phase) * 0.0035F;
        this.zd += Mth.cos(this.age * 0.19F + this.phase) * 0.0035F;
        this.yd -= FALL;
        move(this.xd, this.yd, this.zd);
        this.xd *= DRAG;
        this.yd *= DRAG;
        this.zd *= DRAG;
        if (this.onGround) {
            this.xd *= 0.5;
            this.zd *= 0.5;
        }
    }

    @Override
    public float getQuadSize(float partialTicks) {
        return this.size;
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    public record Free(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xd, double yd, double zd, RandomSource random) {
            WildLeafParticle particle = new WildLeafParticle(level, x, y, z, sprites);
            particle.lifetime = 50 + random.nextInt(40);
            particle.xd = xd;
            particle.yd = yd;
            particle.zd = zd;
            return particle;
        }
    }

    public record Swirl(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double radius, double rise, double omega, RandomSource random) {
            WildLeafParticle particle = new WildLeafParticle(level, x, y, z, sprites);
            if (radius == 1.0 && rise == 1.0 && omega == 1.0) {
                LivingEntity anchor = nearest(level, x, y, z);
                particle.lifetime = 30 + random.nextInt(25);
                if (anchor == null) {
                    particle.yd = 0.02;
                    return particle;
                }
                double dx = x - anchor.getX();
                double dz = z - anchor.getZ();
                double reach = Mth.clamp(Math.sqrt(dx * dx + dz * dz), 0.3, 0.8) + anchor.getBbWidth() * 0.3;
                particle.anchor = anchor;
                particle.orbit(0.0, y - anchor.getY(), 0.0, reach, 0.012 + random.nextFloat() * 0.02,
                        (random.nextBoolean() ? 1.0 : -1.0) * (0.12 + random.nextFloat() * 0.1));
                particle.angle = Math.atan2(dz, dx);
                particle.place();
                particle.xo = particle.x;
                particle.yo = particle.y;
                particle.zo = particle.z;
                return particle;
            }
            particle.lifetime = 40 + random.nextInt(30);
            particle.orbit(x, y, z, radius, rise, omega);
            return particle;
        }

        private static @Nullable LivingEntity nearest(ClientLevel level, double x, double y, double z) {
            LivingEntity best = null;
            double bestDistance = Double.MAX_VALUE;
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(x, y, z, x, y, z).inflate(1.2))) {
                double distance = entity.distanceToSqr(x, entity.getY(), z);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = entity;
                }
            }
            return best;
        }
    }
}
