package muddykat.alchemia.client.gui;

import net.minecraft.world.item.Item;
import muddykat.alchemia.registration.registers.ItemRegistry;
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
import muddykat.alchemia.common.potion.BrewBase;
import muddykat.alchemia.common.potion.BrewCourse;
import muddykat.alchemia.common.potion.CauldronHeat;
import muddykat.alchemia.common.potion.MapReveals;
import muddykat.alchemia.common.potion.WhirlNotes;
import muddykat.alchemia.common.potion.Whirlpool;
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
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.neoforged.neoforge.client.ClientHooks;
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
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.PotionContents;

import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class AlchemicalScreen extends AbstractContainerScreen<AlchemicalCauldronMenu> {
    private static final int FOLIO_W = CauldronArt.FOLIO_W;
    private static final int FOLIO_H = CauldronArt.FOLIO_H;
    private static final int LEDGER_SHEET_W = 110;
    private static final int LEDGER_W = ScrollWidgetRenderer.outerWidth(LEDGER_SHEET_W);
    private static final int LEDGER_X = FOLIO_W + 2;
    private static final int LEDGER_Y = 10;
    private static final int LEDGER_INNER_X = LEDGER_X + ScrollWidgetRenderer.ROLL_END + 6;
    private static final int LEDGER_INNER_W = LEDGER_SHEET_W - 12;
    private static final int LEDGER_PAD = 8;
    private static final int TRAY_X = (FOLIO_W - CauldronArt.TRAY_W) / 2;
    private static final int TRAY_Y = FOLIO_H - CauldronArt.TRAY_TUCK;
    private static final int IMAGE_WIDTH = LEDGER_X + LEDGER_W;
    private static final int IMAGE_HEIGHT = TRAY_Y + CauldronArt.TRAY_H;

    private static final int VIEW_INSET_X = 20;
    private static final int VIEW_INSET_Y = 32;
    private static final int VIEW_WIDTH = 328;
    private static final int VIEW_HEIGHT = 146;
    private static final int HEADER_Y = 15;
    private static final int HEADER_RIGHT = VIEW_INSET_X + VIEW_WIDTH;

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
    private static final int LINE_HEIGHT = 10;
    private static final int SKULL_PITCH = 13;
    private static final int SKULL_ICON = 12;
    private static final int READOUT_H = 50;

    private static final int PREP_Y = AlchemicalCauldronMenu.SLOT_ROW_Y;
    private static final int WORK_X = 62;
    private static final int WORK_W = 80;
    private static final int ADD_X = 146;
    private static final int ADD_W = 84;
    private static final int INFO_X = 236;
    private static final int BUTTON_H = 18;

    private static final int TIMELINE_X = 18;
    private static final int TIMELINE_Y = 206;
    private static final int STIR_X = 240;
    private static final int HEAT_X = 296;
    private static final int ACTION_W = 54;

    private static final int QUEUE_COLUMNS = TileEntityAlchemyCauldron.MAX_QUEUE;
    private static final int QUEUE_ROWS = 1;
    private static final int QUEUE_CELL = 18;

    private static final int PRESS_STIR = 0;
    private static final int PRESS_HEAT = 1;
    private static final int PRESS_ADD = 2;
    private static final int PRESS_WORK = 3;
    private static final long PRESS_MS = 140L;
    private static final long POP_MS = 320L;
    private static final long GHOST_MS = 420L;
    private static final long SLIDE_MS = 260L;
    private static final long ARRIVE_MS = 300L;
    private static final long LANDING_MS = 700L;
    private static final long READOUT_FADE_MS = 180L;
    private static final double HEAT_PUMP = CauldronHeat.PUMP;
    private static final double HEAT_COOL = CauldronHeat.COOL_PER_SECOND;
    private static final long HEAT_HOLD_MS = CauldronHeat.HOLD_MS;
    private static final double HEAT_STRAIN_GAP = CauldronHeat.STRAIN_GAP;
    private static final long HEAT_TRUST_MS = 300L;
    private static final double HEAT_STIR_BONUS = 1.5;
    private static final int WHIRL_INK = 0x17130F;
    private static final int WHIRL_FAINT = 0xFF6F6352;
    private static final int WHIRL_SHINE = 0xFFF4ECDB;
    private static final int WHIRL_LINK_ALPHA = 170;
    private static final double WHIRL_BASE_TURNS = 2.0;
    private static final int WHIRL_PAPER = 0xFFE4D6B3;
    private static final int WHIRL_FADE_CHUNKS = 12;
    private static final double WHIRL_FADE_START = 0.55;
    private static final double WHIRL_FADE_CORE = 0.15;
    private static final double WHIRL_FADE_MAX = 0.85;
    private static final double WHIRL_EXIT_TURNS = 1.75;
    private static final int FOG_TINT = 0xE4DCCD;
    private static final boolean FOG_OF_WAR_ENABLED = false;
    private static final int FOG_BLUR_PASSES = 3;
    private static final double FOG_EDGE_LOW = 0.12;
    private static final double FOG_EDGE_HIGH = 0.88;
    private static final double FOG_HIDES = 0.6;
    private static final double FOG_PIECE_PIXELS = 4.0;
    private static final long BOIL_MS = CauldronHeat.BOIL_MS;
    private static final double BOIL_REST_SHARE = CauldronHeat.BOIL_REST_SHARE;
    private static final long STRAIN_MS = 320L;

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
    private static final double GRID_BASE = PotionMap.CELL / 2.0;
    private static final double GRID_MIN_PIXELS = 8.0;
    private static final int GRID_RGB = 0x967E5C;
    private static final int GRID_ALPHA = 40;
    private static final double GRID_FADE_FROM = 0.115;
    private static final double GRID_FADE_TO = 0.12;
    private static final int PATH_THICKNESS = 2;
    private static final double MIN_PATH_SCALE = 0.5;
    private static final double PATH_BASE_SCALE = 2.0;

    private static final double MIN_ZOOM = 0.1;
    private static final double MAX_ZOOM = 3.0;
    private static final double ZOOM_STEP = 1.18;
    private static final double LABEL_ZOOM = 1.2;

    private static final int INK = CauldronArt.INK;
    private static final int INK_SOFT = CauldronArt.INK_SOFT;
    private static final int INK_FAINT = CauldronArt.INK_FAINT;
    private static final int INK_RED = CauldronArt.INK_RED;
    private static final int NAME_SELECTION = 0x803B2A1E;
    private static final int FRAME_OUTER = 0xFF967E5C;
    private static final int FRAME_INNER = 0xFF604A34;
    private static final int FRAME_SHADOW = 0x2A3B2A1E;
    private static final int TRAY_SHADOW = 0x50120E0D;
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
    private static final double NOTICE_ROLL_MS = 420.0;
    private static final long NOTICE_READ_BASE_MS = 2200L;
    private static final long NOTICE_READ_PER_CHAR_MS = 55L;
    private static final long NOTICE_HOVER_GRACE_MS = 800L;
    private static final int NOTICE_PAD = 7;
    private static final int NOTICE_MAX_H = 84;
    private static final int NOTICE_GAP = 2;
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

    private static final int NAME_X = 34;
    private static final int NAME_Y = HEADER_Y - 3;
    private static final int NAME_W = 160;
    private static final int NAME_H = 14;
    private static final int NAME_TEXT_Y = 3;

    private static final int MAX_PENDING_STIRS = 40;
    private static final double HOP_DISTANCE = PotionMap.CELL;
    private static final double MARKER_CHASE_MS = 45.0;
    private static final double FOLLOW_MARGIN = 0.3;
    private static final double CHARGE_SHRINK = 0.4;
    private static final long PUFF_MS = 1200L;
    private static final long PUFF_STAGGER_MS = 110L;
    private static final int PUFF_COUNT = 5;
    private static final double SHAKE_HEAT = 0.7;
    private static final float SHAKE_PIVOT_Y = 4.0F;
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
    private final List<Component> pendingNotices = new ArrayList<>();
    private List<Component> shownNotices = List.of();
    private boolean noticeOpen;
    private double noticeRoll;
    private long noticeClock;
    private long noticeCloseAt;
    private boolean noticeManual;
    private int noticeHitX;
    private int noticeHitY;
    private int noticeHitW;
    private int noticeHitH;
    private Set<AlchemiaEffect> knownSeen;
    private PotionMap.MapEntry glowEntry;
    private double glow;
    private Component hoverName;
    private Component hoverDetail;
    private int hoverX;
    private int hoverY;
    private @Nullable InkTooltip hoverIcon;
    private @Nullable PendingTooltip pendingTooltip;
    private ItemStack flyingStack = ItemStack.EMPTY;
    private int waterSeen = -1;
    private double heat;
    private double heatShown;
    private long heatFrame;
    private long heatHeldUntil;
    private long boiledAt;
    private long puffAt;
    private int puffRgb;
    private long strainAt;
    private long nextBubbleSound;
    private double stirCredit;
    private double whirlTurn;
    private long lastStokeAt;
    private Set<String> whirlExits = Set.of();
    private Set<String> whirlExitsSeen;
    private List<ItemStack> readableGuides = List.of();
    private @Nullable PotionMap sessionMap;
    private long[] sessionBits = new long[0];
    private @Nullable PathPoint sessionAt;
    private long @Nullable [] fogBits;
    private float @Nullable [] fogCorners;
    private int fogColumns;
    private int flyingX;
    private int flyingY;
    private @Nullable Identifier hoverSprite;

    private int pressed = -1;
    private long pressedAt = Long.MIN_VALUE;
    private List<String> potencyTokens;
    private List<PotencySlot> potencyCommitted = List.of();
    private final Map<Integer, Long> potencyBorn = new java.util.HashMap<>();
    private final List<PotencyGhost> potencyGhosts = new ArrayList<>();
    private List<ItemStack> queueShown;
    private int[] queueFrom = new int[0];
    private long queueMoved;
    private int arrival = -1;
    private long arrivalAt;
    private @Nullable Object landingKey;
    private long landingAt = Long.MIN_VALUE;
    private PathPoint landingPoint = PathPoint.ORIGIN;
    private int landingRgb;
    private double workShown;
    private @Nullable Object readoutKey;
    private long readoutAt = Long.MIN_VALUE;
    private @Nullable PathPoint readoutPos;

    private record PotencyGhost(int index, PotencySlot slot, long since) {
    }

    public AlchemicalScreen(AlchemicalCauldronMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, IMAGE_WIDTH, IMAGE_HEIGHT);
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

        this.uiScale = snappedScale();
        this.leftPos = Math.round((this.width / this.uiScale - this.imageWidth) / 2.0F);
        this.topPos = Math.round((this.height / this.uiScale - this.imageHeight) / 2.0F);

        String existing = this.getMenu().getCauldron().getBrewName();
        this.nameField = new EditBox(this.font, this.leftPos + NAME_X + 2, this.topPos + NAME_Y + 2,
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

    private float snappedScale() {
        float fit = Math.min((float) this.width / (this.imageWidth + SCREEN_MARGIN),
                (float) this.height / (this.imageHeight + SCREEN_MARGIN));
        if (fit >= 1.0F) return 1.0F;

        int guiScale = this.minecraft == null ? 1 : this.minecraft.getWindow().getGuiScale();
        int steps = (int) Math.floor(fit * guiScale + 1.0E-4);
        return steps >= 1 ? (float) steps / guiScale : fit;
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

        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && noticeHeld(guiX, guiY)) {
            toggleNotice();
            return true;
        }

        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && overHeatButton(guiX, guiY)) {
            if (canHeat()) {
                press(PRESS_HEAT);
                stoke();
            }
            return true;
        }

        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && overStirButton(guiX, guiY)) {
            if (canStir()) {
                press(PRESS_STIR);
                stirring = true;
                if (!inFlight()) sendStir(1);
            }
            return true;
        }

        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && overAddButton(guiX, guiY)) {
            if (canAdd()) {
                press(PRESS_ADD);
                sendButton(AlchemicalCauldronMenu.BUTTON_ADD);
            }
            return true;
        }

        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && overToolButton(guiX, guiY)) {
            if (canApplyTool()) {
                press(PRESS_WORK);
                applying = true;
                applyTicks = 0;
                sendButton(applyButton());
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
                stirCredit = 0.0;
            } else if (!shownCourse.teleporting(map()) && !inFlight()) {
                stirCredit += 1.0 + heatIntensity() * HEAT_STIR_BONUS;
                int units = (int) Math.floor(stirCredit);
                stirCredit -= units;
                sendStir(units);
            }
        }

        if (!applying) return;

        IngredientTool tool = heldTool();
        if (tool == null || !canApplyTool()) {
            applying = false;
            return;
        }

        applyTicks++;
        boolean fast = ClientIngredientPathTooltip.shiftDown();
        int interval = Math.max(1, tool.ticksPerStep() / (fast ? TileEntityAlchemyCauldron.FAST_TOOL_DIVISOR : 1));
        if (applyTicks % interval == 0) {
            sendButton(applyButton());
        }
    }

    private boolean overMap(double mouseX, double mouseY) {
        if (overNameField(mouseX, mouseY)) return false;
        return mouseX >= viewLeft() && mouseX < viewLeft() + VIEW_WIDTH
                && mouseY >= viewTop() && mouseY < viewTop() + VIEW_HEIGHT;
    }

    private boolean overNameField(double mouseX, double mouseY) {
        return over(mouseX, mouseY, NAME_X, NAME_Y, NAME_W, NAME_H);
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
        int water = this.getMenu().getCauldron().getWaterLevel();
        if (water > 0 && waterSeen <= 0) resetView();
        waterSeen = water;
        tickHeat();
        reconcileStir();
        trackMarker();
        tickZoom();

        refreshKnowledge();
        watchWhirls();
        watchNotes();
        watchKnown();
        watchIngredients();
        hoverName = null;
        hoverDetail = null;
        hoverIcon = null;
        hoverSprite = null;
        pendingTooltip = null;

        int guiMouseX = (int) Math.round(toGui(mouseX));
        int guiMouseY = (int) Math.round(toGui(mouseY));

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.scale(this.uiScale, this.uiScale);

        int ledgerH = ledgerHeight();
        graphics.fill(this.leftPos + TRAY_X + 2, this.topPos + TRAY_Y + 3,
                this.leftPos + TRAY_X + CauldronArt.TRAY_W + 2, this.topPos + TRAY_Y + CauldronArt.TRAY_H + 2, TRAY_SHADOW);
        CauldronArt.tray(graphics, this.leftPos + TRAY_X, this.topPos + TRAY_Y);
        CauldronArt.folio(graphics, this.leftPos, this.topPos);

        extractMap(graphics, guiMouseX, guiMouseY);
        extractMapFrame(graphics);
        extractHeader(graphics, guiMouseX, guiMouseY);
        extractPrep(graphics, guiMouseX, guiMouseY);
        extractTimeline(graphics, guiMouseX, guiMouseY);
        extractStirButton(graphics, guiMouseX, guiMouseY);
        extractHeatButton(graphics, guiMouseX, guiMouseY);
        extractLedger(graphics, guiMouseX, guiMouseY, ledgerH);
        extractNotice(graphics, ledgerH, guiMouseX, guiMouseY);

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
            whirlExits = Set.of();
            readableGuides = List.of();
            ingredientKeys = Set.of();
            deskInReach = false;
            return;
        }

        Set<AlchemiaEffect> learned = new java.util.LinkedHashSet<>();
        Set<String> noted = new java.util.HashSet<>();
        Set<String> handled = new java.util.HashSet<>();
        Set<String> exits = new java.util.HashSet<>();
        List<ItemStack> guides = GuideShelf.readable(this.minecraft.player, cauldron.getLevel(), cauldron.getBlockPos());
        for (ItemStack guide : guides) {
            learned.addAll(RecipeDiscovery.knownTo(guide));
            noted.addAll(WorldEffects.notesIn(guide));
            handled.addAll(IngredientDiscovery.stored(guide));
            exits.addAll(WhirlNotes.exitsIn(guide));
        }
        readableGuides = guides;
        known = learned;
        notes = noted;
        whirlExits = exits;
        ingredientKeys = handled;
        deskInReach = guides.size() > RecipeDiscovery.guidesOf(this.minecraft.player).size();
    }

    private void sendStir(int units) {
        if (pendingStirs.size() >= MAX_PENDING_STIRS || units <= 0) return;

        boolean fast = ClientIngredientPathTooltip.shiftDown();
        int count = Math.min(AlchemicalCauldronMenu.MAX_STIRS_PER_CLICK, units * (fast ? TileEntityAlchemyCauldron.FAST_STIRS : 1));
        sendButton(count == 1 ? AlchemicalCauldronMenu.BUTTON_STIR : AlchemicalCauldronMenu.BUTTON_STIR_MANY + count);
        pendingStirs.addLast(count);
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
            if (flightPlan.isEmpty()) flightPlan = whirlFlight(markerAt, brewAt);
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

    private boolean over(double mouseX, double mouseY, int x, int y, int w, int h) {
        int left = this.leftPos + x;
        int top = this.topPos + y;
        return mouseX >= left && mouseX < left + w && mouseY >= top && mouseY < top + h;
    }

    private boolean overStirButton(double mouseX, double mouseY) {
        return over(mouseX, mouseY, STIR_X, TIMELINE_Y, ACTION_W, BUTTON_H);
    }

    private boolean overHeatButton(double mouseX, double mouseY) {
        return over(mouseX, mouseY, HEAT_X, TIMELINE_Y, ACTION_W, BUTTON_H);
    }

    private boolean overAddButton(double mouseX, double mouseY) {
        return over(mouseX, mouseY, ADD_X, PREP_Y, ADD_W, BUTTON_H);
    }

    private boolean overToolButton(double mouseX, double mouseY) {
        return over(mouseX, mouseY, WORK_X, PREP_Y, WORK_W, BUTTON_H);
    }

    private void press(int button) {
        pressed = button;
        pressedAt = Util.getMillis();
    }

    private boolean pressedDown(int button, boolean held) {
        return held || (pressed == button && Util.getMillis() - pressedAt < PRESS_MS);
    }

    private int queueCellAt(double mouseX, double mouseY) {
        int gridX = this.leftPos + TIMELINE_X;
        int gridY = this.topPos + TIMELINE_Y;

        int column = (int) Math.floor((mouseX - gridX) / QUEUE_CELL);
        int row = (int) Math.floor((mouseY - gridY) / QUEUE_CELL);
        if (column < 0 || column >= QUEUE_COLUMNS || row < 0 || row >= QUEUE_ROWS) return -1;

        int index = row * QUEUE_COLUMNS + column;
        return index < queuedIngredients().size() ? index : -1;
    }

    private TileEntityAlchemyCauldron.@Nullable PotencyPlan brewPreview(TileEntityAlchemyCauldron cauldron) {
        if (brewCheck() != TileEntityAlchemyCauldron.BrewCheck.READY || cauldron.gainAt(brewAt) <= 0) return null;
        return cauldron.brewedWith(cauldron.effectAt(brewAt));
    }

    private record PotencySlot(String token, Component name, Holder<MobEffect> effect, boolean redstone, boolean incoming, boolean unknown) {}

    private List<PotencySlot> potencySlots(TileEntityAlchemyCauldron cauldron) {
        TileEntityAlchemyCauldron.PotencyPlan preview = brewPreview(cauldron);
        TileEntityAlchemyCauldron.PotencyPlan plan = preview == null ? cauldron.currentPotency() : preview;
        Holder<MobEffect> brewing = preview == null ? null : preview.effects().getLast().getEffect();
        return slotsOf(cauldron, plan, brewing);
    }

    private List<PotencySlot> slotsOf(TileEntityAlchemyCauldron cauldron, TileEntityAlchemyCauldron.PotencyPlan plan,
                                      @Nullable Holder<MobEffect> brewing) {
        List<PotencySlot> slots = new ArrayList<>();
        int held = brewing == null ? 0 : cauldron.heldPotency(brewing);
        int bonus = plan.redstone() * TileEntityAlchemyCauldron.REDSTONE_DURATION_BONUS;
        int charges = 0;

        for (String token : plan.order()) {
            if (token.equals(TileEntityAlchemyCauldron.REDSTONE_TOKEN)) {
                slots.add(new PotencySlot(token + "#" + charges++, Component.translatable("alchemia.gui.redstone", bonus / 20),
                        null, true, false, false));
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
                    slots.add(new PotencySlot(token + "#" + i, name, instance.getEffect(), false, fresh && i >= held, unknown));
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

    private void watchWhirls() {
        if (whirlExitsSeen == null) {
            whirlExitsSeen = new java.util.HashSet<>(whirlExits);
            return;
        }
        if (!whirlExitsSeen.containsAll(whirlExits)) queueNotice(Component.translatable("alchemia.gui.whirl.noted"));
        whirlExitsSeen = new java.util.HashSet<>(whirlExits);
    }

    private void queueNotice(Component text) {
        pendingNotices.add(text);
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

    private void extractNotice(GuiGraphicsExtractor graphics, int ledgerH, int mouseX, int mouseY) {
        long now = Util.getMillis();
        double elapsed = noticeClock == 0L ? 0.0 : Math.min(100L, now - noticeClock);
        noticeClock = now;

        if (!pendingNotices.isEmpty()) {
            if (noticeRoll > 0.0) {
                noticeOpen = false;
                noticeManual = false;
                noticeCloseAt = 0L;
            } else {
                shownNotices = List.copyOf(pendingNotices);
                pendingNotices.clear();
                noticeOpen = true;
                noticeManual = false;
                noticeCloseAt = 0L;
            }
        }
        if (shownNotices.isEmpty()) return;

        noticeRoll = Mth.clamp(noticeRoll + (noticeOpen ? 1.0 : -1.0) * elapsed / NOTICE_ROLL_MS, 0.0, 1.0);

        int sheetW = LEDGER_SHEET_W;
        int innerW = sheetW - NOTICE_PAD * 2;
        List<FormattedCharSequence> lines = new ArrayList<>();
        int characters = 0;
        for (Component notice : shownNotices) {
            lines.addAll(this.font.split(notice, innerW));
            characters += notice.getString().length();
        }
        int sheetH = Math.min(NOTICE_MAX_H, lines.size() * LINE_HEIGHT + NOTICE_PAD * 2 - 2);

        if (noticeOpen && noticeRoll >= 1.0 && noticeCloseAt == 0L && !noticeManual) {
            noticeCloseAt = now + NOTICE_READ_BASE_MS + characters * NOTICE_READ_PER_CHAR_MS;
        }
        if (noticeCloseAt != 0L && now >= noticeCloseAt && !noticeHeld(mouseX, mouseY)) {
            noticeOpen = false;
            noticeCloseAt = 0L;
        } else if (noticeCloseAt != 0L && now >= noticeCloseAt) {
            noticeCloseAt = now + NOTICE_HOVER_GRACE_MS;
        }

        double open = PathGeometry.ease(noticeRoll);
        int left = this.leftPos + LEDGER_X;
        int topY = this.topPos + LEDGER_Y + ledgerH + NOTICE_GAP;
        int sheetTop = topY + ScrollWidgetRenderer.ROLL_H - 1;
        int bottomY = topY + (int) Math.round(open * (ScrollWidgetRenderer.ROLL_H - 1 + sheetH));
        int sheetX = left + ScrollWidgetRenderer.ROLL_END;
        int rollW = ScrollWidgetRenderer.outerWidth(sheetW);

        ScrollWidgetRenderer.paperPlain(graphics, sheetX, sheetTop, sheetW, bottomY + 1, () -> {
            int lineY = sheetTop + NOTICE_PAD - 1;
            for (FormattedCharSequence line : lines) {
                graphics.text(this.font, line, sheetX + (sheetW - this.font.width(line)) / 2, lineY, INK, false);
                lineY += LINE_HEIGHT;
            }
        });

        int pullY = bottomY + ScrollWidgetRenderer.ROLL_H - ScrollWidgetRenderer.PULL_TUCK;
        noticeHitX = left;
        noticeHitY = topY;
        noticeHitW = rollW;
        noticeHitH = pullY + ScrollWidgetRenderer.PULL_H - topY;
        boolean over = noticeHeld(mouseX, mouseY);
        ScrollWidgetRenderer.pull(graphics, left + (rollW - ScrollWidgetRenderer.PULL_W) / 2, pullY + (over ? 1 : 0), over);
        ScrollWidgetRenderer.roll(graphics, left, topY, rollW);
        ScrollWidgetRenderer.roll(graphics, left, bottomY, rollW);
    }

    private boolean noticeHeld(double mouseX, double mouseY) {
        return noticeHitW > 0 && mouseX >= noticeHitX && mouseX < noticeHitX + noticeHitW
                && mouseY >= noticeHitY && mouseY < noticeHitY + noticeHitH;
    }

    private void toggleNotice() {
        if (shownNotices.isEmpty()) return;
        noticeOpen = !noticeOpen;
        noticeManual = noticeOpen;
        noticeCloseAt = 0L;
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

    private int potencyRows() {
        return Math.max(1, (TileEntityAlchemyCauldron.maxPotency() + POTENCY_PER_ROW - 1) / POTENCY_PER_ROW);
    }

    private int ledgerHeight() {
        return ScrollWidgetRenderer.outerHeight(ledgerSheetHeight());
    }

    private int ledgerSheetHeight() {
        return 5 + 13 + 10 + 13 + 12 + 10 + potencyRows() * POTENCY_CELL
                + 4 + 11 + 14 + 9 + 12 + READOUT_H + LEDGER_PAD;
    }

    private int ledgerCentre() {
        return this.leftPos + LEDGER_INNER_X + LEDGER_INNER_W / 2;
    }

    private void extractLedger(GuiGraphicsExtractor graphics, int mouseX, int mouseY, int ledgerH) {
        ScrollWidgetRenderer.openPlain(graphics, this.leftPos + LEDGER_X, this.topPos + LEDGER_Y, LEDGER_SHEET_W, ledgerSheetHeight(),
                () -> extractLedgerSheet(graphics, mouseX, mouseY, ledgerH));
    }

    private void extractLedgerSheet(GuiGraphicsExtractor graphics, int mouseX, int mouseY, int ledgerH) {
        TileEntityAlchemyCauldron cauldron = this.getMenu().getCauldron();
        int centre = ledgerCentre();
        int left = this.leftPos + LEDGER_INNER_X;
        int right = left + LEDGER_INNER_W;
        int y = this.topPos + LEDGER_Y + ScrollWidgetRenderer.ROLL_H - 1 + 5;

        CauldronArt.initialled(graphics, this.font, Component.translatable("alchemia.gui.effects.title").getString(), centre, y, 0.875F);
        y += 13;
        CauldronArt.divider(graphics, centre, y, LEDGER_INNER_W);
        y += 10;

        extractVial(graphics, cauldron, centre - CauldronArt.VIAL_W / 2, y);
        y += 13;

        Component caption = cauldron.getWaterLevel() > 0
                ? Component.translatable("alchemia.gui.base.label", cauldron.getBase().displayName())
                : Component.translatable("alchemia.gui.effects.dry");
        centredClipped(graphics, caption, centre, y, LEDGER_INNER_W, INK_FAINT);
        y += 12;

        if (cauldron.isSpoiled()) {
            extractRuined(graphics, centre, y, this.topPos + LEDGER_Y + ledgerH - ScrollWidgetRenderer.ROLL_H - 2);
            return;
        }

        Component hovered = null;
        List<PotencySlot> committed = slotsOf(cauldron, cauldron.currentPotency(), null);
        watchPotency(committed);

        int budget = TileEntityAlchemyCauldron.maxPotency();
        int used = cauldron.totalPotency();
        ledgerRow(graphics, Component.translatable("alchemia.gui.ledger.potency"), centre, y,
                used >= budget ? INK_RED : INK_SOFT);
        y += 10;
        Component potencyHover = extractPotency(graphics, cauldron, centre, y, mouseX, mouseY);
        if (potencyHover != null) hovered = potencyHover;
        y += potencyRows() * POTENCY_CELL + 4;

        int cap = cauldron.getInstabilityCap();
        int spent = Math.min(cauldron.getInstability(), cap);
        ledgerRow(graphics, Component.translatable("alchemia.gui.ledger.instability"), centre, y,
                spent > 0 && spent + 1 >= cap ? INK_RED : INK_SOFT);
        y += 11;
        extractInstability(graphics, cap, spent, centre, y);
        y += 14;

        CauldronArt.divider(graphics, centre, y, LEDGER_INNER_W);
        y += 9;
        String heading = Component.translatable("alchemia.gui.readout.title").getString();
        CauldronArt.initialled(graphics, this.font, heading, centre, y, 0.75F);
        y += 12;

        extractReadout(graphics, cauldron, left, y);

        if (hovered != null) {
            ink(graphics, hoverIcon != null ? hoverIcon : InkTooltip.of(hovered, List.of()), mouseX, mouseY);
        }
    }

    private void ledgerRow(GuiGraphicsExtractor graphics, Component label, int centre, int y, int colour) {
        graphics.text(this.font, label, centre - this.font.width(label) / 2, y, colour, false);
    }

    private void centredClipped(GuiGraphicsExtractor graphics, Component text, int centre, int y, int width, int colour) {
        String shown = clip(text, width);
        graphics.text(this.font, shown, centre - this.font.width(shown) / 2, y, colour, false);
    }

    private String clip(Component text, int width) {
        String plain = text.getString();
        if (this.font.width(plain) <= width) return plain;
        return this.font.plainSubstrByWidth(plain, width - this.font.width("…")) + "…";
    }

    private void extractRuined(GuiGraphicsExtractor graphics, int centre, int y, int bottom) {
        graphics.item(new ItemStack(Items.SKELETON_SKULL), centre - 8, y + 4);
        int lineY = y + 26;
        for (FormattedCharSequence line : this.font.split(Component.translatable("alchemia.gui.effects.ruined"), LEDGER_INNER_W)) {
            if (lineY + LINE_HEIGHT > bottom) break;
            graphics.text(this.font, line, centre - this.font.width(line) / 2, lineY, INK_RED, false);
            lineY += LINE_HEIGHT;
        }
    }

    private static int liquidWidth(int level) {
        int inner = CauldronArt.VIAL_W - 4;
        return Math.max(4, Math.round(inner * Math.min(1.0F, (float) level / BrewBase.MAX_FILL_LEVEL)));
    }

    private static double hash(int seed, long salt) {
        long h = seed * 0x9E3779B97F4A7C15L + salt * 0xC2B2AE3D27D4EB4FL;
        h ^= h >>> 31;
        h *= 0xBF58476D1CE4E5B9L;
        h ^= h >>> 29;
        return (h >>> 11) * 0x1.0p-53;
    }

    private void extractVial(GuiGraphicsExtractor graphics, TileEntityAlchemyCauldron cauldron, int x, int y) {
        CauldronArt.vial(graphics, x, y);

        int level = cauldron.getWaterLevel();
        int filled = liquidWidth(level);
        if (level > 0) {
            int rgb = cauldron.isSpoiled() ? 0x55603E : cauldron.getPotionColor() & 0xFFFFFF;
            int inner = CauldronArt.VIAL_W - 4;
            long millis = Util.getMillis();
            int top = y + 2;
            int rows = CauldronArt.VIAL_H - 4;
            double radius = CauldronArt.VIAL_H / 2.0 - 2.0;

            for (int row = 0; row < rows; row++) {
                double dy = row + 0.5 - rows / 2.0;
                int inset = (int) Math.ceil(radius - Math.sqrt(Math.max(0.0, radius * radius - dy * dy)));
                int from = x + 2 + inset;
                int to = x + 2 + Math.min(filled, inner - inset);
                if (to <= from) continue;
                double light = row == 0 ? 0.35 : row == 1 ? 0.15 : row >= rows - 1 ? -0.3 : 0.0;
                int shade = light >= 0 ? ARGB.srgbLerp((float) light, rgb, 0xFFFFFF) : ARGB.srgbLerp((float) -light, rgb, 0x000000);
                graphics.fill(from, top + row, to, top + row + 1, ARGB.opaque(shade));
            }

            if (!cauldron.isSpoiled()) {
                extractBubbles(graphics, x, top, rows, filled, rgb, millis);
                if (boiledAt != 0L) {
                    double flash = 1.0 - Math.min(1.0, (millis - boiledAt) / (double) BOIL_MS);
                    graphics.fill(x + 2, top, x + 2 + filled, top + rows, ARGB.color((int) Math.round(150 * flash), BREW_GLOW_RGB));
                }
            }
        }

        CauldronArt.vialGlass(graphics, x, y);
        if (level > 0 && !cauldron.isSpoiled()) extractHeatGauge(graphics, x, y, filled);
    }

    private void extractBubbles(GuiGraphicsExtractor graphics, int x, int top, int rows, int filled, int rgb, long millis) {
        double hot = heatIntensity();
        int bright = ARGB.opaque(ARGB.srgbLerp(0.6F, rgb, 0xFFFFFF));
        int pale = ARGB.opaque(ARGB.srgbLerp(0.35F, rgb, 0xFFFFFF));
        int left = x + 3;
        int right = x + 2 + filled - 2;
        int span = Math.max(1, right - left);
        int count = 6 + (int) Math.round(hot * 14);
        double cycle = 1150.0 - 720.0 * hot;

        for (int bubble = 0; bubble < count; bubble++) {
            double speed = 0.6 + 0.8 * hash(bubble, 1);
            double t = millis * speed / cycle + hash(bubble, 2) * 16.0;
            long generation = (long) Math.floor(t);
            double phase = t - generation;
            long salt = generation * 8 + 3;
            double wander = Math.sin(phase * Math.PI * (1.5 + 2.5 * hash(bubble, salt + 1)) + bubble) * (0.5 + hot * 1.8);
            int bx = Mth.clamp(left + (int) Math.round(hash(bubble, salt) * span + wander), left, right);
            int by = top + rows - 1 - (int) Math.floor(phase * rows);
            if (by < top) continue;

            boolean large = hot > 0.4 && hash(bubble, salt + 2) < (hot - 0.4) * 1.1;
            int colour = by == top ? ARGB.color(220, 0xFFFFFF) : large ? pale : bright;
            graphics.fill(bx, by, Math.min(right + 1, bx + (large ? 2 : 1)), by + 1, colour);
            if (large && by > top) graphics.fill(bx, by - 1, bx + 1, by, bright);
        }

        if (hot > 0.15) {
            long beat = millis / (long) Math.round(110 - 60 * hot);
            for (int column = left; column <= right; column++) {
                if (hash(column, beat) < hot * 0.22) graphics.fill(column, top, column + 1, top + 1, bright);
            }
        }

        int glint = x + 2 + (int) ((millis / (45 - 20 * hot)) % Math.max(1, filled + 20)) - 10;
        if (glint > x + 3 && glint < x + 2 + filled - 2) {
            graphics.fill(glint, top, glint + 2, top + 1, ARGB.color(200, 0xFFFFFF));
        }
    }

    private void extractHeatGauge(GuiGraphicsExtractor graphics, int x, int y, int filled) {
        long millis = Util.getMillis();
        boolean ready = brewCheck() == TileEntityAlchemyCauldron.BrewCheck.READY;
        boolean boiling = boiledAt != 0L;
        double cap = heatCap();
        int last = x + 1 + filled;

        double beat = (Math.sin(millis / 220.0) + 1.0) * 0.5;
        int markAlpha = ready ? (int) Math.round(150 + beat * 105) : 110;
        graphics.fill(last, y - 3, last + 1, y + 1, ARGB.color(markAlpha, INK_RED & 0xFFFFFF));
        graphics.fill(last - 1, y - 3, last + 2, y - 2, ARGB.color(markAlpha, INK_RED & 0xFFFFFF));

        double hot = heatIntensity();
        if (hot > 0.55) {
            int puffs = (int) Math.round((hot - 0.55) * 14);
            for (int puff = 0; puff < puffs; puff++) {
                double phase = (millis / 700.0 + hash(puff, 9) * 4.0) % 1.0;
                int px = x + 4 + (int) Math.round(hash(puff, (long) (millis / 700.0 + hash(puff, 9) * 4.0)) * Math.max(1, filled - 6));
                int py = y - 1 - (int) Math.floor(phase * 6);
                graphics.fill(px, py, px + 1, py + 1, ARGB.color((int) Math.round(140 * (1.0 - phase)), 0xFFFFFF));
            }
        }

        double strain = strainAt == 0L ? 1.0 : Math.min(1.0, (millis - strainAt) / (double) STRAIN_MS);
        int shake = strain < 1.0 ? (int) Math.round(Math.sin(strain * Math.PI * 6) * 2 * (1.0 - strain)) : 0;
        int travel = (int) Math.round(Mth.clamp(heatShown, 0.0, cap) * (CauldronArt.VIAL_W - 4));
        int notchX = Mth.clamp(x + 1 + Math.max(1, travel), x + 2, last) + shake;
        graphics.fill(notchX, y + 1, notchX + 1, y + CauldronArt.VIAL_H - 1, ARGB.color(boiling ? 230 : 150, 0x2A1D13));
        CauldronArt.notch(graphics, notchX, y - CauldronArt.NOTCH_H + 1,
                boiling || (ready && heatShown > cap - HEAT_STRAIN_GAP - 0.03));
    }

    private void watchPotency(List<PotencySlot> committed) {
        long now = Util.getMillis();
        potencyBorn.values().removeIf(since -> now - since > POP_MS);
        potencyGhosts.removeIf(ghost -> now - ghost.since() > GHOST_MS);

        if (potencyTokens == null) {
            potencyTokens = committed.stream().map(PotencySlot::token).toList();
            potencyCommitted = committed;
            return;
        }

        List<String> tokens = committed.stream().map(PotencySlot::token).toList();
        if (tokens.equals(potencyTokens)) return;

        Set<String> before = new java.util.HashSet<>(potencyTokens);
        Set<String> after = new java.util.HashSet<>(tokens);
        for (int index = 0; index < tokens.size(); index++) {
            if (!before.contains(tokens.get(index))) potencyBorn.put(index, now);
        }
        for (int index = 0; index < potencyCommitted.size(); index++) {
            PotencySlot gone = potencyCommitted.get(index);
            if (!after.contains(gone.token())) potencyGhosts.add(new PotencyGhost(index, gone, now));
        }
        potencyTokens = tokens;
        potencyCommitted = committed;
    }

    private @Nullable Component extractPotency(GuiGraphicsExtractor graphics, TileEntityAlchemyCauldron cauldron,
                                               int centre, int gridY, int mouseX, int mouseY) {
        int budget = TileEntityAlchemyCauldron.maxPotency();
        int columns = Math.min(budget, POTENCY_PER_ROW);
        int gridX = centre - columns * POTENCY_CELL / 2;
        long now = Util.getMillis();
        List<PotencySlot> filled = potencySlots(cauldron);
        Component hovered = null;

        for (int index = 0; index < budget; index++) {
            int x = gridX + (index % POTENCY_PER_ROW) * POTENCY_CELL;
            int y = gridY + (index / POTENCY_PER_ROW) * POTENCY_CELL;
            Long born = potencyBorn.get(index);
            double pop = born == null ? 1.0 : Math.min(1.0, (now - born) / (double) POP_MS);
            boolean taken = index < filled.size();
            boolean incoming = taken && filled.get(index).incoming();

            CauldronArt.cell(graphics, x, y, pop < 1.0 || incoming ? CauldronArt.Cell.GILT : CauldronArt.Cell.PLAIN);
            if (!taken) continue;

            PotencySlot slot = filled.get(index);
            double grow = 1.0 + 0.45 * (1.0 - pop) * (1.0 - pop);
            double beat = (Math.sin(now / 300.0) + 1.0) * 0.5;
            int alpha = incoming ? (int) Math.round(80 + beat * 150) : 255;
            drawPotencyIcon(graphics, slot, x + 9, y + 9, grow, alpha);

            if (mouseX >= x && mouseX < x + POTENCY_CELL && mouseY >= y && mouseY < y + POTENCY_CELL) {
                hovered = slot.name();
                hoverIcon = slot.redstone() || slot.unknown()
                        ? InkTooltip.of(slot.name(), List.of())
                        : InkTooltip.of(slot.name(), List.of()).withSprite(AlchemiaEffects.sprite(slot.effect()));
            }
        }

        for (PotencyGhost ghost : potencyGhosts) {
            if (ghost.index() >= budget) continue;
            double t = Math.min(1.0, (now - ghost.since()) / (double) GHOST_MS);
            int x = gridX + (ghost.index() % POTENCY_PER_ROW) * POTENCY_CELL;
            int y = gridY + (ghost.index() / POTENCY_PER_ROW) * POTENCY_CELL;
            drawPotencyIcon(graphics, ghost.slot(), x + 9, y + 9 - (int) Math.round(t * 7), 1.0 - 0.3 * t,
                    (int) Math.round(255 * (1.0 - t)));
        }
        return hovered;
    }

    private void drawPotencyIcon(GuiGraphicsExtractor graphics, PotencySlot slot, int cx, int cy, double scale, int alpha) {
        if (alpha < 6) return;
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(cx, cy);
        pose.scale((float) scale, (float) scale);
        if (slot.redstone()) {
            CauldronArt.icon(graphics, CauldronArt.Icon.REDSTONE, -6, -6, false, alpha);
        } else if (slot.unknown()) {
            drawSymbol(graphics, 0, 0, 16, ARGB.color(alpha * (UNKNOWN_TINT >>> 24) / 255, UNKNOWN_TINT & 0xFFFFFF));
        } else {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, AlchemiaEffects.sprite(slot.effect()), -8, -8, 16, 16,
                    ARGB.color(alpha, 0xFFFFFF));
        }
        pose.popMatrix();
    }

    private void extractInstability(GuiGraphicsExtractor graphics, int cap, int spent, int centre, int y) {
        int width = cap * SKULL_PITCH - (SKULL_PITCH - SKULL_ICON);
        int x = centre - width / 2;
        float scale = SKULL_ICON / 16.0F;
        ItemStack intact = new ItemStack(Items.SKELETON_SKULL);
        ItemStack lost = new ItemStack(Items.WITHER_SKELETON_SKULL);

        for (int index = 0; index < cap; index++) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(x + index * SKULL_PITCH, y);
            graphics.pose().scale(scale, scale);
            graphics.item(index < spent ? lost : intact, 0, 0);
            graphics.pose().popMatrix();
        }
    }

    private record Readout(@Nullable Identifier sprite, int symbol, Component title, int titleColour, List<Component> lines) {
    }

    private static final int READOUT_NONE = 0;
    private static final int READOUT_UNKNOWN = 1;
    private static final int READOUT_DORMANT = 2;
    private static final int READOUT_NOTED = 3;

    private @Nullable Readout readout(TileEntityAlchemyCauldron cauldron) {
        if (cauldron.getWaterLevel() <= 0) {
            return new Readout(null, READOUT_NONE, Component.translatable("alchemia.gui.effects.dry"), INK_FAINT,
                    List.of(Component.translatable("alchemia.gui.readout.fill")));
        }
        if (!PotionMap.isReady()) return null;

        TileEntityAlchemyCauldron.BrewCheck check = brewCheck();
        PotionMap.PotionEffectPosition found = cauldron.effectAt(brewAt);
        if (check == TileEntityAlchemyCauldron.BrewCheck.UNAVAILABLE) return null;
        boolean caught = map().whirlpoolAt(brewAt) != null;
        if (check == TileEntityAlchemyCauldron.BrewCheck.NOTHING || found.getEffect() == null) {
            if (caught) {
                return new Readout(null, READOUT_NONE, Component.translatable("alchemia.gui.readout.whirl"), INK_SOFT,
                        List.of(Component.translatable("alchemia.gui.readout.whirl.hint")));
            }
            return new Readout(null, READOUT_NONE, Component.translatable("alchemia.gui.readout.nothing"), INK_FAINT,
                    List.of(Component.translatable("alchemia.gui.readout.nothing.hint")));
        }

        AlchemiaEffect recipe = found.getRecipe();
        boolean discovered = recipe != null && known.contains(recipe);
        Component name = discovered ? found.getEffect().value().getDisplayName() : Component.translatable("alchemia.gui.unknown");
        Identifier sprite = discovered ? recipe.sprite() : null;
        int symbol = discovered ? READOUT_NONE
                : recipe != null && WorldEffects.anyVisible(notes, recipe) ? READOUT_NOTED : READOUT_UNKNOWN;
        List<Component> lines = new ArrayList<>();

        switch (check) {
            case DORMANT -> {
                Component hint = dormantHint(recipe);
                lines.add(hint != null ? hint : Component.translatable("alchemia.brew.dormant"));
                return new Readout(null, READOUT_DORMANT, discovered
                        ? Component.translatable("alchemia.gui.dormant.named", name) : Component.translatable("alchemia.gui.dormant"),
                        INK_SOFT, lines);
            }
            case WEAKER -> lines.add(Component.translatable("alchemia.brew.weaker"));
            case TOO_POTENT -> lines.add(Component.translatable("alchemia.brew.too_potent"));
            default -> {
                lines.add(Component.translatable(found.isPerfect() ? "alchemia.gui.readout.perfect" : "alchemia.gui.readout.level",
                        roman(found.getStrength() + 1)));
                Component rotation = rotationNote(cauldron);
                if (rotation != null) lines.add(rotation);
            }
        }
        Component held = check == TileEntityAlchemyCauldron.BrewCheck.READY || check == TileEntityAlchemyCauldron.BrewCheck.WEAKER
                ? heldBackHint() : null;
        if (held != null) lines.add(held);

        if (caught) lines.add(Component.translatable("alchemia.gui.readout.whirl"));
        int colour = check == TileEntityAlchemyCauldron.BrewCheck.READY ? INK : INK_SOFT;
        return new Readout(sprite, symbol, name, colour, lines);
    }

    private List<PathPoint> whirlFlight(PathPoint from, PathPoint to) {
        if (!PotionMap.isReady()) return List.of();
        for (Whirlpool pool : map().whirlpools()) {
            if (pool.exit().distanceTo(to) < 1.0 && pool.centre().distanceTo(from) <= pool.radius()) return pool.link();
        }
        return List.of();
    }

    private void drawWhirlpools(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        TileEntityAlchemyCauldron cauldron = this.getMenu().getCauldron();
        Whirlpool held = map().whirlpoolAt(brewAt);
        PathRenderer.Clip clip = viewClip();
        double hover = Double.MAX_VALUE;

        for (Whirlpool pool : map().whirlpools()) {
            if (!whirlExits.contains(pool.mark(cauldron.getBase()))) continue;

            PathRenderer.dashes(graphics, toScreen(pool.link()), 1, ARGB.color(WHIRL_LINK_ALPHA, WHIRL_INK), true, 0.0, clip, pathScale());
            PathPoint exit = toScreen(pool.exit());
            double mark = Math.max(2.0, PotionMap.CELL * 0.3 * zoom);
            PathRenderer.polyline(graphics, spiral(pool.exit(), PotionMap.CELL * 0.45, -pool.spin(), 0.0, WHIRL_EXIT_TURNS),
                    whirlLine(false), WHIRL_FAINT);
            PathRenderer.disc(graphics, exit.x(), exit.y(), Math.max(1.0, mark * 0.3), ARGB.opaque(WHIRL_INK));

            double distance = Math.hypot(mouseX - exit.x(), mouseY - exit.y());
            if (!fogged(pool.exit()) && distance <= mark + 3 && distance < hover) {
                hover = distance;
                hoverX = mouseX;
                hoverY = mouseY;
                hoverName = Component.translatable("alchemia.gui.whirl.exit");
                hoverDetail = Component.translatable("alchemia.gui.whirl.exit.hint");
            }
        }

        for (Whirlpool pool : map().whirlpools()) {
            PathPoint centre = toScreen(pool.centre());
            double reach = pool.radius() * zoom;
            if (centre.x() + reach < viewLeft() || centre.x() - reach > viewLeft() + VIEW_WIDTH
                    || centre.y() + reach < viewTop() || centre.y() - reach > viewTop() + VIEW_HEIGHT) continue;

            boolean caught = pool == held;
            double turns = WHIRL_BASE_TURNS + pool.radius() / PotionMap.CELL;
            List<PathPoint> groove = spiral(pool.centre(), pool.radius(), pool.spin(), pool.spin() * whirlTurn, turns);
            drawFadingSpiral(graphics, groove, whirlLine(caught), caught ? ARGB.opaque(WHIRL_INK) : WHIRL_FAINT);

            double distance = Math.hypot(mouseX - centre.x(), mouseY - centre.y());
            if (!fogged(pool.centre()) && distance <= reach && distance < hover) {
                hover = distance;
                hoverX = mouseX;
                hoverY = mouseY;
                hoverName = Component.translatable("alchemia.gui.whirl");
                hoverDetail = Component.translatable(whirlExits.contains(pool.mark(cauldron.getBase()))
                        ? "alchemia.gui.whirl.hint.known" : "alchemia.gui.whirl.hint");
            }
        }
    }

    private void drawFadingSpiral(GuiGraphicsExtractor graphics, List<PathPoint> groove, double weight, int ink) {
        int count = groove.size();
        for (int chunk = 0; chunk < WHIRL_FADE_CHUNKS; chunk++) {
            int from = chunk * (count - 1) / WHIRL_FADE_CHUNKS;
            int to = (chunk + 1) * (count - 1) / WHIRL_FADE_CHUNKS;
            if (to <= from) continue;

            double t = (chunk + 0.5) / WHIRL_FADE_CHUNKS;
            double outer = Mth.clamp((t - WHIRL_FADE_START) / (1.0 - WHIRL_FADE_START), 0.0, 1.0);
            double inner = Mth.clamp(1.0 - t / WHIRL_FADE_CORE, 0.0, 1.0);
            float fade = (float) Math.min(WHIRL_FADE_MAX, outer * outer * (3.0 - 2.0 * outer) + inner * 0.35);

            List<PathPoint> piece = groove.subList(from, to + 1);
            List<PathPoint> shine = new ArrayList<>(piece.size());
            for (PathPoint point : piece) shine.add(new PathPoint(point.x() + 1.0, point.y() + 1.0));
            PathRenderer.polyline(graphics, shine, weight, ARGB.opaque(ARGB.srgbLerp(fade, WHIRL_SHINE, WHIRL_PAPER)));
            PathRenderer.polyline(graphics, piece, weight, ARGB.opaque(ARGB.srgbLerp(fade, ink, WHIRL_PAPER)));
        }
    }

    private double whirlLine(boolean caught) {
        double base = Mth.clamp(zoom, 0.6, 2.0);
        return caught ? base * 1.6 : base;
    }

    private List<PathPoint> spiral(PathPoint centre, double radius, int spin, double turn, double turns) {
        int samples = Mth.clamp((int) Math.round(radius * zoom * turns * 0.9), 24, 220);
        double start = radius * 0.08;
        List<PathPoint> points = new ArrayList<>(samples + 1);
        for (int step = 0; step <= samples; step++) {
            double t = (double) step / samples;
            double r = start + (radius - start) * t;
            double angle = turn - spin * t * turns * 2.0 * Math.PI;
            points.add(toScreen(new PathPoint(centre.x() + Math.cos(angle) * r, centre.y() + Math.sin(angle) * r)));
        }
        return points;
    }

    private void updateFog() {
        if (!FOG_OF_WAR_ENABLED) {
            fogBits = null;
            fogCorners = null;
            return;
        }
        PotionMap map = map();
        if (sessionMap != map) {
            sessionMap = map;
            sessionBits = MapReveals.empty(map);
            sessionAt = null;
            fogBits = null;
        }
        if (sessionAt == null || sessionAt.distanceTo(brewAt) >= MapReveals.CELL / 2.0) {
            MapReveals.mark(sessionBits, map, brewAt);
            sessionAt = brewAt;
        }

        long[] combined = sessionBits.clone();
        BrewBase base = this.getMenu().getCauldron().getBase();
        for (ItemStack guide : readableGuides) {
            long[] stored = MapReveals.bits(guide, base, map);
            for (int index = 0; index < combined.length; index++) combined[index] |= stored[index];
        }
        if (fogBits != null && java.util.Arrays.equals(combined, fogBits)) return;

        fogBits = combined;
        fogColumns = MapReveals.columns(map);
        int columns = fogColumns;
        float[] field = new float[columns * columns];
        for (int row = 0; row < columns; row++) {
            for (int column = 0; column < columns; column++) {
                field[row * columns + column] = MapReveals.revealed(combined, columns, column, row) ? 1.0F : 0.0F;
            }
        }
        for (int pass = 0; pass < FOG_BLUR_PASSES; pass++) field = blur(field, columns);

        float[] corners = new float[(columns + 1) * (columns + 1)];
        for (int row = 0; row <= columns; row++) {
            for (int column = 0; column <= columns; column++) {
                float sum = 0.0F;
                for (int dy = -1; dy <= 0; dy++) {
                    for (int dx = -1; dx <= 0; dx++) {
                        int x = column + dx;
                        int y = row + dy;
                        if (x >= 0 && y >= 0 && x < columns && y < columns) sum += field[y * columns + x];
                    }
                }
                double clear = Mth.clamp((sum / 4.0 - FOG_EDGE_LOW) / (FOG_EDGE_HIGH - FOG_EDGE_LOW), 0.0, 1.0);
                clear = clear * clear * (3.0 - 2.0 * clear);
                corners[row * (columns + 1) + column] = (float) (1.0 - clear);
            }
        }
        fogCorners = corners;
    }

    private static float[] blur(float[] field, int columns) {
        float[] out = new float[field.length];
        for (int row = 0; row < columns; row++) {
            for (int column = 0; column < columns; column++) {
                float sum = 0.0F;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int x = column + dx;
                        int y = row + dy;
                        if (x >= 0 && y >= 0 && x < columns && y < columns) sum += field[y * columns + x];
                    }
                }
                out[row * columns + column] = sum / 9.0F;
            }
        }
        return out;
    }

    private double fogCorner(long column, long row) {
        if (fogCorners == null || column < 0 || row < 0 || column > fogColumns || row > fogColumns) return 1.0;
        return fogCorners[(int) row * (fogColumns + 1) + (int) column];
    }

    private double fogAt(double mapX, double mapY) {
        double gx = mapX / MapReveals.CELL;
        double gy = mapY / MapReveals.CELL;
        long column = (long) Math.floor(gx);
        long row = (long) Math.floor(gy);
        double fx = gx - column;
        double fy = gy - row;
        double upper = Mth.lerp(fx, fogCorner(column, row), fogCorner(column + 1, row));
        double lower = Mth.lerp(fx, fogCorner(column, row + 1), fogCorner(column + 1, row + 1));
        return Mth.lerp(fy, upper, lower);
    }

    private boolean fogged(PathPoint at) {
        return FOG_OF_WAR_ENABLED && fogAt(at.x(), at.y()) > FOG_HIDES;
    }

    private void drawFog(GuiGraphicsExtractor graphics, int left, int top, int right, int bottom) {
        if (!FOG_OF_WAR_ENABLED) return;
        int cell = (int) MapReveals.CELL;
        long firstColumn = (long) Math.floor(mapAtX(left) / cell);
        long lastColumn = (long) Math.floor(mapAtX(right) / cell);
        long firstRow = (long) Math.floor(mapAtY(top) / cell);
        long lastRow = (long) Math.floor(mapAtY(bottom) / cell);
        int pieces = Mth.clamp((int) Math.round(cell * zoom / FOG_PIECE_PIXELS), 1, 4);
        int piece = cell / pieces;
        int tileCells = CauldronArt.MAP_TILE / cell;

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate((float) screenX(0.0), (float) screenY(0.0));
        pose.scale((float) zoom, (float) zoom);

        for (long row = firstRow; row <= lastRow; row++) {
            long runStart = Long.MIN_VALUE;
            for (long column = firstColumn; column <= lastColumn + 1; column++) {
                boolean inside = column <= lastColumn;
                double f00 = inside ? fogCorner(column, row) : 0.0;
                double f10 = inside ? fogCorner(column + 1, row) : 0.0;
                double f01 = inside ? fogCorner(column, row + 1) : 0.0;
                double f11 = inside ? fogCorner(column + 1, row + 1) : 0.0;
                double low = Math.min(Math.min(f00, f10), Math.min(f01, f11));
                double high = Math.max(Math.max(f00, f10), Math.max(f01, f11));
                boolean full = inside && low >= 0.995;
                boolean sameTile = runStart != Long.MIN_VALUE && Math.floorDiv(runStart, tileCells) == Math.floorDiv(column, tileCells);

                if (runStart != Long.MIN_VALUE && (!full || !sameTile)) {
                    fogPiece(graphics, (int) (runStart * cell), (int) (row * cell), (int) ((column - runStart) * cell), cell, 1.0);
                    runStart = Long.MIN_VALUE;
                }
                if (full) {
                    if (runStart == Long.MIN_VALUE) runStart = column;
                    continue;
                }
                if (!inside || high <= 0.005) continue;

                for (int sy = 0; sy < pieces; sy++) {
                    for (int sx = 0; sx < pieces; sx++) {
                        double u = (sx + 0.5) / pieces;
                        double v = (sy + 0.5) / pieces;
                        double fog = Mth.lerp(v, Mth.lerp(u, f00, f10), Mth.lerp(u, f01, f11));
                        if (fog <= 0.005) continue;
                        fogPiece(graphics, (int) (column * cell) + sx * piece, (int) (row * cell) + sy * piece, piece, piece, fog);
                    }
                }
            }
        }
        pose.popMatrix();
    }

    private void fogPiece(GuiGraphicsExtractor graphics, int x, int y, int w, int h, double fog) {
        CauldronArt.mapPaperPiece(graphics, x, y, w, h, ARGB.color((int) Math.round(255 * fog), FOG_TINT));
    }

    private static String roman(int value) {
        String[] numerals = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return value >= 1 && value <= numerals.length ? numerals[value - 1] : String.valueOf(value);
    }

    private void extractReadout(GuiGraphicsExtractor graphics, TileEntityAlchemyCauldron cauldron, int left, int y) {
        Readout readout = readout(cauldron);
        if (readout == null) return;

        long now = Util.getMillis();
        List<Object> key = List.of(readout.title().getString(), readout.lines().stream().map(Component::getString).toList());
        if (!key.equals(readoutKey)) {
            boolean moved = readoutPos != null && readoutPos.distanceTo(brewAt) > 1.0E-6;
            if (readoutKey != null && moved && readout.titleColour() == INK) triggerLanding(cauldron);
            readoutKey = key;
            readoutAt = now;
        }
        readoutPos = brewAt;
        double fade = Math.min(1.0, (now - readoutAt) / (double) READOUT_FADE_MS);
        int alpha = (int) Math.round(255 * fade);
        if (alpha < 8) return;

        double flash = landingAt == Long.MIN_VALUE ? 1.0 : Math.min(1.0, (now - landingAt) / (double) LANDING_MS);
        if (flash < 1.0) {
            int glow = (int) Math.round(90 * (1.0 - flash));
            graphics.fill(left - 2, y - 2, left + LEDGER_INNER_W + 2, y + 18, ARGB.color(glow, BREW_GLOW_RGB));
        }

        boolean iconed = readout.sprite() != null || readout.symbol() != READOUT_NONE;
        if (readout.sprite() != null) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, readout.sprite(), left, y, 16, 16, ARGB.color(alpha, 0xFFFFFF));
        } else if (readout.symbol() == READOUT_DORMANT) {
            drawClosedEye(graphics, left + 8, y + 8, 16);
        } else if (readout.symbol() == READOUT_NOTED) {
            drawOpenEye(graphics, left + 8, y + 8, 16);
        } else if (readout.symbol() == READOUT_UNKNOWN) {
            drawSymbol(graphics, left + 8, y + 8, 16, ARGB.color(alpha * (UNKNOWN_TINT >>> 24) / 255, UNKNOWN_TINT & 0xFFFFFF));
        }

        int textX = iconed ? left + 20 : left;
        int titleW = LEDGER_INNER_W - (textX - left);
        List<FormattedCharSequence> title = this.font.split(readout.title(), titleW);
        int titleY = title.size() > 1 ? y - 1 : y + 4;
        for (int line = 0; line < Math.min(2, title.size()); line++) {
            graphics.text(this.font, title.get(line), textX, titleY + line * 9, ARGB.color(alpha, readout.titleColour() & 0xFFFFFF), false);
        }

        int lineY = y + 20;
        int bottom = y + READOUT_H;
        for (Component line : readout.lines()) {
            for (FormattedCharSequence wrapped : this.font.split(line, LEDGER_INNER_W)) {
                if (lineY + 9 > bottom) return;
                graphics.text(this.font, wrapped, left, lineY, ARGB.color(alpha, INK_SOFT & 0xFFFFFF), false);
                lineY += 9;
            }
        }
    }

    private void triggerLanding(TileEntityAlchemyCauldron cauldron) {
        PotionMap.PotionEffectPosition found = cauldron.effectAt(brewAt);
        if (found.getEffect() == null || !found.isPerfect() || found.getRecipe() == null) return;
        PathPoint at = map().positionOf(found.getRecipe());
        if (at == null) return;

        List<Object> key = List.of(found.getRecipe(), found.getStrength());
        if (key.equals(landingKey) && Util.getMillis() - landingAt < LANDING_MS * 3) return;
        landingKey = key;
        landingAt = Util.getMillis();
        landingPoint = at;
        landingRgb = known.contains(found.getRecipe()) ? found.getEffect().value().getColor() : AURA_UNKNOWN_RGB;
    }

    private void drawLandingPulse(GuiGraphicsExtractor graphics) {
        if (landingAt == Long.MIN_VALUE) return;
        double t = (Util.getMillis() - landingAt) / (double) LANDING_MS;
        if (t >= 1.0) return;

        PathPoint centre = toScreen(landingPoint);
        double eased = PathGeometry.ease(t);
        double radius = (PotionMap.EFFECT_CORE + (PotionMap.EFFECT_RADIUS * 1.4 - PotionMap.EFFECT_CORE) * eased) * zoom;
        int alpha = (int) Math.round(220 * (1.0 - t));
        if (radius >= 2.0) {
            PathRenderer.ring(graphics, centre.x(), centre.y(), radius, ARGB.color(alpha, landingRgb), false);
            PathRenderer.ring(graphics, centre.x(), centre.y(), radius * 0.7, ARGB.color(alpha / 2, landingRgb), false);
        }
    }

    private void buttonFace(GuiGraphicsExtractor graphics, int x, int y, int w, boolean ready, boolean hovered, boolean down, boolean call) {
        Identifier sprite;
        if (!ready) {
            sprite = CauldronArt.BUTTON_DISABLED;
        } else if (call) {
            sprite = down ? CauldronArt.BUTTON_READY_PRESSED : hovered ? CauldronArt.BUTTON_READY_HIGHLIGHTED : CauldronArt.BUTTON_READY;
        } else {
            sprite = down ? CauldronArt.BUTTON_PRESSED : hovered ? CauldronArt.BUTTON_HIGHLIGHTED : CauldronArt.BUTTON;
        }
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, w, BUTTON_H);
    }

    private void buttonLabel(GuiGraphicsExtractor graphics, int x, int y, int w, boolean ready, boolean down, boolean call,
                             CauldronArt.@Nullable Icon icon, Component label) {
        int sink = down && ready ? 1 : 0;
        int colour = !ready ? INK_FAINT : call ? CauldronArt.PAGE_SHINE : INK;
        int iconW = icon == null ? 0 : CauldronArt.ICON + 3;
        String shown = clip(label, w - 8 - iconW);
        int start = x + (w - this.font.width(shown) - iconW) / 2;
        if (icon != null) CauldronArt.icon(graphics, icon, start, y + 3 + sink, !ready);
        graphics.text(this.font, shown, start + iconW, y + 5 + sink, colour, false);
    }

    private void extractStirButton(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        boolean ready = canStir();
        boolean hovered = overStirButton(mouseX, mouseY);
        boolean down = pressedDown(PRESS_STIR, stirring);
        int x = this.leftPos + STIR_X;
        int y = this.topPos + TIMELINE_Y;

        buttonFace(graphics, x, y, ACTION_W, ready, hovered, down, false);
        buttonLabel(graphics, x, y, ACTION_W, ready, down, false, CauldronArt.Icon.STIR, Component.translatable("alchemia.gui.stir"));

        if (hovered) {
            ink(graphics, InkTooltip.of(Component.translatable("alchemia.gui.stir"), List.of(Component.translatable(ready
                    ? "alchemia.gui.stir.hint" : "alchemia.gui.stir.idle"))), mouseX, mouseY);
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

    private boolean canHeat() {
        TileEntityAlchemyCauldron cauldron = this.getMenu().getCauldron();
        return cauldron.getWaterLevel() > 0 && !cauldron.isSpoiled() && PotionMap.isReady();
    }

    private void stoke() {
        long now = Util.getMillis();
        if (boiledAt != 0L) return;

        sendButton(AlchemicalCauldronMenu.BUTTON_HEAT);
        lastStokeAt = now;
        double cap = heatCap();
        heat = Math.min(cap, heat + HEAT_PUMP);
        heatHeldUntil = now + HEAT_HOLD_MS;
        uiSound(SoundEvents.FIRECHARGE_USE, 1.3F + (float) heatIntensity() * 0.4F, 0.18F);

        if (heat < cap - 1.0E-6) return;
        if (brewCheck() == TileEntityAlchemyCauldron.BrewCheck.READY) {
            boiledAt = now;
            TileEntityAlchemyCauldron cauldron = this.getMenu().getCauldron();
            PotionMap.PotionEffectPosition found = cauldron.effectAt(brewAt);
            puffRgb = found.getEffect() != null ? found.getEffect().value().getColor() : cauldron.getPotionColor();
            puffAt = now;
        } else {
            heat = Math.max(0.0, cap - HEAT_STRAIN_GAP);
            strainAt = now;
            uiSound(SoundEvents.FIRE_EXTINGUISH, 1.6F, 0.15F);
        }
    }

    private void tickHeat() {
        long now = Util.getMillis();
        double dt = heatFrame == 0L ? 0.0 : Math.min(0.1, (now - heatFrame) / 1000.0);
        heatFrame = now;

        if (boiledAt != 0L && now - boiledAt >= BOIL_MS) {
            boiledAt = 0L;
            heat = heatCap() * BOIL_REST_SHARE;
            heatHeldUntil = now + HEAT_HOLD_MS;
        }
        heat = Math.min(heat, heatCap());
        if (!canHeat()) {
            boiledAt = 0L;
            heat = Math.max(0.0, heat - HEAT_COOL * 4.0 * dt);
        } else if (boiledAt == 0L && now >= heatHeldUntil) {
            heat = Math.max(0.0, heat - HEAT_COOL * dt);
        }

        ContainerData data = this.getMenu().getCauldronData();
        if (now - lastStokeAt > HEAT_TRUST_MS && boiledAt == 0L) {
            double server = (double) data.get(TileEntityAlchemyCauldron.DATA_HEAT) / CauldronHeat.SYNC_SCALE;
            heat += (server - heat) * Math.min(1.0, dt * 6.0);
        }
        if (data.get(TileEntityAlchemyCauldron.DATA_BOIL) > 0 && boiledAt == 0L && now - lastStokeAt < BOIL_MS * 2) {
            boiledAt = now;
            puffRgb = this.getMenu().getCauldron().getPotionColor();
            puffAt = now;
        }

        double rate = heat > heatShown ? 16.0 : 7.0;
        heatShown += (heat - heatShown) * (1.0 - Math.exp(-rate * dt));
        whirlTurn = (whirlTurn + Whirlpool.SPIN * heatIntensity() * dt) % (2.0 * Math.PI * 1000.0);

        double hot = heatIntensity();
        if (hot > 0.3 && now >= nextBubbleSound) {
            float pitch = 0.8F + ThreadLocalRandom.current().nextFloat() * 0.5F;
            uiSound(SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, pitch, 0.08F + (float) hot * 0.22F);
            nextBubbleSound = now + Math.round(900 - 650 * hot) + ThreadLocalRandom.current().nextInt(160);
        }
    }

    private double heatCap() {
        return CauldronHeat.cap(this.getMenu().getCauldron().getWaterLevel());
    }

    private double heatIntensity() {
        double cap = heatCap();
        return cap <= 0.0 ? 0.0 : Mth.clamp(heatShown / cap, 0.0, 1.0);
    }

    private void uiSound(SoundEvent sound, float pitch, float volume) {
        if (this.minecraft == null) return;
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
    }

    private void extractHeatButton(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        boolean usable = canHeat();
        boolean ready = usable && brewCheck() == TileEntityAlchemyCauldron.BrewCheck.READY;
        boolean hovered = overHeatButton(mouseX, mouseY);
        boolean down = pressedDown(PRESS_HEAT, false);
        int x = this.leftPos + HEAT_X;
        int y = this.topPos + TIMELINE_Y;

        buttonFace(graphics, x, y, ACTION_W, usable, hovered, down, ready);

        if (ready && !down) {
            double phase = (Util.getMillis() % 2600L) / 2600.0;
            if (phase < 0.4) {
                int sweep = x + (int) Math.round((ACTION_W + 10) * phase / 0.4) - 6;
                graphics.enableScissor(x + 2, y + 2, x + ACTION_W - 2, y + BUTTON_H - 2);
                graphics.fill(sweep, y + 2, sweep + 3, y + BUTTON_H - 2, ARGB.color(70, 0xFFFFFF));
                graphics.fill(sweep + 3, y + 2, sweep + 4, y + BUTTON_H - 2, ARGB.color(35, 0xFFFFFF));
                graphics.disableScissor();
            }
        }

        buttonLabel(graphics, x, y, ACTION_W, usable, down, ready, CauldronArt.Icon.BREW, Component.translatable("alchemia.gui.heat"));

        if (hovered) {
            ink(graphics, InkTooltip.of(Component.translatable("alchemia.gui.heat"),
                    List.of(Component.translatable(usable ? "alchemia.gui.heat.hint" : "alchemia.gui.heat.idle"))), mouseX, mouseY);
        }
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

    private static int applyButton() {
        return ClientIngredientPathTooltip.shiftDown() ? AlchemicalCauldronMenu.BUTTON_APPLY_TOOL_FAST : AlchemicalCauldronMenu.BUTTON_APPLY_TOOL;
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

    private void extractPrep(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        TileEntityAlchemyCauldron cauldron = this.getMenu().getCauldron();
        ItemStack active = activeIngredient();
        ItemStack held = cauldron.getTool();
        IngredientTool tool = IngredientTool.of(held);
        int y = this.topPos + PREP_Y;

        int ingredientX = this.leftPos + AlchemicalCauldronMenu.ACTIVE_SLOT_X;
        int toolX = this.leftPos + AlchemicalCauldronMenu.TOOL_SLOT_X;
        CauldronArt.cell(graphics, ingredientX, y, active.isEmpty() ? CauldronArt.Cell.PLAIN : CauldronArt.Cell.SELECTED);
        CauldronArt.cell(graphics, toolX, y, held.isEmpty() ? CauldronArt.Cell.PLAIN : CauldronArt.Cell.SELECTED);
        if (active.isEmpty()) CauldronArt.icon(graphics, CauldronArt.Icon.INGREDIENTS, ingredientX + 3, y + 3, true, 170);
        if (held.isEmpty()) CauldronArt.icon(graphics, CauldronArt.Icon.TOOL, toolX + 3, y + 3, true, 170);

        extractWorkButton(graphics, tool, active, mouseX, mouseY);
        extractAddButton(graphics, mouseX, mouseY);
        extractPrepInfo(graphics, active, this.leftPos + INFO_X, y);
    }

    private void extractWorkButton(GuiGraphicsExtractor graphics, @Nullable IngredientTool tool, ItemStack active, int mouseX, int mouseY) {
        boolean ready = canApplyTool();
        boolean hovered = overToolButton(mouseX, mouseY);
        boolean down = pressedDown(PRESS_WORK, applying);
        int x = this.leftPos + WORK_X;
        int y = this.topPos + PREP_Y;

        double target = tool != null && TileEntityAlchemyCauldron.isBrewingInput(active) ? Mth.clamp(tool.progress(active), 0.0F, 1.0F) : 0.0;
        workShown = Math.abs(target - workShown) < 0.002 ? target : Mth.lerp(0.3, workShown, target);

        buttonFace(graphics, x, y, WORK_W, ready || workShown > 0.0, hovered && ready, down, false);
        int fill = (int) Math.round((WORK_W - 4) * workShown);
        if (fill >= 4) {
            int sink = down && ready ? 1 : 0;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, CauldronArt.WORK_FILL, x + 2, y + 2 + sink, fill, BUTTON_H - 4 - sink);
        }

        Component label = Component.translatable(tool == null ? "alchemia.gui.tool.missing" : tool.actionKey());
        buttonLabel(graphics, x, y, WORK_W, ready || workShown > 0.0, down, false, null, label);

        if (!hovered) return;
        ItemStack heldTool = this.getMenu().getCauldron().getTool();
        InkTooltip tip = tool == null
                ? InkTooltip.of(Component.translatable("alchemia.gui.tool.title"), List.of(Component.translatable("alchemia.gui.tool.needs")))
                : InkTooltip.of(heldTool.getHoverName(), List.of(Component.translatable("alchemia.gui.tool.hint"))).withIcon(heldTool);
        ink(graphics, tip, mouseX, mouseY);
    }

    private void extractAddButton(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        boolean ready = canAdd();
        boolean hovered = overAddButton(mouseX, mouseY);
        boolean down = pressedDown(PRESS_ADD, false);
        int x = this.leftPos + ADD_X;
        int y = this.topPos + PREP_Y;

        buttonFace(graphics, x, y, ADD_W, ready, hovered, down, false);
        buttonLabel(graphics, x, y, ADD_W, ready, down, false, null, Component.translatable("alchemia.gui.add"));
    }

    private void extractPrepInfo(GuiGraphicsExtractor graphics, ItemStack active, int x, int y) {
        int width = this.leftPos + HEADER_RIGHT - x;
        if (active.isEmpty()) {
            graphics.text(this.font, clip(Component.translatable("alchemia.gui.prep.empty"), width), x, y + 1, INK_FAINT, false);
            graphics.text(this.font, clip(Component.translatable("alchemia.gui.prep.empty.hint"), width), x, y + 10, INK_FAINT, false);
            return;
        }

        Component name = active.getCount() > 1
                ? Component.translatable("alchemia.gui.prep.count", active.getHoverName(), active.getCount())
                : active.getHoverName();
        Component detail;
        if (BrewModifiers.of(active) != null) {
            detail = Component.translatable("alchemia.gui.prep.modifier");
        } else if (VanillaIngredients.isLure(active)) {
            detail = Component.translatable("alchemia.gui.prep.lure", Ingredients.crushPercent(ItemIngredient.crushOf(active)));
        } else if (ItemIngredient.canCrush(active)) {
            int crush = ItemIngredient.crushOf(active);
            detail = crush <= 0 ? Component.translatable("alchemia.gui.prep.whole")
                    : Component.translatable("alchemia.gui.prep.ground", Ingredients.crushPercent(crush));
        } else {
            detail = Component.translatable("alchemia.gui.prep.ready");
        }
        graphics.text(this.font, clip(name, width), x, y + 1, INK, false);
        graphics.text(this.font, clip(detail, width), x, y + 10, INK_FAINT, false);
    }

    private void watchQueue(List<ItemStack> queued) {
        if (queueShown == null) {
            queueShown = copies(queued);
            queueFrom = identity(queued.size());
            return;
        }
        if (sameStacks(queued, queueShown)) return;

        int[] from = new int[queued.size()];
        java.util.Arrays.fill(from, -1);
        int previous = queueShown.size();

        int shift = 0;
        while (shift < previous && !matchesFrom(queued, queueShown, shift)) shift++;
        if (shift < previous || previous == 0) {
            for (int index = 0; index < queued.size(); index++) from[index] = index + shift < previous ? index + shift : -1;
        } else {
            int removed = 0;
            while (removed < previous && removed < queued.size() && sameStack(queued.get(removed), queueShown.get(removed))) removed++;
            for (int index = 0; index < queued.size(); index++) {
                int source = index < removed ? index : index + 1;
                from[index] = source < previous && sameStack(queued.get(index), queueShown.get(source)) ? source : -1;
            }
        }

        arrival = -1;
        for (int index = 0; index < from.length; index++) {
            if (from[index] < 0) arrival = index;
        }
        queueFrom = from;
        queueMoved = Util.getMillis();
        if (arrival >= 0) arrivalAt = queueMoved;
        queueShown = copies(queued);
    }

    private static boolean matchesFrom(List<ItemStack> now, List<ItemStack> before, int shift) {
        int overlap = Math.min(now.size(), before.size() - shift);
        if (overlap <= 0 || before.size() - shift > now.size()) return false;
        for (int index = 0; index < overlap; index++) {
            if (!sameStack(now.get(index), before.get(index + shift))) return false;
        }
        return true;
    }

    private static boolean sameStacks(List<ItemStack> a, List<ItemStack> b) {
        if (a.size() != b.size()) return false;
        for (int index = 0; index < a.size(); index++) {
            if (!sameStack(a.get(index), b.get(index))) return false;
        }
        return true;
    }

    private static boolean sameStack(ItemStack a, ItemStack b) {
        return ItemStack.isSameItemSameComponents(a, b);
    }

    private static List<ItemStack> copies(List<ItemStack> stacks) {
        List<ItemStack> copied = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) copied.add(stack.copy());
        return copied;
    }

    private static int[] identity(int size) {
        int[] from = new int[size];
        for (int index = 0; index < size; index++) from[index] = index;
        return from;
    }

    private void extractTimeline(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        List<ItemStack> queued = queuedIngredients();
        watchQueue(queued);
        flyingStack = ItemStack.EMPTY;

        int gridX = this.leftPos + TIMELINE_X;
        int gridY = this.topPos + TIMELINE_Y;
        int hoveredCell = queueCellAt(mouseX, mouseY);
        long now = Util.getMillis();
        double slide = PathGeometry.ease(Math.min(1.0, (now - queueMoved) / (double) SLIDE_MS));
        double arrive = Math.min(1.0, (now - arrivalAt) / (double) ARRIVE_MS);
        List<Component> hovered = null;
        ItemStack hoveredStack = ItemStack.EMPTY;

        for (int index = 0; index < QUEUE_COLUMNS * QUEUE_ROWS; index++) {
            int x = gridX + (index % QUEUE_COLUMNS) * QUEUE_CELL;
            int y = gridY + (index / QUEUE_COLUMNS) * QUEUE_CELL;
            boolean current = index == 0 && shownCourse.started() && index < queued.size();
            CauldronArt.Cell state = index >= queued.size() ? CauldronArt.Cell.PLAIN
                    : current ? CauldronArt.Cell.GILT : CauldronArt.Cell.SELECTED;
            CauldronArt.cell(graphics, x, y, state);
        }

        for (int index = 0; index < queued.size() && index < QUEUE_COLUMNS * QUEUE_ROWS; index++) {
            int x = gridX + (index % QUEUE_COLUMNS) * QUEUE_CELL;
            int y = gridY + (index / QUEUE_COLUMNS) * QUEUE_CELL;
            ItemStack stack = queued.get(index);
            int source = index < queueFrom.length ? queueFrom[index] : index;

            if (source < 0 && index == arrival && arrive < 1.0) {
                double t = PathGeometry.ease(arrive);
                double startX = this.leftPos + AlchemicalCauldronMenu.ACTIVE_SLOT_X;
                double startY = this.topPos + PREP_Y;
                flyingStack = stack;
                flyingX = (int) Math.round(Mth.lerp(t, startX, x)) + 1;
                flyingY = (int) Math.round(Mth.lerp(t, startY, y) - Math.sin(Math.PI * t) * 6.0) + 1;
            } else {
                int offset = source >= 0 && slide < 1.0 ? (int) Math.round((source - index) * QUEUE_CELL * (1.0 - slide)) : 0;
                graphics.item(stack, x + 1 + offset, y + 1);
            }

            if (index == 0 && shownCourse.started() && PotionMap.isReady()) {
                PotionMap.RouteLeg leg = shownCourse.current(map());
                double length = leg == null ? 0.0 : BrewCourse.lengthOf(leg);
                int filled = length <= 0.0 ? 0 : Mth.clamp((int) Math.round(14 * shownCourse.progress() / length), 0, 14);
                if (filled >= 2) graphics.blitSprite(RenderPipelines.GUI_TEXTURED, CauldronArt.LEG_PROGRESS, x + 2, y + 14, filled, 3);
            }

            if (index == hoveredCell) {
                hovered = queueTooltip(index, stack);
                hoveredStack = stack;
            }
        }

        drawLureBrackets(graphics, gridX, gridY, queued.size(), hoveredCell);

        if (hovered != null) {
            List<Component> body = new ArrayList<>();
            for (int line = 1; line < hovered.size(); line++) body.add(Component.literal(hovered.get(line).getString()));
            ink(graphics, InkTooltip.of(hovered.getFirst(), body).withIcon(hoveredStack), mouseX, mouseY);
        }
    }

    private void extractHeader(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int y = this.topPos + HEADER_Y;
        CauldronArt.icon(graphics, CauldronArt.Icon.COPY, this.leftPos + VIEW_INSET_X, y - 2, false);

        boolean focused = this.nameField != null && this.nameField.isFocused();
        boolean hovered = overNameField(mouseX, mouseY);
        int ruleY = this.topPos + NAME_Y + NAME_H - 1;
        int ruleX = this.leftPos + NAME_X;
        for (int x = 0; x < NAME_W; x++) {
            if (focused || hovered || x % 2 == 0) {
                graphics.fill(ruleX + x, ruleY, ruleX + x + 1, ruleY + 1, focused ? INK_SOFT : INK_FAINT);
            }
        }
        drawNameText(graphics);

        int right = this.leftPos + HEADER_RIGHT;
        Component recipes = Component.translatable("alchemia.gui.recipes", known.size(), RecipeDiscovery.total());
        int recipesX = right - this.font.width(recipes);
        graphics.text(this.font, recipes, recipesX, y, INK_SOFT, false);
        CauldronArt.icon(graphics, CauldronArt.Icon.EYE, recipesX - CauldronArt.ICON - 3, y - 2, false);

        Component zoomLabel = Component.translatable("alchemia.gui.zoom", Math.round(zoom * 100));
        int diamondX = recipesX - CauldronArt.ICON - 12;
        CauldronArt.diamond(graphics, diamondX, y + 1);
        graphics.text(this.font, zoomLabel, diamondX - 5 - this.font.width(zoomLabel), y, INK_FAINT, false);
    }

    private void extractMapFrame(GuiGraphicsExtractor graphics) {
        int x0 = viewLeft();
        int y0 = viewTop();
        int x1 = x0 + VIEW_WIDTH;
        int y1 = y0 + VIEW_HEIGHT;

        graphics.fill(x0, y0, x1, y0 + 1, FRAME_SHADOW);
        graphics.fill(x0, y0 + 1, x0 + 1, y1, FRAME_SHADOW);
        graphics.outline(x0 - 1, y0 - 1, VIEW_WIDTH + 2, VIEW_HEIGHT + 2, FRAME_INNER);
        graphics.outline(x0 - 2, y0 - 2, VIEW_WIDTH + 4, VIEW_HEIGHT + 4, ARGB.color(110, FRAME_OUTER & 0xFFFFFF));

        int half = CauldronArt.DIAMOND / 2;
        CauldronArt.diamond(graphics, x0 - 2 - half, y0 - 2 - half);
        CauldronArt.diamond(graphics, x1 + 1 - half, y0 - 2 - half);
        CauldronArt.diamond(graphics, x0 - 2 - half, y1 + 1 - half);
        CauldronArt.diamond(graphics, x1 + 1 - half, y1 + 1 - half);
    }

    private void queueTooltip(List<ClientTooltipComponent> lines, int guiX, int guiY, @Nullable Identifier style,
                              boolean spaced, ItemStack stack) {
        if (pendingTooltip != null || lines.isEmpty()) return;
        pendingTooltip = new PendingTooltip(List.copyOf(lines), guiX, guiY, style, spaced, stack);
    }

    private void ink(GuiGraphicsExtractor graphics, InkTooltip tooltip, int guiX, int guiY) {
        queueTooltip(List.of(ClientTooltipComponent.create(tooltip)), guiX, guiY, InkTooltip.STYLE, false, ItemStack.EMPTY);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (this.hoveredSlot == null || !this.hoveredSlot.hasItem()) return;
        ItemStack item = this.hoveredSlot.getItem();
        if (!this.menu.getCarried().isEmpty()) return;

        int guiX = (int) Math.round(toGui(mouseX));
        int guiY = (int) Math.round(toGui(mouseY));
        List<ClientTooltipComponent> lines = ClientHooks.gatherTooltipComponents(item, this.getTooltipFromContainerItem(item),
                item.getTooltipImage(), guiX, scaledWidth(), scaledHeight(), this.font);
        queueTooltip(lines, guiX, guiY, item.get(DataComponents.TOOLTIP_STYLE), true, item);
    }

    private int scaledWidth() {
        return (int) Math.floor(this.width / this.uiScale);
    }

    private int scaledHeight() {
        return (int) Math.floor(this.height / this.uiScale);
    }

    private void extractPendingTooltip(GuiGraphicsExtractor graphics) {
        PendingTooltip tip = pendingTooltip;
        pendingTooltip = null;
        if (tip == null) return;

        int screenW = scaledWidth();
        int screenH = scaledHeight();
        ClientTooltipPositioner positioner = (width, height, x, y, w, h) ->
                DefaultTooltipPositioner.INSTANCE.positionTooltip(screenW, screenH, x, y, w, h);

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.scale(this.uiScale, this.uiScale);
        graphics.nextStratum();
#if MC_VER >= 26300
        graphics.tooltip(this.font, tip.lines(), tip.x(), tip.y(), positioner, tip.style(), tip.spaced(), tip.stack());
#else
        graphics.tooltip(this.font, tip.lines(), tip.x(), tip.y(), positioner, tip.style());
#endif
        pose.popMatrix();
    }

    private record PendingTooltip(List<ClientTooltipComponent> lines, int x, int y, @Nullable Identifier style,
                                  boolean spaced, ItemStack stack) {
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.scale(this.uiScale, this.uiScale);

        super.extractContents(graphics, (int) Math.round(toGui(mouseX)), (int) Math.round(toGui(mouseY)), a);
        if (!flyingStack.isEmpty()) {
            graphics.nextStratum();
            graphics.item(flyingStack, flyingX, flyingY);
        }

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

        if (hoverName != null) {
            InkTooltip tip = InkTooltip.of(hoverName, hoverDetail == null ? List.of() : List.of(hoverDetail));
            ink(graphics, hoverSprite != null ? tip.withSprite(hoverSprite) : tip, hoverX, hoverY);
        }
        extractPendingTooltip(graphics);
    }

    private void drawNameText(GuiGraphicsExtractor graphics) {
        if (this.nameField == null) return;

        int textX = nameTextX();
        int fieldY = this.topPos + NAME_Y;
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
        return this.leftPos + NAME_X + 2;
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

    private void extractMap(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        TileEntityAlchemyCauldron cauldron = this.getMenu().getCauldron();

        int left = viewLeft();
        int top = viewTop();
        int right = left + VIEW_WIDTH;
        int bottom = top + VIEW_HEIGHT;

        graphics.enableScissor(left, top, right, bottom);
        drawMapPaper(graphics, left, top, right, bottom);
        graphics.nextStratum();

        if (!PotionMap.isReady() || cauldron.getWaterLevel() <= 0) {
            drawMapGrid(graphics, left, top, right, bottom);
            graphics.nextStratum();
            graphics.disableScissor();
            return;
        }

        updateFog();
        PathPoint centre = toScreen(new PathPoint(map().getMiddlePosition(), map().getMiddlePosition()));

        extractDeadzones(graphics, left, top, right, bottom);
        drawWhirlpools(graphics, mouseX, mouseY);

        int node = effectNode();
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

                if (!fogged(entry.position()) && mouseX >= nx - half && mouseX <= nx + half && mouseY >= ny - half && mouseY <= ny + half) {
                    hoverX = mouseX;
                    hoverY = mouseY;
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

            if (!fogged(entry.position()) && mouseX >= nx - half && mouseX <= nx + half && mouseY >= ny - half && mouseY <= ny + half) {
                hoverX = mouseX;
                hoverY = mouseY;
                int cap = cauldron.levelCap(recipe);
                Component held = cap <= entry.effect().getStrength() && WorldEffects.tierVisible(notes, recipe, cap + 1)
                        ? WorldEffects.levelHint(recipe, cap + 1) : null;

                if (discovered) {
                    hoverName = entry.effect().getEffect().value().getDisplayName();
                    hoverSprite = recipe.sprite();
                    hoverDetail = held;
                } else {
                    hoverName = Component.translatable("alchemia.gui.unknown");
                    hoverDetail = held != null ? held : Component.translatable("alchemia.gui.unknown.hint");
                }
            }
        }

        drawLureFields(graphics);
        graphics.nextStratum();
        drawFog(graphics, left, top, right, bottom);
        graphics.nextStratum();
        drawMapGrid(graphics, left, top, right, bottom);
        drawMapEdge(graphics, left, top, right, bottom);
        drawLandingPulse(graphics);

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
            drawBrewMarker(graphics, (int) Math.round(markerX), (int) Math.round(markerY), cauldron, markerScale, markerSpin);
            drawBrewPuff(graphics, (int) Math.round(markerX), (int) Math.round(markerY));
        }

        graphics.disableScissor();
    }

    private void drawMapPaper(GuiGraphicsExtractor graphics, int left, int top, int right, int bottom) {
        int tile = CauldronArt.MAP_TILE;
        long firstX = (long) Math.floor(mapAtX(left) / tile);
        long lastX = (long) Math.floor(mapAtX(right) / tile);
        long firstY = (long) Math.floor(mapAtY(top) / tile);
        long lastY = (long) Math.floor(mapAtY(bottom) / tile);

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate((float) screenX(0.0), (float) screenY(0.0));
        pose.scale((float) zoom, (float) zoom);
        for (long row = firstY; row <= lastY; row++) {
            for (long column = firstX; column <= lastX; column++) {
                CauldronArt.mapPaper(graphics, (int) (column * tile), (int) (row * tile));
            }
        }
        pose.popMatrix();
    }

    private void drawMapGrid(GuiGraphicsExtractor graphics, int left, int top, int right, int bottom) {
        double fade = Mth.clamp((zoom - GRID_FADE_FROM) / (GRID_FADE_TO - GRID_FADE_FROM), 0.0, 1.0);
        if (fade <= 0.0) return;
        double spacing = GRID_BASE;
        while (spacing * zoom < GRID_MIN_PIXELS) spacing *= 2.0;
        double minor = Mth.clamp((spacing * zoom - GRID_MIN_PIXELS) / GRID_MIN_PIXELS, 0.0, 1.0);
        int phaseX = (int) Math.round(screenX(0.0));
        int phaseY = (int) Math.round(screenY(0.0));

        for (long line = (long) Math.floor(mapAtX(left) / spacing); line <= (long) Math.ceil(mapAtX(right) / spacing); line++) {
            int x = (int) Math.round(screenX(line * spacing));
            int alpha = gridAlpha(line, minor, fade);
            if (x < left || x >= right || alpha <= 0) continue;
            CauldronArt.dashDown(graphics, x, top, bottom, top - phaseY, ARGB.color(alpha, GRID_RGB));
        }
        for (long line = (long) Math.floor(mapAtY(top) / spacing); line <= (long) Math.ceil(mapAtY(bottom) / spacing); line++) {
            int y = (int) Math.round(screenY(line * spacing));
            int alpha = gridAlpha(line, minor, fade);
            if (y < top || y >= bottom || alpha <= 0) continue;
            CauldronArt.dashAcross(graphics, left, right, y, left - phaseX, ARGB.color(alpha, GRID_RGB));
        }
    }

    private static int gridAlpha(long line, double minor, double fade) {
        return (int) Math.round(GRID_ALPHA * fade * (Math.floorMod(line, 2L) == 0 ? 1.0 : minor));
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
            return;
        }

        if (stranded) {
            graphics.item(new ItemStack(Items.SKELETON_SKULL), tip.roundedX() - 8, tip.roundedY() - 8);
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

    private void drawBrewMarker(GuiGraphicsExtractor graphics, int x, int y, TileEntityAlchemyCauldron cauldron, double scale, double spin) {
        if (scale <= 0.01) return;

        long millis = Util.getMillis();
        double pulse = (Math.sin(millis / 380.0) + 1.0) * 0.5;
        int radius = (int) Math.round(NODE_BASE * 0.55 + pulse * 3.0);
        int alpha = (int) Math.round(70 + pulse * 90);

        double hot = heatIntensity();
        double tremble = boiledAt != 0L ? 1.4 : hot >= SHAKE_HEAT ? hot : 0.0;
        double amount = tremble < 0.05 ? 0.0 : tremble * 0.4;
        float shakeX = (float) ((Math.sin(millis * 0.11) + Math.sin(millis * 0.173 + 1.3)) * 0.5 * amount);
        float shakeY = (float) ((Math.sin(millis * 0.131 + 0.7) + Math.sin(millis * 0.191 + 2.1)) * 0.5 * amount);
        float wobble = (float) ((Math.sin(millis * 0.09) + Math.sin(millis * 0.141 + 0.9) * 0.5) / 1.5 * 0.11 * tremble);

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x + shakeX, y + shakeY);
        pose.rotate((float) spin);
        pose.scale((float) scale, (float) scale);
        float mapScale = (float) markerZoom();
        pose.scale(mapScale, mapScale);
        pose.translate(0.0F, SHAKE_PIVOT_Y);
        pose.rotate(wobble);
        pose.translate(0.0F, -SHAKE_PIVOT_Y);

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

    private void drawBrewPuff(GuiGraphicsExtractor graphics, int x, int y) {
        if (puffAt == 0L) return;

        long elapsed = Util.getMillis() - puffAt;
        if (elapsed >= PUFF_MS + PUFF_COUNT * PUFF_STAGGER_MS) {
            puffAt = 0L;
            return;
        }

        for (int index = 0; index < PUFF_COUNT; index++) {
            double t = (elapsed - index * PUFF_STAGGER_MS) / (double) PUFF_MS;
            if (t <= 0.0 || t >= 1.0) continue;

            double side = index % 2 == 0 ? 1.0 : -1.0;
            double drift = side * (1.0 + index * 0.7) * t * 5.0 + Math.sin(t * 5.0 + index) * 1.5;
            double rise = 4.0 + t * (14.0 + index * 2.0);
            double radius = 2.0 + t * (4.0 + index * 0.6);
            double fade = Math.pow(1.0 - t, 1.3) * Math.min(1.0, t * 8.0);
            int alpha = (int) Math.round(140 * fade);
            if (alpha < 3) continue;

            double mapScale = markerZoom();
            double cx = x + drift * mapScale;
            double cy = y - (8 + rise) * mapScale;
            radius *= mapScale;
            PathRenderer.disc(graphics, cx, cy, radius, ARGB.color(alpha, puffRgb));
            PathRenderer.disc(graphics, cx - radius * 0.2, cy - radius * 0.25, radius * 0.55, ARGB.color(alpha / 2, ARGB.opaque(lighten(puffRgb))));
        }
    }

    private static int lighten(int rgb) {
        int r = (rgb >> 16 & 0xFF) + (255 - (rgb >> 16 & 0xFF)) / 2;
        int g = (rgb >> 8 & 0xFF) + (255 - (rgb >> 8 & 0xFF)) / 2;
        int b = (rgb & 0xFF) + (255 - (rgb & 0xFF)) / 2;
        return r << 16 | g << 8 | b;
    }

    private int effectNode() {
        return Math.max(6, (int) Math.round(NODE_BASE * Mth.clamp(zoom, 0.55, 2.0)));
    }

    private double markerZoom() {
        return effectNode() / 16.0;
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
