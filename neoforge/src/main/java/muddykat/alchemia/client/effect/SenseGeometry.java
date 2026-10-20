package muddykat.alchemia.client.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import muddykat.alchemia.Alchemia;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class SenseGeometry {

    public static final Identifier WHITE = Identifier.fromNamespaceAndPath(Alchemia.MODID, "textures/effect/white.png");
    public static final Identifier GLOW = Identifier.fromNamespaceAndPath(Alchemia.MODID, "textures/effect/glow.png");

    private SenseGeometry() {
    }

    public static float throughWalls(int depth) {
        if (depth <= 0) return 1.0F;
        return Math.max(0.22F, 0.65F * (float) Math.pow(0.88, depth - 1));
    }

    public static int wallDepth(Level level, Vec3 from, BlockPos target, int limit) {
        Vec3 to = Vec3.atCenterOf(target);
        Vec3 step = to.subtract(from);
        double length = step.length();
        if (length < 1.0E-4) return 0;

        int x = BlockPos.containing(from).getX();
        int y = BlockPos.containing(from).getY();
        int z = BlockPos.containing(from).getZ();
        int stepX = (int) Math.signum(step.x);
        int stepY = (int) Math.signum(step.y);
        int stepZ = (int) Math.signum(step.z);
        double deltaX = stepX == 0 ? Double.MAX_VALUE : Math.abs(length / step.x);
        double deltaY = stepY == 0 ? Double.MAX_VALUE : Math.abs(length / step.y);
        double deltaZ = stepZ == 0 ? Double.MAX_VALUE : Math.abs(length / step.z);
        double nextX = stepX == 0 ? Double.MAX_VALUE : deltaX * (stepX > 0 ? Math.floor(from.x) + 1 - from.x : from.x - Math.floor(from.x));
        double nextY = stepY == 0 ? Double.MAX_VALUE : deltaY * (stepY > 0 ? Math.floor(from.y) + 1 - from.y : from.y - Math.floor(from.y));
        double nextZ = stepZ == 0 ? Double.MAX_VALUE : deltaZ * (stepZ > 0 ? Math.floor(from.z) + 1 - from.z : from.z - Math.floor(from.z));

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int solid = 0;
        for (int guard = 0; guard < 512; guard++) {
            if (nextX < nextY && nextX < nextZ) {
                x += stepX;
                nextX += deltaX;
            } else if (nextY < nextZ) {
                y += stepY;
                nextY += deltaY;
            } else {
                z += stepZ;
                nextZ += deltaZ;
            }
            if (x == target.getX() && y == target.getY() && z == target.getZ()) return solid;
            if (level.getBlockState(cursor.set(x, y, z)).canOcclude() && ++solid > limit) return solid;
        }
        return solid;
    }

    public static void billboard(PoseStack.Pose pose, VertexConsumer buffer, Vec3 centre, Quaternionf facing, float halfWidth,
                                 float halfHeight, int colour) {
        billboard(pose, buffer, centre, facing, halfWidth, halfHeight, colour, 1.0F, 1.0F);
    }

    public static void billboard(PoseStack.Pose pose, VertexConsumer buffer, Vec3 centre, Quaternionf facing, float halfWidth,
                                 float halfHeight, int colour, float maxU, float maxV) {
        Vector3f right = facing.transform(new Vector3f(halfWidth, 0.0F, 0.0F));
        Vector3f up = facing.transform(new Vector3f(0.0F, halfHeight, 0.0F));
        float x = (float) centre.x;
        float y = (float) centre.y;
        float z = (float) centre.z;
        float[][] corners = {
                {x - right.x - up.x, y - right.y - up.y, z - right.z - up.z, maxU, maxV},
                {x - right.x + up.x, y - right.y + up.y, z - right.z + up.z, maxU, 0.0F},
                {x + right.x + up.x, y + right.y + up.y, z + right.z + up.z, 0.0F, 0.0F},
                {x + right.x - up.x, y + right.y - up.y, z + right.z - up.z, 0.0F, maxV}};
        for (int index = 0; index < 4; index++) textured(pose, buffer, corners[index], colour);
        for (int index = 3; index >= 0; index--) textured(pose, buffer, corners[index], colour);
    }

    public static final int ALL_SIDES = 0x3F;
    private static final float SHELL_LIFT = 0.015F;
    private static final Direction[] SIDES = Direction.values();

    public static void shell(PoseStack.Pose pose, VertexConsumer buffer, AABB box, int open, Vec3 camera, int colour) {
        for (Direction side : SIDES) {
            if ((open & 1 << side.ordinal()) == 0) continue;
            Direction.Axis axis = side.getAxis();
            double plane = side.getAxisDirection() == Direction.AxisDirection.POSITIVE ? box.max(axis) : box.min(axis);
            if ((camera.get(axis) - plane) * side.getAxisDirection().getStep() <= 0.0) continue;

            Direction.Axis first = axis == Direction.Axis.X ? Direction.Axis.Y : Direction.Axis.X;
            Direction.Axis second = axis == Direction.Axis.Z ? Direction.Axis.Y : Direction.Axis.Z;
            double offset = plane + SHELL_LIFT * side.getAxisDirection().getStep();
            double[][] corners = {
                    {box.min(first), box.min(second)}, {box.max(first), box.min(second)},
                    {box.max(first), box.max(second)}, {box.min(first), box.max(second)}};
            for (double[] corner : corners) {
                double[] at = new double[3];
                at[axis.ordinal()] = offset;
                at[first.ordinal()] = corner[0];
                at[second.ordinal()] = corner[1];
                SenseShader.vertex(buffer.addVertex(pose, (float) (at[0] - camera.x), (float) (at[1] - camera.y), (float) (at[2] - camera.z)),
                        colour, (float) (corner[0] - Math.floor(corner[0])), (float) (corner[1] - Math.floor(corner[1])));
            }
        }
    }

    public static void plainBox(PoseStack.Pose pose, VertexConsumer buffer, AABB box, int colour) {
        for (float[][] face : faces(box)) {
            for (float[] corner : face) buffer.addVertex(pose, corner[0], corner[1], corner[2]).setColor(colour);
        }
    }

    private static void textured(PoseStack.Pose pose, VertexConsumer buffer, float[] corner, int colour) {
        buffer.addVertex(pose, corner[0], corner[1], corner[2]).setColor(colour).setUv(corner[3], corner[4]).setLight(LightCoordsUtil.FULL_BRIGHT);
    }

    private static float[][][] faces(AABB box) {
        float x0 = (float) box.minX;
        float y0 = (float) box.minY;
        float z0 = (float) box.minZ;
        float x1 = (float) box.maxX;
        float y1 = (float) box.maxY;
        float z1 = (float) box.maxZ;
        return new float[][][]{
                {{x0, y0, z0, 0, 0}, {x1, y0, z0, 1, 0}, {x1, y0, z1, 1, 1}, {x0, y0, z1, 0, 1}},
                {{x0, y1, z0, 0, 0}, {x0, y1, z1, 0, 1}, {x1, y1, z1, 1, 1}, {x1, y1, z0, 1, 0}},
                {{x0, y0, z0, 0, 0}, {x0, y1, z0, 0, 1}, {x1, y1, z0, 1, 1}, {x1, y0, z0, 1, 0}},
                {{x0, y0, z1, 0, 0}, {x1, y0, z1, 1, 0}, {x1, y1, z1, 1, 1}, {x0, y1, z1, 0, 1}},
                {{x0, y0, z0, 0, 0}, {x0, y0, z1, 1, 0}, {x0, y1, z1, 1, 1}, {x0, y1, z0, 0, 1}},
                {{x1, y0, z0, 0, 0}, {x1, y1, z0, 0, 1}, {x1, y1, z1, 1, 1}, {x1, y0, z1, 1, 0}}};
    }
}
