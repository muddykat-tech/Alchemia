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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.ArrayList;
import java.util.List;
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
    private static final int EFFECTS_ROW_H = 22;

    private static final int VIEW_INSET_X = 22;
    private static final int VIEW_INSET_Y = 16;
    private static final int VIEW_WIDTH = 265;
    private static final int VIEW_HEIGHT = 160;

    private static final int SLOT_ROW_X = 101;
    private static final int SLOT_ROW_Y = 181;
    private static final int INVENTORY_X = 74;
    private static final int INVENTORY_Y = 222;
    private static final int HOTBAR_Y = 280;

    private static final int BUTTON_X = 213;
    private static final int BUTTON_Y = 181;
    private static final int BUTTON_W = 72;
    private static final int BUTTON_H = 18;

    private static final int BUTTON_FILL = 0xFFC9A96E;
    private static final int BUTTON_HOVER = 0xFFDCC08A;
    private static final int BUTTON_DISABLED = 0xFFB6A98C;
    private static final int GHOST_RGB = 0x6A4E2A;
    private static final int BLOCKED_RGB = 0x6B2418;

    private static final int PANEL_FILL = 0xFFE7CD9D;
    private static final int PANEL_EDGE = 0xFF8A6A42;
    private static final int SLOT_FILL = 0xFFC7A876;
    private static final int SLOT_EDGE = 0xFF6B5335;

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
        if (PotionMap.INSTANCE == null) return;
        double limit = PotionMap.INSTANCE.getMaxAlignment() + 8;
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
        extractEffectsPanel(graphics);
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
        if (PotionMap.INSTANCE == null) return;

        TileEntityAlchemyCauldron cauldron = this.getMenu().getCauldron();
        int x = cauldron.getXAlignment();
        int y = cauldron.getYAlignment();
        animStartX = x;
        animStartY = y;

        for (ItemStack stack : queuedIngredients()) {
            PotionMap.PotionPath path = PotionMap.INSTANCE.walk(x, y, stack);
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

    private void extractEffectsPanel(GuiGraphicsExtractor graphics) {
        TileEntityAlchemyCauldron cauldron = this.getMenu().getCauldron();
        List<MobEffectInstance> effects = new ArrayList<>(cauldron.getEffectList());

        int panelX = this.leftPos + IMAGE_WIDTH + EFFECTS_GAP;
        int panelY = this.topPos + EFFECTS_Y;
        int panelH = 30 + TileEntityAlchemyCauldron.MAX_EFFECTS * EFFECTS_ROW_H + 4;

        graphics.fill(panelX, panelY, panelX + EFFECTS_W, panelY + panelH, PANEL_FILL);
        graphics.outline(panelX, panelY, EFFECTS_W, panelH, PANEL_EDGE);

        drawCentredWrapped(graphics, Component.translatable("alchemia.gui.effects.title"),
                panelX + EFFECTS_W / 2, panelY + 5, EFFECTS_W - 8, INK);

        int swatch = cauldron.getWaterLevel() > 0 ? cauldron.getPotionColor() : 0x7A6A50;
        graphics.fill(panelX + 4, panelY + 17, panelX + EFFECTS_W - 4, panelY + 21, ARGB.opaque(swatch));
        graphics.outline(panelX + 4, panelY + 17, EFFECTS_W - 8, 4, PANEL_EDGE);

        int rowY = panelY + 26;

        if (cauldron.isSpoiled()) {
            graphics.item(new ItemStack(Items.SKELETON_SKULL), panelX + (EFFECTS_W - 16) / 2, rowY + 2);
            drawCentredWrapped(graphics, Component.translatable("alchemia.gui.effects.ruined"),
                    panelX + EFFECTS_W / 2, rowY + 22, EFFECTS_W - 8, ARGB.opaque(BLOCKED_RGB));
            return;
        }

        if (effects.isEmpty()) {
            drawCentredWrapped(graphics, Component.translatable(cauldron.getWaterLevel() > 0
                            ? "alchemia.gui.effects.none" : "alchemia.gui.effects.dry"),
                    panelX + EFFECTS_W / 2, rowY + 6, EFFECTS_W - 8, INK_FAINT);
            return;
        }

        for (MobEffectInstance instance : effects) {
            slotBackdrop(graphics, panelX + 4, rowY);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Hud.getMobEffectSprite(instance.getEffect()),
                    panelX + 5, rowY + 1, 16, 16);

            Component name = instance.getEffect().value().getDisplayName();
            Component detail = Component.translatable("alchemia.gui.potency",
                    instance.getAmplifier() + 1, instance.getDuration() / 20);

            graphics.text(this.font, this.font.plainSubstrByWidth(name.getString(), EFFECTS_W - 30),
                    panelX + 24, rowY + 2, INK, false);
            graphics.text(this.font, detail, panelX + 24, rowY + 11, INK_FAINT, false);

            rowY += EFFECTS_ROW_H;
        }

        if (effects.size() >= TileEntityAlchemyCauldron.MAX_EFFECTS) {
            drawCentredWrapped(graphics, Component.translatable("alchemia.gui.effects.full"),
                    panelX + EFFECTS_W / 2, panelY + panelH - 11, EFFECTS_W - 8, ARGB.opaque(BLOCKED_RGB));
        }
    }

    private void extractBrewButton(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        boolean ready = canBrew();
        int x = this.leftPos + BUTTON_X;
        int y = this.topPos + BUTTON_Y;

        int fill = !ready ? BUTTON_DISABLED : (overButton(mouseX, mouseY) ? BUTTON_HOVER : BUTTON_FILL);
        graphics.fill(x, y, x + BUTTON_W, y + BUTTON_H, fill);
        graphics.outline(x, y, BUTTON_W, BUTTON_H, SLOT_EDGE);

        Component label = Component.translatable("alchemia.gui.brew");
        graphics.text(this.font, label, x + (BUTTON_W - this.font.width(label)) / 2, y + 5, ready ? INK : INK_FAINT, false);
    }

    private boolean canBrew() {
        TileEntityAlchemyCauldron cauldron = this.getMenu().getCauldron();
        return cauldron.getWaterLevel() > 0 && !queuedIngredients().isEmpty();
    }

    private List<ItemStack> queuedIngredients() {
        List<ItemStack> queued = new ArrayList<>();
        for (int slot = 0; slot < AlchemicalCauldronMenu.INGREDIENT_SLOTS; slot++) {
            ItemStack stack = this.getMenu().getSlot(slot).getItem();
            if (stack.getItem() instanceof ItemIngredient) {
                for (int i = 0; i < stack.getCount(); i++) queued.add(stack.copyWithCount(1));
            }
        }
        return queued;
    }

    private void extractSlotPanels(GuiGraphicsExtractor graphics) {
        for (int slot = 0; slot < AlchemicalCauldronMenu.INGREDIENT_SLOTS; slot++) {
            slotBackdrop(graphics, this.leftPos + SLOT_ROW_X + slot * 18, this.topPos + SLOT_ROW_Y);
        }

        int panelX = this.leftPos + INVENTORY_X - 6;
        int panelY = this.topPos + INVENTORY_Y - 6;
        int panelW = 9 * 18 + 12;
        int panelH = (HOTBAR_Y + 18) - INVENTORY_Y + 12;

        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, PANEL_FILL);
        graphics.outline(panelX, panelY, panelW, panelH, PANEL_EDGE);

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
        graphics.fill(x, y, x + 18, y + 18, SLOT_FILL);
        graphics.outline(x, y, 18, 18, SLOT_EDGE);
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
        graphics.fill(fieldX, fieldY, fieldX + NAME_W, fieldY + NAME_H, 0x40FFF6DC);
        graphics.outline(fieldX, fieldY, NAME_W, NAME_H, 0x559A7A52);

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
        if (PotionMap.INSTANCE == null) return;

        TileEntityAlchemyCauldron cauldron = this.getMenu().getCauldron();
        double originX = screenX(cauldron.getXAlignment());
        double originY = screenY(cauldron.getYAlignment());

        int left = viewLeft();
        int top = viewTop();
        int right = left + VIEW_WIDTH;
        int bottom = top + VIEW_HEIGHT;

        graphics.enableScissor(left, top, right, bottom);

        extractDeadzones(graphics, left, top, right, bottom);

        int node = Math.max(6, (int) Math.round(NODE_BASE * Mth.clamp(zoom, 0.55, 2.0)));
        int half = node / 2;
        int thickness = Math.max(1, (int) Math.round(2 * Mth.clamp(zoom, 0.5, 2.0)));

        for (PotionMap.MapEntry entry : PotionMap.INSTANCE.entries()) {
            if (entry.effect().getEffect() == null) continue;

            int nx = (int) Math.round(screenX(entry.x()));
            int ny = (int) Math.round(screenY(entry.y()));

            if (nx < left - node || nx > right + node || ny < top - node || ny > bottom + node) continue;

            drawThread(graphics, (int) Math.round(originX), (int) Math.round(originY), nx, ny, thickness);

            PotionEnum recipe = entry.effect().getRecipe();
            boolean discovered = recipe != null && known.contains(recipe);

            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Hud.getMobEffectSprite(entry.effect().getEffect()),
                    nx - half, ny - half, node, node, discovered ? -1 : UNKNOWN_TINT);

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

        for (ItemStack queuedStack : queued) {
            if (!(queuedStack.getItem() instanceof ItemIngredient)) continue;

            PotionMap.PotionPath path = PotionMap.INSTANCE.walk(x, y, queuedStack);
            if (path.stalled()) {
                stalled = true;
                continue;
            }

            if (path.dead()) {
                for (int[] cell : PotionMap.INSTANCE.teleportCurve(x, y, queuedStack)) {
                    int px = (int) Math.round(screenX(cell[0]));
                    int py = (int) Math.round(screenY(cell[1]));
                    drawSegment(graphics, prevX, prevY, px, py, true);
                    prevX = px;
                    prevY = py;
                }

                int[] grave = path.cells().get(0);
                prevX = (int) Math.round(screenX(grave[0]));
                prevY = (int) Math.round(screenY(grave[1]));
                doomed = true;
                break;
            }

            List<int[]> trail = path.teleport()
                    ? PotionMap.INSTANCE.teleportCurve(x, y, queuedStack)
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

        int predicted = cauldron.previewEffectCount(queued);
        boolean overloaded = predicted > TileEntityAlchemyCauldron.MAX_EFFECTS;

        int half = Math.max(3, node / 3);
        int ghost = stalled || doomed || overloaded ? BLOCKED_RGB : GHOST_RGB;
        graphics.outline(prevX - half, prevY - half, half * 2, half * 2, ARGB.color(200, ghost));
        graphics.outline(prevX - half - 1, prevY - half - 1, half * 2 + 2, half * 2 + 2, ARGB.color(80, ghost));

        if (doomed || overloaded) {
            graphics.item(new ItemStack(Items.SKELETON_SKULL), prevX - 8, prevY - 8);
            Component warning = Component.translatable(doomed
                    ? "alchemia.gui.doomed" : "alchemia.gui.overloaded", predicted);
            int warnX = Mth.clamp(prevX, viewLeft() + this.font.width(warning) / 2 + 2,
                    viewLeft() + VIEW_WIDTH - this.font.width(warning) / 2 - 2);
            graphics.text(this.font, warning, warnX - this.font.width(warning) / 2, prevY + half + 3,
                    ARGB.opaque(BLOCKED_RGB), false);
            return;
        }

        if (PotionMap.INSTANCE != null) {
            PotionMap.PotionEffectPosition landing = PotionMap.INSTANCE.getEffectPotion(new int[]{x, y});
            Component label = landing.getEffect() == null
                    ? Component.translatable("alchemia.gui.predicted.none")
                    : landing.getEffect().value().getDisplayName();
            int labelX = Mth.clamp(prevX, viewLeft() + this.font.width(label) / 2 + 2,
                    viewLeft() + VIEW_WIDTH - this.font.width(label) / 2 - 2);
            graphics.text(this.font, label, labelX - this.font.width(label) / 2, prevY + half + 3, INK, false);
        }
    }

    private void drawNodeDot(GuiGraphicsExtractor graphics, int px, int py, int cellX, int cellY) {
        if (PotionMap.INSTANCE.getEffectAt(cellX, cellY) == null) return;
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

    private void extractDeadzones(GuiGraphicsExtractor graphics, int left, int top, int right, int bottom) {
        if (PotionMap.INSTANCE == null) return;

        for (long packed : PotionMap.INSTANCE.deadzoneCells()) {
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

            if (!PotionMap.INSTANCE.isDeadzone(cx, cy - 1)) graphics.fill(x0, y0, x1, y0 + 1, ARGB.color(150, BLOCKED_RGB));
            if (!PotionMap.INSTANCE.isDeadzone(cx, cy + 1)) graphics.fill(x0, y1 - 1, x1, y1, ARGB.color(150, BLOCKED_RGB));
            if (!PotionMap.INSTANCE.isDeadzone(cx - 1, cy)) graphics.fill(x0, y0, x0 + 1, y1, ARGB.color(150, BLOCKED_RGB));
            if (!PotionMap.INSTANCE.isDeadzone(cx + 1, cy)) graphics.fill(x1 - 1, y0, x1, y1, ARGB.color(150, BLOCKED_RGB));
        }
    }

    private void drawBrewMarker(GuiGraphicsExtractor graphics, int x, int y, TileEntityAlchemyCauldron cauldron, int node, double scale, double spin) {
        if (scale <= 0.01) return;

        double pulse = (Math.sin(Util.getMillis() / 380.0) + 1.0) * 0.5;
        int radius = (int) Math.round(node * 0.55 + pulse * 3.0);
        int alpha = (int) Math.round(70 + pulse * 90);

        ItemStack marker = new ItemStack(Items.POTION);
        marker.set(DataComponents.POTION_CONTENTS,
                new PotionContents(Optional.empty(), Optional.of(cauldron.getPotionColor()), List.of(), Optional.empty()));

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        pose.rotate((float) spin);
        pose.scale((float) scale, (float) scale);

        graphics.outline(-radius, -radius, radius * 2, radius * 2, ARGB.color(alpha, 0x2A1F14));
        graphics.outline(-radius - 1, -radius - 1, radius * 2 + 2, radius * 2 + 2, ARGB.color(alpha / 3, 0x2A1F14));
        graphics.item(marker, -8, -8);

        pose.popMatrix();
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
