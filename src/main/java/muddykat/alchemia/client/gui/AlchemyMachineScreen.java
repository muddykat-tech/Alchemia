package muddykat.alchemia.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import muddykat.alchemia.common.blocks.tileentity.container.AlchemyMachineMenu;
import muddykat.alchemia.common.blocks.tileentity.container.AlchemyMachineMenu.UpgradePanel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.List;

public class AlchemyMachineScreen extends AbstractContainerScreen<AlchemyMachineMenu> {

    private static final int INK = 0xFF4A3A28;
    private static final int INK_FAINT = 0xFF7A6A52;

    private static final int HINT_Y = 24;

    private static final int FLAME_X = 56;
    private static final int FLAME_Y = 72;

    public static final int COOK_X = 77;
    public static final int COOK_Y = 76;

    public AlchemyMachineScreen(AlchemyMachineMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = AlchemyMachineMenu.GUI_WIDTH;
        this.imageHeight = AlchemyMachineMenu.GUI_HEIGHT;
        this.inventoryLabelY = AlchemyMachineMenu.INVENTORY_Y - 12;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        int left = this.leftPos;
        int top = this.topPos;

        for (UpgradePanel panel : this.menu.getPanels()) {
            AlchemiaSprites.upgradePanel(panel.side())
                    .blit(graphics, left + panel.x(), top + panel.y(), panel.width(), panel.height());
        }

        AlchemiaSprites.ALCHEMY_MACHINE.blit(graphics, left, top, AlchemyMachineMenu.GUI_WIDTH, AlchemyMachineMenu.GUI_HEIGHT);

        for (Slot slot : this.menu.slots) {
            AlchemiaSprites.SLOT.blit(graphics, left + slot.x - 1, top + slot.y - 1, AlchemiaSprites.SLOT_SIZE, AlchemiaSprites.SLOT_SIZE);
        }

        if (this.menu.getPotionSlots() == 0) {
            Component hint = Component.translatable("alchemia.gui.machine.no_upgrade");
            graphics.drawString(this.font, hint, left + (AlchemyMachineMenu.GUI_WIDTH - this.font.width(hint)) / 2,
                    top + HINT_Y, INK_FAINT, false);
        }

        drawBurn(graphics, left, top);
        drawCook(graphics, left, top);
    }

    private void drawBurn(GuiGraphics graphics, int left, int top) {
        int x = left + FLAME_X;
        int y = top + FLAME_Y;

        AlchemiaSprites.BURN_TRACK.blit(graphics, x, y, AlchemiaSprites.BURN_W, AlchemiaSprites.BURN_H);

        int duration = this.menu.getLitDuration();
        if (duration <= 0 || !this.menu.isLit()) return;

        int filled = Mth.clamp(this.menu.getLitTime() * AlchemiaSprites.BURN_H / duration, 0, AlchemiaSprites.BURN_H);
        if (filled <= 0) return;

        AlchemiaSprites.BURN_PROGRESS.blit(graphics, AlchemiaSprites.BURN_W, AlchemiaSprites.BURN_H, 0, AlchemiaSprites.BURN_H - filled,
                x, y + AlchemiaSprites.BURN_H - filled, AlchemiaSprites.BURN_W, filled);
    }

    private void drawCook(GuiGraphics graphics, int left, int top) {
        int x = left + COOK_X;
        int y = top + COOK_Y;

        AlchemiaSprites.COOK_TRACK.blit(graphics, x, y, AlchemiaSprites.COOK_W, AlchemiaSprites.COOK_H);

        int duration = this.menu.getCookDuration();
        if (duration <= 0) return;

        int filled = Mth.clamp(this.menu.getCookTime() * AlchemiaSprites.COOK_W / duration, 0, AlchemiaSprites.COOK_W);
        if (filled <= 0) return;

        AlchemiaSprites.COOK_PROGRESS.blit(graphics, AlchemiaSprites.COOK_W, AlchemiaSprites.COOK_H, 0, 0,
                x, y, filled, AlchemiaSprites.COOK_H);
    }

    public List<Rect2i> getPanelAreas() {
        return this.menu.getPanels().stream()
                .map(panel -> new Rect2i(this.leftPos + panel.x(), this.topPos + panel.y(), panel.width(), panel.height()))
                .toList();
    }

    @Override
    protected boolean hasClickedOutside(double mx, double my, int xo, int yo, int button) {
        if (!super.hasClickedOutside(mx, my, xo, yo, button)) return false;
        for (UpgradePanel panel : this.menu.getPanels()) {
            if (panel.contains(mx - xo, my - yo)) return false;
        }
        return true;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.leftPos + this.titleLabelX, this.topPos + this.titleLabelY, INK, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.leftPos + this.inventoryLabelX,
                this.topPos + this.inventoryLabelY, INK, false);
    }
}
