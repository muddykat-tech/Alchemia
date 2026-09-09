package muddykat.alchemia.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import muddykat.alchemia.Alchemia;
import muddykat.alchemia.common.blocks.tileentity.TileEntityAlchemyCauldron;
import muddykat.alchemia.common.blocks.tileentity.container.AlchemicalCauldronMenu;
import muddykat.alchemia.common.items.ItemIngredient;
import muddykat.alchemia.common.potion.PotionEnum;
import muddykat.alchemia.common.potion.PotionMap;
import muddykat.alchemia.common.potion.RecipeDiscovery;
import net.minecraft.util.Util;
import muddykat.alchemia.common.network.packets.PacketNameBrew;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
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

    private static final Identifier SYMBOLS_TEXTURE = Identifier.fromNamespaceAndPath(Alchemia.MODID, "textures/book/images/symbols.png");
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

    private static final int GHOST_RGB = 0x6A4E2A;
    private static final int BLOCKED_RGB = 0x6B2418;
    private static final int GAINED_RGB = 0x57C24A;
    private static final int EDGE_FILL = 0x662A4A8E;
    private static final int EDGE_LINE = 0xAA3C63B4;
    private static final int AURA_UNKNOWN_RGB = 0x7A6A50;
    private static final int AURA_CORE_ALPHA = 96;
    private static final int AURA_DASH = 2;

    private static final double SPACING_X = 21.0;
    private static final double SPACING_Y = 22.0;

    private static final double MIN_ZOOM = 0.4;
    private static final double MAX_ZOOM = 4.0;
    private static final double ZOOM_STEP = 1.18;
    private static final double LABEL_ZOOM = 1.5;

    private static final int INK = 0xFF4A3A28;
    private static final int INK_FAINT = 0xFF7A6A52;
    private static final int LINE_RGB = 0x282828;
    private static final int UNKNOWN_TINT = 0xFF7A6A52;
    private static final int NODE_BASE = 18;

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
    private static final int NAME_H = 12;

    private EditBox nameField;
    private boolean panning;
    private Set<PotionEnum> known = Set.of();
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

        String existing = this.getMenu().getCauldron().getBrewName();
        this.nameField = new EditBox(this.font, viewLeft() + NAME_X + 2, viewTop() + NAME_Y + 2,
                NAME_W - 4, NAME_H - 4, Component.translatable("alchemia.gui.name"));
        this.nameField.setBordered(false);
        this.nameField.setMaxLength(PacketNameBrew.MAX_LENGTH);
        this.nameField.setTextColor(INK);
        this.nameField.setValue(existing);
        this.nameField.setHint(Component.translatable("alchemia.gui.name.hint"));
        this.nameField.setResponder(value -> ClientPacketDistributor.sendToServer(new PacketNameBrew(value)));
        addRenderableWidget(this.nameField);
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
        this.nameField.setFocused(false);
        this.setFocused(null);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
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
        return viewCentreX() + (mapX - camX) * SPACING_X * zoom;
    }

    private double screenY(double mapY) {
        return viewCentreY() + (mapY - camY) * SPACING_Y * zoom;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY == 0 || !overMap(mouseX, mouseY)) return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);

        anchorScreenX = mouseX;
        anchorScreenY = mouseY;
        anchorWorldX = camX + (mouseX - viewCentreX()) / (SPACING_X * zoom);
        anchorWorldY = camY + (mouseY - viewCentreY()) / (SPACING_Y * zoom);

        double factor = scrollY > 0 ? ZOOM_STEP : 1.0 / ZOOM_STEP;
        targetZoom = Mth.clamp(targetZoom * factor, MIN_ZOOM, MAX_ZOOM);
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (panning && event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            camX -= dx / (SPACING_X * zoom);
            camY -= dy / (SPACING_Y * zoom);
            targetZoom = zoom;
            clampCamera();
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (!overNameField(event.x(), event.y())) {
            releaseNameFocus();
        }

        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && overButton(event.x(), event.y())) {
            if (canBrew() && this.minecraft != null && this.minecraft.gameMode != null) {
                startBrewAnimation();
                this.minecraft.gameMode.handleInventoryButtonClick(this.getMenu().containerId, AlchemicalCauldronMenu.BUTTON_BREW);
            }
            return true;
        }

        if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT && overMap(event.x(), event.y())) {
            resetView();
            return true;
        }

        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && overMap(event.x(), event.y())) {
            panning = true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) panning = false;
        return super.mouseReleased(event);
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
        double limit = map().getMaxAlignment() + 8;
        camX = Mth.clamp(camX, -8.0, limit);
        camY = Mth.clamp(camY, -8.0, limit);
    }

    private void tickZoom() {
        if (Math.abs(targetZoom - zoom) < 0.0005) {
            zoom = targetZoom;
            return;
        }
        zoom = Mth.lerp(0.35, zoom, targetZoom);
        camX = anchorWorldX - (anchorScreenX - viewCentreX()) / (SPACING_X * zoom);
        camY = anchorWorldY - (anchorScreenY - viewCentreY()) / (SPACING_Y * zoom);
        clampCamera();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);

        if (!viewInitialised) resetView();
        tickZoom();

        known = this.minecraft != null && this.minecraft.player != null
                ? RecipeDiscovery.knownTo(this.minecraft.player)
                : Set.of();
        hoverName = null;
        hoverDetail = null;

        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, IMAGE_WIDTH, BOOK_HEIGHT, IMAGE_WIDTH, BOOK_HEIGHT);
        extractMap(graphics, mouseX, mouseY);
        extractSlotPanels(graphics);
        extractEffectsPanel(graphics, mouseX, mouseY);
        extractBrewButton(graphics, mouseX, mouseY);
    }

    private record Hop(int x, int y, boolean teleport) {
        int duration() {
            return teleport ? TELEPORT_MS : MOVE_MS_PER_CELL;
        }
    }

    private static final int MOVE_MS_PER_CELL = 95;
    private static final int TELEPORT_MS = 460;

    private final List<Hop> animHops = new ArrayList<>();
    private int animStartX;
    private int animStartY;
    private long animStart;

    private void startBrewAnimation() {
        animHops.clear();
        if (!PotionMap.isReady()) return;

        TileEntityAlchemyCauldron cauldron = this.getMenu().getCauldron();
        int x = cauldron.getXAlignment();
        int y = cauldron.getYAlignment();
        animStartX = x;
        animStartY = y;

        for (ItemStack stack : queuedIngredients()) {
            PotionMap.PotionPath path = map().walk(x, y, stack);
            if (path.stalled()) continue;

            for (int[] cell : path.cells()) {
                animHops.add(new Hop(cell[0], cell[1], path.teleport()));
            }

            int[] end = path.end(x, y);
            x = end[0];
            y = end[1];
        }

        animStart = Util.getMillis();
    }

    private int animDuration() {
        int total = 0;
        for (Hop hop : animHops) total += hop.duration();
        return total;
    }

    private boolean animating() {
        return !animHops.isEmpty() && Util.getMillis() - animStart < animDuration();
    }

    private double[] animState() {
        long elapsed = Util.getMillis() - animStart;
        int prevX = animStartX;
        int prevY = animStartY;

        for (Hop hop : animHops) {
            if (elapsed >= hop.duration()) {
                elapsed -= hop.duration();
                prevX = hop.x();
                prevY = hop.y();
                continue;
            }

            double t = (double) elapsed / hop.duration();
            if (!hop.teleport()) {
                return new double[]{prevX + (hop.x() - prevX) * t, prevY + (hop.y() - prevY) * t, 1.0, 0.0};
            }

            if (t < 0.5) {
                double k = t * 2.0;
                return new double[]{prevX, prevY, 1.0 - k, k * 4 * Math.PI};
            }

            double k = (t - 0.5) * 2.0;
            return new double[]{hop.x(), hop.y(), k, (1.0 - k) * 4 * Math.PI};
        }

        return new double[]{prevX, prevY, 1.0, 0.0};
    }

    private boolean overButton(double mouseX, double mouseY) {
        int x = this.leftPos + BUTTON_X;
        int y = this.topPos + BUTTON_Y;
        return mouseX >= x && mouseX < x + BUTTON_W && mouseY >= y && mouseY < y + BUTTON_H;
    }

    private int drawCentredWrapped(GuiGraphicsExtractor graphics, Component text, int centreX, int y, int maxWidth, int colour) {
        for (FormattedCharSequence line : this.font.split(text, maxWidth)) {
            graphics.text(this.font, line, centreX - this.font.width(line) / 2, y, colour, false);
            y += 10;
        }
        return y;
    }

    private Map<Holder<MobEffect>, Integer> gainedEffects() {
        TileEntityAlchemyCauldron cauldron = this.getMenu().getCauldron();
        Map<Holder<MobEffect>, Integer> predicted = cauldron.previewPotencies(queuedIngredients());
        Map<Holder<MobEffect>, Integer> gained = new LinkedHashMap<>();
        if (predicted == null) return gained;

        Map<Holder<MobEffect>, Integer> current = new LinkedHashMap<>();
        for (MobEffectInstance instance : cauldron.getEffectList()) {
            current.put(instance.getEffect(), instance.getAmplifier() + 1);
        }

        predicted.forEach((effect, potency) -> {
            int held = current.getOrDefault(effect, 0);
            if (potency > held) gained.put(effect, potency - held);
        });
        return gained;
    }

    private record PotencySlot(Component name, Holder<MobEffect> effect, boolean redstone) {}

    private List<PotencySlot> potencySlots(TileEntityAlchemyCauldron cauldron) {
        List<PotencySlot> slots = new ArrayList<>();

        for (int charge = 0; charge < cauldron.redstoneCharges(); charge++) {
            slots.add(new PotencySlot(Component.translatable("alchemia.gui.redstone",
                    cauldron.getDurationBonus() / 20), null, true));
        }

        for (MobEffectInstance instance : cauldron.getEffectList()) {
            Component name = Component.translatable("alchemia.gui.effect.entry",
                    instance.getEffect().value().getDisplayName(), instance.getAmplifier() + 1,
                    (instance.getDuration() + cauldron.getDurationBonus()) / 20);

            for (int i = 0; i <= instance.getAmplifier(); i++) {
                slots.add(new PotencySlot(name, instance.getEffect(), false));
            }
        }
        return slots;
    }

    private void drawUnknownSymbol(GuiGraphicsExtractor graphics, int cx, int cy, int node) {
        int w = Math.max(1, Math.round(node * (float) UNKNOWN_W / UNKNOWN_CELL));
        int h = Math.max(1, Math.round(node * (float) UNKNOWN_H / UNKNOWN_CELL));
        graphics.blit(RenderPipelines.GUI_TEXTURED, SYMBOLS_TEXTURE,
                cx - w / 2, cy - h / 2, UNKNOWN_U, UNKNOWN_V, w, h,
                UNKNOWN_W, UNKNOWN_H, SYMBOLS_W, SYMBOLS_H, UNKNOWN_TINT);
    }

    private void ghostDust(GuiGraphicsExtractor graphics, int slotX, int slotY) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, SYMBOLS_TEXTURE,
                slotX + (18 - DUST_W) / 2, slotY + (18 - DUST_H) / 2,
                DUST_U, DUST_V, DUST_W, DUST_H, SYMBOLS_W, SYMBOLS_H, GHOST_ALPHA);
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

        List<Holder<MobEffect>> incoming = new ArrayList<>();
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
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Hud.getMobEffectSprite(incoming.get(ahead)),
                            x + 1, y + 1, 16, 16, ARGB.color((int) Math.round(70 + beat * 110), 0xFFFFFF));
                }
                continue;
            }

            PotencySlot slot = filled.get(index);
            if (slot.redstone()) {
                graphics.item(new ItemStack(Items.REDSTONE), x + 1, y + 1);
            } else {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Hud.getMobEffectSprite(slot.effect()),
                        x + 1, y + 1, 16, 16);
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

        if (hovered != null) graphics.setTooltipForNextFrame(hovered, mouseX, mouseY);
    }

    private Component extractInstability(GuiGraphicsExtractor graphics, TileEntityAlchemyCauldron cauldron,
                                         int panelX, int panelY, int mouseX, int mouseY) {
        int cap = cauldron.getInstabilityCap();
        int spent = Math.min(cauldron.getInstability(), cap);

        drawCentredWrapped(graphics, Component.translatable("alchemia.gui.base.label",
                        Component.translatable(cauldron.getBase().translationKey())),
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

    private void extractBrewButton(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        boolean ready = canBrew();
        int x = this.leftPos + BUTTON_X;
        int y = this.topPos + BUTTON_Y;

        Identifier sprite = !ready
                ? AlchemiaSprites.BUTTON_DISABLED
                : (overButton(mouseX, mouseY) ? AlchemiaSprites.BUTTON_HIGHLIGHTED : AlchemiaSprites.BUTTON);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, BUTTON_W, BUTTON_H);

        Component label = Component.translatable("alchemia.gui.brew");
        graphics.text(this.font, label, x + (BUTTON_W - this.font.width(label)) / 2, y + 5, ready ? INK : INK_FAINT, false);
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

    private void extractSlotPanels(GuiGraphicsExtractor graphics) {
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
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, AlchemiaSprites.SLOT,
                x, y, AlchemiaSprites.SLOT_SIZE, AlchemiaSprites.SLOT_SIZE);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        graphics.blit(RenderPipelines.GUI_TEXTURED, FRAME_TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, IMAGE_WIDTH, BOOK_HEIGHT, IMAGE_WIDTH, BOOK_HEIGHT);
        extractOverlayText(graphics);

        if (hoverName != null) {
            List<net.minecraft.util.FormattedCharSequence> lines = new ArrayList<>();
            lines.add(hoverName.getVisualOrderText());
            if (hoverDetail != null) lines.add(hoverDetail.getVisualOrderText());
            graphics.setTooltipForNextFrame(lines, hoverX, hoverY);
        }
    }

    private void extractOverlayText(GuiGraphicsExtractor graphics) {
        int left = viewLeft() + 4;
        int top = viewTop() + 3;

        int fieldX = viewLeft() + NAME_X;
        int fieldY = viewTop() + NAME_Y;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, AlchemiaSprites.NAME_FIELD,
                fieldX, fieldY, NAME_W, NAME_H);

        Component recipes = Component.translatable("alchemia.gui.recipes", known.size(), RecipeDiscovery.total());
        graphics.text(this.font, recipes, viewLeft() + VIEW_WIDTH - 4 - this.font.width(recipes), top, INK_FAINT, false);

        Component zoomLabel = Component.translatable("alchemia.gui.zoom", Math.round(zoom * 100));
        graphics.text(this.font, zoomLabel, left, viewTop() + VIEW_HEIGHT - 11, INK_FAINT, false);

        int queued = queuedIngredients().size();
        if (queued > 0) {
            Component queuedLabel = Component.translatable("alchemia.gui.queued", queued);
            graphics.text(this.font, queuedLabel, this.leftPos + VIEW_INSET_X, this.topPos + BUTTON_Y + 5, INK_FAINT, false);
        }
    }

    private void extractMap(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (!PotionMap.isReady()) return;

        TileEntityAlchemyCauldron cauldron = this.getMenu().getCauldron();
        double originX = screenX(cauldron.getXAlignment());
        double originY = screenY(cauldron.getYAlignment());

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

            int nx = (int) Math.round(screenX(entry.x()));
            int ny = (int) Math.round(screenY(entry.y()));

            if (nx < left - node || nx > right + node || ny < top - node || ny > bottom + node) continue;

            PotionEnum recipe = entry.effect().getRecipe();
            boolean discovered = recipe != null && known.contains(recipe);

            drawEffectAura(graphics, nx, ny, entry, discovered);
            drawThread(graphics, (int) Math.round(originX), (int) Math.round(originY), nx, ny, thickness);

            if (discovered) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Hud.getMobEffectSprite(entry.effect().getEffect()),
                        nx - half, ny - half, node, node, -1);
            } else {
                drawUnknownSymbol(graphics, nx, ny, node);
            }


            if (discovered && zoom >= LABEL_ZOOM) {
                Component name = entry.effect().getEffect().value().getDisplayName();
                graphics.text(this.font, name, nx - this.font.width(name) / 2, ny + half + 2, INK, false);
            }

            if (mouseX >= nx - half && mouseX <= nx + half && mouseY >= ny - half && mouseY <= ny + half) {
                hoverX = mouseX;
                hoverY = mouseY;
                if (discovered) {
                    hoverName = entry.effect().getEffect().value().getDisplayName();
                    hoverDetail = Component.translatable("alchemia.gui.potency",
                            entry.effect().getStrength() + 1, entry.effect().getDuration() / 20);
                } else {
                    hoverName = Component.translatable("alchemia.gui.unknown");
                    hoverDetail = Component.translatable("alchemia.gui.unknown.hint");
                }
            }
        }

        extractPlan(graphics, cauldron, (int) Math.round(originX), (int) Math.round(originY), node);

        double markerX = originX;
        double markerY = originY;
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

    private void extractPlan(GuiGraphicsExtractor graphics, TileEntityAlchemyCauldron cauldron, int originX, int originY, int node) {
        List<ItemStack> queued = queuedIngredients();
        if (queued.isEmpty()) return;

        int x = cauldron.getXAlignment();
        int y = cauldron.getYAlignment();
        int prevX = originX;
        int prevY = originY;
        boolean stalled = false;
        boolean doomed = false;
        boolean stranded = false;

        Set<Holder<MobEffect>> held = new HashSet<>();
        for (MobEffectInstance instance : cauldron.getEffectList()) held.add(instance.getEffect());

        List<int[]> pickups = new ArrayList<>();
        int simulatedInstability = cauldron.getInstability();

        for (ItemStack queuedStack : queued) {
            if (!TileEntityAlchemyCauldron.isBrewingInput(queuedStack)) continue;

            PotionMap.PotionPath path = map().walk(x, y, queuedStack);
            if (path.stalled()) {
                stalled = true;
                if (path.teleport()) drawOffMapAttempt(graphics, x, y, queuedStack, prevX, prevY, node);
                continue;
            }

            if (path.dead()) {
                for (int[] cell : map().teleportCurve(x, y, queuedStack)) {
                    int px = (int) Math.round(screenX(cell[0]));
                    int py = (int) Math.round(screenY(cell[1]));
                    drawSegment(graphics, prevX, prevY, px, py, true);
                    prevX = px;
                    prevY = py;
                }

                int[] grave = path.cells().get(0);
                int graveX = (int) Math.round(screenX(grave[0]));
                int graveY = (int) Math.round(screenY(grave[1]));

                simulatedInstability++;
                if (simulatedInstability >= cauldron.getInstabilityCap()) {
                    prevX = graveX;
                    prevY = graveY;
                    doomed = true;
                    break;
                }

                drawPlanMarker(graphics, graveX, graveY, node, BLOCKED_RGB);
                prevX = graveX;
                prevY = graveY;
                x = grave[0];
                y = grave[1];
                stranded = true;
                continue;
            }

            List<int[]> trail = path.teleport()
                    ? map().teleportCurve(x, y, queuedStack)
                    : path.cells();

            for (int[] cell : trail) {
                int px = (int) Math.round(screenX(cell[0]));
                int py = (int) Math.round(screenY(cell[1]));
                drawSegment(graphics, prevX, prevY, px, py, path.teleport());
                if (!path.teleport()) drawNodeDot(graphics, px, py, cell[0], cell[1]);
                prevX = px;
                prevY = py;
            }

            int[] end = path.end(x, y);

            if (!path.teleport()) {
                List<int[]> cells = path.cells();
                for (int i = 0; i < cells.size() - 1; i++) {
                    PotionMap.PotionEffectPosition passed =
                            map().getEffectAt(cells.get(i)[0], cells.get(i)[1]);
                    if (passed != null && passed.getEffect() != null && held.add(passed.getEffect())) {
                        pickups.add(cells.get(i));
                    }
                }
            }

            PotionMap.PotionEffectPosition arrival = map().getEffectPotion(end);
            if (arrival.getEffect() != null && held.add(arrival.getEffect())) {
                pickups.add(end);
            }

            if (path.teleport()) {
                int px = (int) Math.round(screenX(end[0]));
                int py = (int) Math.round(screenY(end[1]));
                drawSegment(graphics, prevX, prevY, px, py, true);
                prevX = px;
                prevY = py;
            }

            x = end[0];
            y = end[1];
        }

        int predicted = cauldron.previewPotency(queued);
        boolean overloaded = predicted > TileEntityAlchemyCauldron.maxPotency();

        int half = Math.max(3, node / 3);
        int ghost = stalled || doomed || overloaded || stranded ? BLOCKED_RGB : GHOST_RGB;

        if (!doomed && !overloaded) {
            for (int[] cell : pickups) {
                drawPlanMarker(graphics, (int) Math.round(screenX(cell[0])),
                        (int) Math.round(screenY(cell[1])), node, GAINED_RGB);
            }
        }

        drawPlanMarker(graphics, prevX, prevY, node, ghost);

        if (doomed || overloaded) {
            graphics.item(new ItemStack(Items.SKELETON_SKULL), prevX - 8, prevY - 8);
            Component warning = doomed
                    ? Component.translatable("alchemia.gui.doomed")
                    : Component.translatable("alchemia.gui.overloaded", predicted, TileEntityAlchemyCauldron.maxPotency());
            int warnX = Mth.clamp(prevX, viewLeft() + this.font.width(warning) / 2 + 2,
                    viewLeft() + VIEW_WIDTH - this.font.width(warning) / 2 - 2);
            graphics.text(this.font, warning, warnX - this.font.width(warning) / 2, prevY + half + 3,
                    ARGB.opaque(BLOCKED_RGB), false);
            return;
        }

        if (stranded) {
            graphics.item(new ItemStack(Items.SKELETON_SKULL), prevX - 8, prevY - 8);
            Component warning = Component.translatable("alchemia.gui.stranded",
                    simulatedInstability, cauldron.getInstabilityCap());
            int warnX = Mth.clamp(prevX, viewLeft() + this.font.width(warning) / 2 + 2,
                    viewLeft() + VIEW_WIDTH - this.font.width(warning) / 2 - 2);
            graphics.text(this.font, warning, warnX - this.font.width(warning) / 2, prevY + half + 3,
                    ARGB.opaque(BLOCKED_RGB), false);
            return;
        }

        if (PotionMap.isReady()) {
            PotionMap.PotionEffectPosition landing = map().getEffectPotion(new int[]{x, y});
            Component label = landing.getEffect() == null
                    ? Component.translatable("alchemia.gui.predicted.none")
                    : landing.getEffect().value().getDisplayName();
            int labelX = Mth.clamp(prevX, viewLeft() + this.font.width(label) / 2 + 2,
                    viewLeft() + VIEW_WIDTH - this.font.width(label) / 2 - 2);
            graphics.text(this.font, label, labelX - this.font.width(label) / 2, prevY + half + 3, INK, false);
        }
    }

    private void drawOffMapAttempt(GuiGraphicsExtractor graphics, int x, int y, ItemStack stack,
                                   int fromX, int fromY, int node) {
        List<int[]> curve = map().teleportCurve(x, y, stack);
        if (curve.isEmpty()) return;

        int px = fromX;
        int py = fromY;

        for (int[] cell : curve) {
            int cx = (int) Math.round(screenX(cell[0]));
            int cy = (int) Math.round(screenY(cell[1]));
            drawSegment(graphics, px, py, cx, cy, true);
            px = cx;
            py = cy;
        }

        int half = Math.max(3, node / 3);
        int blocked = ARGB.color(220, BLOCKED_RGB);

        graphics.fill(px - half, py - 1, px + half, py + 1, blocked);
        graphics.fill(px - 1, py - half, px + 1, py + half, blocked);
        graphics.outline(px - half - 1, py - half - 1, half * 2 + 2, half * 2 + 2, ARGB.color(120, BLOCKED_RGB));

        Component label = Component.translatable("alchemia.gui.offmap");
        int labelX = Mth.clamp(px, viewLeft() + this.font.width(label) / 2 + 2,
                viewLeft() + VIEW_WIDTH - this.font.width(label) / 2 - 2);
        graphics.text(this.font, label, labelX - this.font.width(label) / 2, py + half + 3,
                ARGB.opaque(BLOCKED_RGB), false);
    }

    private void drawPlanMarker(GuiGraphicsExtractor graphics, int x, int y, int node, int rgb) {
        int half = Math.max(3, node / 3);
        graphics.outline(x - half, y - half, half * 2, half * 2, ARGB.color(200, rgb));
        graphics.outline(x - half - 1, y - half - 1, half * 2 + 2, half * 2 + 2, ARGB.color(80, rgb));
    }

    private void drawNodeDot(GuiGraphicsExtractor graphics, int px, int py, int cellX, int cellY) {
        if (map().getEffectAt(cellX, cellY) == null) return;
        graphics.fill(px - 2, py - 2, px + 2, py + 2, ARGB.color(220, 0x3C6B2A));
    }

    private void drawSegment(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, boolean dashed) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length < 1) return;

        int steps = (int) Math.min(360, Math.ceil(dashed ? length / 2.0 : length));
        for (int i = 0; i <= steps; i++) {
            if (dashed && (i / 3) % 2 == 1) continue;
            double t = (double) i / steps;
            int px = (int) Math.round(x0 + dx * t);
            int py = (int) Math.round(y0 + dy * t);
            graphics.fill(px - 1, py - 1, px + 1, py + 1, ARGB.color(190, GHOST_RGB));
        }
    }

    private void drawMapEdge(GuiGraphicsExtractor graphics, int left, int top, int right, int bottom) {
        int size = map().getSize();

        int x0 = Mth.clamp((int) Math.round(screenX(-0.5)), left, right);
        int x1 = Mth.clamp((int) Math.round(screenX(size - 0.5)), left, right);
        int y0 = Mth.clamp((int) Math.round(screenY(-0.5)), top, bottom);
        int y1 = Mth.clamp((int) Math.round(screenY(size - 0.5)), top, bottom);

        if (y0 > top) graphics.fill(left, top, right, y0, EDGE_FILL);
        if (y1 < bottom) graphics.fill(left, y1, right, bottom, EDGE_FILL);
        if (x0 > left) graphics.fill(left, y0, x0, y1, EDGE_FILL);
        if (x1 < right) graphics.fill(x1, y0, right, y1, EDGE_FILL);

        if (x0 > left) graphics.fill(x0 - 1, y0, x0 + 1, y1, EDGE_LINE);
        if (x1 < right) graphics.fill(x1 - 1, y0, x1 + 1, y1, EDGE_LINE);
        if (y0 > top) graphics.fill(x0, y0 - 1, x1, y0 + 1, EDGE_LINE);
        if (y1 < bottom) graphics.fill(x0, y1 - 1, x1, y1 + 1, EDGE_LINE);
    }

    private void extractDeadzones(GuiGraphicsExtractor graphics, int left, int top, int right, int bottom) {
        if (!PotionMap.isReady()) return;

        for (long packed : map().deadzoneCells()) {
            int cx = (int) (packed >> 32);
            int cy = (int) packed;

            int x0 = (int) Math.round(screenX(cx - 0.5));
            int x1 = (int) Math.round(screenX(cx + 0.5));
            int y0 = (int) Math.round(screenY(cy - 0.5));
            int y1 = (int) Math.round(screenY(cy + 0.5));
            if (x1 <= x0) x1 = x0 + 1;
            if (y1 <= y0) y1 = y0 + 1;
            if (x0 > right || x1 < left || y0 > bottom || y1 < top) continue;

            graphics.fill(x0, y0, x1, y1, ARGB.color(70, BLOCKED_RGB));

            if (!map().isDeadzone(cx, cy - 1)) graphics.fill(x0, y0, x1, y0 + 1, ARGB.color(150, BLOCKED_RGB));
            if (!map().isDeadzone(cx, cy + 1)) graphics.fill(x0, y1 - 1, x1, y1, ARGB.color(150, BLOCKED_RGB));
            if (!map().isDeadzone(cx - 1, cy)) graphics.fill(x0, y0, x0 + 1, y1, ARGB.color(150, BLOCKED_RGB));
            if (!map().isDeadzone(cx + 1, cy)) graphics.fill(x1 - 1, y0, x1, y1, ARGB.color(150, BLOCKED_RGB));
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
        ItemStack marker = new ItemStack(cauldron.hasGunpowder() ? Items.SPLASH_POTION : Items.POTION);
        marker.set(DataComponents.POTION_CONTENTS,
                new PotionContents(Optional.empty(), Optional.of(cauldron.getPotionColor()), List.of(), Optional.empty()));
        graphics.item(marker, -8, -8);

        pose.popMatrix();
    }

    private void drawEffectAura(GuiGraphicsExtractor graphics, int cx, int cy,
                                PotionMap.MapEntry entry, boolean discovered) {
        double cellW = SPACING_X * zoom;
        double cellH = SPACING_Y * zoom;
        if (cellW < 2 || cellH < 2) return;

        double reachX = (PotionMap.EFFECT_RADIUS + 0.5) * cellW;
        double reachY = (PotionMap.EFFECT_RADIUS + 0.5) * cellH;
        if (cx + reachX < viewLeft() || cx - reachX > viewLeft() + VIEW_WIDTH
                || cy + reachY < viewTop() || cy - reachY > viewTop() + VIEW_HEIGHT) return;

        int rgb = discovered ? entry.effect().getEffect().value().getColor() : AURA_UNKNOWN_RGB;
        double pulse = (Math.sin(Util.getMillis() / 520.0 + cx * 0.35) + 1.0) * 0.5;
        double glow = 0.72 + pulse * 0.28;

        int radius = PotionMap.EFFECT_RADIUS;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                int ring = Math.max(Math.abs(dx), Math.abs(dy));

                int cellX = entry.x() + dx;
                int cellY = entry.y() + dy;

                int x0 = (int) Math.round(screenX(cellX - 0.5));
                int x1 = (int) Math.round(screenX(cellX + 0.5));
                int y0 = (int) Math.round(screenY(cellY - 0.5));
                int y1 = (int) Math.round(screenY(cellY + 0.5));
                if (x1 <= x0) x1 = x0 + 1;
                if (y1 <= y0) y1 = y0 + 1;

                int alpha = (int) Math.round(AURA_CORE_ALPHA / (1.0 + ring) * glow);
                graphics.fill(x0, y0, x1, y1, ARGB.color(alpha, rgb));

                int edge = ARGB.color(Math.min(255, (int) Math.round(alpha * 2.4)), rgb);
                if (outsideReach(dx, dy - 1, radius)) dottedEdge(graphics, x0, y0, x1, y0 + 1, true, edge);
                if (outsideReach(dx, dy + 1, radius)) dottedEdge(graphics, x0, y1 - 1, x1, y1, true, edge);
                if (outsideReach(dx - 1, dy, radius)) dottedEdge(graphics, x0, y0, x0 + 1, y1, false, edge);
                if (outsideReach(dx + 1, dy, radius)) dottedEdge(graphics, x1 - 1, y0, x1, y1, false, edge);
            }
        }
    }

    private void dottedEdge(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1,
                            boolean horizontal, int colour) {
        int length = horizontal ? x1 - x0 : y1 - y0;

        for (int i = 0; i < length; i++) {
            if ((i / AURA_DASH) % 2 == 1) continue;

            if (horizontal) {
                graphics.fill(x0 + i, y0, x0 + i + 1, y1, colour);
            } else {
                graphics.fill(x0, y0 + i, x1, y0 + i + 1, colour);
            }
        }
    }

    private static boolean outsideReach(int dx, int dy, int radius) {
        return Math.max(Math.abs(dx), Math.abs(dy)) > radius;
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
