package muddykat.alchemia.client.gui;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.helper.UpgradeSide;
import net.minecraft.resources.ResourceLocation;

public final class AlchemiaSprites {

    public static final ResourceLocation PANEL = sprite("container/panel");
    public static final ResourceLocation SLOT = sprite("container/slot");
    public static final ResourceLocation PARCHMENT_SLOT = sprite("container/parchment_slot");
    public static final ResourceLocation BUTTON = sprite("container/button");
    public static final ResourceLocation BUTTON_HIGHLIGHTED = sprite("container/button_highlighted");
    public static final ResourceLocation BUTTON_DISABLED = sprite("container/button_disabled");
    public static final ResourceLocation NAME_FIELD = sprite("container/name_field");
    public static final ResourceLocation POTION_SWATCH = sprite("container/potion_swatch");
    public static final ResourceLocation ALCHEMY_MACHINE = sprite("container/alchemy_machine");
    public static final ResourceLocation UPGRADE_PANEL_RIGHT = sprite("container/upgrade_panel_right");
    public static final ResourceLocation UPGRADE_PANEL_LEFT = sprite("container/upgrade_panel_left");
    public static final ResourceLocation UPGRADE_PANEL_TOP = sprite("container/upgrade_panel_top");
    public static final ResourceLocation BURN_TRACK = sprite("container/burn_track");
    public static final ResourceLocation BURN_PROGRESS = sprite("container/burn_progress");
    public static final ResourceLocation COOK_TRACK = sprite("container/cook_track");
    public static final ResourceLocation COOK_PROGRESS = sprite("container/cook_progress");
    public static final ResourceLocation GRIND_PANEL = sprite("hud/grind_panel");
    public static final ResourceLocation PATH_TOOLTIP_PANEL = sprite("tooltip/path_panel");
    public static final ResourceLocation DELETE_BUTTON = sprite("guide/delete_button");
    public static final ResourceLocation DELETE_BUTTON_HIGHLIGHTED = sprite("guide/delete_button_highlighted");
    public static final ResourceLocation ROW_HIGHLIGHT = sprite("guide/row_highlight");

    public static final int SLOT_SIZE = 18;
    public static final int BURN_W = 14;
    public static final int BURN_H = 14;
    public static final int COOK_W = 38;
    public static final int COOK_H = 8;

    private AlchemiaSprites() {
    }

    public static ResourceLocation upgradePanel(UpgradeSide side) {
        return switch (side) {
            case RIGHT -> UPGRADE_PANEL_RIGHT;
            case LEFT -> UPGRADE_PANEL_LEFT;
            case TOP -> UPGRADE_PANEL_TOP;
        };
    }

    private static ResourceLocation sprite(String path) {
        return new ResourceLocation(Alchemia.MODID, path);
    }
}
