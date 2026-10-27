package muddykat.alchemia.client.effect;

import muddykat.alchemia.common.effect.PortalTimeline;
import muddykat.alchemia.common.effect.ReturnPortalEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class PortalParticles {

    private static final int SLAM_BURST = 36;
    private static final double FRONT_OFFSET = 0.06;
    private static final double REACH_X = ReturnPortalRenderer.HOLE * ReturnPortalRenderer.PLANE_WIDTH * 0.85;
    private static final double REACH_Y = ReturnPortalRenderer.HOLE * ReturnPortalRenderer.PLANE_HEIGHT * 0.85;

    private PortalParticles() {
    }

    public static void tick(ReturnPortalEntity portal) {
        if (!portal.faces(camera())) return;

        Level level = portal.level();
        RandomSource random = portal.getRandom();
        Vec3 normal = portal.normal();
        double yaw = Math.toRadians(portal.getYRot());
        double rightX = Math.cos(yaw);
        double rightZ = Math.sin(yaw);
        double centreY = portal.getY() + ReturnPortalEntity.HALF_HEIGHT;
        double frontX = portal.getX() - normal.x * FRONT_OFFSET;
        double frontZ = portal.getZ() - normal.z * FRONT_OFFSET;

        if (portal.collapse() == PortalTimeline.SLAM_TICK) {
            for (int index = 0; index < SLAM_BURST; index++) {
                double angle = random.nextDouble() * 2 * Math.PI;
                double reach = Math.sqrt(random.nextDouble());
                double across = Math.cos(angle) * REACH_X * reach;
                double up = Math.sin(angle) * REACH_Y * reach;
                level.addParticle(ParticleTypes.END_ROD, frontX + rightX * across, centreY + up, frontZ + rightZ * across,
                        rightX * across * 0.08 - normal.x * 0.04, up * 0.08, rightZ * across * 0.08 - normal.z * 0.04);
            }
        }
    }

    private static Vec3 camera() {
#if MC_VER >= 26200
        return Minecraft.getInstance().gameRenderer.mainCamera().position();
#else
        return Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
#endif
    }
}
