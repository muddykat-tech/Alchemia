package muddykat.alchemia.client.gui;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import org.joml.Matrix4f;
import org.joml.Vector2ic;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GuiDraw {

    private static final String EFFECT_PREFIX = "mob_effect/";
    private static final Map<ResourceLocation, AlchemiaSprites.Sprite> GUI_SPRITES = new HashMap<>();
    private static final MetadataSectionSerializer<JsonObject> GUI_META = new MetadataSectionSerializer<>() {
        @Override
        public String getMetadataSectionName() {
            return "gui";
        }

        @Override
        public JsonObject fromJson(JsonObject json) {
            return json;
        }
    };
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
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(pose, x0, y0, 0).uv(u0, v0).endVertex();
        buffer.vertex(pose, x0, y1, 0).uv(u0, v1).endVertex();
        buffer.vertex(pose, x1, y1, 0).uv(u1, v1).endVertex();
        buffer.vertex(pose, x1, y0, 0).uv(u1, v0).endVertex();
        BufferUploader.drawWithShader(buffer.end());
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
            ResourceLocation effect = new ResourceLocation(sprite.getNamespace(), sprite.getPath().substring(EFFECT_PREFIX.length()));
            MobEffect found = BuiltInRegistries.MOB_EFFECT.get(effect);
            if (found != null && BuiltInRegistries.MOB_EFFECT.containsKey(effect)) {
                effect(graphics, found, x, y, width, height, argb);
                return;
            }
        }
        atlasSprite(sprite).blit(graphics, x, y, width, height, argb);
    }

    private static AlchemiaSprites.Sprite atlasSprite(ResourceLocation id) {
        return GUI_SPRITES.computeIfAbsent(id, GuiDraw::loadSprite);
    }

    private static AlchemiaSprites.Sprite loadSprite(ResourceLocation id) {
        ResourceLocation texture = new ResourceLocation(id.getNamespace(), "textures/gui/sprites/" + id.getPath() + ".png");
        int width = 16;
        int height = 16;
        int border = 0;
        var resource = Minecraft.getInstance().getResourceManager().getResource(texture);
        if (resource.isPresent()) {
            try (InputStream input = resource.get().open(); NativeImage image = NativeImage.read(input)) {
                width = image.getWidth();
                height = image.getHeight();
            } catch (IOException ignored) {
            }
            try {
                JsonObject meta = resource.get().metadata().getSection(GUI_META).orElse(null);
                JsonObject scaling = meta != null && meta.has("scaling") ? meta.getAsJsonObject("scaling") : null;
                if (scaling != null && "nine_slice".equals(GsonHelper.getAsString(scaling, "type", ""))) {
                    width = GsonHelper.getAsInt(scaling, "width", width);
                    height = GsonHelper.getAsInt(scaling, "height", height);
                    border = GsonHelper.getAsInt(scaling, "border", 0);
                }
            } catch (IOException ignored) {
            }
        }
        return new AlchemiaSprites.Sprite(texture, width, height, border);
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

    public static void effect(GuiGraphics graphics, MobEffect effect, int x, int y, int width, int height) {
        effect(graphics, effect, x, y, width, height, -1);
    }

    public static void effect(GuiGraphics graphics, MobEffect effect, int x, int y, int width, int height, int argb) {
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
            atlasSprite(new ResourceLocation(style.getNamespace(), "tooltip/" + style.getPath() + "_background")).blit(graphics, outerX, outerY, outerW, outerH);
            atlasSprite(new ResourceLocation(style.getNamespace(), "tooltip/" + style.getPath() + "_frame")).blit(graphics, outerX, outerY, outerW, outerH);
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
