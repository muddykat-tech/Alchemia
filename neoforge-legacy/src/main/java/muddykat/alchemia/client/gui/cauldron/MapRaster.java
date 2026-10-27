package muddykat.alchemia.client.gui.cauldron;

import muddykat.alchemia.client.gui.GuiDraw;

import com.mojang.blaze3d.platform.NativeImage;
import muddykat.alchemia.Alchemia;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

final class MapRaster {

    private static int serial;

    private final String name;
    private int[] pixels = new int[0];
    private int width;
    private int height;
    private boolean blank = true;
    private ResourceLocation id;
    private DynamicTexture texture;

    MapRaster(String name) {
        this.name = name;
    }

    int[] begin(int width, int height) {
        if (width != this.width || height != this.height) {
            close();
            this.width = width;
            this.height = height;
            pixels = new int[width * height];
        } else {
            java.util.Arrays.fill(pixels, 0);
        }
        return pixels;
    }

    void finish() {
        blank = true;
        for (int pixel : pixels) {
            if (pixel != 0) {
                blank = false;
                break;
            }
        }
        if (blank) return;

        if (texture == null) {
            id = ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, "dynamic/map_" + name + "_" + serial++);
            texture = new DynamicTexture(width, height, true);
            Minecraft.getInstance().getTextureManager().register(id, texture);
        }
        NativeImage image = texture.getPixels();
        if (image == null) return;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) image.setPixelRGBA(x, y, abgr(pixels[y * width + x]));
        }
        texture.upload();
    }

    private static int abgr(int argb) {
        return argb & 0xFF00FF00 | (argb & 0xFF) << 16 | argb >> 16 & 0xFF;
    }

    void draw(GuiGraphics graphics, int left, int top) {
        if (blank || texture == null) return;
        GuiDraw.blit(graphics, id, left, top, 0.0F, 0.0F, width, height, width, height);
    }

    void close() {
        if (texture != null) Minecraft.getInstance().getTextureManager().release(id);
        texture = null;
        id = null;
        blank = true;
        width = 0;
        height = 0;
    }
}
