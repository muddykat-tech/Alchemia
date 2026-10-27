package muddykat.alchemia.client.gui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import muddykat.alchemia.Alchemia;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.WeakHashMap;

public final class GuideInk {

    public enum Ink {
        NONE,
        INKED,
        DETAILED
    }

    public static final Ink PIPELINE = Ink.INKED;
    public static final Ink DETAILED_PIPELINE = Ink.DETAILED;
    public static final Ink NONE = Ink.NONE;

    public static final int PLAIN = 0xFFFFFFFF;
    public static final int SHORT = 0x70FFFFFF;

    private static final ResourceLocation WIDGETS = ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, "textures/gui/guide/widgets.png");
    private static final ResourceLocation WITHER_SKIN = ResourceLocation.withDefaultNamespace("textures/entity/skeleton/wither_skeleton.png");
    private static final ResourceLocation WITHER_HEAD = ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, "dynamic/ink_wither_head");
    private static final int WATER_BOTTLE_U = 240;
    private static final int WATER_BOTTLE_V = 68;

    private static final Map<TextureAtlasSprite, ContrastRange> CONTRAST_RANGES = new WeakHashMap<>();
    private static final ContrastRange FULL_CONTRAST = new ContrastRange(0.0F, 1.0F);
    private static @Nullable ContrastRange witherHead;

    private static @Nullable ShaderInstance inked;
    private static @Nullable ShaderInstance detailed;

    private record ContrastRange(float min, float max) {
    }

    private GuideInk() {
    }

    public static void register(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, "guide_ink_legacy"),
                DefaultVertexFormat.POSITION_TEX_COLOR), loaded -> inked = loaded);
        event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, "guide_ink_detailed_legacy"),
                DefaultVertexFormat.POSITION_TEX_COLOR), loaded -> detailed = loaded);
    }

    public static void item(GuiGraphics graphics, ItemStack stack, int x, int y, int colour) {
        item(graphics, stack, x, y, colour, PIPELINE);
    }

    public static void detailedItem(GuiGraphics graphics, ItemStack stack, int x, int y, int colour) {
        item(graphics, stack, x, y, colour, DETAILED_PIPELINE);
    }

    public static void item(GuiGraphics graphics, ItemStack stack, int x, int y, int colour, Ink ink) {
        if (!sprite(graphics, stack, x, y, colour, ink)) graphics.renderItem(stack, x, y);
    }

    public static void waterBottle(GuiGraphics graphics, int x, int y, int colour) {
        GuiDraw.blit(graphics, WIDGETS, x, y, WATER_BOTTLE_U, WATER_BOTTLE_V, 16, 16, 256, 256, colour);
    }

    public static void blit(GuiGraphics graphics, Ink ink, ResourceLocation texture, int x, int y, float u, float v, int width, int height,
                            int textureWidth, int textureHeight) {
        ShaderInstance shader = shader(ink);
        if (shader == null) {
            GuiDraw.blit(graphics, texture, x, y, u, v, width, height, textureWidth, textureHeight);
            return;
        }
        quad(graphics, shader, texture, x, y, width, height, u / textureWidth, (u + width) / textureWidth,
                v / textureHeight, (v + height) / textureHeight, PLAIN);
    }

    private static @Nullable ShaderInstance shader(Ink ink) {
        return switch (ink) {
            case INKED -> inked;
            case DETAILED -> detailed;
            case NONE -> null;
        };
    }

    private static boolean sprite(GuiGraphics graphics, ItemStack stack, int x, int y, int colour, Ink ink) {
        ShaderInstance shader = shader(ink);
        if (shader == null) return false;
        if (stack.is(Items.WITHER_SKELETON_SKULL)) return head(graphics, shader, x, y, colour, ink);
        Minecraft minecraft = Minecraft.getInstance();
        BakedModel model = minecraft.getItemRenderer().getModel(stack, minecraft.level, null, 0);
        TextureAtlasSprite sprite = model.getParticleIcon();
        if (sprite == null || sprite.contents().name().getPath().equals("missingno")) return false;

        int vertexColour = ink == Ink.DETAILED ? encodeContrastRange(colour, contrastRange(sprite)) : colour;
        quad(graphics, shader, InventoryMenu.BLOCK_ATLAS, x, y, 16, 16, sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1(), vertexColour);
        return true;
    }

    private static boolean head(GuiGraphics graphics, ShaderInstance shader, int x, int y, int colour, Ink ink) {
        ContrastRange range = witherHead();
        if (range == null) return false;
        int vertexColour = ink == Ink.DETAILED ? encodeContrastRange(colour, range) : colour;
        quad(graphics, shader, WITHER_HEAD, x, y, 16, 16, 0.0F, 1.0F, 0.0F, 1.0F, vertexColour);
        return true;
    }

    private static @Nullable ContrastRange witherHead() {
        if (witherHead != null) return witherHead;
        var resource = Minecraft.getInstance().getResourceManager().getResource(WITHER_SKIN);
        if (resource.isEmpty()) return null;
        int[] icon;
        try (InputStream input = resource.get().open(); NativeImage skin = NativeImage.read(input)) {
            icon = HeadIcon.render(pixels(skin), skin.getWidth(), skin.getHeight());
        } catch (IOException exception) {
            return null;
        }

        NativeImage image = new NativeImage(HeadIcon.SIZE, HeadIcon.SIZE, true);
        for (int y = 0; y < HeadIcon.SIZE; y++) {
            for (int x = 0; x < HeadIcon.SIZE; x++) image.setPixelRGBA(x, y, swapRedBlue(icon[y * HeadIcon.SIZE + x]));
        }
        Minecraft.getInstance().getTextureManager().register(WITHER_HEAD, new DynamicTexture(image));
        witherHead = contrastOf(icon);
        return witherHead;
    }

    private static int[] pixels(NativeImage image) {
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) pixels[y * image.getWidth() + x] = swapRedBlue(image.getPixelRGBA(x, y));
        }
        return pixels;
    }

    private static int swapRedBlue(int pixel) {
        return pixel & 0xFF00FF00 | (pixel >> 16 & 0xFF) | (pixel & 0xFF) << 16;
    }

    private static void quad(GuiGraphics graphics, ShaderInstance shader, ResourceLocation texture, int x, int y, int width, int height,
                             float u0, float u1, float v0, float v1, int colour) {
        graphics.flush();
        RenderSystem.setShader(() -> shader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Matrix4f pose = graphics.pose().last().pose();
        float r = (colour >> 16 & 255) / 255.0F;
        float g = (colour >> 8 & 255) / 255.0F;
        float b = (colour & 255) / 255.0F;
        float a = (colour >>> 24) / 255.0F;
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.addVertex(pose, x, y, 0).setUv(u0, v0).setColor(r, g, b, a);
        buffer.addVertex(pose, x, y + height, 0).setUv(u0, v1).setColor(r, g, b, a);
        buffer.addVertex(pose, x + width, y + height, 0).setUv(u1, v1).setColor(r, g, b, a);
        buffer.addVertex(pose, x + width, y, 0).setUv(u1, v0).setColor(r, g, b, a);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
    }

    private static int encodeContrastRange(int colour, ContrastRange range) {
        int min = Math.round(range.min() * 255.0F);
        int max = Math.round(range.max() * 255.0F);
        return colour & 0xFF000000 | min << 16 | max << 8;
    }

    private static ContrastRange contrastRange(TextureAtlasSprite sprite) {
        return CONTRAST_RANGES.computeIfAbsent(sprite, GuideInk::readContrastRange);
    }

    private static ContrastRange readContrastRange(TextureAtlasSprite sprite) {
        ResourceLocation name = sprite.contents().name();
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(name.getNamespace(), "textures/" + name.getPath() + ".png");
        var resource = Minecraft.getInstance().getResourceManager().getResource(texture);
        if (resource.isEmpty()) return FULL_CONTRAST;

        try (InputStream input = resource.get().open(); NativeImage image = NativeImage.read(input)) {
            return contrastOf(pixels(image));
        } catch (IOException exception) {
            return FULL_CONTRAST;
        }
    }

    private static ContrastRange contrastOf(int[] pixels) {
        float min = 1.0F;
        float max = 0.0F;
        boolean found = false;
        for (int pixel : pixels) {
            if ((pixel >>> 24) < 26) continue;

            float red = (pixel >> 16 & 0xFF) / 255.0F;
            float green = (pixel >> 8 & 0xFF) / 255.0F;
            float blue = (pixel & 0xFF) / 255.0F;
            float luminance = red * 0.299F + green * 0.587F + blue * 0.114F;
            min = Math.min(min, luminance);
            max = Math.max(max, luminance);
            found = true;
        }
        return found && max - min > 0.001F ? new ContrastRange(min, max) : FULL_CONTRAST;
    }
}
