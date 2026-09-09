package muddykat.alchemia.client.gui;

import muddykat.alchemia.common.blocks.tileentity.container.AlchemyMachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class AlchemyMachineScreen extends AbstractContainerScreen<AlchemyMachineMenu> {

    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 202;

    private static final int INK = 0xFF4A3A28;
    private static final int INK_FAINT = 0xFF7A6A52;

    private static final int FLAME_X = 49;
    private static final int FLAME_Y = 72;

    private static final int COOK_X = 70;
    private static final int COOK_Y = 76;

    public AlchemyMachineScreen(AlchemyMachineMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, IMAGE_WIDTH, IMAGE_HEIGHT);
        this.inventoryLabelY = AlchemyMachineMenu.INVENTORY_Y - 12;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int left = this.leftPos;
        int top = this.topPos;

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, AlchemiaSprites.ALCHEMY_MACHINE,
                left, top, IMAGE_WIDTH, IMAGE_HEIGHT);

        for (Slot slot : this.menu.slots) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, AlchemiaSprites.SLOT,
                    left + slot.x - 1, top + slot.y - 1, AlchemiaSprites.SLOT_SIZE, AlchemiaSprites.SLOT_SIZE);
        }

        if (this.menu.getPotionSlots() == 0) {
            Component hint = Component.translatable("alchemia.gui.machine.no_upgrade");
            graphics.text(this.font, hint, left + (IMAGE_WIDTH - this.font.width(hint)) / 2,
                    top + AlchemyMachineMenu.POTION_Y + 4, INK_FAINT, false);
        }

        drawBurn(graphics, left, top);
        drawCook(graphics, left, top);
    }

    private void drawBurn(GuiGraphicsExtractor graphics, int left, int top) {
        int x = left + FLAME_X;
        int y = top + FLAME_Y;

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, AlchemiaSprites.BURN_TRACK,
                x, y, AlchemiaSprites.BURN_W, AlchemiaSprites.BURN_H);

        int duration = this.menu.getLitDuration();
        if (duration <= 0 || !this.menu.isLit()) return;

        int filled = Math.clamp(this.menu.getLitTime() * AlchemiaSprites.BURN_H / duration, 0, AlchemiaSprites.BURN_H);
        if (filled <= 0) return;

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, AlchemiaSprites.BURN_PROGRESS,
                AlchemiaSprites.BURN_W, AlchemiaSprites.BURN_H, 0, AlchemiaSprites.BURN_H - filled,
                x, y + AlchemiaSprites.BURN_H - filled, AlchemiaSprites.BURN_W, filled);
    }

    private void drawCook(GuiGraphicsExtractor graphics, int left, int top) {
        int x = left + COOK_X;
        int y = top + COOK_Y;

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, AlchemiaSprites.COOK_TRACK,
                x, y, AlchemiaSprites.COOK_W, AlchemiaSprites.COOK_H);

        int duration = this.menu.getCookDuration();
        if (duration <= 0) return;

        int filled = Math.clamp(this.menu.getCookTime() * AlchemiaSprites.COOK_W / duration, 0, AlchemiaSprites.COOK_W);
        if (filled <= 0) return;

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, AlchemiaSprites.COOK_PROGRESS,
                AlchemiaSprites.COOK_W, AlchemiaSprites.COOK_H, 0, 0,
                x, y, filled, AlchemiaSprites.COOK_H);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.title, this.leftPos + this.titleLabelX, this.topPos + this.titleLabelY, INK, false);
        graphics.text(this.font, this.playerInventoryTitle, this.leftPos + this.inventoryLabelX,
                this.topPos + this.inventoryLabelY, INK, false);
    }
}
