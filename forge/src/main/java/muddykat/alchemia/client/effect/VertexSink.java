package muddykat.alchemia.client.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class VertexSink {

    private final VertexConsumer out;
    private final VertexFormat format;
    private boolean open;
    private float x;
    private float y;
    private float z;
    private int colour;
    private float u;
    private float v;
    private int overlay;
    private int light;
    private float normalX;
    private float normalY;
    private float normalZ;

    private VertexSink(VertexConsumer out, VertexFormat format) {
        this.out = out;
        this.format = format;
    }

    public static VertexSink of(VertexConsumer out, VertexFormat format) {
        return new VertexSink(out, format);
    }

    public VertexSink addVertex(PoseStack.Pose pose, float x, float y, float z) {
        return addVertex(pose.pose(), x, y, z);
    }

    public VertexSink addVertex(Matrix4f matrix, float x, float y, float z) {
        Vector4f at = matrix.transform(new Vector4f(x, y, z, 1.0F));
        return addVertex(at.x(), at.y(), at.z());
    }

    public VertexSink addVertex(float x, float y, float z) {
        flush();
        this.open = true;
        this.x = x;
        this.y = y;
        this.z = z;
        this.colour = 0xFFFFFFFF;
        this.u = 0.0F;
        this.v = 0.0F;
        this.overlay = OverlayTexture.NO_OVERLAY;
        this.light = LightCoordsUtil.FULL_BRIGHT;
        this.normalX = 0.0F;
        this.normalY = 1.0F;
        this.normalZ = 0.0F;
        return this;
    }

    public VertexSink setColor(int argb) {
        this.colour = argb;
        return this;
    }

    public VertexSink setColor(float red, float green, float blue, float alpha) {
        return setColor(ARGB.colorFromFloat(alpha, red, green, blue));
    }

    public VertexSink setUv(float u, float v) {
        this.u = u;
        this.v = v;
        return this;
    }

    public VertexSink setOverlay(int overlay) {
        this.overlay = overlay;
        return this;
    }

    public VertexSink setLight(int light) {
        this.light = light;
        return this;
    }

    public VertexSink setNormal(PoseStack.Pose pose, float x, float y, float z) {
        Vector3f normal = pose.normal().transform(new Vector3f(x, y, z));
        return setNormal(normal.x(), normal.y(), normal.z());
    }

    public VertexSink setNormal(float x, float y, float z) {
        this.normalX = x;
        this.normalY = y;
        this.normalZ = z;
        return this;
    }

    public void flush() {
        if (!open) return;
        open = false;
        out.vertex(x, y, z);
        for (VertexFormatElement element : format.getElements()) {
            switch (element.getUsage()) {
                case COLOR -> out.color(ARGB.red(colour), ARGB.green(colour), ARGB.blue(colour), ARGB.alpha(colour));
                case UV -> {
                    if (element.getIndex() == 0) out.uv(u, v);
                    else if (element.getIndex() == 1) out.overlayCoords(overlay);
                    else out.uv2(light);
                }
                case NORMAL -> out.normal(normalX, normalY, normalZ);
                default -> {
                }
            }
        }
        out.endVertex();
    }
}
