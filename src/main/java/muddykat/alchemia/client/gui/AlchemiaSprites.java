package muddykat.alchemia.client.gui;

import muddykat.alchemia.Alchemia;
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
    public static final Identifier BURN_TRACK = sprite("container/burn_track");
    public static final Identifier BURN_PROGRESS = sprite("container/burn_progress");
    public static final Identifier COOK_TRACK = sprite("container/cook_track");
    public static final Identifier COOK_PROGRESS = sprite("container/cook_progress");
    public static final Identifier GRIND_PANEL = sprite("hud/grind_panel");
    public static final Identifier PATH_TOOLTIP_PANEL = sprite("tooltip/path_panel");
    public static final Identifier DELETE_BUTTON = sprite("guide/delete_button");
    public static final Identifier DELETE_BUTTON_HIGHLIGHTED = sprite("guide/delete_button_highlighted");
    public static final Identifier ROW_HIGHLIGHT = sprite("guide/row_highlight");

    public static final int SLOT_SIZE = 18;
    public static final int BURN_W = 14;
    public static final int BURN_H = 14;
    public static final int COOK_W = 38;
    public static final int COOK_H = 8;

    private AlchemiaSprites() {
    }

    private static Identifier sprite(String path) {
        return Identifier.fromNamespaceAndPath(Alchemia.MODID, path);
    }
}
