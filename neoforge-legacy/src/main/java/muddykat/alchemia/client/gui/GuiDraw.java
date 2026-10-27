package muddykat.alchemia.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import muddykat.alchemia.common.potion.AlchemiaEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import org.joml.Matrix4f;
import org.joml.Vector2ic;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.List;

public final class GuiDraw {

    private static final String EFFECT_PREFIX = "mob_effect/";
    private static final float TOOLTIP_Z = 400.0F;
    private static final int TOOLTIP_INSET = 12;

    private GuiDraw() {
    }

    public static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, float u, float v, int width, int height,
                            int textureWidth, int textureHeight) {
        RenderSystem.enableBlend();
        graphics.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);
    }

    public static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, float u, float v, int width, int height,
                            int textureWidth, int textureHeight, int argb) {
        tinted(graphics, argb, () -> graphics.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight));
    }

    public static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, float u, float v, int width, int height,
                            int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        RenderSystem.enableBlend();
        graphics.blit(texture, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
    }

    public static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, float u, float v, int width, int height,
                            int regionWidth, int regionHeight, int textureWidth, int textureHeight, int argb) {
        tinted(graphics, argb, () -> graphics.blit(texture, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight));
    }

    public static void blitUv(GuiGraphics graphics, ResourceLocation texture, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1) {
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        Matrix4f pose = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(pose, x0, y0, 0).setUv(u0, v0);
        buffer.addVertex(pose, x0, y1, 0).setUv(u0, v1);
        buffer.addVertex(pose, x1, y1, 0).setUv(u1, v1);
        buffer.addVertex(pose, x1, y0, 0).setUv(u1, v0);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    public static void sprite(GuiGraphics graphics, AlchemiaSprites.Sprite sprite, int x, int y, int width, int height) {
        sprite.blit(graphics, x, y, width, height);
    }

    public static void sprite(GuiGraphics graphics, AlchemiaSprites.Sprite sprite, int x, int y, int width, int height, int argb) {
        sprite.blit(graphics, x, y, width, height, argb);
    }

    public static void sprite(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height) {
        sprite(graphics, sprite, x, y, width, height, -1);
    }

    public static void sprite(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height, int argb) {
        if (sprite.getPath().startsWith(EFFECT_PREFIX)) {
            ResourceLocation effect = sprite.withPath(sprite.getPath().substring(EFFECT_PREFIX.length()));
            var holder = BuiltInRegistries.MOB_EFFECT.getHolder(effect);
            if (holder.isPresent()) {
                effect(graphics, holder.get(), x, y, width, height, argb);
                return;
            }
        }
        tinted(graphics, argb, () -> graphics.blitSprite(sprite, x, y, width, height));
    }

    public static void nextStratum(GuiGraphics graphics) {
        graphics.flush();
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
    }

    public static void sprite(GuiGraphics graphics, TextureAtlasSprite sprite, int x, int y, int width, int height) {
        sprite(graphics, sprite, x, y, width, height, -1);
    }

    public static void sprite(GuiGraphics graphics, TextureAtlasSprite sprite, int x, int y, int width, int height, int argb) {
        tinted(graphics, argb, () -> graphics.blit(x, y, 0, width, height, sprite));
    }

    public static void effect(GuiGraphics graphics, AlchemiaEffect effect, int x, int y, int width, int height) {
        effect(graphics, effect.effect(), x, y, width, height, -1);
    }

    public static void effect(GuiGraphics graphics, AlchemiaEffect effect, int x, int y, int width, int height, int argb) {
        effect(graphics, effect.effect(), x, y, width, height, argb);
    }

    public static void effect(GuiGraphics graphics, Holder<MobEffect> effect, int x, int y, int width, int height) {
        effect(graphics, effect, x, y, width, height, -1);
    }

    public static void effect(GuiGraphics graphics, Holder<MobEffect> effect, int x, int y, int width, int height, int argb) {
        sprite(graphics, Minecraft.getInstance().getMobEffectTextures().get(effect), x, y, width, height, argb);
    }

    public static void tinted(GuiGraphics graphics, int argb, Runnable draw) {
        RenderSystem.enableBlend();
        graphics.setColor((argb >> 16 & 255) / 255.0F, (argb >> 8 & 255) / 255.0F, (argb & 255) / 255.0F, (argb >>> 24) / 255.0F);
        draw.run();
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void scissor(GuiGraphics graphics, int x0, int y0, int x1, int y1) {
        Matrix4f pose = graphics.pose().last().pose();
        Vector4f from = pose.transform(new Vector4f(x0, y0, 0.0F, 1.0F));
        Vector4f to = pose.transform(new Vector4f(x1, y1, 0.0F, 1.0F));
        graphics.enableScissor(Mth.floor(Math.min(from.x(), to.x())), Mth.floor(Math.min(from.y(), to.y())),
                Mth.ceil(Math.max(from.x(), to.x())), Mth.ceil(Math.max(from.y(), to.y())));
    }

    public static void tooltip(GuiGraphics graphics, Font font, List<?> lines, int x, int y) {
        if (lines.isEmpty()) return;
        if (lines.get(0) instanceof Component) {
            @SuppressWarnings("unchecked")
            List<Component> components = (List<Component>) lines;
            graphics.renderComponentTooltip(font, components, x, y);
        } else {
            @SuppressWarnings("unchecked")
            List<FormattedCharSequence> sequences = (List<FormattedCharSequence>) lines;
            graphics.renderTooltip(font, sequences, x, y);
        }
    }

    public static void tooltip(GuiGraphics graphics, Font font, List<ClientTooltipComponent> lines, int x, int y,
                               ClientTooltipPositioner positioner, @Nullable ResourceLocation style) {
        if (lines.isEmpty()) return;
        int width = 0;
        int height = lines.size() == 1 ? -2 : 0;
        for (ClientTooltipComponent line : lines) {
            width = Math.max(width, line.getWidth(font));
            height += line.getHeight();
        }
        Vector2ic at = positioner.positionTooltip(graphics.guiWidth(), graphics.guiHeight(), x, y, width, height);
        int left = at.x();
        int top = at.y();

        graphics.flush();
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, TOOLTIP_Z);
        if (style == null) {
            TooltipRenderUtil.renderTooltipBackground(graphics, left, top, width, height, 0);
        } else {
            int outerX = left - TOOLTIP_INSET;
            int outerY = top - TOOLTIP_INSET;
            int outerW = width + TOOLTIP_INSET * 2;
            int outerH = height + TOOLTIP_INSET * 2;
            RenderSystem.enableBlend();
            graphics.blitSprite(style.withPath(path -> "tooltip/" + path + "_background"), outerX, outerY, outerW, outerH);
            graphics.blitSprite(style.withPath(path -> "tooltip/" + path + "_frame"), outerX, outerY, outerW, outerH);
        }

        int lineY = top;
        for (int index = 0; index < lines.size(); index++) {
            ClientTooltipComponent line = lines.get(index);
            line.renderText(font, left, lineY, graphics.pose().last().pose(), graphics.bufferSource());
            lineY += line.getHeight() + (index == 0 ? 2 : 0);
        }
        graphics.flush();
        lineY = top;
        for (int index = 0; index < lines.size(); index++) {
            ClientTooltipComponent line = lines.get(index);
            line.renderImage(font, left, lineY, graphics);
            lineY += line.getHeight() + (index == 0 ? 2 : 0);
        }
        graphics.pose().popPose();
        graphics.flush();
    }

    public static void tooltip(GuiGraphics graphics, Font font, Component line, int x, int y) {
        graphics.renderTooltip(font, line, x, y);
    }
}
