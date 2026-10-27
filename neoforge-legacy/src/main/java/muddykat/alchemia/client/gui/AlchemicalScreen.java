package muddykat.alchemia.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import muddykat.alchemia.client.gui.cauldron.ActionButtons;
import muddykat.alchemia.client.gui.cauldron.BrewContext;
import muddykat.alchemia.client.gui.cauldron.BrewHints;
import muddykat.alchemia.client.gui.cauldron.BrewMarker;
import muddykat.alchemia.client.gui.cauldron.CauldronKnowledge;
import muddykat.alchemia.client.gui.cauldron.EffectNodes;
import muddykat.alchemia.client.gui.cauldron.HeatGauge;
import muddykat.alchemia.client.gui.cauldron.LandingPulse;
import muddykat.alchemia.client.gui.cauldron.LedgerPanel;
import muddykat.alchemia.client.gui.cauldron.LureLayer;
import muddykat.alchemia.client.gui.cauldron.MapCamera;
import muddykat.alchemia.client.gui.cauldron.MapFog;
import muddykat.alchemia.client.gui.cauldron.MapInk;
import muddykat.alchemia.client.gui.cauldron.MapPaper;
import muddykat.alchemia.client.gui.cauldron.MapSurface;
import muddykat.alchemia.client.gui.cauldron.MapView;
import muddykat.alchemia.client.gui.cauldron.MarkerFlight;
import muddykat.alchemia.client.gui.cauldron.NoticeScroll;
import muddykat.alchemia.client.gui.cauldron.QueueTimeline;
import muddykat.alchemia.client.gui.cauldron.RouteLayer;
import muddykat.alchemia.client.gui.cauldron.StirSync;
import muddykat.alchemia.client.gui.cauldron.TooltipQueue;
import muddykat.alchemia.client.gui.cauldron.WhirlpoolLayer;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.blocks.tileentity.container.AlchemicalCauldronMenu;
import muddykat.alchemia.common.config.Configuration;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.helper.IngredientTool;
import muddykat.alchemia.common.items.helper.Ingredients;
import muddykat.alchemia.common.items.helper.PathPoint;
import muddykat.alchemia.common.network.packets.PacketNameBrew;
import muddykat.alchemia.common.potion.BrewCourse;
import muddykat.alchemia.common.potion.BrewModifiers;
import muddykat.alchemia.common.potion.CauldronHeat;
import muddykat.alchemia.common.potion.PotionMap;
import muddykat.alchemia.common.potion.RecipeDiscovery;
import muddykat.alchemia.common.potion.VanillaIngredients;
import muddykat.alchemia.common.potion.Whirlpool;
import muddykat.alchemia.registration.registers.ItemRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import muddykat.alchemia.client.effect.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.Util;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.network.PacketDistributor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import javax.annotation.Nullable;

import java.util.List;

public class AlchemicalScreen extends AbstractContainerScreen<AlchemicalCauldronMenu> implements BrewContext {
    private static final int FOLIO_W = CauldronArt.FOLIO_W;
    private static final int FOLIO_H = CauldronArt.FOLIO_H;
    private static final int LEDGER_X = FOLIO_W + 2;
    private static final int LEDGER_Y = 10;
    private static final int TRAY_X = (FOLIO_W - CauldronArt.TRAY_W) / 2;
    private static final int TRAY_Y = FOLIO_H - CauldronArt.TRAY_TUCK;
    private static final int IMAGE_WIDTH = LEDGER_X + LedgerPanel.WIDTH;
    private static final int IMAGE_HEIGHT = TRAY_Y + CauldronArt.TRAY_H;

    private static final int VIEW_INSET_X = 20;
    private static final int VIEW_INSET_Y = 32;
    private static final int VIEW_WIDTH = 328;
    private static final int VIEW_HEIGHT = 146;
    private static final int HEADER_Y = 15;
    private static final int HEADER_RIGHT = VIEW_INSET_X + VIEW_WIDTH;

    private static final int PREP_Y = AlchemicalCauldronMenu.SLOT_ROW_Y;
    private static final int WORK_X = 62;
    private static final int WORK_W = 80;
    private static final int ADD_X = 146;
    private static final int ADD_W = 84;
    private static final int INFO_X = 236;
    private static final int BUTTON_H = ActionButtons.HEIGHT;

    private static final int TIMELINE_X = 18;
    private static final int TIMELINE_Y = 206;
    private static final int STIR_X = 240;
    private static final int HEAT_X = 296;
    private static final int ACTION_W = 54;

    private static final int PRESS_STIR = 0;
    private static final int PRESS_HEAT = 1;
    private static final int PRESS_ADD = 2;
    private static final int PRESS_WORK = 3;
    private static final double HEAT_STIR_BONUS = 1.5;

    private static final int INK = CauldronArt.INK;
    private static final int INK_SOFT = CauldronArt.INK_SOFT;
    private static final int INK_FAINT = CauldronArt.INK_FAINT;
    private static final int NAME_SELECTION = 0x803B2A1E;
    private static final int FRAME_OUTER = 0xFF967E5C;
    private static final int FRAME_INNER = 0xFF604A34;
    private static final int FRAME_SHADOW = 0x2A3B2A1E;
    private static final int TRAY_SHADOW = 0x50120E0D;

    private static final int SCREEN_MARGIN = 8;

    private static final int NAME_X = 34;
    private static final int NAME_Y = HEADER_Y - 3;
    private static final int NAME_W = 160;
    private static final int NAME_H = 14;
    private static final int NAME_TEXT_Y = 3;

    private static boolean deskInReach;

    private float uiScale = 1.0F;

    private EditBox nameField;
    private final InkTextField nameText = new InkTextField(NAME_W - 4, INK, INK_FAINT, NAME_SELECTION);
    private boolean panning;
    private boolean applying;
    private int applyTicks;
    private boolean stirring;
    private double stirCredit;
    private double whirlTurn;
    private int waterSeen = -1;
    private double workShown;
    private @Nullable InkTooltip mapHover;
    private int hoverX;
    private int hoverY;

    private final CauldronKnowledge knowledge = new CauldronKnowledge();
    private final StirSync stirs = new StirSync();
    private final MapCamera camera = new MapCamera(this::map);
    private final MarkerFlight flight = new MarkerFlight(this);
    private final NoticeScroll notices = new NoticeScroll();
    private final ActionButtons buttons = new ActionButtons();
    private final TooltipQueue tooltips = new TooltipQueue();
    private final HeatGauge heatGauge = new HeatGauge();
    private final BrewMarker brewMarker = new BrewMarker();
    private final MapFog fog = new MapFog();
    private final MapSurface surface = new MapSurface();
    private final LandingPulse landing = new LandingPulse();
    private final LedgerPanel ledger = new LedgerPanel(this, heatGauge, landing, tooltips);
    private final LureLayer lures = new LureLayer(this, this::uiSound);
    private final RouteLayer routes = new RouteLayer(this, lures);
    private final QueueTimeline timeline = new QueueTimeline(this, lures, tooltips);
    private final EffectNodes nodes = new EffectNodes(this);

    public AlchemicalScreen(AlchemicalCauldronMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = IMAGE_WIDTH;
        this.imageHeight = IMAGE_HEIGHT;
    }

    public static boolean readingAtDesk() {
        return deskInReach;
    }

    @Override
    public TileEntityAlchemyCauldron cauldron() {
        return this.getMenu().getCauldron();
    }

    @Override
    public PotionMap map() {
        return PotionMap.get(cauldron().getBase());
    }

    @Override
    public BrewCourse course() {
        return stirs.course();
    }

    @Override
    public PathPoint brewAt() {
        return stirs.brewAt();
    }

    @Override
    public CauldronKnowledge knowledge() {
        return knowledge;
    }

    @Override
    public MapView view() {
        return camera.view();
    }

    @Override
    public ItemStack pendingIngredient() {
        return this.getMenu().getSlot(TileEntityAlchemyCauldron.ACTIVE_SLOT).getItem();
    }

    @Override
    public net.minecraft.client.gui.Font getFont() {
        return this.font;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        resetView();
    }

    @Override
    protected void init() {
        super.init();

        this.uiScale = snappedScale();
        this.leftPos = Math.round((this.width / this.uiScale - this.imageWidth) / 2.0F);
        this.topPos = Math.round((this.height / this.uiScale - this.imageHeight) / 2.0F);
        camera.place(this.leftPos + VIEW_INSET_X, this.topPos + VIEW_INSET_Y, VIEW_WIDTH, VIEW_HEIGHT);
        timeline.place(this.leftPos + TIMELINE_X, this.topPos + TIMELINE_Y,
                this.leftPos + AlchemicalCauldronMenu.ACTIVE_SLOT_X, this.topPos + PREP_Y);

        String existing = cauldron().getBrewName();
        this.nameField = new EditBox(this.font, this.leftPos + NAME_X + 2, this.topPos + NAME_Y + 2,
                NAME_W - 4, NAME_H - 4, Component.translatable("alchemia.gui.name")) {
            @Override
            public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float a) {
            }
        };
        this.nameField.setBordered(false);
        this.nameField.setMaxLength(PacketNameBrew.MAX_LENGTH);
        this.nameField.setTextColor(INK);
        this.nameField.setValue(existing);
        this.nameField.setHint(Component.translatable("alchemia.gui.name.hint"));
        this.nameField.setResponder(value -> PacketDistributor.sendToServer(new PacketNameBrew(value)));
        addRenderableWidget(this.nameField);

        stirs.reset(cauldron().getStirCount());
        this.stirring = false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.nameField != null && this.nameField.isFocused()) {
            if (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER) {
                releaseNameFocus();
                return true;
            }
            if (!(keyCode == InputConstants.KEY_ESCAPE) && (this.nameField.keyPressed(keyCode, scanCode, modifiers) || this.nameField.canConsumeInput())) return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private float snappedScale() {
        float fit = Math.min((float) this.width / (this.imageWidth + SCREEN_MARGIN),
                (float) this.height / (this.imageHeight + SCREEN_MARGIN));
        if (fit >= 1.0F) return 1.0F;

        int guiScale = this.minecraft == null ? 1 : (int) this.minecraft.getWindow().getGuiScale();
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
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    private double toGui(double value) {
        return value / this.uiScale;
    }

    private void resetView() {
        camera.reset(cauldron().getBrewPosition());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        double guiX = toGui(mouseX);
        double guiY = toGui(mouseY);
        if (scrollY == 0 || !overMap(guiX, guiY)) return super.mouseScrolled(guiX, guiY, scrollX, scrollY);

        camera.scroll(guiX, guiY, scrollY);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (panning && button == InputConstants.MOUSE_BUTTON_LEFT) {
            camera.drag(toGui(dx), toGui(dy));
            return true;
        }
        if (this.nameField != null && this.nameField.isFocused() && button == InputConstants.MOUSE_BUTTON_LEFT) {
            this.nameField.mouseDragged(toGui(mouseX), toGui(mouseY), button, toGui(dx), toGui(dy));
            this.nameField.moveCursorTo(nameIndexAt(toGui(mouseX)), true);
            return true;
        }
        return super.mouseDragged(toGui(mouseX), toGui(mouseY), button, toGui(dx), toGui(dy));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double guiX = toGui(mouseX);
        double guiY = toGui(mouseY);

        if (!overNameField(guiX, guiY)) {
            releaseNameFocus();
        } else if (this.nameField != null && button == InputConstants.MOUSE_BUTTON_LEFT) {
            this.setFocused(this.nameField);
            this.nameField.setFocused(true);
            this.nameField.mouseClicked(guiX, guiY, button);
            this.nameField.moveCursorTo(nameIndexAt(guiX), hasShiftDown());
            return true;
        }

        if (button == InputConstants.MOUSE_BUTTON_LEFT && notices.held(guiX, guiY)) {
            notices.toggle();
            return true;
        }

        if (button == InputConstants.MOUSE_BUTTON_LEFT && overHeatButton(guiX, guiY)) {
            if (canHeat()) {
                buttons.press(PRESS_HEAT);
                stoke();
            }
            return true;
        }

        if (button == InputConstants.MOUSE_BUTTON_LEFT && overStirButton(guiX, guiY)) {
            if (canStir()) {
                buttons.press(PRESS_STIR);
                stirring = true;
                if (!flight.inFlight()) sendStir(1);
            }
            return true;
        }

        if (button == InputConstants.MOUSE_BUTTON_LEFT && overAddButton(guiX, guiY)) {
            if (canAdd()) {
                buttons.press(PRESS_ADD);
                sendButton(AlchemicalCauldronMenu.BUTTON_ADD);
            }
            return true;
        }

        if (button == InputConstants.MOUSE_BUTTON_LEFT && overToolButton(guiX, guiY)) {
            if (canApplyTool()) {
                buttons.press(PRESS_WORK);
                applying = true;
                applyTicks = 0;
                sendButton(applyButton());
            }
            return true;
        }

        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            int cell = timeline.cellAt(guiX, guiY);
            if (cell >= 0) {
                sendButton(AlchemicalCauldronMenu.BUTTON_UNQUEUE + cell);
                return true;
            }
        }

        if (button == InputConstants.MOUSE_BUTTON_RIGHT && overMap(guiX, guiY)) {
            resetView();
            return true;
        }

        if (button == InputConstants.MOUSE_BUTTON_LEFT && overMap(guiX, guiY)) {
            panning = true;
        }

        return super.mouseClicked(guiX, guiY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            panning = false;
            applying = false;
            stirring = false;
        }
        return super.mouseReleased(toGui(mouseX), toGui(mouseY), button);
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        if (stirring) {
            if (!canStir()) {
                stirring = false;
                stirCredit = 0.0;
            } else if (!course().teleporting(map()) && !flight.inFlight()) {
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
        return camera.contains(mouseX, mouseY);
    }

    private boolean overNameField(double mouseX, double mouseY) {
        return over(mouseX, mouseY, NAME_X, NAME_Y, NAME_W, NAME_H);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float a) {
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float a, int mouseX, int mouseY) {
    }

    private void drawFolio(GuiGraphics graphics, int mouseX, int mouseY, float a) {

        if (!camera.ready()) resetView();
        int water = cauldron().getWaterLevel();
        if (water > 0 && waterSeen <= 0) resetView();
        waterSeen = water;
        tickHeat();
        stirs.reconcile(cauldron());
        flight.track(Util.getMillis());
        nodes.trackGlow(flight.at(), flight.frameMs());
        flight.steer(camera, stirring);
        camera.tick();

        refreshKnowledge();
        knowledge.announce(notices::queue);
        mapHover = null;
        tooltips.clear();

        int guiMouseX = (int) Math.round(toGui(mouseX));
        int guiMouseY = (int) Math.round(toGui(mouseY));

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.scale(this.uiScale, this.uiScale, 1.0F);

        int ledgerH = LedgerPanel.height();
        graphics.fill(this.leftPos + TRAY_X + 2, this.topPos + TRAY_Y + 3,
                this.leftPos + TRAY_X + CauldronArt.TRAY_W + 2, this.topPos + TRAY_Y + CauldronArt.TRAY_H + 2, TRAY_SHADOW);
        CauldronArt.tray(graphics, this.leftPos + TRAY_X, this.topPos + TRAY_Y);
        CauldronArt.folio(graphics, this.leftPos, this.topPos);

        extractMap(graphics, guiMouseX, guiMouseY);
        extractMapFrame(graphics);
        extractHeader(graphics, guiMouseX, guiMouseY);
        extractPrep(graphics, guiMouseX, guiMouseY);
        timeline.draw(graphics, guiMouseX, guiMouseY);
        extractStirButton(graphics, guiMouseX, guiMouseY);
        extractHeatButton(graphics, guiMouseX, guiMouseY);
        ledger.draw(graphics, this.leftPos + LEDGER_X, this.topPos + LEDGER_Y, heatCap(), guiMouseX, guiMouseY);
        notices.draw(graphics, this.font, this.leftPos + LEDGER_X, this.topPos + LEDGER_Y + ledgerH + NoticeScroll.GAP,
                LedgerPanel.SHEET_W, guiMouseX, guiMouseY);

        pose.popPose();
    }

    @Override
    public void removed() {
        super.removed();
        deskInReach = false;
        surface.close();
    }

    private void refreshKnowledge() {
        knowledge.refresh(this.minecraft == null ? null : this.minecraft.player, cauldron());
        deskInReach = knowledge.deskInReach();
    }

    private void sendStir(int units) {
        stirs.send(units, ClientIngredientPathTooltip.shiftDown(), this::sendButton);
    }

    private boolean canStir() {
        TileEntityAlchemyCauldron cauldron = cauldron();
        return cauldron.getWaterLevel() > 0 && !cauldron.isSpoiled() && PotionMap.isReady() && course().canStir();
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

    private void updateFog() {
        if (!Configuration.fogOfWar()) {
            fog.forget();
            return;
        }
        fog.track(map(), brewAt(), cauldron().getBase(), knowledge.guides(), Util.getMillis());
    }

    private boolean fogged(PathPoint at) {
        return fog.hides(at, Util.getMillis());
    }

    private void extractStirButton(GuiGraphics graphics, int mouseX, int mouseY) {
        boolean ready = canStir();
        boolean hovered = overStirButton(mouseX, mouseY);
        boolean down = buttons.down(PRESS_STIR, stirring);
        int x = this.leftPos + STIR_X;
        int y = this.topPos + TIMELINE_Y;

        ActionButtons.face(graphics, x, y, ACTION_W, ready, hovered, down, false);
        ActionButtons.label(graphics, this.font, x, y, ACTION_W, ready, down, false, CauldronArt.Icon.STIR,
                Component.translatable("alchemia.gui.stir"));

        if (hovered) {
            tooltips.ink(InkTooltip.of(Component.translatable("alchemia.gui.stir"), List.of(Component.translatable(ready
                    ? "alchemia.gui.stir.hint" : "alchemia.gui.stir.idle"))), mouseX, mouseY);
        }
    }

    private boolean canHeat() {
        TileEntityAlchemyCauldron cauldron = cauldron();
        return cauldron.getWaterLevel() > 0 && !cauldron.isSpoiled() && PotionMap.isReady();
    }

    private void stoke() {
        if (heatGauge.boiling()) return;

        sendButton(AlchemicalCauldronMenu.BUTTON_HEAT);
        long now = Util.getMillis();
        if (heatGauge.stoke(now, heatCap(), BrewHints.ready(this), this::uiSound) != HeatGauge.Stoke.BOILED) return;

        TileEntityAlchemyCauldron cauldron = cauldron();
        PotionMap.PotionEffectPosition found = cauldron.effectAt(brewAt());
        brewMarker.puff(found.getEffect() != null ? found.getEffect().value().getColor() : cauldron.getPotionColor(), now);
    }

    private void tickHeat() {
        long now = Util.getMillis();
        ContainerData data = this.getMenu().getCauldronData();
        double server = (double) data.get(TileEntityAlchemyCauldron.DATA_HEAT) / CauldronHeat.SYNC_SCALE;
        boolean serverBoil = data.get(TileEntityAlchemyCauldron.DATA_BOIL) > 0;
        if (heatGauge.tick(now, canHeat(), heatCap(), server, serverBoil, this::uiSound)) {
            brewMarker.puff(cauldron().getPotionColor(), now);
        }
        whirlTurn = (whirlTurn + Whirlpool.SPIN * heatIntensity() * heatGauge.lastDt()) % (2.0 * Math.PI * 1000.0);
    }

    private double heatCap() {
        return CauldronHeat.cap(cauldron().getWaterLevel());
    }

    private double heatIntensity() {
        return heatGauge.intensity(heatCap());
    }

    private void uiSound(SoundEvent sound, float pitch, float volume) {
        if (this.minecraft == null) return;
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
    }

    private void extractHeatButton(GuiGraphics graphics, int mouseX, int mouseY) {
        boolean usable = canHeat();
        boolean ready = usable && BrewHints.ready(this);
        boolean hovered = overHeatButton(mouseX, mouseY);
        boolean down = buttons.down(PRESS_HEAT, false);
        int x = this.leftPos + HEAT_X;
        int y = this.topPos + TIMELINE_Y;

        ActionButtons.face(graphics, x, y, ACTION_W, usable, hovered, down, ready);

        if (ready && !down) {
            double phase = (Util.getMillis() % 2600L) / 2600.0;
            if (phase < 0.4) {
                int sweep = x + (int) Math.round((ACTION_W + 10) * phase / 0.4) - 6;
                GuiDraw.scissor(graphics, x + 2, y + 2, x + ACTION_W - 2, y + BUTTON_H - 2);
                graphics.fill(sweep, y + 2, sweep + 3, y + BUTTON_H - 2, ARGB.color(70, 0xFFFFFF));
                graphics.fill(sweep + 3, y + 2, sweep + 4, y + BUTTON_H - 2, ARGB.color(35, 0xFFFFFF));
                graphics.disableScissor();
            }
        }

        ActionButtons.label(graphics, this.font, x, y, ACTION_W, usable, down, ready, CauldronArt.Icon.BREW,
                Component.translatable("alchemia.gui.heat"));

        if (hovered) {
            tooltips.ink(InkTooltip.of(Component.translatable("alchemia.gui.heat"),
                    List.of(Component.translatable(usable ? "alchemia.gui.heat.hint" : "alchemia.gui.heat.idle"))), mouseX, mouseY);
        }
    }

    private static int applyButton() {
        return ClientIngredientPathTooltip.shiftDown() ? AlchemicalCauldronMenu.BUTTON_APPLY_TOOL_FAST : AlchemicalCauldronMenu.BUTTON_APPLY_TOOL;
    }

    private void sendButton(int buttonId) {
        if (this.minecraft == null || this.minecraft.gameMode == null) return;
        this.minecraft.gameMode.handleInventoryButtonClick(this.getMenu().containerId, buttonId);
    }

    private boolean canAdd() {
        return cauldron().canQueue();
    }

    private boolean canApplyTool() {
        return cauldron().canApplyTool();
    }

    private IngredientTool heldTool() {
        return IngredientTool.of(cauldron().getTool());
    }

    private void extractPrep(GuiGraphics graphics, int mouseX, int mouseY) {
        TileEntityAlchemyCauldron cauldron = cauldron();
        ItemStack active = pendingIngredient();
        ItemStack held = cauldron.getTool();
        IngredientTool tool = IngredientTool.of(held);
        int y = this.topPos + PREP_Y;

        int ingredientX = this.leftPos + AlchemicalCauldronMenu.ACTIVE_SLOT_X;
        int toolX = this.leftPos + AlchemicalCauldronMenu.TOOL_SLOT_X;
        CauldronArt.cell(graphics, ingredientX, y, active.isEmpty() ? CauldronArt.Cell.PLAIN : CauldronArt.Cell.SELECTED);
        CauldronArt.cell(graphics, toolX, y, held.isEmpty() ? CauldronArt.Cell.PLAIN : CauldronArt.Cell.SELECTED);
        if (active.isEmpty()) GuideInk.detailedItem(graphics,
                new ItemStack(ItemRegistry.getItemFromRegistry(Ingredients.Druids_Rosemary.getRegistryName())),
                ingredientX + 1, y + 1, ARGB.color(170, 0xFFFFFF));
        if (held.isEmpty()) GuideInk.detailedItem(graphics,
                new ItemStack(ItemRegistry.getItemFromRegistry("mortar_and_pestle")), toolX + 1, y + 1, GuideInk.PLAIN);

        extractWorkButton(graphics, tool, active, mouseX, mouseY);
        extractAddButton(graphics, mouseX, mouseY);
        extractPrepInfo(graphics, active, this.leftPos + INFO_X, y);
    }

    private void extractWorkButton(GuiGraphics graphics, @Nullable IngredientTool tool, ItemStack active, int mouseX, int mouseY) {
        boolean ready = canApplyTool();
        boolean hovered = overToolButton(mouseX, mouseY);
        boolean down = buttons.down(PRESS_WORK, applying);
        int x = this.leftPos + WORK_X;
        int y = this.topPos + PREP_Y;

        double target = tool != null && TileEntityAlchemyCauldron.isBrewingInput(active) ? Mth.clamp(tool.progress(active), 0.0F, 1.0F) : 0.0;
        workShown = Math.abs(target - workShown) < 0.002 ? target : Mth.lerp(0.3, workShown, target);

        ActionButtons.face(graphics, x, y, WORK_W, ready || workShown > 0.0, hovered && ready, down, false);
        int fill = (int) Math.round((WORK_W - 4) * workShown);
        if (fill >= 4) {
            int sink = down && ready ? 1 : 0;
            GuiDraw.sprite(graphics, CauldronArt.WORK_FILL, x + 2, y + 2 + sink, fill, BUTTON_H - 4 - sink);
        }

        Component label = Component.translatable(tool == null ? "alchemia.gui.tool.missing" : tool.actionKey());
        ActionButtons.label(graphics, this.font, x, y, WORK_W, ready || workShown > 0.0, down, false, null, label);

        if (!hovered) return;
        ItemStack heldTool = cauldron().getTool();
        InkTooltip tip = tool == null
                ? InkTooltip.of(Component.translatable("alchemia.gui.tool.title"), List.of(Component.translatable("alchemia.gui.tool.needs")))
                : InkTooltip.of(heldTool.getHoverName(), List.of(Component.translatable("alchemia.gui.tool.hint"))).withIcon(heldTool);
        tooltips.ink(tip, mouseX, mouseY);
    }

    private void extractAddButton(GuiGraphics graphics, int mouseX, int mouseY) {
        boolean ready = canAdd();
        boolean hovered = overAddButton(mouseX, mouseY);
        boolean down = buttons.down(PRESS_ADD, false);
        int x = this.leftPos + ADD_X;
        int y = this.topPos + PREP_Y;

        ActionButtons.face(graphics, x, y, ADD_W, ready, hovered, down, false);
        ActionButtons.label(graphics, this.font, x, y, ADD_W, ready, down, false, null, Component.translatable("alchemia.gui.add"));
    }

    private void extractPrepInfo(GuiGraphics graphics, ItemStack active, int x, int y) {
        int width = this.leftPos + HEADER_RIGHT - x;
        if (active.isEmpty()) {
            graphics.drawString(this.font, CauldronArt.clip(this.font, Component.translatable("alchemia.gui.prep.empty"), width), x, y + 1, INK_FAINT, false);
            graphics.drawString(this.font, CauldronArt.clip(this.font, Component.translatable("alchemia.gui.prep.empty.hint"), width), x, y + 10, INK_FAINT, false);
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
        graphics.drawString(this.font, CauldronArt.clip(this.font, name, width), x, y + 1, INK, false);
        graphics.drawString(this.font, CauldronArt.clip(this.font, detail, width), x, y + 10, INK_FAINT, false);
    }

    private void extractHeader(GuiGraphics graphics, int mouseX, int mouseY) {
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
        Component recipes = Component.translatable("alchemia.gui.recipes", knowledge.known().size(), RecipeDiscovery.total());
        int recipesX = right - this.font.width(recipes);
        graphics.drawString(this.font, recipes, recipesX, y, INK_SOFT, false);
        CauldronArt.icon(graphics, CauldronArt.Icon.EYE, recipesX - CauldronArt.ICON - 3, y - 2, false);

        Component zoomLabel = Component.translatable("alchemia.gui.zoom", Math.round(camera.zoom() * 100));
        int diamondX = recipesX - CauldronArt.ICON - 12;
        CauldronArt.diamond(graphics, diamondX, y + 1);
        graphics.drawString(this.font, zoomLabel, diamondX - 5 - this.font.width(zoomLabel), y, INK_FAINT, false);
    }

    private void extractMapFrame(GuiGraphics graphics) {
        int x0 = this.leftPos + VIEW_INSET_X;
        int y0 = this.topPos + VIEW_INSET_Y;
        int x1 = x0 + VIEW_WIDTH;
        int y1 = y0 + VIEW_HEIGHT;

        graphics.fill(x0, y0, x1, y0 + 1, FRAME_SHADOW);
        graphics.fill(x0, y0 + 1, x0 + 1, y1, FRAME_SHADOW);
        graphics.renderOutline(x0 - 1, y0 - 1, VIEW_WIDTH + 2, VIEW_HEIGHT + 2, FRAME_INNER);
        graphics.renderOutline(x0 - 2, y0 - 2, VIEW_WIDTH + 4, VIEW_HEIGHT + 4, ARGB.color(110, FRAME_OUTER & 0xFFFFFF));

        int half = CauldronArt.DIAMOND / 2;
        CauldronArt.diamond(graphics, x0 - 2 - half, y0 - 2 - half);
        CauldronArt.diamond(graphics, x1 + 1 - half, y0 - 2 - half);
        CauldronArt.diamond(graphics, x0 - 2 - half, y1 + 1 - half);
        CauldronArt.diamond(graphics, x1 + 1 - half, y1 + 1 - half);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.hoveredSlot == null || !this.hoveredSlot.hasItem()) return;
        ItemStack item = this.hoveredSlot.getItem();
        if (!this.menu.getCarried().isEmpty()) return;

        int guiX = (int) Math.round(toGui(mouseX));
        int guiY = (int) Math.round(toGui(mouseY));
        List<ClientTooltipComponent> lines = ClientHooks.gatherTooltipComponents(item, this.getTooltipFromContainerItem(item),
                item.getTooltipImage(), guiX, scaledWidth(), scaledHeight(), this.font);
        tooltips.queue(lines, guiX, guiY, null, true, item);
    }

    private int scaledWidth() {
        return (int) Math.floor(this.width / this.uiScale);
    }

    private int scaledHeight() {
        return (int) Math.floor(this.height / this.uiScale);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float a) {
        super.renderBackground(graphics, mouseX, mouseY, a);
        drawFolio(graphics, mouseX, mouseY, a);

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.scale(this.uiScale, this.uiScale, 1.0F);
        super.render(graphics, (int) Math.round(toGui(mouseX)), (int) Math.round(toGui(mouseY)), a);
        timeline.drawFlying(graphics);
        pose.popPose();

        renderTooltip(graphics, mouseX, mouseY);

        if (mapHover != null) tooltips.ink(mapHover, hoverX, hoverY);
        tooltips.draw(graphics, this.font, this.uiScale, scaledWidth(), scaledHeight());
    }

    private void drawNameText(GuiGraphics graphics) {
        if (this.nameField == null) return;
        nameText.draw(graphics, this.font, this.nameField, Component.translatable("alchemia.gui.name.hint"),
                nameTextX(), this.topPos + NAME_Y + NAME_TEXT_Y);
    }

    private int nameTextX() {
        return this.leftPos + NAME_X + 2;
    }

    private int nameIndexAt(double guiX) {
        return nameText.indexAt(this.font, this.nameField, guiX, nameTextX());
    }

    private void extractMap(GuiGraphics graphics, int mouseX, int mouseY) {
        TileEntityAlchemyCauldron cauldron = cauldron();
        MapView view = camera.view();

        GuiDraw.scissor(graphics, view.left(), view.top(), view.right(), view.bottom());
        MapPaper.paper(graphics, view);
        GuiDraw.nextStratum(graphics);

        if (!PotionMap.isReady() || cauldron.getWaterLevel() <= 0) {
            MapPaper.grid(graphics, view);
            GuiDraw.nextStratum(graphics);
            graphics.disableScissor();
            return;
        }

        PotionMap map = map();
        updateFog();
        surface.update(view, map, fog, Util.getMillis());
        PathPoint centre = view.toScreen(new PathPoint(map.getMiddlePosition(), map.getMiddlePosition()));

        surface.drawDeadSpace(graphics);
        WhirlpoolLayer.Hover whirl = WhirlpoolLayer.draw(graphics, view, map, cauldron.getBase(), brewAt(), knowledge.whirlExits(),
                whirlTurn, view.pathScale(), this::fogged, mouseX, mouseY);
        if (whirl != null) mapHover = InkTooltip.of(whirl.name(), List.of(whirl.detail()));

        InkTooltip nodeHover = nodes.draw(graphics, centre, this::fogged, mouseX, mouseY);
        if (nodeHover != null) mapHover = nodeHover;
        hoverX = mouseX;
        hoverY = mouseY;

        lures.drawFields(graphics);
        GuiDraw.nextStratum(graphics);
        surface.drawEdge(graphics);
        GuiDraw.nextStratum(graphics);
        surface.drawFog(graphics);
        GuiDraw.nextStratum(graphics);
        MapPaper.grid(graphics, view);
        landing.draw(graphics, view);

        int node = view.effectNode();
        PathRenderer.marker(graphics, centre.x(), centre.y(), Math.max(4, node / 2), MapInk.GHOST_RGB);

        routes.drawPlan(graphics, node);
        routes.drawPending(graphics, node);
        flight.drawTrail(graphics, view);

        MarkerFlight.Pose pose = flight.pose();
        double markerX = view.screenX(pose.x());
        double markerY = view.screenY(pose.y());

        if (markerX >= view.left() && markerX <= view.right() && markerY >= view.top() && markerY <= view.bottom()) {
            long millis = Util.getMillis();
            brewMarker.draw(graphics, (int) Math.round(markerX), (int) Math.round(markerY), cauldron, MapView.NODE_BASE,
                    pose.scale(), pose.spin(), view.markerZoom(), heatIntensity(), heatGauge.boiling(),
                    cauldron.isSplash() || splashPending(cauldron), millis);
            brewMarker.drawPuff(graphics, (int) Math.round(markerX), (int) Math.round(markerY), view.markerZoom(), millis);
        }

        graphics.disableScissor();
    }

    private boolean splashPending(TileEntityAlchemyCauldron cauldron) {
        return BrewModifiers.of(pendingIngredient()) == BrewModifiers.GUNPOWDER
                && cauldron.modifierRefusal(BrewModifiers.GUNPOWDER) == null;
    }
}
