package muddykat.alchemia.client.gui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.client.effect.SceneCapture;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public final class RepairHint {

    private static final long FADE_IN_MS = 350L;
    private static final long HOLD_MS = 1700L;
    private static final long FADE_OUT_MS = 550L;
    private static final double LIFT = 1.35;
    private static final int PANEL = 24;
    private static final int RISE = 4;
    private static final int BORDER = 0x3B2A1E;
    private static final int PAPER = 0xF0E5C9;
    private static final int SLOT = 0xD5C29C;
    private static final int SLOT_SHADE = 0xB7A27B;

    private static @Nullable BlockPos pos;
    private static ItemStack item = ItemStack.EMPTY;
    private static long start;
    private static final Map<Item, ResourceLocation> CUBES = new HashMap<>();

    private RepairHint() {
    }

    public static void show(BlockPos at, ItemStack stack) {
        pos = at.immutable();
        item = stack;
        start = Util.getMillis();
    }

    public static void render(GuiGraphics graphics, DeltaTracker delta) {
        if (pos == null || item.isEmpty()) return;
        long age = Util.getMillis() - start;
        if (age > FADE_IN_MS + HOLD_MS + FADE_OUT_MS) {
            pos = null;
            item = ItemStack.EMPTY;
            return;
        }

        float alpha = age < FADE_IN_MS ? ease(age / (float) FADE_IN_MS)
                : age < FADE_IN_MS + HOLD_MS ? 1.0F
                : 1.0F - ease((age - FADE_IN_MS - HOLD_MS) / (float) FADE_OUT_MS);
        SceneCapture.View view = SceneCapture.camera();
        if (view == null || alpha <= 0.01F) return;
        float[] at = view.project(pos.getX() + 0.5, pos.getY() + LIFT, pos.getZ() + 0.5);
        if (at == null) return;

        int x = Math.round(at[0] * graphics.guiWidth()) - PANEL / 2;
        int y = Math.round((1.0F - at[1]) * graphics.guiHeight()) - PANEL / 2 + Math.round((1.0F - alpha) * RISE);
        panel(graphics, x, y, alpha);

        Minecraft minecraft = Minecraft.getInstance();
        BakedModel model = minecraft.getItemRenderer().getModel(item, minecraft.level, null, 0);
        TextureAtlasSprite sprite = model.getParticleIcon();
        if (sprite != null && !sprite.contents().name().getPath().equals("missingno")) {
            ResourceLocation cube = item.getItem() instanceof BlockItem ? cube(item.getItem(), sprite) : null;
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
            if (cube != null) {
                graphics.blit(cube, x + 4, y + 4, 16, 16, 0.0F, 0.0F, CubeIcon.SIZE, CubeIcon.SIZE, CubeIcon.SIZE, CubeIcon.SIZE);
            } else {
                graphics.blit(x + 4, y + 4, 0, 16, 16, sprite);
            }
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        } else if (alpha > 0.5F) {
            graphics.renderItem(item, x + 4, y + 4);
        }
    }

    private static void panel(GuiGraphics graphics, int x, int y, float alpha) {
        graphics.fill(x + 1, y + PANEL, x + PANEL + 1, y + PANEL + 1, color(alpha * 0.35F, 0));
        graphics.fill(x + PANEL, y + 1, x + PANEL + 1, y + PANEL, color(alpha * 0.35F, 0));
        graphics.fill(x, y, x + PANEL, y + PANEL, color(alpha, BORDER));
        graphics.fill(x + 1, y + 1, x + PANEL - 1, y + PANEL - 1, color(alpha, PAPER));
        graphics.fill(x + 3, y + 3, x + PANEL - 3, y + PANEL - 3, color(alpha, SLOT_SHADE));
        graphics.fill(x + 4, y + 4, x + PANEL - 3, y + PANEL - 3, color(alpha, SLOT));
        int tip = x + PANEL / 2;
        graphics.fill(tip - 2, y + PANEL, tip + 2, y + PANEL + 1, color(alpha, BORDER));
        graphics.fill(tip - 1, y + PANEL + 1, tip + 1, y + PANEL + 2, color(alpha, BORDER));
    }

    private static int color(float alpha, int rgb) {
        return Math.round(Mth.clamp(alpha, 0.0F, 1.0F) * 255.0F) << 24 | rgb & 0xFFFFFF;
    }

    private static @Nullable ResourceLocation cube(Item item, TextureAtlasSprite sprite) {
        if (CUBES.containsKey(item)) return CUBES.get(item);
        ResourceLocation baked = bake(sprite);
        CUBES.put(item, baked);
        return baked;
    }

    private static @Nullable ResourceLocation bake(TextureAtlasSprite sprite) {
        ResourceLocation name = sprite.contents().name();
        var resource = Minecraft.getInstance().getResourceManager().getResource(ResourceLocation.fromNamespaceAndPath(name.getNamespace(), "textures/" + name.getPath() + ".png"));
        if (resource.isEmpty()) return null;

        int[] icon;
        try (InputStream input = resource.get().open(); NativeImage image = NativeImage.read(input)) {
            int[] pixels = new int[image.getWidth() * image.getHeight()];
            for (int py = 0; py < image.getHeight(); py++) {
                for (int px = 0; px < image.getWidth(); px++) pixels[py * image.getWidth() + px] = swapRedBlue(image.getPixelRGBA(px, py));
            }
            icon = CubeIcon.render(pixels, image.getWidth(), image.getHeight());
        } catch (IOException exception) {
            return null;
        }

        NativeImage baked = new NativeImage(CubeIcon.SIZE, CubeIcon.SIZE, true);
        for (int py = 0; py < CubeIcon.SIZE; py++) {
            for (int px = 0; px < CubeIcon.SIZE; px++) baked.setPixelRGBA(px, py, swapRedBlue(icon[py * CubeIcon.SIZE + px]));
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Alchemia.MODID, "dynamic/repair_cube_" + CUBES.size());
        Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(baked));
        return id;
    }

    private static int swapRedBlue(int pixel) {
        return pixel & 0xFF00FF00 | (pixel >> 16 & 0xFF) | (pixel & 0xFF) << 16;
    }

    private static float ease(float t) {
        float clamped = Mth.clamp(t, 0.0F, 1.0F);
        return clamped * clamped * (3.0F - 2.0F * clamped);
    }
}
