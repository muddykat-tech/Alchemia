package muddykat.alchemia.client.gui;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.helper.UpgradeSide;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

public final class AlchemiaSprites {

    public record Sprite(ResourceLocation texture, int width, int height, int border) {
        public void blit(GuiGraphics graphics, int x, int y, int drawWidth, int drawHeight) {
            if (border > 0) {
                graphics.blitNineSliced(texture, x, y, drawWidth, drawHeight, border, width, height, 0, 0);
            } else {
                graphics.blit(texture, x, y, 0, 0, drawWidth, drawHeight, width, height);
            }
        }

        public void blit(GuiGraphics graphics, int x, int y) {
            blit(graphics, x, y, width, height);
        }

        public void blit(GuiGraphics graphics, int texWidth, int texHeight, int u, int v,
                         int x, int y, int drawWidth, int drawHeight) {
            graphics.blit(texture, x, y, u, v, drawWidth, drawHeight, texWidth, texHeight);
        }

        public void blit(GuiGraphics graphics, int x, int y, int drawWidth, int drawHeight, int argb) {
            float alpha = (argb >>> 24) / 255.0F;
            float red = ((argb >> 16) & 255) / 255.0F;
            float green = ((argb >> 8) & 255) / 255.0F;
            float blue = (argb & 255) / 255.0F;

            graphics.setColor(red, green, blue, alpha);
            blit(graphics, x, y, drawWidth, drawHeight);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    public static final Sprite PANEL = sprite("container/panel", 16, 16, 4);
    public static final Sprite SLOT = sprite("container/slot", 18, 18, 0);
    public static final Sprite PARCHMENT_SLOT = sprite("container/parchment_slot", 18, 18, 0);
    public static final Sprite BUTTON = sprite("container/button", 16, 16, 3);
    public static final Sprite BUTTON_HIGHLIGHTED = sprite("container/button_highlighted", 16, 16, 3);
    public static final Sprite BUTTON_DISABLED = sprite("container/button_disabled", 16, 16, 3);
    public static final Sprite NAME_FIELD = sprite("container/name_field", 16, 16, 2);
    public static final Sprite POTION_SWATCH = sprite("container/potion_swatch", 16, 16, 1);
    public static final Sprite ALCHEMY_MACHINE = sprite("container/alchemy_machine", 176, 202, 0);
    public static final Sprite UPGRADE_PANEL_RIGHT = sprite("container/upgrade_panel_right", 32, 104, 0);
    public static final Sprite UPGRADE_PANEL_LEFT = sprite("container/upgrade_panel_left", 32, 86, 0);
    public static final Sprite UPGRADE_PANEL_TOP = sprite("container/upgrade_panel_top", 68, 32, 0);
    public static final Sprite BURN_TRACK = sprite("container/burn_track", 14, 14, 0);
    public static final Sprite BURN_PROGRESS = sprite("container/burn_progress", 14, 14, 0);
    public static final Sprite COOK_TRACK = sprite("container/cook_track", 38, 8, 0);
    public static final Sprite COOK_PROGRESS = sprite("container/cook_progress", 38, 8, 0);
    public static final Sprite GRIND_PANEL = sprite("hud/grind_panel", 16, 16, 4);
    public static final Sprite PATH_TOOLTIP_PANEL = sprite("tooltip/path_panel", 16, 16, 4);
    public static final Sprite DELETE_BUTTON = sprite("guide/delete_button", 16, 16, 3);
    public static final Sprite DELETE_BUTTON_HIGHLIGHTED = sprite("guide/delete_button_highlighted", 16, 16, 3);
    public static final Sprite ROW_HIGHLIGHT = sprite("guide/row_highlight", 16, 16, 4);

    public static final int SLOT_SIZE = 18;
    public static final int BURN_W = 14;
    public static final int BURN_H = 14;
    public static final int COOK_W = 38;
    public static final int COOK_H = 8;

    private AlchemiaSprites() {
    }

    public static Sprite upgradePanel(UpgradeSide side) {
        return switch (side) {
            case RIGHT -> UPGRADE_PANEL_RIGHT;
            case LEFT -> UPGRADE_PANEL_LEFT;
            case TOP -> UPGRADE_PANEL_TOP;
        };
    }

    public static void blitMobEffect(GuiGraphics graphics, MobEffect effect, int x, int y, int width, int height) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getMobEffectTextures().get(effect);
        graphics.blit(x, y, 0, width, height, sprite);
    }

    public static void blitMobEffect(GuiGraphics graphics, MobEffect effect, int x, int y, int width, int height, int argb) {
        float alpha = (argb >>> 24) / 255.0F;
        float red = ((argb >> 16) & 255) / 255.0F;
        float green = ((argb >> 8) & 255) / 255.0F;
        float blue = (argb & 255) / 255.0F;

        graphics.setColor(red, green, blue, alpha);
        blitMobEffect(graphics, effect, x, y, width, height);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static Sprite sprite(String path, int width, int height, int border) {
        return new Sprite(new ResourceLocation(Alchemia.MODID, "textures/gui/sprites/" + path + ".png"),
                width, height, border);
    }
}
