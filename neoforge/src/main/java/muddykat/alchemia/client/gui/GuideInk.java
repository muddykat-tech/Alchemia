package muddykat.alchemia.client.gui;

#if MC_VER >= 26300
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
#else
import com.mojang.blaze3d.pipeline.RenderPipeline;
#endif
import com.mojang.blaze3d.platform.NativeImage;
import muddykat.alchemia.Alchemia;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.WeakHashMap;

public final class GuideInk {

    public static final RenderPipeline PIPELINE = pipeline("guide_ink");
    public static final RenderPipeline DETAILED_PIPELINE = pipeline("guide_ink_detailed");

    private static RenderPipeline pipeline(String name) {
        var builder = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(Alchemia.MODID, "pipeline/" + name))
            .withFragmentShader(Identifier.fromNamespaceAndPath(Alchemia.MODID, "core/" + name));
#if MC_VER >= 26300
        builder = builder.withShaderDefine("ALCHEMIA_LOCATIONS");
#endif
        return builder.build();
    }

    public static final int PLAIN = 0xFFFFFFFF;
    public static final int SHORT = 0x70FFFFFF;

    private static final Identifier WIDGETS = Identifier.fromNamespaceAndPath(Alchemia.MODID, "textures/gui/guide/widgets.png");
    private static final int WATER_BOTTLE_U = 240;
    private static final int WATER_BOTTLE_V = 68;
    private static final Identifier WITHER_SKIN = Identifier.withDefaultNamespace("textures/entity/skeleton/wither_skeleton.png");
    private static final Identifier WITHER_HEAD = Identifier.fromNamespaceAndPath(Alchemia.MODID, "dynamic/ink_wither_head");

    private static final ItemStackRenderState STATE = new ItemStackRenderState();
    private static final RandomSource RANDOM = RandomSource.create();
    private static final Map<TextureAtlasSprite, ContrastRange> CONTRAST_RANGES = new WeakHashMap<>();
    private static final ContrastRange FULL_CONTRAST = new ContrastRange(0.0F, 1.0F);
    private static @Nullable ContrastRange witherHead;

    private record ContrastRange(float min, float max) {
    }

    private GuideInk() {
    }

    public static void item(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, int colour) {
        item(graphics, stack, x, y, colour, PIPELINE);
    }

    public static void detailedItem(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, int colour) {
        item(graphics, stack, x, y, colour, DETAILED_PIPELINE);
    }

    public static void item(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, int colour, RenderPipeline pipeline) {
        if (!sprite(graphics, stack, x, y, colour, pipeline)) graphics.item(stack, x, y);
    }

    public static void waterBottle(GuiGraphicsExtractor graphics, int x, int y, int colour) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, WIDGETS, x, y, WATER_BOTTLE_U, WATER_BOTTLE_V, 16, 16, 256, 256, colour);
    }

    private static boolean sprite(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, int colour, RenderPipeline pipeline) {
        if (stack.is(Items.WITHER_SKELETON_SKULL)) return head(graphics, x, y, colour, pipeline);
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getItemModelResolver().updateForTopItem(STATE, stack, ItemDisplayContext.GUI, minecraft.level, null, 0);
        RANDOM.setSeed(0L);
        Material.Baked material = STATE.pickParticleMaterial(RANDOM);
        if (material == null) return false;
        TextureAtlasSprite sprite = material.sprite();

        int vertexColour = pipeline == DETAILED_PIPELINE ? encodeContrastRange(colour, contrastRange(sprite)) : colour;
        graphics.blitSprite(pipeline, sprite, x, y, 16, 16, vertexColour);
        return true;
    }

    private static boolean head(GuiGraphicsExtractor graphics, int x, int y, int colour, RenderPipeline pipeline) {
        ContrastRange range = witherHead();
        if (range == null) return false;
        int vertexColour = pipeline == DETAILED_PIPELINE ? encodeContrastRange(colour, range) : colour;
        float scale = 16.0F / HeadIcon.SIZE;
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(scale, scale);
        graphics.blit(pipeline, WITHER_HEAD, 0, 0, 0.0F, 0.0F, HeadIcon.SIZE, HeadIcon.SIZE, HeadIcon.SIZE, HeadIcon.SIZE, vertexColour);
        graphics.pose().popMatrix();
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

        DynamicTexture texture = new DynamicTexture(WITHER_HEAD::toString, HeadIcon.SIZE, HeadIcon.SIZE, true);
        NativeImage image = texture.getPixels();
        if (image == null) return null;
        for (int y = 0; y < HeadIcon.SIZE; y++) {
            for (int x = 0; x < HeadIcon.SIZE; x++) image.setPixel(x, y, icon[y * HeadIcon.SIZE + x]);
        }
        texture.upload();
        Minecraft.getInstance().getTextureManager().register(WITHER_HEAD, texture);
        witherHead = contrastOf(icon);
        return witherHead;
    }

    private static int[] pixels(NativeImage image) {
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) pixels[y * image.getWidth() + x] = image.getPixel(x, y);
        }
        return pixels;
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
        Identifier name = sprite.contents().name();
        Identifier texture = Identifier.fromNamespaceAndPath(name.getNamespace(), "textures/" + name.getPath() + ".png");
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
