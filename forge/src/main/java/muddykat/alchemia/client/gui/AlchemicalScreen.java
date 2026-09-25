package muddykat.alchemia.client.gui;

import net.minecraft.Util;
import com.mojang.blaze3d.platform.InputConstants;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.blocks.tileentity.container.AlchemicalCauldronMenu;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.items.helper.PathGeometry;
import muddykat.alchemia.common.items.helper.PathPoint;
import muddykat.alchemia.common.potion.PotionEnum;
import muddykat.alchemia.common.potion.PotionMap;
import muddykat.alchemia.common.potion.VanillaIngredients;
import muddykat.alchemia.common.potion.RecipeDiscovery;
import muddykat.alchemia.common.network.packets.PacketNameBrew;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import muddykat.alchemia.common.network.NetworkHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potions;
import muddykat.alchemia.common.utility.PotionStacks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AlchemicalScreen extends AbstractContainerScreen<AlchemicalCauldronMenu> {
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(Alchemia.MODID, "textures/book/images/book.png");
    private static final ResourceLocation FRAME_TEXTURE = new ResourceLocation(Alchemia.MODID, "textures/book/images/book_frame.png");

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

    private static final ResourceLocation SYMBOLS_TEXTURE = new ResourceLocation(Alchemia.MODID, "textures/book/images/symbols.png");
    private static final int SYMBOLS_W = 21;
    private static final int SYMBOLS_H = 45;
    private static final int DUST_U = 0;
    private static final int DUST_V = 16;
    private static final int DUST_W = 12;
    private static final int DUST_H = 11;
    private static final int UNKNOWN_U = 14;
    private static final int UNKNOWN_V = 16;
    private static final int UNKNOWN_W = 7;
    private static final int UNKNOWN_H = 11;
    private static final int UNKNOWN_CELL = 16;
    private static final int GHOST_ALPHA = 0x80FFFFFF;
    private static final int POTENCY_PER_ROW = 5;
    private static final int POTENCY_CELL = 18;
    private static final int INSTABILITY_BLOCK_H = 30;
    private static final int LINE_HEIGHT = 10;
    private static final int RUINED_TEXT_Y = 46;

    private static final int SLOT_ROW_X = 101;
    private static final int SLOT_ROW_Y = 181;
    private static final int INVENTORY_X = 74;
    private static final int INVENTORY_Y = 222;
    private static final int HOTBAR_Y = 280;

    private static final int BUTTON_X = 213;
    private static final int BUTTON_Y = 181;
    private static final int BUTTON_W = 72;
    private static final int BUTTON_H = 18;

    private static final int GHOST_RGB = 0x2B2015;
    private static final int BLOCKED_RGB = 0x6B2418;
    private static final int GAINED_RGB = 0x57C24A;
    private static final int EDGE_FILL = 0x662A4A8E;
    private static final int EDGE_LINE = 0xAA3C63B4;
    private static final int AURA_UNKNOWN_RGB = 0x7A6A50;
    private static final int AURA_CORE_ALPHA = 96;
    private static final int AURA_DASH = 2;

    private static final double MAP_OVERSCAN = 4 * PotionMap.CELL;
    private static final int PATH_THICKNESS = 2;

    private static final double MIN_ZOOM = 0.1;
    private static final double MAX_ZOOM = 3.0;
    private static final double ZOOM_STEP = 1.18;
    private static final double LABEL_ZOOM = 1.2;

    private static final int INK = 0xFF4A3A28;
    private static final int NAME_SELECTION = 0x804A3A28;
    private static final int INK_FAINT = 0xFF7A6A52;
    private static final int LINE_RGB = 0x282828;
    private static final int UNKNOWN_TINT = 0xFF7A6A52;
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

    private EditBox nameField;
    private boolean panning;
    private Set<PotionEnum> known = Set.of();
    private Component hoverName;
    private Component hoverDetail;
    private int hoverX;
    private int hoverY;

    public AlchemicalScreen(AlchemicalCauldronMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = IMAGE_WIDTH + EFFECTS_GAP + EFFECTS_W;
        this.imageHeight = IMAGE_HEIGHT;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void resize(net.minecraft.client.Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
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
            public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            }
        };
        this.nameField.setBordered(false);
        this.nameField.setMaxLength(PacketNameBrew.MAX_LENGTH);
        this.nameField.setTextColor(INK);
        this.nameField.setValue(existing);
        this.nameField.setHint(Component.translatable("alchemia.gui.name.hint"));
        this.nameField.setResponder(value -> NetworkHandler.sendToServer(new PacketNameBrew(value)));
        addRenderableWidget(this.nameField);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.nameField != null && this.nameField.isFocused()) {
            if (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER) {
                releaseNameFocus();
                return true;
            }
            if (keyCode != InputConstants.KEY_ESCAPE
                    && (this.nameField.keyPressed(keyCode, scanCode, modifiers) || this.nameField.canConsumeInput())) return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void releaseNameFocus() {
        if (this.nameField == null) return;
        this.nameField.setFocused(false);
        this.setFocused(null);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    private double toGui(double value) {
        return value / this.uiScale;
    }

    private int toScreen(double value) {
        return Math.round((float) value * this.uiScale);
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
        TileEntityAlchemyCauldron cauldron = this.getMenu().getCauldron();
        camX = cauldron.getXAlignment();
        camY = cauldron.getYAlignment();
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
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        double guiX = toGui(mouseX);
        double guiY = toGui(mouseY);
        if (scrollY == 0 || !overMap(guiX, guiY)) return super.mouseScrolled(guiX, guiY, scrollY);

        anchorScreenX = guiX;
        anchorScreenY = guiY;
        anchorWorldX = mapAtX(guiX);
        anchorWorldY = mapAtY(guiY);

        double factor = scrollY > 0 ? ZOOM_STEP : 1.0 / ZOOM_STEP;
        targetZoom = Mth.clamp(targetZoom * factor, MIN_ZOOM, MAX_ZOOM);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (panning && button == InputConstants.MOUSE_BUTTON_LEFT) {
            camX -= toGui(dx) / zoom;
            camY -= toGui(dy) / zoom;
            targetZoom = zoom;
            clampCamera();
            return true;
        }
        if (this.nameField != null && this.nameField.isFocused() && button == InputConstants.MOUSE_BUTTON_LEFT) {
            return this.nameField.mouseDragged(toGui(mouseX), toGui(mouseY), button, toGui(dx), toGui(dy));
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
            return true;
        }

        if (button == InputConstants.MOUSE_BUTTON_LEFT && overButton(guiX, guiY)) {
            if (canBrew() && this.minecraft != null && this.minecraft.gameMode != null) {
                startBrewAnimation();
                this.minecraft.gameMode.handleInventoryButtonClick(this.getMenu().containerId, AlchemicalCauldronMenu.BUTTON_BREW);
            }
            return true;
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
        if (button == InputConstants.MOUSE_BUTTON_LEFT) panning = false;
        return super.mouseReleased(toGui(mouseX), toGui(mouseY), button);
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
    protected void renderBg(GuiGraphics graphics, float a, int mouseX, int mouseY) {
        RenderSystem.enableBlend();

        if (!viewInitialised) resetView();
        tickZoom();

        known = this.minecraft != null && this.minecraft.player != null
                ? RecipeDiscovery.knownTo(this.minecraft.player)
                : Set.of();
        hoverName = null;
        hoverDetail = null;

        graphics.blit(BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, IMAGE_WIDTH, BOOK_HEIGHT, IMAGE_WIDTH, BOOK_HEIGHT);
        extractMap(graphics, mouseX, mouseY);
        extractSlotPanels(graphics);
        extractEffectsPanel(graphics, mouseX, mouseY);
        extractBrewButton(graphics, mouseX, mouseY);
        drawNameField(graphics);
    }

    private record Leg(List<PathPoint> points, boolean teleport) {
        double length() {
            return PathGeometry.length(points);
        }

        PathPoint from() {
            return points.get(0);
        }

        PathPoint to() {
            return points.get(points.size() - 1);
        }
    }

    private static final double TRAVEL_PER_SECOND = 260.0;
    private static final long TELEPORT_MS = 460L;

    private final List<Leg> animLegs = new ArrayList<>();
    private long animStart;

    private void startBrewAnimation() {
        animLegs.clear();
        if (!PotionMap.isReady()) return;

        TileEntityAlchemyCauldron cauldron = this.getMenu().getCauldron();
        double x = cauldron.getXAlignment();
        double y = cauldron.getYAlignment();

        for (PotionMap.RouteLeg leg : map().route(x, y, queuedIngredients())) {
            PotionMap.PotionPath path = leg.path();
            if (path.stalled()) continue;

            PathPoint landing = path.end(x, y);
            animLegs.add(new Leg(path.teleport()
                    ? List.of(new PathPoint(x, y), landing)
                    : path.points(), path.teleport()));

            x = landing.x();
            y = landing.y();
        }

        animStart = Util.getMillis();
    }

    private long legDuration(Leg leg) {
        return leg.teleport() ? TELEPORT_MS
                : Math.max(60L, Math.round(leg.length() / TRAVEL_PER_SECOND * 1000.0));
    }

    private long animDuration() {
        long total = 0L;
        for (Leg leg : animLegs) total += legDuration(leg);
        return total;
    }

    private boolean animating() {
        return !animLegs.isEmpty() && Util.getMillis() - animStart < animDuration();
    }

    private double[] animState() {
        long elapsed = Util.getMillis() - animStart;
        PathPoint at = animLegs.isEmpty() ? PathPoint.ORIGIN : animLegs.get(0).from();

        for (Leg leg : animLegs) {
            long duration = legDuration(leg);

            if (elapsed >= duration) {
                elapsed -= duration;
                at = leg.to();
                continue;
            }

            double t = (double) elapsed / duration;

            if (!leg.teleport()) {
                PathPoint travelled = PathGeometry.pointAt(leg.points(), leg.length() * t);
                return new double[]{travelled.x(), travelled.y(), 1.0, 0.0};
            }

            if (t < 0.5) {
                double k = t * 2.0;
                return new double[]{leg.from().x(), leg.from().y(), 1.0 - k, k * 4 * Math.PI};
            }

            double k = (t - 0.5) * 2.0;
            return new double[]{leg.to().x(), leg.to().y(), k, (1.0 - k) * 4 * Math.PI};
        }

        return new double[]{at.x(), at.y(), 1.0, 0.0};
    }

    private boolean overButton(double mouseX, double mouseY) {
        int x = this.leftPos + BUTTON_X;
        int y = this.topPos + BUTTON_Y;
        return mouseX >= x && mouseX < x + BUTTON_W && mouseY >= y && mouseY < y + BUTTON_H;
    }

    private int drawCentredWrapped(GuiGraphics graphics, Component text, int centreX, int y, int maxWidth, int colour) {
        for (FormattedCharSequence line : this.font.split(text, maxWidth)) {
            graphics.drawString(this.font, line, centreX - this.font.width(line) / 2, y, colour, false);
            y += 10;
        }
        return y;
    }

    private Map<MobEffect, Integer> gainedEffects() {
        TileEntityAlchemyCauldron cauldron = this.getMenu().getCauldron();
        Map<MobEffect, Integer> predicted = cauldron.previewPotencies(queuedIngredients());
        Map<MobEffect, Integer> gained = new LinkedHashMap<>();
        if (predicted == null) return gained;

        Map<MobEffect, Integer> current = new LinkedHashMap<>();
        for (MobEffectInstance instance : cauldron.getEffectList()) {
            current.put(instance.getEffect(), instance.getAmplifier() + 1);
        }

        predicted.forEach((effect, potency) -> {
            int held = current.getOrDefault(effect, 0);
            if (potency > held) gained.put(effect, potency - held);
        });
        return gained;
    }

    private record PotencySlot(Component name, MobEffect effect, boolean redstone) {}

    private List<PotencySlot> potencySlots(TileEntityAlchemyCauldron cauldron) {
        List<PotencySlot> slots = new ArrayList<>();

        for (int charge = 0; charge < cauldron.redstoneCharges(); charge++) {
            slots.add(new PotencySlot(Component.translatable("alchemia.gui.redstone",
                    cauldron.getDurationBonus() / 20), null, true));
        }

        for (MobEffectInstance instance : cauldron.getEffectList()) {
            Component name = Component.translatable("alchemia.gui.effect.entry",
                    instance.getEffect().getDisplayName(), instance.getAmplifier() + 1,
                    (instance.getDuration() + cauldron.getDurationBonus()) / 20);

            for (int i = 0; i <= instance.getAmplifier(); i++) {
                slots.add(new PotencySlot(name, instance.getEffect(), false));
            }
        }
        return slots;
    }

    private void drawUnknownSymbol(GuiGraphics graphics, int cx, int cy, int node) {
        int w = Math.max(1, Math.round(node * (float) UNKNOWN_W / UNKNOWN_CELL));
        int h = Math.max(1, Math.round(node * (float) UNKNOWN_H / UNKNOWN_CELL));
        RenderSystem.enableBlend();
        graphics.setColor(AlchemiaSprites.red(UNKNOWN_TINT), AlchemiaSprites.green(UNKNOWN_TINT),
                AlchemiaSprites.blue(UNKNOWN_TINT), AlchemiaSprites.alpha(UNKNOWN_TINT));
        graphics.blit(SYMBOLS_TEXTURE, cx - w / 2, cy - h / 2, w, h,
                UNKNOWN_U, UNKNOWN_V, UNKNOWN_W, UNKNOWN_H, SYMBOLS_W, SYMBOLS_H);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void ghostDust(GuiGraphics graphics, int slotX, int slotY) {
        RenderSystem.enableBlend();
        graphics.setColor(AlchemiaSprites.red(GHOST_ALPHA), AlchemiaSprites.green(GHOST_ALPHA),
                AlchemiaSprites.blue(GHOST_ALPHA), AlchemiaSprites.alpha(GHOST_ALPHA));
        graphics.blit(SYMBOLS_TEXTURE,
                slotX + (18 - DUST_W) / 2, slotY + (18 - DUST_H) / 2,
                DUST_W, DUST_H, (float) DUST_U, (float) DUST_V,
                DUST_W, DUST_H, SYMBOLS_W, SYMBOLS_H);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void extractEffectsPanel(GuiGraphics graphics, int mouseX, int mouseY) {
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

        AlchemiaSprites.PANEL.blit(graphics, panelX, panelY, EFFECTS_W, panelH);

        drawCentredWrapped(graphics, Component.translatable("alchemia.gui.effects.title"),
                panelX + EFFECTS_W / 2, panelY + 5, EFFECTS_W - 8, INK);

        int swatch = cauldron.getWaterLevel() > 0 ? cauldron.getPotionColor() : 0x7A6A50;
        AlchemiaSprites.POTION_SWATCH.blit(graphics, panelX + 4, panelY + 17, EFFECTS_W - 8, 4, ARGB.opaque(swatch));

        if (ruined) {
            graphics.renderItem(new ItemStack(Items.SKELETON_SKULL), panelX + (EFFECTS_W - 16) / 2, panelY + 26);

            int lineY = panelY + RUINED_TEXT_Y;
            for (FormattedCharSequence line : ruinedLines) {
                graphics.drawString(this.font, line, panelX + (EFFECTS_W - this.font.width(line)) / 2, lineY,
                        ARGB.opaque(BLOCKED_RGB), false);
                lineY += LINE_HEIGHT;
            }
            return;
        }

        List<PotencySlot> filled = potencySlots(cauldron);

        List<MobEffect> incoming = new ArrayList<>();
        gainedEffects().forEach((effect, potency) -> {
            for (int i = 0; i < potency; i++) incoming.add(effect);
        });
        int gridX = panelX + (EFFECTS_W - columns * POTENCY_CELL) / 2;
        int gridY = panelY + 26;
        Component hovered = null;

        for (int index = 0; index < budget; index++) {
            int x = gridX + (index % POTENCY_PER_ROW) * POTENCY_CELL;
            int y = gridY + (index / POTENCY_PER_ROW) * POTENCY_CELL;

            slotBackdrop(graphics, x, y);

            if (index >= filled.size()) {
                int ahead = index - filled.size();
                if (ahead < incoming.size()) {
                    double beat = (Math.sin(Util.getMillis() / 300.0) + 1.0) * 0.5;
                    AlchemiaSprites.blitMobEffect(graphics, incoming.get(ahead), x + 1, y + 1, 16, 16, ARGB.color((int) Math.round(70 + beat * 110), 0xFFFFFF));
                }
                continue;
            }

            PotencySlot slot = filled.get(index);
            if (slot.redstone()) {
                graphics.renderItem(new ItemStack(Items.REDSTONE), x + 1, y + 1);
            } else {
                AlchemiaSprites.blitMobEffect(graphics, slot.effect(), x + 1, y + 1, 16, 16);
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

        if (hovered != null) graphics.renderTooltip(this.font, hovered, mouseX, mouseY);
    }

    private Component extractInstability(GuiGraphics graphics, TileEntityAlchemyCauldron cauldron,
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
                graphics.renderItem(new ItemStack(Items.SKELETON_SKULL), x + 1, gridY + 1);
            }

            if (mouseX >= x && mouseX < x + POTENCY_CELL && mouseY >= gridY && mouseY < gridY + POTENCY_CELL) {
                hovered = Component.translatable("alchemia.gui.instability", spent, cap);
            }
        }

        return hovered;
    }

    private void extractBrewButton(GuiGraphics graphics, int mouseX, int mouseY) {
        boolean ready = canBrew();
        int x = this.leftPos + BUTTON_X;
        int y = this.topPos + BUTTON_Y;

        AlchemiaSprites.Sprite sprite = !ready
                ? AlchemiaSprites.BUTTON_DISABLED
                : (overButton(mouseX, mouseY) ? AlchemiaSprites.BUTTON_HIGHLIGHTED : AlchemiaSprites.BUTTON);
        sprite.blit(graphics, x, y, BUTTON_W, BUTTON_H);

        Component label = Component.translatable("alchemia.gui.brew");
        graphics.drawString(this.font, label, x + (BUTTON_W - this.font.width(label)) / 2, y + 5, ready ? INK : INK_FAINT, false);
    }

    private boolean canBrew() {
        TileEntityAlchemyCauldron cauldron = this.getMenu().getCauldron();
        if (cauldron.getWaterLevel() <= 0) return false;
        return !queuedIngredients().isEmpty() || cauldron.hasRedstone();
    }

    private PotionMap map() {
        return PotionMap.get(this.getMenu().getCauldron().getBase());
    }

    private List<ItemStack> queuedIngredients() {
        List<ItemStack> queued = new ArrayList<>();
        for (int slot = 0; slot < AlchemicalCauldronMenu.INGREDIENT_SLOTS; slot++) {
            ItemStack stack = this.getMenu().getSlot(slot).getItem();
            if (TileEntityAlchemyCauldron.isBrewingInput(stack)) {
                for (int i = 0; i < stack.getCount(); i++) queued.add(stack.copyWithCount(1));
            }
        }
        return queued;
    }

    private void extractSlotPanels(GuiGraphics graphics) {
        for (int slot = 0; slot < AlchemicalCauldronMenu.INGREDIENT_SLOTS; slot++) {
            slotBackdrop(graphics, this.leftPos + SLOT_ROW_X + slot * 18, this.topPos + SLOT_ROW_Y);
        }

        TileEntityAlchemyCauldron cauldron = this.getMenu().getCauldron();
        int specialX = this.leftPos + AlchemicalCauldronMenu.SPECIAL_SLOT_X;
        int specialY = this.topPos + SLOT_ROW_Y;

        slotBackdrop(graphics, specialX, specialY);
        slotBackdrop(graphics, specialX + AlchemicalCauldronMenu.SPECIAL_SLOT_GAP, specialY);

        if (!cauldron.hasRedstone()) ghostDust(graphics, specialX, specialY);
        if (!cauldron.hasGunpowder()) ghostDust(graphics, specialX + AlchemicalCauldronMenu.SPECIAL_SLOT_GAP, specialY);

        int panelX = this.leftPos + INVENTORY_X - 6;
        int panelY = this.topPos + INVENTORY_Y - 6;
        int panelW = 9 * 18 + 12;
        int panelH = (HOTBAR_Y + 18) - INVENTORY_Y + 12;

        AlchemiaSprites.PANEL.blit(graphics, panelX, panelY, panelW, panelH);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                slotBackdrop(graphics, this.leftPos + INVENTORY_X + column * 18, this.topPos + INVENTORY_Y + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            slotBackdrop(graphics, this.leftPos + INVENTORY_X + column * 18, this.topPos + HOTBAR_Y);
        }
    }

    private void slotBackdrop(GuiGraphics graphics, int x, int y) {
        AlchemiaSprites.PARCHMENT_SLOT.blit(graphics, x, y, AlchemiaSprites.SLOT_SIZE, AlchemiaSprites.SLOT_SIZE);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float a) {
        RenderSystem.enableBlend();
        this.renderBackground(graphics);

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.scale(this.uiScale, this.uiScale, 1.0F);

        super.render(graphics, (int) Math.round(toGui(mouseX)), (int) Math.round(toGui(mouseY)), a);
        graphics.blit(FRAME_TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, IMAGE_WIDTH, BOOK_HEIGHT, IMAGE_WIDTH, BOOK_HEIGHT);
        extractOverlayText(graphics);

        pose.popPose();

        this.renderTooltip(graphics, mouseX, mouseY);

        if (hoverName != null) {
            List<net.minecraft.util.FormattedCharSequence> lines = new ArrayList<>();
            lines.add(hoverName.getVisualOrderText());
            if (hoverDetail != null) lines.add(hoverDetail.getVisualOrderText());
            graphics.renderTooltip(this.font, lines, hoverX, hoverY);
        }
    }

    private void drawNameField(GuiGraphics graphics) {
        int fieldX = viewLeft() + NAME_X;
        int fieldY = viewTop() + NAME_Y;

        AlchemiaSprites.NAME_FIELD.blit(graphics, fieldX, fieldY, NAME_W, NAME_H);
    }

    private void drawNameText(GuiGraphics graphics) {
        if (this.nameField == null) return;

        int textX = viewLeft() + NAME_X + 2;
        int textY = viewTop() + NAME_Y + 2 + (NAME_H - 4 - this.font.lineHeight) / 2 + 1;

        String value = this.nameField.getValue();
        boolean focused = this.nameField.isFocused();

        if (value.isEmpty() && !focused) {
            Component hint = Component.translatable("alchemia.gui.name.hint");
            graphics.drawString(this.font, hint, textX, textY, INK_FAINT, false);
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

        if (focused && selection != cursor) {
            int from = Math.min(cursor, selection);
            int to = Math.max(cursor, selection);
            int selStart = textX + this.font.width(value.substring(0, from));
            int selEnd = textX + this.font.width(value.substring(0, to));
            graphics.fill(selStart, textY - 1, selEnd, textY + this.font.lineHeight, NAME_SELECTION);
        }

        graphics.drawString(this.font, value, textX, textY, INK, false);

        if (focused && (System.currentTimeMillis() / 400L) % 2L == 0L) {
            int caret = textX + this.font.width(value.substring(0, cursor));
            graphics.fill(caret, textY - 1, caret + 1, textY + this.font.lineHeight, INK);
        }
    }

    private void extractOverlayText(GuiGraphics graphics) {
        drawNameText(graphics);

        int left = viewLeft() + 4;
        int top = viewTop() + 3;

        Component recipes = Component.translatable("alchemia.gui.recipes", known.size(), RecipeDiscovery.total());
        graphics.drawString(this.font, recipes, viewLeft() + VIEW_WIDTH - 4 - this.font.width(recipes), top, INK_FAINT, false);

        Component zoomLabel = Component.translatable("alchemia.gui.zoom", Math.round(zoom * 100));
        graphics.drawString(this.font, zoomLabel, left, viewTop() + VIEW_HEIGHT - 11, INK_FAINT, false);

        int queued = queuedIngredients().size();
        if (queued > 0) {
            Component queuedLabel = Component.translatable("alchemia.gui.queued", queued);
            graphics.drawString(this.font, queuedLabel, this.leftPos + VIEW_INSET_X, this.topPos + BUTTON_Y + 5, INK_FAINT, false);
        }
    }

    private void extractMap(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!PotionMap.isReady()) return;

        TileEntityAlchemyCauldron cauldron = this.getMenu().getCauldron();
        PathPoint origin = toScreen(new PathPoint(cauldron.getXAlignment(), cauldron.getYAlignment()));

        int left = viewLeft();
        int top = viewTop();
        int right = left + VIEW_WIDTH;
        int bottom = top + VIEW_HEIGHT;

        graphics.enableScissor(toScreen(left), toScreen(top), toScreen(right), toScreen(bottom));

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

            PotionEnum recipe = entry.effect().getRecipe();
            boolean discovered = recipe != null && known.contains(recipe);

            drawEffectAura(graphics, nx, ny, entry, discovered);
            drawThread(graphics, origin.roundedX(), origin.roundedY(), nx, ny, thickness);

            if (discovered) {
                AlchemiaSprites.blitMobEffect(graphics, entry.effect().getEffect(), nx - half, ny - half, node, node, -1);
            } else {
                drawUnknownSymbol(graphics, nx, ny, node);
            }

            if (discovered && zoom >= LABEL_ZOOM) {
                Component name = entry.effect().getEffect().getDisplayName();
                graphics.drawString(this.font, name, nx - this.font.width(name) / 2, ny + half + 2, INK, false);
            }

            if (mouseX >= nx - half && mouseX <= nx + half && mouseY >= ny - half && mouseY <= ny + half) {
                hoverX = toScreen(mouseX);
                hoverY = toScreen(mouseY);
                if (discovered) {
                    hoverName = entry.effect().getEffect().getDisplayName();
                    hoverDetail = Component.translatable("alchemia.gui.potency",
                            entry.effect().getStrength() + 1, entry.effect().getDuration() / 20);
                } else {
                    hoverName = Component.translatable("alchemia.gui.unknown");
                    hoverDetail = Component.translatable("alchemia.gui.unknown.hint");
                }
            }
        }

        extractPlan(graphics, cauldron, node);

        double markerX = origin.x();
        double markerY = origin.y();
        double markerScale = 1.0;
        double markerSpin = 0.0;

        if (animating()) {
            double[] state = animState();
            markerX = screenX(state[0]);
            markerY = screenY(state[1]);
            markerScale = state[2];
            markerSpin = state[3];
        }

        if (markerX >= left && markerX <= right && markerY >= top && markerY <= bottom) {
            drawBrewMarker(graphics, (int) Math.round(markerX), (int) Math.round(markerY), cauldron, node, markerScale, markerSpin);
        }

        graphics.disableScissor();
    }

    private List<PathPoint> chain(List<PotionMap.RouteLeg> legs, double startX, double startY) {
        List<PathPoint> points = new ArrayList<>();
        points.add(new PathPoint(startX, startY));

        for (PotionMap.RouteLeg leg : legs) {
            List<PathPoint> span = leg.curve().isEmpty() ? leg.path().points() : leg.curve();
            for (PathPoint point : span) {
                if (points.get(points.size() - 1).distanceTo(point) < 0.01) continue;
                points.add(point);
            }
            if (!leg.curve().isEmpty()) points.add(leg.path().end(startX, startY));
        }
        return points;
    }

    private PathPoint drawLeg(GuiGraphics graphics, PotionMap.RouteLeg leg, PathPoint from, int rgb, int alpha) {
        PotionMap.PotionPath path = leg.path();

        if (!path.teleport()) {
            if (path.points().size() < 2) return from;

            List<PathPoint> screen = toScreen(path.points());
            PathRenderer.dashes(graphics, screen, PATH_THICKNESS, ARGB.color(alpha, rgb), false);
            return screen.get(screen.size() - 1);
        }

        PathPoint tip = from;

        if (leg.curve().size() > 1) {
            List<PathPoint> flight = toScreen(leg.curve());
            PathRenderer.dashes(graphics, flight, PATH_THICKNESS, ARGB.color(alpha, rgb), true);
            tip = flight.get(flight.size() - 1);
        }

        PathPoint landing = toScreen(path.end(0, 0));
        PathRenderer.dashes(graphics, List.of(tip, landing), PATH_THICKNESS, ARGB.color(alpha, rgb), true);
        return landing;
    }

    private void extractPlan(GuiGraphics graphics, TileEntityAlchemyCauldron cauldron, int node) {
        List<ItemStack> queued = queuedIngredients();
        if (queued.isEmpty()) return;

        double x = cauldron.getXAlignment();
        double y = cauldron.getYAlignment();
        PathPoint tip = toScreen(new PathPoint(x, y));

        boolean stalled = false;
        boolean doomed = false;
        boolean stranded = false;

        Set<MobEffect> held = new HashSet<>();
        for (MobEffectInstance instance : cauldron.getEffectList()) held.add(instance.getEffect());

        List<PathPoint> pickups = new ArrayList<>();
        int simulatedInstability = cauldron.getInstability();

        for (PotionMap.RouteLeg leg : map().route(x, y, queued)) {
            ItemStack queuedStack = leg.stack();
            if (!TileEntityAlchemyCauldron.isBrewingInput(queuedStack)) continue;

            PotionMap.PotionPath path = leg.path();

            if (path.stalled()) {
                stalled = true;
                if (path.teleport()) drawOffMapAttempt(graphics, x, y, queuedStack, node);
                continue;
            }

            PathPoint landing = path.end(x, y);

            if (path.dead()) {
                tip = drawLeg(graphics, leg, tip, GHOST_RGB, 190);

                simulatedInstability++;
                if (simulatedInstability >= cauldron.getInstabilityCap()) {
                    doomed = true;
                    break;
                }

                drawPlanMarker(graphics, tip, node, BLOCKED_RGB);
                x = landing.x();
                y = landing.y();
                stranded = true;
                continue;
            }

            tip = drawLeg(graphics, leg, tip, GHOST_RGB, 190);

            if (!path.teleport()) {
                for (PotionMap.MapEntry passed : map().touched(path.points(), false)) {
                    if (passed.effect().getEffect() != null && held.add(passed.effect().getEffect())) {
                        pickups.add(passed.position());
                    }
                }
            }

            PotionMap.PotionEffectPosition arrival = map().getEffectPotion(landing);
            if (arrival.getEffect() != null && held.add(arrival.getEffect())) pickups.add(landing);

            x = landing.x();
            y = landing.y();
        }

        int predicted = cauldron.previewPotency(queued);
        boolean overloaded = predicted > TileEntityAlchemyCauldron.maxPotency();

        int half = Math.max(3, node / 3);
        int ghost = stalled || doomed || overloaded || stranded ? BLOCKED_RGB : GHOST_RGB;

        if (!doomed && !overloaded) {
            for (PathPoint pickup : pickups) drawPlanMarker(graphics, toScreen(pickup), node, GAINED_RGB);
        }

        drawPlanMarker(graphics, tip, node, ghost);

        if (doomed || overloaded) {
            graphics.renderItem(new ItemStack(Items.SKELETON_SKULL), tip.roundedX() - 8, tip.roundedY() - 8);
            drawPlanWarning(graphics, doomed
                    ? Component.translatable("alchemia.gui.doomed")
                    : Component.translatable("alchemia.gui.overloaded", predicted, TileEntityAlchemyCauldron.maxPotency()),
                    tip, half);
            return;
        }

        if (stranded) {
            graphics.renderItem(new ItemStack(Items.SKELETON_SKULL), tip.roundedX() - 8, tip.roundedY() - 8);
            drawPlanWarning(graphics, Component.translatable("alchemia.gui.stranded",
                    simulatedInstability, cauldron.getInstabilityCap()), tip, half);
            return;
        }

        PotionMap.PotionEffectPosition landing = map().getEffectPotion(x, y);
        Component label = landing.getEffect() == null
                ? Component.translatable("alchemia.gui.predicted.none")
                : landing.getEffect().getDisplayName();
        drawMapLabel(graphics, label, tip, half, INK);
    }

    private void drawPlanWarning(GuiGraphics graphics, Component warning, PathPoint at, int half) {
        drawMapLabel(graphics, warning, at, half, ARGB.opaque(BLOCKED_RGB));
    }

    private void drawMapLabel(GuiGraphics graphics, Component label, PathPoint at, int half, int colour) {
        int width = this.font.width(label);
        int labelX = Mth.clamp(at.roundedX(), viewLeft() + width / 2 + 2, viewLeft() + VIEW_WIDTH - width / 2 - 2);
        graphics.drawString(this.font, label, labelX - width / 2, at.roundedY() + half + 3, colour, false);
    }

    private void drawOffMapAttempt(GuiGraphics graphics, double x, double y, ItemStack stack, int node) {
        List<PathPoint> curve = toScreen(map().teleportCurve(x, y, stack));
        if (curve.size() < 2) return;

        PathRenderer.dashes(graphics, curve, PATH_THICKNESS, ARGB.color(190, BLOCKED_RGB), true);

        PathPoint tip = curve.get(curve.size() - 1);
        int half = Math.max(3, node / 3);

        PathRenderer.spiral(graphics, tip.x(), tip.y(), half, 2, ARGB.color(220, BLOCKED_RGB));
        drawPlanWarning(graphics, Component.translatable("alchemia.gui.offmap"), tip, half);
    }

    private void drawPlanMarker(GuiGraphics graphics, PathPoint at, int node, int rgb) {
        PathRenderer.marker(graphics, at.x(), at.y(), Math.max(3, node / 3), rgb);
    }

    private void drawMapEdge(GuiGraphics graphics, int left, int top, int right, int bottom) {
        int size = map().getSize();

        int x0 = Mth.clamp((int) Math.round(screenX(0)), left, right);
        int x1 = Mth.clamp((int) Math.round(screenX(size)), left, right);
        int y0 = Mth.clamp((int) Math.round(screenY(0)), top, bottom);
        int y1 = Mth.clamp((int) Math.round(screenY(size)), top, bottom);

        if (y0 > top) graphics.fill(left, top, right, y0, EDGE_FILL);
        if (y1 < bottom) graphics.fill(left, y1, right, bottom, EDGE_FILL);
        if (x0 > left) graphics.fill(left, y0, x0, y1, EDGE_FILL);
        if (x1 < right) graphics.fill(x1, y0, right, y1, EDGE_FILL);

        if (x0 > left) graphics.fill(x0 - 1, y0, x0 + 1, y1, EDGE_LINE);
        if (x1 < right) graphics.fill(x1 - 1, y0, x1 + 1, y1, EDGE_LINE);
        if (y0 > top) graphics.fill(x0, y0 - 1, x1, y0 + 1, EDGE_LINE);
        if (y1 < bottom) graphics.fill(x0, y1 - 1, x1, y1, EDGE_LINE);
    }

    private static final double MIN_TILE_PIXELS = 3.0;
    private static final double EDGE_TILE_PIXELS = 5.0;

    private int deadzoneLevel(PotionMap map) {
        int level = 0;
        while (level + 1 < map.lodLevels() && PotionMap.tileSize(level) * zoom < MIN_TILE_PIXELS) level++;
        return level;
    }

    private void extractDeadzones(GuiGraphics graphics, int left, int top, int right, int bottom) {
        if (!PotionMap.isReady()) return;

        PotionMap map = map();
        int level = deadzoneLevel(map);
        double span = PotionMap.tileSize(level);
        int last = map.tileCount(level) - 1;

        int fromX = Mth.clamp((int) Math.floor(mapAtX(left) / span), 0, last);
        int toX = Mth.clamp((int) Math.floor(mapAtX(right) / span), 0, last);
        int fromY = Mth.clamp((int) Math.floor(mapAtY(top) / span), 0, last);
        int toY = Mth.clamp((int) Math.floor(mapAtY(bottom) / span), 0, last);

        boolean edges = span * zoom >= EDGE_TILE_PIXELS;
        int fill = ARGB.color(70, BLOCKED_RGB);
        int edge = ARGB.color(150, BLOCKED_RGB);

        for (int tileY = fromY; tileY <= toY; tileY++) {
            int y0 = (int) Math.round(screenY(PotionMap.tileOrigin(level, tileY)));
            int y1 = (int) Math.round(screenY(PotionMap.tileOrigin(level, tileY + 1)));
            if (y1 <= y0) y1 = y0 + 1;

            int runStart = -1;

            for (int tileX = fromX; tileX <= toX + 1; tileX++) {
                boolean solid = tileX <= toX && map.isDeadTile(level, tileX, tileY);

                if (solid && runStart < 0) runStart = tileX;
                if (solid || runStart < 0) continue;

                int x0 = (int) Math.round(screenX(PotionMap.tileOrigin(level, runStart)));
                int x1 = (int) Math.round(screenX(PotionMap.tileOrigin(level, tileX)));
                if (x1 <= x0) x1 = x0 + 1;

                graphics.fill(x0, y0, x1, y1, fill);

                if (edges) {
                    if (!map.isDeadTile(level, runStart - 1, tileY)) graphics.fill(x0, y0, x0 + 1, y1, edge);
                    if (!map.isDeadTile(level, tileX, tileY)) graphics.fill(x1 - 1, y0, x1, y1, edge);

                    for (int run = runStart; run < tileX; run++) {
                        int rx0 = (int) Math.round(screenX(PotionMap.tileOrigin(level, run)));
                        int rx1 = (int) Math.round(screenX(PotionMap.tileOrigin(level, run + 1)));
                        if (rx1 <= rx0) rx1 = rx0 + 1;

                        if (!map.isDeadTile(level, run, tileY - 1)) graphics.fill(rx0, y0, rx1, y0 + 1, edge);
                        if (!map.isDeadTile(level, run, tileY + 1)) graphics.fill(rx0, y1 - 1, rx1, y1, edge);
                    }
                }

                runStart = -1;
            }
        }
    }

    private void drawBrewMarker(GuiGraphics graphics, int x, int y, TileEntityAlchemyCauldron cauldron, int node, double scale, double spin) {
        if (scale <= 0.01) return;

        double pulse = (Math.sin(Util.getMillis() / 380.0) + 1.0) * 0.5;
        int radius = (int) Math.round(node * 0.55 + pulse * 3.0);
        int alpha = (int) Math.round(70 + pulse * 90);

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0.0F);
        pose.mulPose(com.mojang.math.Axis.ZP.rotation((float) spin));
        pose.scale((float) scale, (float) scale, 1.0F);

        graphics.renderOutline(-radius, -radius, radius * 2, radius * 2, ARGB.color(alpha, 0x2A1F14));
        graphics.renderOutline(-radius - 1, -radius - 1, radius * 2 + 2, radius * 2 + 2, ARGB.color(alpha / 3, 0x2A1F14));
        ItemStack marker = new ItemStack(cauldron.hasGunpowder() ? Items.SPLASH_POTION : Items.POTION);
        PotionStacks.apply(marker, Potions.WATER, List.of(), cauldron.getPotionColor());
        graphics.renderItem(marker, -8, -8);

        pose.popPose();
    }

    private void drawEffectAura(GuiGraphics graphics, int cx, int cy,
                                PotionMap.MapEntry entry, boolean discovered) {
        double reach = PotionMap.EFFECT_RADIUS * zoom;
        if (reach < 2.0) return;

        if (cx + reach < viewLeft() || cx - reach > viewLeft() + VIEW_WIDTH
                || cy + reach < viewTop() || cy - reach > viewTop() + VIEW_HEIGHT) return;

        int rgb = discovered ? entry.effect().getEffect().getColor() : AURA_UNKNOWN_RGB;
        double pulse = (Math.sin(Util.getMillis() / 520.0 + cx * 0.35) + 1.0) * 0.5;
        int alpha = (int) Math.round(AURA_CORE_ALPHA * (0.72 + pulse * 0.28));

        PathRenderer.aura(graphics, cx, cy, reach, rgb, alpha);
        PathRenderer.ring(graphics, cx, cy, reach, ARGB.color(Math.min(255, alpha * 2), rgb), true);

        double core = PotionMap.EFFECT_CORE * zoom;
        if (core >= 2.0) PathRenderer.ring(graphics, cx, cy, core, ARGB.color(Math.min(255, alpha * 3), rgb), false);
    }

    private void drawThread(GuiGraphics graphics, int x0, int y0, int x1, int y1, int thickness) {
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
