package muddykat.alchemia.client.gui;

import net.minecraft.world.item.Item;
import muddykat.alchemia.registration.registers.ItemRegistry;
import muddykat.alchemia.common.potion.Imbuements;
import muddykat.alchemia.common.potion.IngredientDiscovery;
import muddykat.alchemia.common.guide.GuideShelf;
import com.mojang.blaze3d.platform.InputConstants;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.blocks.tileentity.container.AlchemicalCauldronMenu;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.helper.IngredientPath;
import muddykat.alchemia.common.items.helper.IngredientTool;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.items.helper.PathGeometry;
import muddykat.alchemia.common.items.helper.PathPoint;
import muddykat.alchemia.common.potion.BrewCourse;
import muddykat.alchemia.common.potion.BrewModifiers;
import muddykat.alchemia.common.potion.LureReach;
import muddykat.alchemia.common.potion.WorldEffects;
import muddykat.alchemia.common.potion.AlchemiaEffect;
import muddykat.alchemia.common.potion.AlchemiaEffects;
import muddykat.alchemia.common.potion.PotionMap;
import muddykat.alchemia.common.potion.VanillaIngredients;
import muddykat.alchemia.common.potion.RecipeDiscovery;
import net.minecraft.util.Util;
import muddykat.alchemia.common.network.packets.PacketNameBrew;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.ChatFormatting;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.PotionContents;

import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AlchemicalScreen extends AbstractContainerScreen<AlchemicalCauldronMenu> {
    private static final Identifier BACKGROUND_TEXTURE = Identifier.fromNamespaceAndPath(Alchemia.MODID, "textures/book/images/book.png");
    private static final Identifier FRAME_TEXTURE = Identifier.fromNamespaceAndPath(Alchemia.MODID, "textures/book/images/book_frame.png");

    private static final int IMAGE_WIDTH = 310;
    private static final int BOOK_HEIGHT = 218;
    private static final int IMAGE_HEIGHT = 298;

    private static final int EFFECTS_GAP = 6;
    private static final int EFFECTS_W = 108;
    private static final int EFFECTS_Y = 16;

    private static final int VIEW_INSET_X = 22;
    private static final int VIEW_INSET_Y = 16;
    private static final int VIEW_WIDTH = 265;
    private static final int VIEW_HEIGHT = 160;

    private static final Identifier GUIDE_WIDGETS = Identifier.fromNamespaceAndPath(Alchemia.MODID, "textures/gui/guide/widgets.png");
    private static final int GUIDE_SHEET = 256;
    private static final int CLOSED_EYE_U = 208;
    private static final int CLOSED_EYE_V = 3;
    private static final int CLOSED_EYE_W = 12;
    private static final int CLOSED_EYE_H = 5;
    private static final int OPEN_EYE_U = 130;
    private static final int OPEN_EYE_V = 2;
    private static final int OPEN_EYE_W = 12;
    private static final int OPEN_EYE_H = 8;
    private static final Identifier SYMBOLS_TEXTURE = Identifier.fromNamespaceAndPath(Alchemia.MODID, "textures/book/images/symbols.png");
    private static final int SYMBOLS_W = 21;
    private static final int SYMBOLS_H = 45;
    private static final int UNKNOWN_U = 14;
    private static final int UNKNOWN_V = 16;
    private static final int UNKNOWN_W = 7;
    private static final int UNKNOWN_H = 11;
    private static final int UNKNOWN_CELL = 16;
    private static final int POTENCY_PER_ROW = 5;
    private static final int POTENCY_CELL = 18;
    private static final int INSTABILITY_BLOCK_H = 30;
    private static final int LINE_HEIGHT = 10;
    private static final int RUINED_TEXT_Y = 46;

    private static final int SLOT_ROW_Y = 181;
    private static final int INVENTORY_X = 74;
    private static final int INVENTORY_Y = 222;
    private static final int HOTBAR_Y = 280;

    private static final int STIR_BUTTON_X = 213;
    private static final int BREW_BUTTON_X = 250;
    private static final int ACTION_BUTTON_W = 35;
    private static final int BUTTON_Y = 181;
    private static final int BUTTON_H = 18;

    private static final int ADD_BUTTON_X = 79;
    private static final int ADD_BUTTON_W = 90;

    private static final int TOOL_PANEL_Y = 126;
    private static final int TOOL_PANEL_H = 64;
    private static final int TOOL_TRACK_X = 30;
    private static final int TOOL_TRACK_Y = 24;
    private static final int TOOL_BUTTON_X = 6;
    private static final int TOOL_BUTTON_Y = 40;
    private static final int TOOL_BUTTON_W = 96;

    private static final int QUEUE_PANEL_Y = 196;
    private static final int QUEUE_COLUMNS = 4;
    private static final int QUEUE_ROWS = 3;
    private static final int QUEUE_CELL = 18;
    private static final int QUEUE_GRID_Y = 14;
    private static final int QUEUE_PANEL_H = QUEUE_GRID_Y + QUEUE_ROWS * QUEUE_CELL + 6;

    private static final int GHOST_RGB = 0x2B2015;
    private static final int PENDING_RGB = 0x1E1710;
    private static final int BLOCKED_RGB = 0x6B2418;
    private static final int PATH_INK_RGB = 0x3D2616;
    private static final int EDGE_FILL = 0x662A4A8E;
    private static final int EDGE_BORDER = 0xCC1C315F;
    private static final int EDGE_BORDER_WIDTH = 2;
    private static final int PATH_ALPHA = 191;
    private static final int BREW_GLOW_RGB = 0xFFE9A8;
    private static final int AURA_UNKNOWN_RGB = 0x7A6A50;
    private static final int AURA_CORE_ALPHA = 96;
    private static final int AURA_DASH = 2;

    private static final double MAP_OVERSCAN = 4 * PotionMap.CELL;
    private static final int PATH_THICKNESS = 2;
    private static final double MIN_PATH_SCALE = 0.5;
    private static final double PATH_BASE_SCALE = 2.0;

    private static final double MIN_ZOOM = 0.1;
    private static final double MAX_ZOOM = 3.0;
    private static final double ZOOM_STEP = 1.18;
    private static final double LABEL_ZOOM = 1.2;

    private static final int INK = 0xFF4A3A28;
    private static final int INK_FAINT = 0xFF7A6A52;
    private static final int NAME_SELECTION = 0x804A3A28;
    private static final int LINE_RGB = 0x282828;
    private static final int UNKNOWN_TINT = 0xFF7A6A52;
    private static final int DORMANT_TINT = 0xFF8E8B86;
    private static final int DORMANT_RGB = 0x5F5C58;
    private static final int DORMANT_FILL_ALPHA = 56;
    private static final int HELD_FILL_ALPHA = 110;
    private static final int DORMANT_EDGE_ALPHA = 90;
    private static final int DORMANT_LIT_RGB = 0xF4E8C6;
    private static final int DORMANT_LIT_ALPHA = 230;
    private static final double DORMANT_GLOW_MS = 180.0;
    private static final long NOTICE_MS = 4200L;
    private static final long NOTICE_FADE_IN_MS = 250L;
    private static final long NOTICE_FADE_OUT_MS = 700L;
    private static final int NOTICE_TOP = 18;
    private static final int NOTICE_PAD = 8;
    private static final int NODE_BASE = 18;

    private static final int SCREEN_MARGIN = 8;

    private float uiScale = 1.0F;

    private double camX;
    private double camY;
    private boolean viewInitialised;

    private double zoom = 1.0;
    private double targetZoom = 1.0;
    private double anchorWorldX;
    private double anchorWorldY;
    private double anchorScreenX;
    private double anchorScreenY;

    private static final int NAME_X = 3;
    private static final int NAME_Y = 2;
    private static final int NAME_W = 132;
    private static final int NAME_H = 14;
    private static final int NAME_TEXT_Y = 3;

    private static final int MAX_PENDING_STIRS = 40;
    private static final double HOP_DISTANCE = PotionMap.CELL;
    private static final double MARKER_CHASE_MS = 45.0;
    private static final double FOLLOW_MARGIN = 0.3;
    private static final double CHARGE_SHRINK = 0.4;
    private static final long VANISH_MS = 220L;
    private static final long APPEAR_MS = 480L;
    private static final long FLIGHT_MIN_MS = 350L;
    private static final long FLIGHT_MAX_MS = 1400L;
    private static final double FLIGHT_PER_MS = 0.45;
    private static final double APPEAR_TILT = 0.75 * Math.PI;
    private static final double APPEAR_DAMPING = 5.5;
    private static final double APPEAR_SWINGS = 3.0;
    private static final double FLIGHT_TRAIL = 3.0 * PotionMap.CELL;
    private static final int FLIGHT_TRAIL_STEPS = 10;

    private static final long LURE_BEND_MS = 650L;
    private static final long LURE_FIELD_MS = 1800L;
    private static final int LURE_FIELD_RINGS = 3;
    private static final int LURE_FIELD_ALPHA = 170;
    private static final int LURE_QUEUED_ALPHA = 70;
    private static final int LURE_GHOST_ALPHA = 70;
    private static final double LURE_INK_MIX = 0.45;
    private static final double TETHER_SPACING = PotionMap.CELL;
    private static final double TETHER_MIN_PIXELS = 1.5;
    private static final int TETHER_ALPHA = 130;
    private static final int LURE_BRACKET_ALPHA = 255;
    private static final int LURE_PREVIEW_ALPHA = 200;
    private static final double LURE_BRACKET_MIX = 0.25;
    private static final int LURE_FOCUS_ALPHA = 60;
    private static final int LURE_CELL_ALPHA = 45;
    private static final int POINTER_INSET = 10;
    private static final double POINTER_SIZE = 5.0;

    private EditBox nameField;
    private int nameScroll;
    private boolean panning;
    private boolean applying;
    private int applyTicks;
    private boolean stirring;
    private final ArrayDeque<Integer> pendingStirs = new ArrayDeque<>();
    private int stirBase;
    private int stirsSent;
    private BrewCourse shownCourse = new BrewCourse(0.0, 0.0);
    private PathPoint brewAt = PathPoint.ORIGIN;
    private PathPoint markerAt;
    private PathPoint hopFrom = PathPoint.ORIGIN;
    private long hopStart = Long.MIN_VALUE;
    private double hopCharge;
    private double chargeShown;
    private List<PathPoint> flightPlan = List.of();
    private List<PathPoint> flight = List.of();
    private double flightLength;
    private long flightMs;
    private boolean flightCamHeld;
    private double flightCamX;
    private double flightCamY;
    private long markerFrame;
    private Set<AlchemiaEffect> known = Set.of();
    private Set<String> notes = Set.of();
    private Set<String> notesSeen;
    private final ArrayDeque<Component> notices = new ArrayDeque<>();
    private Set<AlchemiaEffect> knownSeen;
    private Component notice;
    private long noticeStart;
    private PotionMap.MapEntry glowEntry;
    private double glow;
    private Component hoverName;
    private Component hoverDetail;
    private int hoverX;
    private int hoverY;

    public AlchemicalScreen(AlchemicalCauldronMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, IMAGE_WIDTH + EFFECTS_GAP + EFFECTS_W, IMAGE_HEIGHT);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        resetView();
    }

    @Override
    protected void init() {
        super.init();

        this.uiScale = Math.min(1.0F, Math.min((float) this.width / (this.imageWidth + SCREEN_MARGIN),
                (float) this.height / (this.imageHeight + SCREEN_MARGIN)));
        this.leftPos = Math.round((this.width / this.uiScale - this.imageWidth) / 2.0F);
        this.topPos = Math.round((this.height / this.uiScale - this.imageHeight) / 2.0F);

        String existing = this.getMenu().getCauldron().getBrewName();
        this.nameField = new EditBox(this.font, viewLeft() + NAME_X + 2, viewTop() + NAME_Y + 2,
                NAME_W - 4, NAME_H - 4, Component.translatable("alchemia.gui.name")) {
            @Override
            public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            }
        };
        this.nameField.setBordered(false);
        this.nameField.setMaxLength(PacketNameBrew.MAX_LENGTH);
        this.nameField.setTextColor(INK);
        this.nameField.setValue(existing);
        this.nameField.setHint(Component.translatable("alchemia.gui.name.hint"));
        this.nameField.setResponder(value -> ClientPacketDistributor.sendToServer(new PacketNameBrew(value)));
        addRenderableWidget(this.nameField);

        this.stirBase = this.getMenu().getCauldron().getStirCount();
        this.stirsSent = 0;
        this.pendingStirs.clear();
        this.stirring = false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.nameField != null && this.nameField.isFocused()) {
            if (event.key() == InputConstants.KEY_RETURN || event.key() == InputConstants.KEY_NUMPADENTER) {
                releaseNameFocus();
                return true;
            }
            if (!event.isEscape() && (this.nameField.keyPressed(event) || this.nameField.canConsumeInput())) return true;
        }
        return super.keyPressed(event);
    }

    private void releaseNameFocus() {
        if (this.nameField == null) return;
        this.nameField.moveCursorToStart(false);
        this.nameField.setFocused(false);
        this.setFocused(null);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    private double toGui(double value) {
        return value / this.uiScale;
    }

    private int toScreen(double value) {
        return Math.round((float) value * this.uiScale);
    }

    private MouseButtonEvent toGui(MouseButtonEvent event) {
        return new MouseButtonEvent(toGui(event.x()), toGui(event.y()), event.buttonInfo());
    }

    private int viewLeft() {
        return this.leftPos + VIEW_INSET_X;
    }

    private int viewTop() {
        return this.topPos + VIEW_INSET_Y;
    }

    private double viewCentreX() {
        return viewLeft() + VIEW_WIDTH / 2.0;
    }

    private double viewCentreY() {
        return viewTop() + VIEW_HEIGHT / 2.0;
    }

    private void resetView() {
        PathPoint at = this.getMenu().getCauldron().getBrewPosition();
        camX = at.x();
        camY = at.y();
        zoom = 1.0;
        targetZoom = 1.0;
        viewInitialised = true;
    }

    private double screenX(double mapX) {
        return viewCentreX() + (mapX - camX) * zoom;
    }

    private double screenY(double mapY) {
        return viewCentreY() + (mapY - camY) * zoom;
    }

    private double mapAtX(double screen) {
        return camX + (screen - viewCentreX()) / zoom;
    }

    private double mapAtY(double screen) {
        return camY + (screen - viewCentreY()) / zoom;
    }

    private PathPoint toScreen(PathPoint point) {
        return new PathPoint(screenX(point.x()), screenY(point.y()));
    }

    private List<PathPoint> toScreen(List<PathPoint> points) {
        List<PathPoint> screen = new ArrayList<>(points.size());
        for (PathPoint point : points) screen.add(toScreen(point));
        return screen;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        double guiX = toGui(mouseX);
        double guiY = toGui(mouseY);
        if (scrollY == 0 || !overMap(guiX, guiY)) return super.mouseScrolled(guiX, guiY, scrollX, scrollY);

        anchorScreenX = guiX;
        anchorScreenY = guiY;
        anchorWorldX = mapAtX(guiX);
        anchorWorldY = mapAtY(guiY);

        double factor = scrollY > 0 ? ZOOM_STEP : 1.0 / ZOOM_STEP;
        targetZoom = Mth.clamp(targetZoom * factor, MIN_ZOOM, MAX_ZOOM);
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (panning && event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            camX -= toGui(dx) / zoom;
            camY -= toGui(dy) / zoom;
            targetZoom = zoom;
            clampCamera();
            return true;
        }
        if (this.nameField != null && this.nameField.isFocused() && event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            this.nameField.mouseDragged(toGui(event), toGui(dx), toGui(dy));
            this.nameField.moveCursorTo(nameIndexAt(toGui(event.x())), true);
            return true;
        }
        return super.mouseDragged(toGui(event), toGui(dx), toGui(dy));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double guiX = toGui(event.x());
        double guiY = toGui(event.y());

        if (!overNameField(guiX, guiY)) {
            releaseNameFocus();
        } else if (this.nameField != null && event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            this.setFocused(this.nameField);
            this.nameField.setFocused(true);
            this.nameField.mouseClicked(toGui(event), doubleClick);
            if (!doubleClick) this.nameField.moveCursorTo(nameIndexAt(guiX), event.hasShiftDown());
            return true;
        }

        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && overBrewButton(guiX, guiY)) {
            if (brewCheck() == TileEntityAlchemyCauldron.BrewCheck.READY) {
                sendButton(AlchemicalCauldronMenu.BUTTON_BREW);
            }
            return true;
        }

        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && overStirButton(guiX, guiY)) {
            if (canStir()) {
                stirring = true;
                if (!inFlight()) sendStir();
            }
            return true;
        }

        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && overAddButton(guiX, guiY)) {
            if (canAdd()) sendButton(AlchemicalCauldronMenu.BUTTON_ADD);
            return true;
        }

        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && overToolButton(guiX, guiY)) {
            if (canApplyTool()) {
                applying = true;
                applyTicks = 0;
                sendButton(AlchemicalCauldronMenu.BUTTON_APPLY_TOOL);
            }
            return true;
        }

        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            int cell = queueCellAt(guiX, guiY);
            if (cell >= 0) {
                sendButton(AlchemicalCauldronMenu.BUTTON_UNQUEUE + cell);
                return true;
            }
        }

        if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT && overMap(guiX, guiY)) {
            resetView();
            return true;
        }

        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && overMap(guiX, guiY)) {
            panning = true;
        }

        return super.mouseClicked(toGui(event), doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            panning = false;
            applying = false;
            stirring = false;
        }
        return super.mouseReleased(toGui(event));
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        if (stirring) {
            if (!canStir()) {
                stirring = false;
            } else if (!shownCourse.teleporting(map()) && !inFlight()) {
                sendStir();
            }
        }

        if (!applying) return;

        IngredientTool tool = heldTool();
        if (tool == null || !canApplyTool()) {
            applying = false;
            return;
        }

        applyTicks++;
        if (applyTicks % Math.max(1, tool.ticksPerStep()) == 0) {
            sendButton(AlchemicalCauldronMenu.BUTTON_APPLY_TOOL);
        }
    }

    private boolean overMap(double mouseX, double mouseY) {
        if (overNameField(mouseX, mouseY)) return false;
        return mouseX >= viewLeft() && mouseX < viewLeft() + VIEW_WIDTH
                && mouseY >= viewTop() && mouseY < viewTop() + VIEW_HEIGHT;
    }

    private boolean overNameField(double mouseX, double mouseY) {
        int x = viewLeft() + NAME_X;
        int y = viewTop() + NAME_Y;
        return mouseX >= x && mouseX < x + NAME_W && mouseY >= y && mouseY < y + NAME_H;
    }

    private void clampCamera() {
        if (!PotionMap.isReady()) return;
        double limit = map().getMaxAlignment() + MAP_OVERSCAN;
        camX = Mth.clamp(camX, -MAP_OVERSCAN, limit);
        camY = Mth.clamp(camY, -MAP_OVERSCAN, limit);
    }

    private void tickZoom() {
        if (Math.abs(targetZoom - zoom) < 0.0005) {
            zoom = targetZoom;
            return;
        }
        zoom = Mth.lerp(0.35, zoom, targetZoom);
        camX = anchorWorldX - (anchorScreenX - viewCentreX()) / zoom;
        camY = anchorWorldY - (anchorScreenY - viewCentreY()) / zoom;
        clampCamera();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);

        if (!viewInitialised) resetView();
        reconcileStir();
        trackMarker();
        tickZoom();

        refreshKnowledge();
        watchNotes();
        watchKnown();
        watchIngredients();
        hoverName = null;
        hoverDetail = null;

        int guiMouseX = (int) Math.round(toGui(mouseX));
        int guiMouseY = (int) Math.round(toGui(mouseY));

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.scale(this.uiScale, this.uiScale);

        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, IMAGE_WIDTH, BOOK_HEIGHT, IMAGE_WIDTH, BOOK_HEIGHT);
        extractMap(graphics, guiMouseX, guiMouseY);
        extractSlotPanels(graphics);
        extractEffectsPanel(graphics, guiMouseX, guiMouseY);
        extractToolPanel(graphics, guiMouseX, guiMouseY);
        extractQueuePanel(graphics, guiMouseX, guiMouseY);
        extractStirButton(graphics, guiMouseX, guiMouseY);
        extractBrewButton(graphics, guiMouseX, guiMouseY);
        extractAddButton(graphics, guiMouseX, guiMouseY);
        drawNameField(graphics);

        pose.popMatrix();
    }

    private static boolean deskInReach;

    public static boolean readingAtDesk() {
        return deskInReach;
    }

    @Override
    public void removed() {
        super.removed();
        deskInReach = false;
    }

    private void refreshKnowledge() {
        TileEntityAlchemyCauldron cauldron = this.getMenu().getCauldron();
        if (this.minecraft == null || this.minecraft.player == null || cauldron.getLevel() == null) {
            known = Set.of();
            notes = Set.of();
            ingredientKeys = Set.of();
            deskInReach = false;
            return;
        }

        Set<AlchemiaEffect> learned = new java.util.LinkedHashSet<>();
        Set<String> noted = new java.util.HashSet<>();
        Set<String> handled = new java.util.HashSet<>();
        List<ItemStack> guides = GuideShelf.readable(this.minecraft.player, cauldron.getLevel(), cauldron.getBlockPos());
        for (ItemStack guide : guides) {
            learned.addAll(RecipeDiscovery.knownTo(guide));
            noted.addAll(WorldEffects.notesIn(guide));
            handled.addAll(IngredientDiscovery.stored(guide));
        }
        known = learned;
        notes = noted;
        ingredientKeys = handled;
        deskInReach = guides.size() > RecipeDiscovery.guidesOf(this.minecraft.player).size();
    }

    private void sendStir() {
        if (pendingStirs.size() >= MAX_PENDING_STIRS) return;

        boolean fast = ClientIngredientPathTooltip.shiftDown();
        sendButton(fast ? AlchemicalCauldronMenu.BUTTON_STIR_FAST : AlchemicalCauldronMenu.BUTTON_STIR);
        pendingStirs.addLast(fast ? TileEntityAlchemyCauldron.FAST_STIRS : 1);
        stirsSent++;
    }

    private void reconcileStir() {
        TileEntityAlchemyCauldron cauldron = this.getMenu().getCauldron();
        int confirmed = cauldron.getStirCount() - stirBase;

        if (confirmed < 0) {
            stirBase = cauldron.getStirCount();
            stirsSent = 0;
            pendingStirs.clear();
            confirmed = 0;
        } else if (confirmed > stirsSent) {
            stirBase += confirmed - stirsSent;
            confirmed = stirsSent;
        }

        while (pendingStirs.size() > stirsSent - confirmed) pendingStirs.removeFirst();

        if (!PotionMap.isReady()) {
            shownCourse = cauldron.getCourse().copy();
            brewAt = new PathPoint(cauldron.getXAlignment(), cauldron.getYAlignment());
            return;
        }

        cauldron.getCourse().legs(map());
        shownCourse = cauldron.getCourse().copy();
        for (int stirs : pendingStirs) {
            for (int count = 0; count < stirs && !shownCourse.teleporting(map()); count++) {
                shownCourse.stir(map(), TileEntityAlchemyCauldron.STIR_STEP, arrival -> true);
            }
        }
        brewAt = shownCourse.position(map());
    }

    private List<PathPoint> flightOf(PotionMap.RouteLeg leg) {
        List<PathPoint> course = new ArrayList<>(leg.curve().size() > 1 ? leg.curve() : leg.path().points());
        PathPoint landing = leg.path().end();

        if (course.isEmpty() || course.get(course.size() - 1).distanceTo(landing) > 0.01) course.add(landing);
        return course;
    }

    private void startFlight(long now) {
        boolean flown = flightPlan.size() > 1
                && flightPlan.get(flightPlan.size() - 1).distanceTo(brewAt) <= HOP_DISTANCE;

        hopFrom = markerAt;
        hopCharge = chargeShown;
        flight = flown ? flightPlan : List.of();
        flightLength = PathGeometry.length(flight);
        flightMs = flown
                ? Mth.clamp(Math.round(flightLength / FLIGHT_PER_MS), FLIGHT_MIN_MS, FLIGHT_MAX_MS)
                : 0L;
        hopStart = flown ? now : now - VANISH_MS;
        flightCamHeld = false;
    }

    private long flightElapsed() {
        return hopStart == Long.MIN_VALUE ? Long.MAX_VALUE : Util.getMillis() - hopStart;
    }

    private boolean inFlight() {
        return flightElapsed() < VANISH_MS + flightMs + APPEAR_MS;
    }

    private double flown() {
        if (flightMs <= 0L) return flightLength;

        double travelled = (double) (flightElapsed() - VANISH_MS) / flightMs;
        return flightLength * PathGeometry.ease(travelled);
    }

    private void followFlight() {
        long elapsed = flightElapsed();
        if (flight.size() < 2 || elapsed < VANISH_MS || elapsed >= VANISH_MS + flightMs + APPEAR_MS) return;

        if (!flightCamHeld) {
            flightCamX = camX - flight.get(0).x();
            flightCamY = camY - flight.get(0).y();
            flightCamHeld = true;
        }

        double walked = flown();
        double held = flightLength <= 0.0 ? 0.0 : 1.0 - walked / flightLength;
        PathPoint at = PathGeometry.pointAt(flight, walked);

        camX = at.x() + flightCamX * held;
        camY = at.y() + flightCamY * held;
        targetZoom = zoom;
        clampCamera();
    }

    private void trackMarker() {
        long now = Util.getMillis();

        double blend = Math.min(1.0, (now - markerFrame) / MARKER_CHASE_MS);
        PotionMap.RouteLeg leg = PotionMap.isReady() ? shownCourse.current(map()) : null;
        boolean winding = leg != null && leg.path().teleport() && !leg.path().stalled();
        double charge = winding && shownCourse.started()
                ? Math.min(1.0, shownCourse.progress() / BrewCourse.TELEPORT_STIR)
                : 0.0;

        if (markerAt == null) {
            markerAt = brewAt;
        } else if (markerAt.distanceTo(brewAt) > HOP_DISTANCE) {
            startFlight(now);
            markerAt = brewAt;
        } else {
            markerAt = markerAt.lerp(brewAt, blend);
        }

        flightPlan = winding ? flightOf(leg) : List.of();
        chargeShown = charge > chargeShown ? chargeShown + (charge - chargeShown) * blend : charge;
        trackGlow(now - markerFrame);
        markerFrame = now;

        if (inFlight()) {
            followFlight();
            return;
        }
        if (!stirring) return;

        double reachX = VIEW_WIDTH * FOLLOW_MARGIN / zoom;
        double reachY = VIEW_HEIGHT * FOLLOW_MARGIN / zoom;
        camX = Mth.clamp(camX, markerAt.x() - reachX, markerAt.x() + reachX);
        camY = Mth.clamp(camY, markerAt.y() - reachY, markerAt.y() + reachY);
        targetZoom = zoom;
        clampCamera();
    }

    private double[] markerState() {
        long elapsed = flightElapsed();

        if (elapsed < VANISH_MS) {
            double k = (double) elapsed / VANISH_MS;
            return new double[]{hopFrom.x(), hopFrom.y(), (1.0 - hopCharge * CHARGE_SHRINK) * (1.0 - k),
                    (hopCharge * 2 + k * 4) * Math.PI};
        }

        if (elapsed < VANISH_MS + flightMs) return new double[]{markerAt.x(), markerAt.y(), 0.0, 0.0};

        if (elapsed < VANISH_MS + flightMs + APPEAR_MS) {
            double t = (double) (elapsed - VANISH_MS - flightMs) / APPEAR_MS;
            double swing = Math.exp(-APPEAR_DAMPING * t) * Math.cos(APPEAR_SWINGS * Math.PI * t) * (1.0 - t);
            return new double[]{markerAt.x(), markerAt.y(), 1.0 - swing, APPEAR_TILT * swing};
        }

        return new double[]{markerAt.x(), markerAt.y(), 1.0 - chargeShown * CHARGE_SHRINK, chargeShown * 2 * Math.PI};
    }

    private void extractFlight(GuiGraphicsExtractor graphics) {
        long elapsed = flightElapsed();
        if (flight.size() < 2 || elapsed >= VANISH_MS + flightMs) return;

        double walked = elapsed < VANISH_MS ? 0.0 : flown();
        double trail = Math.min(FLIGHT_TRAIL, walked);

        for (int step = 0; step < FLIGHT_TRAIL_STEPS && trail > 0.0; step++) {
            double from = walked - trail + trail * step / FLIGHT_TRAIL_STEPS;
            double to = walked - trail + trail * (step + 1) / FLIGHT_TRAIL_STEPS;
            int alpha = (int) Math.round(PATH_ALPHA * (step + 0.5) / FLIGHT_TRAIL_STEPS);
            drawFlightPiece(graphics, PathGeometry.truncate(PathGeometry.tail(flight, from), to - from), from, alpha);
        }

        drawFlightPiece(graphics, PathGeometry.tail(flight, walked), walked, PATH_ALPHA);
    }

    private void drawFlightPiece(GuiGraphicsExtractor graphics, List<PathPoint> piece, double from, int alpha) {
        if (piece.size() < 2 || alpha <= 0) return;
        PathRenderer.dashes(graphics, toScreen(piece), PATH_THICKNESS, ARGB.color(alpha, PATH_INK_RGB), true,
                from * zoom, viewClip(), pathScale());
    }

    private boolean canStir() {
        TileEntityAlchemyCauldron cauldron = this.getMenu().getCauldron();
        return cauldron.getWaterLevel() > 0 && !cauldron.isSpoiled() && PotionMap.isReady() && shownCourse.canStir();
    }

    private TileEntityAlchemyCauldron.BrewCheck brewCheck() {
        return this.getMenu().getCauldron().checkBrew(brewAt);
    }

    private boolean overStirButton(double mouseX, double mouseY) {
        int x = this.leftPos + STIR_BUTTON_X;
        int y = this.topPos + BUTTON_Y;
        return mouseX >= x && mouseX < x + ACTION_BUTTON_W && mouseY >= y && mouseY < y + BUTTON_H;
    }

    private boolean overBrewButton(double mouseX, double mouseY) {
        int x = this.leftPos + BREW_BUTTON_X;
        int y = this.topPos + BUTTON_Y;
        return mouseX >= x && mouseX < x + ACTION_BUTTON_W && mouseY >= y && mouseY < y + BUTTON_H;
    }

    private boolean overAddButton(double mouseX, double mouseY) {
        int x = this.leftPos + ADD_BUTTON_X;
        int y = this.topPos + BUTTON_Y;
        return mouseX >= x && mouseX < x + ADD_BUTTON_W && mouseY >= y && mouseY < y + BUTTON_H;
    }

    private boolean overToolButton(double mouseX, double mouseY) {
        int x = sidePanelX() + TOOL_BUTTON_X;
        int y = this.topPos + TOOL_PANEL_Y + TOOL_BUTTON_Y;
        return mouseX >= x && mouseX < x + TOOL_BUTTON_W && mouseY >= y && mouseY < y + BUTTON_H;
    }

    private int queueCellAt(double mouseX, double mouseY) {
        int gridX = sidePanelX() + (EFFECTS_W - QUEUE_COLUMNS * QUEUE_CELL) / 2;
        int gridY = this.topPos + QUEUE_PANEL_Y + QUEUE_GRID_Y;

        int column = (int) Math.floor((mouseX - gridX) / QUEUE_CELL);
        int row = (int) Math.floor((mouseY - gridY) / QUEUE_CELL);
        if (column < 0 || column >= QUEUE_COLUMNS || row < 0 || row >= QUEUE_ROWS) return -1;

        int index = row * QUEUE_COLUMNS + column;
        return index < queuedIngredients().size() ? index : -1;
    }

    private int drawCentredWrapped(GuiGraphicsExtractor graphics, Component text, int centreX, int y, int maxWidth, int colour) {
        for (FormattedCharSequence line : this.font.split(text, maxWidth)) {
            graphics.text(this.font, line, centreX - this.font.width(line) / 2, y, colour, false);
            y += 10;
        }
        return y;
    }

    private TileEntityAlchemyCauldron.@Nullable PotencyPlan brewPreview(TileEntityAlchemyCauldron cauldron) {
        if (brewCheck() != TileEntityAlchemyCauldron.BrewCheck.READY || cauldron.gainAt(brewAt) <= 0) return null;
        return cauldron.brewedWith(cauldron.effectAt(brewAt));
    }

    private record PotencySlot(Component name, Holder<MobEffect> effect, boolean redstone, boolean incoming, boolean unknown) {}

    private List<PotencySlot> potencySlots(TileEntityAlchemyCauldron cauldron) {
        List<PotencySlot> slots = new ArrayList<>();

        TileEntityAlchemyCauldron.PotencyPlan preview = brewPreview(cauldron);
        TileEntityAlchemyCauldron.PotencyPlan plan = preview == null ? cauldron.currentPotency() : preview;
        Holder<MobEffect> brewing = preview == null ? null : preview.effects().getLast().getEffect();
        int held = brewing == null ? 0 : cauldron.heldPotency(brewing);
        int bonus = plan.redstone() * TileEntityAlchemyCauldron.REDSTONE_DURATION_BONUS;

        for (String token : plan.order()) {
            if (token.equals(TileEntityAlchemyCauldron.REDSTONE_TOKEN)) {
                slots.add(new PotencySlot(Component.translatable("alchemia.gui.redstone", bonus / 20), null, true, false, false));
                continue;
            }

            for (MobEffectInstance instance : plan.effects()) {
                if (!instance.getEffect().getRegisteredName().equals(token)) continue;

                AlchemiaEffect slotRecipe = RecipeDiscovery.byEffect(instance.getEffect());
                boolean unknown = slotRecipe == null || !known.contains(slotRecipe);
                Component name = Component.translatable("alchemia.gui.effect.entry",
                        unknown ? Component.translatable("alchemia.gui.unknown") : instance.getEffect().value().getDisplayName(), instance.getAmplifier() + 1,
                        (instance.getDuration() + bonus) / 20);
                boolean fresh = instance.getEffect().equals(brewing);
                for (int i = 0; i <= instance.getAmplifier(); i++) {
                    slots.add(new PotencySlot(name, instance.getEffect(), false, fresh && i >= held, unknown));
                }
            }
        }
        return slots;
    }

    private @Nullable Component rotationNote(TileEntityAlchemyCauldron cauldron) {
        TileEntityAlchemyCauldron.PotencyPlan preview = brewPreview(cauldron);
        if (preview == null) return null;

        Holder<MobEffect> brewing = preview.effects().getLast().getEffect();
        Component note = null;
        for (MobEffectInstance instance : cauldron.getEffectList()) {
            if (instance.getEffect().equals(brewing)) continue;

            int left = 0;
            for (MobEffectInstance kept : preview.effects()) {
                if (kept.getEffect().equals(instance.getEffect())) left = kept.getAmplifier() + 1;
            }
            if (left > instance.getAmplifier()) continue;

            Component name = instance.getEffect().value().getDisplayName();
            Component part = left == 0
                    ? Component.translatable("alchemia.gui.brew.pushed", name)
                    : Component.translatable("alchemia.gui.brew.fades", name, left);
            note = note == null ? part : Component.empty().append(note).append(", ").append(part);
        }

        int lostRedstone = cauldron.redstoneCharges() - preview.redstone();
        if (lostRedstone > 0) {
            Component part = Component.translatable("alchemia.gui.brew.pushed_redstone", lostRedstone);
            note = note == null ? part : Component.empty().append(note).append(", ").append(part);
        }
        return note;
    }

    private void drawDormant(GuiGraphicsExtractor graphics, PotionMap.MapEntry entry, int cx, int cy, int node) {
        double reach = PotionMap.EFFECT_RADIUS * zoom;
        if (reach >= 2.0) {
            PathRenderer.disc(graphics, cx, cy, reach, ARGB.color(DORMANT_FILL_ALPHA, DORMANT_RGB));

            double lit = entry == glowEntry ? glow : 0.0;
            int edgeAlpha = (int) Math.round(Mth.lerp(lit, DORMANT_EDGE_ALPHA, DORMANT_LIT_ALPHA));
            int edgeRgb = ARGB.srgbLerp((float) lit, DORMANT_RGB, DORMANT_LIT_RGB);
            PathRenderer.ring(graphics, cx, cy, reach, ARGB.color(edgeAlpha, edgeRgb), false);
            if (lit > 0.0) PathRenderer.ring(graphics, cx, cy, reach - 1.0, ARGB.color(edgeAlpha / 2, edgeRgb), false);
        }
        AlchemiaEffect recipe = entry.effect().getRecipe();
        if (recipe != null && !known.contains(recipe) && WorldEffects.anyVisible(notes, recipe)) {
            drawClosedEye(graphics, cx, cy, node);
        } else {
            drawSymbol(graphics, cx, cy, node, DORMANT_TINT);
        }
    }

    private void trackGlow(double elapsedMs) {
        PotionMap.MapEntry near = null;
        if (PotionMap.isReady() && markerAt != null) {
            PotionMap.MapEntry entry = map().nearest(markerAt.x(), markerAt.y(), PotionMap.EFFECT_RADIUS);
            if (entry != null && this.getMenu().getCauldron().isDormant(entry.effect())) near = entry;
        }

        double step = Math.min(1.0, elapsedMs / DORMANT_GLOW_MS);
        if (near != null && near != glowEntry) {
            glowEntry = near;
            glow = 0.0;
        }
        glow += ((near != null ? 1.0 : 0.0) - glow) * step;
    }

    private void watchNotes() {
        if (notesSeen == null) {
            notesSeen = new java.util.HashSet<>(notes);
            return;
        }

        Component news = WorldEffects.newsOf(notesSeen, notes);
        if (news != null) queueNotice(Component.translatable("alchemia.gui.noted", news));
        notesSeen = new java.util.HashSet<>(notes);
    }

    private void queueNotice(Component text) {
        if (notice == null) {
            notice = text;
            noticeStart = Util.getMillis();
        } else {
            notices.addLast(text);
        }
    }

    private Set<String> ingredientKeys = Set.of();
    private Set<String> ingredientsSeen;

    private void watchIngredients() {
        if (ingredientsSeen == null) {
            ingredientsSeen = new java.util.HashSet<>(ingredientKeys);
            return;
        }

        Component added = null;
        for (String key : ingredientKeys) {
            if (ingredientsSeen.contains(key)) continue;
            Component name = ingredientName(key);
            if (name == null) continue;
            added = added == null ? name : Component.empty().append(added).append(", ").append(name);
        }
        if (added != null) queueNotice(Component.translatable("alchemia.gui.recorded", added));
        ingredientsSeen = new java.util.HashSet<>(ingredientKeys);
    }

    private static @Nullable Component ingredientName(String key) {
        Ingredients ingredient = IngredientDiscovery.byName(key);
        if (ingredient != null) {
            return new ItemStack(ItemRegistry.getItemFromRegistry(ingredient.getRegistryName())).getHoverName();
        }
        Item reagent = IngredientDiscovery.reagentByKey(key);
        return reagent == null ? null : new ItemStack(reagent).getHoverName();
    }

    private void watchKnown() {
        if (knownSeen == null) {
            knownSeen = new java.util.HashSet<>(known);
            return;
        }

        Component added = null;
        for (AlchemiaEffect recipe : known) {
            if (knownSeen.contains(recipe)) continue;

            Component name = recipe.displayName();
            added = added == null ? name : Component.empty().append(added).append(", ").append(name);
        }

        if (added != null) {
            queueNotice(Component.translatable("alchemia.gui.recorded", added));
        }
        knownSeen = new java.util.HashSet<>(known);
    }

    private void extractNotice(GuiGraphicsExtractor graphics) {
        if (notice == null) return;

        long elapsed = Util.getMillis() - noticeStart;
        if (elapsed >= NOTICE_MS) {
            notice = notices.pollFirst();
            noticeStart = Util.getMillis();
            return;
        }

        double fade = elapsed < NOTICE_FADE_IN_MS
                ? (double) elapsed / NOTICE_FADE_IN_MS
                : Math.min(1.0, (double) (NOTICE_MS - elapsed) / NOTICE_FADE_OUT_MS);
        int alpha = (int) Math.round(255 * fade);
        if (alpha < 8) return;

        List<FormattedCharSequence> lines = this.font.split(notice, VIEW_WIDTH - NOTICE_PAD * 4);
        int width = 0;
        for (FormattedCharSequence line : lines) width = Math.max(width, this.font.width(line));
        width += NOTICE_PAD * 2;
        int height = lines.size() * LINE_HEIGHT + NOTICE_PAD * 2 - 2;

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.scale(this.uiScale, this.uiScale);

        int x = viewLeft() + (VIEW_WIDTH - width) / 2;
        int y = viewTop() + NOTICE_TOP;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, AlchemiaSprites.PATH_TOOLTIP_PANEL, x, y, width, height,
                ARGB.color(alpha, 0xFFFFFF));

        int lineY = y + NOTICE_PAD;
        for (FormattedCharSequence line : lines) {
            graphics.text(this.font, line, x + (width - this.font.width(line)) / 2, lineY, ARGB.color(alpha, INK & 0xFFFFFF), false);
            lineY += LINE_HEIGHT;
        }

        pose.popMatrix();
    }

    private Component dormantHint(AlchemiaEffect recipe) {
        WorldEffects.Requirement requirement = WorldEffects.requirement(recipe);
        return requirement != null && WorldEffects.wakeVisible(notes, recipe) ? requirement.hint() : null;
    }

    private void drawUnknownSymbol(GuiGraphicsExtractor graphics, int cx, int cy, int node) {
        drawSymbol(graphics, cx, cy, node, UNKNOWN_TINT);
    }

    private void drawClosedEye(GuiGraphicsExtractor graphics, int cx, int cy, int node) {
        int w = Math.max(1, Math.round(node * (float) CLOSED_EYE_W / UNKNOWN_CELL));
        int h = Math.max(1, Math.round(w * (float) CLOSED_EYE_H / CLOSED_EYE_W));
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUIDE_WIDGETS,
                cx - w / 2, cy - h / 2, CLOSED_EYE_U, CLOSED_EYE_V, w, h,
                CLOSED_EYE_W, CLOSED_EYE_H, GUIDE_SHEET, GUIDE_SHEET, -1);
    }

    private void drawOpenEye(GuiGraphicsExtractor graphics, int cx, int cy, int node) {
        int w = Math.max(1, Math.round(node * (float) OPEN_EYE_W / UNKNOWN_CELL));
        int h = Math.max(1, Math.round(w * (float) OPEN_EYE_H / OPEN_EYE_W));
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUIDE_WIDGETS,
                cx - w / 2, cy - h / 2, OPEN_EYE_U, OPEN_EYE_V, w, h,
                OPEN_EYE_W, OPEN_EYE_H, GUIDE_SHEET, GUIDE_SHEET, -1);
    }

    private void drawSymbol(GuiGraphicsExtractor graphics, int cx, int cy, int node, int tint) {
        int w = Math.max(1, Math.round(node * (float) UNKNOWN_W / UNKNOWN_CELL));
        int h = Math.max(1, Math.round(node * (float) UNKNOWN_H / UNKNOWN_CELL));
        graphics.blit(RenderPipelines.GUI_TEXTURED, SYMBOLS_TEXTURE,
                cx - w / 2, cy - h / 2, UNKNOWN_U, UNKNOWN_V, w, h,
                UNKNOWN_W, UNKNOWN_H, SYMBOLS_W, SYMBOLS_H, tint);
    }

    private void extractEffectsPanel(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        TileEntityAlchemyCauldron cauldron = this.getMenu().getCauldron();

        int budget = TileEntityAlchemyCauldron.maxPotency();
        int rows = Math.max(1, (budget + POTENCY_PER_ROW - 1) / POTENCY_PER_ROW);
        int columns = Math.min(budget, POTENCY_PER_ROW);

        boolean ruined = cauldron.isSpoiled();
        List<FormattedCharSequence> ruinedLines = ruined
                ? this.font.split(Component.translatable("alchemia.gui.effects.ruined"), EFFECTS_W - 8)
                : List.of();

        int panelX = this.leftPos + IMAGE_WIDTH + EFFECTS_GAP;
        int panelY = this.topPos + EFFECTS_Y;
        int panelH = ruined
                ? RUINED_TEXT_Y + ruinedLines.size() * LINE_HEIGHT + 6
                : 30 + rows * POTENCY_CELL + 8 + INSTABILITY_BLOCK_H;

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, AlchemiaSprites.PANEL, panelX, panelY, EFFECTS_W, panelH);

        drawCentredWrapped(graphics, Component.translatable("alchemia.gui.effects.title"),
                panelX + EFFECTS_W / 2, panelY + 5, EFFECTS_W - 8, INK);

        int swatch = cauldron.getWaterLevel() > 0 ? cauldron.getPotionColor() : 0x7A6A50;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, AlchemiaSprites.POTION_SWATCH,
                panelX + 4, panelY + 17, EFFECTS_W - 8, 4, ARGB.opaque(swatch));

        if (ruined) {
            graphics.item(new ItemStack(Items.SKELETON_SKULL), panelX + (EFFECTS_W - 16) / 2, panelY + 26);

            int lineY = panelY + RUINED_TEXT_Y;
            for (FormattedCharSequence line : ruinedLines) {
                graphics.text(this.font, line, panelX + (EFFECTS_W - this.font.width(line)) / 2, lineY,
                        ARGB.opaque(BLOCKED_RGB), false);
                lineY += LINE_HEIGHT;
            }
            return;
        }

        List<PotencySlot> filled = potencySlots(cauldron);

        int gridX = panelX + (EFFECTS_W - columns * POTENCY_CELL) / 2;
        int gridY = panelY + 26;
        Component hovered = null;

        for (int index = 0; index < budget; index++) {
            int x = gridX + (index % POTENCY_PER_ROW) * POTENCY_CELL;
            int y = gridY + (index / POTENCY_PER_ROW) * POTENCY_CELL;

            slotBackdrop(graphics, x, y);

            if (index >= filled.size()) continue;

            PotencySlot slot = filled.get(index);
            if (slot.redstone()) {
                graphics.item(new ItemStack(Items.REDSTONE), x + 1, y + 1);
            } else {
                double beat = (Math.sin(Util.getMillis() / 300.0) + 1.0) * 0.5;
                int tint = slot.incoming() ? ARGB.color((int) Math.round(70 + beat * 110), 0xFFFFFF) : 0xFFFFFFFF;
                if (slot.unknown()) {
                    int alpha = slot.incoming() ? (int) Math.round(70 + beat * 110) : 255;
                    drawSymbol(graphics, x + 9, y + 9, 16, ARGB.color(alpha * (UNKNOWN_TINT >>> 24) / 255, UNKNOWN_TINT & 0xFFFFFF));
                } else {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, AlchemiaEffects.sprite(slot.effect()),
                        x + 1, y + 1, 16, 16, tint);
                }
            }

            if (mouseX >= x && mouseX < x + POTENCY_CELL && mouseY >= y && mouseY < y + POTENCY_CELL) {
                hovered = slot.name();
            }
        }

        int used = cauldron.totalPotency();
        drawCentredWrapped(graphics, Component.translatable("alchemia.gui.effects.budget", used, budget),
                panelX + EFFECTS_W / 2, panelY + panelH - INSTABILITY_BLOCK_H - 11, EFFECTS_W - 8,
                used >= budget ? ARGB.opaque(BLOCKED_RGB) : INK_FAINT);

        Component instabilityHover = extractInstability(graphics, cauldron, panelX,
                panelY + panelH - INSTABILITY_BLOCK_H, mouseX, mouseY);
        if (instabilityHover != null) hovered = instabilityHover;

        if (hovered != null) graphics.setTooltipForNextFrame(hovered, toScreen(mouseX), toScreen(mouseY));
    }

    private Component extractInstability(GuiGraphicsExtractor graphics, TileEntityAlchemyCauldron cauldron,
                                         int panelX, int panelY, int mouseX, int mouseY) {
        int cap = cauldron.getInstabilityCap();
        int spent = Math.min(cauldron.getInstability(), cap);

        drawCentredWrapped(graphics, Component.translatable("alchemia.gui.base.label",
                        cauldron.getBase().displayName()),
                panelX + EFFECTS_W / 2, panelY, EFFECTS_W - 8, INK_FAINT);

        int gridX = panelX + (EFFECTS_W - cap * POTENCY_CELL) / 2;
        int gridY = panelY + 11;
        Component hovered = null;

        for (int index = 0; index < cap; index++) {
            int x = gridX + index * POTENCY_CELL;
            slotBackdrop(graphics, x, gridY);

            if (index < spent) {
                graphics.item(new ItemStack(Items.SKELETON_SKULL), x + 1, gridY + 1);
            }

            if (mouseX >= x && mouseX < x + POTENCY_CELL && mouseY >= gridY && mouseY < gridY + POTENCY_CELL) {
                hovered = Component.translatable("alchemia.gui.instability", spent, cap);
            }
        }

        return hovered;
    }

    private void extractStirButton(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        boolean ready = canStir();
        boolean hovered = overStirButton(mouseX, mouseY);
        int x = this.leftPos + STIR_BUTTON_X;
        int y = this.topPos + BUTTON_Y;

        Identifier sprite = !ready
                ? AlchemiaSprites.BUTTON_DISABLED
                : (stirring || hovered ? AlchemiaSprites.BUTTON_HIGHLIGHTED : AlchemiaSprites.BUTTON);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, ACTION_BUTTON_W, BUTTON_H);

        Component label = Component.translatable("alchemia.gui.stir");
        graphics.text(this.font, label, x + (ACTION_BUTTON_W - this.font.width(label)) / 2, y + 5,
                ready ? INK : INK_FAINT, false);

        if (hovered) {
            graphics.setTooltipForNextFrame(Component.translatable(ready
                    ? "alchemia.gui.stir.hint" : "alchemia.gui.stir.idle"), toScreen(mouseX), toScreen(mouseY));
        }
    }

    private Component heldBackHint() {
        TileEntityAlchemyCauldron cauldron = this.getMenu().getCauldron();
        PotionMap.PotionEffectPosition raw = cauldron.rawEffectAt(brewAt);
        PotionMap.PotionEffectPosition capped = cauldron.effectAt(brewAt);

        if (raw.getEffect() == null || raw.getStrength() <= capped.getStrength()) return null;
        int tier = capped.getStrength() + 2;
        if (!WorldEffects.tierVisible(notes, raw.getRecipe(), tier)) return null;
        return WorldEffects.levelHint(raw.getRecipe(), tier);
    }

    private void drawHeldBack(GuiGraphicsExtractor graphics, PotionMap.MapEntry entry, int cx, int cy) {
        int strength = entry.effect().getStrength();
        int cap = this.getMenu().getCauldron().levelCap(entry.effect().getRecipe());
        if (cap > strength) return;

        double radius = (cap == strength ? PotionMap.EFFECT_CORE : PotionMap.CELL * (strength - cap)) * zoom;
        if (radius < 2.0) return;

        PathRenderer.disc(graphics, cx, cy, radius, ARGB.color(HELD_FILL_ALPHA, DORMANT_RGB));
        PathRenderer.ring(graphics, cx, cy, radius, ARGB.color(DORMANT_EDGE_ALPHA, DORMANT_RGB), false);
    }

    private Component brewHint(TileEntityAlchemyCauldron.BrewCheck check) {
        TileEntityAlchemyCauldron cauldron = this.getMenu().getCauldron();

        return switch (check) {
            case NOTHING -> Component.translatable("alchemia.brew.nothing");
            case DORMANT -> {
                Component hint = dormantHint(cauldron.effectAt(brewAt).getRecipe());
                yield hint != null ? Component.translatable("alchemia.gui.brew.dormant", hint)
                        : Component.translatable("alchemia.brew.dormant");
            }
            case WEAKER -> Component.translatable("alchemia.brew.weaker");
            case TOO_POTENT -> Component.translatable("alchemia.brew.too_potent");
            case UNAVAILABLE -> null;
            case READY -> {
                PotionMap.PotionEffectPosition found = cauldron.effectAt(brewAt);
                if (found.getEffect() == null) yield null;

                boolean discovered = found.getRecipe() != null && known.contains(found.getRecipe());
                Component name = discovered
                        ? found.getEffect().value().getDisplayName()
                        : Component.translatable("alchemia.gui.unknown");
                Component ready = Component.translatable(found.isPerfect() ? "alchemia.gui.brew.perfect" : "alchemia.gui.brew.ready",
                        name, found.getStrength() + 1);
                Component rotation = rotationNote(cauldron);
                yield rotation == null ? ready : Component.translatable("alchemia.gui.brew.rotates", ready, rotation);
            }
        };
    }

    private void extractBrewButton(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        TileEntityAlchemyCauldron.BrewCheck check = brewCheck();
        boolean ready = check == TileEntityAlchemyCauldron.BrewCheck.READY;
        boolean hovered = overBrewButton(mouseX, mouseY);
        int x = this.leftPos + BREW_BUTTON_X;
        int y = this.topPos + BUTTON_Y;

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                ready ? AlchemiaSprites.BUTTON_HIGHLIGHTED : AlchemiaSprites.BUTTON_DISABLED,
                x, y, ACTION_BUTTON_W, BUTTON_H);

        if (ready) {
            double beat = (Math.sin(Util.getMillis() / 220.0) + 1.0) * 0.5;
            graphics.fill(x + 1, y + 1, x + ACTION_BUTTON_W - 1, y + BUTTON_H - 1,
                    ARGB.color((int) Math.round(30 + beat * (hovered ? 110 : 80)), BREW_GLOW_RGB));
        }

        Component label = Component.translatable("alchemia.gui.brew");
        graphics.text(this.font, label, x + (ACTION_BUTTON_W - this.font.width(label)) / 2, y + 5,
                ready ? INK : INK_FAINT, false);

        Component hint = hovered ? brewHint(check) : null;
        if (hint == null) return;

        List<FormattedCharSequence> lines = new ArrayList<>();
        lines.add(hint.getVisualOrderText());
        Component held = check == TileEntityAlchemyCauldron.BrewCheck.READY || check == TileEntityAlchemyCauldron.BrewCheck.WEAKER
                ? heldBackHint() : null;
        if (held != null) lines.add(held.getVisualOrderText());
        graphics.setTooltipForNextFrame(lines, toScreen(mouseX), toScreen(mouseY));
    }

    private PotionMap map() {
        return PotionMap.get(this.getMenu().getCauldron().getBase());
    }

    private List<ItemStack> queuedIngredients() {
        return shownCourse.plannedStacks();
    }

    private ItemStack activeIngredient() {
        return this.getMenu().getSlot(TileEntityAlchemyCauldron.ACTIVE_SLOT).getItem();
    }

    private int sidePanelX() {
        return this.leftPos + IMAGE_WIDTH + EFFECTS_GAP;
    }

    private void sendButton(int buttonId) {
        if (this.minecraft == null || this.minecraft.gameMode == null) return;
        this.minecraft.gameMode.handleInventoryButtonClick(this.getMenu().containerId, buttonId);
    }

    private boolean canAdd() {
        return this.getMenu().getCauldron().canQueue();
    }

    private boolean canApplyTool() {
        return this.getMenu().getCauldron().canApplyTool();
    }

    private IngredientTool heldTool() {
        return IngredientTool.of(this.getMenu().getCauldron().getTool());
    }

    private void extractAddButton(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        boolean ready = canAdd();
        int x = this.leftPos + ADD_BUTTON_X;
        int y = this.topPos + BUTTON_Y;

        Identifier sprite = !ready
                ? AlchemiaSprites.BUTTON_DISABLED
                : (overAddButton(mouseX, mouseY) ? AlchemiaSprites.BUTTON_HIGHLIGHTED : AlchemiaSprites.BUTTON);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, ADD_BUTTON_W, BUTTON_H);

        Component label = Component.translatable("alchemia.gui.add");
        graphics.text(this.font, label, x + (ADD_BUTTON_W - this.font.width(label)) / 2, y + 5,
                ready ? INK : INK_FAINT, false);

        if (!overAddButton(mouseX, mouseY)) return;

        TileEntityAlchemyCauldron cauldron = this.getMenu().getCauldron();
        BrewModifiers modifier = BrewModifiers.of(activeIngredient());
        Component hint;
        if (modifier != null) {
            String refusal = cauldron.modifierRefusal(modifier);
            Imbuements.Attuner attuner = Imbuements.of(activeIngredient());
            if (refusal == null && attuner != null) {
                hint = Component.translatable("alchemia.gui.add.attuner", attuner.region());
            } else {
                hint = Component.translatable(refusal != null ? refusal
                        : "alchemia.gui.add." + modifier.name().toLowerCase(java.util.Locale.ROOT));
            }
        } else {
            hint = cauldron.queueFull()
                    ? Component.translatable("alchemia.gui.queue.full", TileEntityAlchemyCauldron.MAX_QUEUE)
                    : Component.translatable("alchemia.gui.add.hint");
        }
        graphics.setTooltipForNextFrame(hint, toScreen(mouseX), toScreen(mouseY));
    }

    private void extractToolPanel(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        ItemStack held = this.getMenu().getCauldron().getTool();
        IngredientTool tool = IngredientTool.of(held);

        int panelX = sidePanelX();
        int panelY = this.topPos + TOOL_PANEL_Y;

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, AlchemiaSprites.TOOL_PANEL,
                panelX, panelY, EFFECTS_W, TOOL_PANEL_H);

        Component title = tool == null
                ? Component.translatable("alchemia.gui.tool.title")
                : held.getHoverName();
        drawCentredWrapped(graphics, title, panelX + EFFECTS_W / 2, panelY + 5, EFFECTS_W - 8, INK);

        slotBackdrop(graphics, this.leftPos + AlchemicalCauldronMenu.TOOL_SLOT_X,
                this.topPos + AlchemicalCauldronMenu.TOOL_SLOT_Y);

        if (tool == null) {
            drawCentredWrapped(graphics, Component.translatable("alchemia.gui.tool.missing"),
                    panelX + TOOL_TRACK_X + AlchemiaSprites.GRIND_W / 2, panelY + TOOL_TRACK_Y,
                    EFFECTS_W - TOOL_TRACK_X - 6, INK_FAINT);
        } else {
            extractToolTrack(graphics, tool, panelX + TOOL_TRACK_X, panelY + TOOL_TRACK_Y);
        }

        extractToolButton(graphics, tool, panelX + TOOL_BUTTON_X, panelY + TOOL_BUTTON_Y, mouseX, mouseY);
    }

    private void extractToolTrack(GuiGraphicsExtractor graphics, IngredientTool tool, int x, int y) {
        ItemStack active = activeIngredient();
        if (!TileEntityAlchemyCauldron.isBrewingInput(active)) return;

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, AlchemiaSprites.GRIND_TRACK,
                x, y, AlchemiaSprites.GRIND_W, AlchemiaSprites.GRIND_H);

        int filled = Mth.clamp(Math.round(tool.progress(active) * AlchemiaSprites.GRIND_W),
                0, AlchemiaSprites.GRIND_W);
        if (filled <= 0) return;

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, AlchemiaSprites.GRIND_PROGRESS,
                AlchemiaSprites.GRIND_W, AlchemiaSprites.GRIND_H, 0, 0,
                x, y, filled, AlchemiaSprites.GRIND_H);
    }

    private void extractToolButton(GuiGraphicsExtractor graphics, IngredientTool tool,
                                   int x, int y, int mouseX, int mouseY) {
        boolean ready = canApplyTool();
        boolean hovered = overToolButton(mouseX, mouseY);

        Identifier sprite = !ready
                ? AlchemiaSprites.BUTTON_DISABLED
                : (applying || hovered ? AlchemiaSprites.BUTTON_HIGHLIGHTED : AlchemiaSprites.BUTTON);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, TOOL_BUTTON_W, BUTTON_H);

        Component label = Component.translatable(tool == null ? "alchemia.gui.tool.apply" : tool.actionKey());
        graphics.text(this.font, label, x + (TOOL_BUTTON_W - this.font.width(label)) / 2, y + 5,
                ready ? INK : INK_FAINT, false);

        if (hovered) {
            graphics.setTooltipForNextFrame(Component.translatable("alchemia.gui.tool.hint"),
                    toScreen(mouseX), toScreen(mouseY));
        }
    }

    private void extractQueuePanel(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        List<ItemStack> queued = queuedIngredients();

        int panelX = sidePanelX();
        int panelY = this.topPos + QUEUE_PANEL_Y;

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, AlchemiaSprites.PANEL,
                panelX, panelY, EFFECTS_W, QUEUE_PANEL_H);

        drawCentredWrapped(graphics, Component.translatable("alchemia.gui.queue.title",
                        queued.size(), TileEntityAlchemyCauldron.MAX_QUEUE),
                panelX + EFFECTS_W / 2, panelY + 4, EFFECTS_W - 8, INK);

        int gridX = panelX + (EFFECTS_W - QUEUE_COLUMNS * QUEUE_CELL) / 2;
        int gridY = panelY + QUEUE_GRID_Y;
        List<Component> hovered = null;

        for (int index = 0; index < QUEUE_COLUMNS * QUEUE_ROWS; index++) {
            int x = gridX + (index % QUEUE_COLUMNS) * QUEUE_CELL;
            int y = gridY + (index / QUEUE_COLUMNS) * QUEUE_CELL;

            slotBackdrop(graphics, x, y);
            if (index >= queued.size()) continue;

            ItemStack stack = queued.get(index);
            graphics.item(stack, x + 1, y + 1);

            if (index == 0 && shownCourse.started() && PotionMap.isReady()) {
                PotionMap.RouteLeg leg = shownCourse.current(map());
                double length = leg == null ? 0.0 : BrewCourse.lengthOf(leg);
                int filled = length <= 0.0 ? 0 : Mth.clamp((int) Math.round(16 * shownCourse.progress() / length), 0, 16);
                graphics.fill(x + 1, y + 15, x + 1 + filled, y + 17, INK);
            }

            if (mouseX >= x && mouseX < x + QUEUE_CELL && mouseY >= y && mouseY < y + QUEUE_CELL) {
                hovered = queueTooltip(index, stack);
            }
        }

        drawLureBrackets(graphics, gridX, gridY, queued.size(), queueCellAt(mouseX, mouseY));

        if (hovered != null) graphics.setComponentTooltipForNextFrame(this.font, hovered, toScreen(mouseX), toScreen(mouseY));
    }

    private void extractSlotPanels(GuiGraphicsExtractor graphics) {
        slotBackdrop(graphics, this.leftPos + AlchemicalCauldronMenu.ACTIVE_SLOT_X, this.topPos + SLOT_ROW_Y);

        int panelX = this.leftPos + INVENTORY_X - 6;
        int panelY = this.topPos + INVENTORY_Y - 6;
        int panelW = 9 * 18 + 12;
        int panelH = (HOTBAR_Y + 18) - INVENTORY_Y + 12;

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, AlchemiaSprites.PANEL, panelX, panelY, panelW, panelH);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                slotBackdrop(graphics, this.leftPos + INVENTORY_X + column * 18, this.topPos + INVENTORY_Y + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            slotBackdrop(graphics, this.leftPos + INVENTORY_X + column * 18, this.topPos + HOTBAR_Y);
        }
    }

    private void slotBackdrop(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, AlchemiaSprites.PARCHMENT_SLOT,
                x, y, AlchemiaSprites.SLOT_SIZE, AlchemiaSprites.SLOT_SIZE);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.scale(this.uiScale, this.uiScale);

        super.extractContents(graphics, (int) Math.round(toGui(mouseX)), (int) Math.round(toGui(mouseY)), a);
        graphics.blit(RenderPipelines.GUI_TEXTURED, FRAME_TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, IMAGE_WIDTH, BOOK_HEIGHT, IMAGE_WIDTH, BOOK_HEIGHT);
        extractOverlayText(graphics);

        pose.popMatrix();
    }

    @Override
    public void extractCarriedItem(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.scale(this.uiScale, this.uiScale);

        super.extractCarriedItem(graphics, (int) Math.round(toGui(mouseX)), (int) Math.round(toGui(mouseY)));

        pose.popMatrix();
    }

#if MC_VER < 26200
    @Override
    public void extractSnapbackItem(GuiGraphicsExtractor graphics) {
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.scale(this.uiScale, this.uiScale);

        super.extractSnapbackItem(graphics);

        pose.popMatrix();
    }
#endif

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        extractNotice(graphics);

        if (hoverName != null) {
            List<net.minecraft.util.FormattedCharSequence> lines = new ArrayList<>();
            lines.add(hoverName.getVisualOrderText());
            if (hoverDetail != null) lines.add(hoverDetail.getVisualOrderText());
            graphics.setTooltipForNextFrame(lines, hoverX, hoverY);
        }
    }

    private void drawNameField(GuiGraphicsExtractor graphics) {
        int fieldX = viewLeft() + NAME_X;
        int fieldY = viewTop() + NAME_Y;

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, AlchemiaSprites.NAME_FIELD,
                fieldX, fieldY, NAME_W, NAME_H);
    }

    private void drawNameText(GuiGraphicsExtractor graphics) {
        if (this.nameField == null) return;

        int textX = nameTextX();
        int fieldY = viewTop() + NAME_Y;
        int textY = fieldY + NAME_TEXT_Y;
        int top = textY - 1;
        int bottom = textY + 9;

        String value = this.nameField.getValue();
        boolean focused = this.nameField.isFocused();

        if (value.isEmpty() && !focused) {
            graphics.text(this.font, Component.translatable("alchemia.gui.name.hint"), textX, textY, INK_FAINT, false);
            return;
        }

        int cursor = Mth.clamp(this.nameField.getCursorPosition(), 0, value.length());
        int selection = cursor;

        String highlighted = this.nameField.getHighlighted();
        if (!highlighted.isEmpty()) {
            int before = cursor - highlighted.length();
            selection = before >= 0 && value.startsWith(highlighted, before)
                    ? before
                    : Math.min(value.length(), cursor + highlighted.length());
        }

        int inner = NAME_W - 4;
        int start = focused ? scrollName(value, cursor, inner) : 0;
        int end = fitting(value, start, inner);
        String shown = value.substring(start, end);
        if (!focused && end < value.length()) {
            String ellipsis = "...";
            shown = value.substring(0, fitting(value, 0, inner - nameWidth(ellipsis))) + ellipsis;
        }

        if (focused && selection != cursor) {
            int from = Mth.clamp(Math.min(cursor, selection), start, end);
            int to = Mth.clamp(Math.max(cursor, selection), start, end);
            int selStart = textX + nameWidth(value.substring(start, from));
            int selEnd = textX + nameWidth(value.substring(start, to));
            graphics.fill(selStart, top, selEnd, bottom, NAME_SELECTION);
        }

        graphics.text(this.font, shown, textX, textY, INK, false);

        if (focused && cursor >= start && cursor <= end && (System.currentTimeMillis() / 400L) % 2L == 0L) {
            int caret = textX + nameWidth(value.substring(start, cursor));
            graphics.fill(caret, top, caret + 1, bottom, INK);
        }
    }

    private int nameTextX() {
        return viewLeft() + NAME_X + 3;
    }

    private int nameWidth(String text) {
        return this.font.width(text);
    }

    private int fitting(String value, int start, int width) {
        int end = start;
        while (end < value.length() && nameWidth(value.substring(start, end + 1)) <= width) end++;
        return end;
    }

    private int scrollName(String value, int cursor, int inner) {
        nameScroll = Mth.clamp(nameScroll, 0, value.length());
        if (cursor < nameScroll) nameScroll = cursor;
        while (nameScroll < cursor && nameWidth(value.substring(nameScroll, cursor)) > inner - 1) nameScroll++;
        while (nameScroll > 0 && nameWidth(value.substring(nameScroll - 1)) <= inner - 1) nameScroll--;
        return nameScroll;
    }

    private int nameIndexAt(double guiX) {
        String value = this.nameField.getValue();
        int start = Mth.clamp(nameScroll, 0, value.length());
        double offset = guiX - nameTextX();
        int best = start;
        double nearest = Double.MAX_VALUE;
        for (int index = start; index <= value.length(); index++) {
            double distance = Math.abs(nameWidth(value.substring(start, index)) - offset);
            if (distance < nearest) {
                nearest = distance;
                best = index;
            }
        }
        return best;
    }

    private void extractOverlayText(GuiGraphicsExtractor graphics) {
        drawNameText(graphics);

        int left = viewLeft() + 4;
        int top = viewTop() + 3;

        Component recipes = Component.translatable("alchemia.gui.recipes", known.size(), RecipeDiscovery.total());
        graphics.text(this.font, recipes, viewLeft() + VIEW_WIDTH - 4 - this.font.width(recipes), top, INK_FAINT, false);

        Component zoomLabel = Component.translatable("alchemia.gui.zoom", Math.round(zoom * 100));
        graphics.text(this.font, zoomLabel, left, viewTop() + VIEW_HEIGHT - 11, INK_FAINT, false);

    }

    private void extractMap(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (!PotionMap.isReady()) return;

        TileEntityAlchemyCauldron cauldron = this.getMenu().getCauldron();
        PathPoint centre = toScreen(new PathPoint(map().getMiddlePosition(), map().getMiddlePosition()));

        int left = viewLeft();
        int top = viewTop();
        int right = left + VIEW_WIDTH;
        int bottom = top + VIEW_HEIGHT;

        graphics.enableScissor(left, top, right, bottom);

        drawMapEdge(graphics, left, top, right, bottom);

        extractDeadzones(graphics, left, top, right, bottom);

        int node = Math.max(6, (int) Math.round(NODE_BASE * Mth.clamp(zoom, 0.55, 2.0)));
        int half = node / 2;
        int thickness = Math.max(1, (int) Math.round(2 * Mth.clamp(zoom, 0.5, 2.0)));

        for (PotionMap.MapEntry entry : map().entries()) {
            if (entry.effect().getEffect() == null) continue;

            PathPoint at = toScreen(entry.position());
            int nx = at.roundedX();
            int ny = at.roundedY();

            if (nx < left - node || nx > right + node || ny < top - node || ny > bottom + node) continue;

            AlchemiaEffect recipe = entry.effect().getRecipe();
            boolean discovered = recipe != null && known.contains(recipe);

            if (cauldron.isDormant(entry.effect())) {
                drawThread(graphics, centre.roundedX(), centre.roundedY(), nx, ny, thickness);
                drawDormant(graphics, entry, nx, ny, node);

                if (mouseX >= nx - half && mouseX <= nx + half && mouseY >= ny - half && mouseY <= ny + half) {
                    hoverX = toScreen(mouseX);
                    hoverY = toScreen(mouseY);
                    hoverName = discovered
                            ? Component.translatable("alchemia.gui.dormant.named", entry.effect().getEffect().value().getDisplayName())
                            : Component.translatable("alchemia.gui.dormant");
                    hoverDetail = dormantHint(recipe);
                }
                continue;
            }

            drawEffectAura(graphics, nx, ny, entry, discovered);
            drawHeldBack(graphics, entry, nx, ny);
            drawThread(graphics, centre.roundedX(), centre.roundedY(), nx, ny, thickness);

            if (discovered) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, recipe.sprite(),
                        nx - half, ny - half, node, node, -1);
            } else if (recipe != null && WorldEffects.anyVisible(notes, recipe)) {
                drawOpenEye(graphics, nx, ny, node);
            } else {
                drawUnknownSymbol(graphics, nx, ny, node);
            }

            if (discovered && zoom >= LABEL_ZOOM) {
                Component name = entry.effect().getEffect().value().getDisplayName();
                graphics.text(this.font, name, nx - this.font.width(name) / 2, ny + half + 2, INK, false);
            }

            if (mouseX >= nx - half && mouseX <= nx + half && mouseY >= ny - half && mouseY <= ny + half) {
                hoverX = toScreen(mouseX);
                hoverY = toScreen(mouseY);
                int cap = cauldron.levelCap(recipe);
                Component held = cap <= entry.effect().getStrength() && WorldEffects.tierVisible(notes, recipe, cap + 1)
                        ? WorldEffects.levelHint(recipe, cap + 1) : null;

                if (discovered) {
                    hoverName = entry.effect().getEffect().value().getDisplayName();
                    hoverDetail = held != null ? held : Component.translatable("alchemia.gui.potency",
                            entry.effect().getStrength() + 1, entry.effect().getDuration() / 20);
                } else {
                    hoverName = Component.translatable("alchemia.gui.unknown");
                    hoverDetail = held != null ? held : Component.translatable("alchemia.gui.unknown.hint");
                }
            }
        }

        drawLureFields(graphics);

        PathRenderer.marker(graphics, centre.x(), centre.y(), Math.max(4, node / 2), GHOST_RGB);

        extractPlan(graphics, cauldron, node);
        extractPending(graphics, cauldron, node);
        extractFlight(graphics);

        double[] state = markerState();
        double markerX = screenX(state[0]);
        double markerY = screenY(state[1]);
        double markerScale = state[2];
        double markerSpin = state[3];

        if (markerX >= left && markerX <= right && markerY >= top && markerY <= bottom) {
            drawBrewMarker(graphics, (int) Math.round(markerX), (int) Math.round(markerY), cauldron, node, markerScale, markerSpin);
        }

        graphics.disableScissor();
    }

    private PotionMap.RouteLeg remainder(PotionMap.RouteLeg leg) {
        PotionMap.PotionPath path = leg.path();
        if (path.teleport() || path.stalled() || !shownCourse.started()) return leg;

        return new PotionMap.RouteLeg(leg.stack(), new PotionMap.PotionPath(
                PathGeometry.tail(path.points(), shownCourse.progress()), false, false, path.dead()),
                leg.curve(), leg.lure());
    }

    private PathPoint drawLeg(GuiGraphicsExtractor graphics, PotionMap.RouteLeg leg, PathPoint from,
                              int rgb, int alpha, double phase) {
        PotionMap.PotionPath path = leg.path();

        if (!path.teleport()) {
            if (path.points().size() < 2) return from;

            List<PathPoint> screen = toScreen(path.points());
            PathRenderer.dashes(graphics, screen, PATH_THICKNESS, ARGB.color(alpha, rgb), false, phase, viewClip(), pathScale());
            return screen.get(screen.size() - 1);
        }

        PathPoint tip = from;

        if (leg.curve().size() > 1) {
            List<PathPoint> flight = toScreen(leg.curve());
            PathRenderer.dashes(graphics, flight, PATH_THICKNESS, ARGB.color(alpha, rgb), true, 0.0, viewClip(), pathScale());
            tip = flight.get(flight.size() - 1);
        }

        PathPoint landing = toScreen(path.end(0, 0));
        PathRenderer.dashes(graphics, List.of(tip, landing), PATH_THICKNESS, ARGB.color(alpha, rgb), true, 0.0,
                viewClip(), pathScale());
        return landing;
    }

    private void extractPlan(GuiGraphicsExtractor graphics, TileEntityAlchemyCauldron cauldron, int node) {
        List<PotionMap.RouteLeg> legs = shownCourse.legs(map());
        int first = shownCourse.spent();
        if (first >= legs.size()) return;

        PathPoint at = brewAt;
        PathPoint tip = toScreen(at);

        boolean stalled = false;
        boolean doomed = false;
        boolean stranded = false;
        int simulatedInstability = cauldron.getInstability();
        List<LureReach.Span> lures = activeLures();
        LureReach.Span pendingLure = pendingLure();

        for (int index = first; index < legs.size(); index++) {
            PotionMap.RouteLeg leg = index == first ? remainder(legs.get(index)) : legs.get(index);
            PotionMap.PotionPath path = leg.path();

            if (path.stalled()) {
                stalled = true;
                if (path.teleport()) drawOffMapAttempt(graphics, at.x(), at.y(), leg.stack(), node);
                continue;
            }

            int rgb = PATH_INK_RGB;
            for (LureReach.Span span : lures) {
                if (span.bends(index)) rgb = mixRgb(PATH_INK_RGB, lureTint(span.target()), LURE_INK_MIX);
            }
            int alpha = pendingLure != null && pendingLure.bends(index) ? LURE_GHOST_ALPHA : PATH_ALPHA;

            tip = drawLeg(graphics, leg, tip, rgb, alpha,
                    index == first ? shownCourse.progress() * zoom : 0.0);
            at = path.end(at.x(), at.y());

            if (!path.dead()) continue;

            simulatedInstability++;
            if (simulatedInstability >= cauldron.getInstabilityCap()) {
                doomed = true;
                break;
            }

            drawPlanMarker(graphics, tip, node, BLOCKED_RGB);
            stranded = true;
        }

        int half = Math.max(3, node / 3);
        drawPlanMarker(graphics, tip, node, stalled || doomed || stranded ? BLOCKED_RGB : GHOST_RGB);

        if (doomed) {
            graphics.item(new ItemStack(Items.SKELETON_SKULL), tip.roundedX() - 8, tip.roundedY() - 8);
            drawPlanWarning(graphics, Component.translatable("alchemia.gui.doomed"), tip, half);
            return;
        }

        if (stranded) {
            graphics.item(new ItemStack(Items.SKELETON_SKULL), tip.roundedX() - 8, tip.roundedY() - 8);
            drawPlanWarning(graphics, Component.translatable("alchemia.gui.stranded",
                    simulatedInstability, cauldron.getInstabilityCap()), tip, half);
        }
    }

    private void drawPlanWarning(GuiGraphicsExtractor graphics, Component warning, PathPoint at, int half) {
        drawMapLabel(graphics, warning, at, half, ARGB.opaque(BLOCKED_RGB));
    }

    private void drawMapLabel(GuiGraphicsExtractor graphics, Component label, PathPoint at, int half, int colour) {
        int width = this.font.width(label);
        int labelX = Mth.clamp(at.roundedX(), viewLeft() + width / 2 + 2, viewLeft() + VIEW_WIDTH - width / 2 - 2);
        graphics.text(this.font, label, labelX - width / 2, at.roundedY() + half + 3, colour, false);
    }

    private Object lureKey;
    private long lureSince;
    private Object lureTrialKey;
    private BrewCourse lureTrial;
    private LureReach.Span lureTrialSpan;

    private LureReach.@Nullable Span pendingLure() {
        ItemStack pending = activeIngredient();
        if (!PotionMap.isReady() || !VanillaIngredients.isLure(pending)) return null;

        List<Object> key = List.of(pending.getItem(), ItemIngredient.crushOf(pending), shownCourse.entries(),
                shownCourse.anchors(), shownCourse.spent(), shownCourse.started(), map());
        if (!key.equals(lureTrialKey)) {
            lureTrialKey = key;
            lureTrial = shownCourse.copy();
            lureTrial.add(pending);
            lureTrialSpan = LureReach.pending(shownCourse, map(), pending);
        }
        return lureTrialSpan;
    }

    private List<LureReach.Span> activeLures() {
        if (!PotionMap.isReady()) return List.of();

        List<LureReach.Span> active = new ArrayList<>();
        for (LureReach.Span span : LureReach.spans(shownCourse.entries(), shownCourse.anchors(), shownCourse.anchorFrom(),
                shownCourse.legs(map()))) {
            if (span.lure() >= shownCourse.spent()) active.add(span);
        }
        return active;
    }

    private int lureTint(AlchemiaEffect target) {
        return known.contains(target) ? target.color() : AURA_UNKNOWN_RGB;
    }

    private static int mixRgb(int from, int to, double amount) {
        int r = (int) Math.round(((from >> 16) & 0xFF) * (1 - amount) + ((to >> 16) & 0xFF) * amount);
        int g = (int) Math.round(((from >> 8) & 0xFF) * (1 - amount) + ((to >> 8) & 0xFF) * amount);
        int b = (int) Math.round((from & 0xFF) * (1 - amount) + (to & 0xFF) * amount);
        return (r << 16) | (g << 8) | b;
    }

    private static PathPoint lerp(PathPoint from, PathPoint to, double amount) {
        return from.plus(to.minus(from).scale(amount));
    }

    private static double breath(long millis) {
        return (Math.sin(millis / 420.0) + 1.0) * 0.5;
    }

    private record LureField(double strength, boolean pending) {
    }

    private void drawLureFields(GuiGraphicsExtractor graphics) {
        Map<AlchemiaEffect, LureField> fields = new java.util.LinkedHashMap<>();
        for (LureReach.Span span : activeLures()) {
            if (Math.max(span.from(), shownCourse.spent()) >= span.lure()) continue;
            fields.merge(span.target(), new LureField(span.strength(), false),
                    (a, b) -> new LureField(Math.max(a.strength(), b.strength()), a.pending() || b.pending()));
        }

        LureReach.Span pending = pendingLure();
        if (pending != null) {
            fields.merge(pending.target(), new LureField(pending.strength(), true),
                    (a, b) -> new LureField(Math.max(a.strength(), b.strength()), true));
        }

        fields.forEach((target, field) -> drawLureField(graphics, target, field.strength(),
                field.pending() ? LURE_FIELD_ALPHA : LURE_QUEUED_ALPHA, field.pending()));
    }

    private void drawLureField(GuiGraphicsExtractor graphics, AlchemiaEffect target, double strength, int alpha, boolean pointer) {
        PathPoint goal = map().positionOf(target);
        if (goal == null) return;

        PathPoint centre = toScreen(goal);
        int tint = lureTint(target);
        double inner = PotionMap.EFFECT_RADIUS * zoom;
        double outer = (PotionMap.EFFECT_RADIUS + PotionMap.LURE_PULL * strength) * zoom;

        int left = viewLeft();
        int top = viewTop();
        int right = left + VIEW_WIDTH;
        int bottom = top + VIEW_HEIGHT;

        boolean onView = centre.x() >= left && centre.x() <= right && centre.y() >= top && centre.y() <= bottom;
        boolean touchesView = centre.x() + outer >= left && centre.x() - outer <= right
                && centre.y() + outer >= top && centre.y() - outer <= bottom;

        if (touchesView && outer >= 2.0) {
            long millis = Util.getMillis();
            PathRenderer.ring(graphics, centre.x(), centre.y(), outer, ARGB.color(alpha / 3, tint), true);

            for (int ring = 0; ring < LURE_FIELD_RINGS; ring++) {
                double phase = ((millis % LURE_FIELD_MS) / (double) LURE_FIELD_MS + ring / (double) LURE_FIELD_RINGS) % 1.0;
                double radius = outer - (outer - inner) * phase;
                int fade = (int) Math.round(alpha * Math.sin(Math.PI * phase));
                if (fade > 0) PathRenderer.ring(graphics, centre.x(), centre.y(), radius, ARGB.color(fade, tint), false);
            }
        }

        if (pointer && !onView) drawOffViewPointer(graphics, centre, tint);
    }

    private void drawOffViewPointer(GuiGraphicsExtractor graphics, PathPoint target, int tint) {
        double cx = viewLeft() + VIEW_WIDTH / 2.0;
        double cy = viewTop() + VIEW_HEIGHT / 2.0;
        double dx = target.x() - cx;
        double dy = target.y() - cy;
        if (Math.abs(dx) < 1.0E-6 && Math.abs(dy) < 1.0E-6) return;

        double halfW = VIEW_WIDTH / 2.0 - POINTER_INSET;
        double halfH = VIEW_HEIGHT / 2.0 - POINTER_INSET;
        double scale = Math.min(halfW / Math.max(Math.abs(dx), 1.0E-6), halfH / Math.max(Math.abs(dy), 1.0E-6));
        double angle = Math.atan2(dy, dx);
        double nudge = breath(Util.getMillis()) * 2.0;

        double x = cx + dx * scale - Math.cos(angle) * nudge;
        double y = cy + dy * scale - Math.sin(angle) * nudge;
        int colour = ARGB.color(230, mixRgb(PATH_INK_RGB, tint, 0.6));

        PathRenderer.chevron(graphics, x, y, angle, POINTER_SIZE, 1.5, colour);
        PathRenderer.chevron(graphics, x - Math.cos(angle) * 4, y - Math.sin(angle) * 4, angle, POINTER_SIZE, 1.0,
                ARGB.color(140, mixRgb(PATH_INK_RGB, tint, 0.6)));
    }

    private void drawPendingLure(GuiGraphicsExtractor graphics, ItemStack pending, int node, long millis) {
        LureReach.Span span = pendingLure();
        if (span == null) return;

        List<Object> key = List.of(pending.getItem(), shownCourse.entries().size(), shownCourse.spent(), shownCourse.started());
        if (!key.equals(lureKey)) {
            lureKey = key;
            lureSince = millis;
        }

        double bend = PathGeometry.ease(Math.min(1.0, (millis - lureSince) / (double) LURE_BEND_MS));
        int tint = lureTint(span.target());
        int ink = mixRgb(PATH_INK_RGB, tint, LURE_INK_MIX);
        int tetherAlpha = (int) Math.round(TETHER_ALPHA * Math.min(1.0, bend * 1.5) * (0.6 + 0.4 * breath(millis)));

        List<PotionMap.RouteLeg> before = shownCourse.legs(map());
        List<PotionMap.RouteLeg> after = lureTrial.legs(map());
        PathPoint tip = null;

        for (int index = Math.max(span.from(), shownCourse.spent()); index < span.lure(); index++) {
            PotionMap.RouteLeg plain = before.get(index);
            PotionMap.RouteLeg bent = after.get(index);
            boolean teleport = bent.path().teleport();
            boolean flight = teleport && bent.curve().size() > 1 && plain.curve().size() > 1;

            List<PathPoint> from = flight ? plain.curve() : plain.path().points();
            List<PathPoint> to = flight ? bent.curve() : bent.path().points();
            if (!teleport && index == shownCourse.spent() && shownCourse.started()) {
                from = PathGeometry.tail(from, shownCourse.progress());
                to = PathGeometry.tail(to, shownCourse.progress());
            }
            if (to.isEmpty()) continue;

            List<PathPoint> moving = new ArrayList<>(to.size());
            for (int step = 0; step < to.size(); step++) {
                PathPoint origin = from.isEmpty() ? to.get(step) : from.get(Math.min(step, from.size() - 1));
                moving.add(lerp(origin, to.get(step), bend));
            }

            drawTethers(graphics, from, moving, ARGB.color(tetherAlpha, tint));

            List<PathPoint> screen = toScreen(moving);
            if (screen.size() > 1) {
                PathRenderer.dashes(graphics, screen, PATH_THICKNESS, ARGB.color(PATH_ALPHA, ink), teleport,
                        0.0, viewClip(), pathScale());
            }
            tip = screen.get(screen.size() - 1);

            if (flight) {
                PathPoint landing = toScreen(lerp(plain.path().end(), bent.path().end(), bend));
                if (tip.distanceTo(landing) > 0.5) {
                    PathRenderer.dashes(graphics, List.of(tip, landing), PATH_THICKNESS, ARGB.color(PATH_ALPHA, ink), true,
                            0.0, viewClip(), pathScale());
                }
                tip = landing;
            }
        }

        if (tip == null) return;

        PathRenderer.ending(graphics, tip.x(), tip.y(), Math.max(3, node / 3.0), 1, ARGB.color(235, PENDING_RGB), false);
    }

    private void drawTethers(GuiGraphicsExtractor graphics, List<PathPoint> from, List<PathPoint> to, int argb) {
        if (from.isEmpty() || ARGB.alpha(argb) <= 0) return;

        double travelled = 0.0;
        double next = TETHER_SPACING / 2.0;

        for (int step = 0; step < to.size(); step++) {
            PathPoint origin = from.get(Math.min(step, from.size() - 1));
            if (step > 0) travelled += origin.distanceTo(from.get(Math.min(step - 1, from.size() - 1)));
            if (travelled < next) continue;
            next += TETHER_SPACING;

            PathPoint a = toScreen(origin);
            PathPoint b = toScreen(to.get(step));
            if (a.distanceTo(b) < TETHER_MIN_PIXELS) continue;

            PathRenderer.line(graphics, a.x(), a.y(), b.x(), b.y(), 1.0, argb, true);
        }
    }

    private record Bracket(int first, int last, int argb, int tint) {
    }

    private int bracketInk(AlchemiaEffect target, int alpha) {
        return ARGB.color(alpha, mixRgb(PATH_INK_RGB, lureTint(target), LURE_BRACKET_MIX));
    }

    private void drawLureBrackets(GuiGraphicsExtractor graphics, int gridX, int gridY, int queuedCount, int hoveredCell) {
        if (!PotionMap.isReady()) return;

        int spent = shownCourse.spent();
        int capacity = QUEUE_COLUMNS * QUEUE_ROWS;
        List<Bracket> brackets = new ArrayList<>();

        for (LureReach.Span span : activeLures()) {
            int lure = span.lure() - spent;
            if (lure >= capacity) continue;
            brackets.add(new Bracket(Math.max(span.from(), spent) - spent, lure,
                    bracketInk(span.target(), LURE_BRACKET_ALPHA), lureTint(span.target())));
        }

        LureReach.Span pending = pendingLure();
        if (pending != null) {
            int lure = pending.lure() - spent;
            if (lure >= queuedCount && lure < capacity) {
                int x = gridX + (lure % QUEUE_COLUMNS) * QUEUE_CELL;
                int y = gridY + (lure / QUEUE_COLUMNS) * QUEUE_CELL;
                graphics.fill(x + 1, y + 1, x + QUEUE_CELL - 1, y + QUEUE_CELL - 1,
                        ARGB.color(LURE_CELL_ALPHA, lureTint(pending.target())));

                int alpha = (int) Math.round(LURE_PREVIEW_ALPHA * (0.7 + 0.3 * breath(Util.getMillis())));
                brackets.add(new Bracket(Math.max(pending.from(), spent) - spent, lure,
                        bracketInk(pending.target(), alpha), lureTint(pending.target())));
            }
        }

        for (Bracket bracket : brackets) {
            if (bracket.last() != hoveredCell) continue;
            for (int cell = Math.max(0, bracket.first()); cell < bracket.last(); cell++) {
                int x = gridX + (cell % QUEUE_COLUMNS) * QUEUE_CELL;
                int y = gridY + (cell / QUEUE_COLUMNS) * QUEUE_CELL;
                graphics.fill(x + 1, y + 1, x + QUEUE_CELL - 1, y + QUEUE_CELL - 1, ARGB.color(LURE_FOCUS_ALPHA, bracket.tint()));
            }
        }

        for (Bracket bracket : brackets) bracket(graphics, gridX, gridY, bracket.first(), bracket.last(), bracket.argb());
    }

    private void bracket(GuiGraphicsExtractor graphics, int gridX, int gridY, int first, int last, int argb) {
        first = Math.max(0, Math.min(first, last));

        for (int cell = first; cell <= last; cell++) {
            int x = gridX + (cell % QUEUE_COLUMNS) * QUEUE_CELL;
            int y = gridY + (cell / QUEUE_COLUMNS) * QUEUE_CELL;
            int x0 = cell == first ? x + 1 : x;
            int x1 = cell == last ? x + QUEUE_CELL - 1 : x + QUEUE_CELL;
            int base = y + QUEUE_CELL - 1;

            graphics.fill(x0, base, x1, base + 1, argb);
            if (cell == first) graphics.fill(x0, base - 3, x0 + 1, base, argb);
            if (cell == last) graphics.fill(x1 - 1, base - 3, x1, base, argb);
        }
    }

    private List<Component> queueTooltip(int index, ItemStack stack) {
        String key = shownCourse.canRemove(index) ? "alchemia.gui.queue.entry"
                : shownCourse.bentByLure(index) ? "alchemia.gui.queue.bent" : "alchemia.gui.queue.stirring";
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable(key, index + 1, stack.getHoverName()));

        int entry = shownCourse.spent() + index;
        for (LureReach.Span span : activeLures()) {
            Component scent = span.target().displayName();
            if (span.lure() == entry) {
                int bends = Math.max(0, span.lure() - Math.max(span.from(), shownCourse.spent()));
                lines.add(Component.translatable(bends > 0 ? "alchemia.gui.queue.lure" : "alchemia.gui.queue.lure.idle",
                        scent, bends).withStyle(ChatFormatting.GRAY));
                if (span.stacked() > 0 && bends > 0) {
                    lines.add(Component.translatable("alchemia.gui.queue.lure.stacked", span.stacked())
                            .withStyle(ChatFormatting.GRAY));
                }
                if (span.strength() > 1.0 + 1.0E-6 && bends > 0) {
                    lines.add(Component.translatable("alchemia.gui.queue.lure.strength",
                            String.format(java.util.Locale.ROOT, "%.1f", span.strength())).withStyle(ChatFormatting.GRAY));
                }
            } else if (span.bends(entry)) {
                lines.add(Component.translatable("alchemia.gui.queue.leaning", scent,
                        shownCourse.entries().get(span.lure()).getHoverName()).withStyle(ChatFormatting.GRAY));
            }
        }
        return lines;
    }

    private final PathAnimator pendingFull = new PathAnimator();
    private final PathAnimator pendingLive = new PathAnimator();

    private Object planKey;
    private List<PathPoint> planCommitted = List.of();
    private List<PathPoint> planPotential = List.of();
    private PathPoint planStart = PathPoint.ORIGIN;
    private boolean planTeleport;
    private boolean planDoomed;

    private double pathScale() {
        return PATH_BASE_SCALE * Mth.clamp(zoom, MIN_PATH_SCALE, MAX_ZOOM);
    }

    private PathRenderer.Clip viewClip() {
        return new PathRenderer.Clip(viewLeft(), viewTop(), viewLeft() + VIEW_WIDTH, viewTop() + VIEW_HEIGHT);
    }

    private List<PathPoint> flightOrHop(PotionMap.PotionPath path, double fromX, double fromY, ItemStack stack) {
        PathPoint landing = path.end(fromX, fromY);
        List<PathPoint> flight = map().teleportFlight(fromX, fromY, stack);

        if (path.points().size() > 1 && flight.size() > 1
                && flight.get(flight.size() - 1).distanceTo(landing) < 1.0) return flight;
        return List.of(path.start(), landing);
    }

    private List<PathPoint> potentialPath(ItemStack stack, double x, double y) {
        VanillaIngredients.Drift drift = VanillaIngredients.driftOf(stack);
        if (drift != null && !drift.lure()) {
            return map().project(x, y, map().fixedDriftOffsets(drift.target(), Ingredients.MAX_CRUSH_UNITS), false).points();
        }
        if (!(stack.getItem() instanceof ItemIngredient item)) return map().walk(x, y, stack).points();

        double maxPotency = item.getIngredient().getPotency(Ingredients.MAX_CRUSH);
        List<PathPoint> offsets = item.previewPath(maxPotency,
                ItemIngredient.trimOf(stack), ItemIngredient.rotationOf(stack));

        return map().project(x, y, offsets, IngredientPath.of(item.getIngredient()).teleports()).points();
    }

    private void extractPending(GuiGraphicsExtractor graphics, TileEntityAlchemyCauldron cauldron, int node) {
        ItemStack pending = activeIngredient();

        if (!TileEntityAlchemyCauldron.isBrewingInput(pending) || !PotionMap.isReady()) {
            pendingFull.forget();
            pendingLive.forget();
            planKey = null;
            return;
        }

        long millis = Util.getMillis();
        List<ItemStack> queued = queuedIngredients();

        if (VanillaIngredients.isLure(pending)) {
            pendingFull.forget();
            pendingLive.forget();
            planKey = null;
            drawPendingLure(graphics, pending, node, millis);
            return;
        }
        lureKey = null;

        List<Object> signature = List.of(pending.getItem(), ItemIngredient.crushOf(pending),
                ItemIngredient.trimOf(pending), ItemIngredient.rotationOf(pending), queued.size());

        PathPoint from = shownCourse.end(map());
        List<Object> key = List.of(signature, from, map());

        if (!key.equals(planKey)) {
            PotionMap.PotionPath path = map().walk(from.x(), from.y(), pending);

            planKey = key;
            planStart = from;
            planTeleport = path.teleport();
            planDoomed = path.dead();
            planCommitted = List.copyOf(path.teleport()
                    ? flightOrHop(path, from.x(), from.y(), pending)
                    : path.points());
            planPotential = List.copyOf(potentialPath(pending, from.x(), from.y()));
        }

        PathPoint start = planStart;
        boolean teleport = planTeleport;
        boolean doomed = planDoomed;
        List<PathPoint> committed = planCommitted;
        List<PathPoint> potential = planPotential;

        List<PathPoint> full = toScreen(pendingFull.update(signature, potential, millis));
        List<PathPoint> live = toScreen(pendingLive.update(signature, committed, millis));

        PathRenderer.Clip clip = viewClip();

        if (full.size() > 1) {
            double walked = PathGeometry.length(live);
            PathRenderer.outline(graphics, PathGeometry.tail(full, walked), PATH_THICKNESS,
                    ARGB.color(PATH_ALPHA, PATH_INK_RGB), teleport, walked, clip, pathScale());
            drawPlanMarker(graphics, full.get(full.size() - 1), node, PENDING_RGB);
        }

        if (live.size() > 1) {
            PathRenderer.dashes(graphics, live, PATH_THICKNESS, ARGB.color(PATH_ALPHA, PATH_INK_RGB), teleport, 0.0,
                    clip, pathScale());
        }

        PathPoint landing = live.isEmpty() ? toScreen(start) : live.get(live.size() - 1);
        PathRenderer.ending(graphics, landing.x(), landing.y(), Math.max(3, node / 3.0), 1,
                ARGB.color(235, doomed ? BLOCKED_RGB : PENDING_RGB), teleport);
    }

    private void drawOffMapAttempt(GuiGraphicsExtractor graphics, double x, double y, ItemStack stack, int node) {
        List<PathPoint> curve = toScreen(map().teleportCurve(x, y, stack));
        if (curve.size() < 2) return;

        PathRenderer.dashes(graphics, curve, PATH_THICKNESS, ARGB.color(PATH_ALPHA, BLOCKED_RGB), true, 0.0,
                viewClip(), pathScale());

        PathPoint tip = curve.get(curve.size() - 1);
        int half = Math.max(3, node / 3);

        PathRenderer.spiral(graphics, tip.x(), tip.y(), half, 2, ARGB.color(220, BLOCKED_RGB));
        drawPlanWarning(graphics, Component.translatable("alchemia.gui.offmap"), tip, half);
    }

    private void drawPlanMarker(GuiGraphicsExtractor graphics, PathPoint at, int node, int rgb) {
        PathRenderer.marker(graphics, at.x(), at.y(), Math.max(3, node / 3), rgb);
    }

    private void drawMapEdge(GuiGraphicsExtractor graphics, int left, int top, int right, int bottom) {
        PotionMap map = map();
        int width = right - left;
        int height = bottom - top;
        if (width <= 0 || height <= 0) return;

        boolean[] outside = new boolean[width * height];
        double step = 1.0 / zoom;
        for (int row = 0; row < height; row++) {
            double mapY = mapAtY(top + row + 0.5);
            double mapX = mapAtX(left + 0.5);
            for (int column = 0; column < width; column++, mapX += step) {
                outside[row * width + column] = !map.inBounds(mapX, mapY);
            }
        }

        boolean[] near = new boolean[width * height];
        int[] count = new int[Math.max(width, height) + 1];
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                count[column + 1] = count[column] + (outside[row * width + column] ? 0 : 1);
            }
            for (int column = 0; column < width; column++) {
                int from = Math.max(0, column - EDGE_BORDER_WIDTH);
                int to = Math.min(width, column + EDGE_BORDER_WIDTH + 1);
                near[row * width + column] = count[to] - count[from] > 0;
            }
        }

        boolean[] border = new boolean[width * height];
        for (int column = 0; column < width; column++) {
            for (int row = 0; row < height; row++) count[row + 1] = count[row] + (near[row * width + column] ? 1 : 0);
            for (int row = 0; row < height; row++) {
                int from = Math.max(0, row - EDGE_BORDER_WIDTH);
                int to = Math.min(height, row + EDGE_BORDER_WIDTH + 1);
                border[row * width + column] = outside[row * width + column] && count[to] - count[from] > 0;
            }
        }

        boolean[] wash = new boolean[width];
        boolean[] rim = new boolean[width];
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                int index = row * width + column;
                rim[column] = border[index];
                wash[column] = outside[index] && !border[index];
            }
            spans(graphics, wash, left, top + row, EDGE_FILL);
            spans(graphics, rim, left, top + row, EDGE_BORDER);
        }
    }

    private static final double MIN_TILE_PIXELS = 3.0;
    private static final double EDGE_TILE_PIXELS = 5.0;

    private int deadzoneLevel(PotionMap map) {
        int level = 0;
        while (level + 1 < map.lodLevels() && PotionMap.tileSize(level) * zoom < MIN_TILE_PIXELS) level++;
        return level;
    }

    private static final double EDGE_ZOOM = 0.45;

    private void extractDeadzones(GuiGraphicsExtractor graphics, int left, int top, int right, int bottom) {
        if (!PotionMap.isReady()) return;

        PotionMap map = map();
        int level = deadzoneLevel(map);
        int width = right - left;
        if (width <= 0) return;

        int fill = ARGB.color(70, BLOCKED_RGB);
        int edge = ARGB.color(150, BLOCKED_RGB);
        boolean edged = zoom >= EDGE_ZOOM;

        boolean[] above = new boolean[width];
        boolean[] row = new boolean[width];
        boolean[] rim = new boolean[width];
        boolean[] rimAbove = new boolean[width];
        double step = 1.0 / zoom;

        for (int y = top; y < bottom; y++) {
            double mapY = mapAtY(y + 0.5);
            double mapX = mapAtX(left + 0.5);

            for (int i = 0; i < width; i++, mapX += step) {
                row[i] = map.deadAt(level, mapX, mapY);
            }

            spans(graphics, row, left, y, fill);

            if (edged) {
                for (int i = 0; i < width; i++) {
                    boolean open = i == 0 || i == width - 1 || !row[i - 1] || !row[i + 1] || !above[i];
                    rim[i] = row[i] && open;
                    rimAbove[i] = !row[i] && above[i];
                }

                spans(graphics, rim, left, y, edge);
                spans(graphics, rimAbove, left, y - 1, edge);
            }

            System.arraycopy(row, 0, above, 0, width);
        }
    }

    private void spans(GuiGraphicsExtractor graphics, boolean[] row, int left, int y, int colour) {
        int start = -1;

        for (int i = 0; i <= row.length; i++) {
            boolean inside = i < row.length && row[i];

            if (inside && start < 0) start = i;
            if (inside || start < 0) continue;

            graphics.fill(left + start, y, left + i, y + 1, colour);
            start = -1;
        }
    }

    private void drawBrewMarker(GuiGraphicsExtractor graphics, int x, int y, TileEntityAlchemyCauldron cauldron, int node, double scale, double spin) {
        if (scale <= 0.01) return;

        double pulse = (Math.sin(Util.getMillis() / 380.0) + 1.0) * 0.5;
        int radius = (int) Math.round(node * 0.55 + pulse * 3.0);
        int alpha = (int) Math.round(70 + pulse * 90);

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        pose.rotate((float) spin);
        pose.scale((float) scale, (float) scale);

        graphics.outline(-radius, -radius, radius * 2, radius * 2, ARGB.color(alpha, 0x2A1F14));
        graphics.outline(-radius - 1, -radius - 1, radius * 2 + 2, radius * 2 + 2, ARGB.color(alpha / 3, 0x2A1F14));
        ItemStack marker = new ItemStack(cauldron.isSplash() || splashPending(cauldron) ? Items.SPLASH_POTION : Items.POTION);
        marker.set(DataComponents.POTION_CONTENTS,
                new PotionContents(Optional.empty(), Optional.of(cauldron.getPotionColor()), List.of(), Optional.empty()));
        if (cauldron.isPrimed()) {
            marker.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            int glow = (int) Math.round(170 + 55 * Math.sin(Util.getMillis() / 260.0));
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, AlchemiaSprites.RADIANT_HALO, -16, -16, 32, 32, ARGB.color(glow, 0xFFFFFF));
        }
        graphics.item(marker, -8, -8);

        pose.popMatrix();
    }

    private boolean splashPending(TileEntityAlchemyCauldron cauldron) {
        return BrewModifiers.of(activeIngredient()) == BrewModifiers.GUNPOWDER
                && cauldron.modifierRefusal(BrewModifiers.GUNPOWDER) == null;
    }

    private void drawEffectAura(GuiGraphicsExtractor graphics, int cx, int cy,
                                PotionMap.MapEntry entry, boolean discovered) {
        double reach = PotionMap.EFFECT_RADIUS * zoom;
        if (reach < 2.0) return;

        if (cx + reach < viewLeft() || cx - reach > viewLeft() + VIEW_WIDTH
                || cy + reach < viewTop() || cy - reach > viewTop() + VIEW_HEIGHT) return;

        int rgb = discovered ? entry.effect().getEffect().value().getColor() : AURA_UNKNOWN_RGB;
        double pulse = (Math.sin(Util.getMillis() / 520.0 + cx * 0.35) + 1.0) * 0.5;
        int alpha = (int) Math.round(AURA_CORE_ALPHA * (0.72 + pulse * 0.28));

        PathRenderer.aura(graphics, cx, cy, reach, rgb, alpha);
        PathRenderer.ring(graphics, cx, cy, reach, ARGB.color(Math.min(255, alpha * 2), rgb), true);

        double core = PotionMap.EFFECT_CORE * zoom;
        if (core >= 2.0) PathRenderer.ring(graphics, cx, cy, core, ARGB.color(Math.min(255, alpha * 3), rgb), false);
    }

    private void drawThread(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, int thickness) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length < 1) return;

        int steps = (int) Math.min(72, Math.ceil(length / 3.0));
        if (steps <= 0) return;

        for (int i = 1; i <= steps; i++) {
            double t = (double) i / steps;
            int px = (int) Math.round(x0 + dx * t);
            int py = (int) Math.round(y0 + dy * t);
            int alpha = (int) Math.round(12 + t * 120);
            graphics.fill(px - thickness / 2, py - thickness / 2,
                    px - thickness / 2 + thickness, py - thickness / 2 + thickness,
                    ARGB.color(alpha, LINE_RGB));
        }
    }
}
