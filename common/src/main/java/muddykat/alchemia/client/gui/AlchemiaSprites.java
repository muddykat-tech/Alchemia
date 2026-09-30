package muddykat.alchemia.client.gui;

import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.helper.UpgradeSide;
import net.minecraft.resources.Identifier;

public final class AlchemiaSprites {

    public static final Identifier PANEL = sprite("container/panel");
    public static final Identifier SLOT = sprite("container/slot");
    public static final Identifier PARCHMENT_SLOT = sprite("container/parchment_slot");
    public static final Identifier BUTTON = sprite("container/button");
    public static final Identifier BUTTON_HIGHLIGHTED = sprite("container/button_highlighted");
    public static final Identifier BUTTON_DISABLED = sprite("container/button_disabled");
    public static final Identifier NAME_FIELD = sprite("container/name_field");
    public static final Identifier POTION_SWATCH = sprite("container/potion_swatch");
    public static final Identifier ALCHEMY_MACHINE = sprite("container/alchemy_machine");
    public static final Identifier UPGRADE_PANEL_RIGHT = sprite("container/upgrade_panel_right");
    public static final Identifier UPGRADE_PANEL_LEFT = sprite("container/upgrade_panel_left");
    public static final Identifier UPGRADE_PANEL_TOP = sprite("container/upgrade_panel_top");
    public static final Identifier BURN_TRACK = sprite("container/burn_track");
    public static final Identifier BURN_PROGRESS = sprite("container/burn_progress");
    public static final Identifier COOK_TRACK = sprite("container/cook_track");
    public static final Identifier COOK_PROGRESS = sprite("container/cook_progress");
    public static final Identifier TOOL_PANEL = sprite("container/tool_panel");
    public static final Identifier GRIND_TRACK = sprite("container/grind_track");
    public static final Identifier GRIND_PROGRESS = sprite("container/grind_progress");
    public static final Identifier GRIND_PANEL = sprite("hud/grind_panel");
    public static final Identifier PATH_TOOLTIP_PANEL = sprite("tooltip/path_panel");
    public static final Identifier PATH_ORIGIN = sprite("tooltip/path_origin");
    public static final Identifier PATH_ENDING = sprite("tooltip/path_ending");
    public static final Identifier PATH_DASH = sprite("path/dash");
    public static final Identifier PATH_DASH_HOLLOW = sprite("path/dash_hollow");
    public static final Identifier PATH_DOT = sprite("path/dot");
    public static final Identifier PATH_DOT_HOLLOW = sprite("path/dot_hollow");
    public static final Identifier DELETE_BUTTON = sprite("guide/delete_button");
    public static final Identifier DELETE_BUTTON_HIGHLIGHTED = sprite("guide/delete_button_highlighted");
    public static final Identifier ROW_HIGHLIGHT = sprite("guide/row_highlight");

    public static final int SLOT_SIZE = 18;
    public static final int BURN_W = 14;
    public static final int BURN_H = 14;
    public static final int COOK_W = 38;
    public static final int COOK_H = 8;
    public static final int GRIND_W = 38;
    public static final int GRIND_H = 8;

    private AlchemiaSprites() {
    }

    public static Identifier upgradePanel(UpgradeSide side) {
        return switch (side) {
            case RIGHT -> UPGRADE_PANEL_RIGHT;
            case LEFT -> UPGRADE_PANEL_LEFT;
            case TOP -> UPGRADE_PANEL_TOP;
        };
    }

    private static Identifier sprite(String path) {
        return Identifier.fromNamespaceAndPath(Alchemia.MODID, path);
    }
}
