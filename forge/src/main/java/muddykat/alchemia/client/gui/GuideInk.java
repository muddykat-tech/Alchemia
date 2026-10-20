package muddykat.alchemia.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import muddykat.alchemia.Alchemia;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RegisterShadersEvent;
import org.joml.Matrix4f;

import java.io.IOException;

public final class GuideInk {

    public static final int PLAIN = 0xFFFFFFFF;
    public static final int SHORT = 0x70FFFFFF;

    private static final ResourceLocation WIDGETS = new ResourceLocation(Alchemia.MODID, "textures/gui/guide/widgets.png");
    private static final int WATER_BOTTLE_U = 240;
    private static final int WATER_BOTTLE_V = 68;

    private static ShaderInstance shader;

    private GuideInk() {
    }

    public static void register(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation(Alchemia.MODID, "guide_ink_legacy"),
                DefaultVertexFormat.POSITION_TEX_COLOR), loaded -> shader = loaded);
    }

    public static void item(GuiGraphics graphics, ItemStack stack, int x, int y, int colour) {
        if (!sprite(graphics, stack, x, y, colour)) graphics.renderItem(stack, x, y);
    }

    public static void waterBottle(GuiGraphics graphics, int x, int y, int colour) {
        RenderSystem.enableBlend();
        graphics.setColor(red(colour), green(colour), blue(colour), alpha(colour));
        graphics.blit(WIDGETS, x, y, WATER_BOTTLE_U, WATER_BOTTLE_V, 16, 16, 256, 256);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static boolean sprite(GuiGraphics graphics, ItemStack stack, int x, int y, int colour) {
        if (shader == null) return false;
        Minecraft minecraft = Minecraft.getInstance();
        BakedModel model = minecraft.getItemRenderer().getModel(stack, minecraft.level, null, 0);
        TextureAtlasSprite sprite = model.getParticleIcon();
        if (sprite == null || sprite.contents().name().getPath().equals("missingno")) return false;

        RenderSystem.setShader(() -> shader);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Matrix4f pose = graphics.pose().last().pose();
        float r = red(colour), g = green(colour), b = blue(colour), a = alpha(colour);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.vertex(pose, x, y, 0).uv(sprite.getU0(), sprite.getV0()).color(r, g, b, a).endVertex();
        buffer.vertex(pose, x, y + 16, 0).uv(sprite.getU0(), sprite.getV1()).color(r, g, b, a).endVertex();
        buffer.vertex(pose, x + 16, y + 16, 0).uv(sprite.getU1(), sprite.getV1()).color(r, g, b, a).endVertex();
        buffer.vertex(pose, x + 16, y, 0).uv(sprite.getU1(), sprite.getV0()).color(r, g, b, a).endVertex();
        Tesselator.getInstance().end();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        return true;
    }

    private static float red(int colour) {
        return (colour >> 16 & 255) / 255.0F;
    }

    private static float green(int colour) {
        return (colour >> 8 & 255) / 255.0F;
    }

    private static float blue(int colour) {
        return (colour & 255) / 255.0F;
    }

    private static float alpha(int colour) {
        return (colour >>> 24) / 255.0F;
    }
}
